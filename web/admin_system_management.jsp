<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="${pageContext.request.contextPath}/css/admin.css" rel="stylesheet" type="text/css">
<title>Admin - System Mangement</title>
</head>

<body>
<div id="banner">
<!-- <img src="someImage.jpg" alt="banner" width="100%" height="100px"/> <!-- TODO: Banner-Image -->
<h2>MIND</h2>
</div>

<div id="content">
<div id="navigation">
	<ul>
		<li><a href="admin_home.jsp">Home</a></li>
		<li><a href="admin_user_management.jsp">User Management</a></li>
		<li><a href="admin_system_management.jsp">System Management</a>
			<ul>
				<li><a href="admin_import_map_location.jsp">Import Map and Location Data</a></li>
				<li><a href="admin_public_displays.jsp">Administrate Public Displays</a></li>
			</ul>
		</li>
	</ul>
</div>
<div id="text-content">
You can <a href="admin_import_map_location.jsp">Import Map and Location Data</a><br>
<br>
You can <a href="admin_public_displays.jsp">Administrate Public Dicplays</a>
</div>
<div id="logout">
       <input type="submit" value="Logout" />
</div>
<div class="clear"></div>
</div>

<div id="footer">
<!-- TODO: nice Footer -->
</div>
</body>
</html>