<!doctype html>
<html>
	<head>
		<title>Resources test</title>
		<meta name="layout" content="resources">
		<r:use module="jquery"/>
	</head>
	<body>
		<div>This text should be red if resources have loaded!</div>
		<r:script>
			$('body > div').css('color', '#f00');
			$('body').append($('<div id="added-by-jquery">O HAI</div>'));
		</r:script>
	</body>
</html>