<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <script src="javascript/jquery-2.1.0.min.js"></script>
    <script src="javascript/library.js"></script>
    <script src="javascript/run.js"></script>
    <title>Call</title>
</head>
<body style="background:#CCCCCC;">
<!-- close popup if it's not focused -->
<script language="javascript" type="text/javascript">
window.onblur = function(){window.close();};
</script>

<form id="addUserForm">
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
            <td><input name="password" id="password" size=40 type="text"/></td>
        </tr>
    </table>
    <input type="button" name="back" value="Cancel Call" onclick="window.close()"/>
    <input type="submit" value="Add User"/>
</form>

</body>
</html>