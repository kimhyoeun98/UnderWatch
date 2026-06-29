<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="방문자 통계" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container py-4">
  <h3 class="ow-section-title mb-4">
    방문자 통계 <small class="text-muted">(최근 2주 · 일별 고유 방문)</small>
  </h3>

  <div class="card shadow-sm">
    <div class="card-body p-0">
      <table class="table table-hover align-middle mb-0">
        <thead>
          <tr><th style="width:160px">날짜</th><th>방문 수</th></tr>
        </thead>
        <tbody>
          <c:choose>
            <c:when test="${empty visits}">
              <tr><td colspan="2" class="text-center py-4 text-muted">방문 기록이 없습니다.</td></tr>
            </c:when>
            <c:otherwise>
              <c:forEach var="v" items="${visits}">
                <tr>
                  <td>${v.visitDate}</td>
                  <td>
                    <div class="d-flex align-items-center gap-2">
                      <div style="height:14px; width:${v.visitCount * 14}px; min-width:4px; background:var(--ow-orange); border-radius:3px"></div>
                      <span>${v.visitCount}</span>
                    </div>
                  </td>
                </tr>
              </c:forEach>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>
  </div>

  <a href="${pageContext.request.contextPath}/admin" class="btn btn-outline-secondary btn-sm mt-3">대시보드</a>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
