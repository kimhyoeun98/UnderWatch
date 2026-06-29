<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="얼굴 로그인" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container py-5">
  <div class="row justify-content-center">
    <div class="col-md-6">
      <div class="card shadow">
        <div class="card-header ow-auth-head py-3">
          <h4 class="mb-0">&#128100; 얼굴 로그인</h4>
        </div>
        <div class="card-body p-4 text-center">

          <div id="secureWarn" class="alert alert-danger d-none">
            카메라는 보안 연결에서만 동작합니다. <strong>http://localhost:8080</strong> 로 접속해 주세요.
          </div>

          <p class="text-muted small mb-3">카메라를 정면으로 바라본 뒤 <b>얼굴 인식</b>을 누르세요.</p>

          <div class="position-relative d-inline-block mb-3">
            <video id="cam" autoplay muted playsinline
                   style="width:100%;max-width:360px;border:1px solid var(--ow-border);border-radius:var(--ow-radius);background:#000;display:block"></video>
            <div style="position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);
                        width:55%;aspect-ratio:1/1;border:2px dashed rgba(255,255,255,.8);
                        border-radius:8px;pointer-events:none"></div>
          </div>

          <div id="status" class="small text-muted mb-3">카메라 준비 중...</div>

          <button id="scanBtn" class="btn btn-warning w-100" disabled>얼굴 인식</button>

          <hr style="border-color: var(--ow-border);">
          <div class="text-center small">
            <a href="${pageContext.request.contextPath}/member/login">아이디로 로그인</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

<script>
(function () {
  var ctx     = '${pageContext.request.contextPath}';
  var video   = document.getElementById('cam');
  var status  = document.getElementById('status');
  var scanBtn = document.getElementById('scanBtn');

  if (!window.isSecureContext) {
    document.getElementById('secureWarn').classList.remove('d-none');
  }
  function setStatus(msg) { status.textContent = msg; }

  // 웹캠 중앙의 정사각형 영역을 잘라 base64 JPEG 으로 만든다 (분석은 서버 LBPH 가 담당)
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

  // 1) 카메라 시작
  navigator.mediaDevices.getUserMedia({ video: true })
    .then(function (stream) {
      video.srcObject = stream;
      setStatus('준비 완료 — 점선 안에 얼굴을 맞추고 인식을 눌러주세요.');
      scanBtn.disabled = false;
    })
    .catch(function (err) {
      setStatus('카메라를 열 수 없습니다: ' + err.message);
    });

  // 2) 얼굴 이미지 전송 → 서버 LBPH 매칭
  scanBtn.addEventListener('click', function () {
    var image = captureSquare(240);
    if (!image) { setStatus('카메라가 아직 준비되지 않았습니다.'); return; }
    scanBtn.disabled = true;
    setStatus('얼굴을 분석하는 중...');

    var body = new URLSearchParams();
    body.append('image', image);
    fetch(ctx + '/member/face/loginProc', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: body.toString()
    })
    .then(function (r) { return r.json(); })
    .then(function (res) {
      if (res.success) {
        setStatus('인증 성공! 이동 중...');
        location.href = res.redirect;
      } else {
        setStatus(res.message || '인증 실패');
        scanBtn.disabled = false;
      }
    })
    .catch(function (err) {
      setStatus('오류: ' + err.message);
      scanBtn.disabled = false;
    });
  });
})();
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
