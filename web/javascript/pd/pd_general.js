/**
 * This function is called onLoad of each PD page.
 */
function onLoadOfPdPage() {
    session = readCookie("MIND_PD_C");
    send(new Arrival("check", session), function (data) {
        if (instanceOf(data.object, Error)) {
            alert("You have to be logged in.");
            window.location.href = "public_display_login.jsp";

        } else {
            initPublicDisplayStuff();
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
        }
        if ($('#show_display_settings').css('display') != 'block') {
            $('#display_settings_img').css('background-image', 'url(../images/pd_icons/settings_default_350px.png)');
        }
    });

}

/**
 * toggle the slide effect on the public displays for the app settings
 */
function toggleAppSettings() {
    if ($('#show_display_settings').css('display') != 'none') { //check wether app settings are still open
        $("#show_display_settings").toggle("slow", function () {
            $('#display_settings_img').css('background-image', 'url(../images/pd_icons/settings_default_350px.png)');
        });
    }
    $("#show_app_settings").toggle("slow", function () {
        if ($('#show_app_settings').css('display') != 'none') {
            $('#app_settings_img').css('background-image', 'url(../images/pd_icons/settings_clicked_350px.png)');
        }
        if ($('#show_app_settings').css('display') != 'block') {
            $('#app_settings_img').css('background-image', 'url(../images/pd_icons/settings_default_350px.png)');
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

