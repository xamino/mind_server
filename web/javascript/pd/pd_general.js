/**
 * This function is called onLoad of each PD page.
 */
function onLoadOfPdPage() {
    var sessionCookie = readCookie("MIND_PD_C");
    var sessionHtml = document.body.getAttribute("lolcat");
    trySession(sessionCookie, function () {
        trySession(sessionHtml, function () {
            alert("You have to be logged in. IP match might have missed!");
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
 * checks wether the away area exists (if not --> info-text)
 */
function checkAwayArea() {

//	users = new Array();

//    send(new Arrival("READ_ALL_AREAS", session), function (data) {
//
//        areas = data.object;

	
    if (!areaExists("Away")) {
        var noAwayArea = "There is currently no away area (ID: 'Away') in the database. Add this area to see all necessary information.<br>Please contact the admin.";
        document.getElementById("awayArea_info").innerHTML = noAwayArea;
    }
//    });
}


function changeBrightness(value){
	
	switch (value) {
    case '1':
        document.getElementById("darkener").style.backgroundColor = "rgba(0,0,0,0.0)";
        document.getElementById("slidertext_brightness").innerHTML = 'Current Brightness: 100%';
        break;
    case '2':
    	document.getElementById("darkener").style.backgroundColor = "rgba(0,0,0,0.2)";
        document.getElementById("slidertext_brightness").innerHTML = 'Current Brightness: 80%';
        break;
    case '3':
    	document.getElementById("darkener").style.backgroundColor = "rgba(0,0,0,0.4)";
        document.getElementById("slidertext_brightness").innerHTML = 'Current Brightness: 60%';
        break;
    case '4':
        document.getElementById("darkener").style.backgroundColor = "rgba(0,0,0,0.6)";
        document.getElementById("slidertext_brightness").innerHTML = 'Current Brightness: 40%';
        break;
    case '5':
    	document.getElementById("darkener").style.backgroundColor = "rgba(0,0,0,0.8)";
        document.getElementById("slidertext_brightness").innerHTML = 'Current Brightness: 20%';
        break;
    default:
        break;
	}

}

/* not finished yet */
/*
//$( "body" ).mouseup(function() {
var lastTimestamp = 0;
$(document).on('mouseup', 'body', function(){
//  alert("up - "+lastTimestamp);
  lastTimestamp = event.timeStamp;  
});

$(document).on('mousedown', 'body', function(){
	changeBrightness(lastBrightness);
	document.getElementById('slider_brightness').value = lastBrightness;
});


$( document ).ready(function(){
	var startBrightness = document.getElementById('slider_brightness').value;
//	alert(startBrightness);
});

var currTimestamp = 0, diff = 0;
var brightnessValue;
var lastBrightness = brightnessValue;
function checkBrightnessSetting(){
	currTimestamp = Date.now();
	if(lastTimestamp == 0){
		lastTimestamp = currTimestamp;
	}
//	alert("curr: "+currTimestamp+", last: "+lastTimestamp);
	diff = +currTimestamp - +lastTimestamp;
//	alert(diff+" --- "+currTimestamp);
	if(diff > (6000 * 5)){
//		alert("change");
		lastTimestamp = currTimestamp;
		brightnessValue = document.getElementById('slider_brightness').value;
		switch(brightnessValue){
		case '1':
			changeBrightness(3);
			document.getElementById('slider_brightness').value = 3;
			break;
		case '2':
			changeBrightness(4);
			document.getElementById('slider_brightness').value = 4;
			break;
		case '3':
			changeBrightness(5);
			document.getElementById('slider_brightness').value = 5;
			break;
		case '4':
			changeBrightness(5);
			document.getElementById('slider_brightness').value = 5;
			break;
		case '5':
			changeBrightness(5);
			document.getElementById('slider_brightness').value = 5;
			break;
		default:
			break;
		}
		
//		alert("test "+brightnessValue);
	}
}*/