<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="게시판 관리" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container py-4">
  <h3 class="ow-section-title mb-4">게시판(카테고리) 관리</h3>

  <c:if test="${not empty error}">
    <div class="alert alert-danger">${error}</div>
  </c:if>

  <!-- 카테고리 목록 (각 카드 = 수정 + 삭제) -->
  <c:forEach var="cat" items="${categories}">
    <div class="card shadow-sm mb-2">
      <div class="card-body d-flex align-items-center gap-2">
        <form method="post" action="${pageContext.request.contextPath}/admin/categories/edit"
              class="row g-2 align-items-center flex-grow-1">
          <input type="hidden" name="no" value="${cat.no}">
          <div class="col-auto text-muted">#${cat.no}</div>
          <div class="col">
            <input name="name" value="${cat.name}" class="form-control form-control-sm" required>
          </div>
          <div class="col-auto">
            <select name="isAdminOnly" class="form-select form-select-sm">
              <option value="N" ${cat.isAdminOnly == 'N' ? 'selected' : ''}>일반</option>
              <option value="Y" ${cat.isAdminOnly == 'Y' ? 'selected' : ''}>관리자전용</option>
            </select>
          </div>
          <div class="col-auto">
            <input name="sortOrder" type="number" value="${cat.sortOrder}" class="form-control form-control-sm"
                   style="width:80px" title="정렬 순서">
          </div>
          <div class="col-auto">
            <button type="submit" class="btn btn-sm btn-warning">수정</button>
          </div>
        </form>
        <form method="post" action="${pageContext.request.contextPath}/admin/categories/delete"
              onsubmit="return confirm('삭제하시겠습니까?')">
          <input type="hidden" name="no" value="${cat.no}">
          <button type="submit" class="btn btn-sm btn-outline-danger">삭제</button>
        </form>
      </div>
    </div>
  </c:forEach>

  <!-- 카테고리 추가 -->
  <div class="card shadow-sm mt-3">
    <div class="card-header"><span class="ow-section-title">카테고리 추가</span></div>
    <div class="card-body">
      <form method="post" action="${pageContext.request.contextPath}/admin/categories/add"
            class="row g-2 align-items-center">
        <div class="col">
          <input name="name" class="form-control form-control-sm" placeholder="카테고리명" required>
        </div>
        <div class="col-auto">
          <select name="isAdminOnly" class="form-select form-select-sm">
            <option value="N">일반</option>
            <option value="Y">관리자전용</option>
          </select>
        </div>
        <div class="col-auto">
          <input name="sortOrder" type="number" value="0" class="form-control form-control-sm"
                 style="width:80px" title="정렬 순서">
        </div>
        <div class="col-auto">
          <button type="submit" class="btn btn-warning btn-sm">추가</button>
        </div>
      </form>
    </div>
  </div>

  <a href="${pageContext.request.contextPath}/admin" class="btn btn-outline-secondary btn-sm mt-3">대시보드</a>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
