var ExpensedManaged = function() {
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
            $("#form-managed\\:date-payment").datetimepicker({
				format: "DD/MM/YYYY hh:mm a",
				defaultDate: new Date()				
			}).pressEnter(function(){
				$("#form-managed\\:quantity").focus();
			});
            $("#form-managed\\:quantity").numeric({
				scale: 2,
				negative:false	
			}).pressEnter(function(){
				$("#form-managed\\:description").focus();
			});
            $("#form-managed\\:description").pressEnter(function(){
				$("#btn-save-manage").trigger("click");
			});
			$("#form-managed").validate({
                errorClass: "has-error text-danger",
                validClass: "has-success",
                errorElement: "em",
                rules: {
                    "form-managed:date-payment": {
                        required: true
                    },
                    "form-managed:quantity": {
                        required: true
                    },
                    "form-managed:description": {
                        required: true
                    }
                },
                messages: {
                    "form-managed:date-payment": {
                        required: "Campo obligatorio"
                    },
                    "form-managed:quantity": {
                        required: "Campo obligatorio"
                    },
                    "form-managed:description": {
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