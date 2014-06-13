$(document).ready(function () {
	var one = $('#content');
	
    one.onresize = function () {
    	getRemainingSpace();
    };
});


function getRemainingSpace(){
	var contentHeight = $('#content').height();
	var remaining_height = parseInt(contentHeight - 400); 
	$('#current_polls').height(remaining_height); 
	alert(remaining_height);
}