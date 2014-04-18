var originalWidth; //the native width of the map-image in pixels
var originalHeight; //the native height of the map-image in pixels
var originalIconSize; //the native size of the icon in pixels
var widthFactor=1; //the factor by which the width of the displayed image deviates from the original image width
var heigthFactor=1; //the factor by which the height of the displayed image deviates from the original image height
var zoomValue = 30; //holds the width value for each zoom-step in pixels

var users; //the current (to be) displayed users;
var areas; //an array of areas - contains all areas that have already been used/needed

//TODO call refreshUserData() considering refresh rate of display

/*NOT IN USE
var slider; //the slider element */
/**
 * This function retrives the original metrics (width & height) of the map image (map.png)
 * @param allusers
 */
function retriveOriginalMetrics(allusers){
	
	//get initial slider value;
	/* NOT IN USE
	slider = document.getElementById("slider");
	previousScaleValue = slider.value; 
	
	users = allusers;
	var imgLoad = $("<img />");
	imgLoad.attr("src", "images/map.png");
	imgLoad.unbind("load");
	imgLoad.bind("load", function () {
		/*originalWidth = this.width;
		originalHeight = this.height;
		computeFactors();
		retriveOriginalIconMetrics();
	});
	*/
}

/**
 * This function retrives the original metrics (width & height) of one icon image
 * @param allusers
 */
function initPublicDisplayStuff(allusers){
	
	//TODO remove parameter

	users = new Array();

    send(new Arrival("READ_ALL_AREAS", session), function (data) {

        areas = data.object;
    	
    	var imgLoad = $("<img />");
    	imgLoad.attr("src", "images/micons/crab.png");
    	imgLoad.unbind("load");
    	imgLoad.bind("load", function () {
//    		originalIconSize = this.width;
    		originalIconSize = 110;
    		
    		//TODO remove
    		updateUserListOnReceive(allusers);
    		//instead: refreshUserData();
    	});
        
    });
    

}


/**
 * This function computes the scale factor of the image
 * and should be called on startup as well as after/when scaling
 */
/* NOT IN USE
function computeFactors(){
	var mapimg = document.getElementById("mapimg");
	if(originalHeight!=null&&originalHeight!=0){
		heigthFactor = mapimg.clientHeight/originalHeight;
	}
	if(originalWidth!=null&&originalWidth!=0){
		widthFactor = mapimg.clientWidth/originalWidth;
	}
}*/

/**
 * This method converts the x value for displaying purposes (subtracting half the width)
 * @param raw_x the original x value (how it is retrieved from the server)
 * @param the width of the element
 * @returns the x value ready for displaying purposes
 */
function getX(raw_x,scale){
	return Math.round((raw_x*widthFactor-(scale/2)));
}

///**
// * This method converts the y value for displaying purposes (subtracting half the height)
// * @param raw_y the original y value (how it is retrieved from the server)
// * @param the height of the element
// * @returns the y value ready for displaying purposes
// */
//function getY(raw_y,scale){
//	return Math.round((raw_y*heigthFactor-(scale/2)));
//}

/**
 * This method converts the scale value (width or height) for displaying purposes
 * given that the ratio is preserved - else width scale
 * @param raw_scale the original scale value
 * @returns the scale value ready for displaying purposes
 */
function getScale(raw_scale){
	return Math.round(raw_scale*widthFactor);
}


/**
 * This function is called on startup of the page,
 * creating and placing all the icons of all users
 */
/*
function initUsersPlacement(){
	//create <img/>s for each icon
	for ( var i = 0; i < users.length; i++) {
		addUserIcon(users[i]);		
	}
	
	//set individual user icon coordinates considering area
	setUserIconCoordsByArea();
	//display all currently tracked users
	updateUserIconPlacement();
}*/

/**
 * This function creates a user icon as an <img/>
 * @param user the user
 */
function addUserIcon(user){
	var divToAugment = document.getElementById("mapscroll");
	var icon=document.createElement("img");
	icon.className="micon";
	icon.src="images/micons/"+user.iconRef;
	icon.id="icon_"+user.email;
	icon.onclick=function () {
	    displayUserInfo(user.email);
	};
	//style
	icon.style.position ="absolute";
	icon.style.cursor="pointer";
   
   divToAugment.appendChild(icon);
   icon = null;
}

/**
 * This function sets a user icon's position and size
 * in consideration of the scale factor
 * @param user the user
 */
function placeUserIcon(user){
	var scale = 0; //the scaled size of the current icon
	var icon = document.getElementById("icon_"+user.email);
	if(icon!=null){
		scale = getScale(originalIconSize);
		icon.style.width=scale+"px";
		icon.style.left=getX(user.x,scale)+"px";
		icon.style.top=getX(user.y,scale)+"px";
		//TODO apply visual effect regarding user status
		icon = null;
	}
}

/**
 * This function sets the x,y coordinates of each user
 * according to the area as lastPosition.
 * Keep in mind: the x and y values are intended to for the center of the icon
 */
function setUserIconCoordsByArea(){
	users.sort(compareByArea); //sort users by area

//	var areastring = "";
//	for ( var i = 0; i < users.length; i++) {
//		areastring += users[i].lastPosition+",";
//	}
//	alert("sorted"+areastring);
	var area=null;
	var currentx = 0;
	var currenty = 0;
	var firstinrow = true;
	//vars for y-value placement (if modification necessary);
	var iconsinarea = 0;
	var iconsperrow = 0;
	for ( var i = 0; i < users.length+1; ) {
		if(area==null || users[i]==null || area.ID != users[i].lastPosition){//if new area to solve
			if(iconsinarea>1){
				checkHeight(area,iconsinarea,iconsperrow,(i-iconsinarea));
				if(i==users.length){break;} //if last user finished
			}
			if(users[i]==null){break;}
			area = getAreaById(users[i].lastPosition);
			currentx = area.topLeftX+Math.round(originalIconSize/2);
			currenty = area.topLeftY+Math.round(originalIconSize/2);
			firstinrow = true;
			iconsinarea = 0;
		}
		if(firstinrow){ //if first in this row - always draw -> move currentx
			users[i].x = currentx;
			users[i].y = currenty;
			currentx += originalIconSize;
			firstinrow = false;
			iconsinarea++;
			i++;
		}else{
			if( (currentx+(originalIconSize/2)) > (area.topLeftX+area.width) ){ //current icon would exceed the row
				firstinrow = true;
				currentx = area.topLeftX+Math.round(originalIconSize/2);
				currenty += originalIconSize;
				if(iconsperrow<1){
					iconsperrow = iconsinarea;					
				}
			}else{ //current icon still fits in this row
				users[i].x = currentx;
				users[i].y = currenty;
				currentx += originalIconSize;
				iconsinarea++;
				i++;
			}
		}
		
//		users[i].x = area.topLeftX+Math.round(originalIconSize/2);
//		users[i].y = area.topLeftY+Math.round(originalIconSize/2);
	}
}

/**
 * This function checks if icons reach out of the area
 * and if so, moves them upwards
 * @param area the area
 * @param iconsinarea the amount of icons in the area
 * @param iconsperrow the max count of icons in a row
 * @param i check users from index i <= i+iconsinarea-1
 */
function checkHeight(area,iconsInArea,iconsPerRow,i){
	
	var outstand = ((users[i+iconsInArea-1].y+Math.round(originalIconSize/2)) - (area.topLeftY+area.height));
//	alert("outstandy "+outstand);
	//if (one of the) lowest icons stands out of the area
	if(outstand > 0){
		var rows = Math.ceil(iconsInArea/iconsPerRow);
		var perRowCounter = 0;
		var yoffsetPerRow = outstand/(rows-1);
		var currentOffset = yoffsetPerRow;
		
		for ( var j=i+iconsPerRow; j < i+iconsInArea; ) { //for each icon - starting from the second row
			if(perRowCounter >= iconsPerRow){//init next row
				currentOffset += yoffsetPerRow;
				perRowCounter = 0;
			}else{//still in this row
				perRowCounter++;
				users[j].y -= currentOffset;
				j++;
			}
		}
	}
}

function compareByArea(user1,user2) {
	  if (user1.lastPosition < user2.lastPosition)
	     return -1;
	  if (user1.lastPosition > user2.lastPosition)
	    return 1;
	  return 0;
}


/**
 * This function sets the proper size and placement of the user icons
 * -> places all the icons correctly by referencing the x,y coordinates of
 * the user objects
 */
function updateUserIconPlacement(){
	for ( var i = 0; i < users.length; i++) {
		placeUserIcon(users[i]);
	}
}

/**
 * This function should be utilized for callback when requesting
 * updated user data
 */
function updateUserListOnReceive(updatedUsers){
	var index;

	//update or remove old user
	for (var i=users.length-1; i >= 0; i--) { //for each old user
		index = userExistsInUserarray(users[i],updatedUsers); //the index of updatedUsers in which current (old) user exists
		if(index >= 0){ //current user exists in new user array - update user position
			if(users[i].lastPosition != updatedUsers[index].lastPosition){ //user's position has changed
				users[i].lastPosition  = updatedUsers[index].lastPosition;
				users[i].x = null;
				users[i].y = null;
			}
			updatedUsers.splice(index,1); //remove new user since already handled
		}else{ //current user is no longer tracked - remove user from list
			var element = document.getElementById("icon_"+users[i].email);
			element.parentNode.removeChild(element);
			users.splice(i,1);
		}
	}

	//add completely new users (who weren't tracked in the previous request/response)
	for ( var i = 0; i < updatedUsers.length; i++) {
		users.push(updatedUsers[i]);
		addUserIcon(updatedUsers[i]);
	}

	//set individual user icon coordinates considering area
	setUserIconCoordsByArea();
	//display all currently tracked users
	updateUserIconPlacement();
}

/**
 * This function checks if a user exists in an array and returns the corresponding index.
 * A user comparison is based upon the email
 * @param user the user to find in the array
 * @param userarray the array which might contain the user
 * @returns {Number} the index of the user in the userarray, -1 if not found
 */
function userExistsInUserarray(user,userarray){
	for ( var i = 0; i < userarray.length; i++) {
//		alert(user.email+" "+userarray[i].email);
		if(userarray[i].email === user.email){
			return i;
		}
	}
	return -1;
}

/**
 * This function should be called periodically to refresh the users location visually
 */
function refreshUserData(){
	send(new Arrival("read_all_positions", session), updateUserListOnReceive);
}


//TEST STUFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF

function loadTestUser(){
	var user1 = new User("a@a.a",null,"a",false);
	user1.lastPosition = 3304;
	user1.iconRef = "crab.png";
//	user1.x = 400;
//	user1.y = 300;
	var user2 = new User("c@c.c",null,"c",false);
	user2.lastPosition = 3301;
	user2.iconRef = "cow.png";
//	user2.x = 450;
//	user2.y = 400;
	var user3 = new User("d@d.d",null,"d",false);
	user3.lastPosition = 3304;
	user3.iconRef = "rabbit.png";
//	user3.x = 450;
//	user3.y = 600;
	var user4 = new User("e@e.e",null,"e",false);
	user4.lastPosition = 3304;
	user4.iconRef = "sheep.png";
	var user5 = new User("f@f.f",null,"f",false);
	user5.lastPosition = 3304;
	user5.iconRef = "deer.png";
	var user6 = new User("g@g.g",null,"g",false);
	user6.lastPosition = 3301;
	user6.iconRef = "crab.png";
	
	var testusers = new Array();
	testusers[0] = user1;
	testusers[1] = user2;
	testusers[2] = user3;
	testusers[3] = user4;
	testusers[4] = user5;
	testusers[5] = user6;
	updateUserListOnReceive(testusers);
}



function getAreaById(id){
	

	for ( var i = 0; i < areas.length; i++) {
		if(areas[i].ID==id){
			return areas[i]; //area has already benn worked with
		}
	}
	alert("not in array");

	//TODO remove static test-data
	//TODO if area not found (should not happen) - don't display or put to away-area or ...
	var area = null;
	switch (id) {
	case 3304:
		area = new Area(3304, null, 0, 0, 223, 297);
		break;
	case 3305:
		area = new Area(3305, null, 233, 0, 223, 297);
		break;
	case 336:
		area = new Area(336, null, 465, 0, 223, 297);
		break;
	case 3303:
		area = new Area(3303, null, 0, 462, 301, 299);
		break;
	case 3301:
		area = new Area(3301, null, 311, 462, 147, 299);
		break;
	case 3302:
		area = new Area(3302, null, 0, 770, 458, 219);
		break;
	case 333:
		area = new Area(333, null, 465, 462, 220, 529);
		break;
	}
	return area;
}


//END TEST STUFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF

/**
 * This functio returns the user object of the email
 * @param email the user's email
 */
function getUserByEmail(email){
	for ( var i = 0; i < users.length; i++) {
		if(users[i].email==email){
			return users[i];
		}
	}
}

/**
 * This function is called when a user's icon was clicked.
 * @param email the email of the user that was clicked on
 */
function displayUserInfo(email){
	var user = getUserByEmail(email);
	if(user!=null){
		balloonify(user);
		//alert("email:"+user.email+" ; name:"+user.name+" x:"+user.x+" y:"+user.y);		
	}
}

//the modified id (for closing purposes) of the current opened balloon, null if no balloon is open 
var openBalloonUserID = null;

/**
 * This function displays or removes a user balloon
 * @param user the user that was clicked on
 */
function balloonify(user){
	
	var id = '#icon_'+user.email;
	//modifying the id by escaping '.' & '@'
	var mod_id = id.replace(/\./g, '\\.');
	mod_id = mod_id.replace(/\@/g, '\\@');
	
	
	if(openBalloonUserID!=null){ //some balloon is open -> close balloon
//		$(openBalloonUserID).hideBalloon();
		removeBalloon();
		if(mod_id===openBalloonUserID){//clicked on icon of just hid balloon -> do not open it again
			openBalloonUserID=null;
			return;
		}
	}

	//SHOW BALLOON
		openBalloonUserID = mod_id;
		
		//TODO dynamitise
		var horizontalpos;
		var verticalpos;
		if(user.x<720){ //half of 1440 - width of our map
			horizontalpos = "right"; }else{ horizontalpos = "left"; }
		if(user.y<540){
			verticalpos = "bottom"; }else{ verticalpos = "top"; }
		var positioning = verticalpos+" "+horizontalpos;
		
		$(mod_id).showBalloon({
		    //TODO possibly check for user status - alter balloon
			position: positioning,
			showDuration: 250,
			contents: '<strong>'+user.name+'</strong>'
			+'<p>Send me a message!</p>'
			//+'<input type="hidden" value="'+user.email+'" id="userBalloonID" />'
			+'<form id="messageForm">'
			+'<select id="predefMsg">'
			+'<option value="komm du">Kannst Du kurz vorbeikommen?</option>'
			+'<option value="ich komme">Ich komme gleich vorbei.</option>'
			+'<option value="keine Zeit">Ich habe keine Zeit.</option>'
			+'<option value="ja">Ja</option>'
			+'<option value="nein">Nein</option>'
			+'</select>'
			+'<br>'
			+'<input id="customMsg" type="text" size="40"/>'
			+'<br>'
			+'<input type="submit" value="Benachrichtigen"/>'
			+'</form>'
			
			+'<br>'
			
			+'<p>Call me!</p>'
			+'<form id="callForm">'
			+'<input type="submit" value="Call '+user.name+'"/>'
			+'</form>'
		});		
		document.getElementById("messageForm").parentNode.id = "userBalloon";
}


function removeBalloon(){
	$(openBalloonUserID).hideBalloon();
	var balloonElement = document.getElementById("userBalloon");
	balloonElement.parentNode.removeChild(balloonElement);
}

/**
 * This function is called when the map was clicked on
 * and handles balloon hiding in case of clicking on no icon
 */
$(document).on("mousedown", "#mapscroll", function (event) {
	  if (!$(event.target).hasClass('micon')) { //if !(click on icon)
		  if(openBalloonUserID!=null){ //if balloon is open -> hide balloon
			  removeBalloon();
			  openBalloonUserID = null;
		  }		  
	  }
	  
});


/**
 * This function is called when the Call button is clicked on the user's popup balloon
 */
$(document).on("submit", "form[id^='callForm']", function (event) {
    event.preventDefault();
  //get email of recipient
    var recipient = openBalloonUserID.replace(/\\/g, '');
    recipient = recipient.substring(6, recipient.length);
    alert("call "+recipient);

    //TODO obvious

});

/**
 * This function is called when the 'Benachrichtigen' button is clicked on the user's popup balloon
 */
$(document).on("submit", "form[id^='messageForm']", function (event) {
    event.preventDefault();
    //get email of recipient
    var recipient = openBalloonUserID.replace(/\\/g, '');
    recipient = recipient.substring(6, recipient.length);

    var predefMsg = $("#predefMsg").find(":selected").text();
    var customMsg = $("#customMsg").val();
    alert("send message to "+recipient+":\nPredefMsg: "+predefMsg+"\nCustomMsg: "+customMsg);
    
    //TODO if custommsg is empty - send predefmsg, else send custommsg
    //TODO possibly check for user status
});


/*
$(document).ready(function(){
	$('.micon').balloon({
		position: "right",
		  contents: '<a href="#">Any HTML!</a><br />'
			    +'<input type="text" size="40" />'
			    +'<input type="submit" value="Search" />'
			});
});*/



//MAP SCALING AND PANNING STUFF - NOT IN USE
/*
var clicking = false;
var previousX;
var previousY;

$(document).on("mousedown", "#mapscroll", function (e) {
e.preventDefault();
previousX = e.clientX;
previousY = e.clientY;
clicking = true;
});

$(document).mouseup(function() {
    clicking = false;
});


$(document).mousemove(function(e) {	
    if (clicking) {
        e.preventDefault();
        //accelerated panning
//        var directionX = (previousX - e.clientX) > 0 ? 1 : -1;
//        var directionY = (previousY - e.clientY) > 0 ? 1 : -1;
//        $("#mapscroll").scrollLeft($("#mapscroll").scrollLeft() + 10 * directionX);
//        $("#mapscroll").scrollTop($("#mapscroll").scrollTop() + 10 * directionY);
        $("#mapscroll").scrollLeft($("#mapscroll").scrollLeft() + (previousX - e.clientX));
        $("#mapscroll").scrollTop($("#mapscroll").scrollTop() + (previousY - e.clientY));
        previousX = e.clientX;
        previousY = e.clientY;
    }
});


$("#scroll").mouseleave(function(e) {
    clicking = false;
});



//SLIDE BAR STUFF
var trigger = 1;
var previousScaleValue = 0;
var zoomWiding = 1;
function doScale(value){

	//determine if zoom in or zoom out an zoom map
	if(previousScaleValue<value){ //zoom in
//		document.getElementById("slidertext").innerHTML = "up:"+previousScaleValue+"-"+value+"-"+mapimg.clientWidth;
		zoomWiding = mapimg.clientWidth+zoomValue;
	}else if(previousScaleValue>value){ //zoom out
		zoomWiding = mapimg.clientWidth-zoomValue;
//		document.getElementById("slidertext").innerHTML = "down:"+previousScaleValue+"-"+value+"-"+mapimg.clientWidth;
	}
	mapimg.style.width = zoomWiding+"px";
	
	//update the scale factors
	computeFactors();
//	document.getElementById("slidertext").innerHTML = "fact w:"+widthFactor+" h:"+heigthFactor;
	//update user icons
	updateUserIconPlacement();
	
	previousScaleValue = value;
	
}*/