var Actor = function () {

    return {
        save: function () {
            if ($("#form").valid()) {
                save();
            }
        },
        back: function () {
            $("#form\\:back").trigger("click")
        },
        refresh: function () {
            refresh();
        }
    };
}();
function change_customer() {
    var customer = $("#form\\:customer");
    if (customer.is(":checked")) {
        $("#btn-customer > span").html("");
        $("#btn-customer").addClass("btn-default").removeClass("btn-alert");
    } else {
        $("#btn-customer > span").html('<i class="fa fs13 fa-times" style="position: absolute;left: 25px;top:10px"></i>');
        $("#btn-customer").addClass("btn-alert").removeClass("btn-default");        
    }
    customer.trigger("click")
}

function change_supplier() {
    var supplier = $("#form\\:supplier");
    if (supplier.is(":checked")) {
        $("#btn-supplier > span").html("");
        $("#btn-supplier").addClass("btn-default").removeClass("btn-alert");

    } else {
        $("#btn-supplier > span").html('<i class="fa fs13 fa-times" style="position: absolute;left: 25px;top:10px"></i>');
        $("#btn-supplier").addClass("btn-alert").removeClass("btn-default");   

    }
    supplier.trigger("click")


}

//$(function () {
//    $("#form").validate({
//        /* @validation states + elements 
//         ------------------------------------------- */
//
//        errorClass: "has-error text-danger",
//        validClass: "has-success",
//        errorElement: "em",
//        /* @validation rules 
//         ------------------------------------------ */
//
//        rules: {
//            "form:identity-document": {
//                required: true
//            },
//            "form:identity-number": {
//                minlength: 2,
//                required: true,
//                integer: true
//            },
//            "form:name": {
//                required: true
//            },
//            "form:customer": {
//                require_from_group: [1, ".rol"]
//            },
//            "form:supplier": {
//                require_from_group: [1, ".rol"]
//            }
//        },
//        messages: {
//            "form:identity-document": {
//                required: "Por favor escoja el documento"
//            },
//            "form:identity-number": {
//                minlength: "Debe ser 2 digitos",
//                required: "Por favor ingrese codigo",
//                integer: "Debe ser 2 digitos"
//            },
//            "form:name": {
//                required: "Por favor ingrese nombre"
//            },
//            "form:customer": {
//                require_from_group: "Seleccione una actividad"
//            },
//            "form:supplier": {
//                require_from_group: "Seleccione una actividad"
//            }
//        },
//        highlight: function (element, errorClass, validClass) {
//            $(element).closest('.form-group').addClass(errorClass).removeClass(validClass);
//            if ($(element).hasClass("select2")) {
//                $(element).addClass("select2-danger").removeClass("select2-success");
//            }
//        },
//        unhighlight: function (element, errorClass, validClass) {
//            $(element).closest('.form-group').removeClass(errorClass).addClass(validClass);
//            if ($(element).hasClass("select2")) {
//                $(element).addClass("select2-success").removeClass("select2-danger");
//            }
//        },
//        errorPlacement: function (error, element) {
//            if (element.is("select")) {
//                error.insertAfter($(element).closest(".form-group").find("span.select2"))
//            } else {
//                error.insertAfter(element);
//            }
//        }
//
//    });
//})