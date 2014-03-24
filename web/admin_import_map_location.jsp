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
	Current Map:
	<img src="images/map.png" alt="MIND_MAP" > 


<!--  
<script type="text/javascript">

	function fileChange()
{
    //FileList Objekt aus dem Input Element mit der ID "fileA"
    var fileList = document.getElementById("fileA").files;
 
    //File Objekt (erstes Element der FileList)
    var file = fileList[0];
 
    //File Objekt nicht vorhanden = keine Datei ausgewählt oder vom Browser nicht unterstützt
    if(!file)
        return;
 
    document.getElementById("fileName").innerHTML = 'Dateiname: ' + file.name;
    document.getElementById("fileSize").innerHTML = 'Dateigröße: ' + file.size + ' B';
    document.getElementById("fileType").innerHTML = 'Dateitype: ' + file.type;
    document.getElementById("progress").value = 0;
    document.getElementById("prozent").innerHTML = "0%";
}

var client = null;
 
function uploadFile()
{
	alert(document.getElementById("fileA").value);
    //Wieder unser File Objekt
    var file = document.getElementById("fileA").files[0];
    //FormData Objekt erzeugen
    var formData = new FormData();
    //XMLHttpRequest Objekt erzeugen
    client = new XMLHttpRequest();
 
    var prog = document.getElementById("progress");
 
    if(!file)
        return;
 
    prog.value = 0;
    prog.max = 100;
 
    //Fügt dem formData Objekt unser File Objekt hinzu
    formData.append("datei", file);
 
    client.onerror = function(e) {
        alert("onError");
    };
 
    client.onload = function(e) {
        document.getElementById("prozent").innerHTML = "100%";
        prog.value = prog.max;
    };
 
    client.upload.onprogress = function(e) {
        var p = Math.round(100 / e.total * e.loaded);
        document.getElementById("progress").value = p;            
        document.getElementById("prozent").innerHTML = p + "%";
    };
 
    client.onabort = function(e) {
        alert("Upload abgebrochen");
    };
 
    client.open("POST", "upload.php");
    client.send(formData);
} 

function uploadAbort() {
    if(client instanceof XMLHttpRequest)
        //Briecht die aktuelle Übertragung ab
        client.abort();
}
 
</script>
 
<form action="" method="post" enctype="multipart/form-data">
    <input name="file" type="file" id="fileA" onchange="fileChange();"/>
    <input name="upload" value="Upload" type="button" onclick="uploadFile();" />
    <input name="abort" value="Abbrechen" type="button" onclick="uploadAbort();" />
</form>
<div>
    <div id="fileName"></div>
    <div id="fileSize"></div>
    <div id="fileType"></div>
    <progress id="progress" style="margin-top:10px"></progress> <span id="prozent"></span>
</div>
-->


<!--
<input id="fileupload" type="file" name="files[]"  multiple>
<script src="javascript/fileupload/jquery.ui.widget.js"></script>
<script src="javascript/fileupload/jquery.iframe-transport.js"></script>
<script src="javascript/fileupload/jquery.fileupload.js"></script>
<script type="text/javascript">
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
</script>
-->

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