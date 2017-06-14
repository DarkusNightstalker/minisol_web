var load_element;
$(function() {
    load_element = $('<i class="fa fa-spinner fa-spin fa-3x fa-fw"></i>');
})
function testHoldon(themeName,Title) {
    HoldOn.open({
        theme: themeName,
        message: "<h4>"+Title+" . . .</h4>"
    });

//    setTimeout(function() {
//        HoldOn.close();
//    }, 10800);
}
function testHoldonClose() {
    HoldOn.close();
}
function begin_load(element) {
    var jElement;
	if (element) {
		if (typeof element === 'string' || element instanceof String) {
			jElement = $(document.getElementById(element));
		} else {
			jElement = $(element);
		}
		jElement.append('<i class="loader-spin fa fa-refresh fa-spin fa-fw"></i>');
		jElement.prop("disabled",true);
	}
    Pace.restart();
    testHoldon('sk-dot','Cargando');
}
function end_load(element) {
    var jElement;
	if (element) {
		if (typeof element === 'string' || element instanceof String) {
			jElement = $(document.getElementById(element));
		} else {
			jElement = $(element);
		}		
		jElement.remove(".loader-spin");
		jElement.prop("disabled",false);
	}
    Pace.stop();
    testHoldonClose();

}

function error() {
    location.reload();
}