<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Willkommen</title>
	<script>
	// you can register settings like this before require.js is loaded
	var require = {
	    baseUrl : '${pageContext.request.contextPath}/scripts/nest',
	    paths : {
	        // the left side is the module ID,
	        // the right side is the path to
	        // the jQuery file, relative to baseUrl.
	        // Also, the path should NOT include
	        // the '.js' file extension. This example
	        // is using jQuery 1.9.0 located at
	        // js/lib/jquery-1.9.0.js, relative to
	        // the HTML page.
	        jquery : 'jquery-2.2.0',
	        "jquery-ui" : 'jquery-ui-1.11.4.custom/jquery-ui',
	        "datatables.net" : 'DataTables/DataTables-1.10.11/js/jquery.dataTables',
	        "datatables.net.jquery-ui" : 'DataTables/DataTables-1.10.11/js/dataTables.jqueryui',
	        app : '../app'
	    }
	};
	</script>
	<c:if test="${empty _mainScript}">
		<%-- wenn kein Skript angegeben ist, auf jeden Fall nest laden --%>
		<c:set var="_mainScript" value="nest" />
	</c:if>
	<script data-main="${_mainScript}" src="${pageContext.request.contextPath}/scripts/nest/require.js"></script>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/scripts/nest/site.css"/>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/scripts/nest/jquery-ui-1.11.4.custom/jquery-ui.css"/>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/scripts/nest/DataTables/DataTables-1.10.11/css/dataTables.jqueryui.css"/>
</head>
<body style="margin: 0px; display: none">
	<jsp:include page="layout.jsp"></jsp:include>
</body>
	<c:if test="${_mainScript eq 'nest'}">
	<script>
		require(["nest"], function(nest) {
			nest.fouc();
		});
	</script>
	</c:if>
</html>