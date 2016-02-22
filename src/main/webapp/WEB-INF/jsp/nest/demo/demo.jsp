<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

	<fieldset style="margin: 5px 0px 15px 0px;">
		<legend>Ihre Rollen:</legend>
		<ul>
		<c:if test="${pageContext.request.isUserInRole('admin')}">
			<li>Administrator</li>
		</c:if>
		<c:if test="${pageContext.request.isUserInRole('info')}">
			<li>Informatiker</li>
		</c:if>
		</ul>
	</fieldset>
	
	aktuelle Zeit: <span id="time">${it.datetime}</span>

	<table class="display" style="width: 100%;">
		<thead>
			<tr>
				<th>Header 1</th>
				<th>Header 2</th>
				<th>Header 3</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach begin="1" end="100" varStatus="status">
				<tr>
					<td>Cell 1 - ${status.index}</td>
					<td>Cell 2 - ${status.index}</td>
					<td>Cell 3 - ${status.index}</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>