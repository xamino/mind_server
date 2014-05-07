
/****************** Admin - Display Management *****************/

/**
 * loads all displays on load of page admin_public_displays.jsp
 */
function loadDisplays() {
    var displays = new PublicDisplay(null, null, null, 0, 0);
//	var users = null;
    doTask("DISPLAY_READ", displays, writeDisplays);
}

function writeDisplays(data) {
    if (data.object.length == 0) {
        var noDisplayInDatabase = "There are currently no displays in the database.<br> Use the button 'Add Display' to add displays to the system.";
        document.getElementById("table_space").innerHTML = noDisplayInDatabase;
    }
    else {
        var tablecontents = "";
        tablecontents = "<table border ='1'>";
        tablecontents += "<tr>";
        tablecontents += "<td>Display Identification: </td>";
        tablecontents += "<td>Display Location: </td>";
        tablecontents += "<td>Display X-Coordinate: </td>";
        tablecontents += "<td>Display Y-Coordinate: </td>";
        tablecontents += "<td>Edit Display: </td>";
        tablecontents += "<td>Remove Display: </td>";
        tablecontents += "</tr>";

        for (var i = 0; i < data.object.length; i++) {
            tablecontents += "<tr>";
            tablecontents += "<td>" + data.object[i].identification + "</td>";
            tablecontents += "<td>" + data.object[i].location + "</td>";
            tablecontents += "<td>" + data.object[i].coordinateX + "</td>";
            tablecontents += "<td>" + data.object[i].coordinateY + "</td>";
            tablecontents += "<td><input type='submit' value='Edit Display' onClick='editDisplayViaPopup(" + JSON.stringify(data.object[i]) + ")'/></td>";
            tablecontents += "<td><input type='submit' value='Remove Display' onClick='removeDisplayViaPopup(" + JSON.stringify(data.object[i]) + ")'/></td>";
            tablecontents += "</tr>";
        }
        tablecontents += "</table>";
        document.getElementById("table_space").innerHTML = tablecontents;

    }

}

/**
 * on Button click 'Add Display' in admin_public_displays.jsp
 * adds a new display with the given identification, password, location, x-coordinate and y-coordinate
 */
function addDisplayViaPopup() {

    var identification = prompt("Please enter the identification of the display you want to add:");

    if (identification != null) {	// if Cancel Button isn't clicked

        var password = prompt("Please enter the password of the display you want to add:");

        if (password != null) {	// if Cancel Button isn't clicked

            var location = prompt("Please enter the location of the display you want to add:");

            if (location != null) {	// if Cancel Button isn't clicked

                var xCoordinate = prompt("Please enter the x-coordinate of the display you want to add:");

                if (xCoordinate != null) {	// if Cancel Button isn't clicked

                    var yCoordinate = prompt("Please enter the y-coordinate of the display you want to add:");

                    if (yCoordinate != null) {	// if Cancel Button isn't clicked

                        if (identification != "" && location != "" && xCoordinate != "" && yCoordinate != "") {	//everything is given
                            newDisplay = new PublicDisplay(identification, password, location, xCoordinate, yCoordinate);
                            doTask("DISPLAY_ADD", newDisplay, function (data) {
                                if (password == "" || password == null) {
                                    alert("The following display has been added:\n" +
                                        "Identification: " + identification + "\n" +
                                        "Location: " + location + "\n" +
                                        "Generated Password: " + data.object.description + "\n" +
                                        "X-Coordinate: " + xCoordinate + "\n" +
                                        "Y-Coordinate: " + yCoordinate);
                                } else {
                                    alert("The following display has been added:\n" +
                                        "Identification: " + identification + "\n" +
                                        "Location: " + location + "\n" +
                                        "Password: " + password + "\n" +
                                        "X-Coordinate: " + xCoordinate + "\n" +
                                        "Y-Coordinate: " + yCoordinate);
                                }

                                window.location.reload();
                            });

                        }

                    }
                }

            }

        }
    }
}


/**
 * on Button click 'Edit Display' in admin_public_displays.jsp
 * edits the selected display
 */
function editDisplayViaPopup(data) {

    //token == password
    var password = prompt("EDIT PASSWORD - If you want a new password, simply enter the new password. If you don't want to change anything, leave it empty.");

    if (password != null) {	// if Cancel Button isn't clicked

        var location = prompt("EDIT LOCATION - If you want to change the location: '" + data.location + "' simply enter the new location. If you don't want to change anything, leave it empty.");

        if (location != null) {	// if Cancel Button isn't clicked

            var xCoordinate = prompt("EDIT X-COORDINATE - If you want to change the x-coordinate: '" + data.coordinateX + "' simply enter the new x-coordinate. If you don't want to change anything, leave it empty.");

            if (xCoordinate != null) {	// if Cancel Button isn't clicked

                var yCoordinate = prompt("EDIT Y-COORDINATE - If you want to change the y-coordinate: '" + data.coordinateY + "' simply enter the new y-coordinate. If you don't want to change anything, leave it empty.");

                if (yCoordinate != null) {	// if Cancel Button isn't clicked

                    //nothing has been changed
                    if (password == "" && location == "" && xCoordinate == "" && yCoordinate == "") {
                        alert("You didn't change anything.");
                    }
                    //something has been changed
                    else {
                        var newIdentification = data.identification, newPassword = data.token, newLocation = data.location, newX = data.coordinateX, newY = data.coordinateY;

                        if (password != "") {
                            newPassword = password;
                        }
                        if (location != "") {
                            newLocation = location;
                        }
                        if (xCoordinate != "") {
                            newX = xCoordinate;
                        }
                        if (yCoordinate != "") {
                            newY = yCoordinate;
                        }

                        var updateDisplay = new PublicDisplay(newIdentification, newPassword, newLocation, newX, newY);
                        alert("DISPLAY: Id - " + newIdentification + ", pas - " + newPassword + ", loc - " + newLocation + ", X - " + newX + ", Y - " + newY);
                        doTask("DISPLAY_UPDATE", updateDisplay, function (event) {
                            alert("The display (name: " + data.identification + ") has been modified.");
                            window.location.reload();
                        });
                    }
                }
            }

        }
    }


}

/**
 * on Button click 'Remove Display' in admin_public_displays.jsp
 * deletes the display
 */
function removeDisplayViaPopup(data) {
    var r = confirm("Do you want to remove the display '" + data.identification + "' ?");
    if (r == true) {
        var displaytodelete = new PublicDisplay(data.identification, null, null, 0, 0);
        doTask("DISPLAY_REMOVE", displaytodelete, function (event) {
            alert("The following user has been deleted:\n" +
                "Identification: " + data.identification +
                "\nLocation: " + data.location +
                "\nX-Coordinate: " + data.coordinateX +
                "\nY-Coordinate: " + data.coordinateY);
            window.location.reload();

        });
    }
}