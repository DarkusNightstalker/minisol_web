var FinancialEntity = function() {
    return {
        save: function() {
            App.validateForm('form',save);
        },
        back: function() {
		back();
        },
        refresh: function() {
            refresh();
        }
    };
}();

$(function() {
    $("#form\\:code")
            .nextOnEnter()
            .focus();
    $("#form\\:name")
            .pressEnter(FinancialEntity.save);
    $("#form").validate({
        errorClass: "has-error text-danger",
        validClass: "has-success",
        errorElement: "em",
        rules: {
            "form:code": {
                required: true,
                maxlength: 2,
            },
            "form:name": {
                required: true,
                maxlength: 255
            }
        },
        messages: {
            "form:code": {
                required: "Campo obligatorio",
                maxlength: "Tamaño maximo de 2 caracteres",
            },
            "form:name": {
                required: "Campo obligatorio",
                maxlength: "Tamaño maximo de 255 caracteres"
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
})