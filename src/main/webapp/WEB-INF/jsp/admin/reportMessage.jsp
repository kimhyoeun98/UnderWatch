<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="쪽지 신고 대화" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container py-4">
  <h3 class="ow-section-title mb-3">쪽지 신고 대화 내용</h3>

  <!-- 신고 요약 -->
  <div class="card shadow-sm mb-3">
    <div class="card-body">
      <div class="d-flex justify-content-between flex-wrap gap-2">
        <span>신고자: <strong class="text-warning">${reporterId}</strong>
          &nbsp;↔&nbsp; 상대: <strong class="text-warning">${partnerId}</strong></span>
        <span class="badge ${report.status == 'PENDING' ? 'bg-warning' : 'bg-secondary'} align-self-center">${report.status}</span>
      </div>
      <hr style="border-color: var(--ow-border);">
      <div class="text-muted small">신고 사유</div>
      <div><c:out value="${report.reason}"/></div>
    </div>
  </div>

  <!-- 대화 스레드 (신고자 기준 오른쪽, 상대 왼쪽) -->
  <div class="card shadow-sm mb-3">
    <div class="card-header"><span class="ow-section-title">대화 (${fn:length(messages)}건)</span></div>
    <div class="card-body">
      <c:choose>
        <c:when test="${empty messages}">
          <p class="text-muted text-center mb-0">두 사용자 간 쪽지가 없습니다. (이미 완전히 삭제되었을 수 있음)</p>
        </c:when>
        <c:otherwise>
          <c:forEach var="m" items="${messages}">
            <c:set var="mine" value="${m.senderId eq reporterId}" />
            <div class="d-flex mb-2 ${mine ? 'justify-content-end' : 'justify-content-start'}">
              <div class="p-2 rounded"
                   style="max-width:75%; background:${mine ? 'var(--ow-accent, #3a4a63)' : 'var(--ow-border, #2a3a55)'};">
                <div class="small text-muted mb-1">
                  <strong class="text-warning"><c:out value="${m.senderNickname}"/></strong>
                  (${m.senderId}) · ${m.regDate}
                </div>
                <div style="white-space:pre-wrap; word-break:break-word;"><c:out value="${m.content}"/></div>
              </div>
            </div>
          </c:forEach>
        </c:otherwise>
      </c:choose>
    </div>
  </div>

  <div class="d-flex justify-content-between">
    <a href="${pageContext.request.contextPath}/admin/reports" class="btn btn-outline-secondary btn-sm">신고 목록</a>
    <c:if test="${report.status == 'PENDING'}">
      <form method="post" action="${pageContext.request.contextPath}/admin/reports/resolve"
            onsubmit="return confirm('이 신고를 처리 완료로 종결하시겠습니까?')">
        <input type="hidden" name="no" value="${report.no}">
        <button type="submit" class="btn btn-sm btn-outline-secondary">처리완료</button>
      </form>
    </c:if>
  </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
