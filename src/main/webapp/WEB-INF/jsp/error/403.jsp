<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="접근 권한 없음" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container py-5 text-center">
  <h1 class="display-1 text-warning ow-head">403</h1>
  <h3>접근 권한이 없습니다.</h3>
  <p class="text-muted">이 페이지에 접근할 권한이 없습니다.</p>
  <a href="${pageContext.request.contextPath}/" class="btn btn-warning mt-2">홈으로</a>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
