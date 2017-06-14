
var UoM = function () {
    return {
        delete: function () {
            var content = "<p>" +
                    "¿Esta seguro que desea ANULAR la unidad de medida :<br/>" +
                    "<b>" + arguments[0] + "</b> " + arguments[1] + "  ?</p>"
            show_delete(content);
        },
        recovery: function (name) {
            var content = "<p>" +
                    "¿Esta seguro que desea ANULAR la unidad de medida :<br/>" +
                    "<b>" + arguments[0] + "</b> " + arguments[1] + "  ?</p>"
            show_recovery(content);
        },
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
