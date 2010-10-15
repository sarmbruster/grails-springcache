<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head>
	  <title>Profile: ${userInstance.userRealName}</title>
	  <meta name="layout" content="main">
  </head>
  <body>
	  <header>
		  <h1>${userInstance.userRealName}</h1>
	  </header>
	  <dl>
		  <dt>Username</dt><dd>${userInstance.username}</dd>
		  <dt>Email</dt><dd>${userInstance.email}</dd>
	  </dl>
  </body>
</html>