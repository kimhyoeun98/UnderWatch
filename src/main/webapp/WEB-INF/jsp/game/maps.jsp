<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="맵 정보" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<style>
.map-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}
.map-card {
  border-radius: 10px;
  overflow: hidden;
  background: #1a1a2e;
  box-shadow: 0 2px 8px rgba(0,0,0,.3);
  transition: transform .18s, box-shadow .18s;
  cursor: default;
}
.map-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 20px rgba(0,0,0,.4);
}
.map-card img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  display: block;
}
.map-card-body {
  padding: 12px 14px;
}
.map-card-name {
  font-size: 1rem;
  font-weight: 700;
  color: #fff;
  margin-bottom: 4px;
}
.map-card-location {
  font-size: .8rem;
  color: #aaa;
  margin-bottom: 8px;
}
.map-mode-badge {
  display: inline-block;
  font-size: .72rem;
  padding: 2px 8px;
  border-radius: 20px;
  background: rgba(248,161,0,.2);
  color: #f8a100;
  border: 1px solid rgba(248,161,0,.35);
  margin: 2px 2px 0 0;
}
.gamemode-filter {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}
.gamemode-btn {
  padding: 5px 16px;
  border-radius: 20px;
  border: 1px solid #dee2e6;
  background: #fff;
  font-size: .85rem;
  cursor: pointer;
  transition: all .15s;
}
.gamemode-btn.active, .gamemode-btn:hover {
  background: #f8a100;
  border-color: #f8a100;
  color: #fff;
}
</style>

<div class="container py-4">
  <jsp:include page="/WEB-INF/jsp/game/game-tabs.jsp" />
  <h3 class="ow-section-title mb-3">맵 정보</h3>

  <c:choose>
    <c:when test="${empty maps}">
      <div class="card shadow-sm">
        <div class="card-body text-center text-muted py-5">맵 정보를 불러올 수 없습니다. (외부 API 연결 상태를 확인하세요)</div>
      </div>
    </c:when>
    <c:otherwise>

      <!-- 게임 모드 필터 -->
      <div class="gamemode-filter" id="modeFilter">
        <button class="gamemode-btn active" data-mode="all">전체</button>
        <button class="gamemode-btn" data-mode="화물 호송">화물 호송</button>
        <button class="gamemode-btn" data-mode="혼합">혼합</button>
        <button class="gamemode-btn" data-mode="쟁탈">쟁탈</button>
        <button class="gamemode-btn" data-mode="밀기">밀기</button>
        <button class="gamemode-btn" data-mode="거점 장악">거점 장악</button>
        <button class="gamemode-btn" data-mode="격전">격전</button>
        <button class="gamemode-btn" data-mode="깃발 쟁탈전">깃발 쟁탈전</button>
      </div>

      <!-- 맵 그리드 -->
      <div class="map-grid" id="mapGrid">
        <c:forEach var="m" items="${maps}">
          <div class="map-card" data-modes="<c:forEach var="gm" items="${m.gamemodesKr}" varStatus="s">${gm}<c:if test="${!s.last}">,</c:if></c:forEach>">
            <c:choose>
              <c:when test="${not empty m.screenshot}">
                <img src="${m.screenshot}" alt="${m.name}" loading="lazy"
                     onerror="this.style.display='none'">
              </c:when>
              <c:otherwise>
                <div style="height:160px;background:#2a2a3e;display:flex;align-items:center;justify-content:center;">
                  <span style="color:#555;font-size:2rem;">🗺</span>
                </div>
              </c:otherwise>
            </c:choose>
            <div class="map-card-body">
              <div class="map-card-name"><c:out value="${m.nameKr}"/></div>
              <c:if test="${not empty m.location}">
                <div class="map-card-location">📍 <c:out value="${m.location}"/></div>
              </c:if>
              <c:forEach var="gm" items="${m.gamemodesKr}">
                <span class="map-mode-badge">${gm}</span>
              </c:forEach>
            </div>
          </div>
        </c:forEach>
      </div>

    </c:otherwise>
  </c:choose>
</div>

<script>
(function () {
  var btns  = document.querySelectorAll('.gamemode-btn');
  var cards = document.querySelectorAll('.map-card');

  btns.forEach(function (btn) {
    btn.addEventListener('click', function () {
      btns.forEach(function (b) { b.classList.remove('active'); });
      btn.classList.add('active');
      var mode = btn.getAttribute('data-mode');
      cards.forEach(function (card) {
        if (mode === 'all') {
          card.style.display = '';
        } else {
          var modes = card.getAttribute('data-modes') || '';
          card.style.display = modes.indexOf(mode) !== -1 ? '' : 'none';
        }
      });
    });
  });
})();
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
