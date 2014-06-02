 
var locationCount;
var locations;
var devices;
var hotspots;

function initShowWifi(){
    var area = new Area("University", null, null, null, null, null);
    doTask("AREA_READ", area, filterLocationStuff);
}

function filterLocationStuff(data){
	locations = data.object[0].locations;
//	alert(JSON.stringify(locations));
//	alert(JSON.stringify(locations[0].wifiMorsels[0]));
//	alert(JSON.stringify(locations[0].wifiMorsels));
	locationCount = locations.length;
	devices = new Array();
	hotspots = new Array();
	var hotspottemp;
	for ( var i = 0; i < locations.length; i++) {
		for ( var j = 0; j < locations[i].wifiMorsels.length; j++) {
			if(!(contains(devices,locations[i].wifiMorsels[j].deviceModel))){
				devices.push(locations[i].wifiMorsels[j].deviceModel);
			}
			hotspottemp = locations[i].wifiMorsels[j].wifiMac;
			if(!(contains(hotspots,hotspottemp))){
				hotspots.push(hotspottemp);
			}
		}
	}
//	alert(devices.length+" of "+counter);
//	alert(hotspots.length);
	loadDevices();
	loadHotspots();
//	drawLocations();
}

function contains(a, obj) {
    var i = a.length;
    while (i--) {
       if (a[i] === obj) {
           return true;
       }
    }
    return false;
}

var hotSelect;

function loadHotspots(){
	hotSelect = document.getElementById("selectHotspot");
	if(hotSelect!=null){
		for ( var i = 0; i < hotspots.length; i++) {
			hotSelect.options[i] = new Option(getRoomByMac(hotspots[i]), i);		
		}
	}else{
		alert("not hotspot select");
	}
}

var devSelect;

function loadDevices(){
	devSelect = document.getElementById("selectDevice");
	for ( var i = 0; i < devices.length; i++) {
		devSelect.options[i] = new Option(devices[i], i);		
	}
}

function drawLocations(){
	
	//remove previous
	var locsToRemove = document.getElementsByClassName("locationCircleAnalize");
	while(locsToRemove[0]){
		locsToRemove[0].parentNode.removeChild(locsToRemove[0]);
	}
	
	
	var mapdiv = document.getElementById("map_png_div");
    var loc;
    
    var hotspot = hotSelect.options[hotSelect.selectedIndex].text;
    hotspot = getMacByRoom(hotspot);
    var device  = devSelect.options[devSelect.selectedIndex].text;
//    alert(hotspot+","+device);
    var green;
    var blue;
    var red = "FF";
	for ( var i = 0; i < locations.length; i++) {
		for ( var j = 0; j < locations[i].wifiMorsels.length; j++) {
			if(locations[i].wifiMorsels[j].deviceModel == device &&
					locations[i].wifiMorsels[j].wifiMac == hotspot){
				
				//DRAW
	            loc = document.createElement("div");
	            loc.className = "locationCircleAnalize";
	            loc.style.marginLeft = (locations[i].coordinateX-10) + "px";
	            loc.style.marginTop  = (locations[i].coordinateY-10) + "px";
	            
	            if(locations[i].wifiMorsels[j].wifiLevel<-50){
	            	green = "FF";
	            	if(locations[i].wifiMorsels[j].wifiLevel<-99){
	            		blue = "00";
	            	}else{
	            		blue = getColorByDB(locations[i].wifiMorsels[j].wifiLevel);
	            	}	            	
	            }else{
	            	blue = "00";
	            	if(locations[i].wifiMorsels[j].wifiLevel>-1){
	            		green = "00";
	            	}else{
	            		green = getColorByDB(locations[i].wifiMorsels[j].wifiLevel);	            		
	            	}
	            }
	            
	            loc.style.background = "#"+red+green+blue;
	            
	            mapdiv.appendChild(loc);
				
			}

		}
	}
	
}

function getRoomByMac(mac){
	if(mac == "00:aa:ab:02:32:06"){
		return "337";
	}else if(mac == "00:aa:ab:02:31:b0"){
		return "3303";
	}else if(mac == "00:aa:ab:02:30:cb"){
		return "340";
	}else if(mac == "00:aa:ab:02:2a:2b"){
		return "338";
	}else if(mac == "00:aa:ab:02:32:0a"){
		return "336";
	}else if(mac == "00:aa:ab:02:2d:69"){
		return "3305";
		
	}else if(mac == "20:cf:30:b7:e2:14"){
		return "332_h";
	}else if(mac == "00:23:cd:20:c9:0c"){
		return "331_h";
	}else if(mac == "00:1f:3f:68:03:69"){
		return "3303_h";
	}else if(mac == "ac:86:74:05:7c:ea"){
		return "340_h";
	}else if(mac == "ac:86:74:05:7e:7a"){
		return "3304_h";
	}else {
		return mac;
	}		
}

function getMacByRoom(room){
	if(room == "337"){
		return "00:aa:ab:02:32:06";
	}else if(room == "3303"){
		return "00:aa:ab:02:31:b0";
	}else if(room == "340"){
		return "00:aa:ab:02:30:cb";
	}else if(room == "338"){
		return "00:aa:ab:02:2a:2b";
	}else if(room == "336"){
		return "00:aa:ab:02:32:0a";
	}else if(room == "3305"){
		return "00:aa:ab:02:2d:69";
		
	}else if(room == "332_h"){
		return "20:cf:30:b7:e2:14";
	}else if(room == "331_h"){
		return "00:23:cd:20:c9:0c";
	}else if(room == "3303_h"){
		return "00:1f:3f:68:03:69";
	}else if(room == "340_h"){
		return "ac:86:74:05:7c:ea";
	}else if(room == "3304_h"){
		return "ac:86:74:05:7e:7a";
	}else {
		return room;
	}		
}

function getColorByDB(db){
	if(db==-37){
		Math.floor((((+db)/+100.0)+1.0) * +255).toString(16);
	}
	
	return Math.floor((((+db)/+100.0)+1.0) * +255).toString(16);
}