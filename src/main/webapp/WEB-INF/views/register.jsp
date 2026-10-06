<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Đăng ký – Personal Task Manager</title><link rel="stylesheet" href="${ctx}/css/style.css"></head>
<body class="scroll"><div class="auth"><form class="card" method="post" action="${ctx}/register">
  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
  <input type="hidden" name="clientTimezoneOffset" id="clientTimezoneOffset" value="0">
  <h2>Tạo tài khoản</h2><p class="sub">Bắt đầu sắp xếp công việc của bạn</p>
  <c:if test="${not empty error}"><div class="alert err"><c:out value="${error}"/></div></c:if>
  <label class="f">Tên đăng nhập</label>
  <input name="username" maxlength="50" value="<c:out value='${username}'/>" required autofocus>
  <label class="f">Email</label>
  <input type="email" name="email" maxlength="100" value="<c:out value='${email}'/>" required>
  <label class="f">Mật khẩu (tối thiểu 6 ký tự)</label>
  <input type="password" name="password" minlength="6" required>
  <label class="f">Nhập lại mật khẩu</label>
  <input type="password" name="confirm" required>
  <button class="primary">Đăng ký</button>
  <p class="sw">Đã có tài khoản? <a href="${ctx}/login">Đăng nhập</a></p>
</form></div><script src="${ctx}/js/app.js"></script></body></html>
