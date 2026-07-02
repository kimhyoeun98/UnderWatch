<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"    uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="pageTitle" value="글쓰기" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container py-4">
  <div class="card shadow-sm">
    <div class="card-header"><span class="ow-section-title">게시글 작성</span></div>
    <div class="card-body">
      <c:if test="${not empty errorMsg}">
        <div class="alert alert-danger">${errorMsg}</div>
      </c:if>
      <form:form action="${pageContext.request.contextPath}/board/write"
                 method="post" modelAttribute="boardVO" enctype="multipart/form-data">

        <%-- B-02 비로그인 작성: 이름/비밀번호 입력(로그인 상태에서는 숨김) --%>
        <c:if test="${pageContext.request.userPrincipal == null}">
          <div class="row g-2 mb-3">
            <div class="col-md-6">
              <label class="form-label">작성자 이름 <span class="text-danger">*</span></label>
              <input type="text" name="guestName" class="form-control" maxlength="50"
                     placeholder="비로그인 작성자 이름">
            </div>
            <div class="col-md-6">
              <label class="form-label">비밀번호 <span class="text-danger">*</span></label>
              <input type="password" name="guestPassword" class="form-control" maxlength="50"
                     placeholder="수정/삭제 시 필요">
            </div>
            <div class="col-12">
              <small class="text-muted">비로그인 글은 입력한 비밀번호로만 수정·삭제할 수 있습니다.</small>
            </div>
          </div>
        </c:if>

        <div class="mb-3">
          <label class="form-label">카테고리 <span class="text-danger">*</span></label>
          <form:select path="categoryNo" cssClass="form-select">
            <form:option value="0">-- 선택 --</form:option>
            <c:forEach var="cat" items="${categories}">
              <form:option value="${cat.no}">${cat.name}</form:option>
            </c:forEach>
          </form:select>
          <form:errors path="categoryNo" cssClass="text-danger small" />
        </div>

        <div class="mb-3">
          <label class="form-label">제목 <span class="text-danger">*</span></label>
          <form:input path="title" cssClass="form-control" placeholder="제목을 입력하세요" />
          <form:errors path="title" cssClass="text-danger small" />
        </div>

        <div class="mb-3">
          <label class="form-label">내용 <span class="text-danger">*</span></label>
          <form:textarea path="content" cssClass="form-control" rows="15" placeholder="내용을 입력하세요" />
          <form:errors path="content" cssClass="text-danger small" />
        </div>

        <div class="mb-3">
          <label class="form-label">이미지 첨부 <small class="text-muted">(jpg/png, 10MB 이하)</small></label>
          <input type="file" name="imageFile" class="form-control" accept=".jpg,.jpeg,.png">
        </div>

        <div class="d-flex gap-2">
          <button type="submit" class="btn btn-warning">등록</button>
          <a href="${pageContext.request.contextPath}/board/list" class="btn btn-outline-secondary">취소</a>
        </div>
      </form:form>
    </div>
  </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
