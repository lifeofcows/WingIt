$(document).ready(function() {
    let urlPrefix = "/?url=";

    $(".searchButton").on("click", function() {
        $(".results").css("display", "none");
        let targetURL = $(".searchTerm").val();
        if (targetURL != "") {
            console.log("Requesting url: " + targetURL);
            $.get(urlPrefix + targetURL, function(data) {
                console.log("Received data with status code: " + data.statusCode);
                $(".results").css("display", "inline-block");
                if (data.statusCode == 200 && data.wing) {
                    $(".resultsText").text("Wing: " + data.wing);
                } else {
                    $(".resultsText").text("Something went wrong!");
                }
            });
        } else {
            alert("Psst... you might want to enter a query!");
        }
    });

});