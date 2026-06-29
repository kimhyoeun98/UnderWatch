<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="마이페이지" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container py-4">
  <h3 class="ow-section-title mb-4">마이페이지</h3>

  <c:if test="${param.updated eq 'true'}">
    <div class="alert alert-success">정보가 수정되었습니다.</div>
  </c:if>
  <c:if test="${param.pwChanged eq 'true'}">
    <div class="alert alert-success">비밀번호가 변경되었습니다.</div>
  </c:if>
  <c:if test="${not empty pwError}">
    <div class="alert alert-danger">${pwError}</div>
  </c:if>
  <c:if test="${not empty profileError}">
    <div class="alert alert-danger">${profileError}</div>
  </c:if>
  <c:if test="${not empty infoError}">
    <div class="alert alert-danger">${infoError}</div>
  </c:if>

  <!-- M-06 프로필 / M-08 등급 -->
  <div class="card shadow-sm mb-4">
    <div class="card-header"><span class="ow-section-title">내 프로필</span></div>
    <div class="card-body d-flex align-items-center gap-3">
      <c:choose>
        <c:when test="${not empty member.profileImg}">
          <img src="${pageContext.request.contextPath}/upload/${member.profileImg}"
               style="width:80px;height:80px;object-fit:cover;border-radius:50%" alt="프로필">
        </c:when>
        <c:otherwise>
          <div class="text-warning"
               style="width:80px;height:80px;border-radius:50%;background:var(--ow-panel-hi);display:flex;align-items:center;justify-content:center;font-size:2rem">&#9670;</div>
        </c:otherwise>
      </c:choose>
      <div class="flex-grow-1">
        <div class="mb-2">
          <strong class="text-warning"><c:out value="${member.nickname}"/></strong>
          <span class="badge bg-warning ms-1">${grade}</span>
          <span class="text-muted small ms-1">활동점수 ${gradeScore}</span>
        </div>
        <form method="post" action="${pageContext.request.contextPath}/member/mypage/profile"
              enctype="multipart/form-data" class="d-flex gap-2 align-items-center">
          <input type="file" name="profileFile" class="form-control form-control-sm" accept=".jpg,.jpeg,.png" required>
          <button type="submit" class="btn btn-warning btn-sm">업로드</button>
        </form>
      </div>
    </div>
  </div>

  <!-- M-09 얼굴 로그인 등록 -->
  <div class="card shadow-sm mb-4">
    <div class="card-header d-flex align-items-center justify-content-between">
      <span class="ow-section-title">얼굴 로그인</span>
      <c:choose>
        <c:when test="${hasFace}"><span class="badge bg-warning">등록됨</span></c:when>
        <c:otherwise><span class="badge bg-secondary">미등록</span></c:otherwise>
      </c:choose>
    </div>
    <div class="card-body">
      <p class="text-muted small mb-3">
        얼굴을 등록하면 로그인 화면에서 <b>아이디 없이 얼굴만으로</b> 로그인할 수 있습니다.
        카메라는 <b>http://localhost:8080</b> 접속 시에만 동작합니다.
      </p>
      <div id="faceSecureWarn" class="alert alert-danger d-none">
        카메라는 보안 연결에서만 동작합니다. http://localhost:8080 으로 접속해 주세요.
      </div>
      <div class="row g-3 align-items-center">
        <div class="col-auto">
          <div class="position-relative d-inline-block">
            <video id="faceCam" autoplay muted playsinline
                   style="width:240px;border:1px solid var(--ow-border);border-radius:var(--ow-radius);background:#000;display:block"></video>
            <div style="position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);
                        width:55%;aspect-ratio:1/1;border:2px dashed rgba(255,255,255,.8);
                        border-radius:8px;pointer-events:none"></div>
          </div>
        </div>
        <div class="col">
          <div id="faceStatus" class="small text-muted mb-2">카메라 준비 중...</div>
          <button id="faceRegBtn" class="btn btn-warning" disabled>
            ${hasFace ? '얼굴 다시 등록' : '얼굴 등록'}
          </button>
        </div>
      </div>
    </div>
  </div>

  <div class="row g-4">

    <!-- 내 정보 수정 -->
    <div class="col-md-6">
      <div class="card shadow-sm">
        <div class="card-header"><span class="ow-section-title">내 정보 수정</span></div>
        <div class="card-body">
          <form method="post" action="${pageContext.request.contextPath}/member/mypage/info">
            <div class="mb-3">
              <label class="form-label">아이디</label>
              <input type="text" class="form-control" value="${member.id}" readonly>
            </div>
            <div class="mb-3">
              <label class="form-label">닉네임 <small class="text-muted">(변경 시 30일 제한)</small></label>
              <input type="text" name="nickname" class="form-control" value="${member.nickname}" required>
            </div>
            <div class="mb-3">
              <label class="form-label">전화번호</label>
              <input type="text" name="phone" class="form-control" value="${member.phone}">
            </div>
            <div class="mb-3">
              <label class="form-label">이메일</label>
              <input type="email" name="email" class="form-control" value="${member.email}" required>
            </div>
            <button type="submit" class="btn btn-warning">정보 수정</button>
          </form>
        </div>
      </div>
    </div>

    <!-- 비밀번호 변경 -->
    <div class="col-md-6">
      <div class="card shadow-sm">
        <div class="card-header"><span class="ow-section-title">비밀번호 변경</span></div>
        <div class="card-body">
          <form method="post" action="${pageContext.request.contextPath}/member/mypage/password">
            <div class="mb-3">
              <label class="form-label">현재 비밀번호</label>
              <input type="password" name="currentPassword" class="form-control" required>
            </div>
            <div class="mb-3">
              <label class="form-label">새 비밀번호</label>
              <input type="password" name="newPassword" class="form-control" minlength="8" required>
            </div>
            <div class="mb-3">
              <label class="form-label">새 비밀번호 확인</label>
              <input type="password" name="confirmPassword" class="form-control" required>
            </div>
            <button type="submit" class="btn btn-outline-secondary">비밀번호 변경</button>
          </form>
        </div>
      </div>
    </div>

  </div>

  <!-- M-05 활동 내역 -->
  <div class="row g-4 mt-1">

    <!-- 내가 쓴 글 -->
    <div class="col-md-6">
      <div class="card shadow-sm">
        <div class="card-header"><span class="ow-section-title">내가 쓴 글</span></div>
        <ul class="list-group list-group-flush">
          <c:forEach var="post" items="${myPosts}">
            <li class="list-group-item d-flex justify-content-between align-items-center">
              <a href="${pageContext.request.contextPath}/board/detail?no=${post.no}" class="text-decoration-none">
                <span class="badge bg-secondary me-1">${post.categoryName}</span>
                <c:out value="${post.title}"/>
                <c:if test="${post.commentCnt > 0}">
                  <span class="text-primary small">[${post.commentCnt}]</span>
                </c:if>
              </a>
              <small class="text-muted">${post.regDate}</small>
            </li>
          </c:forEach>
          <c:if test="${empty myPosts}">
            <li class="list-group-item text-muted text-center">작성한 글이 없습니다.</li>
          </c:if>
        </ul>
      </div>
    </div>

    <!-- 내가 쓴 댓글 -->
    <div class="col-md-6">
      <div class="card shadow-sm">
        <div class="card-header"><span class="ow-section-title">내가 쓴 댓글</span></div>
        <ul class="list-group list-group-flush">
          <c:forEach var="cmt" items="${myComments}">
            <li class="list-group-item">
              <a href="${pageContext.request.contextPath}/board/detail?no=${cmt.boardNo}" class="text-decoration-none d-block">
                <c:out value="${cmt.content}"/>
              </a>
              <small class="text-muted">↳ <c:out value="${cmt.boardTitle}"/> · ${cmt.regDate}</small>
            </li>
          </c:forEach>
          <c:if test="${empty myComments}">
            <li class="list-group-item text-muted text-center">작성한 댓글이 없습니다.</li>
          </c:if>
        </ul>
      </div>
    </div>

  </div>
</div>

<!-- M-09 얼굴 등록 스크립트 (외부 라이브러리 없이 웹캠 캡처 → 서버 LBPH 분석) -->
<script>
(function () {
  var ctx    = '${pageContext.request.contextPath}';
  var video  = document.getElementById('faceCam');
  var status = document.getElementById('faceStatus');
  var btn    = document.getElementById('faceRegBtn');
  if (!video) return;

  if (!window.isSecureContext) {
    document.getElementById('faceSecureWarn').classList.remove('d-none');
  }
  function setStatus(m) { status.textContent = m; }

  // 웹캠 중앙의 정사각형 영역을 잘라 base64 JPEG 으로 만든다
  function captureSquare(size) {
    var vw = video.videoWidth, vh = video.videoHeight;
    if (!vw || !vh) return null;
    var side = Math.min(vw, vh);
    var sx = (vw - side) / 2, sy = (vh - side) / 2;
    var canvas = document.createElement('canvas');
    canvas.width = size; canvas.height = size;
    canvas.getContext('2d').drawImage(video, sx, sy, side, side, 0, 0, size, size);
    return canvas.toDataURL('image/jpeg', 0.92);
  }

  navigator.mediaDevices.getUserMedia({ video: true })
    .then(function (stream) {
      video.srcObject = stream;
      setStatus('준비 완료 — 점선 안에 얼굴을 맞추고 등록하세요.');
      btn.disabled = false;
    })
    .catch(function (err) {
      setStatus('카메라를 열 수 없습니다: ' + err.message);
    });

  btn.addEventListener('click', function () {
    var image = captureSquare(240);
    if (!image) { setStatus('카메라가 아직 준비되지 않았습니다.'); return; }
    btn.disabled = true;
    setStatus('얼굴을 분석하는 중...');

    var body = new URLSearchParams();
    body.append('image', image);
    fetch(ctx + '/member/face/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: body.toString()
    })
    .then(function (r) { return r.json(); })
    .then(function (res) {
      setStatus(res.message || (res.success ? '등록 완료' : '등록 실패'));
      btn.disabled = false;
    })
    .catch(function (err) {
      setStatus('오류: ' + err.message);
      btn.disabled = false;
    });
  });
})();
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
