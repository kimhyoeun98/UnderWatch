<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="신고 처리" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container py-4">
  <h3 class="ow-section-title mb-4">신고 처리</h3>

  <div class="card shadow-sm">
    <div class="card-body p-0">
      <table class="table table-hover align-middle mb-0">
        <thead>
          <tr>
            <th style="width:140px">대상</th><th style="width:110px">신고자</th>
            <th>사유</th><th style="width:90px">상태</th>
            <th style="width:130px">신고일</th><th style="width:120px">처리</th>
          </tr>
        </thead>
        <tbody>
          <c:choose>
            <c:when test="${empty reports}">
              <tr><td colspan="6" class="text-center py-4 text-muted">신고 내역이 없습니다.</td></tr>
            </c:when>
            <c:otherwise>
              <c:forEach var="r" items="${reports}">
                <tr>
                  <td>
                    <c:choose>
                      <c:when test="${r.targetType == 'B'}">
                        <span class="badge bg-secondary">게시글</span>
                        <a href="${pageContext.request.contextPath}/board/detail?no=${r.targetNo}" class="text-decoration-none">#${r.targetNo}</a>
                      </c:when>
                      <c:when test="${r.targetType == 'M'}">
                        <span class="badge bg-info">쪽지</span>
                      </c:when>
                      <c:otherwise>
                        <span class="badge bg-secondary">댓글</span> #${r.targetNo}
                      </c:otherwise>
                    </c:choose>
                  </td>
                  <td>${r.reporterId}</td>
                  <td><c:out value="${r.reason}"/></td>
                  <td><span class="badge ${r.status == 'PENDING' ? 'bg-warning' : 'bg-secondary'}">${r.status}</span></td>
                  <td class="text-muted small">${r.regDate}</td>
                  <td>
                    <c:choose>
                      <c:when test="${r.status == 'PENDING' and r.targetType == 'M'}">
                        <%-- 쪽지 신고: 대화 내용 확인 + 종결 처리 --%>
                        <div class="d-flex gap-1">
                          <a href="${pageContext.request.contextPath}/admin/reports/message?no=${r.no}"
                             class="btn btn-sm btn-outline-info">대화 보기</a>
                          <form method="post" action="${pageContext.request.contextPath}/admin/reports/resolve"
                                onsubmit="return confirm('이 신고를 처리 완료로 종결하시겠습니까?')">
                            <input type="hidden" name="no" value="${r.no}">
                            <button type="submit" class="btn btn-sm btn-outline-secondary">처리완료</button>
                          </form>
                        </div>
                      </c:when>
                      <c:when test="${r.status == 'PENDING'}">
                        <form method="post" action="${pageContext.request.contextPath}/admin/reports/blind"
                              onsubmit="return confirm('블라인드 처리하시겠습니까?')">
                          <input type="hidden" name="no" value="${r.no}">
                          <input type="hidden" name="targetType" value="${r.targetType}">
                          <input type="hidden" name="targetNo" value="${r.targetNo}">
                          <button type="submit" class="btn btn-sm btn-outline-danger">블라인드</button>
                        </form>
                      </c:when>
                      <c:otherwise>
                        <c:if test="${r.targetType == 'M'}">
                          <a href="${pageContext.request.contextPath}/admin/reports/message?no=${r.no}"
                             class="btn btn-sm btn-outline-info">대화 보기</a>
                        </c:if>
                        <span class="text-muted small">처리됨</span>
                      </c:otherwise>
                    </c:choose>
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
