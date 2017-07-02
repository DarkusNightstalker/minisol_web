/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var SupplierReturnM = function () {
    return {
        save: function () {            
            App.validateForm("form", save);
        },
        back: function () {
            $("#form\\:back").trigger("click");
        },
        refresh: function () {
            refresh();
        },
        configure_purchase: function () {
             $("#form\\:purchase").select2({
                placeholder: "Seleccione una comprobante",
                width: "100%",
                containerCssClass: ":all:",
                templateResult: function(data) {
                    var text = data.text;
                    if (data.id) {
                        var d = data.text.split("||");                                                                                        
                        text = $("<div><span class='fs11'>"+d[0]+"</span><br/><small class='fs9'>"+d[1]+"</small></div>");
                    }
                    return text;
                },
                templateSelection: function(data) {
                    var text = data.text;

                    if (data.id) {
                        var d = data.text.split("||");
                        text = d[0];
                    }
                    return text;
                }
            });
        }
    };
}();
$(function(){
    $('#form\\:supplier input[type=text]').focus().nextOnEnter();
    $('#form\\:date-issue').datetimepicker({
        pickTime: false,
        format: "DD/MM/YYYY"
    }).nextOnEnter();
    $("#form\\:document-number").paddingLeft("0", 8).defaultSelect().nextOnEnter();
    $("#form").validate({
        errorClass: "has-error text-danger danger",
        validClass: "has-success success",
        errorElement: "em",
        rules: {
            "form:purchase": {
                required: true
            },
            "form:payment-proof": {
                required: true
            },
            "form:document-number": {
                required: true
            },
            "form:date-issue": {
                required: true
            },
            "form:repayment": {
                required: true,
                min: 0
            }
        },
        messages: {
            "form:purchase": {
                required: "Campo obligatorio"
            },
            "form:payment-proof": {
                required: "Campo obligatorio"
            },
            "form:document-number": {
                required: "Campo obligatorio"
            },
            "form:date-issue": {
                required: "Campo obligatorio",
                date: "Debe ser una fecha"
            },
            "form:repayment": {
                required: "Campo obligatorio",
                min: "Minimo valor : 0"
            }
        },
        highlight: function (element, errorClass, validClass) {
            if ($(element).hasClass("detail-item")) {
                $(element).closest('td').addClass(errorClass).removeClass(validClass)
            } else {
                $(element).closest('.form-group').addClass(errorClass).removeClass(validClass);
            }
            if ($(element).hasClass("select2")) {
                $(element).addClass("select2-danger").removeClass("select2-success");
            }
        },
        unhighlight: function (element, errorClass, validClass) {
            if ($(element).hasClass("detail-item")) {
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
                if (element.hasClass("detail-item")) {
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
})