<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="관리자 대시보드" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container py-4">
  <h3 class="ow-section-title mb-4">관리자 대시보드</h3>

  <div class="row g-3 mb-4">
    <div class="col-md-4">
      <div class="card shadow-sm">
        <div class="card-body text-center">
          <div class="text-muted small">전체 회원</div>
          <div class="ow-head text-warning" style="font-size:2rem">${memberCount}</div>
        </div>
      </div>
    </div>
    <div class="col-md-4">
      <div class="card shadow-sm">
        <div class="card-body text-center">
          <div class="text-muted small">전체 게시글</div>
          <div class="ow-head text-warning" style="font-size:2rem">${postCount}</div>
        </div>
      </div>
    </div>
    <div class="col-md-4">
      <div class="card shadow-sm">
        <div class="card-body text-center">
          <div class="text-muted small">카테고리</div>
          <div class="ow-head text-warning" style="font-size:2rem">${categoryCount}</div>
        </div>
      </div>
    </div>
  </div>

  <div class="d-flex gap-2">
    <a href="${pageContext.request.contextPath}/admin/members" class="btn btn-warning">회원 관리</a>
    <a href="${pageContext.request.contextPath}/admin/categories" class="btn btn-warning">게시판 관리</a>
    <a href="${pageContext.request.contextPath}/admin/reports" class="btn btn-warning">신고 처리</a>
    <a href="${pageContext.request.contextPath}/admin/stats" class="btn btn-warning">방문자 통계</a>
  </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
