/****************Admin - Sensor Management****************/


/**
 * loads all sensors on load of page admin_sensor_management.jsp
 */
function loadSensors() {
    var sensors = new WifiSensor(null, null, null);
    doTask("SENSOR_READ", sensors, writeSensors);
}

function writeSensors(data) {
    if (data.object.length == 0) {
        var noSensorsInDatabase = "There are currently no sensors in the database.<br> Use the button 'Add Sensors' to add sensors to the system.";
        document.getElementById("table_space").innerHTML = noSensorsInDatabase;
    }
    else {
        var tablecontents = "";
        tablecontents = "<table border ='1'>";
        tablecontents += "<tr>";
        tablecontents += "<td>WifiSensor - ID: </td>";
        tablecontents += "<td>Area: </td>";
        tablecontents += "<td>tokenHash: </td>";
        //tablecontents += "<td>Edit Sensor: </td>";
        tablecontents += "<td>Remove Sensor: </td>";
        tablecontents += "</tr>";

        for (var i = 0; i < data.object.length; i++) {
        	alert(i);
            tablecontents += "<tr>";
            tablecontents += "<td>" + data.object[i].identification + "</td>";
            tablecontents += "<td>" + data.object[i].area + "</td>";
            tablecontents += "<td>" + data.object[i].tokenHash + "</td>";
            //tablecontents += "<td><input type='submit' value='Edit User' onClick='javascript:popupOpen_editUser(this.id)' id='editUser" +i+ "'/></td>";
            //tablecontents += "<td><input type='submit' value='Remove User' onClick='javascript:popupOpen_removeUser(this.id)' id='removeUser" +i+ "'/></td>";
            //tablecontents += "<td><input type='submit' value='Edit User' onClick='editUserViaPopup(" + JSON.stringify(data.object[i]) + ")'/></td>";
            tablecontents += "<td><input type='submit' value='Remove Sensor Data' onClick='removeSensorViaPopup(" + JSON.stringify(data.object[i]) + ")'/></td>";
            tablecontents += "</tr>";
        }
        tablecontents += "</table>";
        document.getElementById("table_space").innerHTML = tablecontents;

    }

}

/**
 * on Button click 'Add Sensor' in admin_sensor_management.jsp
 * adds a new sensor with the given identification, area and popup
 */
function addSensorViaPopup() {


    var identification = prompt("Please enter the identification of the sensor you want to add:");

    if (identification != null) {	// if Cancel Button isn't clicked

        var area = prompt("Please enter the area of the sensor you want to add:");

        if (area != null) {	// if Cancel Button isn't clicked

            var tokenHash = prompt("Please enter the password of the sensor you want to add:");

            //	if(password != null){	// if Cancel Button isn't clicked

            if (identification != "" && area != "" /*&& password != ""*/) {	//everything is given
                var newSensor = new WifiSensor(identification, tokenHash, area);
                doTask("SENSOR_ADD", newSensor, function (data) {
                    if (tokenHash == "" || tokenHash == null) {
                        alert("The following sensor has been added:\n" +
                            "Identification: " + identification + "\n" +
                            "Area: " + area + "\n" +
                            "Generated Password: " + data.object.description);
                    } else {
                        alert("The following sensor has been added:\n" +
                            "Identification: " + identification + "\n" +
                            "Area: " + area + "\n" +
                            "Password: " + tokenHash);
                    }
                    window.location.reload();
                });

            }

        }
    }
}


/**
 * Creates a popup, enabling the admin to delete the sensor
 * @param data
 * the sensor data that can be deleted (JSON.stringified)
 */
function removeSensorViaPopup(data) {
    var r = confirm("Do you want to remove the sensor with the identification'" + data.identification + "' ?");
    if (r == true) {
        var sensortodelete = new WifiSensor(data.identification, null, null);
        doTask("SENSOR_REMOVE", sensortodelete, function (event) {
            alert("The following sensor has been deleted:\n" +
                "Identification: " + data.identification +
                "\nArea: " + data.area);
            window.location.reload();

        });
    }
}


if($('#settingsBrightness').is(':visible')) {
    alert("da");
}
