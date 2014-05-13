<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <script src="javascript/jquery-2.1.0.min.js"></script>
<script src="javascript/library.js"></script>
<script src="javascript/run.js"></script>
<script src="javascript/pd/displayMap.js"></script>
<script src="javascript/pd/pd_general.js"></script>
<script src="javascript/jquery.balloon.js"></script>
<link href="${pageContext.request.contextPath}/css/public_display.css" rel="stylesheet" type="text/css">

<%
    response.setHeader("Cache-Control", "no-cache, must-revalidate");
    response.setHeader("Cache-Control", "max-age=0, must-revalidate");
%>  

<title>Public Display</title>
</head>
<body onload="onLoadOfPdPage(), checkAwayArea()">

<!-- MAP DISPLAY STUFF -->
<div id="mapscroll">

		<!-- <img id="mapimg" class="mapcontent" src="images/map.png"/>-->
		<!-- <img class="micon" src="images/micons/crab.png"/> -->
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
		<a href="#" id="app_settings_img" onclick="toggleAppSettings()"></a>	<!-- appSettingsClicked() -->
		<p>App Settings</p>
		</div>
		<div id="display_settings">
		<a href="#" id="display_settings_img" onclick="toggleDisplaySettings()"></a> 	<!-- displaySettingsClicked() -->
		<p>Display Settings</p>
		</div>
	</div>
	<div id="content_popup">
		<p id="balloonIdle">0</p>
		<p id="userInfoOnUpdate">info</p>
		<div id="awayArea_info"></div>

		<div id ="show_display_settings" style="display:none;">
		 	<!-- <div id='settingsBrightness'>
			    <h3>Display Brightness</h3><br>
			    <div id="sliderdiv_brightness">
					<input id="slider_brightness" type="range" name="points" min="1" max="5" onchange="changeBrightness(this.value)">
					<p id="slidertext_brightness">Brightness: 3</p>
				</div>
		    </div> -->
		    <div id='settingsRefresh'>
		    	<hr><br><h3>Refresh Rate</h3><br>
		    	From 5 sec (fastest rate) to 60 sec (slowest rate).<br>
		    	<br>
		    	<div id="sliderdiv_refresh">
					<input id="slider_refresh" type="range" name="points" min="1" max="5" value="2" onchange="changeRefreshRate(this.value)"> <!--oninput,onchange="changeRefreshRate(this.value)"-->
					<p id="slidertext_refresh">Current Refresh Rate: every 10 sec</p>
				</div>
		    </div>
		    <!-- <a href='#' id='mute_img' onclick='mute()'></a><br>-->
		    <hr><br>
		    <div id="buttonsDiv">
			    <button type='button' id='displaySettingsBack' class="shadow">Back</button> <!-- settingsBackButton() -->
			    <button type='button' id='displayLogoutButton' class="shadow" onclick='logoutDisplay()'>Logout Display</button>
			    <button type="button" id='toggleFullscreen' class="shadow">toggleFullscreen</button>
		    </div>
	    </div>
	    <div id ="show_app_settings" style="display:none;">
	    	There are currently no apps on your system.
	    	<hr><br><button type='button' id='appSettingsBack' class="shadow" onclick='toggleAppSettings()'>Back</button>
	    </div>
	  </div>
    	<div id="login_location">
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
</body>
</html>