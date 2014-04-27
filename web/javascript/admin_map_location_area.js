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
    imgLoad.bind("load", function () {
        imgWidth = this.width;
        imgHeight = this.height;

        var input = "";
        input = "Current Map:<br>";
        input += "<div id='map_png_div' style='overflow:auto; margin-top:0px; width:" + imgWidth + "px; height:" + imgHeight + "px; background-size:auto; background-image:url(images/map.png); background-repeat:no-repeat;'><div id='draw_rect_div'></div></div>";
        document.getElementById("map_container_div").innerHTML = input;
    });
} else {
    var input = "";
    input = "There is currently no map available!<br>";
    input += "<br> Please upload one - MIND needs one to work!";
    document.getElementById("map_div").innerHTML = input;
}

function doesFileExist(urlToFile) {
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
 * thanks to: http://marco-difeo.de/2011/06/13/jquery-rectangle-selection/ and math voodoo *sigh*
 * draws on mouse down on map in map --> corrects if an existing area is touched
 * coordinates x1,y1 (start point); x2,y2 (end point)
 */
// Click coordinates
var x1, x2, y1, y2;

//Variable indicates wether a mousedown event within your selection happend or not
var selection = false;

// Global mouse button variables
var gMOUSEUP = false;
var gMOUSEDOWN = false;

// Global Events if left mousebutton is pressed or nor (usability fix)
$(document).mouseup(function () {
    gMOUSEUP = true;
    gMOUSEDOWN = false;
});
$(document).mousedown(function () {
    gMOUSEUP = false;
    gMOUSEDOWN = true;
});

// Selection frame (playground :D)
$(document).on("mousedown", "#map_png_div", function (e) {
    selection = true;
    // store mouseX and mouseY
    x1 = e.pageX - this.offsetLeft;
    y1 = e.pageY - this.offsetTop;
    for (var i = 0; i < allAreas.length; i++) {	//all existing areas
        var id = allAreas[i].ID;
        if (id != "University") {	//not in University
            if (x1 >= allAreas[i].topLeftX && x1 <= (allAreas[i].topLeftX + allAreas[i].width)) {	    	//in x-values of existing area
                if (y1 >= allAreas[i].topLeftY && y1 <= (allAreas[i].topLeftY + allAreas[i].height)) { 	//in y-values of existing area
                    selection = false;
                }
            }
        }
    }

});

var TOP;
var LEFT;
var WIDTH;
var HEIGHT;
var drawrect;

// If selection is true (mousedown on selection frame) the mousemove 
// event will draw the selection div
$(document).on("mousemove", "#map_png_div", function (e) {
    if (selection) {
        // Store current mouseposition
        x2 = e.pageX - this.offsetLeft;
        y2 = e.pageY - this.offsetTop;

        // Prevent the selection div to get outside of your frame
        (x2 < 0) ? selection = false : ($(this).width() < x2) ? selection = false : (y2 < 0) ? selection = false : ($(this).height() < y2) ? selection = false : selection = true;
        ;

        // If the mouse is inside your frame resize the selection div
        if (selection) {
            // Calculate the div selection rectancle for positive and negative values
            TOP = (y1 < y2) ? y1 : y2;
            LEFT = (x1 < x2) ? x1 : x2;
            WIDTH = (x1 < x2) ? x2 - x1 : x1 - x2;
            HEIGHT = (y1 < y2) ? y2 - y1 : y1 - y2;


            for (var i = 0; i < allAreas.length; i++) {	//all existing areas
                var id = allAreas[i].ID;
                if (id != "University") {	//not in University
                    if (x2 >= allAreas[i].topLeftX && x2 <= (allAreas[i].topLeftX + allAreas[i].width)) {	    	//in x-values of existing area
                        if (y2 >= allAreas[i].topLeftY && y2 <= (allAreas[i].topLeftY + allAreas[i].height)) { 	//in y-values of existing area
                            selection = false; //x2,y2 in area
                        }
                        else if (y1 >= allAreas[i].topLeftY && y1 <= (allAreas[i].topLeftY + allAreas[i].height)) {
                            selection = false; //x2,y1 in area
                        }
                    }
                    else if (y2 >= allAreas[i].topLeftY && y2 <= (allAreas[i].topLeftY + allAreas[i].height)) {
                        if (x1 >= allAreas[i].topLeftX && x1 <= (allAreas[i].topLeftX + allAreas[i].width)) {
                            selection = false; //x1,y2 in area
                        }
                    }
                }
            }

            if (selection) {

                // Use CSS to place your selection div
                drawrect = document.getElementById("draw_rect_div");

//            	drawrect.style.border="1px solid blue";
                drawrect.style.background = "#1B94E0";
                drawrect.style.opacity = "0.4";
                drawrect.style.filter = "alpha(opacity=40)";
                drawrect.style.margin = "0px";
                drawrect.style.padding = "0px";
                drawrect.style.position = 'absolute';
                drawrect.style.zIndex = "5000";
                drawrect.style.marginLeft = LEFT + "px";
                drawrect.style.marginTop = TOP + "px";
                drawrect.style.width = WIDTH + "px";
                drawrect.style.height = HEIGHT + "px";
                drawrect.style.display = "block";	//rect stays till next comes
                drawrect.style.backgroundImage = "none";
//            });
//                $("#drawrect").show();


                //output to simply add area
//            $('#areaForm').html('( x1 : ' + x1 + ' )  ( x2 : ' + x2 + ' )  ( y1 : ' + y1 + '  )  ( y2 : ' + y2 + ' ) ');
                $('#areaForm').html('<table border="0" cellpadding="3" cellspacing="0">' +
                    '<tr><td>Id (numbers, i.e. 331):</td><td><input type="text" id="id" name="id"></td></tr>' +
                    //a user has the possibility to modify the values --> error detection!
//            		'<tr><td>X-Coordinate:</td><td id="update_tdX"><input type="text" id="xCor" name="xCor" value="'+x1+'"></td></tr>'+
//            		'<tr><td>Y-Coordinate:</td><td id="update_tdY"><input type="text" id="yCor" name="yCor" value="'+y1+'"></td></tr>'+
//            		'<tr><td>Width:</td><td id="update_tdWidth"><input type="text" id="width" name="width" value="'+WIDTH+'"></td></tr>'+
//            		'<tr><td>Height:</td><td id="update_tdHeight"><input type="text" id="height" name="height" value="'+HEIGHT+'"></td></tr>'+
//            		'<tr><td><input type="submit" value="Add Area"></td><td></td></tr></table>');
                    //so the user can't change the values --> no overlapping with over areas possible
                    '<tr id="update_tdX"><td>X-Coordinate:</td><td id="xCor">' + x1 + '</td></tr>' +
                    '<tr id="update_tdY"><td>Y-Coordinate:</td><td id="yCor">' + y1 + '</td></tr>' +
                    '<tr id="update_tdWidth"><td>Width:</td><td id="width">' + WIDTH + '</td></tr>' +
                    '<tr id="update_tdHeight"><td>Height:</td><td id="height">' + HEIGHT + '</td></tr>' +
                    '<tr><td><input type="submit" value="Add Area"></td><td></td></tr></table>');
            }
        }
    }
});
// Selection complete, hide the selection div (or fade it out)
$(document).on("mouseup", "#map_png_div", function (e) {
//$('#map_div').mouseup(function() {

    // all table rows are white
    var trs = document.getElementsByTagName("tr");
    for (var i = 0, len = trs.length; i < len; i++) {
        trs[i].style.backgroundColor = "white";
    }

    selection = false;

    //no area to add (too small)
    if (WIDTH <= 3 && HEIGHT <= 3) {
        $('#areaForm').html('Draw on the map to add an area.<br>');
    }
    else {
        //check wether one bigger area is still in the drawed rect
        var currentY = Number.MAX_VALUE;
        var currentX = Number.MAX_VALUE;
        for (var i = 0; i < allAreas.length; i++) {	//all existing areas
            var id = allAreas[i].ID;
            if (id != "University") {	//not in University
//	       		alert("id: "+id+";y1: "+y1+";y2: "+y2+";topLeftY: "+allAreas[i].topLeftY+";height: "+allAreas[i].height+";topLeftY+height: "+(allAreas[i].topLeftY + allAreas[i].height));

                //from right to left
                if (x2 <= allAreas[i].topLeftX && x1 >= allAreas[i].topLeftX) {
                    //TopLeftX and TopLeftY are in the drawed rect --> add 2px because of border
                    if (y2 >= allAreas[i].topLeftY && y1 <= allAreas[i].topLeftY) {
                        if (currentY < Number.MAX_VALUE && currentY < allAreas[i].topLeftY) {
                            //do nothing
                        }
                        else {
                            //alert("1");
                            drawrect.style.height = (HEIGHT - (y2 - allAreas[i].topLeftY) + 2) + "px";
                            //		            	var updateHeight = '<input type="text" id="height" name="height" value="'+(HEIGHT-(y2-allAreas[i].topLeftY))+'">';
                            var updateHeight = '<td>Height:</td><td>' + (HEIGHT - (y2 - allAreas[i].topLeftY) + 2) + '</td>';
                            document.getElementById("update_tdHeight").innerHTML = updateHeight;
                            currentY = allAreas[i].topLeftY;
                        }
                    }
                    //TopLeftX and (TopLeftY+height) are in the drawed rect
                    else if (y1 >= (allAreas[i].topLeftY + allAreas[i].height) && y2 <= (allAreas[i].topLeftY + allAreas[i].height)) {
                        if (currentY < Number.MAX_VALUE && currentY > (allAreas[i].topLeftY + allAreas[i].height)) {
                            //do nothing
                        } else {
                            //alert("2");
                            drawrect.style.height = (HEIGHT - ((allAreas[i].topLeftY + allAreas[i].height) - y2)) + "px";
                            drawrect.style.marginTop = (TOP + ((allAreas[i].topLeftY + allAreas[i].height) - y2)) + "px";
                            //			   				var updateHeight = '<input type="text" id="height" name="height" value="'+(HEIGHT-((allAreas[i].topLeftY+allAreas[i].height)-y2))+'">';
                            var updateHeight = '<td>Height:</td><td>' + (HEIGHT - ((allAreas[i].topLeftY + allAreas[i].height) - y2)) + '</td>';
                            document.getElementById("update_tdHeight").innerHTML = updateHeight;
                            //			   				var updateTopLeftY = '<input type="text" id="yCor" name="yCor" value="'+(y1+((allAreas[i].topLeftY+allAreas[i].height)-y2))+'">';
                            var updateTopLeftY = '<td>Y-Coordinate:</td><td>' + (y1 + ((allAreas[i].topLeftY + allAreas[i].height) - y2)) + '</td>';
                            document.getElementById("update_tdY").innerHTML = updateTopLeftY;
                            currentY = (allAreas[i].topLeftY + allAreas[i].height);
                        }
                    }

                }

                else if (x1 <= allAreas[i].topLeftX && x2 >= allAreas[i].topLeftX) {
                    //TopLeftX and TopLeftY are in the drawed rect
                    //drawing from left to right --> add 2px because of border || drawing from top to bottom --> add 2px because of border
                    if (y2 >= allAreas[i].topLeftY && y1 <= allAreas[i].topLeftY) {
                        if (x2 > (allAreas[i].topLeftX + allAreas[i].width)) {
                            if (currentY < Number.MAX_VALUE && currentY < allAreas[i].topLeftY) {
                                //do nothing
                            }
                            else {
                                //alert("3");
                                drawrect.style.height = (HEIGHT - (y2 - allAreas[i].topLeftY) + 2) + "px";
                                //		   					var updateHeight = '<input type="text" id="height" name="height" value="'+(HEIGHT-(y2-allAreas[i].topLeftY))+'">';
                                var updateHeight = '<td>Height:</td><td>' + (HEIGHT - (y2 - allAreas[i].topLeftY) + 2) + '</td>';
                                document.getElementById("update_tdHeight").innerHTML = updateHeight;
                                currentY = allAreas[i].topLeftY;
                            }
                        }
                        else if (x2 < (allAreas[i].topLeftX + allAreas[i].width)) {
                            if (currentX < Number.MAX_VALUE && currentX < allAreas[i].topLeftX) {
                                //do nothing
                            } else {
                                //alert("4");
                                drawrect.style.width = (WIDTH - (x2 - allAreas[i].topLeftX) + 2) + "px";
                                //		   					var updateWidth = '<input type="text" id="width" name="width" value="'+(WIDTH-(x2-allAreas[i].topLeftX))+'">';
                                var updateWidth = '<td>Width:</td><td>' + (WIDTH - (x2 - allAreas[i].topLeftX) + 2) + '</td>';
                                document.getElementById("update_tdWidth").innerHTML = updateWidth;
                                currentX = allAreas[i].topLeftX;
                            }
                        }
                    }
                    //TopLeftX and TopLeftY are in the drawed rect
                    //drawing from bottom to top --> add 2px because of border
                    //BUG but no bad one
                    if (y1 >= allAreas[i].topLeftY && y2 <= allAreas[i].topLeftY) {
                        if (currentX < Number.MAX_VALUE && currentX < allAreas[i].topLeftX) {
                            //do nothing
                        } else {
                            //alert("5");
                            drawrect.style.width = (WIDTH - (x2 - allAreas[i].topLeftX) + 2) + "px";
                            drawrect.style.height = HEIGHT + "px";
//			   				var updateWidth = '<input type="text" id="width" name="width" value="'+(WIDTH-(x2-allAreas[i].topLeftX))+'">';
                            var updateWidth = '<td>Width:</td><td>' + (WIDTH - (x2 - allAreas[i].topLeftX) + 2) + '</td>';
                            document.getElementById("update_tdWidth").innerHTML = updateWidth;
                            currentX = allAreas[i].topLeftX;
                        }
                    }
                    //TopLeftX and (TopLeftY+height) are in the drawed rect
                    //from left to right
                    else if (y1 >= (allAreas[i].topLeftY + allAreas[i].height) && y2 <= (allAreas[i].topLeftY + allAreas[i].height)) {
                        if (currentY < Number.MAX_VALUE && currentY > (allAreas[i].topLeftY + allAreas[i].height)) {
                            //do nothing
                        } else {
                            //alert("6");
//			   				if(!(y2<allAreas[i].topLeftY)){
                            drawrect.style.height = (HEIGHT - ((allAreas[i].topLeftY + allAreas[i].height) - y2)) + "px";
                            drawrect.style.marginTop = (TOP + ((allAreas[i].topLeftY + allAreas[i].height) - y2)) + "px";
                            //			   				var updateHeight = '<input type="text" id="height" name="height" value="'+(HEIGHT-((allAreas[i].topLeftY+allAreas[i].height)-y2))+'">';
                            var updateHeight = '<td>Height:</td><td>' + (HEIGHT - ((allAreas[i].topLeftY + allAreas[i].height) - y2)) + '</td>';
                            document.getElementById("update_tdHeight").innerHTML = updateHeight;
                            //			   				var updateTopLeftY = '<input type="text" id="yCor" name="yCor" value="'+(y1+((allAreas[i].topLeftY+allAreas[i].height)-y2))+'">';
                            var updateTopLeftY = '<td>Y-Coordinate:</td><td>' + (y1 + ((allAreas[i].topLeftY + allAreas[i].height) - y2)) + '</td>';
                            document.getElementById("update_tdY").innerHTML = updateTopLeftY;
                            currentY = (allAreas[i].topLeftY + allAreas[i].height);
                        }
                    }

                }
                //(TopLeftX+width) and TopLeftY are in the drawed rect
                else if (x2 <= (allAreas[i].topLeftX + allAreas[i].width) && x1 >= (allAreas[i].topLeftX + allAreas[i].width)) {
                    //draw from top to bottom
                    if (y2 >= allAreas[i].topLeftY && y1 <= allAreas[i].topLeftY) {
                        if (currentX < Number.MAX_VALUE && currentX > (allAreas[i].topLeftX + allAreas[i].width)) {
                            //do nothing
                        }
                        else {
                            //alert("7");
                            drawrect.style.width = (WIDTH - ((allAreas[i].topLeftX + allAreas[i].width) - x2)) + "px";
                            drawrect.style.marginLeft = (LEFT + ((allAreas[i].topLeftX + allAreas[i].width) - x2)) + "px";
                            //		   				var updateWidth = '<input type="text" id="width" name="width" value="'+(WIDTH-((allAreas[i].topLeftX+allAreas[i].width)-x2))+'">';
                            var updateWidth = '<td>Width:</td><td>' + (WIDTH - ((allAreas[i].topLeftX + allAreas[i].width) - x2)) + '</td>';
                            document.getElementById("update_tdWidth").innerHTML = updateWidth;
                            //		   				var updateTopLeftX = '<input type="text" id="xCor" name="xCor" value="'+(x1+((allAreas[i].topLeftX+allAreas[i].width)-x2))+'">';
                            var updateTopLeftX = '<td>X-Coordinate:</td><td>' + (x1 + ((allAreas[i].topLeftX + allAreas[i].width) - x2)) + '</td>';
                            document.getElementById("update_tdX").innerHTML = updateTopLeftX;
                            currentX = (allAreas[i].topLeftX + allAreas[i].width);
                        }
                    }
                    //draw from bottom to top
                    else if (y1 >= allAreas[i].topLeftY && y2 <= allAreas[i].topLeftY) {
                        if (currentX < Number.MAX_VALUE && currentX > (allAreas[i].topLeftX + allAreas[i].width)) {
                            //do nothing
                        } else {
                            //alert("8");
                            drawrect.style.width = (WIDTH - ((allAreas[i].topLeftX + allAreas[i].width) - x2)) + "px";
                            drawrect.style.marginLeft = (LEFT + ((allAreas[i].topLeftX + allAreas[i].width) - x2)) + "px";
                            //		   				var updateWidth = '<input type="text" id="width" name="width" value="'+(WIDTH-((allAreas[i].topLeftX+allAreas[i].width)-x2))+'">';
                            var updateWidth = '<td>Width:</td><td>' + (WIDTH - ((allAreas[i].topLeftX + allAreas[i].width) - x2)) + '</td>';
                            document.getElementById("update_tdWidth").innerHTML = updateWidth;
                            //		   				var updateTopLeftX = '<input type="text" id="xCor" name="xCor" value="'+(x1+((allAreas[i].topLeftX+allAreas[i].width)-x2))+'">';
                            var updateTopLeftX = '<td>X-Coordinate:</td><td>' + (x1 + ((allAreas[i].topLeftX + allAreas[i].width) - x2)) + '</td>';
                            document.getElementById("update_tdX").innerHTML = updateTopLeftX;
                            currentX = (allAreas[i].topLeftX + allAreas[i].width);
                        }
                    }
                }
            }
        }
    }


//    $("#selection").hide();
});

// Usability fix. If mouse leaves the selection and enters the selection frame again with mousedown
$(document).on("mouseenter", "#map_png_div", function (e) {
//$("#map_div").mouseenter(function() {
    (gMOUSEDOWN) ? selection = true : selection = false;
});
// Usability fix. If mouse leaves the selection and enters the selection div again with mousedown
$(document).on("mouseenter", "#draw_rect_div", function (e) {	//#selection ???????
//$("#selection").mouseenter(function() {
    (gMOUSEDOWN) ? selection = true : selection = false;
});
// Set selection to false, to prevent further selection outside of your selection frame
$(document).on("mouseleave", "#map_png_div", function (e) {
//$('#map_div').mouseleave(function() {
    selection = false;
});

/*************** end of drawing on map *****************/


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
            tablecontents += "<tr id='room_" + data.object[i].ID + "'>";	//id to highlight clicked area
            tablecontents += "<td>" + data.object[i].ID + "</td>";
            tablecontents += "<td>" + data.object[i].locations.length + "</td>";
            tablecontents += "<td>" + data.object[i].topLeftX + "</td>";
            tablecontents += "<td>" + data.object[i].topLeftY + "</td>";
            tablecontents += "<td>" + data.object[i].width + "</td>";
            tablecontents += "<td>" + data.object[i].height + "</td>";
            if (data.object[i].ID == "University") {
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
    var r = confirm("Do you want to remove the Area (ID: '" + data.ID + "')?");
    if (r == true) {
        var areatodelete = new Area(data.ID, null, 0, 0, 0, 0);
        doTask("AREA_REMOVE", areatodelete, function (event) {

            alert("The following Area has been deleted:\n" +
                "ID: " + data.ID);
            window.location.reload();

        });
    }
}

/**
 * on Button click 'Add Area' in admin_import_map_location.jsp
 * adds an area with the given id
 */
$(document).on("submit", "#areaForm", function (event) {
    event.preventDefault();
    if (checkInp()) {	//id is valid
        var id, xCor, yCor, width, height;
        id = $("#id").val();
        if (id != null && id != "") {
//	    xCor = $("#xCor").val();
//	    yCor = $("#yCor").val();
//	    width = $("#width").val();
//	    height = $("#height").val();
            xCor = document.getElementById("xCor").innerHTML - 2;	//--> because of border
            yCor = document.getElementById("yCor").innerHTML - 2;	//--> because of border
            width = document.getElementById("width").innerHTML;
            height = document.getElementById("height").innerHTML;
            newArea = new Area(id, null, xCor, yCor, width, height);

            doTask("AREA_ADD", newArea, function () {
                window.location.reload();
            });
        } else {
            alert("You have to set an id for your area!");
        }
    } else {
        alert("The id may only contain numbers");	//:A-Z, a-z, 0-9, - and _
    }

});

/************** draw existing areas on map *************/

var allAreas;

/**
 * draw all areas on the given map in admin_import_map_location.jsp
 * areas are clickable
 */
function drawAreas() {
    var areas = new Area(null, null, 0, 0, 0, 0);
    doTask("AREA_READ", areas, function (data) {
        allAreas = data.object;
        if (data.object.length == 1) {

        }
        else {
            for (var i = 0; i < data.object.length; i++) {
                var id = data.object[i].ID;
                if (id == "University") {
                    // don't do anything --> University isn't been drawn
                }
                else {
                    var x = data.object[i].topLeftX;
                    var y = data.object[i].topLeftY;
                    var width = data.object[i].width;
                    var height = data.object[i].height;

                    var div = document.getElementById("map_png_div");
                    var clickedArea = "";
                    clickedArea += '<div id="' + id + '" onClick="clickOnArea(' + id + ')" style=" cursor:pointer; background-color: #C2DFFF; border: 2px solid black; opacity: .5; filter: alpha(opacity=50); position:absolute; margin-top:' + (y + 2) + 'px; margin-left: ' + (x + 2) + 'px; width: ' + (width - 4) + 'px; height: ' + (height - 4) + 'px;"></div>';
                    div.innerHTML = div.innerHTML + clickedArea;

                }
            }

        }

    });

}

/**
 * detects which area is cklicked and shows the user information about this area
 */
function clickOnArea(id) {
    var areaOfId = getAreaById(id);

    var id = areaOfId.ID;
    var x = areaOfId.topLeftX;
    var y = areaOfId.topLeftY;
    var width = areaOfId.width;
    var height = areaOfId.height;

    // all table rows are white
    var trs = document.getElementsByTagName("tr");
    for (var i = 0, len = trs.length; i < len; i++) {
        trs[i].style.backgroundColor = "white";
    }
    //highlight clicked area in table
    var idToHightlight = 'room_' + id;
    var highlightRow = document.getElementById(idToHightlight);
    highlightRow.style.backgroundColor = "lightblue";


    var cklickOnArea = document.getElementById(id);
    cklickOnArea.style.cursor = 'pointer';
//        $('#areaForm').html('Edit room with the id '+id+':<br><table border="0" cellpadding="3" cellspacing="0">'+
////        		'<tr><td>Id:</td><td><input type="text" id="id" name="id" value="'+id+'"></td></tr>'+
//        		'<tr><td>X-Coordinate:</td><td><input type="text" id="xCor" name="xCor" value="'+x+'"></td></tr>'+
//        		'<tr><td>Y-Coordinate:</td><td><input type="text" id="yCor" name="yCor" value="'+y+'"></td></tr>'+
//        		'<tr><td>Width:</td><td><input type="text" id="width" name="width" value="'+width+'"></td></tr>'+
//        		'<tr><td>Height:</td><td><input type="text" id="height" name="height" value="'+height+'"></td></tr>'+
//        		'<tr><td><input type="button" value="Edit Area" onClick="editArea('+id+')"></td><td></td></tr></table>');
    $('#areaForm').html('Information to room with id ' + id + ':<br><table border="0" cellpadding="3" cellspacing="0">' +
        '<tr><td>X-Coordinate:</td><td>' + x + '</td></tr>' +
        '<tr><td>Y-Coordinate:</td><td>' + y + '</td></tr>' +
        '<tr><td>Width:</td><td>' + width + '</td></tr>' +
        '<tr><td>Height:</td><td>' + height + '</td></tr>' +
        '</table>');

}

/**
 * currently NOT IN USE
 * the user can edit the clicked area
 */
function editArea(id) {

    var xCor, yCor, width, height;
    xCor = $("#xCor").val();
    yCor = $("#yCor").val();
    width = $("#width").val();
    height = $("#height").val();

    var editArea = new Area(id, null, xCor, yCor, width, height);
    doTask("AREA_UPDATE", editArea, function () {
        window.location.reload();
    });

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

/****************** input check ********************/

/**
 * check wether id only contains A-Z, a-z, 0-9, - and _
 * returns true if input is ok
 */
function checkInp() {
    var x = document.getElementById("id").value;
//  if (x.match(/^[a-zA-Z0-9\-_]+$/))		--> problem with click method
    if (x.match(/^[0-9]+$/)) {
        return true;
    }
}

/****************** get data of area ********************/

/**
 * returns the data of an area
 * @param id
 *    id of the area to return
 */
function getAreaById(id) {
    for (var i = 0; i < allAreas.length; i++) {
        if (allAreas[i].ID == id) {
            return allAreas[i];
        }
    }
}
