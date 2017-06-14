
var Report = function () {
    return {
        configure: function (form_id) {
            $("#" + form_id + " input.dt").datetimepicker({
                format: "DD/MM/YYYY hh:mm a"
            });
            $("#" + form_id + " input.d").datetimepicker({
                format: "DD/MM/YYYY",
                pickTime: false
            });
            $("#" + form_id + " input,#" + form_id + " select").not(document.getElementsByClassName("none-pass")).bind("keypress", function (e) {
                if (e.keyCode == 13) {
                    var inps = $("#" + form_id + " input,#" + form_id + " select"); //add select too
                    for (var x = 0; x < inps.length; x++) {
                        if (inps[x] == this) {
                            while ((inps[x]).name == (inps[x + 1]).name) {
                                x++;
                            }
                            if ((x + 1) < inps.length)
                                $(inps[x + 1]).focus();
                        }
                    }
                    e.preventDefault();
                }
            });
            $("#" + form_id).validate({
                errorClass: "has-error text-danger",
                validClass: "has-success",
                errorElement: "em",
                highlight: function (element, errorClass, validClass) {
                    $(element).closest('.form-group').addClass(errorClass).removeClass(validClass);
                },
                unhighlight: function (element, errorClass, validClass) {
                    $(element).closest('.form-group').removeClass(errorClass).addClass(validClass);
                },
                errorPlacement: function (error, element) {
                    var ipt = $(element).closest(".input-group");
                    if (ipt != null && ipt.hasClass("input-group")) {
                        error.insertAfter(ipt);
                    } else {
                        error.insertAfter(element);
                    }
                }
            });
            $("#" + form_id).find("button[data-exp=pdf]").on("click", function () {
                App.validateForm(form_id, function () {
                    $("#" + form_id + "\\:pdf").trigger("click");
                });
            });
            $("#" + form_id).find("button[data-exp=xlsx]").on("click", function () {
                App.validateForm(form_id, function () {
                    $("#" + form_id + "\\:xlsx").trigger("click");
                });
            });
            $("#" + form_id).find("button[data-exp=docx]").on("click", function () {
                App.validateForm(form_id, function () {
                    $("#" + form_id + "\\:docx").trigger("click");
                });

            });
            $("#" + form_id).find("button[data-exp=html]").on("click", function () {
                App.validateForm(form_id, function () {
                    $("#" + form_id + "\\:html").trigger("click");
                });
            });
        },
        manyToOneField: function (element, value, target) {
            var str_value = $(document.getElementById(target)).val();
            var target_value = str_value.length == 0 ? new Array() : $(document.getElementById(target)).val().split('::');
            if ($(element).is(":checked"))
            {
                target_value.push(value);
            } else {
                if (target_value.indexOf(value) != -1)
                    target_value.splice(target_value.indexOf(value), 1);
            }
            $(document.getElementById(target)).val(target_value.join('::'));
        },
        clean: function (form_id) {
            try {
                $("#" + form_id).validate().resetForm();
                $("#" + form_id + " .form-group").removeClass("has-error text-danger has-success")
            } catch (exception) {

            }
            $("#" + form_id + " input[type=checkbox]").each(function () {
                $(this).prop('checked', false);
                $(this).trigger("change");
            });
            $("#" + form_id + " input[type=text]").each(function () {
                this.value = "";
            });
        }
    };
}();