/**
 * Get session
 */
var session = session = readCookie("MIND_Admin_C_session");

// register handlers
$(document).ready(function () {
    // check
    check();
    // register button
    $('#update').click(build);
    $('#logout').click(private_logout);
});

/**
 * Checks if session is valid and builds page.
 */
function check() {
    send(new Arrival("check", session), function (data) {
        if (instanceOf(data.object, Error)) {
            window.location.href = "/login.jsp";
        } else {
            build();
        }
    });
}

/**
 * Builds all tables new.
 */
function build() {
    send(new Arrival("admin_user_read", session, new User()), function (data) {
        b_table('users', data.object);
    });
    send(new Arrival("display_read", session, new PublicDisplay()), function (data) {
        b_table('displays', data.object);
    });
    send(new Arrival("poll_read", session, new Poll()), function (data) {
        b_table('polls', data.object);
    });
    send(new Arrival("sensor_read", session, new WifiSensor()), function (data) {
        b_table('sensors', data.object);
    });
    // note that we compact this one (we'll get the morsels later):
    send(new Arrival("area_read", session, new Area(), true), function (data) {
        b_table('areas', data.object);
    });
    send(new Arrival("location_read", session, new Location()), function (data) {
        b_table('locations', data.object);
    });
    send(new Arrival("area_read", session, new Area("University")), function (data) {
        var locs = data.object[0].locations;
        var morsels = [];
        $.each(locs, function (index, item) {
            $.each(item.wifiMorsels, function (i, mor) {
                morsels.push(mor);
            });
        });
        b_table('morsels', morsels);
        $('#morsel_amount').text(morsels.length);
    });
}

function b_table(table_name, array) {
    var tableObject = document.getElementById(table_name);
    var properties = new Reflector(array[0]).getProperties();
    // filter
    var reflect = FilterProperties(properties);
    // build header
    var table = '<tr>';
    $.each(reflect, function (index, item) {
        table += '<th>' + item + '</th>';
    });
    table += '</tr>';
    // build body
    $.each(array, function (index, object) {
        table += '<tr>';
        $.each(reflect, function (index, field) {
            // if array just count, don't put them all out
            if ($.isArray(object[field])) {
                table += '<td title="' + Formulate(object[field]) + '">' + object[field].length + '* </td>';
            } else {
                table += '<td>' + object[field] + '</td>';
            }
        });
        table += '</tr>';
    });
    tableObject.innerHTML = table;
}

function FilterProperties(properties) {
    var reflect = [];
    $.each(properties, function (index, item) {
        if (item != '$type' && item != 'pwdHash' && item != 'token' && item != 'tokenHash') {
            reflect.push(item);
        }
    });
    return reflect;
}

/**
 * Watch out, this is an object!
 * @param obj
 * @constructor
 */
var Reflector = function (obj) {
    this.getProperties = function () {
        var properties = [];
        for (var prop in obj) {
            if (typeof obj[prop] != 'function') {
                properties.push(prop);
            }
        }
        return properties;
    };
};

/**
 * @return {string}
 */
function Formulate(array) {
    var answer = "Subobjects:\n";
    // for each object
    $.each(array, function (index2, object) {
        // for each reflector
        var properties = new Reflector(object).getProperties();
        var reflect = FilterProperties(properties);
        var second = true;
        $.each(reflect, function (index, field) {
            if (second) {
                second = false;
            } else {
                answer += ", ";
            }
            if ($.isArray(object[field])) {
                answer += reflect[index] + "=" + object[field].length + "*";
            } else {
                answer += reflect[index] + "=" + object[field];
            }
        });
        answer += '\n';
    });
    return answer;
}

function private_logout() {
    // remove session
    deleteCookie("MIND_Admin_C_session");
    send(new Arrival("logout", session, null), function (data) {
        window.location = "/login.jsp";
    });
}
