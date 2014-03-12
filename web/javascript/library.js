/**
 * GENERAL LIBRARY, ONLY TOUCH IF YOU KNOW WHAT YOU'RE DOING!
 */

// Setup some general AJAX stuff (always executed)
$.ajaxSetup({
    url: '/Servlet/bla',
    type: 'POST',
    contentType: 'application/json; charset=utf-8',
    dataType: 'json'
});

/**
 * Helper method for sending data. Just give it a JSON object (mostly always Arrival) and, if desired, a callback
 * function that looks so: function(data) {...}.
 * @param data
 * @param callback
 */
function send(data, callback) {
    $.ajax({data: JSON.stringify(data)}).done(callback);
}

// OBJECT DEFINITIONS HERE –––––––––––––––––––––––––––––––––––––––––––––––––––––––––––-

function Arrival(task, sessionHash, object) {
    this.$type = "Arrival";
    this.sessionHash = sessionHash;
    this.task = task;
    this.object = object;
}

function User(email, password, name) {
    this.$type = "User";
    this.email = email;
    this.pwdHash = password;
    this.name = name;
    // this.admin = admin; // can not be changed by unauthorized clients, don't try :P
}

function Success(name, description) {
    this.$type = "Success";
    this.name = name;
    this.description = description;
}

function Error(name, description) {
    this.$type = "Error";
    this.name = name;
    this.description = description;
}

function Area(ID, locations, topLeftX, topLeftY, width, height) {
    this.$type = "Area";
    this.ID = ID;
    this.locations = locations;
    this.topLeftX = topLeftX;
    this.topLeftY = topLeftY;
    this.width = width;
    this.height = height;
}

function Location(coordinateX, coordinateY, wifiMorsels) {
    this.$type = "Location"
    this.coordinateX = coordinateX;
    this.coordinateY = coordinateY;
    this.wifiMorsels = wifiMorsels;
}

function WifiMorsel(wifiMac, wifiName, wifiLevel) {
    this.$type = "WifiMorsel";
    this.wifiMac = wifiMac;
    this.wifiName = wifiName;
    this.wifiLevel = wifiLevel;
}