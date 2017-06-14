/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
function setCaretAtEnd(elem) {
    var elemLen = elem.value.length;
    // For IE Only
    if (document.selection) {
        // Set focus
        elem.focus();
        // Use IE Ranges
        var oSel = document.selection.createRange();
        // Reset position to 0 & then set at end
        oSel.moveStart('character', -elemLen);
        oSel.moveStart('character', elemLen);
        oSel.moveEnd('character', 0);
        oSel.select();
    } else if (elem.selectionStart || elem.selectionStart == '0') {
        // Firefox/Chrome
        elem.selectionStart = elemLen;
        elem.selectionEnd = elemLen;
        elem.focus();
    }
}
var Purchase = function () {
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
        configure_supplier: function () {
            $("#form\\:identity-number").pressEnter(function () {

                if ($("#form\\:exist-supplier").val() != "") {
                    $("#form\\:btn-s-product").trigger("click");
                } else
                    search_supplier();
            }).numeric({
                precision: 11,
                scale: 0
            }).on("input", function () {
                if ($("#form\\:exist-supplier").val() != "") {
                    null_customer();
                }
            }).defaultSelect();
        },
        configure_detail: function () {
            $("#form\\:igv").numeric({
                scale: 2,
                negative: false
            });
            $("#form\\:disccount").numeric({
                scale: 2,
                negative: false
            });
            $("#form\\:disccount").on("input", function () {
                Purchase.update_total();
            });
            $("#form\\:detail-wrapper input").numeric({
                scale: 2,
                negative: false
            });
            var update_row = function (element) {
                var subtotal = 0;
                var index = $(element).closest("tr").attr("data-index");

                var subtotal = parseFloat($("#form\\:detaill\\:" + index + "\\:subtotal").val());
                if (isNaN(subtotal))
                    subtotal = 0;
                var quantity = parseFloat($("#form\\:detaill\\:" + index + "\\:quantity").val());
                if (isNaN(quantity))
                    quantity = 0;
                var percent_igv = parseFloat($("#form\\:percent-igv").val());
                if (isNaN(percent_igv))
                    percent_igv = 0;
                var igv = parseFloat((subtotal * percent_igv).toFixed(2));
                if (isNaN(igv))
                    igv = 0;

                subtotal += igv;
                var unit_price = 0;
                try {
                    unit_price = parseFloat((subtotal / quantity).toFixed(2));
                } catch (err) {
                }
                if (isNaN(unit_price) || !isFinite(unit_price)) {
                    unit_price = 0.00;
                }
                $("#form\\:detaill\\:" + index + "\\:unit-price").val(unit_price.toFixed(2));
                $("#form\\:detaill\\:" + index + "\\:display-unit-price").text(unit_price.toFixed(2));
                $("#form\\:detaill\\:" + index + "\\:igv").val(igv.toFixed(2));
                $("#form\\:detaill\\:" + index + "\\:display-igv").text(igv.toFixed(2));
            }
            $("#form\\:detail-wrapper .quantity").on("input", function () {
                update_row(this);
                Purchase.update_summary();
            });

            $("#form\\:detail-wrapper .subtotal").on("input", function () {
                update_row(this);
                Purchase.update_summary();
            }).each(function () {
                update_row(this);
            });
            Purchase.update_summary();
        },
        update_summary: function () {
            var subtotal = 0;
            var igv = 0;
            $("#form\\:detail-wrapper .subtotal").each(function () {
                var s = parseFloat(this.value);
                if (!isNaN(s))
                    subtotal += s;
            });
            $("#form\\:detail-wrapper .display-igv").closest("td").find("input[type=hidden]").each(function () {
                var s = parseFloat(this.value);
                if (!isNaN(s))
                    igv += s;
            });
            $("#form\\:display-subtotal").text(subtotal.toFixed(2));
            $("#form\\:subtotal").val(subtotal.toFixed(2));
            $("#form\\:display-igv").text(igv.toFixed(2));
            $("#form\\:igv-value").val(igv.toFixed(2));
            Purchase.update_total();
        },
        update_total: function () {
            var total = 0;
            var subtotal = parseFloat($("#form\\:subtotal").val());
            if (isNaN(subtotal))
                subtotal = 0;
            var igv = parseFloat($("#form\\:igv-value").val());
            if (isNaN(igv))
                igv = 0;
            var disccount = parseFloat($("#form\\:disccount").val());
            if (isNaN(disccount))
                disccount = 0;
            total = subtotal + igv - disccount;
            $("#form\\:display-total").html(total.toFixed(2))
        },
        supplier: function () {
            return {
                init: function () {
                    $("#form-supplier").validate({
                        errorClass: "has-error text-danger",
                        validClass: "has-success",
                        errorElement: "em",
                        rules: {
                            "form-supplier:identity-document": {
                                required: true
                            },
                            "form-supplier:identity-number": {
                                required: true
                            },
                            "form-supplier:other": {
                                require_from_group: [1, ".name"]
                            },
                            "form-supplier:name": {
                                require_from_group: [1, ".name"]
                            }
                        },
                        messages: {
                            "form-supplier:identity-document": {
                                required: "Campo obligatorio"
                            },
                            "form-supplier:identity-number": {
                                required: "Campo obligatorio"
                            },
                            "form-supplier:other": {
                                require_from_group: "Campo obligatorio"
                            },
                            "form-supplier:name": {
                                require_from_group: "Campo obligatorio"
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
                                $(element).addClass("select2-danger").removeClass("select2-success");
                            }
                        },
                        errorPlacement: function (error, element) {
                            error.insertAfter(element);
                            if (element.is("select")) {
                                error.insertAfter($(element).closest(".form-group").find("span.select2"));
                            } else {
                                error.insertAfter(element);
                            }
                        }
                    });
                },
                before_save: function () {
                    if ($("#form-supplier").valid()) {
                        save_supplier();
                    }
                },
                after_save: function () {
                    if ($("#form-supplier\\:valid").val() == "true") {
                        update_supplier();
                    }
                }
            };
        }(),
        productSearch: function () {
            return {
                init: function () {
                    var h = parseInt($("#form-search-product\\:product-table-wrapper > table").first().css("height").replace("px", ""));
                    h = h - 40;
                    $("#form-search-product\\:product-table-wrapper > table tbody").css("height", h + "px");
                    $("#form-search-product\\:product-table-wrapper > table .input-sm").numeric({
                        scale: 2,
                        negative: false
                    });
                    $("#form-search-product\\:product-table-wrapper > table tr").each(function () {
                        var row = $(this);
                        var id = row.attr('data-index');
                        $("#form-search-product\\:data\\:" + id + "\\:quantity").on("input", function () {
                            var quantity = parseFloat($("#form-search-product\\:data\\:" + id + "\\:quantity").val());
                            if (isNaN(quantity))
                                quantity = 0;
                            var subtotal = parseFloat($("#form-search-product\\:data\\:" + id + "\\:subtotal").val());
                            if (isNaN(subtotal))
                                subtotal = 0;
                            var percent_igv = parseFloat($("#form-search-product\\:percent-igv").val());
                            if (isNaN(percent_igv))
                                percent_igv = 0;
                            var igv = subtotal * percent_igv;
                            subtotal += igv;
                            var unit_price = 0;
                            try {
                                unit_price = subtotal / quantity;
                            } catch (err) {
                            }
                            if (isNaN(unit_price))
                                unit_price = 0;
                            if (!isFinite(unit_price))
                                unit_price = 0;
                            $("#form-search-product\\:data\\:" + id + "\\:display-unit-price").text(unit_price.toFixed(2));
                            $("#form-search-product\\:data\\:" + id + "\\:unit-price").val(unit_price.toFixed(2));
                            $("#form-search-product\\:data\\:" + id + "\\:igv").val(igv.toFixed(2));
                            $("#form-search-product\\:data\\:" + id + "\\:display-igv").text(igv.toFixed(2));
                        }).pressEnter(function () {
                            $(document.getElementById("form-search-product:data:" + id + ":subtotal")).focus();
//                            setCaretAtEnd(document.getElementById("form-search-product:data:" + id + ":subtotal"));
                        });
                        $("#form-search-product\\:data\\:" + id + "\\:subtotal").on("input", function () {
                            var subtotal = parseFloat($("#form-search-product\\:data\\:" + id + "\\:subtotal").val());
                            if (isNaN(subtotal))
                                subtotal = 0;
                            var quantity = parseFloat($("#form-search-product\\:data\\:" + id + "\\:quantity").val());
                            if (isNaN(quantity))
                                quantity = 0;
                            var percent_igv = parseFloat($("#form-search-product\\:percent-igv").val());
                            if (isNaN(percent_igv))
                                percent_igv = 0;
                            var igv = subtotal * percent_igv;
                            subtotal += igv;
                            var unit_price = 0;
                            try {
                                unit_price = subtotal / quantity;
                            } catch (err) {
                            }
                            if (isNaN(unit_price))
                                unit_price = 0;
                            if (!isFinite(unit_price))
                                unit_price = 0;
                            $("#form-search-product\\:data\\:" + id + "\\:display-unit-price").text(unit_price.toFixed(2));
                            $("#form-search-product\\:data\\:" + id + "\\:unit-price").val(unit_price.toFixed(2));
                            $("#form-search-product\\:data\\:" + id + "\\:igv").val(igv.toFixed(2));
                            $("#form-search-product\\:data\\:" + id + "\\:display-igv").text(igv.toFixed(2));
                        }).trigger("input").pressEnter(function () {
                            setCaretAtEnd(document.getElementById("form-search-product:data:" + id + ":unit-price-sale"));
                            //$("#form-search-product\\:data\\:" + id + "\\:unit-price-sale").focus();

                        });
                        $("#form-search-product\\:data\\:" + id + "\\:unit-price-sale").pressEnter(function () {
                            var table_body = row.closest("tbody");
                            var last_row_id = table_body.find("tr").last().attr("data-index");
                            if (row.attr("data-index") == last_row_id) {
                                var next_btn = $("#form-search-product\\:next-btn");
                                if (next_btn.length) {
                                    Purchase.productSearch.after_search();
                                    next_btn.trigger("click");
                                }
                            } else {
                                var next_row = row.next("tr");
                                while (next_row.hasClass("hidden")) {
                                    if (next_row.attr("data-index") == last_row_id) {
                                        next_row = null;
                                        break;
                                    }
                                    next_row = next_row.next("tr");
                                }
                                if (next_row != null) {
                                    var id_next = next_row.attr("data-index");
                                    setCaretAtEnd(document.getElementById("form-search-product:data:" + id_next + ":quantity"));
                                } else {
                                    alert("Hola")
                                }
                            }
                        }).caretEnd();
                    });
                },
                after_search: function () {
                    var row = $("#form-search-product\\:product-table-wrapper > table tbody tr:not(.hidden)").first();
                    if (row.length) {
                        var id = row.attr('data-index');
                        $("#form-search-product\\:data\\:" + id + "\\:quantity").focus();
                    }
                }
            };
        }(),
        productManage: function () {
            return {
                init: function () {
                    $("#form-add-product\\:product-line").select2({
                        placeholder: "Seleccione una linea de productos",
                        width: "100%",
                        allowClear: true,
                        containerCssClass: ":all:"
                    }).pressEnter(function () {
                        setCaretAtEnd(document.getElementById('form-add-product:description'));
                    });
                    $("#form-add-product\\:name").pressEnter(function () {
                        setCaretAtEnd(document.getElementById('form-add-product:barcode'));
                    });
                    $("#form-add-product\\:description").pressEnter(function () {
                        setCaretAtEnd(document.getElementById('form-add-product:quantity'));
                    });
                    $("#form-add-product\\:barcode").numeric({
                        scale: 0,
                        negative: false
                    }).pressEnter(function () {
                        $("#form-add-product\\:product-line").select2('open');
                    });
                    $("#form-add-product\\:quantity").numeric({
                        scale: 2,
                        negative: false
                    }).on("input", function () {
                        var subtotal = parseFloat($("#form-add-product\\:subtotal").val());
                        if (isNaN(subtotal))
                            subtotal = 0;
                        var quantity = parseFloat($("#form-add-product\\:quantity").val());
                        if (isNaN(quantity))
                            quantity = 0;
                        var percent_igv = parseFloat($("#form-add-product\\:percent-igv").val());
                        if (isNaN(percent_igv))
                            percent_igv = 0;

                        var igv = subtotal * percent_igv;
                        var unit_price = ((subtotal + igv) / quantity);
                        if (isNaN(unit_price) || !isFinite(unit_price))
                            unit_price = 0;

                        $("#form-add-product\\:igv").text(igv.toFixed(2));
                        $("#form-add-product\\:unit-price").text("S/. " + unit_price.toFixed(2));
                    }).pressEnter(function () {
                        setCaretAtEnd(document.getElementById('form-add-product:subtotal'));
                    });
                    $("#form-add-product\\:subtotal").numeric({
                        scale: 2,
                        negative: false
                    }).on("input", function () {
                        var subtotal = parseFloat($("#form-add-product\\:subtotal").val());
                        if (isNaN(subtotal))
                            subtotal = 0;
                        var quantity = parseFloat($("#form-add-product\\:quantity").val());
                        if (isNaN(quantity))
                            quantity = 0;
                        var percent_igv = parseFloat($("#form-add-product\\:percent-igv").val());
                        if (isNaN(percent_igv))
                            percent_igv = 0;

                        var igv = subtotal * percent_igv;
                        var unit_price = ((subtotal + igv) / quantity);
                        if (isNaN(unit_price) || !isFinite(unit_price))
                            unit_price = 0;

                        $("#form-add-product\\:igv").text(igv.toFixed(2));
                        $("#form-add-product\\:unit-price").text("S/. " + unit_price.toFixed(2));
                    }).pressEnter(function () {
                        setCaretAtEnd(document.getElementById('percent-utility'));
                    });
                    $("#percent-utility").pressEnter(function () {
                        setCaretAtEnd(document.getElementById('form-add-product:add_quantity'));
                    });
                    $("#form-add-product").validate({
                        errorClass: "has-error text-danger",
                        validClass: "has-success",
                        errorElement: "em",
                        rules: {
                            "form-add-product:name": {
                                required: true
                            },
                            "form-add-product:barcode": {
                                required: true
                            },
                            "form-add-product:uom": {
                                required: true
                            },
                            "form-add-product:quantity": {
                                required: true
                            },
                            "form-add-product:subtotal": {
                                required: true
                            }
                        },
                        messages: {
                            "form-add-product:name": {
                                required: "Ingrese el nombre del producto"
                            },
                            "form-add-product:barcode": {
                                required: "Ingrese el código de barras"
                            },
                            "form-add-product:uom": {
                                required: "Seleccione una categoria de unidades"
                            },
                            "form-add-product:quantity": {
                                required: "Campo obligatorio"
                            },
                            "form-add-product:subtotal": {
                                required: "Campo Obligatorio"
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
                                if (element.attr("id") == "form-add-product:barcode") {
                                    error.insertAfter($(element).closest(".input-group"));
                                } else
                                    error.insertAfter(element);
                            }
                        }
                    });
                },
                before_save: function () {
                    if ($("#form-add-product").valid()) {
                        save_product();
                    }
                },
                after_save: function () {
                    if ($("#form-add-product\\:valid").val() == "true") {
                        update_products();
                    }
                }
            };
        }()
    };
}();
$(function () {

    $('#form\\:date-issue').datetimepicker({
        pickTime: false,
        format: "DD/MM/YYYY"
    }).focus().nextOnEnter();
    $("#form\\:serie").paddingLeft("0", 4).defaultSelect().nextOnEnter();
    $("#form\\:document-number").paddingLeft("0", 8).defaultSelect().nextOnEnter();
    $("#form").validate({
        errorClass: "has-error text-danger danger",
        validClass: "has-success success",
        errorElement: "em",
        rules: {
            "form:payment-proof": {
                required: true
            },
            "form:serie": {
                required: true,
                minlength: 4
            },
            "form:document-number": {
                required: true
            },
            "form:date-issue": {
                required: true
            },
            "form:disccount": {
                required: true,
                min: 0
            }
        },
        messages: {
            "form:payment-proof": {
                required: "Campo obligatorio"
            },
            "form:serie": {
                required: "Campo obligatorio",
                minlength: "Minimo 4 caracteres"
            },
            "form:document-number": {
                required: "Campo obligatorio",
            },
            "form:date-issue": {
                required: "Campo obligatorio",
                date: "Debe ser una fecha"
            },
            "form:disccount": {
                required: "Campo obligatorio",
                min: "Minimo valor : 0"
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
                error.insertAfter($(element).closest(".form-group").find("span.select2"))
            } else {
                if ($(element).closest(".input-group") != null) {
                    error.insertAfter($(element).closest(".input-group"));
                } else {
                    error.insertAfter(element);
                }
            }
        }

    });
});