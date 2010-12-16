<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head>
	  <title>Edit Profile: ${userInstance.name}</title>
	  <meta name="layout" content="main">
  </head>
  <body>
	  <header>
		  <h1>Edit Your Profile</h1>
	  </header>
	  <g:form action="update">
		  <ul>
			  <li><label for="username">Username</label><g:textField name="username" value="${fieldValue(bean: userInstance, field: 'username')}"/></li>
			  <li><label for="password">Password</label><g:passwordField name="password"/></li>
			  <li><label for="name">Name</label><g:textField name="name" value="${fieldValue(bean: userInstance, field: 'name')}"/></li>
		  </ul>
		  <fieldset class="buttons">
			  <button type="submit">Update</button>
		  </fieldset>
	  </g:form>
  </body>
</html>