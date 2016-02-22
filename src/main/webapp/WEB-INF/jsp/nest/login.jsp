<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

	<form method="POST" action="${pageContext.request.contextPath}/j_security_check">
		<table>
			<tr>
				<td colspan="2">Login</td>
			</tr>
			<tr>
				<td>Name:</td>
				<td><input type="text" name="j_username" /></td>
			</tr>
			<tr>
				<td>Passwort:</td>
				<td><input type="password" name="j_password" /></td>
			</tr>
			<tr style="height: 2em;">
				<td colspan="2" style="color: red;">
					<c:if test="${loginFailed}">
					Name oder Passwort unbekannt
					</c:if>
				</td>
			</tr>
			<tr>
				<td colspan="2"><input type="submit" value="Go" /></td>
			</tr>
		</table>
	</form>
	<c:set scope="session" var="loginFailed" value="${false}"></c:set>