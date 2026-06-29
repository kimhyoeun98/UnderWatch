<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="구인구직" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container py-4">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h3 class="ow-section-title mb-0">파티 구인구직</h3>
    <c:if test="${pageContext.request.userPrincipal != null}">
      <a href="${pageContext.request.contextPath}/party/write" class="btn btn-warning btn-sm">모집글 작성</a>
    </c:if>
  </div>

  <!-- P-02 필터 -->
  <form method="get" action="${pageContext.request.contextPath}/party/list" class="row g-2 mb-3">
    <div class="col-auto">
      <select name="roleType" class="form-select form-select-sm">
        <option value="">역할 전체</option>
        <option value="탱커"   ${filter.roleType == '탱커'   ? 'selected' : ''}>탱커</option>
        <option value="딜러"   ${filter.roleType == '딜러'   ? 'selected' : ''}>딜러</option>
        <option value="지원가" ${filter.roleType == '지원가' ? 'selected' : ''}>지원가</option>
      </select>
    </div>
    <div class="col-auto">
      <select name="tier" class="form-select form-select-sm">
        <option value="">티어 전체</option>
        <option value="브론즈"       ${filter.tier == '브론즈'       ? 'selected' : ''}>브론즈</option>
        <option value="실버"         ${filter.tier == '실버'         ? 'selected' : ''}>실버</option>
        <option value="골드"         ${filter.tier == '골드'         ? 'selected' : ''}>골드</option>
        <option value="플래티넘"     ${filter.tier == '플래티넘'     ? 'selected' : ''}>플래티넘</option>
        <option value="다이아몬드"   ${filter.tier == '다이아몬드'   ? 'selected' : ''}>다이아몬드</option>
        <option value="마스터"       ${filter.tier == '마스터'       ? 'selected' : ''}>마스터</option>
        <option value="그랜드마스터" ${filter.tier == '그랜드마스터' ? 'selected' : ''}>그랜드마스터</option>
        <option value="챔피언"       ${filter.tier == '챔피언'       ? 'selected' : ''}>챔피언</option>
      </select>
    </div>
    <div class="col-auto">
      <select name="micYn" class="form-select form-select-sm">
        <option value="">마이크 전체</option>
        <option value="Y" ${filter.micYn == 'Y' ? 'selected' : ''}>마이크 사용</option>
        <option value="N" ${filter.micYn == 'N' ? 'selected' : ''}>마이크 미사용</option>
      </select>
    </div>
    <div class="col-auto">
      <select name="status" class="form-select form-select-sm">
        <option value="">상태 전체</option>
        <option value="RECRUITING" ${filter.status == 'RECRUITING' ? 'selected' : ''}>모집중</option>
        <option value="CLOSED"     ${filter.status == 'CLOSED'     ? 'selected' : ''}>모집완료</option>
      </select>
    </div>
    <div class="col-auto">
      <button type="submit" class="btn btn-warning btn-sm">필터</button>
    </div>
  </form>

  <!-- 목록 -->
  <c:choose>
    <c:when test="${empty parties}">
      <div class="card shadow-sm"><div class="card-body text-center text-muted py-5">조건에 맞는 모집글이 없습니다.</div></div>
    </c:when>
    <c:otherwise>
      <c:forEach var="p" items="${parties}">
        <div class="card shadow-sm mb-2 party-card"
             style="cursor:pointer"
             data-writer-id="<c:out value='${p.writerId}'/>"
             data-writer-nickname="<c:out value='${p.writerNickname}'/>"
             data-title="<c:out value='${p.title}'/>"
             data-is-mine="${pageContext.request.userPrincipal != null and p.writerId eq pageContext.request.userPrincipal.name}"
             data-party-no="${p.no}"
             data-party-status="${p.status}">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-start">
              <div>
                <span class="badge ${p.status == 'RECRUITING' ? 'bg-warning' : 'bg-secondary'} me-1">
                  ${p.status == 'RECRUITING' ? '모집중' : '모집완료'}
                </span>
                <strong><c:out value="${p.title}"/></strong>
              </div>
              <small class="text-muted"><c:out value="${p.writerNickname}"/> · ${p.regDate}</small>
            </div>
            <div class="mt-2 small">
              <c:if test="${not empty p.roleType}"><span class="badge bg-secondary">${p.roleType}</span></c:if>
              <c:if test="${not empty p.tier}"><span class="badge bg-secondary">${p.tier}</span></c:if>
              <span class="badge bg-secondary">마이크 ${p.micYn == 'Y' ? '사용' : '미사용'}</span>
              <c:if test="${not empty p.mainHero}"><span class="badge bg-secondary">주영웅: <c:out value="${p.mainHero}"/></span></c:if>
            </div>
            <c:if test="${not empty p.content}">
              <p class="mb-1 mt-2" style="white-space:pre-wrap"><c:out value="${p.content}"/></p>
            </c:if>
            <c:if test="${pageContext.request.userPrincipal != null and p.writerId eq pageContext.request.userPrincipal.name}">
              <form method="post" action="${pageContext.request.contextPath}/party/status" style="display:inline"
                    onclick="event.stopPropagation()">
                <input type="hidden" name="no" value="${p.no}">
                <button type="submit" class="btn btn-outline-secondary btn-sm mt-2">
                  ${p.status == 'RECRUITING' ? '모집완료로 변경' : '다시 모집중으로'}
                </button>
              </form>
            </c:if>
          </div>
        </div>
      </c:forEach>
    </c:otherwise>
  </c:choose>
</div>

<!-- 쪽지 보내기 사이드 패널 -->
<div id="msgSidePanel" style="
  position:fixed; top:0; right:-420px; width:400px; height:100%;
  background:#fff; box-shadow:-4px 0 20px rgba(0,0,0,.25);
  z-index:1050; transition:right .3s ease; display:flex; flex-direction:column;">

  <div style="background:#f8a100; padding:16px 20px; display:flex; justify-content:space-between; align-items:center;">
    <span style="font-weight:700; font-size:1rem; color:#fff;">쪽지 보내기</span>
    <button id="closeSidePanel" style="background:none;border:none;color:#fff;font-size:1.4rem;line-height:1;cursor:pointer;">&times;</button>
  </div>

  <div style="padding:20px; flex:1; overflow-y:auto;">
    <div id="panelReceiverInfo" class="mb-3 p-3" style="background:#f8f9fa; border-radius:8px; font-size:.9rem;">
      <!-- JS로 채움 -->
    </div>

    <div id="panelLoginRequired" class="alert alert-warning d-none">
      쪽지를 보내려면 <a href="${pageContext.request.contextPath}/member/login">로그인</a>이 필요합니다.
    </div>
    <div id="panelOwnPost" class="alert alert-info d-none">
      내가 작성한 모집글입니다.
    </div>

    <form id="panelMsgForm" method="post" action="${pageContext.request.contextPath}/message/write" class="d-none">
      <input type="hidden" name="receiverId" id="panelReceiverId">
      <div class="mb-3">
        <label class="form-label fw-semibold">내용</label>
        <textarea name="content" id="panelContent" class="form-control" rows="7"
                  placeholder="모집글 작성자에게 보낼 메시지를 입력하세요." required></textarea>
      </div>
      <button type="submit" class="btn btn-warning w-100">보내기</button>
    </form>
  </div>
</div>

<!-- 오버레이 -->
<div id="sidePanelOverlay" style="
  display:none; position:fixed; inset:0;
  background:rgba(0,0,0,.4); z-index:1049;"></div>

<script>
(function () {
  var panel    = document.getElementById('msgSidePanel');
  var overlay  = document.getElementById('sidePanelOverlay');
  var closeBtn = document.getElementById('closeSidePanel');

  var isLoggedIn = ${pageContext.request.userPrincipal != null ? 'true' : 'false'};

  function openPanel(writerId, writerNickname, title, isMine) {
    // 수신자 정보 표시
    document.getElementById('panelReceiverInfo').innerHTML =
      '<strong>모집글:</strong> ' + escHtml(title) + '<br>' +
      '<strong>작성자:</strong> ' + escHtml(writerNickname);

    // 영역 초기화
    document.getElementById('panelLoginRequired').classList.add('d-none');
    document.getElementById('panelOwnPost').classList.add('d-none');
    document.getElementById('panelMsgForm').classList.add('d-none');

    if (!isLoggedIn) {
      document.getElementById('panelLoginRequired').classList.remove('d-none');
    } else if (isMine) {
      document.getElementById('panelOwnPost').classList.remove('d-none');
    } else {
      document.getElementById('panelReceiverId').value = writerId;
      document.getElementById('panelContent').value = '';
      document.getElementById('panelMsgForm').classList.remove('d-none');
    }

    panel.style.right = '0';
    overlay.style.display = 'block';
    document.body.style.overflow = 'hidden';
  }

  function closePanel() {
    panel.style.right = '-420px';
    overlay.style.display = 'none';
    document.body.style.overflow = '';
  }

  function escHtml(str) {
    return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
  }

  document.querySelectorAll('.party-card').forEach(function (card) {
    card.addEventListener('click', function () {
      openPanel(
        card.dataset.writerId,
        card.dataset.writerNickname,
        card.dataset.title,
        card.dataset.isMine === 'true'
      );
    });
  });

  closeBtn.addEventListener('click', closePanel);
  overlay.addEventListener('click', closePanel);
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') closePanel();
  });
})();
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
