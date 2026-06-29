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
