$(function() {
    $('#modal-managed').on('shown.bs.modal', function() {
        $('#form-managed\\:username').focus();
    });
});
var UserManaged = function() {
    return {
        before_save: function() {
            if ($("#form-managed").valid()) {
                save_user();
            }
        },
        after_save: function() {
            if ($("#form-managed\\:valid").val() == "true") {
                update_users();
            }
        },
        init: function(context_path) {
            $("#form-managed").validate({
                errorClass: "has-error text-danger",
                validClass: "has-success",
                errorElement: "em",
                rules: {
                    "form-managed:username": {
                        required: true
                    },
                    "form-managed:password": {
                        required: true
                    },
                    "form-managed:repeat-password": {
                        equalTo: "#form-managed\\:password"
                    },
                    "form-managed:exist-employee": {
                        required: true
                    }
                },
                messages: {
                    "form-managed:username": {
                        required: "Campo obligatorio"
                    },
                    "form-managed:password": {
                        required: "Campo obligatorio"
                    },
                    "form-managed:repeat-password": {
                        equalTo: "Debe ser igual al campo contraseña"
                    },
                    "form-managed:exist-employee": {
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
        configure_employee: function() {
            $("#form-managed\\:identity-number").pressEnter(function() {
                    if ($("#form-managed\\:exist-employee").val() != "") {

                    } else
                        search_employee();
                })
                .numeric({
                    precision: 8,
                    scale: 0
                })
                .on("input", function() {
                    if ($("#form-managed\\:exist-employee").val() != "") {
                        null_employee();
                    }
                });
        }
    };
}();