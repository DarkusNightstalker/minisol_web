var Purchase = function() {
    return {
        delete: function() {
			var content = "<p>"+
							"¿Esta seguro que desea ANULAR la :<br/>"+
							"<b>"+arguments[0]+" "+arguments[1]+"-"+arguments[2]+"</b><br/>"+
							"<b> PROV&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b> &nbsp;&nbsp;&nbsp;"+arguments[3]+"<br/>"+
							"<b> SUBTOTAL:</b> &nbsp;&nbsp;&nbsp;S/. "+arguments[4]+"  ?</p>" 
            show_delete(content);
        },
        recovery: function(name) {
            $.SmartMessageBox({
                title: "Aviso!",
                content: "Esta seguro de restaurar la dependencia de tareas '" + name + "'",
                buttons: '[No][Si]'
            }, function(ButtonPressed) {
                if (ButtonPressed === "Si") {
                    recovery();
                }
            });
        },
        payment: function() {
            return {
                init: function() {

                    $(function() {
                        $("#form-payment\\:date-payment").datetimepicker({
                            format: 'DD/MM/YYYY HH:mm'
                        });
                    });

                    $("#form-payment\\:quantity")
                        .numeric({
                            scale: 2,
                            negative: false
                        })
                        .on("input", function() {
                            var max = parseFloat($(this).attr("max"));
                            var val = parseFloat(this.value);
                            if (!isNaN(val)) {
                                if (val > max) {
                                    this.value = max;
                                }
                            }
                        });
                    $("#form-payment").validate({
                        errorClass: "has-error text-danger",
                        validClass: "has-success success",
                        errorElement: "em",
                        rules: {
                            "form-payment:date-payment": {
                                required: true
                            },
                            "form-payment:quantity": {
                                required: true
                            }
                        },
                        messages: {
                            "form-payment:date-payment": {
                                required: "Campo obligatorio"
                            },
                            "form-payment:quantity": {
                                required: "Campo obligatorio"
                            }
                        },
                        highlight: function(element, errorClass, validClass) {
                            $(element).closest('.form-group').addClass(errorClass).removeClass(validClass);
                        },
                        unhighlight: function(element, errorClass, validClass) {
                            $(element).closest('.form-group').removeClass(errorClass).addClass(validClass);
                        },
                        errorPlacement: function(error, element) {
                            error.insertAfter(element);
                        }
                    });
                },
                save: function() {
                    if ($("#form-payment").valid()) {
                        save_payments();
                    }
                }
            };
        }(),
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

function open_payments() {
    $.magnificPopup.open({
        removalDelay: 500,
        items: {
            src: '#modal-payment'
        },
        callbacks: {
            beforeOpen: function(e) {
                this.st.mainClass = 'mfp-zoomIn';
            },
            close: function() {
                search();
                return true;
            }
        },

        modal: false,
        midClick: false
    });
}