<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="패치 노트" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<style>
.patch-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 18px;
}
.patch-card {
  border-radius: 10px;
  overflow: hidden;
  background: #1a1a2e;
  box-shadow: 0 2px 8px rgba(0,0,0,.3);
  transition: transform .18s, box-shadow .18s;
  display: flex;
  flex-direction: column;
}
.patch-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 20px rgba(0,0,0,.4);
}
.patch-card-thumb {
  width: 100%;
  height: 170px;
  object-fit: cover;
  display: block;
  background: #2a2a3e;
}
.patch-card-thumb-placeholder {
  width: 100%;
  height: 170px;
  background: linear-gradient(135deg, #1e2a3e 0%, #2a1e3e 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.5rem;
}
.patch-card-body {
  padding: 14px 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 10px;
}
.patch-card-title {
  font-size: .95rem;
  font-weight: 700;
  color: #fff;
  line-height: 1.4;
}
.patch-card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.patch-card-date {
  font-size: .78rem;
  color: #888;
}
.patch-new-badge {
  font-size: .68rem;
  padding: 2px 8px;
  border-radius: 20px;
  background: #f8a100;
  color: #fff;
  font-weight: 700;
}
.patch-link-btn {
  display: block;
  text-align: center;
  padding: 8px;
  background: rgba(248,161,0,.12);
  color: #f8a100;
  border-top: 1px solid rgba(248,161,0,.2);
  font-size: .85rem;
  font-weight: 600;
  transition: background .15s;
  text-decoration: none;
}
.patch-link-btn:hover {
  background: rgba(248,161,0,.25);
  color: #f8a100;
}
</style>

<div class="container py-4">
  <jsp:include page="/WEB-INF/jsp/game/game-tabs.jsp" />

  <div class="d-flex justify-content-between align-items-center mb-3">
    <h3 class="ow-section-title mb-0">패치 노트</h3>
    <a href="https://overwatch.blizzard.com/ko-kr/news/patch-notes/"
       target="_blank" class="btn btn-outline-secondary btn-sm">공식 사이트 ↗</a>
  </div>

  <c:choose>
    <c:when test="${empty patches}">
      <div class="card shadow-sm">
        <div class="card-body text-center text-muted py-5">
          패치 노트를 불러올 수 없습니다.<br>
          <a href="https://overwatch.blizzard.com/ko-kr/news/patch-notes/"
             target="_blank" class="btn btn-warning btn-sm mt-3">공식 패치 노트 보기 ↗</a>
        </div>
      </div>
    </c:when>
    <c:otherwise>
      <div class="patch-grid">
        <c:forEach var="p" items="${patches}" varStatus="st">
          <div class="patch-card">
            <c:choose>
              <c:when test="${not empty p.thumbnail}">
                <img class="patch-card-thumb" src="${p.thumbnail}" alt="${p.title}"
                     loading="lazy" onerror="this.parentNode.innerHTML='<div class=\'patch-card-thumb-placeholder\'>📋</div>'">
              </c:when>
              <c:otherwise>
                <div class="patch-card-thumb-placeholder">📋</div>
              </c:otherwise>
            </c:choose>

            <div class="patch-card-body">
              <div class="patch-card-title">
                <c:choose>
                  <c:when test="${not empty p.title}"><c:out value="${p.title}"/></c:when>
                  <c:otherwise>패치 노트</c:otherwise>
                </c:choose>
              </div>
              <div class="patch-card-meta">
                <span class="patch-card-date"><c:out value="${p.date}"/></span>
                <c:if test="${st.index == 0}"><span class="patch-new-badge">NEW</span></c:if>
              </div>
            </div>

            <a href="${not empty p.url ? p.url : 'https://overwatch.blizzard.com/ko-kr/news/patch-notes/'}"
               target="_blank" class="patch-link-btn">자세히 보기 ↗</a>
          </div>
        </c:forEach>
      </div>
    </c:otherwise>
  </c:choose>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
