<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Dashboard quản trị</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="admin-page">
<main class="admin-shell">
    <aside class="admin-sidebar">
        <div class="admin-brand"><span class="admin-brand-mark">Q</span><div><strong>Quản trị</strong><small>Task Manager</small></div></div>
        <nav class="admin-menu" aria-label="Điều hướng quản trị">
            <a class="admin-menu-item active" href="${pageContext.request.contextPath}/admin"><span>▦</span>Dashboard</a>
            <a class="admin-menu-item" href="${pageContext.request.contextPath}/admin/users"><span>♙</span>Quản trị người dùng</a>
            <span class="admin-menu-item disabled" aria-disabled="true"><span>✓</span>Quản trị công việc<small>Sắp có</small></span>
        </nav>
        <form class="admin-logout" method="post" action="${pageContext.request.contextPath}/logout">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <button type="submit">↪ Đăng xuất</button>
        </form>
    </aside>
    <div class="admin-content">
        <header class="admin-header">
            <div><h1>Dashboard quản trị</h1><p>Tổng quan hoạt động tài khoản</p></div>
        </header>
    <section class="admin-grid" aria-label="Thống kê người dùng">
        <div class="admin-stat total"><span class="admin-stat-label">Tổng số tài khoản</span><span class="admin-stat-value">${statistics.totalUsers}</span></div>
        <div class="admin-stat active"><span class="admin-stat-label">Đang hoạt động</span><span class="admin-stat-value">${statistics.activeUsers}</span></div>
        <div class="admin-stat locked"><span class="admin-stat-label">Đã bị khóa</span><span class="admin-stat-value">${statistics.lockedUsers}</span></div>
    </section>
    <section class="admin-section">
        <h2>Người dùng đăng ký gần đây</h2>
        <div class="admin-table-wrap">
        <table class="admin-table">
            <thead><tr><th>Tên đăng nhập</th><th>Email</th><th>Vai trò</th><th>Ngày đăng ký</th></tr></thead>
            <tbody>
            <c:choose>
                <c:when test="${empty statistics.recentUsers}"><tr><td colspan="4" class="admin-muted">Chưa có người dùng.</td></tr></c:when>
                <c:otherwise><c:forEach var="user" items="${statistics.recentUsers}">
                    <tr><td><c:out value="${user.username}"/></td><td><c:out value="${user.email}"/></td><td><c:out value="${user.role}"/></td><td><c:out value="${user.formattedCreatedAt}"/></td></tr>
                </c:forEach></c:otherwise>
            </c:choose>
            </tbody>
        </table>
        </div>
        </section>
    </div>
</main>
<script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>
