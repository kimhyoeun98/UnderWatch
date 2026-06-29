<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="페이지를 찾을 수 없음" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container py-5 text-center">
  <h1 class="display-1 text-warning ow-head">404</h1>
  <h3>페이지를 찾을 수 없습니다.</h3>
  <p class="text-muted">요청한 페이지가 존재하지 않거나 이동되었습니다.</p>
  <a href="${pageContext.request.contextPath}/" class="btn btn-warning mt-2">홈으로</a>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
