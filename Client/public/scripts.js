$(document).ready(function() {
    let baseURL = "http://localhost:8080/WingIt/rest/rs";

    $(".searchButton").on("click", function() {
        let targetURL = $(".searchTerm").val();
        if (targetURL != "") {
            let encodedTargetURL = encodeURIComponent(targetURL);
            console.log("Sending encodedTargetURL: " + encodedTargetURL);
            $.get(baseURL + "/" + encodedTargetURL, function(data) {
                alert("Search was performed for query: " + targetURL);
            });
        } else {
            alert("Psst... you might want to enter a query!");
        }
    });

});