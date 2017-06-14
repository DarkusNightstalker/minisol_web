
var ElectronicBook = function () {
    return {
        delete: function () {
            var content = "<p>" +
                    "¿Esta seguro que desea ANULAR el libro electronico :<br/>" +
                    "<b>" + arguments[0] + "</b> " + arguments[1] + "  ?</p>"
            show_delete(content);
        },
        recovery: function () {
            var content = "<p>" +
                    "¿Esta seguro que desea RECUPERAR el documento de identidad :<br/>" +
                    "<b>" + arguments[0] + "</b> " + arguments[1] + "  ?</p>"
            show_recovery(content);
        }
    };
}();
$(function () {
    $("#form\\:code")
            .nextOnEnter()
            .focus();
    $("#form\\:name")
            .pressEnter(function () {
                search();
            });
})
function  clean() {
    $("#form\\:code").val('');
    $("#form\\:name").val('');
}
