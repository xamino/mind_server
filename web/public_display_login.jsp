<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"><script language="JavaScript" src="http://code.jquery.com/jquery-2.1.0.js"></script>
    <script src="javascript/library.js"></script>
    <script src="javascript/run.js"></script>
	<link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet" type="text/css">
    <title>Login your Public Display</title>
</head>

<body>
<div id="container">
<div id="banner">
    <!-- <img src="someImage.jpg" alt="banner" width="100%" height="100px"/> <!-- TODO: Banner-Image -->
    <h2>MIND</h2>
</div>

<div id="content">
    <div id="text-content">
        Log in the public display.
    </div>
    <div id="login">
        <form id="loginDisplayForm">
            <table>
                <tr>
                    <td>Identification:</td>
                    <td><input name="identification" id="identification" size=40 type="text"/></td>
                </tr>
                <tr>
                    <td>Password:</td>
                    <td><input name="password" id="password" size=20 type="text"/></td>
                </tr>
            </table>
            <input type="submit" value="Login"/>
        </form>
    </div>

</div>

<div id="footer">
    <!-- TODO: nice Footer -->
</div>
</div>
</body>
</html>