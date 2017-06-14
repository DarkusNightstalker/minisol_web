/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var SRC = function () {

    padding_right = function (s, c, n) {
        if (!s || !c || s.length >= n) {
            return s;
        }
        var max = (n - s.length) / c.length;
        for (var i = 0; i < max; i++) {
            s += c;
        }
        return s;
    }
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
    $("#form\\:date-issue").datetimepicker({
        language: "es",
        format: "DD/MM/YYYY",
        pickTime: false
    });
    $("#form\\:payment-proof").select2({
        placeholder: "Seleccione un comprobante de pago",
        width: "100%",
        allowClear: true,
        containerCssClass: ":all:"
    });
    $("#form\\:repayment").numeric({scale: 2, negative: false});
    $("#form").validate({
        errorClass: "has-error text-danger danger",
        validClass: "has-success success",
        errorElement: "em",
        rules: {
            "form:sale": {
                required: true
            },
            "form:payment-proof": {
                required: true
            },
            "form:repayment": {
                required: true,
            }
        },
        messages: {
            "form:sale": {
                required: "Campo Obligatorio"
            },
            "form:payment-proof": {
                required: "Campo Obligatorio"
            },
            "form:repayment": {
                required: "Campo Obligatorio",
            }
        },
        highlight: function (element, errorClass, validClass) {
            if ($(element).hasClass("table-input")) {
                $(element).closest('td').addClass(errorClass).removeClass(validClass)
            } else {
                $(element).closest('.form-group').addClass(errorClass).removeClass(validClass);
            }
            if ($(element).hasClass("select2")) {
                $(element).addClass("select2-danger").removeClass("select2-success");
            }
        },
        unhighlight: function (element, errorClass, validClass) {
            if ($(element).hasClass("table-input")) {
                $(element).closest('td').removeClass(errorClass).addClass(validClass);
            } else {
                $(element).closest('.form-group').removeClass(errorClass).addClass(validClass);
            }

            if ($(element).hasClass("select2")) {
                $(element).addClass("select2-success").removeClass("select2-danger");
            }
        },
        errorPlacement: function (error, element) {
            if (element.is("select")) {
                if (element.hasClass("table-input")) {
                    error.insertAfter(element);
                } else
                    error.insertAfter($(element).closest(".form-group").find("span.select2"))
            } else {
                var ipt = $(element).closest(".input-group");
                if (ipt != null && ipt.hasClass("input-group")) {
                    error.insertAfter(ipt);
                } else {
                    error.insertAfter(element);
                }
            }
        }

    });
});