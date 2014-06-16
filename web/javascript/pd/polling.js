/**
 * checks how much space is remaining for the polling
 */
function getRemainingSpace(){
	var contentHeight = $('#content').height();
	var awayAreaHeight = $('#awayArea_info').height();
	var balloonIdleHeight = $('#balloonIdle').height();
	var remaining_height = parseInt(contentHeight - awayAreaHeight - balloonIdleHeight - 400 - 32); 
	$('#current_polls').css('height', remaining_height);
	loadPolls();
}

/**
 * load all polls 
 */
function loadPolls(){
	doTask("read_all_polls", null, function (data){
		if(data.object.length == null){
			var noPollsInDatabase = "There are currently no polls.";
			document.getElementById("current_polls").innerHTML = noPollsInDatabase;
		}else{
			var polls = "";
			for (var i = 0; i < data.object.length; i++) {
				
//				polls += "<div id='poll_"+i"'>"+data.object[i].icon+"<br>"+data.object[i].question+"</div>";

				polls += "<div id='poll_"+ i +"'>";
				polls += data.object[i].icon+"  "+data.object[i].question+"<br>";
				//if null
				for(var j = 0; j < data.object.user.length; j++){
					polls += data.object.user[j].icon; //User
				}
				
					
				polls += "<hr>";
				polls += "</div>";
		        document.getElementById("current_polls").innerHTML = polls;
			
			}
		}
	});
}

function addPollsToUser(){
	
}