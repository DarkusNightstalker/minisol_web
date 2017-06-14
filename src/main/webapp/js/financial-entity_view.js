
var FinancialEntity = function () {
    return {
        delete: function () {
            var content = "<p>" +
                    "¿Esta seguro que desea ANULAR la entidad financiera :<br/>" +
                    "<b>" + arguments[0] + "</b> " + arguments[1] + "  ?</p>"
            show_delete(content);
        },
        recovery: function () {
            var content = "<p>" +
                    "¿Esta seguro que desea RECUPERAR la entidad financiera :<br/>" +
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
});
function  clean() {
    $("#form\\:code").val('');
    $("#form\\:name").val('');
}
