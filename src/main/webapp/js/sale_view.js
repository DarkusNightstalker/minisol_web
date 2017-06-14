
var Sale = function () {
	var sendId;
    return {
        delete: function () {
			var content = "<p>"+
							"¿Esta seguro que desea ANULAR la :<br/>"+
							"<b>"+arguments[0].toUpperCase()+(arguments[1] ? " ELECTRÓNICA" : "")+" "+arguments[2]+"-"+arguments[3]+"</b><br/>"+
							"<b> CLIENTE&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b> &nbsp;&nbsp;&nbsp;"+(arguments[4] =="" ? "<em>Ninguno</em>" : arguments[4])+"<br/>"+
							"<b> SUBTOTAL&nbsp;:</b> &nbsp;&nbsp;&nbsp;S/. "+arguments[5].toFixed(2)+"  ?</p>" 
            show_delete(content);
        },
        recovery: function (name) {
			var content = "<p>"+
							"¿Esta seguro que desea RECUPERAR la :<br/>"+
							"<b>"+arguments[0].toUpperCase()+(arguments[1] ? " ELECTRÓNICA" : "")+" "+arguments[2]+"-"+arguments[3]+"</b><br/>"+
							"<b> CLIENTE&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b> &nbsp;&nbsp;&nbsp;"+(arguments[4] =="" ? "<em>Ninguno</em>" : arguments[4])+"<br/>"+
							"<b> SUBTOTAL&nbsp;:</b> &nbsp;&nbsp;&nbsp;S/. "+arguments[5].toFixed(2)+"  ?</p>" 
            show_recovery(content);
        }, 
		currentSendId: function () {
			return [{name: 'id', value: sendId}];
        },
        send: function () {
			var content = "<p>"+
							"¿Esta seguro que desea procesar con la sunat la :<br/>"+
							"<b>"+arguments[0].toUpperCase()+(arguments[1] ? " ELECTRÓNICA" : "")+" "+arguments[2]+"-"+arguments[3]+"</b><br/>"+
							"<b> CLIENTE&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b> &nbsp;&nbsp;&nbsp;"+(arguments[4] =="" ? "<em>Ninguno</em>" : arguments[4])+"<br/>"+
							"<b> SUBTOTAL&nbsp;:</b> &nbsp;&nbsp;&nbsp;S/. "+arguments[5].toFixed(2)+"  ?</p>";
			sendId = arguments[6];
            show_send(content);
        }
    };
}();
