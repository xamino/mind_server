<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <script src="javascript/jquery-2.1.0.min.js"></script>
<script src="javascript/library.js"></script>
<script src="javascript/run.js"></script>
<title>Remove User</title>
</head>
<body>

<form id="removeUserForm">
	 Do you want to remove the User Nr. <%= request.getParameter("id") %> (Name: xxx)?<br>
    <button type="button" name="back" onclick="window.close()">Back</button> 
    <input type="submit" value="Remove User"/>
</form>

</body>
</html>