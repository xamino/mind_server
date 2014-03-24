<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script language="JavaScript" src="http://code.jquery.com/jquery-2.1.0.js"></script>
<script src="javascript/library.js"></script>
<script src="javascript/run.js"></script>
<link href="${pageContext.request.contextPath}/css/public_display.css" rel="stylesheet" type="text/css">
<title>Public Display</title>
</head>
<body onload="onLoadOfPdPage()">
<div id="map">

</div>
<div id="content">
	<div id="settings">
		<div id="app_settings">
		<a href="http://ran.ge/" title="Professional WordPress Development" id="app_settings_img"></a>
		<p>App Settings</p>
		</div>
		<div id="display_settings">
		<a href="http://ran.ge/" title="Professional WordPress Development" id="display_settings_img"></a>
		<p>Display Settings</p>
		</div>
	</div>
	<div id="content_popup">
	TODO: Here: popups etc.
	</div>
	<div id="login_location">
		<div id="location">
		<a href="http://ran.ge/" title="Professional WordPress Development" id="location_img"></a>
		<p>Location Force</p>
		</div>
		<div id="login">
		<a href="http://ran.ge/" title="Professional WordPress Development" id="login_img"></a>
		<p>Login</p>
		</div>
		<div id="privacy">
		<a href="http://ran.ge/" title="Professional WordPress Development" id="privacy_img"></a>
		<p>Privacy Setting</p>
		</div>
	</div>
</div>
</body>
</html>