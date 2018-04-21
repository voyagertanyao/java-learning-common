<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page import="org.springframework.web.context.WebApplicationContext" %>
<%@page import="com.cmcciot.platform.hoapi.auth.service.AuthUserService" %>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<html>
	<body>
		<%
			WebApplicationContext wac = WebApplicationContextUtils
				.getWebApplicationContext(request.getSession(true).getServletContext());
			AuthUserService service = (AuthUserService)wac.getBean("authUserService");
			String userid = service.findUserByUsername("admin").getUserid();
			response.getWriter().println("{userID:"+userid+"}");
			response.getWriter().flush();
		%>
	</body>
</html>