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
<title>Admin - Import Map and Location Data</title>
</head>

<body onload="onLoadOfAdminPage();loadLocations(); loadAreas();">
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

<h3>Map Upload</h3>

	<form action="UploadServlet" method="post" enctype="multipart/form-data">
		<input type="file" name="file" size="50" />
		<br/>
		<input type="submit" value="Upload File" />
	</form>
	<div id="map_div"></div>
	
	<script>
	function doesFileExist(urlToFile)
{
    var xhr = new XMLHttpRequest();
    xhr.open('HEAD', urlToFile, false);
    xhr.send();
    if (xhr.status == "404") {
        return false;
    } else {
        return true;
    }
}
	</script>
	<script>
	var url_png = doesFileExist("images/map.png");
	alert("png: "+url_png);
	var url_jpg = doesFileExist("images/map.jpg");
	alert("jpg: "+url_jpg);
 	var url_jpeg = doesFileExist("images/map.jpeg");
 	alert("jpeg: "+url_jpeg);
 
	if (url_png == true) { 
	var input = "";
	input = "Current Map:";
	input += "<img id='testimage_png' alt='MIND_MAP' src='/images/map.png' >";
    document.getElementById("map_div").innerHTML = test;
    }else if(url_jpg == true){
    var input = "";
	input = "Current Map:";
	input += "<img id='testimage_jpg' alt='MIND_MAP' src='/images/map.jpg' >";
    document.getElementById("map_div").innerHTML = test;
    }else if(url_jpg == true){
    var input = "";
	input = "Current Map:";
	input += "<img id='testimage_jpeg' alt='MIND_MAP' src='/images/map.jpeg' >";
    document.getElementById("map_div").innerHTML = test;
	} else {
    var test = "";
	test = "Image isn't available";
	test += "<br> Sorry!";
    document.getElementById("map_div").innerHTML = test;
	}
	</script>
	 
	 <br><hr>
	 <h3>Areas</h3>
	 <br>Here you see all Areas which are currently in MIND.
	 <br>To Remove one area, simply click 'Remove Location'. The area 'universe' can't be removed.
	 <br>You can't add or edit an area here. You have to use the MIND-application for this.
	 <div id="infoText_areas"></div>
	 <div id="table_space_areas"></div> 
	 
	 <br><hr>
	 <h3>Locations</h3>
	 <br>Here you see all locations which are currently in MIND.
	 <br>To Remove one location, simply click 'Remove Location'.
	 <br>You can't add or edit a location here. You have to use the MIND-application for this.
	 <div id="infoText_locations"></div>
	 
	<div id="table_space_locations"></div> 

</div>
<div id="logout">
       <input type="submit" value="Logout" onClick="logout; window.location='index.jsp'" />
</div>
<div class="clear"></div>
</div>

<div id="footer">
<!-- TODO: nice Footer -->
</div>
</div>
</body>
</html>