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
		input += "<div id='testimage_png' style='overflow:auto; margin-top:0px; width:"+imgWidth+"px; height:"+imgHeight+"px; background-size:auto; background-image:url(images/map.png); background-repeat:no-repeat;'><div id='selection'></div></div>";
		document.getElementById("map_div").innerHTML = input;
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
$(document).on("mousedown","#testimage_png", function(e) {
    selection = true;
    // store mouseX and mouseY
    x1 = e.pageX - this.offsetLeft;
    y1 = e.pageY - this.offsetTop;
});

// If selection is true (mousedown on selection frame) the mousemove 
// event will draw the selection div
$(document).on("mousemove","#testimage_png", function(e) {
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
            var selection2 = document.getElementById("selection");
            
            		//$("#selection").css({
//            	selection2.style.border="5px solid black";
            	selection2.style.background="#1B94E0";
            	selection2.style.opacity="0.4";
            	selection2.style.filter="alpha(opacity=40)";
            	selection2.style.margin="0px";
            	selection2.style.padding="0px";
                selection2.style.position = 'relative';
//                selection2.style.zIndex = "5000";
                selection2.style.marginLeft = LEFT+"px";
//                selection2.style.marginTop = "0px !important";
                selection2.style.marginTop = (TOP-20)+"px";
                selection2.style.width = WIDTH+"px";
                selection2.style.height = HEIGHT+"px";
                selection2.style.display="block";
                selection2.style.backgroundImage="none";
//            });
                //selection2.style.display = 'block';
//                $("#selection").show();
            

            // Info output
            $('#status2').html('( x1 : ' + x1 + ' )  ( x2 : ' + x2 + ' )  ( y1 : ' + y1 + '  )  ( y2 : ' + y2 + ' ) ');
        }
    }
});
// Selection complete, hide the selection div (or fade it out)
$(document).on("mouseup","#map_div", function(e) {
//$('#map_div').mouseup(function() {
    selection = false;
//    $("#selection").hide();
});
// Usability fix. If mouse leaves the selection and enters the selection frame again with mousedown
$(document).on("mouseenter","#map_div", function(e) {
//$("#map_div").mouseenter(function() {
    (gMOUSEDOWN) ? selection = true : selection = false;
});
// Usability fix. If mouse leaves the selection and enters the selection div again with mousedown
$(document).on("mouseenter","#selection", function(e) {
//$("#selection").mouseenter(function() {
    (gMOUSEDOWN) ? selection = true : selection = false;
});
// Set selection to false, to prevent further selection outside of your selection frame
$(document).on("mouseleave","#map_div", function(e) {
//$('#map_div').mouseleave(function() {
    selection = false;
});