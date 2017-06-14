var IdentityDocument = function () {
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
    $("#form\\:length")
            .nextOnEnter()
            .numeric({negative: false, scale: 0});
    $("#form\\:name")
            .pressEnter(IdentityDocument.save);
    $("#form").validate({
        errorClass: "has-error text-danger",
        validClass: "has-success",
        errorElement: "em",
        rules: {
            "form:code": {
                required: true,
                maxlength: 2,
            },
            "form:abbr": {
                maxlength: 50
            },
            "form:name": {
                required: true,
                maxlength: 255
            },
            "form:length": {
                required: true,
                min: 1,
                max: 32767
            }
        },
        messages: {
            "form:code": {
                required: "Campo obligatorio",
                maxlength: "Tamaño maximo de 2 caracteres",
            },
            "form:abbr": {
                maxlength: "Tamaño maximo de 50 caracteres"
            },
            "form:name": {
                required: "Campo obligatorio",
                maxlength: "Tamaño maximo de 255 caracteres"
            },
            "form:length": {
                required: "Campo obligatorio",
                min: "Valor minimo de 1",
                max: "Valor maximo de 32767"
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