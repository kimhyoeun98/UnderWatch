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

            <!-- 전화번호 -->
            <div class="mb-3">
              <label class="form-label">전화번호</label>
              <form:input path="phone" cssClass="form-control" placeholder="010-1234-5678" />
              <form:errors path="phone" cssClass="text-danger small" />
            </div>

            <!-- 이메일 -->
            <div class="mb-3">
              <label class="form-label">이메일 <span class="text-danger">*</span></label>
              <div class="input-group">
                <form:input path="email" cssClass="form-control" type="email" />
                <button type="button" class="btn btn-outline-secondary" onclick="checkEmail()">중복확인</button>
              </div>
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
  const email = document.querySelector('[name="email"]').value;
  if (!email) return;
  fetch(ctx + '/member/emailCheck?email=' + encodeURIComponent(email))
    .then(r => r.text())
    .then(msg => {
      const el = document.getElementById('emailMsg');
      el.textContent = msg === 'OK' ? '사용 가능한 이메일입니다.' : '이미 사용 중인 이메일입니다.';
      el.className = 'form-text ' + (msg === 'OK' ? 'text-success' : 'text-danger');
    });
}
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
