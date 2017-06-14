/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var SR = function() {
	
    padding_right = function(s, c, n) {
        if (!s || !c || s.length >= n) {
            return s;
        }
        var max = (n - s.length) / c.length;
        for (var i = 0; i < max; i++) {
            s += c;
        }
        return s;
    }
    return {
        save: function() {
            if ($("#form").valid()) {
                save();
            }
        },
        back: function() {
            back();
        },
        refresh: function() {
            refresh();
        },
        configure_responsible: function() {
			$("#form\\:responsible").select2({
                placeholder: "Seleccione un transporte",
                width: "100%",
                allowClear: true,
                containerCssClass: ":all:",
                templateResult: function(data) {
                    var text = data.text;
                    if (data.id) {
                        var d = data.text.split("||");
                        text = $("<div>"+d[2]+"<br/><span  class='fs8'><b>" + d[0] + "</b>&nbsp;&nbsp;" + d[1] + "</span></div>");
                    }
                    return text;
                },
                templateSelection: function(data) {
                    var text = data.text;
                    if (data.id) {
                        var d = data.text.split("||");
                        text = $("<div>"+d[2]+" <span  class='fs8'><b>" + d[0] + "</b>&nbsp;&nbsp;" + d[1] + "</span></div>");
                    }
                    return text;
                }
            });
        },
        stockSearch: function() {
            return {
                init: function() {
					$("#form-add\\:search").on("input",function(){
						var value = $(this).val();
                        $("#form-add table tr").each(function (index) {
                            if (index != 0) {

                                $row = $(this);

                                var t = $row.text();
                                var r = t.search(value);
                                if (r == -1) {
                                    $(this).addClass("hidden");
                                } else {
                                    $(this).removeClass("hidden");
                                }
                            }
                        });
					});
                    $("#form-add").validate({
                        errorClass: "has-error text-danger",
                        validClass: "has-success",
                        errorElement: "em",
                        highlight: function(element, errorClass, validClass) {
                            $(element).closest('.form-group').addClass(errorClass).removeClass(validClass);
                            if ($(element).hasClass("select2")) {
                                $(element).addClass("select2-danger").removeClass("select2-success");
                            }
                        },
                        unhighlight: function(element, errorClass, validClass) {
                            $(element).closest('.form-group').removeClass(errorClass).addClass(validClass);
                            if ($(element).hasClass("select2")) {
                                $(element).addClass("select2-success").removeClass("select2-danger");
                            }
                        },
                        errorPlacement: function(error, element) {
                            if (element.is("select")) {
                                error.insertAfter($(element).closest(".form-group").find("span.select2"))
                            } else {
                                error.insertAfter(element);
                            }
                        }
                    });
                },
                save: function() {
                    if ($("#form-add").valid()) {
                        add_items();
                    }
                }
            };
        }(),
        responsible: function() {
            return {
                init: function() {
                    $("#form-responsible").validate({
                        errorClass: "has-error text-danger",
                        validClass: "has-success",
                        errorElement: "em",
                        rules: {
                            "form-responsible:identity-document": {
                                required: true
                            },
                            "form-responsible:identity-number": {
                                required: true
                            },
                            "form-responsible:other": {
                                require_from_group: [1, ".name"]
                            },
                            "form-responsible:name": {
                                require_from_group: [1, ".name"]
                            }
                        },
                        messages: {
                            "form-responsible:identity-document": {
                                required: "Campo obligatorio"
                            },
                            "form-responsible:identity-number": {
                                required: "Campo obligatorio"
                            },
                            "form-responsible:other": {
                                require_from_group: "Campo obligatorio"
                            },
                            "form-responsible:name": {
                                require_from_group: "Campo obligatorio"
                            }
                        },
                        highlight: function(element, errorClass, validClass) {
                            $(element).closest('.form-group').addClass(errorClass).removeClass(validClass);
                            if ($(element).hasClass("select2")) {
                                $(element).addClass("select2-danger").removeClass("select2-success");
                            }
                        },
                        unhighlight: function(element, errorClass, validClass) {
                            $(element).closest('.form-group').removeClass(errorClass).addClass(validClass);
                            if ($(element).hasClass("select2")) {
                                $(element).addClass("select2-danger").removeClass("select2-success");
                            }
                        },
                        errorPlacement: function(error, element) {
                            error.insertAfter(element);
                            if (element.is("select")) {
                                error.insertAfter($(element).closest(".form-group").find("span.select2"));
                            } else {
                                error.insertAfter(element);
                            }
                        }
                    });
                },
                before_save: function() {
                    if ($("#form-responsible").valid()) {
                        save_customer();
                    }
                },
                after_save: function() {
                    if ($("#form-responsible\\:valid").val() == "true") {
                        update_customer();
                    }
                }
            };
        }(),
    };
}();
$(function() {
    $("#form\\:date-issue").datetimepicker({
        format:'DD/MM/YYYY',
		pickTime:false
    });
    $("#form\\:document-number").numeric({
        scale:0,
		negative:false
    });
    $("#form").validate({
        errorClass: "has-error text-danger danger",
        validClass: "has-success success",
        errorElement: "em",
        rules: {
			"form:responsible":{
				required:true
			},
            "form:date-issue": {
                required: true
            },
            "form:serie": {
                required: true,
                minlength: 4
            },
            "form:document-number": {
                required: true,
            }
        },
        messages: {
			"form:responsible":{
				required: "Campo obligatorio"
			},
            "form:date-issue": {
                required: "Campo obligatorio"
            },
            "form:serie": {
                required: "Campo obligatorio",
                minlength: "Minimo 4 letras"
            },
            "form:document-number": {
                required: "Campo obligatorio",
            }
        },
        highlight: function(element, errorClass, validClass) {
			if($(element).hasClass("detail-item")){
				$(element).closest('td').addClass(errorClass).removeClass(validClass)
			}else{
				$(element).closest('.form-group').addClass(errorClass).removeClass(validClass);
			}
            if ($(element).hasClass("select2")) {
                $(element).addClass("select2-danger").removeClass("select2-success");
            }
        },
        unhighlight: function(element, errorClass, validClass) {
			if($(element).hasClass("detail-item")){
				$(element).closest('td').removeClass(errorClass).addClass(validClass);
			}else{
				$(element).closest('.form-group').removeClass(errorClass).addClass(validClass);
			}

            if ($(element).hasClass("select2")) {
                $(element).addClass("select2-success").removeClass("select2-danger");
            }
        },
        errorPlacement: function(error, element) {
            if (element.is("select")) {
				if(element.hasClass("detail-item")){
                    error.insertAfter(element);
				}else
                error.insertAfter($(element).closest(".form-group").find("span.select2"))
            } else {
				var ipt =$(element).closest(".input-group");
                if (ipt != null && ipt.hasClass("input-group")) {
                    error.insertAfter(ipt);
                } else {
                    error.insertAfter(element);
                }
            }
        }

    });
});