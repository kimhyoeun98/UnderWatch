<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="오버워치 커뮤니티" scope="request" />
<c:set var="showHero"  value="true" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container py-4">
  <div class="row g-3">

    <!-- ===== 좌측: 검색 + 게시글 목록 ===== -->
    <div class="col-lg-9">

      <div class="card shadow-sm">
        <div class="card-header d-flex justify-content-between align-items-center">
          <span class="ow-section-title">실시간 게시글</span>
          <div class="d-flex gap-2">
            <c:if test="${pageContext.request.userPrincipal != null}">
              <a href="${pageContext.request.contextPath}/board/write" class="btn btn-warning btn-sm">글쓰기</a>
            </c:if>
            <a href="${pageContext.request.contextPath}/board/list" class="btn btn-outline-secondary btn-sm">전체보기</a>
          </div>
        </div>
        <div class="card-body p-0">
          <table class="table table-hover align-middle mb-0">
            <thead>
              <tr>
                <th style="width:100px">카테고리</th>
                <th>제목</th>
                <th style="width:110px">작성자</th>
                <th style="width:80px">날짜</th>
                <th style="width:60px">조회</th>
              </tr>
            </thead>
            <tbody>
              <c:choose>
                <c:when test="${empty recentPosts}">
                  <tr><td colspan="5" class="text-center py-5 text-muted">아직 게시글이 없습니다. 첫 글을 남겨보세요!</td></tr>
                </c:when>
                <c:otherwise>
                  <c:forEach var="post" items="${recentPosts}">
                    <tr>
                      <td><span class="badge bg-secondary">${post.categoryName}</span></td>
                      <td>
                        <a href="${pageContext.request.contextPath}/board/detail?no=${post.no}" class="text-decoration-none">
                          <c:out value="${post.title}"/>
                          <c:if test="${post.commentCnt > 0}">
                            <span class="text-primary ms-1 small">[${post.commentCnt}]</span>
                          </c:if>
                        </a>
                      </td>
                      <td class="small"><c:out value="${post.writerNickname}"/></td>
                      <td class="text-muted small">${post.regDate}</td>
                      <td class="text-muted small">${post.viewCnt}</td>
                    </tr>
                  </c:forEach>
                </c:otherwise>
              </c:choose>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 게시글 검색 (카드 밑) -->
      <form method="get" action="${pageContext.request.contextPath}/board/list" class="row g-2 mt-3">
        <div class="col-auto">
          <select name="searchType" class="form-select form-select-sm">
            <option value="titleContent">제목+내용</option>
            <option value="title">제목</option>
            <option value="content">내용</option>
            <option value="writer">작성자</option>
          </select>
        </div>
        <div class="col">
          <input type="search" name="keyword" class="form-control form-control-sm" placeholder="게시글 검색">
        </div>
        <div class="col-auto">
          <button type="submit" class="btn btn-warning btn-sm px-3">검색</button>
        </div>
      </form>
    </div>

    <!-- ===== 우측: 사이드바 ===== -->
    <div class="col-lg-3">

      <!-- 로그인 / 사용자 박스 -->
      <div class="card shadow-sm mb-3">
        <c:choose>
          <c:when test="${pageContext.request.userPrincipal == null}">
            <div class="card-body">
              <form method="post" action="${pageContext.request.contextPath}/member/loginProc">
                <div class="mb-2">
                  <input type="text" name="id" class="form-control form-control-sm" placeholder="아이디" required>
                </div>
                <div class="mb-2">
                  <input type="password" name="password" class="form-control form-control-sm" placeholder="비밀번호" required>
                </div>
                <button type="submit" class="btn btn-warning btn-sm w-100">로그인</button>
              </form>

              <!-- M-09 얼굴 로그인 -->
              <a href="${pageContext.request.contextPath}/member/face/login"
                 class="btn btn-outline-secondary btn-sm w-100 mt-2">&#128100; 얼굴로 로그인</a>

              <!-- M-03 소셜 로그인 (키가 등록된 제공자만 노출) -->
              <c:if test="${not empty oauthProviders}">
                <div class="text-center text-muted mt-2" style="font-size:.75rem;">SNS 간편 로그인</div>
                <div class="d-grid gap-1 mt-1">
                  <c:if test="${oauthProviders.contains('kakao')}">
                    <a href="${pageContext.request.contextPath}/oauth2/authorization/kakao"
                       class="btn btn-sm fw-bold" style="background:#FEE500;color:#191600;">카카오로 로그인</a>
                  </c:if>
                  <c:if test="${oauthProviders.contains('naver')}">
                    <a href="${pageContext.request.contextPath}/oauth2/authorization/naver"
                       class="btn btn-sm fw-bold text-white" style="background:#03C75A;">네이버로 로그인</a>
                  </c:if>
                  <c:if test="${oauthProviders.contains('google')}">
                    <a href="${pageContext.request.contextPath}/oauth2/authorization/google"
                       class="btn btn-sm fw-bold border" style="background:#fff;color:#333;">Google로 로그인</a>
                  </c:if>
                </div>
              </c:if>

              <div class="d-flex justify-content-center gap-2 mt-2 small">
                <a href="${pageContext.request.contextPath}/member/register">회원가입</a>
              </div>
            </div>
          </c:when>
          <c:otherwise>
            <div class="card-body text-center">
              <p class="mb-2">
                <strong class="text-warning">${pageContext.request.userPrincipal.name}</strong> 님
              </p>
              <div class="d-grid gap-2">
                <a href="${pageContext.request.contextPath}/member/mypage" class="btn btn-outline-secondary btn-sm">마이페이지</a>
                <a href="${pageContext.request.contextPath}/member/logout" class="btn btn-outline-secondary btn-sm">로그아웃</a>
              </div>
            </div>
          </c:otherwise>
        </c:choose>
      </div>

      <!-- 구인구직 (모집 중) -->
      <div class="card shadow-sm mb-3">
        <div class="card-header d-flex justify-content-between align-items-center">
          <span class="ow-section-title">구인구직</span>
          <a href="${pageContext.request.contextPath}/party/list" class="btn btn-outline-secondary btn-sm">더보기</a>
        </div>
        <ul class="list-group list-group-flush">
          <c:choose>
            <c:when test="${empty recentParties}">
              <li class="list-group-item text-muted text-center small">모집 중인 글이 없습니다.</li>
            </c:when>
            <c:otherwise>
              <c:forEach var="p" items="${recentParties}" varStatus="st">
                <c:if test="${st.index < 6}">
                  <li class="list-group-item small">
                    <a href="${pageContext.request.contextPath}/party/list" class="text-decoration-none d-block">
                      <c:if test="${not empty p.roleType}"><span class="badge bg-secondary">${p.roleType}</span></c:if>
                      <c:out value="${p.title}"/>
                    </a>
                  </li>
                </c:if>
              </c:forEach>
            </c:otherwise>
          </c:choose>
        </ul>
      </div>

      <!-- 안내 박스 -->
      <div class="card shadow-sm">
        <div class="card-header"><span class="ow-section-title">커뮤니티 안내</span></div>
        <div class="card-body small text-muted">
          상단 메뉴에서 카테고리별 게시판으로 이동할 수 있습니다.
          오버워치 관련 정보와 팁을 자유롭게 공유해 주세요.
        </div>
      </div>

    </div>

  </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
