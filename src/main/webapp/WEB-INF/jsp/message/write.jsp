<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="쪽지 쓰기" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container py-4">
  <div class="row justify-content-center">
    <div class="col-md-7">
      <div class="card shadow-sm">
        <div class="card-header"><span class="ow-section-title">쪽지 쓰기</span></div>
        <div class="card-body">
          <c:if test="${not empty error}"><div class="alert alert-danger">${error}</div></c:if>
          <form method="post" action="${pageContext.request.contextPath}/message/write">
            <div class="mb-3">
              <label class="form-label">받는 사람 (아이디)</label>
              <input type="text" name="receiverId" class="form-control" value="${to}" required>
            </div>
            <div class="mb-3">
              <label class="form-label">내용</label>
              <textarea name="content" class="form-control" rows="6" required></textarea>
            </div>
            <div class="d-flex gap-2">
              <button type="submit" class="btn btn-warning">보내기</button>
              <a href="${pageContext.request.contextPath}/message" class="btn btn-outline-secondary">취소</a>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
