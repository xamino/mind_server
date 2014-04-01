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
    $.ajax({data: JSON.stringify(data)}).done(function (data) {
        // Check for logout
        var test = data.object;
        if (test.$type == "Error" && test.type == "SECURITY" && test.description == "Session invalid!") {
            window.location = "index.jsp";
        }
        callback(data);
    });
}

/**
 * Function that checks whether two objects are of the same instance. ONLY FOR OBJECTS THAT DEFINE $type FIELD!
 * @param object_a
 * @param object_b
 */
function instanceOf(object_a, object_b) {
    // null returns false
    if (object_a == undefined || object_b == undefined)
        return false;
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

/**
 * Writes a Cookie - credit to http://stackoverflow.com/questions/2257631/how-create-a-session-using-javascript
 * @param name The name of the cookie (set in isAdmin)
 * @param value The value (session)
 */
function writeCookie(name, value) {
    var date, expires;
//    if (days) {
    date = new Date();
    date.setTime(date.getTime() + (60 * 60 * 1000));
    expires = "; expires=" + date.toGMTString();
//            }else{
//        expires = "";
//    }
    document.cookie = name + "=" + value + expires + "; path=/";
}

/**
 * returns a Cookie corresponding to the forwarded parameter 'name'
 * credit to http://stackoverflow.com/questions/2257631/how-create-a-session-using-javascript
 */
function readCookie(name) {
    var i, c, ca, nameEQ = name + "=";
    ca = document.cookie.split(';');
    for (i = 0; i < ca.length; i++) {
        c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1, c.length);
        }
        if (c.indexOf(nameEQ) == 0) {
            return c.substring(nameEQ.length, c.length);
        }
    }
    return '';
}

/**
 * deletes the cookie with the given name
 */
function deleteCookie(name) {
    writeCookie(name, "");
}

// OBJECT DEFINITIONS HERE –––––––––––––––––––––––––––––––––––––––––––––––––––––––––––-

function Arrival(task, sessionHash, object) {
    this.$type = "Arrival";
    this.sessionHash = sessionHash;
    this.task = task;
    this.object = object;
}

function User(email, password, name, admin) {
    this.$type = "User";
    this.email = email;
    this.pwdHash = password;
    this.name = name;
    this.admin = admin; // can not be changed by unauthorized clients, don't try :P
}

function Success(type, description) {
    this.$type = "Success";
    this.type = type;
    this.description = description;
}

function Error(type, description) {
    this.$type = "Error";
    this.type = type;
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

function PublicDisplay(identification, token, location, coordinateX, coordinateY) {
    this.$type = "PublicDisplay";
    this.identification = identification;
    this.token = token;
    this.location = location;
    this.coordinateX = coordinateX;
    this.coordinateY = coordinateY;
}
