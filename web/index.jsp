<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>MIND | Test Page</title>
    <script language="JavaScript" src="http://code.jquery.com/jquery-2.1.0.js"></script>
    <script language="JavaScript" type="text/javascript" src="javascript/library.js"></script>
    <script language="JavaScript" type="text/javascript" src="javascript/run.js"></script>
    <script language="JavaScript" type="text/javascript" src="javascript/unittest.js"></script>
</head>
<body>
button - only till we know whats on index.jsp <br>
<input type="button" name="redirect" value="start mind" onclick="document.location.href='login.jsp'">
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
<input type="button" name="usertest" value="User Unit Test" onclick="userAccessTest();">
<br>
<input type="button" name="areatest" value="Area Unit Test" onclick="areaTest();">
<br>
<input type="button" name="positiontest" value="Position Unit Test" onclick="positionTest();">
<br>
<input type="button" name="displaytest" value="Public Display Test" onclick="displayAdminTest();">
<br>
<br>
<input type="button" name="cleandb" value="WARNING: Clean DB" onclick="cleanDB();alert('Done');">
</body>
</html>
