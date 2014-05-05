var originalWidth; //the native width of the map-image in pixels
var originalHeight; //the native height of the map-image in pixels
var displayedWidth; //the current width of the displayed map-image in pixels
var displayedHeight; //the current height of the map-image in pixels
var displayedIconSize=0; //the native size of the icon in pixels
var iconByAreaFactor = 0.45; //the factor by which the icon size is set -> (smallest area width or height)*iconByAreaFactor
var iconByMapWidthFactor = 0.06; //the factor by which the icon size is set -> displayedWidth*iconByAreaFactor
var defaultIconSize = 110; //if something goes wrong when setting the icon size - defaultIconSize will be applied
var factor=1; //the size-factor by which the displayed image deviates from the original image
//TODO read from server
var refreshRate = 10; //the refresh rate for locating - in seconds
var interval; //the interval of location refreshing
var balloonClosingTime = 5;
//var widthFactor=1; //the factor by which the width of the displayed image deviates from the original image width
//var heigthFactor=1; //the factor by which the height of the displayed image deviates from the original image height
//var zoomValue = 30; //holds the width value for each zoom-step in pixels

var users; //the current (to be) displayed users;
var areas; //an array of areas - contains all areas that have already been used/needed


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
function initPublicDisplayStuff(){
	
	var elem = document.body; // Make the body go full screen.
	requestFullScreen(elem);

	users = new Array();

    send(new Arrival("READ_ALL_AREAS", session), function (data) {

        areas = data.object;
    	
        //get Metrics
        //get icon metrics
//    	var imgLoad = $("<img />");
//    	imgLoad.attr("src", "images/micons/crab.png");
//    	imgLoad.unbind("load");
//    	imgLoad.bind("load", function () {
//    		displayedIconSize = this.width;
//    		displayedIconSize = 110;
    		
    		//get map metrics
    		var mapImgLoad = $("<img />");
    		mapImgLoad.attr("src", "images/map.png");
    		mapImgLoad.unbind("load");
    		mapImgLoad.bind("load", function () {
    			originalWidth = this.width;
    			originalHeight = this.height;
    			
    			retriveBackgroundImageSizeMetricsAndFactor();

    			computeIconSize();

    			refreshUserData();
//    			interval = setInterval(function(){refreshUserData();},refreshRate*+1000);
    			initInterval();

    		});
    	
    	});
//    });
    
}

function requestFullScreen(element) {
    // Supports most browsers and their versions.
    var requestMethod = element.requestFullScreen || element.webkitRequestFullScreen || element.mozRequestFullScreen || element.msRequestFullScreen;

    if (requestMethod) { // Native full screen.
        requestMethod.call(element);
    } else if (typeof window.ActiveXObject !== "undefined") { // Older IE.
        var wscript = new ActiveXObject("WScript.Shell");
        if (wscript !== null) {
            wscript.SendKeys("{F11}");
        }
    }
}

/**
 * This function resets the interval - should be called after the refreshRate was changed
 */
function initInterval(){
	if(interval!=null){
		clearInterval(interval);		
	}
	interval = setInterval(function(){refreshUserData();},refreshRate*+1000);		
}

/**
 * This funciton computes the icon size by considering the smalles area height or width
 */
function computeIconSize(){
//	var smallest = 99999999;
//	for ( var i = 0; i < areas.length; i++) {
//		if(areas[i].width<smallest){smallest = areas[i].width;}
//		if(areas[i].hegiht<smallest){smallest = areas[i].height;}
//	}
//	
//	if(smallest<9999999){
//		displayedIconSize = Math.round(iconByAreaFactor*smallest*factor);
//	}
//	if(displayedIconSize==0){
//		displayedIconSize = defaultIconSize;
//	}
	displayedIconSize = Math.round(displayedWidth*iconByMapWidthFactor);
//	alert("iconsize: "+displayedIconSize);
}

/**
 * This function retrives the actual width and height of the map image
 * and computes the factor by which the displayed image deviates from the actual image
 */
function retriveBackgroundImageSizeMetricsAndFactor(){
	
	displayedHeight = $("#mapscroll").height();
	displayedWidth = $("#mapscroll").width();
	
	if( (displayedWidth/displayedHeight) >= (originalWidth/originalHeight) ){
		factor = displayedHeight/originalHeight;
		displayedWidth = factor*originalWidth;
	}else{
		factor = displayedWidth/originalWidth;
		displayedHeight = factor*originalHeight;
	}
	
//	alert('width =' + displayedWidth + ', height = ' + displayedHeight); 
}


//$(document).on("resize", "#mapscroll", function () {
//	
//	alert("resize");
//
//});



$( document ).ready(function() {
//	$("#mapscroll").onresize=mapResize;
	window.onresize=function(){mapResize();};
});

function mapResize(){

	//if resize is called on startup -> no resize necessary
	if(displayedWidth==null){
//		alert("don't resize_");
		return;
	}

	oldDisplayedWidth = displayedWidth;
	oldDisplayedHeight = displayedHeight;
	
	//compute map & icon metrics
	retriveBackgroundImageSizeMetricsAndFactor();
	
	if(oldDisplayedWidth==displayedWidth){
//		alert("no resize");
		return;
	}
	computeIconSize();
	
	//update user placement
	if(users != null){
		changeFactor = +displayedWidth/+oldDisplayedWidth;
		for ( var i = 0; i < users.length; i++) {
			users[i].x = Math.round(+changeFactor*+users[i].x);
			users[i].y = Math.round(+changeFactor*+users[i].y);
			placeUserIcon(users[i]);
		}
	}
	
//	alert("resize by factor "+changeFactor);
}

//$('#mapscroll').bind('resize', function(){
//    alert('resized');
//});
//
//$(window).resize(function(){
//$('#mapscroll').resize();
//});

/**
 * This function computes the scale factor of the image
 * and should be called on startup as well as after/when scaling
 */
/*
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
 * This method converts the x (or y) value for displaying purposes (subtracting half the width)
 * @param raw_x the original x value (how it is retrieved from the server)
 * @param the width of the element
 * @returns the x value ready for displaying purposes
 */
function getX(raw_x,scale){
	return Math.round((raw_x*factor-(scale/2)));
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
	return Math.round(raw_scale*factor);
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
	icon.id="icon_"+user.email;
	icon.className="micon";

	icon.src="/images/custom_icons/icon_"+user.email;
	icon.onerror = function () {
	  this.src = '/images/custom_icons/defaulticon.png'; //Defualt icon
	};
	
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
	var icon = document.getElementById("icon_"+user.email);
	if(icon!=null){

		icon.style.width=displayedIconSize+"px";
		icon.style.left=Math.round(user.x-displayedIconSize/2)+"px";
		icon.style.top=Math.round(user.y-displayedIconSize/2)+"px";
		
		//apply visual effect regarding user status
		var statusinfo = getInfoByStatus(user.status);
		icon.className = 'micon '+statusinfo.classname;
	}
}

/**
 * This function sets the x,y coordinates of each user
 * according to the area as position.
 * Keep in mind: the x and y values are intended to for the center of the icon
 */
function setUserIconCoordsByArea(){
	
	users.sort(compareByArea); //sort users by area

//	var areastring = "";
//	for ( var i = 0; i < users.length; i++) {
//		areastring += users[i].position+",";
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
		if(area==null || users[i]==null || area.ID != users[i].position){//if new area to solve
			if(iconsinarea>1){
				checkHeight(area,iconsinarea,iconsperrow,(i-iconsinarea));
				if(i==users.length){break;} //if last user finished
			}
			if(users[i]==null){break;}
			area = getAreaById(users[i].position);
			//TODO handle area==null;
			currentx = Math.round(area.topLeftX*factor+Math.round(displayedIconSize/2));
			currenty = Math.round(area.topLeftY*factor+Math.round(displayedIconSize/2));
			firstinrow = true;
			iconsinarea = 0;
		}
		if(firstinrow){ //if first in this row - always draw -> move currentx
			users[i].x = currentx;
			users[i].y = currenty;
			//alert(currentx+",-,"+currenty);
			currentx += displayedIconSize;
			firstinrow = false;
			iconsinarea++;
			i++;
		}else{
			if( (currentx+(Math.round(displayedIconSize/2))) > Math.round(area.topLeftX*factor+area.width*factor) ){ //current icon would exceed the row
				firstinrow = true;
				currentx = Math.round(area.topLeftX*factor+Math.round(displayedIconSize/2));
				currenty += displayedIconSize;
				if(iconsperrow<1){
					iconsperrow = iconsinarea;					
				}
			}else{ //current icon still fits in this row
				users[i].x = currentx;
				users[i].y = currenty;
				currentx += displayedIconSize;
				iconsinarea++;
				i++;
			}
		}
		
//		users[i].x = area.topLeftX+Math.round(displayedIconSize/2);
//		users[i].y = area.topLeftY+Math.round(displayedIconSize/2);
		
//		alert(users[i].x+","+users[i].y);	
	}//end for each user
	
//	var texty = "";
//	for(var i = 0; i < users.length; i++){
//		texty += users[i].email+":"+users[i].x+","+users[i].y+"\n";
//	}
//	alert(texty);
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
	
	var outstand = ((users[i+iconsInArea-1].y+Math.round(displayedIconSize/2)) - Math.round((area.topLeftY*factor+area.height*factor)));
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
	  if (user1.position < user2.position)
	     return -1;
	  if (user1.position > user2.position)
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
function updateUserListOnReceive(data){
	var updatedUsers = data.object;
	
	document.getElementById("userInfoOnUpdate").innerHTML = JSON.stringify(data);
	
	
	var index;

	//update or remove old user
	for (var i=users.length-1; i >= 0; i--) { //for each old user
		index = userExistsInUserarray(users[i],updatedUsers); //the index of updatedUsers in which current (old) user exists
		if(index >= 0){ //current user exists in new user array - update user position
//			if(users[i].position != updatedUsers[index].position){ //user's position has changed
//				users[i].position  = updatedUsers[index].position;
				users[i] = updatedUsers[index];
				users[i].x = null;
				users[i].y = null;
//			}
			updatedUsers.splice(index,1); //remove new user since already handled
		}else{ //current user is no longer tracked - remove user from list
			var element = document.getElementById("icon_"+users[i].email);
			element.parentNode.removeChild(element);
			users.splice(i,1);
		}
	}

	if(updatedUsers !=null){
		//add completely new users (who weren't tracked in the previous request/response)
		for ( var i = 0; i < updatedUsers.length; i++) {
			users.push(updatedUsers[i]);
			addUserIcon(updatedUsers[i]);
		}		
	}
	
//	texty = "";
	//check for AWAY status and set position to "1" for Away-Area
	for ( var i = 0; i < users.length; i++) {
		if(users[i].status==="AWAY"){
			users[i].position = "1";
		}
//		texty += users[i].email+":"+users[i].position+",";
	}
	
	//set individual user icon coordinates considering area
	setUserIconCoordsByArea();
	//display all currently tracked users
	updateUserIconPlacement();
	
	
	var element = document.getElementById("mapscroll");
//	redrawElement(element); // currently not in use but it would work!
	$(element).redraw();

}

jQuery.fn.redraw = function() {
    return this.hide(0, function() {
        $(this).show();
    });
};

/* see method call
 function redrawElement(element){

    if (!element) { return; }

    var n = document.createTextNode(' ');
    var disp = element.style.display;  // don't worry about previous display style

    element.appendChild(n);
    element.style.display = 'none';

    setTimeout(function(){
        element.style.display = disp;
        n.parentNode.removeChild(n);
    },20); // you can play with this timeout to make it as short as possible
}*/


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

var refreshCounter = 0;
/**
 * This function should be called periodically to refresh the users location visually
 */
function refreshUserData(){
	if(balloonIsOpen()){
		return;
	}
	
//	loadTestUser();
//	updateUserListOnReceive(users);
	refreshCounter = +refreshCounter+1;
	if(document.getElementById("balloonIdle")!=null){
		document.getElementById("balloonIdle").innerHTML = refreshCounter;		
	}
//	alert("refreshy");
	send(new Arrival("read_all_positions", session), updateUserListOnReceive);
}


//TEST STUFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF

function loadTestUser(){
	var user1 = new User("a@a.a",null,"a",false);
	user1.status = "OCCUPIED";
	user1.position = 3301;
//	user1.iconRef = "crab.png";
//	user1.x = 400;
//	user1.y = 300;
	var user2 = new User("c@c.c",null,"c",false);
	user2.position = 3301;
	user2.status = "OCCUPIED";
//	user2.iconRef = "cow.png";
//	user2.x = 450;
//	user2.y = 400;
	var user3 = new User("d@d.d",null,"d",false);
	user3.position = 3302;
	user3.status = "AVAILABLE";
//	user3.iconRef = "rabbit.png";
//	user3.x = 450;
//	user3.y = 600;
	var user4 = new User("e@e.e",null,"e",false);
	user4.position = 3301;
	user4.status = "AWAY";
//	user4.iconRef = "sheep.png";
	var user5 = new User("f@f.f",null,"f",false);
	user5.position = 3301;
	user5.status = "DO_NOT_DISTURB";
//	user5.iconRef = "deer.png";

	
	var testusers = new Array();
	testusers[0] = user1;
	testusers[1] = user2;
	testusers[2] = user3;
	testusers[3] = user4;
	testusers[4] = user5;
	updateUserListOnReceive(testusers);
}



function getAreaById(id){
	

	for ( var i = 0; i < areas.length; i++) {
		if(areas[i].ID==id){
			return areas[i]; //area has already benn worked with
		}
	}
//	alert("area "+id+" does not exist in array");

	return null;
}


//END TEST STUFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF

function StatusInfo(color,txt,classname){
	this.color = color;
	this.txt = txt;
	this.classname = classname;
}

function getInfoByStatus(status){
	var statusInfo;
	switch (status) {
	case 'AVAILABLE':
		statusInfo = new StatusInfo('#6AFF50','Verf�gbar','miconAvailable');
		break;
	case 'OCCUPIED':
		statusInfo = new StatusInfo('#5CB9FF','Besch�ftigt','miconOccupied');
		break;
	case 'DO_NOT_DISTURB':
		statusInfo = new StatusInfo('#FF5543','Bitte nicht st�ren','miconDnD');
		break;
	case 'AWAY':
		statusInfo = new StatusInfo('#DDDDDD','Nicht da','miconAway');
		break;
	default:
		statusInfo = new StatusInfo('#DDDDDD','','');
		break;
	}
	return statusInfo;
}

function getStatusByStatus(status){
	switch (status) {
	case 'AVAILABLE':
		return 'Verf�gbar';
		break;
	case 'OCCUPIED':
		return 'Besch�ftigt';
		break;
	case 'DO_NOT_DISTURB':
		return 'Bitte nicht st�ren';
		break;
	case 'AWAY':
		return 'Nicht da';
		break;
	default:
		return '';
		break;
	}
}

/**
 * This function returns the user object by email
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
	}
}

//the modified id (for closing purposes) of the current opened balloon, null if no balloon is open 
var openBalloonUserID = null;

var idleInterval;

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
		var previousBalloonifiedID = openBalloonUserID;
		removeBalloon();
		if(mod_id===previousBalloonifiedID){//clicked on icon of just hid balloon -> do not open it again
			return;
		}
	}

	//CREATE BALLOON
		//bring selected user-icon to front
		bringUserToFront(mod_id);
	
		openBalloonUserID = mod_id;
		
		var horizontalpos;
		var verticalpos;
		if((+user.x)<(+displayedWidth/+2)){
			horizontalpos = "right"; }else{ horizontalpos = "left"; }
		if((+user.y)<(+displayedHeight/+2)){
			verticalpos = "bottom"; }else{ verticalpos = "top"; }
		var positioning = verticalpos+" "+horizontalpos;
		var statusInfo = getInfoByStatus(user.status);
		$(mod_id).showBalloon({
			position: positioning,
			showDuration: 250,
			contents: '<p id="balloonParagraph" style="background-color:'+statusInfo.color+';">'
				+'<strong>'+user.name+' in '+user.position+'</strong>'
				+'<br>'+statusInfo.txt+'</p>'
				/*
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
			+'</form>'*/
		});		
		document.getElementById("balloonParagraph").parentNode.id = "userBalloon";
		
	    //Increment the idle time counter every second
		if(idleInterval==null){
			idleInterval = setInterval(timerIncrement, 1000);			
		}
}

/**
 * This function is to be called when a user-icon is selected
 * @param mod_id the modified id of the icon-img for jquery-selection
 */
function bringUserToFront(mod_id){
//	var id = '#icon_'+user.email;
//	//modifying the id by escaping '.' & '@'
//	var mod_id = id.replace(/\./g, '\\.');
//	mod_id = mod_id.replace(/\@/g, '\\@');
	$(mod_id).appendTo("#mapscroll");
	
	//DO GLOW
//	var icon=document.createElement("img");
//	icon.className="micon";
//	$(mod_id).className = "miconSelected";
//	$(mod_id).removeClass();
	$(mod_id).addClass('miconSelected');
}



/**
 * This function removes the opened balloon
 */
function removeBalloon(){
	if(!balloonIsOpen()){ //if no balloon is open -> no need to remove
		openBalloonUserID=null;
//		alert("removeBalloon - no balloon open");
		return;
	}
	//reset balloon idle counter stuff
//	clearInterval(interval);
//	resetInterval();
	balloonIdleTime = 0;
	//hide...
	$(openBalloonUserID).hideBalloon();
	//& delete balloon
	var balloonElement = document.getElementById("userBalloon");
	balloonElement.parentNode.removeChild(balloonElement);
	
	//REMOVE GLOW
	$(openBalloonUserID).removeClass('miconSelected');

	openBalloonUserID=null;

}

//reset balloonIdleTime with mousemove & keypress
$(document).on("mousemove", function (e) {
//	alert("mousemove");
	balloonIdleTime = 0;
//	if(document.getElementById("balloonIdle")!=null){
//		document.getElementById("balloonIdle").innerHTML = balloonIdleTime;		
//	}
});
$(document).on("keypress", function (e) {
//	alert("keypress");
	balloonIdleTime = 0;
//	if(document.getElementById("balloonIdle")!=null){
//		document.getElementById("balloonIdle").innerHTML = balloonIdleTime;		
//	}
});

/**
 * This function increments the balloonIdleTime & calls removeBalloon() in case of
 * balloonClosingTime was reached
 */
function timerIncrement() {
	if(balloonIsOpen()){
		balloonIdleTime = +balloonIdleTime + 1;
//		if(document.getElementById("balloonIdle")!=null){
//			document.getElementById("balloonIdle").innerHTML = balloonIdleTime;		
//		}
		if (balloonIdleTime >= balloonClosingTime) {
			removeBalloon();
		}
	}
}

/**
 * This function checks, if a balloon is open
 * @returns {Boolean} true if open, else false
 */
function balloonIsOpen(){
	if(document.getElementById("userBalloon")==null){
		return false;
	}else{
		return true;
	}
}

/**
 * This function is called when the map was clicked on
 * and handles balloon hiding in case of clicking on no icon
 */
$(document).on("mousedown", "#mapscroll", function (event) {
	  if (!$(event.target).hasClass('micon')) { //if !(click on icon)
		  if(balloonIsOpen()){ //if balloon is open -> hide balloon
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

    //TODO obvious - init call - call server

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

function changeRefreshRate(value){

	if(value == 1){
		refreshRate = 5;
		document.getElementById("slidertext_refresh").innerHTML = 'Current Refresh Rate: 1 (every 5 sec)';
	}
	if(value == 2){
		refreshRate = 10;
		document.getElementById("slidertext_refresh").innerHTML = 'Current Refresh Rate: 2 (every 10 sec)';
	}
	if(value == 3){
		refreshRate = 15;
		document.getElementById("slidertext_refresh").innerHTML = 'Current Refresh Rate: 3 (every 15 sec)';
	}
	if(value == 4){
		refreshRate = 30;
		document.getElementById("slidertext_refresh").innerHTML = 'Current Refresh Rate: 4 (every 30 sec)';
	}
	if(value == 5){
		refreshRate = 60;
		document.getElementById("slidertext_refresh").innerHTML = 'Current Refresh Rate: 5 (every 60 sec)';
	}
	initInterval();
}

/*
function changeBrightness(value){

	document.getElementById("slidertext_brightness").innerHTML = "Brightness: "+value;
	
}*/