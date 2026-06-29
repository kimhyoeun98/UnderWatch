<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"    uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="pageTitle" value="모집글 작성" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container py-4">
  <div class="card shadow-sm">
    <div class="card-header"><span class="ow-section-title">파티 모집글 작성</span></div>
    <div class="card-body">
      <form:form action="${pageContext.request.contextPath}/party/write" method="post" modelAttribute="partyVO">

        <div class="mb-3">
          <label class="form-label">제목 <span class="text-danger">*</span></label>
          <form:input path="title" cssClass="form-control" placeholder="예: 골드 탱커 듀오 구합니다" required="true" />
        </div>

        <div class="row g-2 mb-3">
          <div class="col-md-3">
            <label class="form-label">역할군</label>
            <form:select path="roleType" cssClass="form-select">
              <form:option value="">선택</form:option>
              <form:option value="탱커">탱커</form:option>
              <form:option value="딜러">딜러</form:option>
              <form:option value="지원가">지원가</form:option>
            </form:select>
          </div>
          <div class="col-md-3">
            <label class="form-label">티어</label>
            <form:select path="tier" cssClass="form-select">
              <form:option value="">선택</form:option>
              <form:option value="브론즈">브론즈</form:option>
              <form:option value="실버">실버</form:option>
              <form:option value="골드">골드</form:option>
              <form:option value="플래티넘">플래티넘</form:option>
              <form:option value="다이아몬드">다이아몬드</form:option>
              <form:option value="마스터">마스터</form:option>
              <form:option value="그랜드마스터">그랜드마스터</form:option>
              <form:option value="챔피언">챔피언</form:option>
            </form:select>
          </div>
          <div class="col-md-3">
            <label class="form-label">주 영웅</label>
            <form:input path="mainHero" cssClass="form-control" placeholder="예: 라인하르트" />
          </div>
          <div class="col-md-3 d-flex align-items-end">
            <div class="form-check pb-2">
              <input type="checkbox" name="micYn" value="Y" class="form-check-input" id="micYn">
              <label class="form-check-label" for="micYn">마이크 사용</label>
            </div>
          </div>
        </div>

        <div class="mb-3">
          <label class="form-label">내용</label>
          <form:textarea path="content" cssClass="form-control" rows="6" placeholder="모집 상세 내용을 입력하세요" />
        </div>

        <div class="d-flex gap-2">
          <button type="submit" class="btn btn-warning">등록</button>
          <a href="${pageContext.request.contextPath}/party/list" class="btn btn-outline-secondary">취소</a>
        </div>
      </form:form>
    </div>
  </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
