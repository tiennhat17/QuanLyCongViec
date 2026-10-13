<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <title>Công việc</title>
  <link rel="stylesheet" href="${ctx}/css/style.css">
  <link rel="stylesheet" href="${ctx}/css/task.css">
</head>

<body>
  <div class="app">
    <!-- SIDEBAR -->
    <aside class="sidebar">
      <div class="user" id="userMenuTrigger">
        <div class="avatar">
          <c:out value="${user.initials}"/>
        </div>
        <div class="user-info">
          <h3><c:out value="${user.username}"/></h3>
          <p><c:out value="${user.email}"/></p>
        </div>
        <span class="user-arrow">⌄</span>
        <div class="account-menu" id="accountMenu">
          <a href="${ctx}/account">Cập nhật hồ sơ</a>
          <form action="${pageContext.request.contextPath}/logout" method="post">
            <input type="hidden" name="${_csrf.parameterName}"
                  value="${_csrf.token}">

            <button type="submit" class="logout">
                Đăng xuất
            </button>
        </form>
        </div>
      </div>
  
      <input id="categorySearch" placeholder="Tìm danh mục...">
      <div class="section-title">
        Danh mục 
        <button id="addCategory" type="button">+</button>
      </div>
      <ul id="categoryList">
        <c:forEach var="c" items="${categories}">
          <li class="${c.id == curCat.id ? 'active' : ''}">
            <a class="category-name" href="${ctx}/tasks?cid=${c.id}"><c:out value="${c.name}"/></a>
            <div class="category-more"
                data-id="${c.id}"
                data-name="<c:out value='${c.name}'/>">
                ⋮
            </div>
          </li>
        </c:forEach>
      </ul>
    </aside>
    
    <!-- TASKS -->
    <main class="task-panel">
      <div class="header">
        <h1><c:out value="${empty curCat ? 'Chưa có danh mục' : curCat.name}"/></h1>
        <c:if test="${not empty curCat}">
          <button id="addTask" type="button">+ Thêm công việc</button>
        </c:if>
      </div>
      <c:if test="${not empty msg}">
        <div class="alert ok"><c:out value="${msg}"/></div>
      </c:if>
      <c:if test="${not empty error}">
        <div class="alert err"><c:out value="${error}"/></div>
      </c:if>
      <div class="alert">
        <c:choose>
          <c:when test="${dueToday > 0}">⚠ ${dueToday} công việc đến hạn hôm nay.</c:when>
          <c:otherwise>Không có công việc gấp.</c:otherwise>
        </c:choose>
      </div>
  
      <input id="taskSearch" placeholder="Tìm công việc...">
  
      <div id="taskList">
        <c:forEach var="t" items="${pending}">
          <div class="task ${t.id == selTask.id ? 'active' : ''}">
            <div class="task-left">
              <form:form method="post" action="${ctx}/tasks/${t.id}/toggle">
                <input type="hidden" name="cid" value="${curCat.id}"><input type="hidden" name="tid" value="${selTask.id}">
                <button class="task-check" title="Đánh dấu hoàn thành"></button>
              </form:form>
              <a class="task-info" href="${ctx}/tasks?cid=${curCat.id}&tid=${t.id}">
                <b>
                  <c:out value="${t.title}"/>
                </b>
                <small class="${t.overdue ? 'late' : ''}">
                  <c:out value="${t.dueLabel}"/>
                </small>
              </a>
            </div>
          </div>
        </c:forEach>
        <c:if test="${empty pending}">
          <p class="empty">Không có công việc nào đang chờ.</p>
        </c:if>
      </div>
  
      <div class="done-box">
        <h4>Completed</h4>
        <div id="completedList">
          <c:forEach var="t" items="${done}">
            <div class="task completed-task ${t.id == selTask.id ? 'active' : ''}">
              <div class="task-left">
                <form:form method="post" action="${ctx}/tasks/${t.id}/toggle">
                  <input type="hidden" name="cid" value="${curCat.id}">
                  <input type="hidden" name="tid" value="${selTask.id}">
                  <button class="task-check completed" title="Bỏ hoàn thành">✓</button>
                </form:form>
                <a class="task-info" href="${ctx}/tasks?cid=${curCat.id}&tid=${t.id}">
                  <b>
                    <c:out value="${t.title}"/>
                  </b>
                  <small>
                    <c:out value="${t.dueLabel}"/>
                  </small>
                </a>
              </div>
            </div>
          </c:forEach>
        </div>
      </div>
    </main>
  
    <!-- DETAIL -->
    <aside class="detail">
      <c:choose>
        <c:when test="${empty selTask}">
          <small>CHI TIẾT CÔNG VIỆC</small><h2>Chọn công việc</h2>
        </c:when>
        <c:otherwise>
          <div class="detail-top">
            <div>
              <small>CHI TIẾT CÔNG VIỆC</small>
              <h2><c:out value="${selTask.title}"/></h2>
            </div>
            <div class="actions">
              <button 
                type="button" id="editTask" 
                data-id="${selTask.id}" data-title="<c:out value='${selTask.title}'/>"
                data-desc="<c:out value='${selTask.description}'/>" 
                data-due="${selTask.dueDate}" 
                data-cat="${selTask.category.id}"
              >
                Sửa
              </button>
              <form:form
                method="post" 
                action="${ctx}/tasks/${selTask.id}/delete" 
                style="margin:0" 
                onsubmit="return confirm('Xóa công việc này?')"
              >
                <input type="hidden" name="cid" value="${curCat.id}">
                <button class="danger">Xóa</button>
              </form:form>
            </div>
          </div>
          <div class="tags">
            <span><c:out value="${selTask.completed ? 'Đã hoàn thành' : 'Đang thực hiện'}"/></span>
            <c:if test="${selTask.overdue}">
              <span style="background:#442126">Quá hạn</span>
            </c:if>
          </div>
          <div class="info">
            <label>Mô tả</label>
            <p style="white-space:pre-wrap">
              <c:out value="${empty selTask.description ? 'Chưa có mô tả' : selTask.description}"/>
            </p>
          </div>
          <div class="info">
            <label>Danh mục</label>
            <p><c:out value="${selTask.category.name}"/></p>
          </div>
          <div class="info">
            <label>Ngày đến hạn</label>
            <p>
              <c:out value="${selTask.dueLabel}"/>
              <c:if test="${not empty selTask.dueDate}">
                (${selTask.dueDate})
              </c:if>
            </p>
          </div>
          <form:form method="post" action="${ctx}/tasks/${selTask.id}/toggle">
            <input type="hidden" name="cid" value="${curCat.id}">
            <input type="hidden" name="tid" value="${selTask.id}">
            <button class="complete-btn">
              <c:out value="${selTask.completed ? 'Đánh dấu chưa hoàn thành' : 'Đánh dấu hoàn thành'}"/>
            </button>
          </form:form>
        </c:otherwise>
      </c:choose>
    </aside>
  
    <!-- Modal danh mục (thêm + sửa) -->
    <div
        id="catModal"
        class="modal ${openCatModal ? '' : 'hidden'}"
    >
        <form:form
            id="catForm"
            method="post"
            action="${ctx}/categories"
            modelAttribute="categoryForm"
            cssClass="modal-content"
        >
            <h3>Thêm danh mục</h3>

            <form:hidden path="id" id="cId"/>
  
            <div class="mb-3">
  
                <label for="catName" class="f">Tên danh mục</label>
                <form:input
                    id="catName"
                    path="name"
                    placeholder="Tên danh mục"
                    cssClass="form-control"
                    cssErrorClass="form-control is-invalid"
                    autofocus="autofocus"
                />
                <form:errors path="name" cssClass="invalid-feedback"/>
            </div>
  
            <div class="modal-actions">
                <button type="button" class="secondary" data-close>Hủy</button>
                <button type="submit" class="primary"> Lưu </button>
            </div>
        </form:form>
  
    </div>
  
    <!-- Modal công việc (thêm + sửa) -->
    <div id="taskModal" class="modal ${openTaskModal ? '' : 'hidden'}">
        <form:form
            id="taskForm"
            class="modal-content"
            method="post"
            action="${ctx}/tasks"
            modelAttribute="taskForm"
        >
            <h3 id="taskModalTitle">Thêm công việc</h3>
  
            <form:hidden path="id" id="tId"/>
            <form:hidden path="categoryId" value="${curCat.id}"/>
  
            <div class="mb-3">
                <label for="tTitle" class="f"> Tiêu đề</label>
  
                <form:input
                    path="title"
                    id="tTitle"
                    cssClass="form-control"
                    cssErrorClass="form-control is-invalid"
                    placeholder="Tên công việc"
                />
  
                <form:errors path="title" cssClass="invalid-feedback"/>
            </div>
  
  
            <div class="mb-3">
  
                <label for="tDesc" class="f">Mô tả</label>
  
                <form:textarea
                    path="description"
                    id="tDesc"
                    cssClass="form-control"
                    rows="4"
                />
  
                <form:errors path="description" cssClass="invalid-feedback"/>
            </div>
  
            <div class="mb-3">
                <label for="tDue" class="f">Ngày đến hạn</label>
  
                <form:input
                    type="date"
                    path="dueDate"
                    id="tDue"
                    cssClass="form-control"
                />
  
                <form:errors path="dueDate" cssClass="invalid-feedback"/>
            </div>
  
  
            <div class="modal-actions">
                <button type="button" class="secondary" data-close>Hủy</button>
                <button type="submit" class="primary">Lưu</button>
            </div>
  
        </form:form>
  
    </div>
    
    <form:form id="delCatForm" method="post" cssStyle="display:none"></form:form>
  </div>




  <script>const ctx='${ctx}';</script>
  <script src="${ctx}/js/app.js"></script>
  <script src="${ctx}/js/task.js"></script>
</body>
</html>
