<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"    uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="pageTitle" value="게시글 수정" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container py-4">
  <div class="card shadow-sm">
    <div class="card-header"><span class="ow-section-title">게시글 수정</span></div>
    <div class="card-body">
      <c:if test="${not empty errorMsg}">
        <div class="alert alert-danger">${errorMsg}</div>
      </c:if>
      <form:form action="${pageContext.request.contextPath}/board/edit"
                 method="post" modelAttribute="boardVO" enctype="multipart/form-data">

        <form:hidden path="no" />

        <div class="mb-3">
          <label class="form-label">카테고리 <span class="text-danger">*</span></label>
          <form:select path="categoryNo" cssClass="form-select">
            <c:forEach var="cat" items="${categories}">
              <form:option value="${cat.no}">${cat.name}</form:option>
            </c:forEach>
          </form:select>
        </div>

        <div class="mb-3">
          <label class="form-label">제목 <span class="text-danger">*</span></label>
          <form:input path="title" cssClass="form-control" />
          <form:errors path="title" cssClass="text-danger small" />
        </div>

        <div class="mb-3">
          <label class="form-label">내용 <span class="text-danger">*</span></label>
          <form:textarea path="content" cssClass="form-control" rows="15" />
          <form:errors path="content" cssClass="text-danger small" />
        </div>

        <div class="mb-3">
          <label class="form-label">이미지 첨부 <small class="text-muted">(새 파일 선택 시 교체)</small></label>
          <c:if test="${not empty boardVO.imageStored}">
            <div class="mb-2">
              <img src="${pageContext.request.contextPath}/upload/${boardVO.imageStored}"
                   class="img-fluid rounded" style="max-height:200px" alt="">
              <div class="form-check mt-1">
                <input type="checkbox" name="removeImage" value="true" id="removeImage" class="form-check-input">
                <label for="removeImage" class="form-check-label small">기존 이미지 삭제</label>
              </div>
            </div>
          </c:if>
          <input type="file" name="imageFile" class="form-control" accept=".jpg,.jpeg,.png">
        </div>

        <div class="d-flex gap-2">
          <button type="submit" class="btn btn-warning">수정 완료</button>
          <a href="${pageContext.request.contextPath}/board/detail?no=${boardVO.no}" class="btn btn-outline-secondary">취소</a>
        </div>
      </form:form>
    </div>
  </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
