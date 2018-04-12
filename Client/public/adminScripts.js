$(document).ready(function() {
    let baseURL = "http://localhost:8080/WingIt/rest/rs";

    $(".command").each(function() {
        var command = $(this);
        $(this).on('click', function() {
            $.get(baseURL + "/" + $(this).attr("id"), function(data) {
                $(command).find(".check").fadeIn("slow", function() {
                    $(command).find(".check").fadeOut("slow", function() {
                    });
                });
            });
        });
    });
});