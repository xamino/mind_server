<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <script src="javascript/jquery-2.1.0.min.js"></script>
    <script src="javascript/library.js"></script>
    <script src="javascript/run.js"></script>
    <script src="javascript/admin/admin_map_location_area.js"></script>
    <script src="javascript/admin/admin_analize_wifi.js"></script>
    <link href="${pageContext.request.contextPath}/css/admin.css" rel="stylesheet" type="text/css">
    <title>Admin - Import Map and Location Data</title>
</head>

<body onload="onLoadOfAdminPage();initShowWifi();">
<div id="container">
    <div id="banner">
        <!-- <img src="someImage.jpg" alt="banner" width="100%" height="100px"/> <!-- TODO: Banner-Image -->
        <h2>MIND</h2>
    </div>

    <div id="content">

        <div id="text-content">

			<select id="selectHotspot" name=selectHotspot>
				<option>Choose...</option>
			</select>
			
			<select id="selectDevice" name=selectDevice>
				<option>Choose...</option>
			</select>
			
			<button onclick="drawLocations()">GO!</button>

            <!-- draw on map -->
            <!-- <div id="map_div"></div>-->
            <div id="map_container_div" onmousedown="return false">
                <!-- <div id="selection"></div>-->
            </div>
            <br>

        </div>
        <div id="logout">
            <input type="submit" value="Logout" onClick="userLogout()"/>
        </div>
        <div class="clear"></div>
    </div>

    <div id="footer">
        <!-- TODO: nice Footer -->
    </div>
</div>
</body>
</html>