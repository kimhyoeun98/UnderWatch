<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="전적 검색" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<style>
.player-hero {
  display: flex; align-items: center; gap: 18px;
  background: #1a1a2e; border-radius: 12px; padding: 20px 24px; color: #fff;
}
.player-hero img.avatar { width: 96px; height: 96px; border-radius: 12px; object-fit: cover; background: #2a2a3e; }
.player-name { font-size: 1.5rem; font-weight: 800; }
.player-endorse { font-size: .9rem; color: #f8a100; }
.rank-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 16px; }
.rank-card { background: #fff; border: 1px solid #eee; border-radius: 10px; padding: 16px; text-align: center; box-shadow: 0 2px 6px rgba(0,0,0,.06); }
.rank-card .role { font-weight: 700; margin-bottom: 8px; }
.rank-card img { width: 64px; height: 64px; object-fit: contain; }
.rank-card .tier { font-weight: 700; text-transform: capitalize; margin-top: 6px; }
.rank-card .none { color: #aaa; padding: 18px 0; }

/* 모드 토글 */
.mode-toggle { display: inline-flex; background: #f0f0f3; border-radius: 999px; padding: 4px; gap: 4px; }
.mode-btn { border: 0; background: transparent; padding: 7px 22px; border-radius: 999px; font-weight: 700; color: #555; cursor: pointer; transition: all .15s; }
.mode-btn.active { background: #f8a100; color: #fff; box-shadow: 0 2px 6px rgba(248,161,0,.4); }
.mode-btn.disabled { opacity: .35; cursor: not-allowed; }

.stat-summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(120px, 1fr)); gap: 12px; }
.stat-box { background: #fff; border: 1px solid #eee; border-radius: 10px; padding: 14px; text-align: center; box-shadow: 0 2px 6px rgba(0,0,0,.06); }
.stat-box .stat-label { font-size: .8rem; color: #888; margin-bottom: 4px; }
.stat-box .stat-value { font-size: 1.25rem; font-weight: 800; color: #f8a100; }

/* 역할 범례 */
.role-legend { display: flex; gap: 14px; font-size: .8rem; color: #777; }
.role-legend span { display: inline-flex; align-items: center; gap: 5px; }
.role-dot { width: 11px; height: 11px; border-radius: 3px; display: inline-block; }

.hero-bars { display: flex; flex-direction: column; gap: 8px; }
.hero-row { display: grid; grid-template-columns: 120px 1fr auto; grid-template-areas: "name bar val" "name extra extra"; align-items: center; gap: 3px 12px; background: #fff; border: 1px solid #eee; border-radius: 8px; padding: 9px 14px; }
.hero-name { grid-area: name; font-weight: 800; color: #14142a; font-size: .98rem; }
.hero-bar-track { grid-area: bar; height: 12px; background: #f0f0f3; border-radius: 6px; overflow: hidden; }
.hero-bar-fill { height: 100%; border-radius: 6px; transition: width .25s; }
.hero-val { grid-area: val; font-variant-numeric: tabular-nums; font-weight: 700; color: #333; min-width: 72px; text-align: right; }
.hero-extra { grid-area: extra; font-size: .76rem; color: #999; }
</style>

<div class="container py-4">
  <jsp:include page="/WEB-INF/jsp/game/game-tabs.jsp" />
  <h3 class="ow-section-title mb-3">전적 검색</h3>

  <!-- 검색 폼 -->
  <form method="get" action="${pageContext.request.contextPath}/game/player" class="mb-4">
    <div class="input-group" style="max-width:460px;">
      <input type="text" name="battletag" class="form-control" placeholder="배틀태그 입력 (예: TeKrop#2217)"
             value="${battletag}" required>
      <button type="submit" class="btn btn-warning fw-bold">검색</button>
    </div>
    <div class="form-text">대소문자를 정확히 입력하세요. 공개 프로필(설정 → 소셜 → career profile: 공개)만 조회됩니다.</div>
  </form>

  <c:if test="${not empty battletag}">
    <c:choose>
      <%-- 조회 실패 --%>
      <c:when test="${empty summary or summary.error}">
        <div class="alert alert-warning">
          <b>${battletag}</b> 님의 전적을 불러올 수 없습니다.<br>
          배틀태그가 정확한지, 그리고 블리자드 <b>커리어 프로필이 공개</b>로 설정돼 있는지 확인하세요.
        </div>
      </c:when>
      <%-- 조회 성공 --%>
      <c:otherwise>
        <div class="player-hero mb-4">
          <c:if test="${not empty summary.avatar}">
            <img class="avatar" src="${summary.avatar}" alt="avatar" onerror="this.style.display='none'">
          </c:if>
          <div>
            <div class="player-name"><c:out value="${summary.username}"/></div>
            <c:if test="${not empty summary.title}">
              <div style="color:#bbb;"><c:out value="${summary.title}"/></div>
            </c:if>
            <c:if test="${not empty summary.endorsement.level}">
              <div class="player-endorse">★ 인도 레벨 ${summary.endorsement.level}</div>
            </c:if>
          </div>
        </div>

        <h5 class="mb-3">경쟁전 랭크 <small class="text-muted">(PC)</small></h5>
        <c:choose>
          <c:when test="${empty summary.competitive or empty summary.competitive.pc}">
            <div class="text-muted mb-4">경쟁전 랭크 기록이 없거나 비공개입니다.</div>
          </c:when>
          <c:otherwise>
            <div class="rank-grid mb-4">
              <c:forEach var="role" items="tank,damage,support">
                <c:set var="r" value="${summary.competitive.pc[role]}" />
                <div class="rank-card">
                  <div class="role">
                    <c:choose>
                      <c:when test="${role == 'tank'}">탱커</c:when>
                      <c:when test="${role == 'damage'}">딜러</c:when>
                      <c:otherwise>지원</c:otherwise>
                    </c:choose>
                  </div>
                  <c:choose>
                    <c:when test="${not empty r}">
                      <c:if test="${not empty r.rank_icon}">
                        <img src="${r.rank_icon}" alt="rank" onerror="this.style.display='none'">
                      </c:if>
                      <div class="tier">${r.division} ${r.tier}</div>
                    </c:when>
                    <c:otherwise>
                      <div class="none">기록 없음</div>
                    </c:otherwise>
                  </c:choose>
                </div>
              </c:forEach>
            </div>
          </c:otherwise>
        </c:choose>

        <!-- 종합/영웅별 전적 (JS 렌더: 모드 토글 + 지표 정렬) -->
        <div id="statsPanel" style="display:none">
          <div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
            <div class="mode-toggle">
              <button type="button" class="mode-btn" data-mode="quickplay">빠른 대전</button>
              <button type="button" class="mode-btn" data-mode="competitive">경쟁전</button>
            </div>
            <div class="role-legend">
              <span><i class="role-dot" style="background:#5a9bd4"></i>탱커</span>
              <span><i class="role-dot" style="background:#e0654f"></i>딜러</span>
              <span><i class="role-dot" style="background:#f0b400"></i>지원</span>
            </div>
          </div>

          <div class="stat-summary mb-4" id="generalRow"></div>

          <div class="d-flex flex-wrap justify-content-between align-items-center mb-2 gap-2">
            <h5 class="mb-0">영웅별 전적 <small class="text-muted">(내림차순)</small></h5>
            <select id="sortMetric" class="form-select" style="max-width:230px">
              <option value="timePlayed">플레이 시간</option>
              <option value="gamesPlayed">플레이한 게임</option>
              <option value="gamesWon">승리한 게임</option>
              <option value="winrate">승률</option>
              <option value="kda">KDA</option>
              <option value="elims">처치 (10분당)</option>
              <option value="assists">도움 (10분당)</option>
              <option value="deaths">죽음 (10분당)</option>
              <option value="damage">입힌 피해 (10분당)</option>
              <option value="healing">치유량 (10분당)</option>
            </select>
          </div>
          <div class="hero-bars" id="heroBars"></div>
          <div id="statsEmpty" class="text-muted mb-3" style="display:none">이 모드의 기록이 없습니다.</div>
        </div>

        <a href="https://overwatch.blizzard.com/ko-kr/career/${fn:replace(battletag,'#','-')}/"
           target="_blank" class="btn btn-outline-secondary btn-sm mt-2">공식 프로필에서 보기 ↗</a>
      </c:otherwise>
    </c:choose>
  </c:if>
</div>

<c:if test="${not empty statsJson}">
<script>
const STATS = ${statsJson};

const METRICS = {
  timePlayed: { fmt: 'time' }, gamesPlayed: { fmt: 'int' }, gamesWon: { fmt: 'int' },
  winrate:    { fmt: 'pct' },  kda: { fmt: 'dec' },         elims: { fmt: 'dec' },
  assists:    { fmt: 'dec' },  deaths: { fmt: 'dec' },      damage: { fmt: 'int' }, healing: { fmt: 'int' }
};
const ROLE_COLOR = { tank: '#5a9bd4', damage: '#e0654f', support: '#f0b400', '': '#9aa0a6' };
let curMode = 'quickplay';

function fmtVal(v, kind) {
  v = v || 0;
  if (kind === 'time') {
    const s = Math.round(v), h = Math.floor(s / 3600), m = Math.floor((s % 3600) / 60), ss = s % 60;
    return h + ':' + String(m).padStart(2, '0') + ':' + String(ss).padStart(2, '0');
  }
  if (kind === 'pct') return (Math.round(v * 100) / 100) + '%';
  if (kind === 'dec') return (Math.round(v * 100) / 100);
  return Math.round(v).toLocaleString();
}

function renderGeneral() {
  const data = STATS[curMode], row = document.getElementById('generalRow');
  if (!data) { row.innerHTML = ''; return; }
  const g = data.general || {};
  const cells = [
    ['플레이 시간', data.generalTime],
    ['판수', (g.games_played || 0).toLocaleString()],
    ['승률', (g.winrate != null ? g.winrate : 0) + '%'],
    ['KDA', g.kda != null ? g.kda : 0]
  ];
  row.innerHTML = cells.map(c =>
    '<div class="stat-box"><div class="stat-label">' + c[0] + '</div><div class="stat-value">' + c[1] + '</div></div>'
  ).join('');
}

function renderHeroes() {
  const data = STATS[curMode], wrap = document.getElementById('heroBars'), empty = document.getElementById('statsEmpty');
  if (!data || !data.heroes || !data.heroes.length) { wrap.innerHTML = ''; empty.style.display = 'block'; return; }
  empty.style.display = 'none';
  const metric = document.getElementById('sortMetric').value, kind = METRICS[metric].fmt;
  const heroes = data.heroes.slice().sort((a, b) => (b[metric] || 0) - (a[metric] || 0));
  const max = Math.max.apply(null, heroes.map(h => h[metric] || 0).concat([1]));
  wrap.innerHTML = heroes.map(h => {
    const v = h[metric] || 0, w = Math.max(2, v * 100 / max), color = ROLE_COLOR[h.role || ''];
    return '<div class="hero-row">'
      + '<div class="hero-name">' + h.nameKr + '</div>'
      + '<div class="hero-bar-track"><div class="hero-bar-fill" style="width:' + w + '%;background:' + color + '"></div></div>'
      + '<div class="hero-val">' + fmtVal(v, kind) + '</div>'
      + '<div class="hero-extra">' + Math.round(h.gamesPlayed) + '게임 · 승률 ' + h.winrate + '% · KDA ' + h.kda + '</div>'
      + '</div>';
  }).join('');
}

function renderAll() { renderGeneral(); renderHeroes(); }

if (STATS && (STATS.quickplay || STATS.competitive)) {
  document.getElementById('statsPanel').style.display = 'block';
  if (!STATS.quickplay && STATS.competitive) curMode = 'competitive';
  document.querySelectorAll('.mode-btn').forEach(btn => {
    const m = btn.dataset.mode;
    if (!STATS[m]) btn.classList.add('disabled');
    btn.classList.toggle('active', m === curMode);
    btn.addEventListener('click', () => {
      if (!STATS[m]) return;
      curMode = m;
      document.querySelectorAll('.mode-btn').forEach(b => b.classList.toggle('active', b.dataset.mode === m));
      renderAll();
    });
  });
  document.getElementById('sortMetric').addEventListener('change', renderHeroes);
  renderAll();
}
</script>
</c:if>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
