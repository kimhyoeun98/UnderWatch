<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"    uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="pageTitle" value="회원가입" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container py-5">
  <div class="row justify-content-center">
    <div class="col-md-6">
      <div class="card shadow">
        <div class="card-header ow-auth-head py-3">
          <h4 class="mb-0">&#9670; 회원가입</h4>
        </div>
        <div class="card-body p-4">

          <form:form action="${pageContext.request.contextPath}/member/register"
                     method="post" modelAttribute="memberVO">

            <!-- 아이디 -->
            <div class="mb-3">
              <label class="form-label">아이디 <span class="text-danger">*</span></label>
              <div class="input-group">
                <form:input path="id" cssClass="form-control" placeholder="영문/숫자 4~20자" required="true" />
                <button type="button" class="btn btn-outline-secondary" onclick="checkId()">중복확인</button>
              </div>
              <div id="idMsg" class="form-text"></div>
              <c:if test="${not empty idError}">
                <div class="text-danger small">${idError}</div>
              </c:if>
              <form:errors path="id" cssClass="text-danger small" />
            </div>

            <!-- 비밀번호 -->
            <div class="mb-3">
              <label class="form-label">비밀번호 <span class="text-danger">*</span></label>
              <form:password path="password" cssClass="form-control" placeholder="8자 이상" />
              <form:errors path="password" cssClass="text-danger small" />
            </div>

            <!-- 닉네임 -->
            <div class="mb-3">
              <label class="form-label">닉네임 <span class="text-danger">*</span></label>
              <div class="input-group">
                <form:input path="nickname" cssClass="form-control" placeholder="2~20자" />
                <button type="button" class="btn btn-outline-secondary" onclick="checkNickname()">중복확인</button>
              </div>
              <div id="nicknameMsg" class="form-text"></div>
              <c:if test="${not empty nicknameError}">
                <div class="text-danger small">${nicknameError}</div>
              </c:if>
              <form:errors path="nickname" cssClass="text-danger small" />
            </div>

            <!-- 이름 -->
            <div class="mb-3">
              <label class="form-label">이름 <span class="text-danger">*</span></label>
              <form:input path="name" cssClass="form-control" />
              <form:errors path="name" cssClass="text-danger small" />
            </div>

            <!-- 전화번호 (하이픈 자동 입력) -->
            <div class="mb-3">
              <label class="form-label">전화번호</label>
              <form:input path="phone" cssClass="form-control" id="phoneInput"
                          placeholder="숫자만 입력하면 자동으로 - 가 들어갑니다" maxlength="13" />
              <form:errors path="phone" cssClass="text-danger small" />
            </div>

            <!-- 이메일 (아이디 + @ + 도메인 선택/직접입력) -->
            <div class="mb-3">
              <label class="form-label">이메일 <span class="text-danger">*</span></label>
              <div class="input-group">
                <input type="text" id="emailLocal" class="form-control" placeholder="아이디" autocomplete="off" />
                <span class="input-group-text">@</span>
                <input type="text" id="emailDomain" class="form-control" placeholder="도메인" autocomplete="off" />
                <select id="emailDomainSelect" class="form-select" style="max-width:150px">
                  <option value="">직접입력</option>
                  <option value="gmail.com">gmail.com</option>
                  <option value="naver.com">naver.com</option>
                  <option value="daum.net">daum.net</option>
                  <option value="kakao.com">kakao.com</option>
                  <option value="nate.com">nate.com</option>
                  <option value="hanmail.net">hanmail.net</option>
                </select>
                <button type="button" class="btn btn-outline-secondary" onclick="checkEmail()">중복확인</button>
              </div>
              <%-- 실제 서버로 전송되는 이메일(아이디@도메인 결합값) --%>
              <form:hidden path="email" />
              <div id="emailMsg" class="form-text"></div>
              <c:if test="${not empty emailError}">
                <div class="text-danger small">${emailError}</div>
              </c:if>
              <form:errors path="email" cssClass="text-danger small" />
            </div>

            <button type="submit" class="btn btn-warning w-100">가입하기</button>
          </form:form>

        </div>
      </div>
    </div>
  </div>
</div>

<script>
const ctx = '${pageContext.request.contextPath}';

function checkId() {
  const id = document.querySelector('[name="id"]').value;
  if (!id) return;
  fetch(ctx + '/member/idCheck?id=' + encodeURIComponent(id))
    .then(r => r.text())
    .then(msg => {
      const el = document.getElementById('idMsg');
      el.textContent = msg === 'OK' ? '사용 가능한 아이디입니다.' : '이미 사용 중인 아이디입니다.';
      el.className = 'form-text ' + (msg === 'OK' ? 'text-success' : 'text-danger');
    });
}

function checkNickname() {
  const nickname = document.querySelector('[name="nickname"]').value;
  if (!nickname) return;
  fetch(ctx + '/member/nicknameCheck?nickname=' + encodeURIComponent(nickname))
    .then(r => r.text())
    .then(msg => {
      const el = document.getElementById('nicknameMsg');
      el.textContent = msg === 'OK' ? '사용 가능한 닉네임입니다.' : '이미 사용 중인 닉네임입니다.';
      el.className = 'form-text ' + (msg === 'OK' ? 'text-success' : 'text-danger');
    });
}

function checkEmail() {
  syncEmail();
  const email = document.querySelector('input[name="email"]').value;
  if (!email || email.indexOf('@') < 1) {
    const el = document.getElementById('emailMsg');
    el.textContent = '이메일을 올바르게 입력하세요.';
    el.className = 'form-text text-danger';
    return;
  }
  fetch(ctx + '/member/emailCheck?email=' + encodeURIComponent(email))
    .then(r => r.text())
    .then(msg => {
      const el = document.getElementById('emailMsg');
      el.textContent = msg === 'OK' ? '사용 가능한 이메일입니다.' : '이미 사용 중인 이메일입니다.';
      el.className = 'form-text ' + (msg === 'OK' ? 'text-success' : 'text-danger');
    });
}

/* ===== #2 전화번호 자동 하이픈 ===== */
(function () {
  const p = document.getElementById('phoneInput');
  if (!p) return;
  p.addEventListener('input', function () {
    let d = p.value.replace(/[^0-9]/g, '').slice(0, 11);
    if (d.startsWith('02')) {                 // 서울 지역번호(2자리)
      if (d.length > 9) p.value = d.replace(/(\d{2})(\d{4})(\d{1,4})/, '$1-$2-$3');
      else if (d.length > 5) p.value = d.replace(/(\d{2})(\d{3,4})(\d{1,4})/, '$1-$2-$3');
      else if (d.length > 2) p.value = d.replace(/(\d{2})(\d{1,4})/, '$1-$2');
      else p.value = d;
    } else {                                  // 휴대폰/그 외(3자리)
      if (d.length > 7) p.value = d.replace(/(\d{3})(\d{4})(\d{1,4})/, '$1-$2-$3');
      else if (d.length > 3) p.value = d.replace(/(\d{3})(\d{1,4})/, '$1-$2');
      else p.value = d;
    }
  });
})();

/* ===== #3 이메일: 아이디 + @ + 도메인 결합 ===== */
const emailLocal  = document.getElementById('emailLocal');
const emailDomain = document.getElementById('emailDomain');
const emailSelect = document.getElementById('emailDomainSelect');
const emailHidden = document.querySelector('input[name="email"]');

function syncEmail() {
  const local  = (emailLocal.value || '').trim();
  const domain = (emailDomain.value || '').trim();
  emailHidden.value = (local && domain) ? (local + '@' + domain) : '';
}

emailSelect.addEventListener('change', function () {
  if (this.value) {                 // 프리셋 도메인 선택 → 고정
    emailDomain.value = this.value;
    emailDomain.readOnly = true;
  } else {                          // 직접입력
    emailDomain.value = '';
    emailDomain.readOnly = false;
    emailDomain.focus();
  }
  syncEmail();
});
emailLocal.addEventListener('input', syncEmail);
emailDomain.addEventListener('input', syncEmail);

/* 검증 실패로 되돌아온 경우: 기존 결합 이메일을 분리해 복원 */
(function initEmail() {
  const cur = (emailHidden.value || '').trim();
  if (cur.indexOf('@') > 0) {
    const parts = cur.split('@');
    emailLocal.value = parts[0];
    emailDomain.value = parts[1];
    const opt = [...emailSelect.options].find(o => o.value === parts[1]);
    if (opt) { emailSelect.value = parts[1]; emailDomain.readOnly = true; }
  }
})();

/* 제출 직전 결합값 보장 */
emailHidden.form.addEventListener('submit', syncEmail);
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
