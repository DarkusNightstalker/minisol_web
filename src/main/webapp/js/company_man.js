var Company = function () {

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
        },
        change_function: function (type,style) {
			var sold = $("#form\\:"+type);
			if (sold.is(":checked")) {
				$("#btn-"+type+" > span").html("");
				$("#btn-"+type).addClass("btn-default").removeClass("btn-"+style);
			} else {
				$("#btn-"+type+" > span").html('<i class="fa fs13 fa-times" style="position: absolute;left: 25px;top:10px"></i>');
				$("#btn-"+type).addClass("btn-"+style).removeClass("btn-default");        
			}
			sold.trigger("click")
        }
    };
}();
$(function () {
    $("#form").validate({
        errorClass: "has-error text-danger",
        validClass: "has-success",
        errorElement: "em",
        rules: {
            "form:ruc": {
                minlength: 11,
                required: true,
                maxlength: 11
            },
            "form:name": {
                required: true
            },
            "form:comercial-name": {
                required: true
            },
            "form:sold": {
                require_from_group: [1, ".rol"]
            },
            "form:buy": {
                require_from_group: [1, ".rol"]
            },
            "form:stored": {
                require_from_group: [1, ".rol"]
            },
            "form:region": {
                required: true
            },
            "form:subregion": {
                required: true
            },
            "form:district": {
                required: true
            },
            "form:city": {
                required: true
            },
            "form:address": {
                required: true
            },
        },
        messages: {
            "form:ruc": {
                minlength: "Minimo 11 numeros",
                required: "Campo obligatorio",
                maxlength: "Maximo 11 numeros"
            },
            "form:name": {
                required: "Campo obligatorio",
            },
            "form:comercial-name": {
                required: "Campo obligatorio",
            },
            "form:sold": {
                require_from_group: "Campo obligatorio"
            },
            "form:buy": {
                require_from_group: "Campo obligatorio"
            },
            "form:stored": {
                require_from_group: "Campo obligatorio"
            },
            "form:region": {
                required: "Campo obligatorio"
            },
            "form:subregion": {
                required: "Campo obligatorio"
            },
            "form:district": {
                required: "Campo obligatorio"
            },
            "form:city": {
                required: "Campo obligatorio"
            },
            "form:address": {
                required: "Campo obligatorio"
            },
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
                error.insertAfter(element);
            }
        }

    });
})
