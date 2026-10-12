<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Lỗi</title>
</head>
<body>
    <h2>Lỗi <c:out value="${status}"/></h2>
    <p><c:out value="${message}"/></p>
    <a href="${pageContext.request.contextPath}/">Về trang chính</a>
</body>
</html>
