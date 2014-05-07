<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>MIND | Test Page</title>
    <script language="JavaScript" src="javascript/jquery-2.1.0.min.js"></script>
    <script language="JavaScript" type="text/javascript" src="javascript/library.js"></script>
    <script language="JavaScript" type="text/javascript" src="javascript/run.js"></script>
    <script language="JavaScript" type="text/javascript" src="javascript/unittest.js"></script>
    <script src="javascript/admin/admin_display_management.js"></script>
	<script src="javascript/admin/admin_map_location.js"></script>
	<script src="javascript/admin/admin_sensor_management.js"></script>
	<script src="javascript/admin/admin_user_management.js"></script>
	<script src="javascript/pd/displayMap.js"></script>
	<script src="javascript/pd/pd_general.js"></script>
</head>
<body>
button - only till we know whats on index.jsp <br>
<input type="button" name="redirect" value="i am a person" onclick="document.location.href='login.jsp'">
<br>
<input type="button" name="redirect" value="i am a public display"
       onclick="document.location.href='public_display_login.jsp'">
<br>
<br>
MIND test page for checking the API and as an example for later work on web interfaces.
<br>
<br>
JS now enabled, open your console!
<br>
<br>
Server IP address is <span id="ip"></span>.
<script language="JavaScript">
    var ip = location.host;
    document.getElementById("ip").innerHTML = ip;
</script>
<br>
<br>
Currently there are <b><span id="sessions">?</span></b> sessions active (without this script).
<input type="button" value="Update" onclick="updateSessions();">
<span id="lastSessionUpdate">Never</span>
<script language="JavaScript">
    var alsdkjf = "";
    function updateSessions() {
        if (alsdkjf == "") {
            alsdkjf = getAdminSession();
        }
        send(new Arrival("admin_read_sessions", alsdkjf, null), function (data) {
            var number = data.object.length - 1;
            document.getElementById("sessions").innerHTML = number;
            var date = new Date();
            document.getElementById("lastSessionUpdate").innerHTML = date.getHours() + ":" + date.getMinutes();
        });
    }
</script>
<br>
<br>
The session is saved in var session for your convenience, no need to copy & paste it. Also note that the JS consists of
three parts: the JQuery library (don't touch!), the library.js (only touch if you're Tamino :P ), and run.js, where all
the functional code should go.
<br>
<br>
Click here to run a unit test of the API. WARNING: while the test is running, the webpage will be unresponsive!
<br>
<input type="button" name="unittest" value="Run API Unit Test" onclick="doUnitTest();">
<br>
<br>
<input type="button" name="admintest" value="Admin Unit Test" onclick="adminRightsTest();">
<br>
<input type="button" name="userupdate" value="User Update Unit Test" onclick="userUpdateTest();">
<br>
<input type="button" name="useraccess" value="User Access Unit Test" onclick="userAccessTest();">
<br>
<input type="button" name="areatest" value="Area Unit Test" onclick="areaTest();">
<br>
<input type="button" name="positiontest" value="Position Unit Test" onclick="positionTest();">
<br>
<input type="button" name="positionreadtest" value="Position Read Unit Test" onclick="testPositionRead();">
<br>
<input type="button" name="statustest" value="Status Unit Test" onclick="statusTest();">
<br>
<input type="button" name="admindisplaytest" value="PD Admin Test" onclick="displayAdminTest();">
<br>
<input type="button" name="displayusertest" value="PD User Test" onclick="displayUserTest();">
<br>
<input type="button" name="sensortest" value="WifiSensor Test" onclick="wifiSensorAPITest();">
<br>
<input type="button" name="fusiontest" value="SensorAlgoFusion Test" onclick="sensorAlgoFusionTest();">
<br>
<br>
<input type="button" name="cleandb" value="WARNING: Clean DB" onclick="cleanDB();alert('Done');">
<br>
<br>
<br>
<br>
<br>
Test user icon upload:
<form action="upload/icon" method="post" enctype="multipart/form-data">
    Email: <input type="email" name="email"/><br>
    Password: <input type="password" name="password"/> <br>
    <input type="file" name="file" size="50"/><br>
    <input type="submit" value="Upload File"/>
</form>
</body>
</html>
