var SaleM = function () {

    return {
        save: function () {
            var valid = true;
            var size = 0;
            $("#formc\\:detail-wrapper .detail-quantity").each(function () {
                if (this.value == "") {
                    valid = false;
                } else {
                    if (parseFloat(this.value) == 0) {
                        valid = false;
                    }
                }
                size++;
            });
            if (!valid) {
                new PNotify({
                    title: 'Valor incorrecto',
                    text: 'Campo de cantidad es obligatorio',
                    shadow: true,
                    opacity: 1,
                    addclass: "stack_top_right",
                    type: "danger",
                    stack: Stacks["stack_top_right"],
                    width: "290px",
                    delay: 1400,
                    icon: "fa fa-warning shaked animated"
                });
                return;
            }
            if (size == 0) {
                new PNotify({
                    title: 'Venta vacia',
                    text: 'Agregue primero productos a la venta',
                    shadow: true,
                    opacity: 1,
                    addclass: "stack_top_right",
                    type: "danger",
                    stack: Stacks["stack_top_right"],
                    width: "290px",
                    delay: 1400,
                    icon: "fa fa-warning shaked animated"
                });
                return;
            }
            save();
        },
        back: function () {
            $("#form\\:back").trigger("click")
        },
        refresh: function () {
            refresh();
        },
        after_search_product: function () {
            if ($("#formc\\:terms-product").val().match(/^[0-9]+$/) != null) {

            }
        },
        configure_detail: function () {
            $("#formc\\:detail-wrapper .detail-quantity")
                    .numeric({scale: 2})
                    .on("input", function () {
                        var quantity;
                        var index = $(this).attr("id").replace("formc:detail:", "").replace(":quantity", "");
                        if (this.value == "") {
                            $("#formc\\:detail\\:" + index + "\\:unite-price").val("0.00");
                            $("#formc\\:detail\\:" + index + "\\:display_unit-price").html("0.00");
                            $("#formc\\:detail\\:" + index + "\\:display_total").html("0.00");
                            $("#formc\\:detail\\:" + index + "\\:subtotal").val("0.00");
                            $("#formc\\:detail\\:" + index + "\\:subtotal").trigger("change");
                            $("#formc\\:detail\\:" + index + "\\:points").val("0.00");
                            $("#formc\\:detail\\:" + index + "\\:points").trigger("change");
                            return;
                        } else {
                            quantity = parseFloat(this.value);
                            if (quantity === 0) {
                                $("#formc\\:detail\\:" + index + "\\:unite-price").val("0.00");
                                $("#formc\\:detail\\:" + index + "\\:display_unit-price").html("0.00");
                                $("#formc\\:detail\\:" + index + "\\:display_total").html("0.00");
                                $("#formc\\:detail\\:" + index + "\\:subtotal").val("0.00");
                                $("#formc\\:detail\\:" + index + "\\:subtotal").trigger("change");
                                $("#formc\\:detail\\:" + index + "\\:points").val("0.00");
                                $("#formc\\:detail\\:" + index + "\\:points").trigger("change");
                                return;
                            }
                        }
                        var max = parseFloat($(this).attr("max"));
                        if (quantity > max) {
                            this.value = max;
                            $(this).trigger("input");
                            new PNotify({
                                title: 'Existencia maxima',
                                text: 'Solo hay en stock ' + max,
                                shadow: true,
                                opacity: 1,
                                addclass: "stack_top_right",
                                type: "danger",
                                stack: Stacks["stack_top_right"],
                                width: "290px",
                                delay: 1400,
                                icon: "fa fa-warning shaked animated"
                            });
                            return;
                        }
                        var size = parseInt($("#formc\\:detail\\:" + index + "\\:price-size").val());
                        var total_price = 0.00;
                        var partial_price = 0;
                        for (var i = size - 1; i >= 0; i--) {
                            var ps_quantity = parseInt($("#formc\\:detail\\:" + index + "\\:price\\:" + i).attr("data-quantity"));
                            var ps_price = parseFloat($("#formc\\:detail\\:" + index + "\\:price\\:" + i).attr("data-price"));

                            if (parseInt(quantity / ps_quantity) === 0) {
                                if (i == 0) {
                                    partial_price += (quantity * ps_price);
                                    total_price += parseFloat((quantity * ps_price).toFixed(2));
                                }
                            } else {
                                total_price += parseFloat((parseInt(quantity / ps_quantity) * ps_price).toFixed(2));
                                if (quantity >= 1 && i == 0) {
                                    partial_price += (parseInt(quantity / ps_quantity) * ps_price);
                                }
                                quantity = quantity % ps_quantity;
                                if (quantity < 1 && i == 0) {
                                    total_price += parseFloat((quantity * ps_price).toFixed(2));
                                }
                            }
                        }
                        $("#formc\\:detail\\:" + index + "\\:unite-price").val((total_price / parseFloat(this.value)).toFixed(2));
                        $("#formc\\:detail\\:" + index + "\\:display_unit-price").html((total_price / parseFloat(this.value)).toFixed(2));
                        $("#formc\\:detail\\:" + index + "\\:display_total").html(total_price.toFixed(2));
                        $("#formc\\:detail\\:" + index + "\\:subtotal").val(total_price.toFixed(2));
                        $("#formc\\:detail\\:" + index + "\\:subtotal").trigger("change");
                        $("#formc\\:detail\\:" + index + "\\:points").val(partial_price.toFixed(2));
                        $("#formc\\:detail\\:" + index + "\\:points").trigger("change");

                    }).pressEnter(function () {
						$("#formc\\:terms-product").focus()
						//var index = parseInt($(this).closest("tr").attr("data-index"));
						//if ($("#formc\\:detail\\:" + (index + 1) + "\\:quantity").length) {
						//	$("#formc\\:detail\\:" + (index + 1) + "\\:quantity").focus()
						//} else {
						//	if ($("#formc\\:discount-points").length) {
						//		$("#formc\\:discount-points").focus();
						//	}
						//}
            });
            $("#formc\\:detail-wrapper .td-other input").on("change", function () {
                SaleM.update_points();
            });
            $("#formc\\:detail-wrapper .td-subtotal input").on("change", function () {
                var subtotal_summary = 0;
                $("#formc\\:detail-wrapper .td-subtotal input").each(function () {
                    var item = parseFloat(this.value);
                    if (!isNaN(item)) {
                        subtotal_summary += item;
                    }
                });
                $("#formc\\:subtotal").val(subtotal_summary.toFixed(2));
                $("#formc\\:subtotal_display").html(subtotal_summary.toFixed(2));
                $("#formc\\:subtotal").trigger("change");
            });
            $("#formc\\:detail-wrapper .subtotal-hidden").first().trigger("change");
        },
        update_subtotal: function () {
            var subtotal_summary = 0;
            $("#formc\\:detail-wrapper .td-subtotal input").each(function () {
                var item = parseFloat(this.value);
                if (!isNaN(item)) {
                    subtotal_summary += item;
                }
            });
            $("#formc\\:subtotal").val(subtotal_summary.toFixed(2));
            $("#formc\\:subtotal_display").html(subtotal_summary.toFixed(2));
            $("#formc\\:subtotal").trigger("change");
        },
        update_points: function () {
            var points = 0;
            $("#formc\\:detail-wrapper .td-other input").each(function () {
                points += parseFloat(this.value);
            });
            $("#formc\\:points").val(parseInt(points));
            $("#formc\\:display_points").html(parseInt(points));
        },
        configure_stock: function () {
            var td_function = function () {
                add_detail([{name: 'index', value: $(this).closest("tr").attr("data-index")}]);
                $(this).closest("tr").find("td").unbind('click')
            }
            var h = parseInt($("#formc\\:product-table-wrapper > table").first().css("height").replace("px", ""));

            h = h - 30;
            $("#formc\\:product-table-wrapper > table tbody").css("height", h + "px");
            $("#formc\\:product-table-wrapper > table .btn-detail").on("click", function () {
                var btn = $(this);
                var next_tr = btn.closest("tr").next("tr");

                if (next_tr.hasClass("hidden")) {
                    next_tr.removeClass("hidden");
                    btn.find("i")
                            .removeClass("fa-plus")
                            .addClass("fa-minus");
                    btn.addClass("btn-danger").removeClass("btn-success")

                } else {
                    next_tr.addClass("hidden");
                    btn.find("i")
                            .addClass("fa-plus")
                            .removeClass("fa-minus");
                    btn.addClass("btn-success").removeClass("btn-danger")
                }
            });
            $("#formc\\:product-table-wrapper > table tr td.data").each(function () {
                var stock_column = $(this);
                var stock_row = stock_column.closest("tr");
                var current_product_id = stock_row.attr("data-p-id");
                var available = true;

                $("#formc\\:detail-wrapper > table tr").each(function () {
                    var product_id = $(this).attr("data-p-id");
                    if (current_product_id == product_id) {
                        available = false;
                    }
                });
                if (available) {
                    stock_column.on("click", td_function).css("cursor", "pointer");
                }
            });
        },
        configure_sale_summary: function () {
            var update_total = function () {
                var subtotal = parseFloat($("#formc\\:subtotal").val());
                var igv = parseFloat($("#formc\\:igv").val());
                var discount = parseFloat($("#formc\\:discount").val());
                var total = subtotal + igv - discount;
                $("#formc\\:total_display").html(total.toFixed(2));
            }
            $("#formc\\:subtotal").on("change", function () {
                var subtotal = parseFloat(this.value);
                var igv = parseFloat(parseFloat(subtotal * parseFloat($("#formc\\:igv-percent").val())).toFixed(2));
                $("#formc\\:igv").val(igv.toFixed(2));
                $("#formc\\:igv_display").html(igv.toFixed(2));
                update_total();
            });
            $("#formc\\:discount").on("change", function () {
                update_total();
            });
        },
        customer: function () {
            return {
                init: function () {
                    $("#form-customer").validate({
                        errorClass: "has-error text-danger",
                        validClass: "has-success",
                        errorElement: "em",
                        rules: {
                            "form-customer:identity-document": {
                                required: true
                            },
                            "form-customer:identity-number": {
                                required: true
                            },
                            "form-customer:other": {
                                require_from_group: [1, ".name"]
                            },
                            "form-customer:name": {
                                require_from_group: [1, ".name"]
                            }
                        },
                        messages: {
                            "form-customer:identity-document": {
                                required: "Campo obligatorio"
                            },
                            "form-customer:identity-number": {
                                required: "Campo obligatorio"
                            },
                            "form-customer:other": {
                                require_from_group: "Campo obligatorio"
                            },
                            "form-customer:name": {
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
                    if ($("#form-customer").valid()) {
                        save_customer();
                    }
                },
                after_save: function () {
                    if ($("#form-customer\\:valid").val() == "true") {
                        update_customer();
                    }
                }
            };
        }()
    };
}();