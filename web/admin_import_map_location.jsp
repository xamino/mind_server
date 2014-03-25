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

<script src="javascript/fileupload/jquery.ui.widget.js"></script>
<script src="javascript/fileupload/jquery.iframe-transport.js"></script>
<script src="javascript/fileupload/jquery.fileupload.js"></script>
<script type="text/javascript"><script>
$(function () {
    $('#fileupload').fileupload({
        dataType: 'json',
        add: function (e, data) {
        	
            data.context = $('<button/>').text('Upload')
                .appendTo(document.body)
                .click(function () {
                    data.context = $('<p/>').text('Uploading...').replaceAll($(this));
                    data.submit();
                });
        },
        done: function (e, data) {
            data.context.text('Upload finished.');
        }
    });
});
/*$(function () {
    $('#fileupload').fileupload({
        dataType: 'json',
        done: function (e, data) {
            $.each(data.result.files, function (index, file) {
                $('<p/>').text(file.name).appendTo(document.body);
            });
        }
    });
}); */
</script>-->
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