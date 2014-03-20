<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script language="JavaScript" src="http://code.jquery.com/jquery-2.1.0.js"></script>
<script src="javascript/library.js"></script>
<script src="javascript/run.js"></script>
<link href="${pageContext.request.contextPath}/css/admin.css" rel="stylesheet" type="text/css">
<title>Admin - Administrate Public Displays</title>
</head>

<body onload="onLoadOfAdminPage();loadDisplays();">
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
	</ul>
</div>
<div id="text-content">
<input type="submit" value="Add Display" onClick="addDisplayViaPopup()" />
	 <br> Info - Add User: You have to add a name, an email and a password. 
	 <br>Info - Edit User: You can't edit a user's email. <br>If you want to change an email you have to add a new user with the desired email (and remove the user with the old email).
	 <div id="infoText"></div>
	 <div id="table_space"></div> 
</div>
<div id="logout">
       <input type="submit" value="Logout" onClick="logout; window.location='index.jsp'"/>
</div>
<div class="clear"></div>
</div>

<div id="footer">
<!-- TODO: nice Footer -->
</div>
</div>
</body>
</html>