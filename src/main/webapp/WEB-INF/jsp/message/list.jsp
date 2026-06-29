<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="쪽지함" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container py-4" style="max-width:600px">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h3 class="ow-section-title mb-0">쪽지함</h3>
    <a href="${pageContext.request.contextPath}/message/write" class="btn btn-warning btn-sm">새 쪽지</a>
  </div>

  <c:if test="${not empty msg}"><div class="alert alert-success">${msg}</div></c:if>

  <c:choose>
    <c:when test="${empty conversations}">
      <div class="card shadow-sm">
        <div class="card-body text-center text-muted py-5">주고받은 쪽지가 없습니다.</div>
      </div>
    </c:when>
    <c:otherwise>
      <div class="list-group shadow-sm">
        <c:forEach var="cv" items="${conversations}">
          <a href="${pageContext.request.contextPath}/message/chat?with=${cv.partnerId}"
             class="list-group-item list-group-item-action d-flex justify-content-between align-items-center py-3">
            <div class="d-flex align-items-center gap-2">
              <div class="rounded-circle d-flex align-items-center justify-content-center"
                   style="width:42px;height:42px;background:#f8a100;color:#fff;font-weight:700;font-size:1rem;flex-shrink:0">
                ${fn:substring(cv.partnerNickname, 0, 1)}
              </div>
              <div>
                <div class="fw-semibold"><c:out value="${cv.partnerNickname}"/></div>
                <small class="text-muted">${cv.lastDate}</small>
              </div>
            </div>
            <c:if test="${cv.unreadCount > 0}">
              <span class="badge rounded-pill bg-warning text-dark">${cv.unreadCount}</span>
            </c:if>
          </a>
        </c:forEach>
      </div>
    </c:otherwise>
  </c:choose>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
