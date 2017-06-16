var SaleVerification = function () {
    return {
        before_verify_vouchers: function () {
            App.validateForm("formv-info", proceed);
        },
        after_verify_vouchers: function () {
            if ($("#formv-info\\:valid").val() == "true") {
                after_verify_vouchers();
            }
        },
        init: function () {
            $("#formv-info").validate({
                errorClass: "has-error text-danger",
                validClass: "has-success",
                errorElement: "em",
                rules: {

                },
                messages: {

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
        },
        async: function () {
            var new_data = $("#formv\\:refresher div");
            if (new_data.length == 0) {
                setTimeout(function () {
                    verify_update();
                }, 1000);
            } else {
                new_data.each(function () {
                    var i = 0;
                    var data = new Array();
                    $(this).find("span").each(function () {
                        data[i] = $(this).text();
                        i++;
                    });
                    var markup = "<tr data-id='" + data[0] + "'>";
                    markup += "<td class='text-center fs11 ptn pbn'>" + data[1] + "</td>";
                    markup += "<td class='text-center fs11 ptn pbn'>" + data[2] + "</td>";
                    markup += "<td class='text-left fs11 ptn pbn'>" + data[3] + "</td>";
                    markup += "<td class='text-right fs11 ptn pbn'>S/. " + data[4] + "</td>";

                    markup += "<td class='text-center ptn pbn' style=''>";
                    markup += "<div class='btn-group' style=''>";
                    markup += "    <button onclick='SaleVerification.verify_contado(" + data[0] + ")'       type='button' title='AL CONTADO' class='btn btn-info w50 pln prn'><i class='fa fa-2x fa-dollar' /></button>";
                    markup += "    <button onclick='SaleVerification.consumption_voucher(" + data[0] + ")'  type='button' title='VALES DE COMPRA' class='btn btn-dark  w50 pln prn'><i class='fa fa-2x fa-money'/></button>";
                    markup += "    <button onclick='SaleVerification.verify_credit(" + data[0] + ")'        type='button' title='CREDITO' class='btn btn-system w50 pln prn'><i class='fa fa-2x fa-clock-o'/></button>";
                    markup += "    <button onclick='SaleVerification.delete(" + data[0] + ")'               type='button' title='ANULAR' class='btn btn-danger w50 pln prn' ><i class='fa fa-2x fa-times'/></button>";
                    markup += " </div>";
                    markup += "</td>";

                    markup += "</tr>";

                    $('#formv\\:table > tbody').append(markup);
                });
                $('#formv\\:table  tfoot').addClass('hidden');
                $("#formv\\:refresher div").remove();
                verify_update();
            }
        },
        verify_contado: function (id) {
            send_verify_ct([{name: 'id', value: id}]);
        },
        verify_credit: function (id) {
            send_verify_cr([{name: 'id', value: id}]);
        },
        consumption_voucher: function (id) {
            send_consumption_voucher([{name: 'id', value: id}]);
        },
        delete: function (id) {
            delete_verify([{name: 'id', value: id}]);
        },
        after_print: function (id) {
            $('#formv\\:table > tbody tr[data-id = ' + id + ']').remove();
            if ($('#formv\\:table > tbody tr').length == 0) {
                $('#formv\\:table > tfoot').removeClass('hidden');
            }
            $('#modal-verify-info').modal('hide');
        },
        after_delete: function (id) {
            $('#formv\\:table > tbody tr[data-id = ' + id + ']').remove();
            if ($('#formv\\:table > tbody tr').length == 0) {
                $('#formv\\:table > tfoot').removeClass('hidden');
            }
        }
    };
}();
function open_verify_sale() {
    $('#modal-verify-sale').modal({
        backdrop: 'static',
        keyboard: false
    });
}
function open_verify_info() {
    $('#modal-verify-info').modal({
        backdrop: 'static',
        keyboard: false
    });
}
$(function () {
    $('#modal-verify-sale').on('shown.bs.modal', function () {
        $('#formv\\:terms').focus();
        verify_update();
    });
    $('#modal-verify-info').on('shown.bs.modal', function () {
        if ($('#formv-info .payment-voucher').length) {
            $('#formv-info .payment-voucher').first().focus();
        } else {
            $('#formv-info\\:proceed').focus();
        }
    }).on('hidden.bs.modal', function () {
        $('#formv\\:table > tbody tr button[data-op=contado]').first().focus();
    })
});