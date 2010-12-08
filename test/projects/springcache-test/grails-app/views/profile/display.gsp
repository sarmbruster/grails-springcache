<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head>
	  <title>Profile: ${userInstance.name}</title>
	  <meta name="layout" content="main">
  </head>
  <body>
	  <header>
		  <h1>${userInstance.name}</h1>
	  </header>
	  <dl>
		  <dt>Username</dt><dd>${userInstance.username}</dd>
	  </dl>
  </body>
</html>