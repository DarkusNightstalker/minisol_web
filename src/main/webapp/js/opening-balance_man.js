var OpeningBalanceManaged = function() {
    return {
        before_save: function () {
            if ($("#form-managed").valid()) {
                save_opening_balance();
            }
        },
        after_save: function () {
            if ($("#form-managed\\:valid").val() == "true") {
                update_opening_balance();
            }
        },
        init: function() {
            
			$("#form-managed").validate({
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
        }
    };
}();