<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <script language="JavaScript" src="http://code.jquery.com/jquery-2.1.0.js"></script>
    <script src="javascript/library.js"></script>
    <script src="javascript/run.js"></script>
    <title>Login</title>
</head>

<body>
<!-- TODO: CSS muss ich noch machen -->
<div id="banner">
    <!-- <img src="someImage.jpg" alt="banner" width="100%" height="100px"/> <!-- TODO: Banner-Image -->
    <h2>MIND</h2>
</div>

<div id="content">
    <div id="text-content">
        Here you can register to work with MIND.<br>
        Enter your name, email and your password (twice).
    </div>
    <div id="register">
        <form id="registerForm">
            <table>
            	<tr>
                    <td> Name :</td>
                    <td><input name="name" id="name" size=40 type="text"/></td>
                </tr>
                <tr>
                    <td> E-Mail :</td>
                    <td><input name="email" id="email" size=40 type="text"/></td>
                </tr>
                <tr>
                    <td> Password :</td>
                    <td><input name="password" id="password" size=20 type="text"/></td>
                </tr>
                <tr>
                    <td> Repeat password :</td>
                    <td><input name="password2" id="password2" size=20 type="text"/></td>
                </tr>
            </table>
            <input type="button" value="Back"/>
            <input type="submit" value="Register"/>
        </form>
    </div>

</div>

<div id="footer">
    <!-- TODO: nice Footer -->
</div>
</body>
</html>