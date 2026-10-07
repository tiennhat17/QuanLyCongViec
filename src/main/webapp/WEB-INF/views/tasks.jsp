<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Công việc</title>
</head>
<body>
    <h2>Công việc của bạn</h2>
    <form method="post" action="${pageContext.request.contextPath}/logout">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <button>Đăng xuất</button>
    </form>
</body>
</html>
