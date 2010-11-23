<html>
	<head>
		<title><g:layoutTitle default="Grails"/></title>
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}"/>
		<link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}" type="image/x-icon"/>
		<g:layoutHead/>
		<g:javascript library="application"/>
	</head>
	<body>
		<div id="spinner" class="spinner" style="display:none;">
			<img src="${resource(dir: 'images', file: 'spinner.gif')}" alt="Spinner"/>
		</div>
		<div id="grailsLogo" class="logo"><a href="http://grails.org"><img src="${resource(dir: 'images', file: 'grails_logo.png')}" alt="Grails" border="0"/></a></div>
		<shiro:isLoggedIn>
			<div id="loggedInUser"><g:message code="auth.loggedInAs" args="[shiro.principal()]" default="Logged in as {0}"/></div>
		</shiro:isLoggedIn>
		<shiro:isNotLoggedIn>
			<div id="loginLink"><g:link controller="login"><g:message code="default.login.label" default="Login here"/></g:link></div>
		</shiro:isNotLoggedIn>
		<g:layoutBody/>
	</body>
</html>