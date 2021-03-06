<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <script src="javascript/jquery-2.1.0.min.js" type="text/javascript"></script>
    <script src="javascript/library.js" type="text/javascript"></script>
    <script src="javascript/run.js" type="text/javascript"></script>
    <script src="javascript/pd/displayMap.js" type="text/javascript"></script>
    <script src="javascript/pd/pd_general.js" type="text/javascript"></script>
    <script src="javascript/pd/polling.js" type="text/javascript"></script>
    <!-- <script type="text/javascript">
	    $(document).ready(function() {
	    	document.getElementById('identification').value = 'pd';
	    	document.getElementById('password').value = 'pd';
	    });
	</script>-->
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
                        <td><input name="password" id="password" size=20 type="password"/></td>
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