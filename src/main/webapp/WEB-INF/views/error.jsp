<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Lỗi <c:out value="${status}"/> – Personal Task Manager</title>
    <link rel="stylesheet" href="${ctx}/css/style.css">
    <style>
        .err-code { font-size: 72px; font-weight: 800; line-height: 1; margin: 0 0 8px; text-align: center; }
        .err-card { text-align: center; }
        .err-card .trace { color: #888; font-size: 12px; margin-top: 16px; }
        .err-card a.primary { display: inline-block; text-decoration: none; margin-top: 16px; }
    </style>
</head>
<body class="scroll">
    <div class="auth">
        <div class="card err-card">
            <div class="err-code"><c:out value="${empty status ? 500 : status}"/></div>
            <h2>
                <c:choose>
                    <c:when test="${status == 404}">Không tìm thấy trang</c:when>
                    <c:when test="${status == 403}">Không có quyền truy cập</c:when>
                    <c:when test="${status == 400}">Yêu cầu không hợp lệ</c:when>
                    <c:when test="${status == 409}">Dữ liệu xung đột</c:when>
                    <c:otherwise>Đã xảy ra lỗi</c:otherwise>
                </c:choose>
            </h2>
            <p class="sub"><c:out value="${message}"/></p>
            <a class="primary" href="${ctx}/">Về trang chính</a>
            <c:if test="${not empty traceId}">
                <p class="trace">Mã lỗi: <c:out value="${traceId}"/></p>
            </c:if>
        </div>
    </div>
</body>
</html>
