
var Rol = function () {
    return {
        delete: function () {
            var content = "<p>" +
                    "¿Esta seguro que desea ANULAR el rol :<br/>" +
                    "<b>" + arguments[0] + "</b>   ?</p>";
            show_delete(content);
        },
        recovery: function (name) {
            var content = "<p>" +
                    "¿Esta seguro que desea RECUPERAR rl rol :<br/>" +
                    "<b>" + arguments[0] + "</b>   ?</p>";
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
