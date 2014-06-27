var originalWidth; //the native width of the map-image in pixels
var originalHeight; //the native height of the map-image in pixels
var displayedWidth; //the current width of the displayed map-image in pixels
var displayedHeight; //the current height of the map-image in pixels
var displayedIconSize = 0; //the native size of the icon in pixels
var iconByAreaFactor = 0.45; //the factor by which the icon size is set -> (smallest area width or height)*iconByAreaFactor
var iconByMapWidthFactor = 0.06; //the factor by which the icon size is set -> displayedWidth*iconByAreaFactor
var defaultIconSize = 110; //if something goes wrong when setting the icon size - defaultIconSize will be applied
var factor = 1; //the size-factor by which the displayed image deviates from the original image
//TODO read from server
var refreshRate = 10; //the refresh rate for locating - in seconds
var interval; //the interval of location refreshing
var balloonClosingTime = 7;
var mapDiv;
//var widthFactor=1; //the factor by which the width of the displayed image deviates from the original image width
//var heigthFactor=1; //the factor by which the height of the displayed image deviates from the original image height
//var zoomValue = 30; //holds the width value for each zoom-step in pixels

var users; //the current (to be) displayed users;
var sortedEmails; //the current user's emails, sorted by areas
var areas = new Array(); //an array of areas - contains all areas that have already been used/needed
var awayAreaExists = true;

$(document).ready(function () {
    window.onresize = function () {
        mapResize();
        getRemainingSpace();   //to find out size of remaining content (for polling) on resize
    };
});

/**
 * This function retrives the original metrics (width & height) of one icon image
 * @param allusers
 */
function initPublicDisplayStuff() {

    mapDiv = document.getElementById("mapscroll");

    users = new Array();

    send(new Arrival("READ_ALL_AREAS", session), function (data) {

        //AREAS STUFF
        var areasArray = data.object;

        //TODO if no areas found -> ??

        for (var i = 0; i < areasArray.length; i++) {
            areas[areasArray[i].ID] = areasArray[i];
        }

        checkAwayArea();

        //END AREAS STUFF

        //get map metrics
        var mapImgLoad = $("<img />");
        mapImgLoad.attr("src", "images/map");
        mapImgLoad.unbind("load");
        mapImgLoad.bind("load", function () {
            originalWidth = this.width;
            originalHeight = this.height;

            retriveBackgroundImageSizeMetricsAndFactor();

            computeIconSize();

            updatePdData();
//			loadTestUser();
            initInterval();
        });
    });
}


document.addEventListener("click", function (event) {

    var element = document.getElementsByTagName("html")[0];

    if (event.target.id === 'toggleFullscreen') {
        if (element.requestFullScreen) {

            if (!document.fullScreen) {
                element.requestFullscreen();
            } else {
                document.exitFullScreen();
            }
        } else if (element.mozRequestFullScreen) {
            if (!document.mozFullScreen) {
                element.mozRequestFullScreen();
            } else {
                document.mozCancelFullScreen();
            }
        } else if (element.webkitRequestFullScreen) {

            if (!document.webkitIsFullScreen) {
                element.webkitRequestFullScreen();
            } else {
                document.webkitCancelFullScreen();
            }
        }
    }

});


/**
 * This function resets the interval - should be called after the refreshRate was changed
 */
function initInterval() {
    if (interval != null) {
        clearInterval(interval);
    }
    interval = setInterval(function () {
        updatePdData();
    }, refreshRate * +1000);
}

/**
 * This funciton computes the icon size by considering the smalles area height or width
 */
function computeIconSize() {
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
    displayedIconSize = Math.round(displayedWidth * iconByMapWidthFactor);
//	alert("iconsize: "+displayedIconSize);
}

/**
 * This function retrives the actual width and height of the map image
 * and computes the factor by which the displayed image deviates from the actual image
 */
function retriveBackgroundImageSizeMetricsAndFactor() {

    displayedHeight = $("#mapscroll").height();
    displayedWidth = $("#mapscroll").width();

    if ((displayedWidth / displayedHeight) >= (originalWidth / originalHeight)) {
        factor = displayedHeight / originalHeight;
        displayedWidth = factor * originalWidth;
    } else {
        factor = displayedWidth / originalWidth;
        displayedHeight = factor * originalHeight;
    }
}


function mapResize() {

    //if resize is called on startup -> no resize necessary
    if (displayedWidth == null) {
//		alert("don't resize_");
        return;
    }

    oldDisplayedWidth = displayedWidth;
    oldDisplayedHeight = displayedHeight;

    //compute map & icon metrics
    retriveBackgroundImageSizeMetricsAndFactor();

    if (oldDisplayedWidth == displayedWidth) {
//		alert("no resize");
        return;
    }
    computeIconSize();

    //update user placement
    if (users != null) {
        changeFactor = +displayedWidth / +oldDisplayedWidth;
        for (var email in users) {
            users[email].x = Math.round(+changeFactor * +users[email].x);
            users[email].y = Math.round(+changeFactor * +users[email].y);
            placeUserIcon(users[email]);
        }
    }

}

/**
 * This method converts the x (or y) value for displaying purposes (subtracting half the width)
 * @param raw_x the original x value (how it is retrieved from the server)
 * @param the width of the element
 * @returns the x value ready for displaying purposes
 */
function getX(raw_x, scale) {
    return Math.round((raw_x * factor - (scale / 2)));
}


/**
 * This method converts the scale value (width or height) for displaying purposes
 * given that the ratio is preserved - else width scale
 * @param raw_scale the original scale value
 * @returns the scale value ready for displaying purposes
 */
function getScale(raw_scale) {
    return Math.round(raw_scale * factor);
}


/**
 * This function creates a user icon as an <img/>
 * @param user the user
 */
function addUserIcon(user) {
    var divToAugment = document.getElementById("mapscroll");
    var icon = document.createElement("img");
    icon.id = "icon_" + user.email;
    icon.className = "micon";

    icon.src = "/images/custom_icons/icon_" + user.email;
//	icon.src='/images/custom_icons/defaulticon.png';
    icon.onerror = function () {
        this.src = '/images/micons/defaulticon.png'; //Default icon
    };

    icon.onclick = function () {
        displayUserInfo(user.email);
    };
    //style
    icon.style.position = "absolute";
    icon.style.cursor = "pointer";

    divToAugment.appendChild(icon);
    icon = null;
}

function removeUserWithIcon(email) {
    var iconElement = document.getElementById('icon_' + email);
    if (iconElement != null) {
        iconElement.parentNode.removeChild(iconElement);
    }
    removeItem(users, email);
}

/**
 * This function sets a user icon's position and size
 * in consideration of the scale factor
 * this function also sets a poll icon to a user, if the user has polls
 * @param user the user
 */
function placeUserIcon(user) {
    var icon = document.getElementById("icon_" + user.email);

    if (icon == null) { //user has no icon image yet
        addUserIcon(user);
        icon = document.getElementById("icon_" + user.email);
    }

    if (icon != null) {

        //refresh source (in case of icon swapping)
        icon.src = "/images/custom_icons/icon_" + user.email + "#" + new Date().getTime();

        icon.style.width = displayedIconSize + "px";
        icon.style.left = Math.round(user.x - displayedIconSize / 2) + "px";
        icon.style.top = Math.round(user.y - displayedIconSize / 2) + "px";

        //apply visual effect regarding user status
        var statusinfo = getInfoByStatus(user.status);
        icon.className = 'micon ' + statusinfo.classname;
    }
    
    //set poll-icon above user-icon (on the map)
    //check if user has a poll
    if(userPollsList[user.email+""] != null){
    	//user has a poll
    	if(userPollsList[user.email+""]==="yes"){
    		//set left and top like above plus 2/3 of user-icon size
    		var left = Math.round(user.x - displayedIconSize / 2) + (Math.round(displayedIconSize / 3)*2);
    		var top = Math.round(user.y - displayedIconSize / 2) +(Math.round(displayedIconSize / 3)*2);
    		var poll_icon = document.getElementById("pollIcon_"+user.email);
    		//if poll icon doesn't exist
    		if(poll_icon==null){
    			//create new poll-icon
    			poll_icon = document.createElement("img");
    			poll_icon.id = "pollIcon_"+user.email;
    			poll_icon.className = "pollIcon_onMap";
    			poll_icon.src = "/images/polling/polling.png";    			
    			poll_icon.style.position = "absolute";
    			poll_icon.style.cursor = "pointer";
    			var divToAugment = document.getElementById("mapscroll");
    			divToAugment.appendChild(poll_icon);
    		}
    		poll_icon.style.width =  Math.round(displayedIconSize / 3) + "px";
    		poll_icon.style.height = Math.round(displayedIconSize / 3) + "px";
    		poll_icon.style.left = left + "px";
    		poll_icon.style.top = top + "px";    		
    	}
    	
    }    
}

/**
 * This function sets the x,y coordinates of each user
 * according to the area as position.
 * Keep in mind: the x and y values are intended to for the center of the icon
 */
function setUserIconCoordsByArea() {

    if (Object.keys(users).length <= 0) {
        return;
    }

    var area = null;
    var currentx = 0;
    var currenty = 0;
    var iconsInRow;
    var rowsInArea;
    var firstInArea;
    var usersInArea;
    //vars for y-value placement (if modification necessary);
    for (var aId in areas) {
        usersInArea = getUserKeysByAreaID(aId);
//		alert(usersInArea.length+" in "+aId);
        //if no user in this area -> continue with next area;
        if (usersInArea.length <= 0) {
            continue;
        }
        area = areas[aId];
        firstInArea = true;
        iconsInRow = 0;
        rowsInArea = 0;
        currentx = Math.round(area.topLeftX * factor + Math.round(displayedIconSize / 2));
        currenty = Math.round(area.topLeftY * factor + Math.round(displayedIconSize / 2));

        for (var i = 0; i < usersInArea.length; i++) { //for each user in this area
//			alert(uId+" in "+aId);
            if (firstInArea) { //if first in this row - always draw -> move currentx
                users[usersInArea[i]].x = currentx;
                users[usersInArea[i]].y = currenty;
                //alert(currentx+",-,"+currenty);
//				iconsInRow++;
                currentx += displayedIconSize;
                firstInArea = false;
                rowsInArea++;
                iconsInRow++;
            } else { //user currently not firstinrow
                //current icon would exceed the row
                if ((currentx + (Math.round(displayedIconSize / 2))) > Math.round(area.topLeftX * factor + area.width * factor)) {
                    currentx = Math.round(area.topLeftX * factor + Math.round(displayedIconSize / 2));
                    currenty += displayedIconSize;
//					//first icon in row:
                    users[usersInArea[i]].x = currentx;
                    users[usersInArea[i]].y = currenty;
                    currentx += displayedIconSize;
                    rowsInArea++;
                } else { //current icon still fits in this row
                    users[usersInArea[i]].x = currentx;
                    users[usersInArea[i]].y = currenty;
                    currentx += displayedIconSize;
//					iconsinarea++;
                    if (rowsInArea == 1) {
                        iconsInRow++;
                    }
                }
//				if(firstinrow){ //if first in this row - always draw -> move currentx
//					users[usersInArea[i]].x = currentx;
//					users[usersInArea[i]].y = currenty;
//					currentx += displayedIconSize;
//					firstinrow = false;
//					rowsInArea++;
//				}
            }

        }//end for each userInArea

        //check for height
        var outstand = ( (currenty + Math.round(displayedIconSize / 2)) -
            Math.round((area.topLeftY * factor + area.height * factor)) );
        if (outstand > 0) {
            var yOffset = 0;
            var inRow = 0;
            for (var k = 0; k < usersInArea.length; k++) {
                if (k + 2 > iconsInRow) { //for all other rows
                    inRow++;
                    if (inRow == iconsInRow) { //new row
                        inRow = 0;
                        yOffset += Math.round(outstand / (rowsInArea - 1));
                    }
                    users[usersInArea[k]].y -= yOffset;
                }
            }
        }
        //end check for height

        usersInArea = new Array();
    }//end for each area

}


///**
// * This function checks if icons reach out of the area
// * and if so, moves them upwards
// * @param area the area
// * @param iconsinarea the amount of icons in the area
// * @param iconsperrow the max count of icons in a row
// * @param i check users from index i <= i+iconsinarea-1
// */
//function checkHeight(area,iconsInArea,iconsPerRow,i){
//	
//	var outstand = ((users[i+iconsInArea-1].y+Math.round(displayedIconSize/2)) - Math.round((area.topLeftY*factor+area.height*factor)));
////	alert("outstandy "+outstand);
//
//	//if (one of the) lowest icons stands out of the area
//	if(outstand > 0){
//		var rows = Math.ceil(iconsInArea/iconsPerRow);
//		var perRowCounter = 0;
//		var yoffsetPerRow = outstand/(rows-1);
//		var currentOffset = yoffsetPerRow;
//		
//		for ( var j=i+iconsPerRow; j < i+iconsInArea; ) { //for each icon - starting from the second row
//			if(perRowCounter >= iconsPerRow){//init next row
//				currentOffset += yoffsetPerRow;
//				perRowCounter = 0;
//			}else{//still in this row
//				perRowCounter++;
//				users[j].y -= currentOffset;
//				j++;
//			}
//		}
//	}
//}


/**
 * This function sets the proper size and placement of the user icons
 * -> places all the icons correctly by referencing the x,y coordinates of
 * the user objects
 */
function updateUserIconPlacement() {
    for (var email in users) {
        placeUserIcon(users[email]);
    }
}

/**
 * This function should be utilized for callback when requesting
 * updated user data
 */
function updateUserListOnReceive(data) {

    var updatedUsersArray = data.object;
//    alert(JSON.stringify(data.object)+"");

//	users = new Array();
//	for ( var i = 0; i < updatedUsersArray.length; i++) {
//		users[updatedUsersArray[i].email] = updatedUsersArray[i];
//	}

    // NEEDED FOR ACCESSING CHANGED USERS OR NEW USERS
    var updatedUsers = new Array();
    for (var i = 0; i < updatedUsersArray.length; i++) {
        updatedUsers[updatedUsersArray[i].email] = updatedUsersArray[i];
    }
//	var updatedUsers = data;

    //debug stuff
//	document.getElementById("userInfoOnUpdate").innerHTML = JSON.stringify(data);

    //update or remove old user
    for (var email in users) { //for each old user
        if (updatedUsers[email] != null) { //current user exists in new user array - update user position
            users[email] = updatedUsers[email];
            removeItem(updatedUsers, email);
        } else { //current user is no longer tracked - remove user from list
            removeUserWithIcon(email);
        }
    }

    for (var email in updatedUsers) {
        users[email] = updatedUsers[email];
    }

    //END NEEDED FOR ACCESSING CHANGED USERS OR NEW USERS

    //check for AWAY status and set position to "Away" for Away-Area
    for (var email in users) {
        if (users[email].status === "AWAY") {
            users[email].position = "Away";
        }
        if (!areaExists(users[email].position)) {
            removeUserWithIcon(email);
        }

    }

    //set individual user icon coordinates considering area
    setUserIconCoordsByArea();
    //display all currently tracked users
    updateUserIconPlacement();


//	var element = document.getElementById("mapscroll");
//	redrawElement(element); // currently not in use but it would work!
    $(mapDiv).redraw();

}

/**
 * This function returns an associated array (email:user) of all users in the area with id 'id'
 * @param id
 * @returns {Array}
 */
function getUserKeysByAreaID(id) {

    var usersInArea = new Array();
    for (var email in users) {
        if (users[email].position == id) {
            usersInArea.push(email);
        }
    }
    return usersInArea;
}

function removeItem(array, key) {
    if (!array.hasOwnProperty(key))
        return;
    if (isNaN(parseInt(key)) || !(array instanceof Array))
        delete array[key];
    else
        array.splice(key, 1);
};


jQuery.fn.redraw = function () {
    return this.hide(0, function () {
        $(this).show();
    });
};


///**
// * This function checks if a user exists in an array and returns the corresponding index.
// * A user comparison is based upon the email
// * @param user the user to find in the array
// * @param userarray the array which might contain the user
// * @returns {Number} the index of the user in the userarray, -1 if not found
// */
//function userExistsInUserarray(user,userarray){
//	for ( var i = 0; i < userarray.length; i++) {
////		alert(user.email+" "+userarray[i].email);
//		if(userarray[i].email === user.email){
//			return i;
//		}
//	}
//	return -1;
//}

var refreshCounter = 0;
/**
 * This function should be called periodically to update the users location, polls, etc. visually
 */
function updatePdData() {

	checkAwayArea();	// check periodically on reload
	getRemainingSpace();	//to find out size of remaining content (for polling) on reload
	
    if (balloonIsOpen()) {
        return;
    }

    refreshCounter = +refreshCounter + 1;
    if (document.getElementById("balloonIdle") != null) {
        document.getElementById("balloonIdle").innerHTML = refreshCounter;
    }
    send(new Arrival("read_all_positions", session), updateUserListOnReceive);
}


//TEST STUFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF

function loadTestUser() {
    var user1 = new User("b@b.b", "b", "b", false);
    user1.status = "OCCUPIED";
    user1.position = 333; //3301
//	user1.iconRef = "crab.png";
//	user1.x = 400;
//	user1.y = 300;
    var user2 = new User("c@c.c", "c", "C", false);
    user2.position = 333; //3301
    user2.status = "OCCUPIED";
//	user2.iconRef = "cow.png";
//	user2.x = 450;
//	user2.y = 400;
    var user3 = new User("d@d.d", "d", "D", false);
    user3.position = 333; //3301
    user3.status = "AVAILABLE";
//	user3.iconRef = "rabbit.png";
//	user3.x = 450;
//	user3.y = 600;
//    var user4 = new User("e@e.e", "e", false);
//    user4.position = 333;//3301;
//    user4.status = "AWAY";
////	user4.iconRef = "sheep.png";
//    var user5 = new User("f@f.f", "f", false);
//    user5.position = 332;//3304;
//    user5.status = "DO_NOT_DISTURB";
////	user5.iconRef = "deer.png";
//    var user6 = new User("fa@f.f", "fa", false);
//    user6.position = 332; //3304;
//    user6.status = "AVAILABLE";
//    var user7 = new User("fb@f.f", "fb", false);
//    user7.position = 332; //3304;
//    user7.status = "AVAILABLE";
//    var user8 = new User("fc@f.f", "fc", false);
//    user8.position = 332;//3304;
//    user8.status = "AVAILABLE";
//    var user9 = new User("fd@f.f", "fd", false);
//    user9.position = 332;//3304;
//    user9.status = "AVAILABLE";
//    var user10 = new User("fe@f.f", "fe", false);
//    user10.position = 332;//3304;
//    user10.status = "AVAILABLE";
//    var user11 = new User("ff@f.f", "ff", false);
//    user11.position = 332;//3304;
//    user11.status = "AVAILABLE";
//    var user12 = new User("fg@f.f", "fg", false);
//    user12.position = 332;//3304;
//    user12.status = "AVAILABLE";

    var testusers = new Array();
    testusers[user1.email] = user1;
    testusers[user2.email] = user2;
    testusers[user3.email] = user3;
//    testusers[user4.email] = user4;
//    testusers[user5.email] = user5;
//    testusers[user6.email] = user6;
//    testusers[user7.email] = user7;
//    testusers[user8.email] = user8;
//    testusers[user9.email] = user9;
//    testusers[user10.email] = user10;
//    testusers[user11.email] = user11;
//    testusers[user12.email] = user12;
//    updateUserListOnReceive(testusers);
}


function getAreaById(id) {

    return areas[id];
//	for ( var i = 0; i < areas.length; i++) {
//		if(areas[i].ID==id){
//			return areas[i]; //area has already benn worked with
//		}
//	}
//
//	return null;
}

function areaExists(id) {
    if (areas[id] == null) {
        return false;
    } else {
        return true;
    }
//	for ( var i = 0; i < areas.length; i++) {
//		if(areas[i].ID===id){
////			alert(id+" equals "+areas[i].ID);
//			return true; //area has already benn worked with
//		}
//	}
////	alert(id+" <- no match");
//	return false;
}


//END TEST STUFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF

function StatusInfo(color, txt, classname) {
    this.color = color;
    this.txt = txt;
    this.classname = classname;
}

function getInfoByStatus(status) {
    var statusInfo;
    switch (status) {
        case 'AVAILABLE':
            statusInfo = new StatusInfo('#6AFF50', 'Verfuegbar', 'miconAvailable');
            break;
        case 'OCCUPIED':
            statusInfo = new StatusInfo('#5CB9FF', 'Beschaeftigt', 'miconOccupied');
            break;
        case 'DO_NOT_DISTURB':
            statusInfo = new StatusInfo('#FF5543', 'Bitte nicht stoeren', 'miconDnD');
            break;
        case 'AWAY':
            statusInfo = new StatusInfo('#DDDDDD', 'Nicht da', 'miconAway');
            break;
        default:
            statusInfo = new StatusInfo('#DDDDDD', '', '');
            break;
    }
    return statusInfo;
}


///**
// * This function returns the user object by email
// * @param email the user's email
// */
//function getUserByEmail(email){
//	for ( var i = 0; i < users.length; i++) {
//		if(users[i].email==email){
//			return users[i];
//		}
//	}
//}

/**
 * This function is called when a user's icon was clicked.
 * @param email the email of the user that was clicked on
 */
function displayUserInfo(email) {
    var user = users[email];
    if (user != null) {
        balloonify(user);
    }
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


//the modified id (for closing purposes) of the current opened balloon, null if no balloon is open 
var openBalloonUserID = null;

var idleInterval;

/**
 * This function displays or removes a user balloon
 * @param user the user that was clicked on
 */
function balloonify(user) {

    var id = '#icon_' + user.email;
    //modifying the id by escaping '.' & '@'
    var mod_id = id.replace(/\./g, '\\.');
    mod_id = mod_id.replace(/\@/g, '\\@');
    if (openBalloonUserID != null) { //some balloon is open -> close balloon
        var previousBalloonifiedID = openBalloonUserID;
        removeBalloon();
        if (mod_id === previousBalloonifiedID) {//clicked on icon of just hid balloon -> do not open it again
            return;
        }
    }

    //CREATE BALLOON == SELECT USER
    //bring selected user-icon to front
//		bringUserToFront(mod_id);
    $(mod_id).addClass('miconSelected');

    openBalloonUserID = mod_id;

    var horizontalpos;
    var verticalpos;
    if ((+user.x) < (+displayedWidth / +2)) {
        horizontalpos = "right";
    } else {
        horizontalpos = "left";
    }
    if ((+user.y) < (+displayedHeight / +2)) {
        verticalpos = "bottom";
    } else {
        verticalpos = "top";
    }
    var positioning = verticalpos + " " + horizontalpos;
    var statusInfo = getInfoByStatus(user.status);
    
    var balloonContent = '<p id="balloonParagraph" style="background-color:' + statusInfo.color + ';">'
    + '<strong>' + user.name + ' in ' + user.position + '</strong>'
    + '<br>' + statusInfo.txt + '</p>';
        
    //add polls to user balloon
    if(userPerPoll[user.email+""] !== undefined){	//only if user has polls
    	balloonContent += '<table class="table_polls">';
	    //for each poll
	    for ( var i = 0; i < userPerPoll[user.email+""].length; i++) {
			//icon 
	    	var icon = userPerPoll[user.email][i][0]+"";
	    	var currentOption;
	    	if(i==0){	//start the tr
	    		balloonContent += '<tr>';
	    	}
	    	else if(i==4){	//start new tr (next line)
	    		balloonContent += '</tr><tr>';
    		}
	    	
	    	else if(i == 7 && userPerPoll[user.email+""].length != 8){	//set ... if user has more than 8 polls
	    		balloonContent += '<td style="vertical-align:middle;">...</td>';
	    		break;
	    	}
	    	balloonContent += '<td>';
	    	balloonContent += '<img class="poll_user_icon" src="'+icon+'">';
	    	balloonContent += '<br>';
	    	//for each checked option (of one user)
	    	for ( var j = 0; j < userPerPoll[user.email+""][i][1].length; j++) {
	    		currentOption = userPerPoll[user.email+""][i][1][j]+"";
	    		if(j == 4){	//only four answers
	    			balloonContent += "...";
	    			break;
	    		}
	    		//check if answer is too long
	    		if(currentOption.length <= 15){
	    			balloonContent += currentOption+"<br>";
	    			
	    		}else{
	    			var balloonContentPart = currentOption.substr(0, 15);
	    			balloonContent += balloonContentPart+" ...<br>";
	    		}
			}
	    	balloonContent += '</td>';
		}
	    balloonContent += '</tr></table>';
    }
    
    $(mod_id).showBalloon({
        position: positioning,
        showDuration: 250,
        contents: balloonContent
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
    if (idleInterval == null) {
        idleInterval = setInterval(timerIncrement, 1000);
    }
}

/**
 * This function is to be called when a user-icon is selected
 * @param mod_id the modified id of the icon-img for jquery-selection
 */
function bringUserToFront(mod_id) {
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
function removeBalloon() {
    if (!balloonIsOpen()) { //if no balloon is open -> no need to remove
        openBalloonUserID = null;
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

    openBalloonUserID = null;

}

//reset balloonIdleTime with mousemove & keypress
$(document).on("mousemove", function (e) {
    balloonIdleTime = 0;
});
$(document).on("keypress", function (e) {
    balloonIdleTime = 0;
});

/**
 * This function increments the balloonIdleTime & calls removeBalloon() in case of
 * balloonClosingTime was reached
 */
function timerIncrement() {
    if (balloonIsOpen()) {
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
function balloonIsOpen() {
    if (document.getElementById("userBalloon") == null) {
        return false;
    } else {
        return true;
    }
}

/**
 * This function is called when the map was clicked on
 * and handles balloon hiding in case of clicking on no icon
 */
$(document).on("mousedown", "#mapscroll", function (event) {
    if (!$(event.target).hasClass('micon')) { //if !(click on icon)
        if (balloonIsOpen()) { //if balloon is open -> hide balloon
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
//    alert("call "+recipient);

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
//    alert("send message to "+recipient+":\nPredefMsg: "+predefMsg+"\nCustomMsg: "+customMsg);

    //TODO if custommsg is empty - send predefmsg, else send custommsg
    //TODO possibly check for user status
});


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

function changeRefreshRate(value) {

    switch (value) {
        case '1':
        {
            refreshRate = 5;
            document.getElementById("slidertext_refresh").innerHTML = 'Current Refresh Rate: every 5 sec';
            break;
        }
        case '2':
            refreshRate = 10;
            document.getElementById("slidertext_refresh").innerHTML = 'Current Refresh Rate: every 10 sec';
            break;
        case '3':
            refreshRate = 15;
            document.getElementById("slidertext_refresh").innerHTML = 'Current Refresh Rate: every 15 sec';
            break;
        case '4':
            refreshRate = 30;
            document.getElementById("slidertext_refresh").innerHTML = 'Current Refresh Rate: every 30 sec';
            break;
        case '5':
            refreshRate = 60;
            document.getElementById("slidertext_refresh").innerHTML = 'Current Refresh Rate: every 60 sec';
            break;
        default:
            break;
    }
    initInterval();
}

/*
 function changeBrightness(value){

 document.getElementById("slidertext_brightness").innerHTML = "Brightness: "+value;

 }*/