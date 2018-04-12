$(document).ready(function() {
    let urlPrefix = "/?url=";

    $(".searchButton").on("click", function() {
        let targetURL = $(".searchTerm").val();
        if (targetURL != "") {
            console.log("Requesting url: " + targetURL);
            $.get(urlPrefix + targetURL, function(data) {
                console.log("Received data: " + data);
            });
        } else {
            alert("Psst... you might want to enter a query!");
        }
    });

});