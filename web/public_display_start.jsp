<%@ page import="de.uulm.mi.mind.logger.Messenger" %>
<%@ page import="de.uulm.mi.mind.objects.PublicDisplay" %>
<%@ page import="de.uulm.mi.mind.security.Active" %>
<%@ page import="de.uulm.mi.mind.security.Security" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <meta HTTP-EQUIV="CACHE-CONTROL" content="NO-CACHE">
    <script src="javascript/jquery-2.1.0.min.js"></script>
    <script src="javascript/library.js"></script>
    <script src="javascript/run.js"></script>
    <script src="javascript/pd/displayMap.js"></script>
    <script src="javascript/pd/pd_general.js"></script>
    <script src="javascript/pd/streaming.js"></script>
    <script src="javascript/pd/polling.js"></script>
    <script src="javascript/jquery.balloon.js"></script>
    <script type="text/javascript">

	</script>
    <link href="${pageContext.request.contextPath}/css/public_display.css" rel="stylesheet" type="text/css">

    <%
        response.setHeader("Cache-Control", "no-cache, must-revalidate");
        response.setHeader("Cache-Control", "max-age=0, must-revalidate");
    %>

    <title>Public Display</title>
</head>
<body onload="onLoadOfPdPage(), setSettings()" lolcat="<%
        String ip = request.getRemoteAddr();
        Active active = null;
        // WARNING:
        if(ip.startsWith("134.60.12")) {
            active = Security.begin(new PublicDisplay("pd", "pd"), null);
        }
        if (active != null) {
            out.print(active.getSESSION());
            Messenger.getInstance().log("PDLogin", "@ "+ip);
            Security.finish(active);
        }
%>">
<!-- MAP DISPLAY STUFF -->
<div id="mapscroll">

    <!-- <img id="mapimg" class="mapcontent" src="images/map.png"/>-->
    <!-- <img class="micon" src="images/micons/crab.png"/> -->
     <input type="button" name="call_button" id="callButton" onclick="callRoom()"/>
   <!-- <input type="submit" value="Open Popup" onclick="javascript:callPopup('public_display_popup_call.jsp')"/> -->
    <!-- call button -->
    <!-- <input type="submit" name="call_button" id="callButton" onclick="javascript:callRoom('public_display_popup_call.jsp')"/> -->
</div>
<!-- Slider not in use -->
<!-- <div id="sliderdiv" style="position: absolute; top: 0px; left: 0px">
	<input id="slider" type="range" name="points" min="1" max="100" onchange="doScale(this.value)"> <!--oninput,onchange-->
<!-- <p id="slidertext">value</p> -->
<!-- </div> -->


<!-- END MAP DISPLAY STUFF -->
<div id="content">
    <div id="settings">
        <div id="app_settings">
            <a href="#" id="app_settings_img" onclick="toggleAppSettings()"></a> <!-- appSettingsClicked() -->
            <span>App Settings</span>
        </div>
        <div id="display_settings">
            <a href="#" id="display_settings_img" onclick="toggleDisplaySettings()"></a>
            <!-- displaySettingsClicked() -->
            <span>Display Settings</span>
        </div>
    </div>
    <div id="content_popup">
        <p id="balloonIdle" style="visibility:hidden; height:0; margin:0;">0</p>
        <div id="support">Support: mind@lists.uni-ulm.de</div><hr>
        <!-- <p id="userInfoOnUpdate">info</p>  -->
        <div id="awayArea_info"></div>
        <div id="container_inner_content">
            <div id="current_polls"></div>
            <div id="show_display_settings" style="display:none;">
                <!-- <div id='settingsBrightness'>
                   <h3>Display Brightness</h3><br>
                   <div id="sliderdiv_brightness">
                       <input id="slider_brightness" type="range" name="points" min="1" max="5" onchange="changeBrightness(this.value)">
                       <p id="slidertext_brightness">Brightness: 3</p>
                   </div>
               </div> -->
                <div id='settingsRefresh'>
                    
                    <h3>Refresh Rate</h3><br>
                    From 10 sec (fastest rate) to 60 sec (slowest rate).<br>
                    <br>

                    <div id="sliderdiv_refresh">
                        <input id="slider_refresh" type="range" name="points" min="1" max="5" value="2"
                               onchange="changeRefreshRate(this.value)">
                        <!--oninput,onchange="changeRefreshRate(this.value)"-->
                        <p id="slidertext_refresh">Current Refresh Rate: every 15 sec</p>
                    </div>                    
                </div>
                <!-- <a href='#' id='mute_img' onclick='mute()'></a><br>-->
                <hr>
                <br>
				
				<div id="sliderdiv_brightness">
					<h3>Brightness</h3><br>
					Change Brightness of the Display (100% - 80% - 60% - 40% - 20%)<br>
					<br>
	                   <input id="slider_brightness" type="range" name="points" min="1" max="5" value="1"
	                          onchange="changeBrightness(this.value)">
	                   <p id="slidertext_brightness">Current Brightness: 100%</p>
                </div>  
                <hr>
                <br>
                <h3>Auto Brightness</h3><br>
                Switch automatic brightness settings (after 15 minutes) on/off<br>
                <br>
                <form name="autoBrights">
                    <input type=radio name="autoBright" value="brightOn" onClick="autoBrightOn_Off()"> On<br>
                    <input type=radio name="autoBright" value="brightOff" onClick="autoBrightOn_Off()"> Off
                </form>
                <hr>
                <br>
                <div id="buttonsDiv">
                    <button type='button' id='displaySettingsBack' class="shadow" onclick='toggleDisplaySettings()'>Back
                    </button>
                    <button type='button' id='displaySettingsRotateMap' class="shadow" onclick='rotateMap()'>Rotate Map (180°)
                    </button>
                    <!-- <button type='button' id='displayLogoutButton' class="shadow" onclick='logoutDisplay()'>Logout
                        Display
                    </button>
                    <button type="button" id='toggleFullscreen' class="shadow">toggleFullscreen</button>-->
                </div>
            </div>
            <div id="show_app_settings" style="display:none;">
                <h3>Polling:</h3><br>
				Show the Polls with the first ending or the newest first.<br>
				<br>
                <form name="pollOrders">
                    <input type=radio name="pollOrder" value="newest" onClick="pollSelection()"> Newest First<br>
                    <input type=radio name="pollOrder" value="ending" onClick="pollSelection()"> Next Ending First
                </form>
                <hr>
                <h3>Get the App:</h3><br>
                <div id="div_qr_code"></div>
                <!-- <img id="qr_code" src="/images/misc/mind_apk_qr.png"/>-->
                <hr>
                <button type='button' id='appSettingsBack' class="shadow" onclick='toggleAppSettings()'>Back</button>
            </div>
        </div>
    </div>
    <div id="login_location" style="visibility:hidden;">
        <!-- <div id="location">
        <a href="http://ran.ge/" title="Professional WordPress Development" id="location_img" class="pd_link"></a>
        <p>Location Force</p>
        </div>-->
        <div id="login">
            <a href="#" title="user_login" id="login_img" class="pd_link"></a>

            <p>Login</p>
        </div>
        <div id="privacy">
            <a href="#" title="Professional WordPress Development" id="privacy_img" class="pd_link"></a>

            <p>Privacy Setting</p>
        </div>
    </div>

</div>
<div id="darkener"></div>
<div id="flash_div"></div>
<div id="flash_div_2" style="position:absolute; left:0; top:0; width:100%; height:100%;"><div id="icon_flash_div"></div></div>
</body>
</html>