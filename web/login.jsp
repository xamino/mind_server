<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <script src="javascript/jquery-2.1.0.min.js"></script>
    <script src="javascript/library.js"></script>
    <script src="javascript/run.js"></script>
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet" type="text/css">
    <title>Login</title>
</head>

<body>
<div id="container">
    <div id="banner">
        <!-- <img src="someImage.jpg" alt="banner" width="100%" height="100px"/> <!-- TODO: Banner-Image -->
        <h2>MIND</h2>
    </div>
    <script type="text/javascript">
    function callPopup(url){
    	newwindow = window.open(url,'','toolbar=0,location=0,directories=0,menubar=0,scrollbars=1,resizable=0,width=640,height=480, top=300,left=500');
    	//if (window.focus) {newwindow.focus();}
        //if (!newwindow.closed) {newwindow.focus();}
		return false;
    }
	</script>
    <div id="content">
		<input type="submit" value="Open Popup" onclick="javascript:callPopup('public_display_popup_call.jsp')"/>
	    <div id="text-content">
            You have to be logged in to work with mind.
        </div>
        <div id="login">
            <form id="loginForm">
                <table>
                    <tr>
                        <td>E-Mail:</td>
                        <td><input name="email" id="email" size=40 type="text"/></td>
                    </tr>
                    <tr>
                        <td>Password:</td>
                        <td><input name="password" id="password" size=20 type="password"/></td>
                    </tr>
                </table>
                <input type="submit" value="Login"/>
            </form>
            <a href="registration.jsp">Registration</a>
        </div>

    </div>

    <div id="footer">
        <!-- TODO: nice Footer -->
    </div>
</div>
</body>
</html>