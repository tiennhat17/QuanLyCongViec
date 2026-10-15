<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Thống kê công việc</title>
    <link rel="stylesheet" href="${ctx}/css/style.css">
    <link rel="stylesheet" href="${ctx}/css/dashboard.css">
</head>
<body class="scroll dashboard-page">
<main class="dashboard-shell">
    <header class="dashboard-header">
        <div>
            <p class="dashboard-eyebrow">TASK MANAGER</p>
            <h1>Thống kê công việc</h1>
            <p class="dashboard-subtitle">Tổng quan tiến độ công việc của bạn.</p>
        </div>
        <div class="dashboard-user">
            <span class="avatar"><c:out value="${user.initials}"/></span>
            <div>
                <strong><c:out value="${user.username}"/></strong>
                <a href="${ctx}/account">Tài khoản cá nhân</a>
            </div>
            <form method="post" action="${ctx}/logout">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button type="submit">Đăng xuất</button>
            </form>
        </div>
    </header>

    <section class="dashboard-grid" aria-label="Thống kê công việc">
        <article class="dashboard-stat total-stat">
            <span class="dashboard-stat-label">Tổng số công việc</span>
            <strong class="dashboard-stat-value"><c:out value="${statistics.totalTasks}"/></strong>
            <span class="dashboard-stat-note">Tất cả công việc của bạn</span>
        </article>
        <article class="dashboard-stat completed-stat">
            <span class="dashboard-stat-label">Đã hoàn thành</span>
            <strong class="dashboard-stat-value"><c:out value="${statistics.completedTasks}"/></strong>
            <span class="dashboard-stat-note">Công việc đã hoàn tất</span>
        </article>
        <article class="dashboard-stat pending-stat">
            <span class="dashboard-stat-label">Chưa hoàn thành</span>
            <strong class="dashboard-stat-value"><c:out value="${statistics.pendingTasks}"/></strong>
            <span class="dashboard-stat-note">Công việc đang chờ xử lý</span>
        </article>
        <article class="dashboard-stat overdue-stat">
            <span class="dashboard-stat-label">Quá hạn</span>
            <strong class="dashboard-stat-value"><c:out value="${statistics.overdueTasks}"/></strong>
            <span class="dashboard-stat-note">Chưa hoàn thành và quá ngày hạn</span>
        </article>
        <article class="dashboard-stat today-stat">
            <span class="dashboard-stat-label">Đến hạn hôm nay</span>
            <strong class="dashboard-stat-value"><c:out value="${statistics.dueTodayTasks}"/></strong>
            <span class="dashboard-stat-note">Công việc cần xử lý trong hôm nay</span>
        </article>
    </section>

    <a class="dashboard-tasks-link" href="${ctx}/tasks">Xem và quản lý công việc <span aria-hidden="true">→</span></a>
</main>
</body>
</html>
