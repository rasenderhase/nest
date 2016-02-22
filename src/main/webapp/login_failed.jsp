<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set scope="session" var="loginFailed" value="${true}"></c:set>
<c:redirect url="/rest/login" />