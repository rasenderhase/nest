<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
	<div id="head" style="height: 60px; border-bottom: solid 1px rgb(204, 204, 204);">
       <div style="border-right: solid 1px rgb(204, 204, 204); float: left; height: 100%; position: absolute; width: 130px;">
       </div>
		<h3 style="margin: 0px;">${pageContext.servletContext.servletContextName}</h3>
		<c:if test="${not empty pageContext.request.userPrincipal}">
			<a style="float: right;" href="${pageContext.request.contextPath}/rest/logout">Logout ${pageContext.request.userPrincipal.name}</a>
		</c:if>
	</div>

	<div id="content" style="">
		<div id="navigation" style="width: 130px; position: absolute">Navigation</div>
		<div style="margin: 0px 0px 0px 130px; border-left: solid 1px rgb(204, 204, 204);">
			<jsp:include page="${_bodyJsp}"></jsp:include>
		</div>
	</div>