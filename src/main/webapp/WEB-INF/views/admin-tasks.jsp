<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Thống kê công việc</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="admin-page">
    <main class="admin-shell">
        <aside class="admin-sidebar">
            <div class="admin-brand">
                <span class="admin-brand-mark">Q</span>
                <div>
                    <strong>Quản trị</strong>
                    <small>Task Manager</small>
                </div>
            </div>
            <nav class="admin-menu" aria-label="Điều hướng quản trị">
                <a class="admin-menu-item" href="${pageContext.request.contextPath}/admin">
                    <span>▦</span>
                    Dashboard
                </a>
                <a class="admin-menu-item" href="${pageContext.request.contextPath}/admin/users">
                    <span>♙</span>
                    Quản trị người dùng
                </a>
                <a class="admin-menu-item active" href="${pageContext.request.contextPath}/admin/tasks">
                    <span>✓</span>
                    Thống kê công việc
                </a>
            </nav>
            <form class="admin-logout" method="post" action="${pageContext.request.contextPath}/logout">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button type="submit">↪ Đăng xuất</button>
            </form>
        </aside>
        <div class="admin-content">
            <header class="admin-header">
                <div>
                    <h1>Thống kê công việc</h1>
                    <p>Tổng quan công việc và danh mục trong toàn hệ thống</p>
                </div>
            </header>
            <section class="admin-grid" aria-label="Thống kê công việc và danh mục">
                <div class="admin-stat total">
                    <span class="admin-stat-label">Tổng số công việc</span>
                    <span class="admin-stat-value">${statistics.totalTasks}</span>
                </div>
                <div class="admin-stat active">
                    <span class="admin-stat-label">Đã hoàn thành</span>
                    <span class="admin-stat-value">${statistics.completedTasks}</span>
                </div>
                <div class="admin-stat locked">
                    <span class="admin-stat-label">Đang chờ làm</span>
                    <span class="admin-stat-value">${statistics.pendingTasks}</span>
                </div>
                <div class="admin-stat locked">
                    <span class="admin-stat-label">Đang bị trễ hạn</span>
                    <span class="admin-stat-value">${statistics.overdueTasks}</span>
                </div>
                <div class="admin-stat active">
                    <span class="admin-stat-label">Tỷ lệ hoàn thành</span>
                    <span class="admin-stat-value">
                        <fmt:formatNumber value="${statistics.completionRate()}" maxFractionDigits="1"/>%
                    </span>
                </div>
                <div class="admin-stat total">
                    <span class="admin-stat-label">Tổng số danh mục</span>
                    <span class="admin-stat-value">${statistics.totalCategories}</span>
                </div>
            </section>
        </div>
    </main>
    <script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>
