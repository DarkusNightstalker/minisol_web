
var PaymentProof = function () {
    return {
        save: function () {
            App.validateForm("form", save);
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
    $("#form  button.rol").on("click", function () {
        var this_ = $(this);
        var check_type = this_.attr("data-check");
        var chk = $("#form\\:" + check_type);
        if (chk.is(":checked")) {
            chk.prop("checked", false);
            this_.removeClass("btn-" + this_.attr("data-select")).addClass("btn-default");
            this_.find("span").html("");
        } else {
            chk.prop("checked", true);
            this_.addClass("btn-" + this_.attr("data-select")).removeClass("btn-default");
            this_.find("span").html('<i class="fa fs13 fa-check" style="position: absolute;left: 25px;top:10px"/>');
        }
    });
    $("#form\\:code")
            .nextOnEnter()
            .focus();
    $("#form\\:abbr")
            .nextOnEnter();
    $("#form\\:name")
            .pressEnter(PaymentProof.save);
    $("#form").validate({
        errorClass: "has-error text-danger",
        validClass: "has-success",
        errorElement: "em",
        rules: {
            "form:code": {
                minlength: 2,
                required: true,
                integer: true
            },
            "form:name": {
                required: true
            }
        },
        messages: {
            "form:code": {
                minlength: "Debe ser 2 digitos",
                required: "Por favor ingrese codigo",
                integer: "Debe ser 2 digitos"
            },
            "form:name": {
                required: "Por favor ingrese nombre"
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