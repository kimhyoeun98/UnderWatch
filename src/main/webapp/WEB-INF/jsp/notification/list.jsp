<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="알림" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container py-4">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h3 class="ow-section-title mb-0">알림</h3>
    <form method="post" action="${pageContext.request.contextPath}/notification/readAll">
      <button type="submit" class="btn btn-outline-secondary btn-sm">모두 읽음</button>
    </form>
  </div>

  <div class="card shadow-sm">
    <ul class="list-group list-group-flush">
      <c:choose>
        <c:when test="${empty notifications}">
          <li class="list-group-item text-muted text-center py-4">알림이 없습니다.</li>
        </c:when>
        <c:otherwise>
          <c:forEach var="n" items="${notifications}">
            <li class="list-group-item ${n.isRead == 'N' ? 'border-start border-warning border-3' : ''}">
              <a href="${pageContext.request.contextPath}${n.link}" class="text-decoration-none d-block">
                <c:if test="${n.isRead == 'N'}"><span class="badge bg-warning me-1">NEW</span></c:if>
                <c:out value="${n.content}"/>
              </a>
              <small class="text-muted">${n.regDate}</small>
            </li>
          </c:forEach>
        </c:otherwise>
      </c:choose>
    </ul>
  </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
