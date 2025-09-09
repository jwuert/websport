<html>
<%@ page session="true"%>
<head>
<meta http-equiv="refresh" content="0; url=../sport/editor.html" />
</head>
<body>
	<p style="text-align:center">
		The user '<%=request.getRemoteUser()%>' has been logged out.
		<br/>
		<a href="../sport/editor.html">back</a>
	</p>
	<%
		session.invalidate();
	%>

</body>
</html>