<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Đăng nhập – Personal Task Manager</title><link rel="stylesheet" href="${ctx}/css/style.css"></head>
<body class="scroll"><div class="auth"><form class="card" method="post" action="${ctx}/login">
  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
  <h2>Đăng nhập</h2><p class="sub">Quản lý công việc cá nhân</p>
  <c:if test="${not empty msg}"><div class="alert ok"><c:out value="${msg}"/></div></c:if>
  <c:if test="${not empty error}"><div class="alert err"><c:out value="${error}"/></div></c:if>
  <label class="f">Tên đăng nhập hoặc email</label>
  <input name="identity" value="<c:out value='${identity}'/>" required autofocus>
  <label class="f">Mật khẩu</label>
  <input type="password" name="password" required>
  <button class="primary">Đăng nhập</button>
  <p class="sw">Chưa có tài khoản? <a href="${ctx}/register">Đăng ký</a></p>
</form></div></body></html>
