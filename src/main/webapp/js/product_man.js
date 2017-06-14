/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var Product = function () {

    return {
        save: function () {
            App.validateForm("form", save);
        },
        configure_suppliers: function () {
            $("#form\\:suppliers").select2({
                placeholder: "",
                width: "100%",
                allowClear: true,
                containerCssClass: ":all:",
                templateResult: function (data) {
                    var text = data.text;
                    if (data.id) {
                        var d = data.text.split("||");
                        text = $("<div><span  class='fs10'>" + d[2] + "</span> <small class='fs10' style='padding-top : 2px'>(<b>" + d[0] + "</b> : " + d[1] + ")</small></div>");
                    }
                    return text;
                },
                templateSelection: function (data) {
                    var text = data.text;

                    if (data.id) {
                        var d = data.text.split("||");
                        text = $("<div><span  class='fs10'>" + d[2] + "</span> <small class='fs10'>(<b>" + d[0] + "</b> : " + d[1] + ")</small></div>");
                    }
                    return text;
                }
            });
        },
        productLine: function () {
            return {
                before_save: function () {
                    App.validateForm("form-product-line", save_product_line);
                },
                after_save: function () {
                    if ($("#form-product-line\\:valid").val() == "true") {
                        update_product_line();
                    }
                },
                init: function () {
                    $("#form-product-line").validate({
                        errorClass: "has-error text-danger",
                        validClass: "has-success",
                        errorElement: "em",
                        rules: {
                            "form-product-line:name": {
                                required: true
                            }
                        },
                        messages: {
                            "form-product-line:name": {
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
        }(),
        uom: function () {
            return {
                before_save: function () {
                    App.validateForm("form-uom", save_uom);
                },
                after_save: function () {
                    if ($("#form-uom\\:valid").val() == "true") {
                        update_uom();
                    }
                },
                init: function () {
                    $("#form-uom").validate({
                        errorClass: "has-error text-danger",
                        validClass: "has-success",
                        errorElement: "em",
                        rules: {
                            "form-uom:code": {
                                required: true
                            },
                            "form-uom:name": {
                                required: true
                            }
                        },
                        messages: {
                            "form-uom:code": {
                                required: "Este campo es requerido"
                            },
                            "form-uom:name": {
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
        }(),
        update_item_psp: function (index, last_value) {
            window["update_item" + index]([{name: 'last_value', value: last_value}, {name: 'index', value: index}]);
        },
        configure_quantity: function (index, last_value) {
            $("#form\\:store\\:" + index + "\\:quantity").pressEnter(function () {
                Product.update_item_psp(index, last_value)
            });
        },
        configure_price: function (index, last_value) {
            $("#form\\:store\\:" + index + "\\:price-value").pressEnter(function () {
                Product.update_item_psp(index, last_value);
            });
        },
        edit_psp: function (index) {
            $("#form\\:store\\:" + index + "\\:l-quantity").addClass("hidden");
            $("#form\\:store\\:" + index + "\\:l-uom").addClass("hidden");
            $("#form\\:store\\:" + index + "\\:l-price").addClass("hidden");
            $(".btn-edit-psp").addClass("hidden");
            $(".btn-drop-psp").addClass("hidden");
            $("#row_add").addClass("hidden");
            $("#btn-cancel-edit-" + index).removeClass("hidden");
            $("#form\\:store\\:" + index + "\\:quantity").removeClass("hidden");
            $("#form\\:store\\:" + index + "\\:uom").removeClass("hidden");
            $("#form\\:store\\:" + index + "\\:price").removeClass("hidden");
        },
        cancel_edit: function (index) {
            $("#form\\:store\\:" + index + "\\:l-quantity").removeClass("hidden");
            $("#form\\:store\\:" + index + "\\:l-uom").removeClass("hidden");
            $("#form\\:store\\:" + index + "\\:l-price").removeClass("hidden");
            $(".btn-edit-psp").removeClass("hidden");
            $(".btn-drop-psp").removeClass("hidden");
            $("#row_add").removeClass("hidden");
            $("#btn-cancel-edit-" + index).addClass("hidden");
            $("#form\\:store\\:" + index + "\\:quantity").addClass("hidden");
            $("#form\\:store\\:" + index + "\\:uom").addClass("hidden");
            $("#form\\:store\\:" + index + "\\:price").addClass("hidden");
        },
        configureSalePrices: function () {
            $("#form .p-quantity").numeric({scale: 2, decimal: "."});
            $("#form .p-price").numeric({scale: 2, decimal: "."});
            $("#form\\:add_quantity").pressEnter(function () {
               $("#form\\:add_price").focus();
            });
            $("#form\\:add_price").pressEnter(function () {
                $("#form\\:add_quantity").focus();
                add_item();
            });
        }
    };
}();
$(function () {
    App.scrollTop();
    $("#form\\:name")
            .nextOnEnter()
            .focus();
    $("#form\\:barcode")
            .nextOnEnter()
            .numeric({scale: 0});
    $("#form\\:description")
            .pressEnter(function () {
                $("#form\\:percent-utility").focus();
            });
    $("#form\\:percent-utility")
            .pressEnter(function () {
                var cost = parseFloat($(this).attr("data-cost"));
                var percent = parseFloat($(this).val());
                if (isNaN(percent))
                    percent = 0;
                var rate = percent / 100;
                $("#price-calculate").html("S/." + ((1 + rate) * cost).toFixed(2));
                $("#form\\:add_quantity").focus();
            })
            .numeric({precision: 3, scale: 0});
    $("#form").validate({
        errorClass: "has-error text-danger",
        validClass: "has-success",
        errorElement: "em",
        rules: {
            "form:name": {
                required: true
            },
            "form:uom-category": {
                required: true
            }
        },
        messages: {
            "form:name": {
                required: "Ingrese el nombre del producto"
            },
            "form:uom-category": {
                required: "Seleccione una categoria de unidades"
            }
        },
        highlight: function (element, errorClass, validClass) {
            $(element).closest('.form-group').addClass(errorClass).removeClass(validClass);
            if ($(element).hasClass("select2")) {
                $(element).addClass("select2-danger").removeClass("select2-success");
            }
        },
        unhighlight: function (element, errorClass, validClass) {
            $(element).closest('.form-group').removeClass(errorClass).addClass(validClass);
            if ($(element).hasClass("select2")) {
                $(element).addClass("select2-success").removeClass("select2-danger");
            }
        },
        errorPlacement: function (error, element) {
            if (element.is("select")) {
                error.insertAfter($(element).closest(".form-group").find("span.select2"));
            } else {
                if (element.attr("id") == "form:barcode") {
                    error.insertAfter($(element).closest(".input-group"));
                } else
                    error.insertAfter(element);
            }
        }
    });
});