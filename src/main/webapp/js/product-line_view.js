
var ProductLine = function () {
    return {
        delete: function (document, document_number, name) {
            $("#modal-delete span.current-selected").html('<b>' + document + '</b> ' + document_number + ' - ' + name);
            show_delete();
        },
        recovery: function (name) {
            $.SmartMessageBox({
                title: "Aviso!",
                content: "Esta seguro de restaurar la dependencia de tareas '" + name + "'",
                buttons: '[No][Si]'
            }, function (ButtonPressed) {
                if (ButtonPressed === "Si") {
                    recovery();
                }
            });
        },
    };
}();
$(function () {
    $("#form\\:code").pressEnter(function(){
		search()
	});
    $("#form\\:name").pressEnter(function(){
		search()
	});
})
function  clean() {
    $("#form\\:code").val('');
    $("#form\\:name").val('');
}
