/**
 * checks how much space is remaining for the polling
 */
function getRemainingSpace(){
	var contentHeight = $('#content').height();
	var awayAreaHeight = $('#awayArea_info').height();
//	var balloonIdleHeight = $('#balloonIdle').height();
	var supportHeight = $('#balloonIdle').height();
	var remaining_height = parseInt(contentHeight - awayAreaHeight - supportHeight - 200 - 32 - 5); //-400 if privacy/login is shown
	$('#current_polls').css('height', remaining_height);
//	loadPolls();
	addPollsToUser();
}


var userPollsList = [];
/**
 * load all polls (initiates to start the placement on the display in the callback)
 * @param callback, callbackdata
 */
function loadPolls(callback, callbackdata){
	userPollsList = [];
	doTask("read_all_polls", null, function (data){
		//no polls in database
		if(data.object.length == 0){
			var noPollsInDatabase = "There are currently no polls.";
			document.getElementById("current_polls").innerHTML = noPollsInDatabase;
		//polls in database
		}else{
			var polls = "";
			//all polls
			for (var i = 0; i < data.object.length; i++) {	
				polls += "<div class='sortedPolls_newest' id='poll_"+ i +"' data-datetime_created='"+data.object[i].created+"' data-datetime_end='"+data.object[i].end+"'>";
				polls += "<table><tr><td><img style='width:72px;' src="+data.object[i].icon+"></td>";
				//cut if too long
				if(data.object[i].question.length <= 50){
					polls += "<td><p style='margin:0px; margin-bottom:10px; width:320px; overflow-x:hidden;'>"+data.object[i].question+"</p>";
				}else{
					var questionPart = data.object[i].question.substr(0, 40);
					polls += "<td><p style='marginBottom:10px; width:320px; overflow-x:hidden;'>"+questionPart+" ...</p>";
				}
				var today = new Date();
				var currDay = today.getDate();
				var pollDay = data.object[i].end.substr(8, 2); 
				var pollTime = data.object[i].end.substr(11,8);
				// day is today
				if(pollDay == currDay){
					//state is closed
					if(data.object[i].state == 'CLOSED'){
						polls += "Closed<br></td><tr></table>";
					}
					//state is ongoing
					else{
						polls += "End: Today - "+pollTime+"<br></td><tr></table>"; // polls += " End: "+data.object[i].end+"<br>";
					}
				// day is tomorrow
				}else{
					polls += "End: Tomorrow - "+pollTime+"<br></td><tr></table>";
				}
				if(data.object[i].options.length == 0){
					//alert("error");
				}else{
					//all options (per poll)
					for(var j = 0; j < data.object[i].options.length; j++){
						polls += data.object[i].options[j].optionValue+":  ";
						//alert(data.object[i].options[j].users.length);
						if(data.object[i].options[j].users.length == 0){
							polls += "No participants right now.";
						}else{
							polls += "<br>";
							//all users (per option per poll)
							for(var k = 0; k < data.object[i].options[j].users.length; k++){
								var mail = data.object[i].options[j].users[k];
								image_url = "/images/custom_icons/icon_"+mail;
								var tempIcon;
								if(imageExists(image_url) == false){
									tempIcon = "<img class='poll_user' src='/images/micons/defaulticon.png'>";
								}else{
									tempIcon = "<img class='poll_user' src='/images/custom_icons/icon_"+mail+"'>";
								}
								polls += tempIcon;

								//List to identify users with polls (yes if user has one or more polls)
								userPollsList[mail+""] = "yes";
							}
						}
						polls += "<br>";
					}
				}
					
				polls += "<hr>";
				polls += "</div>";
			}
			
//			alert(userPollsList.length);
			
			document.getElementById("current_polls").innerHTML = polls;
			//sort - poll with newest creation date first 
			if(newestFirst == true && endingFirst == false){
				var elems = $.makeArray($(".sortedPolls_newest"));
				elems.sort(function(a, b) {
					var test = a.getAttribute('data-datetime_created')+"";
					var test2 = b.getAttribute('data-datetime_created')+"";
//					alert("a: "+test+"b: "+test2);
				    return new Date( a.getAttribute('data-datetime_created') ) > new Date( b.getAttribute('data-datetime_created') );
				});
				$("#current_polls").html(elems);
				//remove last hr
				$('#current_polls hr').slice(-1).remove();
			//sort - poll with first ending first (default)
			}else if(newestFirst == false && endingFirst == true || newestFirst == false && endingFirst == false){	//sort - ending first
				var elems = $.makeArray($(".sortedPolls_newest"));
				elems.sort(function(a, b) {
					var test = a.getAttribute('data-datetime_end')+"";
					var test2 = b.getAttribute('data-datetime_end')+"";
//					alert("a: "+test+"b: "+test2);
				    return new Date( a.getAttribute('data-datetime_end') ) > new Date( b.getAttribute('data-datetime_end') );
				});
				$("#current_polls").html(elems);
				//remove last hr
				$('#current_polls hr').slice(-1).remove();
			}

		}
		//to secure that check is always first and after that (!) is the update of the icon placement
		callback(callbackdata);
	});
	
}

var user_polls = [];
var userPerPoll = [];
/**
 * add all polls to the users
 */
function addPollsToUser(){
	
	doTask("read_all_polls", null, function (data){
		//no polls in database
		if(data.object.length == 0){
			//do nothing
		}else{//polls in database
			userPerPoll = [];
			//get current position in array
			var tempPoll=-1;
			//all polls
			for (var i = 0; i < data.object.length; i++) {
				//options per poll
				tempPoll = -1;
				for (var j = 0; j < data.object[i].options.length; j++) {
					//users per options per poll
					for (var k = 0; k < data.object[i].options[j].users.length; k++) {
						//create user array
						var user = data.object[i].options[j].users[k]+"";
						if (userPerPoll[user] == undefined) {
							userPerPoll[user] = [];
						}
						//add poll to user
						if(userPerPoll[user][tempPoll] == undefined){
							userPerPoll[user].push([]);
							userPerPoll[user][userPerPoll[user].length-1][0] = data.object[i].icon;
							userPerPoll[user][userPerPoll[user].length-1][1] = [];
							tempPoll++;
						}
						//add option to current user
						userPerPoll[user][userPerPoll[user].length-1][1].push(data.object[i].options[j].optionValue+"");

					}
				}

			}
			
		}
	});
	
}

/**
 * check wether image exists
 */
function imageExists(image_url){

    var http = new XMLHttpRequest();

    http.open('HEAD', image_url, false);
    http.send();

    return http.status != 404;

}

var newestFirst = false;
var endingFirst = false;
/**
 * poll - order
 * checks which radio button is checked
 */
function pollSelection(){

    if (document.pollOrders.elements[0].checked){
//    	alert(document.pollOrders.elements[0].value);
    	newestFirst = true;
    	endingFirst = false;
    }
    else{
//    	alert(document.pollOrders.elements[1].value);
    	endingFirst = true;
    	newestFirst = false;
    }
}