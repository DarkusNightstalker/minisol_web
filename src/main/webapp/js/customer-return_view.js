var SRC = function() {
    return {
        delete: function() {
			var content = "<p>"+
							"¿Esta seguro que desea ANULAR la :<br/>"+
							"<b>"+arguments[0]+" "+arguments[1]+"-"+arguments[2]+"</b><br/>"+
							"<b> Venta&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b> &nbsp;&nbsp;&nbsp;"+arguments[3]+" "+arguments[4]+"-"+arguments[5]+"<br/>"+
							"<b> Monto:</b> &nbsp;&nbsp;&nbsp;S/. "+arguments[6]+"  ?</p>" 
            show_delete(content);
        },
        recovery: function() {
			var content = "<p>"+
							"¿Esta seguro que desea RECUPERAR la :<br/>"+
							"<b>"+arguments[0]+" "+arguments[1]+"-"+arguments[2]+"</b><br/>"+
							"<b> Venta&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b> &nbsp;&nbsp;&nbsp;"+arguments[3]+" "+arguments[4]+"-"+arguments[5]+"<br/>"+
							"<b> Monto:</b> &nbsp;&nbsp;&nbsp;S/. "+arguments[6]+"  ?</p>" 
            show_recovery(content);
        },
        send: function() {
			var content = "<p>"+
							"¿Esta seguro que desea ENVIAR A LA SUNAT la :<br/>"+
							"<b>"+arguments[0]+" "+arguments[1]+"-"+arguments[2]+"</b><br/>"+
							"<b> Venta&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b> &nbsp;&nbsp;&nbsp;"+arguments[3]+" "+arguments[4]+"-"+arguments[5]+"<br/>"+
							"<b> Monto:</b> &nbsp;&nbsp;&nbsp;S/. "+arguments[6]+"  ?</p>" 
            show_send(content);
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