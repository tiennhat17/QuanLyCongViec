<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Dashboard quản trị</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .summary-list {
            line-height: 1.8;
            margin: 0;
            padding-left: 22px;
            color: #f3f4f6;
        }
        .summary-list li { margin-bottom: 4px; }
        .summary-list strong { color: #ffffff; }
    </style>
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
                <a class="admin-menu-item active" href="${pageContext.request.contextPath}/admin">
                    <span>▦</span>
                    Dashboard
                </a>
                <a class="admin-menu-item" href="${pageContext.request.contextPath}/admin/users">
                    <span>♙</span>
                    Quản trị người dùng
                </a>
                <a class="admin-menu-item" href="${pageContext.request.contextPath}/admin/tasks">
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
                    <h1>Dashboard quản trị</h1>
                    <p>Tổng quan hoạt động tài khoản</p>
                </div>
            </header>
            <section class="admin-grid" aria-label="Thống kê hệ thống">
                <div class="admin-stat total">
                    <span class="admin-stat-label">Tổng số tài khoản</span>
                    <span class="admin-stat-value">${dashboard.totalUsers}</span>
                </div>
                <div class="admin-stat active">
                    <span class="admin-stat-label">Đang hoạt động</span>
                    <span class="admin-stat-value">${dashboard.activeUsers}</span>
                </div>
                <div class="admin-stat locked">
                    <span class="admin-stat-label">Đã bị khóa</span>
                    <span class="admin-stat-value">${dashboard.lockedUsers}</span>
                </div>
                <div class="admin-stat total">
                    <span class="admin-stat-label">Tổng số công việc</span>
                    <span class="admin-stat-value">${dashboard.totalTasks}</span>
                </div>
                <div class="admin-stat active">
                    <span class="admin-stat-label">Đã hoàn thành</span>
                    <span class="admin-stat-value">${dashboard.completedTasks}</span>
                </div>
                <div class="admin-stat locked">
                    <span class="admin-stat-label">Đang chờ làm</span>
                    <span class="admin-stat-value">${dashboard.pendingTasks}</span>
                </div>
                <div class="admin-stat locked">
                    <span class="admin-stat-label">Đang bị trễ hạn</span>
                    <span class="admin-stat-value">${dashboard.overdueTasks}</span>
                </div>
                <div class="admin-stat active">
                    <span class="admin-stat-label">Tỷ lệ hoàn thành</span>
                    <span class="admin-stat-value">
                        <fmt:formatNumber value="${dashboard.completionRate}" maxFractionDigits="1"/>%
                    </span>
                </div>
                <div class="admin-stat total">
                    <span class="admin-stat-label">Tổng số danh mục</span>
                    <span class="admin-stat-value">${dashboard.totalCategories}</span>
                </div>
            </section>

            <%-- ========== Tóm tắt nhanh (chuyển từ /admin/categories) ========== --%>
            <section class="admin-section" aria-label="Tóm tắt nhanh">
                <h2>Tóm tắt nhanh</h2>
                <ul class="summary-list">
                    <li>
                        Hệ thống có
                        <strong>${dashboard.totalCategories}</strong> danh mục
                        thuộc <strong>${dashboard.uniqueCategoryOwners}</strong> người dùng.
                    </li>
                    <li>
                        Tỷ lệ hoàn thành toàn hệ thống:
                        <strong><fmt:formatNumber value="${dashboard.completionRate}" maxFractionDigits="1"/>%</strong>
                        (đã hoàn thành ${dashboard.completedTasks} trên tổng ${dashboard.totalTasks} task).
                    </li>
                    <li>
                        <c:choose>
                            <c:when test="${dashboard.overdueTasks == 0}">
                                Không có task nào đang bị trễ hạn.
                            </c:when>
                            <c:otherwise>
                                Đang có <strong>${dashboard.overdueTasks}</strong> task bị trễ hạn cần xử lý.
                            </c:otherwise>
                        </c:choose>
                    </li>
                </ul>
            </section>

            <section class="admin-section">
                <h2>Người dùng đăng ký gần đây</h2>
                <div class="admin-table-wrap">
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th>Tên đăng nhập</th>
                                <th>Email</th>
                                <th>Vai trò</th>
                                <th>Ngày đăng ký</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty dashboard.recentUsers}">
                                    <tr>
                                        <td colspan="4" class="admin-muted">Chưa có người dùng.</td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="user" items="${dashboard.recentUsers}">
                                        <tr>
                                            <td><c:out value="${user.username}"/></td>
                                            <td><c:out value="${user.email}"/></td>
                                            <td><c:out value="${user.role}"/></td>
                                            <td><c:out value="${user.formattedCreatedAt}"/></td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
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
