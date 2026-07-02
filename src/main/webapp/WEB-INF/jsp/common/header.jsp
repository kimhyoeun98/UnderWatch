<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title><c:out value="${pageTitle}" default="UnderWatch"/> — UnderWatch</title>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Oswald:wght@400;500;600;700&family=Noto+Sans+KR:wght@400;500;700&display=swap">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/underwatch.css">
</head>
<body>

<!-- 1. 메인 네비바 (로고 + 로그인) -->
<nav class="navbar navbar-expand-lg ow-navbar">
  <div class="container">
    <a class="navbar-brand d-flex align-items-center" href="${pageContext.request.contextPath}/">
      <img src="${pageContext.request.contextPath}/resources/images/ow-logo.png" alt="UnderWatch"
           height="30" class="me-2" onerror="this.style.display='none'">
      UnderWatch
    </a>
    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navMenu">
      <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="navMenu">
      <ul class="navbar-nav ms-auto">
        <c:if test="${pageContext.request.userPrincipal != null}">
          <c:if test="${pageContext.request.isUserInRole('ADMIN')}">
            <li class="nav-item">
              <a class="nav-link text-warning" href="${pageContext.request.contextPath}/admin">관리자</a>
            </li>
          </c:if>
          <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/notification">
              알림 <span id="notiBadge" class="badge bg-warning"<c:if test="${unreadNotifications <= 0}"> style="display:none"</c:if>>${unreadNotifications}</span>
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/message">
              쪽지 <span id="msgBadge" class="badge bg-warning"<c:if test="${unreadMessages <= 0}"> style="display:none"</c:if>>${unreadMessages}</span>
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/member/mypage">
              ${pageContext.request.userPrincipal.name} 님
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/member/logout">로그아웃</a>
          </li>
        </c:if>
        <c:if test="${pageContext.request.userPrincipal == null}">
          <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/member/login">로그인</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/member/register">회원가입</a>
          </li>
        </c:if>
      </ul>
    </div>
  </div>
</nav>

<c:if test="${pageContext.request.userPrincipal != null}">
<script>
(function () {
  var ctx = '${pageContext.request.contextPath}';
  function setBadge(id, n) {
    var el = document.getElementById(id);
    if (!el) return;
    if (n > 0) { el.textContent = n; el.style.display = ''; }
    else { el.style.display = 'none'; }
  }
  var ws, retry = 0;
  function connect() {
    var proto = (location.protocol === 'https:') ? 'wss://' : 'ws://';
    ws = new WebSocket(proto + location.host + ctx + '/ws/notify');
    ws.onopen = function () { retry = 0; };
    ws.onmessage = function (e) {
      try {
        var d = JSON.parse(e.data);
        if (d.type === 'message') setBadge('msgBadge', d.count);
        else if (d.type === 'notification') setBadge('notiBadge', d.count);
      } catch (_) { /* 잘못된 메시지는 무시 */ }
    };
    ws.onclose = function () {
      // 연결이 끊기면 지수 백오프로 재연결 (최대 30초 간격)
      retry = Math.min(retry + 1, 6);
      setTimeout(connect, retry * 5000);
    };
    ws.onerror = function () { try { ws.close(); } catch (_) {} };
  }
  connect();
})();
</script>
</c:if>

<!-- 2. 히어로(로고 배너) — 메인에서만 표시 -->
<c:if test="${showHero}">
  <section class="ow-hero ow-hero-sm">
    <div class="container">
      <h1 class="d-inline-flex align-items-center gap-2">
        <img src="${pageContext.request.contextPath}/resources/images/ow-logo.png" alt=""
             height="56" onerror="this.style.display='none'">
        UnderWatch
      </h1>
      <p>오버워치 유저들의 정보 공유 &amp; 소통 공간</p>
    </div>
  </section>
</c:if>

<!-- 3. 카테고리 서브 네비게이션 (모든 페이지 공통) -->
<nav class="ow-subnav">
  <div class="container">
    <a href="${pageContext.request.contextPath}/board/list"
       class="ow-subnav-link ${empty param.categoryNo or param.categoryNo == 0 ? 'active' : ''}">
      전체
    </a>
    <c:forEach var="cat" items="${categories}">
      <a href="${pageContext.request.contextPath}/board/list?categoryNo=${cat.no}"
         class="ow-subnav-link ${param.categoryNo == cat.no ? 'active' : ''}">
        ${cat.name}
      </a>
    </c:forEach>
    <a href="${pageContext.request.contextPath}/party/list" class="ow-subnav-link">구인구직</a>
    <a href="${pageContext.request.contextPath}/game/heroes" class="ow-subnav-link">게임정보</a>
  </div>
</nav>

<main class="ow-main">

<c:if test="${not empty globalMsg}">
  <div class="container mt-3">
    <div class="alert alert-info mb-0">${globalMsg}</div>
  </div>
</c:if>
