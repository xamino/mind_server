/********************* loads map if existing ******************/

/**
 * loads map if it exists (in admin_import_map_location.jsp) 
 * displays information if no map exists
 */
var url_png = doesFileExist("images/map.png"); 
if (url_png == true) { 
    var imgWidth; 
    var imgHeight;
    var imgLoad = $("<img />");
    imgLoad.attr("src", "images/map.png");
    imgLoad.unbind("load");
    imgLoad.bind("load", function(){
    	imgWidth = this.width;
    	imgHeight =this.height;
    	
	   	var input = "";
		input = "Current Map:<br>";
		input += "<div id='map_png_div' style='overflow:auto; margin-top:0px; width:"+imgWidth+"px; height:"+imgHeight+"px; background-size:auto; background-image:url(images/map.png); background-repeat:no-repeat;'><div id='draw_rect_div'></div></div>";
		document.getElementById("map_container_div").innerHTML = input;
//		var test =$("div");
//		<div id='draw_rect_div'></div>
//		document.getElementById("map_png_div").appendChild(test);
	});
} else {
	var input = "";
	input = "There is currently no map available!<br>";
	input += "<br> Please upload one - MIND needs one to work!";
    document.getElementById("map_div").innerHTML = input;
}

function doesFileExist(urlToFile){
    var xhr = new XMLHttpRequest();
    xhr.open('HEAD', urlToFile, false);
    xhr.send();
    if (xhr.status == "404") {
        return false;
    } else {
        return true;
    }
}

/**
 * shows x and y coordinate of mouse down (in admin_import_map_location.jsp) 
 * currently not in use --> do not delete (could be helpful in the future) 
 * for use this has to be in the html: <br><div id="testy"></div>
 */
//for all: "*"
/*$( "#map_div", document.body ).click(function( event ) {
	var offset = $( this ).offset();
	event.stopPropagation();
	alert("left-distance: "+(event.clientX - offset.left));
alert("top-distance: "+(event.clientY - offset.top));
	$( "#testy" ).text( this.tagName +
" coords ( " + offset.left + ", " + offset.top + " )" );
});*/


/****************************** draw in map ******************************/

/**
 * thanks to: http://marco-difeo.de/2011/06/13/jquery-rectangle-selection/
 */
// Click coordinates
var x1, x2, y1, y2;

//Variable indicates wether a mousedown event within your selection happend or not
var selection = false;

// Global mouse button variables
var gMOUSEUP = false;
var gMOUSEDOWN = false;

// Global Events if left mousebutton is pressed or nor (usability fix)
$(document).mouseup(function() {
    gMOUSEUP = true;
    gMOUSEDOWN = false;
});
$(document).mousedown(function() {
    gMOUSEUP = false;
    gMOUSEDOWN = true;
});

// Selection frame (playground :D)
$(document).on("mousedown","#map_png_div", function(e) {
	selection = true;
    // store mouseX and mouseY
	x1 = e.pageX - this.offsetLeft;
    y1 = e.pageY - this.offsetTop;
	for(var i = 0; i<allAreas.length; i++){	//all existing areas
		 var id = allAreas[i].ID;
		 if(id != "universe"){	//not in universe
			 if(x1 >= allAreas[i].topLeftX && x1 <= (allAreas[i].topLeftX+ allAreas[i].width)){	    	//in x-values of existing area
				 if(y1 >= allAreas[i].topLeftY && y1 <= (allAreas[i].topLeftY+ allAreas[i].height)){ 	//in y-values of existing area
					 selection = false;
				 }
			 }
		 }
	 }
		
});

// If selection is true (mousedown on selection frame) the mousemove 
// event will draw the selection div
$(document).on("mousemove","#map_png_div", function(e) {
    if (selection) {
        // Store current mouseposition
        x2 = e.pageX - this.offsetLeft;
        y2 = e.pageY - this.offsetTop;

        // Prevent the selection div to get outside of your frame
        (x2 < 0) ? selection = false : ($(this).width() < x2) ? selection = false : (y2 < 0) ? selection = false : ($(this).height() < y2) ? selection = false : selection = true;;

        // If the mouse is inside your frame resize the selection div
        if (selection) {
            // Calculate the div selection rectancle for positive and negative values
            var TOP = (y1 < y2) ? y1 : y2;
            var LEFT = (x1 < x2) ? x1 : x2;
            var WIDTH = (x1 < x2) ? x2 - x1 : x1 - x2;
            var HEIGHT = (y1 < y2) ? y2 - y1 : y1 - y2;

            
            // Use CSS to place your selection div
            var drawrect = document.getElementById("draw_rect_div");
            
//            	drawrect.style.border="1px solid blue";
            	drawrect.style.background="#1B94E0";
            	drawrect.style.opacity="0.4";
            	drawrect.style.filter="alpha(opacity=40)";
            	drawrect.style.margin="0px";
            	drawrect.style.padding="0px";
            	drawrect.style.position = 'absolute';
//                drawrect.style.zIndex = "5000";
            	drawrect.style.marginLeft = LEFT+"px";
            	drawrect.style.marginTop = TOP+"px";
            	drawrect.style.width = WIDTH+"px";
            	drawrect.style.height = HEIGHT+"px";
            	drawrect.style.display="block";	//rect stays till next comes
            	drawrect.style.backgroundImage="none";
//            });
//                $("#drawrect").show();
            

            //output to simply add area
//            $('#addAreaForm').html('( x1 : ' + x1 + ' )  ( x2 : ' + x2 + ' )  ( y1 : ' + y1 + '  )  ( y2 : ' + y2 + ' ) ');
            $('#addAreaForm').html('<table border="0" cellpadding="3" cellspacing="0">'+
            		'<tr><td>Id (name, i.e. Room 331):</td><td><input type="text" id="id" name="id"></td></tr>'+
            		'<tr><td>X-Coordinate:</td><td><input type="text" id="xCor" name="xCor" value="'+x1+'"></td></tr>'+
            		'<tr><td>Y-Coordinate:</td><td><input type="text" id="yCor" name="yCor" value="'+y1+'"></td></tr>'+
            		'<tr><td>Width:</td><td><input type="text" id="width" name="width" value="'+WIDTH+'"></td></tr>'+
            		'<tr><td>Height:</td><td><input type="text" id="height" name="height" value="'+HEIGHT+'"></td></tr>'+
            		'<tr><td><input type="submit" value="Add Area"></td><td></td></tr></table>');
        }
    }
});
// Selection complete, hide the selection div (or fade it out)
$(document).on("mouseup","#map_png_div", function(e) {
//$('#map_div').mouseup(function() {
    selection = false;
//    $("#selection").hide();
});
// Usability fix. If mouse leaves the selection and enters the selection frame again with mousedown
$(document).on("mouseenter","#map_png_div", function(e) {
//$("#map_div").mouseenter(function() {
    (gMOUSEDOWN) ? selection = true : selection = false;
});
// Usability fix. If mouse leaves the selection and enters the selection div again with mousedown
$(document).on("mouseenter","#draw_rect_div", function(e) {	//#selection ???????
//$("#selection").mouseenter(function() {
    (gMOUSEDOWN) ? selection = true : selection = false;
});
// Set selection to false, to prevent further selection outside of your selection frame
$(document).on("mouseleave","#map_png_div", function(e) {
//$('#map_div').mouseleave(function() {
    selection = false;
});


////////////// doesn't work =( ///////////////
$(document).on("mousedown","#331", function(e) { 
	selection = false;
	});


/**
 * on Button click 'Add Area' in admin_import_map_location.jsp
 * ads an area with the given id and values
 */
$(document).on("submit", "#addAreaForm", function (event) {
    event.preventDefault();
    var id, xCor, yCor, width, height;
    id = $("#id").val();
    if (id != null && id != "") {
    xCor = $("#xCor").val();
//    if (xCor != null && xCor != "") {
    yCor = $("#yCor").val();
//    if (yCor != null && yCor != "") {
    width = $("#width").val();
//    if (width != null && width != "") {
    height = $("#height").val();
//    if (height != null && height != "") {
    newArea = new Area(id, null, xCor, yCor, width, height);

    doTask("AREA_ADD", newArea, function(){
    	window.location.reload();
    });
//    }}}}
    }else{
    	alert("You have to set an id for your area!");
    }

});

/************** draw existing areas on map *************/

var allAreas;

function drawAreas(){
	var areas = new Area(null, null, 0, 0, 0, 0);
    doTask("AREA_READ", areas, function(data){
    	allAreas = data.object;
    	if (data.object.length == 1) {
    		
    	}
    	else{
    		for (var i = 0; i < data.object.length; i++) {
    			var id = data.object[i].ID;
    			if(id == "universe"){
    				// don't do anything --> universe isn't been drawn
    			}
    			else{
                var x = data.object[i].topLeftX;
                var y = data.object[i].topLeftY;
                var width = data.object[i].width;
                var height = data.object[i].height;
                
                var div = document.getElementById( "map_png_div" );
                var testytest = "";
                testytest += '<div id="'+id+'" onClick="clickOnArea('+id+')" style=" cursor:pointer; background-color: #C2DFFF; border: 2px solid black; opacity: .5; filter: alpha(opacity=50); position:absolute; margin-top:'+(y+2)+'px; margin-left: '+(x+2)+'px; width: '+(width-4)+'px; height: '+(height-4)+'px;"></div>'; 
                div.innerHTML = div.innerHTML + testytest;
                
             //If mouse leaves the selection
//                var idAsString = '"#'+id+'"';
//                $(document).on("mouseenter",idAsString, function(e) {
//                	e.stopPropagation();      
//                	selection = false;
//                    });
                

//    			$(document).on("mousedown",idAsString, function(e) {
//    				e.stopPropagation(); 
//    				selection = false;
//    				});
                
                
                
    			}}	//else and for

    	}

    });
    
}


function getAreaById (id){
	for ( var i = 0; i < allAreas.length; i++) {
		if(allAreas[i].ID == id){
			return allAreas[i];
		}
	}
}


function clickOnArea(id){
	
	var areaOfId = getAreaById(id);
	
	
	var id = areaOfId.ID;
    var x = areaOfId.topLeftX;
    var y = areaOfId.topLeftY;
    var width = areaOfId.width;
    var height = areaOfId.height;
//        alert("id: "+id+"x: "+x+"y: "+y);

	var cklickOnArea = document.getElementById(id);
//
    cklickOnArea.style.cursor = 'pointer';
        $('#addAreaForm').html('Edit room with the id '+id+':<br><table border="0" cellpadding="3" cellspacing="0">'+
//        		'<tr><td>Id:</td><td><input type="text" id="id" name="id" value="'+id+'"></td></tr>'+
        		'<tr><td>X-Coordinate:</td><td><input type="text" id="xCor" name="xCor" value="'+x+'"></td></tr>'+
        		'<tr><td>Y-Coordinate:</td><td><input type="text" id="yCor" name="yCor" value="'+y+'"></td></tr>'+
        		'<tr><td>Width:</td><td><input type="text" id="width" name="width" value="'+width+'"></td></tr>'+
        		'<tr><td>Height:</td><td><input type="text" id="height" name="height" value="'+height+'"></td></tr>'+
        		'<tr><td><input type="button" value="Edit Area" onClick="editArea('+id+')"></td><td></td></tr></table>');
	
}

function editArea(id){

	alert(id);
//	var readArea = new Area(id, null, 0, 0, 0, 0);
//	doTask("AREA_READ", readArea, function(){
//		window.location.reload();
	
	
	
		var xCor, yCor, width, height;
	    xCor = $("#xCor").val();
	    yCor = $("#yCor").val();
	    width = $("#width").val();
	    height = $("#height").val();
		
		var editArea = new Area(id, null, xCor, yCor, width, height);
		doTask("AREA_UPDATE", editArea, function(){
			window.location.reload();
		});
	
//	});
}


/****************Admin - Area Management****************/

/**
 * loads all areas on load of page admin_import_map_location.jsp
 */
function loadAreas() {
    //String ID, DataList<Location> locations, int topLeftX, int topLeftY, int width, int height
    var areas = new Area(null, null, 0, 0, 0, 0);
    doTask("AREA_READ", areas, writeAreas);
}

function writeAreas(data) {
    if (data.object.length == 0) {
        var noLocationsInDatabase = "There are currently no areas in the database. Add areas in the MIND-application.";
        document.getElementById("table_space_areas").innerHTML = noLocationsInDatabase;
    }
    else {

        var tablecontents = "";
        tablecontents = "<table border ='1'>";
        tablecontents += "<tr>";
        tablecontents += "<td>ID: </td>";
        //TODO: anzahl der locations
        tablecontents += "<td>Number of Locations in this Area: </td>";
        tablecontents += "<td>Top-Left-X-Value: </td>";
        tablecontents += "<td>Top-Left-Y-Value: </td>";
        tablecontents += "<td>Width: </td>";
        tablecontents += "<td>Height: </td>";
        tablecontents += "</tr>";

        for (var i = 0; i < data.object.length; i++) {
            tablecontents += "<tr>";
            tablecontents += "<td>" + data.object[i].ID + "</td>";
            tablecontents += "<td>" + data.object[i].locations.length + "</td>";
            tablecontents += "<td>" + data.object[i].topLeftX + "</td>";
            tablecontents += "<td>" + data.object[i].topLeftY + "</td>";
            tablecontents += "<td>" + data.object[i].width + "</td>";
            tablecontents += "<td>" + data.object[i].height + "</td>";
            if (data.object[i].ID == "universe") {
                tablecontents += "<td><input type='submit' value='Remove Location' disabled='true' /></td>";
            } else {
                tablecontents += "<td><input type='submit' value='Remove Location' onClick='removeAreaViaPopup(" + JSON.stringify(data.object[i]) + ")'/></td>";
            }
            tablecontents += "</tr>";
        }
        tablecontents += "</table>";
        document.getElementById("table_space_areas").innerHTML = tablecontents;
        drawAreas();

    }

}


/**
 * Creates a popup, enabling the admin to delete the area
 * @param data
 * the location data that can be deleted (JSON.stringified)
 */
function removeAreaViaPopup(data) {
    var r = confirm("Do you want to remove the Area (ID: '" + data.id + "')?");
    if (r == true) {
        var areatodelete = new Area(data.ID, null, 0, 0, 0, 0);
        doTask("AREA_REMOVE", areatodelete, function (event) {

            alert("The following Area has been deleted:\n" +
                "ID: " + data.ID);
            window.location.reload();

        });
    }
}

/****************Admin - Location Management****************/


/**
 * loads all locations on load of page admin_import_map_location.jsp
 */
function loadLocations() {
    var locations = new Location(0, 0, null);
    doTask("LOCATION_READ", locations, writeLocations);
}

function writeLocations(data) {
    if (data.object.length == 0) {
        var noLocationsInDatabase = "<br><br>There are currently no locations in the database. Add locations in the MIND-application.";
        document.getElementById("table_space_locations").innerHTML = noLocationsInDatabase;
    }
    else {

        var tablecontents = "";
        tablecontents = "<table border ='1'>";
        tablecontents += "<tr>";
        tablecontents += "<td>X-Coordinate: </td>";
        tablecontents += "<td>Y-Coordinate: </td>";
        tablecontents += "<td>Remove Location: </td>";
        tablecontents += "</tr>";

        for (var i = 0; i < data.object.length; i++) {
            tablecontents += "<tr>";
            tablecontents += "<td>" + data.object[i].coordinateX + "</td>";
            tablecontents += "<td>" + data.object[i].coordinateY + "</td>";
            tablecontents += "<td><input type='submit' value='Remove Location' onClick='removeLocationViaPopup(" + JSON.stringify(data.object[i]) + ")'/></td>";
            tablecontents += "</tr>";
        }
        tablecontents += "</table>";
        document.getElementById("table_space_locations").innerHTML = tablecontents;

    }

}


/**
 * Creates a popup, enabling the admin to delete the location
 * @param data
 * the location data that can be deleted (JSON.stringified)
 */
function removeLocationViaPopup(data) {
    var r = confirm("Do you want to remove the location (x: '" + data.coordinateX + "',y: '" + data.coordinateY + "')?");
    if (r == true) {
        var locationtodelete = new Location(data.coordinateX, data.coordinateY, null);
        doTask("LOCATION_REMOVE", locationtodelete, function (event) {

            alert("The following location has been deleted:\n" +
                "X-Coordinate: " + data.coordinateX +
                "\nY_Coordinate: " + data.coordinateY);
            window.location.reload();

        });
    }
}
