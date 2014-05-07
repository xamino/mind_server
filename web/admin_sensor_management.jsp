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
<script src="javascript/admin/admin_map_location_area.js"></script>
<script src="javascript/admin/admin_sensor_management.js"></script>
<script src="javascript/admin/admin_user_management.js"></script>
<script src="javascript/pd/displayMap.js"></script>
<script src="javascript/pd/pd_general.js"></script>
<link href="${pageContext.request.contextPath}/css/admin.css" rel="stylesheet" type="text/css">
<title>Admin - User Management</title>
</head>

<body onload="onLoadOfAdminPage();loadSensors();">
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
	 <input type="submit" value="Add Sensor" onClick="addSensorViaPopup()" />
	 <br>Info - Add Sensor: You have to add identification, area and password for the sensor. If you don't enter a password mind will generate one for you.
	 <!-- <br>Info - Edit User: You can't edit a user's email. <br>If you want to change an email you have to add a new user with the desired email (and remove the user with the old email).
	 <br>Info - Remove User: You can't remove yourself. Another admin has to remove you if necessary.-->
	 <div id="infoText"></div>
	<!-- <table border ="1">
        <tr>
            <td>User Name:</td>
            <td>User Email:</td>
            <td>User Password:</td>
            <td>Edit:</td>
            <td>Remove:</td>
        </tr>
        <tr>      
                <td>x</td>
                <td>y</td>
                <td>z</td>
                <td><input type="submit" value="Edit User" onClick="javascript:popupOpen()" /></td>
                <td><input type="submit" value="Remove User" onClick="javascript:popupOpen()" /></td>  
            </tr>
         for(int i = 0; i < allFestivals.size(); i+=1) { %>
            <tr>      
                <td>${allFestivals.get(i).getFestivalName()}</td>
                <td>${allFestivals.get(i).getLocation()}</td>
                <td>${allFestivals.get(i).getStartDate()}</td>
                <td><input type="submit" value="Add User" onClick="javascript:popupOpen()" /></td>
                <td>${allFestivals.get(i).getURL()}</td>  
            </tr>
        } 
    </table> -->
	<div id="table_space">
    
	</div> 


</div>
<div id="logout">
       <input type="submit" value="Logout" onClick="userLogout()" />
</div>
<div class="clear"></div>
</div>

<div id="footer">
<!-- TODO: nice Footer -->
</div>
</div>
</body>
</html>