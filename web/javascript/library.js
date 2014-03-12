/**
 * GENERAL LIBRARY, ONLY TOUCH IF YOU KNOW WHAT YOU'RE DOING!
 */

// Setup some general AJAX stuff (always executed)
$.ajaxSetup({
    url: 'Servlet',
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

/**
 * Function that checks whether two objects are of the same instance. ONLY FOR OBJECTS THAT DEFINE $type FIELD!
 * @param object_a
 * @param object_b
 */
function instanceOf(object_a, object_b) {
    // If null, might be class (if null, next step will return false)
    if (object_a.$type == undefined) {
        object_a = new object_a();
    }
    if (object_b.$type == undefined) {
        object_b = new object_b();
    }
    // Check if we can compare
    if (object_a.$type == undefined || object_b.$type == undefined)
        return false;
    // And now compare
    if (object_a.$type == object_b.$type)
        return true;
    return false;
}

/**
 * Function for reading parameters out of an URL. Returns an empty string if
 * none found. Credit: http://www.netlobo.com/url_query_string_javascript.html
 *
 * @param parameterName
 *            The name of the parameter tor read.
 * @returns The value of the parameter. Null if none found.
 */
function getURLParameter(parameterName) {
    parameterName = parameterName.replace(/[\[]/, "\\\[").replace(/[\]]/,
        "\\\]");
    var regexS = "[\\?&]" + parameterName + "=([^&#]*)";
    var regex = new RegExp(regexS);
    var results = regex.exec(window.location.href);
    if (results == null) {
        return "";
    } else {
        return results[1];
    }
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
    this.$type = "Location";
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