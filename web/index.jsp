<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>MIND | Test Page</title>
    <script language="JavaScript" type="text/javascript" src="jquery-2.1.0.min.js"></script>
    <script language="JavaScript" type="text/javascript" src="library.js"></script>
    <script language="JavaScript" type="text/javascript" src="run.js"></script>
</head>
<body>
button - only till we know whats on index.jsp <br>
<input type="button" name="redirect" value="start mind" onclick="document.location.href='admin_home.jsp'">
<br>
<br>
MIND test page for checking the API and as an example for later work on web interfaces.
<br>
<br>
JS now enabled, open your console!
<br>
<br>
JS Functions currently available: login(email, password), register(email, password, name), logout(), and send(data).
<br>
<br>
The session is saved in var session for your convenience, no need to copy & paste it. Also note that the JS consists of
three parts: the JQuery library (don't touch!), the library.js (only touch if you're Tamino :P ), and run.js, where all
the functional code should go.
</body>
</html>
