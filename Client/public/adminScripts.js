$(document).ready(function() {
    let baseURL = "http://localhost:8080/WingIt/admin/";
    let adminPrefix = "/admin/?adminRequest=";

    $(".command").each(function() {
        var commandBlock = $(this);
        var command = $(this).attr("id");
        $(this).on('click', function() {
            $.get(adminPrefix + command, function(data) {
                $(commandBlock).find(".check").fadeIn("slow", function() {
                    $(commandBlock).find(".check").fadeOut("slow");
                });
            });

            // $.get(baseURL + $(this).attr("id"), function(data) {
            //     $(commandBlock).find(".check").fadeIn("slow", function() {
            //         $(commandBlock).find(".check").fadeOut("slow", function() {
            //         });
            //     });
            // });
        });
    });
});