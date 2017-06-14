/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var SM = function() {
	
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
        configure_public_carrier: function() {
			$("#form\\:carrier").select2({
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
                        text = d[2];
                    }
                    return text;
                }
            });
        },
        stockSearch: function() {
            return {
                init: function() {
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
					$("#form-add .quantity").pressEnter(function(){
						
						
					})
                },
                save: function() {
                    if ($("#form-add").valid()) {
                        add_items();
                    }
                },
				after_search: function() {
					var row = $("#form-add\\:product-table-wrapper > table tbody tr:not(.hidden)").first();
                    if (row.length) {
                        var id = row.attr('data-index');
                        $("#form-add\\:data\\:" + id + "\\:quantity").focus();
                    }
                }
            };
        }(),
        carrierManage: function() {
            return {
                init: function() {
                    $("#form-carrier\\:ruc").numeric({
                        precision: 11,
                        scale: 0,
                        negative: false
                    });
                    $("#form-carrier").validate({
                        errorClass: "has-error text-danger",
                        validClass: "has-success",
                        errorElement: "em",
                        rules: {
                            "form-carrier:ruc": {
                                minlength: 11,
                                required: true
                            },
                            "form-carrier:name": {
                                required: true
                            }
                        },
                        messages: {
                            "form-carrier:ruc": {
                                minlength: "El ruc son 11 digitos",
                                required: "Este campo es obligatorio"
                            },
                            "form-carrier:name": {
                                required: "Este campo es obligatorio"
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
                    if ($('#form-carrier').valid()) {
                        save_carrier();
                    }
                }
            };
        }()
    };
}();
$(function() {
    $("#form\\:date-shipping").datetimepicker({
        format:'DD/MM/YYYY',
		pickTime:false
    }).pressEnter(function(){
		$("#form\\:date-arrival").focus();
	});
    $("#form\\:date-arrival").datetimepicker({
        format:'DD/MM/YYYY',
		pickTime:false
    });
    $("#form").validate({
        errorClass: "has-error text-danger danger",
        validClass: "has-success success",
        errorElement: "em",
        rules: {
			"form:target":{
				required:true
			},
			"form:carrier":{
				required:true
			},
            "form:date-shipping": {
                required: true
            },
            "form:date-arrival": {
                required: true
            },
            "form:payment-proof": {
                required: true
            },
            "form:serie": {
                required: true,
                minlength: 4
            },
            "form:document-number": {
                required: true,
            },
            "form:driver-license": {
                required: true,
            },
            "form:transport-description": {
                required: true,
            }
        },
        messages: {
			"form:target":{
				required: "Campo obligatorio"
			},
			"form:carrier":{
				required: "Campo obligatorio"
			},
            "form:date-shipping": {
                required: "Campo obligatorio"
            },
            "form:date-arrival": {
                required: "Campo obligatorio"
            },
            "form:payment-proof": {
                required: "Campo obligatorio"
            },
            "form:serie": {
                required: "Campo obligatorio",
                minlength: "Minimo 4 letras o digitos"
            },
            "form:document-number": {
                required: "Campo obligatorio",
            },
            "form:driver-license": {
                required: "Campo Obligatorio",
            },
            "form:transport-description": {
                required: "Campo Obligatorio",
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