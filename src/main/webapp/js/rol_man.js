$(function () {
    $('#modal-managed').on('shown.bs.modal', function () {
        $('#form-managed\\:name').focus();
    });
});
var RolManaged = function () {
    return {
        before_save: function () {
            if ($("#form-managed").valid()) {
                save_user();
            }
        },
        after_save: function () {
            if ($("#form-managed\\:valid").val() == "true") {
                update_users();
            }
        },
		all_selected: function (elem) {
			$(elem).closest("table").find(".permission-ck").prop("checked",true).trigger("change");
        },
		all_disselected: function (elem) {
			$(elem).closest("table").find(".permission-ck").prop("checked",false).trigger("change");
        },
        configure_permissions: function () {
			
                                        $(".permission-ck").on("change", function () {
                                            if (this.checked) {
												if($(this).closest("td").find("i").length == 0){
													$(this).closest("td").append("<i class='fa text-success fa-check ck-confirm'/>");
													$(this).closest("tr").addClass("success")
												}
                                            } else {
                                                $(this).closest("td").find(".ck-confirm").remove();
												$(this).closest("tr").removeClass("success")
                                            }
                                        }).trigger("change").closest("tr").on("click", function () {                                            
                                            $(this).find(".permission-ck").trigger("click");
                                        });
        },
        init: function (context_path) {
            $("#form-managed").validate({
                errorClass: "has-error text-danger",
                validClass: "has-success",
                errorElement: "em",
                rules: {
                    "form-managed:name": {
                        required: true
                    }
                },
                messages: {
                    "form-managed:name": {
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