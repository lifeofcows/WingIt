$(document).ready(function() {
    let baseURL = "http://localhost:8080/WingIt/admin/";
    let adminPrefix = "/admin/?adminRequest=";

    $(".command").each(function() {
        var commandBlock = $(this);
        var command = $(this).attr("id");
        $(this).on('click', function() {
            console.log("Sending request");
            $.get(adminPrefix + command, function(data) {
                console.log("Received data: " + data);
                $(commandBlock).find(".check").fadeIn("slow", function() {
                    $(commandBlock).find(".check").fadeOut("slow");
                });
            });
        });
    });
});