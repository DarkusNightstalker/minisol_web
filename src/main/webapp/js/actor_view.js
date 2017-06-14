
var Actor = function () {
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
		synchro: function () {
            return {
				configure_captcha:function(){
					$("#form-synchro\\:captcha").pressEnter(function(){
						Actor.synchro.before_synchro();
					})
				},
                before_synchro: function () {
                    App.validateForm("form-synchro", begin_synchro);
                },
                after_synchro: function () {
                    synchro_progress();
                    $("#form-synchro\\:cc-wrapper").addClass("hidden");
                    $("#form-synchro\\:pg-wrapper").removeClass("hidden");
                },
                end_synchro: function () {
                },
                reload_captcha :function() {
                    $("#form-synchro\\:cc-wrapper").removeClass("hidden");
                    $("#form-synchro\\:pg-wrapper").addClass("hidden");
                },
                update_progress: function () {
                    if ($("#form-synchro\\:valid").val() == "false") {
                        reload_captcha();
                    } else if ($("#form-synchro\\:finalize").val() == "false") {
                        synchro_progress();
                    } else {
                        Actor.synchro.end_synchro();
                    }
                },
                init: function () {
                    $(document).on("keypress", "#form-synchro", function (event) {
                        return event.keyCode != 13;
                    });
                    $("#form-synchro").validate({
                        errorClass: "has-error text-danger",
                        validClass: "has-success",
                        errorElement: "em",
                        rules: {
                            "form-synchro:captcha": {
                                required: true
                            }
                        },
                        messages: {
                            "form-synchro:captcha": {
                                required: "Este campo es requerido"
                            }
                        },
                        highlight: function (element, errorClass, validClass) {
                            $(element).closest('.form-group').addClass(errorClass).removeClass(validClass);
                        },
                        unhighlight: function (element, errorClass, validClass) {
                            $(element).closest('.form-group').removeClass(errorClass).addClass(validClass);
                        },
                        errorPlacement: function (error, element) {
                            error.insertAfter(element);
                        }
                    });
                }
            };
        }()
    };
}();
$(function () {
    $("#form\\:customer").on("click", function () {
        if (!this.checked && !$("#form\\:supplier").attr("checked")) {
            $("#form\\:supplier").trigger("click");
        }
        search();
    });
    $("#form\\:supplier").on("click", function () {
        if (!this.checked && !$("#form\\:customer").attr("checked")) {
            $("#form\\:customer").trigger("click");
        }
        search();
    });
    $("#form\\:identity-document").pressEnter(function(){
		search();
	});
    $("#form\\:name").pressEnter(function(){
		search();
	});
})
function  clean() {
    $("#form\\:identity-number").val('');
    $("#form\\:name").val('');
}
