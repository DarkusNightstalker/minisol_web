var StockType = function () {
    return {
        save: function () {
            if ($("#form").valid()) {
                save();
            }
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
    $("#form").validate({

        errorClass: "has-error text-danger",
        validClass: "has-success",
        errorElement: "em",

        rules: {
            "form:code": {
                required: true,
                maxlength: 2
            },
            "form:name": {
                required: true,
                maxlength: 50
            }
        },
        messages: {
            "form:name": {
                required: "Campo Obligatorio",
                maxlength: "Maximo 50 caracteres"
            },
            "form:code": {
                required: "Campo Obligatorio",
                maxlength: "Maximo 2 caracteres"
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