<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="로그인" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container py-5">
  <div class="row justify-content-center">
    <div class="col-md-5">
      <div class="card shadow">
        <div class="card-header ow-auth-head py-3">
          <h4 class="mb-0">&#9670; 로그인</h4>
        </div>
        <div class="card-body p-4">

          <c:if test="${not empty errorMsg}">
            <div class="alert alert-danger">${errorMsg}</div>
          </c:if>
          <c:if test="${param.registered eq 'true'}">
            <div class="alert alert-success">회원가입이 완료되었습니다. 로그인해주세요.</div>
          </c:if>

          <form method="post" action="${pageContext.request.contextPath}/member/loginProc">
            <div class="mb-3">
              <label class="form-label">아이디</label>
              <input type="text" name="id" class="form-control" required autofocus>
            </div>
            <div class="mb-3">
              <label class="form-label">비밀번호</label>
              <input type="password" name="password" class="form-control" required>
            </div>
            <button type="submit" class="btn btn-warning w-100">로그인</button>
          </form>

          <!-- M-09 얼굴 로그인 -->
          <a href="${pageContext.request.contextPath}/member/face/login"
             class="btn btn-outline-secondary w-100 mt-2">&#128100; 얼굴로 로그인</a>

          <hr style="border-color: var(--ow-border);">

          <!-- M-03 소셜 로그인 (키가 등록된 제공자만 노출) -->
          <c:if test="${not empty oauthProviders}">
            <p class="text-center text-muted small mb-2">SNS 계정으로 간편 로그인</p>
            <div class="d-grid gap-2">
              <c:if test="${oauthProviders.contains('kakao')}">
                <a href="${pageContext.request.contextPath}/oauth2/authorization/kakao"
                   class="btn fw-bold" style="background:#FEE500;color:#191600;">카카오로 로그인</a>
              </c:if>
              <c:if test="${oauthProviders.contains('naver')}">
                <a href="${pageContext.request.contextPath}/oauth2/authorization/naver"
                   class="btn fw-bold text-white" style="background:#03C75A;">네이버로 로그인</a>
              </c:if>
              <c:if test="${oauthProviders.contains('google')}">
                <a href="${pageContext.request.contextPath}/oauth2/authorization/google"
                   class="btn fw-bold border" style="background:#fff;color:#333;">Google로 로그인</a>
              </c:if>
            </div>
            <hr style="border-color: var(--ow-border);">
          </c:if>
          <div class="text-center">
            <a href="${pageContext.request.contextPath}/member/register">회원가입</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
