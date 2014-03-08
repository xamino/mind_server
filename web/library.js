/**
 * GENERAL LIBRARY, ONLY TOUCH IF YOU KNOW WHAT YOU'RE DOING!
 */

// Setup some general AJAX stuff (always executed)
$.ajaxSetup({
    url: '/Servlet',
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
}