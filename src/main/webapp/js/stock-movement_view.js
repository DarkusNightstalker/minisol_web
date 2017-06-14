var SM = function() {
    return {
        delete: function() {
            var content = "<p>" +
                "¿Esta seguro que desea anular la :<br/>" +
                "<b>" + arguments[0] + " " + arguments[1] + "-" + arguments[2] + "</b><br/>" +
                "<b> DESTINO&nbsp;&nbsp;&nbsp;:</b> &nbsp;" + arguments[3] + "   ?</p>"
            show_delete(content);
        },
        send: function() {
            var content = "<p>" +
                "¿Esta seguro que desea enviar a la sunat la :<br/>" +
                "<b>" + arguments[0] + " " + arguments[1] + "-" + arguments[2] + "</b><br/>" +
                "<b> DESTINO&nbsp;&nbsp;&nbsp;:</b> &nbsp;" + arguments[3] + "   ?</p>"
            show_send(content);
        },
        receive: function() {
            var content = "<p>" +
                "¿Esta seguro que desea recepcionar los productos DE :<br/>" +
                "<b>" + arguments[0] + " " + arguments[1] + "-" + arguments[2] + "</b><br/>" +
                "<b> DESTINO&nbsp;&nbsp;&nbsp;:</b> &nbsp;" + arguments[3] + "   ?</p>"
            show_receive(content);
        },
        recovery: function(name) {
            var content = "<p>" +
                "¿Esta seguro que desea restaurar la :<br/>" +
                "<b>" + arguments[0] + " " + arguments[1] + "-" + arguments[2] + "</b><br/>" +
                "<b> DESTINO&nbsp;&nbsp;&nbsp;:</b> &nbsp;" + arguments[3] + "   ?</p>"
            show_recovery(content);
        },
        print: function(ctx,id) {
            $("<iframe>") // create a new iframe element
                .hide() // make it invisible
                .attr("src", ctx+"/service/print_ism.xhtml?id="+id) // point the iframe to the page you want to print
                .appendTo("body");
        }
    };
}();
$(function() {
    $("#form\\:payment-proof").on('change', function() {
        search();
    });
    $("#form\\:identity-number").pressEnter(function() {
        search();
    });
    $("#form\\:name").pressEnter(function() {
        search();
    });
})

function clean() {
    $("#form\\:identity-number").val('');
    $("#form\\:name").val('');
}