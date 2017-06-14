var ProductLine = function () {
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
            "form:name": {
                required: true,
                maxlength: 50
            }
        },
        messages: {
            "form:name": {
                required: "Campo Obligatorio",
                maxlength: "Maximo 50 caracteres"
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