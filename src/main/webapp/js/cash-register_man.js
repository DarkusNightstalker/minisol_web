var CashRegisterManaged = function () {
    return {
        before_save: function () {
            if ($("#form-manage").valid()) {
                save_payment();
            }
        },
        after_save: function () {
            if ($("#form-manage\\:valid").val() == "true") {
                update_payment();
            }
        },
        refresh: function () {
            refresh();
        },
        init: function () {
            var calculate = function () {
                var total = 0;
                var initial_cash = 0;// parseFloat($("#form-manage\\:initial-cash").val());
                $("#form-manage .cash-type").each(function () {
                    var value = parseInt($(this).val());
                    var factor = parseFloat($(this).attr("data-cash-value"));
                    if (isNaN(value))
                        value = 0;
                    total += value * factor;
                });
                if (isNaN(initial_cash))
                    initial_cash = 0;
                total += initial_cash;
                $("#form-manage\\:table-vouchers > tbody tr").each(function () {
                    var index = $(this).attr("data-index")
                    var value = parseInt($("#form-manage\\:vouchers\\:" + index + "\\:value").val());
                    if (isNaN(value))
                        value = 0;
                    var quantity = parseInt($("#form-manage\\:vouchers\\:" + index + "\\:quantity").val());
                    if (isNaN(quantity))
                        quantity = 0;
                    var subbtotal = (value * quantity);
                    $(this).find(".voucher-total").text(subbtotal.toFixed(2))
                    total += subbtotal
                });
                var visa = parseFloat($("#form-manage\\:visa").val());
                if (isNaN(visa))
                    visa = 0;
                total += visa;
                $("#form-manage\\:real-cash").val(total.toFixed(2));
                $("#form-manage\\:display-real-cash").text("S/. " + total.toFixed(2));
            };

            $("input[data-cash=0]").focus();

            $("#form-manage .cash-type").numeric({
                negative: false,
                scale: 0
            }).pressEnter(function () {
                var index = parseInt($(this).attr("data-cash"));
                var next = $("input[data-cash=" + (index + 1) + "]");
                if (next.length) {
                    next.focus();
                } else {
                    $("#form-manage\\:table-vouchers > tbody tr[data-index=0] input.voucher-value").focus();
                }
            }).on("input", calculate).each(function () {
                if (this.value == "0") {
                    this.value = "";
                }
            });
            $("#form-manage\\:table-vouchers .voucher-value").on("input", calculate).numeric({
                scale: 0,
                negative: false
            }).pressEnter(function () {
                $(this).closest("tr").find(".voucher-quantity").focus();
            });
            $("#form-manage\\:table-vouchers .voucher-quantity").on("input", calculate).numeric({
                scale: 0,
                negative: false
            }).pressEnter(function () {
                var index = parseInt($(this).closest("tr").attr("data-index"))
                var next_row = $("#form-manage\\:table-vouchers > tbody tr[data-index=" + (index + 1) + "]");
                if (next_row.length) {
                    next_row.find(".voucher-value").focus();
                } else {
                    CashRegisterManaged.before_save();
                }
            });
            $("#form-manage").validate({
                errorClass: "has-error text-danger",
                validClass: "has-success",
                errorElement: "em",
                rules: {
                    "form-manage:initial-cash": {
                        required: true
                    }
                },
                messages: {
                    "form-manage:initial-cash": {
                        required: true
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
}();