/**
 * This function is called onLoad of each PD page.
 */
function onLoadOfPdPage() {
    var sessionCookie = readCookie("MIND_PD_C");
    var sessionHtml = document.body.getAttribute("lolcat");
    trySession(sessionCookie, function () {
        trySession(sessionHtml, function () {
            alert("You have to be logged in. Credentials might be incorrect, the PD might not exist, or the IP match" +
                "might have missed. Please contact an administrator if the issue persists!");
            window.location.href = "public_display_login.jsp";
        }, function () {
            session = sessionHtml;
            initPublicDisplayStuff();
        })
    }, function () {
        session = sessionCookie;
        initPublicDisplayStuff();
    });
}

/**
 * Small helper function.
 * @param session Session to check.
 * @param errorCallback Called on error.
 * @param okayCallback Called if all okay.
 */
function trySession(session, errorCallback, okayCallback) {
    send(new Arrival("check", session), function (data) {
        if (instanceOf(data.object, Error)) {
            errorCallback();
        } else {
            okayCallback();
        }
    });
}

/**
 * This function is called when the public display attempts to display
 * the locations of the users (their icons) on the map
 */
function displayUserLocations() {

    //send(new Arrival("read_all_positions", session), retriveOriginalMetrics);
    handleAllUsersPositionData();
}

function handleAllUsersPositionData() {

//TESTAREA
    //User(email, password, name, admin) {
//	var user1 = new User("a@a.a",null,"a",false);
//	user1.lastPosition = 3304;
//	user1.status = "AVAILABLE";
////	user1.iconRef = "crab.png";
//	var user2 = new User("b@b.b",null,"b",false);
//	user2.lastPosition = 3304;
//	user2.status = "OCCUPIED";
////	user2.iconRef = "lion.png";
//	
//	var users = new Array();
//	users[0] = user1;
//	users[1] = user2;
//	initPublicDisplayStuff(users);
//END TESTAREA
    initPublicDisplayStuff();
}

/*********************** public display - toggle settings **************************/

/**
 * toggle the slide effect on the public displays for the display settings
 */
function toggleDisplaySettings() {
    if ($('#show_app_settings').css('display') != 'none') {	//check wether app settings are still open
        $("#show_app_settings").slideToggle("slow", function () {
            $('#app_settings_img').css('background-image', 'url(../images/pd_icons/settings_default_350px.png)');
        });
    }
    $("#show_display_settings").slideToggle("slow", function () {
        if ($('#show_display_settings').css('display') != 'none') {
            $('#display_settings_img').css('background-image', 'url(../images/pd_icons/settings_clicked_350px.png)');
            /*$("#toggleFullscreen").css('position', 'absolute');
             $("#toggleFullscreen").css('right', '15px');
             var buttonwidth = $("#toggleFullscreen").width();
             $("#displayLogoutButton").css('position', 'absolute');
             $("#displayLogoutButton").css('right', (+buttonwidth + +50)+'px');*/
            $('#current_polls').css('display', 'none');
        }
        if ($('#show_display_settings').css('display') != 'block') {
            $('#display_settings_img').css('background-image', 'url(../images/pd_icons/settings_default_350px.png)');
            $('#current_polls').css('display', 'block');
        }
    });

}

/**
 * toggle the slide effect on the public displays for the app settings
 */
function toggleAppSettings() {
    if ($('#show_display_settings').css('display') != 'none') { //check wether display settings are still open
        $("#show_display_settings").slideToggle("slow", function () {
            $('#display_settings_img').css('background-image', 'url(../images/pd_icons/settings_default_350px.png)');
        });
    }
    $("#show_app_settings").slideToggle("slow", function () {
        if ($('#show_app_settings').css('display') != 'none') {
            $('#app_settings_img').css('background-image', 'url(../images/pd_icons/settings_clicked_350px.png)');
            $('#current_polls').css('display', 'none');
        }
        if ($('#show_app_settings').css('display') != 'block') {
            $('#app_settings_img').css('background-image', 'url(../images/pd_icons/settings_default_350px.png)');
            $('#current_polls').css('display', 'block');
        }
    });

}

/**
 * checks whether the away area exists (if not --> info-text)
 */
function checkAwayArea() {

//	users = new Array();

//    send(new Arrival("READ_ALL_AREAS", session), function (data) {
//
//        areas = data.object;

	
    if (!areaExists("Away")) {
        ///// TO MANY ERRORS WITH THIS
    	//var noAwayArea = "There is currently no away area (ID: 'Away') in the database. Add this area to see all necessary information.<br>Please contact the admin.";
        //document.getElementById("awayArea_info").innerHTML = noAwayArea;
    }
//    });
}



///////////////////////// local storage stuff, display settings and app settings ///////////////////////////////////
var lastRefreshVar;
var autoBrightVar;
var lastBrightness = 1+"";
var mapRotateVar;
var pollSelectVar;
/**
 * set settings made by user (with localStorage)
 */
function setSettings(){

	//display settings
	//autoBrightness on/off
	autoBrightVar = localStorage.getItem('autoBright');
	if(autoBrightVar != 'on' && autoBrightVar != 'off'){
		autoBrightVar = 'on';	//default
	}
	if(autoBrightVar == 'on'){	//switch auto brightness on
		brightnessTimeout = setTimeout(function(){autoBrightness(lastBrightnessVar);},time); //because no user click is detected at the beginning
		document.autoBrights.autoBright[0].checked=true;
		document.autoBrights.autoBright[1].checked=false;
//		localStorage.setItem('autoBright', 'on');
	}else{	//switch auto brightness off
		document.autoBrights.autoBright[1].checked=true;
		document.autoBrights.autoBright[0].checked=false;
//		localStorage.setItem('autoBright', 'off');
		clearTimeout(brightnessTimeout);	//clear timeout if one exists
	}
	
	//set brightness
	lastBrightnessVar = localStorage.getItem('lastBrightness');
	if(lastBrightnessVar == null){	
		lastBrightnessVar = 1+"";	//default
	}
	$('#slider_brightness').val(lastBrightnessVar);
	changeBrightness(lastBrightnessVar);
	
	//set refresh rate
	lastRefreshVar = localStorage.getItem('refreshRate');
	if(lastRefreshVar == null){
		lastRefreshVar = 2+"";	//default
	}
	$('#slider_refresh').val(lastRefreshVar);
	changeRefreshRate(lastRefreshVar);

	//set Rotation
	mapRotateVar = localStorage.getItem('mapRotation');
	if(mapRotateVar != '0' && mapRotateVar != '180' || mapRotateVar == '0'){
		localStorage.setItem('mapRotation', '0');
		setMapRotation();
	}
	else if(mapRotateVar == '180'){	//switch auto brightness on	
		localStorage.setItem('mapRotation', '180');
		setMapRotation();
	}
	//app settings
	//poll order
	pollSelectVar = localStorage.getItem('pollSelect');
	if(pollSelectVar != 'newestFirst' && pollSelectVar != 'endingFirst'){
		pollSelectVar = 'endingFirst';	//default
	}
	if(pollSelectVar == 'newestFirst'){	//switch auto brightness on
		document.pollOrders.elements[0].checked=true;
		document.pollOrders.elements[1].checked=false;
//		localStorage.setItem('pollSelect', 'newestFirst');
		pollSelection();
	}else{	//switch auto brightness on
		document.pollOrders.elements[1].checked=true;
		document.pollOrders.elements[0].checked=false;
//		localStorage.setItem('pollSelect', 'endingFirst');
		pollSelection();
	}
	
}

/** function changes brightness of display after slider interaction
 * 	is also been called if brightness changes automatically
 */
function changeBrightness(value){
	switch (value) {
    case '1':
        document.getElementById("darkener").style.backgroundColor = "rgba(0,0,0,0.0)";
        document.getElementById("slidertext_brightness").innerHTML = 'Current Brightness: 100%';
        localStorage.setItem('lastBrightness', 1);
        break;
    case '2':
    	document.getElementById("darkener").style.backgroundColor = "rgba(0,0,0,0.2)";
        document.getElementById("slidertext_brightness").innerHTML = 'Current Brightness: 80%';
        localStorage.setItem('lastBrightness', 2);
        break;
    case '3':
    	document.getElementById("darkener").style.backgroundColor = "rgba(0,0,0,0.4)";
        document.getElementById("slidertext_brightness").innerHTML = 'Current Brightness: 60%';
        localStorage.setItem('lastBrightness', 3);
        break;
    case '4':
        document.getElementById("darkener").style.backgroundColor = "rgba(0,0,0,0.6)";
        document.getElementById("slidertext_brightness").innerHTML = 'Current Brightness: 40%';
        localStorage.setItem('lastBrightness', 4);
        break;
    case '5':
    	document.getElementById("darkener").style.backgroundColor = "rgba(0,0,0,0.8)";
        document.getElementById("slidertext_brightness").innerHTML = 'Current Brightness: 20%';
        localStorage.setItem('lastBrightness', 5);
        break;
    default:
        break;
	}

}


var lastBrightnessVar;
/** 
 * checks whether user interacts with the display
 */
$(document).on('mousedown', 'body', function(){
	brightUp();
});

function brightUp(){
	lastBrightnessVar = localStorage.getItem('lastBrightness');
	if(lastBrightnessVar == null){	//may never been called
		lastBrightnessVar = 1+"";
	}
	changeBrightness(lastBrightnessVar);	//change to last Brightness (setted by user)
	clearTimeout(brightnessTimeout);
}

var brightnessTimeout;
var time = 1000 * 60 * 15;
//var time = 1000 * 5;
$(document).on('mouseup', 'body', function(){if(autoBrightVar == 'on'){
	brightnessTimeout = setTimeout(function(){autoBrightness(lastBrightnessVar);},
		time);}
});



/**
 * sets the brightness automatically (after timeout)
 * @param value is the last brightness (setted by the user)
 */
function autoBrightness(value){
	switch (value) {
    case '1':
        document.getElementById("darkener").style.backgroundColor = "rgba(0,0,0,0.4)";
        break;
    case '2':
    	document.getElementById("darkener").style.backgroundColor = "rgba(0,0,0,0.6)";
        break;
    case '3':
    	document.getElementById("darkener").style.backgroundColor = "rgba(0,0,0,0.8)";
        break;
    case '4':
        document.getElementById("darkener").style.backgroundColor = "rgba(0,0,0,0.8)";
        break;
    case '5':
    	document.getElementById("darkener").style.backgroundColor = "rgba(0,0,0,0.8)";
        break;
    default:
        break;
	}
}

var autoBrightnessOn;
/**
 * auto-brightness on/off
 */
function autoBrightOn_Off(){

    if (document.autoBrights.autoBright[0].checked == true){
    	localStorage.setItem('autoBright', 'on');
    }
    else{
    	localStorage.setItem('autoBright', 'off');
    	clearTimeout(brightnessTimeout);	//clear timeout if one exists
    }
}

var mapRotation;
/**
 * map rotation via Button click (0°/180°)
 */
function rotateMap(){
	mapRotation = localStorage.getItem('mapRotation');
	if(mapRotation == '0'){
		localStorage.setItem('mapRotation', '180');
		setMapRotation();
	}else if (mapRotation == '180'){
		localStorage.setItem('mapRotation', '0');
		setMapRotation();
	}
}

var mapContainer, mapRotate, imageURL;
/**
 * rotates the map if necessary (0°/180°)
 */
function setMapRotation(){
	mapContainer = document.getElementById("mapscroll");
	mapRotate = localStorage.getItem('mapRotation');
	imageURL = "/images/map_180";
	if(mapRotate+'' == '180'){	//map has to be 180°
		if(imageExists(imageURL) == false){	//error map remains 0°
//		mapContainer.style.backgroundImage = "url('/images/map_180')", "url('/images/map')";
//		mapContainer.style.onerror = function () {
			alert("There is currently no rotated image. Please contact the admin.");
			localStorage.setItem('mapRotation', '0');
			mapContainer.style.backgroundImage = "url('/images/map')"; //Default Map-Image
		}else{
			mapContainer.style.backgroundImage = "url('/images/map_180')";
			updateAreasRotation(true);
		}// map is rotated (180°)
	}else if(mapRotate == '0'){
		mapContainer.style.backgroundImage = "url('/images/map')";
		updateAreasRotation(false);
	}// map is rotated (0°)
}



/**
 * check whether image exists
 */
function imageExists(image_url){

    var http = new XMLHttpRequest();

    http.open('HEAD', image_url, false);
    http.send();

    return http.status != 404;

}
