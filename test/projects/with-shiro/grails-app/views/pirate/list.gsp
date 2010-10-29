<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
	<head>
		<meta name="layout" content="main">
		<title><g:message code="pirate.list.title" default="List of Fearsome Pirates"/></title>
	</head>
	<body class="pirate">
		<section>
			<h1><g:message code="pirate.list.title" default="List of Fearsome Pirates"/></h1>
			<ul>
				<g:each var="pirate" in="${pirates}">
					<li>${pirate.name}</li>
				</g:each>
			</ul>
		</section>
	</body>
</html>