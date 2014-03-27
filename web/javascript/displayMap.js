var clicking = false;
var previousX;
var previousY;

var usersToDisplay;

function handleAllUsersPositionPlacement(allusers){
	
	usersToDisplay = allusers;
	
	//var iconelements = "";
	var img;
	var offset=130;
	var divToAugment = document.getElementById("mapscroll");
	for ( var i = 0; i < usersToDisplay.length; i++) {
		//iconelements += "<img class='micon' src='images/micons/"+allusers[i].iconRef+"'/>";
		//var content = document.createTextNode(iconelements);
//		theDiv.appendChild(content);
		
		   img=document.createElement("img");
		   img.className="micon";
		   img.src="images/micons/"+usersToDisplay[i].iconRef;
		   img.id="icon_"+usersToDisplay[i].email;
		   img.style.position ="absolute";
		   //TODO compute size & placement for icon on screen -> maybe compute prior to this and add as user attribute->user.x
		   img.style.top="15%";
		   img.style.left=offset*i+"px";
		   img.style.width="130px";
		   //img.style.width="10%";
		   divToAugment.appendChild(img);
		   img = null;
		
	}
	
//	var div = document.getElementById("mapWithIcons");
//	alert("na: "+div.innterHTML +" hm "+ iconelements);
//	div.innerHTML = div.innterHTML + iconelements;
	
}


$(document).on("mousedown", "#mapscroll", function (e) {
e.preventDefault();
previousX = e.clientX;
previousY = e.clientY;
clicking = true;
});

$(document).mouseup(function() {
    clicking = false;
});


$(document).mousemove(function(e) {	
    if (clicking) {
        e.preventDefault();
        //accelerated panning
//        var directionX = (previousX - e.clientX) > 0 ? 1 : -1;
//        var directionY = (previousY - e.clientY) > 0 ? 1 : -1;
//        $("#mapscroll").scrollLeft($("#mapscroll").scrollLeft() + 10 * directionX);
//        $("#mapscroll").scrollTop($("#mapscroll").scrollTop() + 10 * directionY);
        $("#mapscroll").scrollLeft($("#mapscroll").scrollLeft() + (previousX - e.clientX));
        $("#mapscroll").scrollTop($("#mapscroll").scrollTop() + (previousY - e.clientY));
        previousX = e.clientX;
        previousY = e.clientY;
    }
});


$("#scroll").mouseleave(function(e) {
    clicking = false;
});


//SLIDE BAR STUFF
var trigger = 1;
function showVal(value){
	
	if(trigger==1){trigger=2;}else{trigger=1;}
	document.getElementById("slidertext").innerHTML = trigger+"value: "+value;

}