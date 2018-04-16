$(document).ready(function() {
    let baseURL = "http://localhost:8080/WingIt/admin/";
    let adminPrefix = "/admin/?adminRequest=";

    $(".command").each(function() {
        var commandBlock = $(this);
        var command = $(this).attr("id");
        $(this).on('click', function() {
            console.log("Sending request: " + command);
            clearData();
            $.get(adminPrefix + command, function(data) {
                console.log("Received data: " + data);
                if (data.classConditionalProbabilities && data.classPriors) {
                    presentData(data.classConditionalProbabilities, data.classPriors);
                }
                $(commandBlock).find(".check").fadeIn("slow", function() {
                    setTimeout(function() {
                        $(commandBlock).find(".check").fadeOut("slow");
                    }, 5000);
                });
            });
        });
    });

    function presentData(classConditionalProbabilities, classPriors) {
        $(".results").css("display", "inline-block");
        $(".classConditionalProbabilities").html("<p>" + classConditionalProbabilities + "</p>");
        $(".classPriors").html("<p>" + classPriors + "</p>");
    }

    function clearData() {
        $(".classConditionalProbabilities").html("");
        $(".classPriors").html("");
        $(".results").css("display", "none");
    }
});