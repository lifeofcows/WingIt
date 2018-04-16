$(document).ready(function() {
    let urlPrefix = "/?url=";

    $(".searchButton").on("click", function() {
        clearData();
        let targetURL = $(".searchTerm").val();
        if (targetURL != "") {
            console.log("Requesting url: " + targetURL);
            $.get(urlPrefix + targetURL, function(data) {
                if (data.statusCode) {
                    console.log("Received data with status code: " + data.statusCode);
                    // presentData(data);
                    var temp = {"statusCode": 200,
                                "wing": "left",
                                "recommendations": {
                                    "left": ["link1", "link2", "link3"],
                                    "centrist": ["link1", "link2", "link3"],
                                    "right": ["link1", "link2", "link3"]
                                }
                            }
                    presentData(temp);
                } else {
                    presentData("{\"statusCode\": \"500\"}");
                }
            });
        } else {
            alert("Psst... you might want to enter a query!");
        }
    });

    function presentData(data) {
        console.log("Presenting data");
        $(".results").css("display", "inline-block");
        if (data.statusCode == 200 && data.wing) {
            console.log("Valid status code and wing");
            if (data.wing == "left" || data.wing == "right") {
                $(".resultsText").text("This article is " + data.wing.toUpperCase() + "-LEANING");
            } else {
                $(".resultsText").text("This article is " + data.wing.toUpperCase());
            }

            if (data.recommendations) {
                console.log("Recommendations found");

                var html = "<table border style=\"margin-left: auto; margin-right: auto; width: 80%;\"> <tr> <th> Wing </th> <th> Recommendation </th> </tr> ";

                Object.keys(data.recommendations).forEach(function(recommendationWing) {

                    console.log("Wing: " + recommendationWing);
                    console.log("Data: " + data.recommendations[recommendationWing]);
                    html += "<tr> <td> " + recommendationWing + " </td> <td> <ul> ";

                    data.recommendations[recommendationWing].forEach(function(recommendationWing) {

                        console.log("Wing: " + recommendationWing);
                        html += "<li> " + recommendationWing + " </li> ";

                    });

                    html += "</tr> ";
                    
                });

                html += "</table>";

                $(".recommendations-table").html(html);
            }

        } else {
            $(".resultsText").text("Something went wrong! Please try again later.");
        }
    }

    function clearData() {
        $(".recommendations-table").html("");
        $(".results").css("display", "none");
    }

});