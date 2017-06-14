var ElectronicBook = function() {
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
            App.validateForm('form',save);
        },
        back: function() {
            back();
        },
        refresh: function() {
            refresh();
        },
        configure_category: function() {
            $("#form\\:category").select2({
                placeholder: "",
                width: "100%",
                allowClear: true,
                containerCssClass: ":all:",
                templateResult: function(data) {
                    var text = data.text;
                    if (data.id) {
                        var d = data.text.split("||");
                        d[0] = padding_right(d[0], " ", 8);
                        d[0] = d[0].replace(/\s/g, "&nbsp;");
                        text = $("<div><b  class='fs10'>" + d[0] + "</b>" + d[1] + "</div>");
                    }
                    return text;
                },
                templateSelection: function(data) {
                    var text = data.text;

                    if (data.id) {
                        var d = data.text.split("||");
                        d[0] = padding_right(d[0], " ", 8);
                        d[0] = d[0].replace(/\s/g, "&nbsp;");
                        text = $("<div><b  class='fs10'>" + d[0] + "</b>" + d[1] + "</div>");
                    }
                    return text;
                }
            });
        },
        electronicBookCategory: function() {
            return {
                init: function() {
                    $("#form-category").validate({
                        errorClass: "has-error text-danger",
                        validClass: "has-success",
                        errorElement: "em",
                        rules: {
                            "form:code": {
                                required: true,
                                maxlength: 6
                            },
                            "form:name": {
                                required: true,
                                maxlength: 255
                            }
                        },
                        messages: {
                            "form:code": {
                                required: "Campo obligatorio",
                                maxlength: "Tamaño maximo de 6 caracteres",
                            },
                            "form:name": {
                                required: "Campo obligatorio",
                                maxlength: "Tamaño maximo de 255 caracteres"
                            }
                        },
                        highlight: function(element, errorClass, validClass) {
                            $(element).closest('.form-group').addClass(errorClass).removeClass(validClass);
                        },
                        unhighlight: function(element, errorClass, validClass) {
                            $(element).closest('.form-group').removeClass(errorClass).addClass(validClass);
                        },
                        errorPlacement: function(error, element) {
                            error.insertAfter(element);
                        }
                    });
                },
                before_save: function() {
                    App.validateForm('form-category',save_category);
                },
                after_save: function() {
                    if ($("#form-category\\:valid").val() == "true") {
                        update_categories();
                    }
                },
                configure_parents: function() {
                    $("#form-category\\:parent").select2({
                        placeholder: "",
                        width: "100%",
                        allowClear: true,
                        containerCssClass: ":all:",
                        templateResult: function(data) {
                            var text = data.text;
                            if (data.id) {
                                var d = data.text.split("||");
                                d[0] = padding_right(d[0], " ", 8);
                                d[0] = d[0].replace(/\s/g, "&nbsp;");
                                text = $("<div><b  class='fs10'>" + d[0].replace(" ", "&nbsp;") + "</b>" + d[1] + "</div>");
                            }
                            return text;
                        },
                        templateSelection: function(data) {
                            var text = data.text;

                            if (data.id) {
                                var d = data.text.split("||");
                                d[0] = padding_right(d[0], " ", 8);
                                d[0] = d[0].replace(/\s/g, "&nbsp;");
                                text = $("<div><b  class='fs10'>" + d[0].replace(" ", "&nbsp;") + "</b>" + d[1] + "</div>");
                            }
                            return text;
                        }
                    });
                },
            };
        }()
    };
}();

$(function() {
    $("#form\\:code")
            .nextOnEnter()
            .focus();
    $("#form\\:name")
            .pressEnter(ElectronicBook.save);
	
    $("#form").validate({
        errorClass: "has-error text-danger",
        validClass: "has-success",
        errorElement: "em",
        rules: {
            "form:category": {
                required: true
            },
            "form:code": {
                required: true,
                maxlength: 8
            },
            "form:name": {
                required: true,
                maxlength: 255
            }
        },
        messages: {
            "form:category": {
                required: "Campo Obligatorio"
            },
            "form:code": {
                required: "Campo obligatorio",
                maxlength: "Tamaño maximo de 8 caracteres",
            },
            "form:name": {
                required: "Campo obligatorio",
                maxlength: "Tamaño maximo de 255 caracteres"
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
                error.insertAfter($(element).closest(".form-group").find("span.select2"));
            } else {
                error.insertAfter(element);
            }
        }
    });
})