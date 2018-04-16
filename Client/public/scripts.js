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
                    presentData(data);
                    // var test = {"statusCode": 200,
                    //             "wing": "left",
                    //             "wingPercentage": 27,
                    //             "recommendations": {
                    //                 "left": ["link1", "link2", "link3"],
                    //                 "centrist": ["link1", "link2", "link3"],
                    //                 "right": ["link1", "link2", "link3"]
                    //             }
                    //         }
                    // presentData(test);
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

            var wingPercentage = 0;
            if (data.wingPercentage) {
                wingPercentage = data.wingPercentage
            } else {
                if (data.wing == "left") {
                    wingPercentage = 25;
                } else if (data.wing == "right") {
                    wingPercentage = 75;
                } else {
                    wingPercentage = 50;
                }
            }

            var g = new JustGage({
                id: "gauge",
                value: wingPercentage,
                min: 0,
                max: 100,
                title: "Political Leaning: " + data.wing.charAt(0).toUpperCase() + data.wing.slice(1),
                hideMinMax: true,
                levelColors: ["#ff0000", "#00bc16", "#3399ff"]
            });

            if (data.recommendations) {
                console.log("Recommendations found");

                var html = "<table border style=\"margin-left: auto; margin-right: auto; width: 80%;\"> ";
                html += "<tr> <th> Wing </th> <th> Recommendation </th> </tr> ";

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
        $("#gauge").empty();
        $(".recommendations-table").html("");
        $(".results").css("display", "none");
    }

});