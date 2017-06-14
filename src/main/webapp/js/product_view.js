/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var Product = function () {
    return {
        delete: function () {
            var content = "<p>" +
                    "¿Esta seguro que desea ANULAR el producto :<br/>" +
                    "<b>" + arguments[0] + "</b> " + arguments[1] + "  ?</p>"
            show_delete(content);
        },
        recovery: function () {
            var content = "<p>" +
                    "¿Esta seguro que desea RECUPERAR el producto:<br/>" +
                    "<b>" + arguments[0] + "</b> " + arguments[1] + "  ?</p>"
            show_recovery(content);
        }
    };
}();
$(function () {
    $("#form\\:barcode")
            .nextOnEnter()
            .focus();
    $("#form\\:name")
            .pressEnter(function () {
                search();
            });
})
function  clean(){
    $("#form\\:barcode").val('');
    $("#form\\:name").val('');
}
