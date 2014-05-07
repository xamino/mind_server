<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <script language="JavaScript" src="javascript/jquery-2.1.0.min.js"></script>
<script src="javascript/library.js"></script>
<script src="javascript/run.js"></script>
<script src="javascript/admin/admin_display_management.js"></script>
<script src="javascript/admin/admin_map_location.js"></script>
<script src="javascript/admin/admin_sensor_management.js"></script>
<script src="javascript/admin/admin_user_management.js"></script>
<script src="javascript/pd/displayMap.js"></script>
<script src="javascript/pd/pd_general.js"></script>
<link href="${pageContext.request.contextPath}/css/admin.css" rel="stylesheet" type="text/css">
<title>Admin - System Mangement</title>
</head>

<body onload="onLoadOfAdminPage()">
<div id="container">
<div id="banner">
<!-- <img src="someImage.jpg" alt="banner" width="100%" height="100px"/> <!-- TODO: Banner-Image -->
<h2>MIND</h2>
</div>

<div id="content">
<div id="navigation">
	<ul>
		<li><a href="admin_home.jsp" class="adminlink">Home</a></li>
		<li><a href="admin_user_management.jsp" class="adminlink">User Management</a></li>
		<li><a href="admin_system_management.jsp" class="adminlink">System Management</a>
			<ul>
				<li><a href="admin_import_map_location.jsp" class="adminlink">Import Map and Location Data</a></li>
				<li><a href="admin_public_displays.jsp" class="adminlink">Administrate Public Displays</a></li>
			</ul>
		</li>
		<li><a href="admin_sensor_management.jsp" class="adminlink">Wifi Sensor Management</a></li>
	</ul>
</div>
<div id="text-content">
You can <a href="admin_import_map_location.jsp" class="adminlink">Import Map and Location Data</a><br>
<br>
You can <a href="admin_public_displays.jsp" class="adminlink">Administrate Public Dicplays</a>
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