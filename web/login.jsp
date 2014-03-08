<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Login</title>
</head>

<body>
<!-- TODO: CSS muss ich noch machen -->
<div id="banner">
<img src="someImage.jpg" alt="banner" width="100%" height="100px"/> <!-- TODO: Banner-Image -->
<h2>MIND</h2>
</div>

<div id="content">
<div id="text-content">
You have to be logged in to work with mind.
</div>
<div id="login">
	<form action="Servlet" method="POST">
       <table>
          <tr>
              <td> Username  : </td><td> <input name="username" size=20 type="text" /> </td> 
          </tr>
              <tr>
          <td> Password  : </td><td> <input name="password" size=20 type="text" /> </td> 
              </tr>
       </table>
       <input type="submit" value="Login" />
	</form>
<a href="registration.jsp">Registration</a>
</div>

</div>

<div id="footer">
<!-- TODO: nice Footer -->
</div>
</body>
</html>