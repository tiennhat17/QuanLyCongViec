<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>403 – Personal Task Manager</title>
    <link rel="stylesheet" href="${ctx}/css/style.css">
    <style>
        .err-code { font-size: 72px; font-weight: 800; line-height: 1; margin: 0 0 8px; text-align: center; }
        .err-card { text-align: center; }
        .err-card a.primary { display: inline-block; text-decoration: none; margin-top: 16px; }
    </style>
</head>
<body class="scroll">
    <div class="auth">
        <div class="card err-card">
            <div class="err-code">403</div>
            <h2>Không có quyền truy cập</h2>
            <p class="sub">Bạn không có quyền truy cập trang này.</p>
            <a class="primary" href="${ctx}/">Về trang chính</a>
        </div>
    </div>
</body>
</html>
