/**
 * checks how much space is remaining for the polling
 */
function getRemainingSpace(){
	var contentHeight = $('#content').height();
	var awayAreaHeight = $('#awayArea_info').height();
//	var balloonIdleHeight = $('#balloonIdle').height();
	var supportHeight = $('#balloonIdle').height();
	var remaining_height = parseInt(contentHeight - awayAreaHeight - supportHeight - 400 - 32); 
	$('#current_polls').css('height', remaining_height);
}