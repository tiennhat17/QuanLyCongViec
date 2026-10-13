<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quản lý người dùng</title>
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
                <a class="admin-menu-item active" href="${pageContext.request.contextPath}/admin/users">
                    <span>♙</span>
                    Quản trị người dùng
                </a>
                <span class="admin-menu-item disabled" aria-disabled="true">
                    <span>✓</span>
                    Quản trị công việc
                    <small>Sắp có</small>
                </span>
            </nav>
            <form class="admin-logout" method="post" action="${pageContext.request.contextPath}/logout">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button type="submit">↪ Đăng xuất</button>
            </form>
        </aside>
        <div class="admin-content">
            <header class="admin-header">
                <div>
                    <h1>Quản lý người dùng</h1>
                    <p>Theo dõi và quản lý tài khoản trong hệ thống</p>
                </div>
            </header>
            <section class="admin-section">
                <c:if test="${not empty msg}">
                    <p class="admin-alert ok"><c:out value="${msg}"/></p>
                </c:if>
                <c:if test="${not empty error}">
                    <p class="admin-alert err"><c:out value="${error}"/></p>
                </c:if>

                <form class="admin-toolbar" method="get" action="${pageContext.request.contextPath}/admin/users">
                    <div class="admin-field">
                        <label for="search">Tìm theo tên đăng nhập hoặc email</label>
                        <input id="search" name="search" value="<c:out value='${search}'/>"
                               placeholder="Nhập tên hoặc email">
                    </div>
                    <button class="admin-primary admin-action" type="submit">Tìm kiếm</button>
                    <c:if test="${not empty search}">
                        <a class="admin-link" href="${pageContext.request.contextPath}/admin/users">Xóa lọc</a>
                    </c:if>
                </form>

                <div class="admin-table-wrap">
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th>Tên đăng nhập</th>
                                <th>Email</th>
                                <th>Vai trò</th>
                                <th>Trạng thái</th>
                                <th>Số lượng công việc</th>
                                <th>Ngày đăng ký</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty users}">
                                    <tr>
                                        <td colspan="7">Không tìm thấy người dùng phù hợp.</td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="user" items="${users.content}">
                                        <tr>
                                            <td><c:out value="${user.username}"/></td>
                                            <td><c:out value="${user.email}"/></td>
                                            <td><c:out value="${user.role}"/></td>
                                            <td class="${user.status ? 'active' : 'locked'}">
                                                ${user.status ? 'Đang hoạt động' : 'Đã bị khóa'}
                                            </td>
                                            <td class="admin-muted">Chưa có module Task</td>
                                            <td><c:out value="${user.formattedCreatedAt}"/></td>
                                            <td>
                                                <form method="post" action="${pageContext.request.contextPath}/admin/users/${user.id}/status">
                                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                                    <input type="hidden" name="enabled" value="${user.status ? 'false' : 'true'}">
                                                    <input type="hidden" name="search" value="<c:out value='${search}'/>">
                                                    <input type="hidden" name="page" value="${page}">
                                                    <button class="admin-action ${user.status ? 'admin-danger' : 'admin-success'}"
                                                            type="submit">
                                                        ${user.status ? 'Khóa tài khoản' : 'Mở khóa'}
                                                    </button>
                                                </form>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
                <c:if test="${users.totalPages > 1}">
                    <nav class="admin-pagination" aria-label="Phân trang danh sách người dùng">
                        <c:if test="${users.hasPrevious()}">
                            <a class="admin-link"
                               href="${pageContext.request.contextPath}/admin/users?page=${page - 1}&amp;search=<c:out value='${search}'/>">
                                Trang trước
                            </a>
                        </c:if>
                        <span>Trang ${page + 1} / ${users.totalPages}</span>
                        <c:if test="${users.hasNext()}">
                            <a class="admin-link"
                               href="${pageContext.request.contextPath}/admin/users?page=${page + 1}&amp;search=<c:out value='${search}'/>">
                                Trang sau
                            </a>
                        </c:if>
                    </nav>
                </c:if>
            </section>
        </div>
    </main>
    <script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>
