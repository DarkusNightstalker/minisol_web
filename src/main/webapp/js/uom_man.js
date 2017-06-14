var UoM = function () {
    return {
        save: function () {
            App.validateForm('form',save);
        },
        back: function () {
           back();
        },
        refresh: function () {
            refresh();
        }
    };
}();
$(function () {
    $("#form\\:code")
            .nextOnEnter()
            .focus();
    $("#form\\:abbr")
            .nextOnEnter();
    $("#form\\:name")
            .pressEnter(function () {
                UoM.save();
            });
    $("#form").validate({

        errorClass: "has-error text-danger",
        validClass: "has-success",
        errorElement: "em",

        rules: {
            "form:name": {
                required: true
            },
            "form:code": {
                required: true
            }
        },
        messages: {
            "form:name": {
                required: "Campo Obligatorio"
            },
            "form:code": {
                required: "Campo Obligatorio"
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
})