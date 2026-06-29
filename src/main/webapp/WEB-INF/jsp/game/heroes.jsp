<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="영웅 정보" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<%-- 역할 아이콘 (currentColor 로 역할색 적용) --%>
<c:set var="icoTank">
  <svg class="ow-roster-ico" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path fill="currentColor" d="M12 2 4 5v6c0 5 3.4 8.4 8 9 4.6-.6 8-4 8-9V5l-8-3z"/></svg>
</c:set>
<c:set var="icoDamage">
  <svg class="ow-roster-ico" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path fill="currentColor" d="M6 3v18l13-9L6 3z"/></svg>
</c:set>
<c:set var="icoSupport">
  <svg class="ow-roster-ico" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path fill="currentColor" d="M10 3h4v7h7v4h-7v7h-4v-7H3v-4h7V3z"/></svg>
</c:set>

<div class="container py-4">
  <jsp:include page="/WEB-INF/jsp/game/game-tabs.jsp" />
  <div class="d-flex align-items-end justify-content-between flex-wrap gap-2 mb-3">
    <h3 class="ow-section-title mb-0">영웅 정보</h3>
    <span class="text-muted small">영웅 위에 마우스를 올리면 이름이 표시됩니다</span>
  </div>

  <c:if test="${empty tanks and empty damages and empty supports}">
    <div class="card shadow-sm"><div class="card-body text-center text-muted py-5">
      영웅 정보를 불러올 수 없습니다. (외부 API 연결 상태를 확인하세요)
    </div></div>
  </c:if>

  <c:if test="${not (empty tanks and empty damages and empty supports)}">

    <!-- 역할 필터 탭 -->
    <div class="ow-role-tabs">
      <button type="button" class="ow-role-tab active" data-role="all">전체</button>
      <button type="button" class="ow-role-tab" data-role="tank">돌격</button>
      <button type="button" class="ow-role-tab" data-role="damage">공격</button>
      <button type="button" class="ow-role-tab" data-role="support">지원</button>
    </div>

    <!-- 역할별 로스터 (인게임 영웅 선택 화면 스타일) -->
    <div class="ow-roster">

      <div class="ow-roster-group" data-role="tank">
        <div class="ow-roster-head">
          ${icoTank}
          <span class="ow-roster-label">돌격</span>
          <span class="ow-roster-count">${fn:length(tanks)}</span>
        </div>
        <div class="ow-roster-grid">
          <c:forEach var="h" items="${tanks}">
            <a href="${pageContext.request.contextPath}/game/heroes/${h.key}" class="ow-tile">
              <img src="${h.portrait}" alt="${h.nameKr}" loading="lazy">
              <span class="ow-tile-name">${h.nameKr}</span>
            </a>
          </c:forEach>
        </div>
      </div>

      <div class="ow-roster-group" data-role="damage">
        <div class="ow-roster-head">
          ${icoDamage}
          <span class="ow-roster-label">공격</span>
          <span class="ow-roster-count">${fn:length(damages)}</span>
        </div>
        <div class="ow-roster-grid">
          <c:forEach var="h" items="${damages}">
            <a href="${pageContext.request.contextPath}/game/heroes/${h.key}" class="ow-tile">
              <img src="${h.portrait}" alt="${h.nameKr}" loading="lazy">
              <span class="ow-tile-name">${h.nameKr}</span>
            </a>
          </c:forEach>
        </div>
      </div>

      <div class="ow-roster-group" data-role="support">
        <div class="ow-roster-head">
          ${icoSupport}
          <span class="ow-roster-label">지원</span>
          <span class="ow-roster-count">${fn:length(supports)}</span>
        </div>
        <div class="ow-roster-grid">
          <c:forEach var="h" items="${supports}">
            <a href="${pageContext.request.contextPath}/game/heroes/${h.key}" class="ow-tile">
              <img src="${h.portrait}" alt="${h.nameKr}" loading="lazy">
              <span class="ow-tile-name">${h.nameKr}</span>
            </a>
          </c:forEach>
        </div>
      </div>

    </div>
  </c:if>
</div>

<script>
(function () {
  var tabs   = document.querySelectorAll('.ow-role-tab');
  var groups = document.querySelectorAll('.ow-roster-group');
  tabs.forEach(function (tab) {
    tab.addEventListener('click', function () {
      tabs.forEach(function (t) { t.classList.remove('active'); });
      tab.classList.add('active');
      var role = tab.getAttribute('data-role');
      groups.forEach(function (g) {
        g.style.display = (role === 'all' || g.getAttribute('data-role') === role) ? '' : 'none';
      });
    });
  });
})();
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
