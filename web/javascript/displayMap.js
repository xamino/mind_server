var originalWidth=1; //the native width of the map-image in pixels
var originalHeight=1; //the native height of the map-image in pixels
var originalIconSize=1; //the native size of the icon in pixels
var widthFactor=1; //the factor by which the width of the displayed image deviates from the original image width
var heigthFactor=1; //the factor by which the height of the displayed image deviates from the original image height
var zoomValue = 10; //holds the width value for each zoom-step in pixels

var users; //the current (to be) displayed users;

var slider; //the slider element
/**
 * This function retrives the original metrics (width & height) of the map image (map.png)
 * @param allusers
 */
function retriveOriginalMetrics(allusers){
	
	//get initial slider value;
	slider = document.getElementById("slider");
	previousScaleValue = slider.value;
	
	users = allusers;
	var imgLoad = $("<img />");
	imgLoad.attr("src", "images/map.png");
	imgLoad.unbind("load");
	imgLoad.bind("load", function () {
		originalWidth = this.width;
		originalHeight = this.height;
		computeFactors();
		retriveOriginalIconMetrics();
	});
}

/**
 * This function retrives the original metrics (width & height) of one icon image
 * @param allusers
 */
function retriveOriginalIconMetrics(){	
	var imgLoad = $("<img />");
	imgLoad.attr("src", "images/micons/crab.png");
	imgLoad.unbind("load");
	imgLoad.bind("load", function () {
		originalIconSize = this.width;
		initUsersPlacement();
	});
}

/**
 * This function computes the scale factor of the image
 * and should be called on startup as well as after/when scaling
 */
function computeFactors(){
	var mapimg = document.getElementById("mapimg");
	if(originalHeight!=null&&originalHeight!=0){
		heigthFactor = mapimg.clientHeight/originalHeight;
	}
	if(originalWidth!=null&&originalWidth!=0){
		widthFactor = mapimg.clientWidth/originalWidth;
	}
}

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
 * This function is practically called on startup of the page,
 * creating and placing all the icons of all users
 */
function initUsersPlacement(){
	//create <img/>s for each icon
	for ( var i = 0; i < users.length; i++) {
		addUserIcon(users[i]);		
	}
	//TODO area -> x,y
	//display all currently tracked users
	updateUserIconPlacement();
}

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
	//style
	icon.style.position ="absolute";
   
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
		icon = null;
	}
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
	for (var i=users.length; i >= 0; i--) { //for each old user
		index = userExists(users[i],updatedUsers); //the index of updatedUsers in which current (old) user exists
		if(index >= 0){ //current user exists in new user array - update user position
			if(users[i].lastPosition != updatedUsers[index].lastPosition){ //user's position has changed
				users[i].lastPosition  = updatedUsers[index].lastPosition;
				users[i].x = null;
				users[i].y = null;
			}
			updatedUsers.splice(index,1); //remove new user since already handled
		}else{ //current user is no longer tracked - remove user from list
			users.splice(i,1);
		}
	}

	//add completely new users (who weren't tracked in the previous request/response)
	for ( var i = 0; i < updatedUsers.length; i++) {
		addUserIcon(updatedUsers[i]);
	}
	//TODO area -> x,y
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




//MAP SCALING AND PANNING STUFF

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
	document.getElementById("slidertext").innerHTML = "facty w:"+widthFactor+" h:"+heigthFactor;
	//update user icons
	updateUserIconPlacement();
	
	previousScaleValue = value;
	
}