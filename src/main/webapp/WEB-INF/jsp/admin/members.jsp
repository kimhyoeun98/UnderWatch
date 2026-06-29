<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="회원 관리" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container py-4">
  <h3 class="ow-section-title mb-4">회원 관리</h3>

  <c:if test="${not empty error}">
    <div class="alert alert-danger">${error}</div>
  </c:if>

  <div class="card shadow-sm">
    <div class="card-body p-0">
      <table class="table table-hover align-middle mb-0">
        <thead>
          <tr>
            <th>아이디</th><th>닉네임</th><th>권한</th><th>상태</th>
            <th style="width:100px">가입일</th><th style="width:230px">관리</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="m" items="${members}">
            <tr>
              <td>${m.id}</td>
              <td><c:out value="${m.nickname}"/></td>
              <td><span class="badge ${m.role == 'ROLE_ADMIN' ? 'bg-warning' : 'bg-secondary'}">${m.role}</span></td>
              <td><span class="badge ${m.status == 'ACTIVE' ? 'bg-secondary' : 'bg-warning'}">${m.status}</span></td>
              <td class="text-muted small">${m.regDate}</td>
              <td>
                <form method="post" action="${pageContext.request.contextPath}/admin/members/status" style="display:inline">
                  <input type="hidden" name="id" value="${m.id}">
                  <input type="hidden" name="status" value="${m.status == 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE'}">
                  <button type="submit" class="btn btn-sm ${m.status == 'ACTIVE' ? 'btn-outline-danger' : 'btn-outline-secondary'}">
                    ${m.status == 'ACTIVE' ? '정지' : '정지해제'}
                  </button>
                </form>
                <form method="post" action="${pageContext.request.contextPath}/admin/members/role" style="display:inline">
                  <input type="hidden" name="id" value="${m.id}">
                  <input type="hidden" name="role" value="${m.role == 'ROLE_ADMIN' ? 'ROLE_USER' : 'ROLE_ADMIN'}">
                  <button type="submit" class="btn btn-sm btn-outline-secondary">
                    ${m.role == 'ROLE_ADMIN' ? '관리자해제' : '관리자지정'}
                  </button>
                </form>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </div>

  <a href="${pageContext.request.contextPath}/admin" class="btn btn-outline-secondary btn-sm mt-3">대시보드</a>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
