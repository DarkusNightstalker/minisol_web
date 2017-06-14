var PaymentVoucherManaged = function() {
    return {
        before_save: function () {
            if ($("#form-managed").valid()) {
                save_payment();
            }
        },
        after_save: function () {
            if ($("#form-managed\\:valid").val() == "true") {
                update_payment();
            }
        },
        init: function() {
            $("#form-managed\\:date-expiry").datetimepicker({
				format: "DD/MM/YYYY",
				pickTime: false			
			}).pressEnter(function(){
				$("#form-managed\\:value").focus();
			});
            $("#form-managed\\:quantity").numeric({
				scale: 0,
				negative:false	
			}).pressEnter(function(){
				$("#form-managed\\:date-expiry").focus();
			}).on("blur",function(){
				update_trama();
			});
			$("#form-managed\\:prefix").pressEnter(function(){
				$("#form-managed\\:quantity").focus();
			}).on("blur",function(){
				update_trama();
			});
            
			$("#form-managed\\:value").pressEnter(function(){
				$("#btn-save-manage").trigger("click");
			});
			$("#form-managed").validate({
                errorClass: "has-error text-danger",
                validClass: "has-success",
                errorElement: "em",
                rules: {
                    "form-managed:date-expiry": {
                        required: true
                    },
                    "form-managed:quantity": {
                        required: true
                    },
                    "form-managed:value": {
                        required: true
                    },
                    "form-managed:prefix": {
                        required: true
                    }
                },
                messages: {
                    "form-managed:date-expiry": {
                        required: "Campo obligatorio"
                    },
                    "form-managed:quantity": {
                        required: "Campo obligatorio"
                    },
                    "form-managed:value": {
                        required: "Campo obligatorio"
                    },
                    "form-managed:prefix": {
                        required: "Campo obligatorio"
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