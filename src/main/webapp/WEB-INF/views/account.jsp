<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Tài khoản cá nhân</title>
    <link rel="stylesheet" href="${ctx}/css/style.css">
</head>
<body class="scroll account-page">
<main class="account-shell">
    <a class="account-back" href="${ctx}${homeUrl}">← Trở về</a>
    <header class="account-heading">
        <h1>Tài khoản cá nhân</h1>
        <p>Cập nhật hồ sơ và bảo mật tài khoản</p>
    </header>
    <c:if test="${not empty msg}"><div class="alert ok"><c:out value="${msg}"/></div></c:if>
    <c:if test="${not empty error}"><div class="alert err"><c:out value="${error}"/></div></c:if>
    <div class="account-layout">
        <section class="card account-card">
            <h2>Thông tin cá nhân</h2>
            <p class="sub">Tên đăng nhập và email của bạn</p>
            <form method="post" action="${ctx}/account/profile">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <label class="f" for="username">Tên đăng nhập</label>
                <input id="username" name="username" maxlength="50" value="<c:out value='${account.username}'/>" required>
                <label class="f" for="email">Email</label>
                <input id="email" type="email" name="email" maxlength="100" value="<c:out value='${account.email}'/>" required>
                <button class="primary" type="submit">Lưu thông tin</button>
            </form>
        </section>
        <section class="card account-card">
            <h2>Đổi mật khẩu</h2>
            <p class="sub">Mật khẩu mới phải có ít nhất 6 ký tự</p>
            <form method="post" action="${ctx}/account/password">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <label class="f" for="currentPassword">Mật khẩu hiện tại</label>
                <input id="currentPassword" type="password" name="currentPassword" autocomplete="current-password" required>
                <label class="f" for="newPassword">Mật khẩu mới</label>
                <input id="newPassword" type="password" name="newPassword" minlength="6" autocomplete="new-password" required>
                <label class="f" for="confirmPassword">Xác nhận mật khẩu mới</label>
                <input id="confirmPassword" type="password" name="confirmPassword" minlength="6" autocomplete="new-password" required>
                <button class="primary" type="submit">Đổi mật khẩu</button>
            </form>
        </section>
    </div>
</main>
</body>
</html>