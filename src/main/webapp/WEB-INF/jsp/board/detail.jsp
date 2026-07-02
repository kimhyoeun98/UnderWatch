<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="${board.title}" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container py-4">

  <c:if test="${not empty reportMsg}">
    <div class="alert alert-info">${reportMsg}</div>
  </c:if>
  <c:if test="${not empty editError}">
    <div class="alert alert-danger">${editError}</div>
  </c:if>

  <!-- 글 본문 -->
  <div class="card shadow-sm mb-4">
    <div class="card-header d-flex justify-content-between align-items-center">
      <div>
        <span class="badge bg-warning me-2">${board.categoryName}</span>
        <strong><c:out value="${board.title}"/></strong>
      </div>
      <small class="text-muted">${board.regDate}</small>
    </div>
    <div class="card-body">
      <div class="d-flex justify-content-between align-items-center text-muted small mb-3">
        <span>작성자: <strong class="text-warning"><c:out value="${board.writerNickname}"/></strong></span>
        <span class="d-flex align-items-center gap-2">
          <span>조회 ${board.viewCnt}</span>
          <!-- B-04 이미지 다운로드: 조회수 옆에 표시 -->
          <c:if test="${not empty board.imageStored}">
            <a href="${pageContext.request.contextPath}/board/download?no=${board.no}"
               class="btn btn-outline-secondary btn-sm py-0">이미지 다운로드</a>
          </c:if>
        </span>
      </div>
      <div class="board-content rounded p-3">
        <c:out value="${board.content}"/>
        <!-- B-04 첨부 이미지: 본문 박스 안에 표시 -->
        <c:if test="${not empty board.imageStored}">
          <div class="mt-3 text-center">
            <img src="${pageContext.request.contextPath}/upload/${board.imageStored}"
                 class="rounded" style="width:100%; max-width:480px; max-height:600px; object-fit:contain"
                 alt="첨부 이미지">
          </div>
        </c:if>
      </div>
      <!-- I-03 추천/비추천 -->
      <div class="text-center mt-3">
        <c:choose>
          <c:when test="${pageContext.request.userPrincipal != null}">
            <form method="post" action="${pageContext.request.contextPath}/board/like" style="display:inline">
              <input type="hidden" name="no" value="${board.no}">
              <button type="submit" class="btn btn-sm ${myVote eq 'L' ? 'btn-primary' : 'btn-outline-primary'}">
                추천 <span class="badge bg-secondary">${board.likeCnt}</span>
              </button>
            </form>
            <form method="post" action="${pageContext.request.contextPath}/board/dislike" style="display:inline">
              <input type="hidden" name="no" value="${board.no}">
              <button type="submit" class="btn btn-sm ${myVote eq 'D' ? 'btn-secondary' : 'btn-outline-secondary'}">
                비추천 <span class="badge bg-secondary">${board.dislikeCnt}</span>
              </button>
            </form>
            <c:if test="${board.writerId ne pageContext.request.userPrincipal.name}">
              <form method="post" action="${pageContext.request.contextPath}/report" style="display:inline"
                    onsubmit="var r=prompt('신고 사유를 입력하세요'); if(r===null) return false; this.reason.value=r; return true;">
                <input type="hidden" name="targetType" value="B">
                <input type="hidden" name="targetNo" value="${board.no}">
                <input type="hidden" name="boardNo" value="${board.no}">
                <input type="hidden" name="reason" value="">
                <button type="submit" class="btn btn-sm btn-outline-danger">신고</button>
              </form>
            </c:if>
          </c:when>
          <c:otherwise>
            <span class="text-muted small">추천 ${board.likeCnt} · 비추천 ${board.dislikeCnt}</span>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
    <div class="card-footer d-flex justify-content-between">
      <a href="${pageContext.request.contextPath}/board/list?currentPage=${param.currentPage}&categoryNo=${param.categoryNo}&searchType=${param.searchType}&keyword=${param.keyword}"
         class="btn btn-outline-secondary btn-sm">목록</a>

      <div>
        <c:choose>
          <%-- B-02 비로그인(게스트) 글: 비밀번호로 수정/삭제 --%>
          <c:when test="${empty board.writerId}">
            <button type="button" class="btn btn-outline-primary btn-sm" onclick="guestEdit(${board.no})">수정</button>
            <button type="button" class="btn btn-outline-danger btn-sm" onclick="guestDelete(${board.no})">삭제</button>
            <c:if test="${isAdmin}">
              <form method="post" action="${pageContext.request.contextPath}/board/delete"
                    style="display:inline" onsubmit="return confirm('관리자 권한으로 삭제하시겠습니까?')">
                <input type="hidden" name="no" value="${board.no}">
                <button type="submit" class="btn btn-danger btn-sm">관리자 삭제</button>
              </form>
            </c:if>
          </c:when>
          <%-- 회원 글 --%>
          <c:otherwise>
            <c:if test="${pageContext.request.userPrincipal != null}">
              <c:set var="loginId" value="${pageContext.request.userPrincipal.name}" />
              <c:if test="${board.writerId eq loginId or isAdmin}">
                <c:if test="${board.writerId eq loginId}">
                  <a href="${pageContext.request.contextPath}/board/edit?no=${board.no}" class="btn btn-outline-primary btn-sm">수정</a>
                </c:if>
                <form method="post" action="${pageContext.request.contextPath}/board/delete"
                      style="display:inline" onsubmit="return confirm('정말 삭제하시겠습니까?')">
                  <input type="hidden" name="no" value="${board.no}">
                  <button type="submit" class="btn btn-outline-danger btn-sm">삭제</button>
                </form>
              </c:if>
            </c:if>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </div>

  <!-- 댓글 목록 -->
  <div class="card shadow-sm mb-4">
    <div class="card-header"><span class="ow-section-title">댓글 ${fn:length(comments)}개</span></div>
    <div class="card-body">

      <c:choose>
        <c:when test="${empty comments}">
          <p class="text-muted text-center mb-0">첫 번째 댓글을 남겨보세요.</p>
        </c:when>
        <c:otherwise>
          <c:forEach var="comment" items="${comments}">
            <!-- 부모 댓글 -->
            <div id="comment-${comment.no}" class="mb-3 ${comment.isDeleted eq 'Y' ? 'text-muted' : ''}">
              <div class="d-flex justify-content-between">
                <strong class="text-warning"><c:out value="${comment.writerNickname}"/></strong>
                <small class="text-muted">${comment.regDate}</small>
              </div>
              <p class="mb-1"><c:out value="${comment.content}"/></p>
              <div class="d-flex gap-2 align-items-center">
                <%-- I-03 댓글 추천/비추천 --%>
                <c:if test="${comment.isDeleted ne 'Y'}">
                  <c:choose>
                    <c:when test="${pageContext.request.userPrincipal != null}">
                      <form method="post" action="${pageContext.request.contextPath}/comment/like" style="display:inline">
                        <input type="hidden" name="no" value="${comment.no}">
                        <input type="hidden" name="boardNo" value="${board.no}">
                        <button type="submit" class="btn btn-link btn-sm p-0 text-primary">▲ ${comment.likeCnt}</button>
                      </form>
                      <form method="post" action="${pageContext.request.contextPath}/comment/dislike" style="display:inline">
                        <input type="hidden" name="no" value="${comment.no}">
                        <input type="hidden" name="boardNo" value="${board.no}">
                        <button type="submit" class="btn btn-link btn-sm p-0 text-secondary">▼ ${comment.dislikeCnt}</button>
                      </form>
                    </c:when>
                    <c:otherwise>
                      <span class="text-muted small">▲ ${comment.likeCnt} · ▼ ${comment.dislikeCnt}</span>
                    </c:otherwise>
                  </c:choose>
                </c:if>
                <c:if test="${pageContext.request.userPrincipal != null}">
                  <c:if test="${comment.isDeleted ne 'Y'}">
                    <button class="btn btn-link btn-sm p-0 text-secondary"
                            onclick="toggleReply('reply-${comment.no}')">답글</button>
                  </c:if>
                </c:if>
                <c:if test="${pageContext.request.userPrincipal != null}">
                  <c:set var="loginId" value="${pageContext.request.userPrincipal.name}" />
                  <c:if test="${comment.writerId eq loginId and comment.isDeleted ne 'Y'}">
                    <form method="post" action="${pageContext.request.contextPath}/comment/delete">
                      <input type="hidden" name="no" value="${comment.no}">
                      <input type="hidden" name="boardNo" value="${board.no}">
                      <button type="submit" class="btn btn-link btn-sm p-0 text-danger"
                              onclick="return confirm('삭제하시겠습니까?')">삭제</button>
                    </form>
                  </c:if>
                  <c:if test="${comment.writerId ne loginId and comment.isDeleted ne 'Y'}">
                    <form method="post" action="${pageContext.request.contextPath}/report" style="display:inline"
                          onsubmit="var r=prompt('신고 사유'); if(r===null) return false; this.reason.value=r; return true;">
                      <input type="hidden" name="targetType" value="C">
                      <input type="hidden" name="targetNo" value="${comment.no}">
                      <input type="hidden" name="boardNo" value="${board.no}">
                      <input type="hidden" name="reason" value="">
                      <button type="submit" class="btn btn-link btn-sm p-0 text-secondary">신고</button>
                    </form>
                  </c:if>
                </c:if>
              </div>

              <!-- 대댓글 입력 폼 -->
              <c:if test="${pageContext.request.userPrincipal != null}">
                <div id="reply-${comment.no}" class="mt-2 comment-reply" style="display:none">
                  <form method="post" action="${pageContext.request.contextPath}/comment/write">
                    <input type="hidden" name="boardNo" value="${board.no}">
                    <input type="hidden" name="parentNo" value="${comment.no}">
                    <div class="input-group input-group-sm">
                      <input type="text" name="content" class="form-control" placeholder="답글 입력" required>
                      <button type="submit" class="btn btn-warning">등록</button>
                    </div>
                  </form>
                </div>
              </c:if>

              <!-- 대댓글 목록 -->
              <c:forEach var="child" items="${comment.children}">
                <div id="comment-${child.no}" class="comment-reply mt-2 ${child.isDeleted eq 'Y' ? 'text-muted' : ''}">
                  <div class="d-flex justify-content-between">
                    <strong class="text-warning"><c:out value="${child.writerNickname}"/></strong>
                    <small class="text-muted">${child.regDate}</small>
                  </div>
                  <p class="mb-1"><c:out value="${child.content}"/></p>
                  <div class="d-flex gap-2 align-items-center">
                  <%-- I-03 대댓글 추천/비추천 --%>
                  <c:if test="${child.isDeleted ne 'Y'}">
                    <c:choose>
                      <c:when test="${pageContext.request.userPrincipal != null}">
                        <form method="post" action="${pageContext.request.contextPath}/comment/like" style="display:inline">
                          <input type="hidden" name="no" value="${child.no}">
                          <input type="hidden" name="boardNo" value="${board.no}">
                          <button type="submit" class="btn btn-link btn-sm p-0 text-primary">▲ ${child.likeCnt}</button>
                        </form>
                        <form method="post" action="${pageContext.request.contextPath}/comment/dislike" style="display:inline">
                          <input type="hidden" name="no" value="${child.no}">
                          <input type="hidden" name="boardNo" value="${board.no}">
                          <button type="submit" class="btn btn-link btn-sm p-0 text-secondary">▼ ${child.dislikeCnt}</button>
                        </form>
                      </c:when>
                      <c:otherwise>
                        <span class="text-muted small">▲ ${child.likeCnt} · ▼ ${child.dislikeCnt}</span>
                      </c:otherwise>
                    </c:choose>
                  </c:if>
                  <c:if test="${pageContext.request.userPrincipal != null}">
                    <c:set var="loginId" value="${pageContext.request.userPrincipal.name}" />
                    <c:if test="${child.writerId eq loginId and child.isDeleted ne 'Y'}">
                      <form method="post" action="${pageContext.request.contextPath}/comment/delete">
                        <input type="hidden" name="no" value="${child.no}">
                        <input type="hidden" name="boardNo" value="${board.no}">
                        <button type="submit" class="btn btn-link btn-sm p-0 text-danger"
                                onclick="return confirm('삭제하시겠습니까?')">삭제</button>
                      </form>
                    </c:if>
                    <c:if test="${child.writerId ne loginId and child.isDeleted ne 'Y'}">
                      <form method="post" action="${pageContext.request.contextPath}/report" style="display:inline"
                            onsubmit="var r=prompt('신고 사유'); if(r===null) return false; this.reason.value=r; return true;">
                        <input type="hidden" name="targetType" value="C">
                        <input type="hidden" name="targetNo" value="${child.no}">
                        <input type="hidden" name="boardNo" value="${board.no}">
                        <input type="hidden" name="reason" value="">
                        <button type="submit" class="btn btn-link btn-sm p-0 text-secondary">신고</button>
                      </form>
                    </c:if>
                  </c:if>
                  </div>
                </div>
              </c:forEach>
            </div>
            <hr class="my-2" style="border-color: var(--ow-border);">
          </c:forEach>
        </c:otherwise>
      </c:choose>

    </div>

    <!-- 댓글 작성 -->
    <div class="card-footer">
      <c:if test="${pageContext.request.userPrincipal != null}">
        <form method="post" action="${pageContext.request.contextPath}/comment/write">
          <input type="hidden" name="boardNo" value="${board.no}">
          <div class="input-group">
            <input type="text" name="content" class="form-control" placeholder="댓글을 입력하세요" required>
            <button type="submit" class="btn btn-warning">등록</button>
          </div>
        </form>
      </c:if>
      <c:if test="${pageContext.request.userPrincipal == null}">
        <p class="text-muted mb-0 text-center">
          <a href="${pageContext.request.contextPath}/member/login">로그인</a> 후 댓글을 작성할 수 있습니다.
        </p>
      </c:if>
    </div>
  </div>

</div>

<script>
var ctx = '${pageContext.request.contextPath}';
function toggleReply(id) {
  const el = document.getElementById(id);
  el.style.display = el.style.display === 'none' ? 'block' : 'none';
}
// B-02 게스트 글: 비밀번호 확인 후 수정/삭제
function guestEdit(no) {
  var pw = prompt('글 비밀번호를 입력하세요');
  if (pw === null) return;
  location.href = ctx + '/board/edit?no=' + no + '&guestPassword=' + encodeURIComponent(pw);
}
function guestDelete(no) {
  var pw = prompt('글 비밀번호를 입력하세요');
  if (pw === null) return;
  if (!confirm('정말 삭제하시겠습니까?')) return;
  var f = document.createElement('form');
  f.method = 'post';
  f.action = ctx + '/board/delete';
  var i1 = document.createElement('input'); i1.type = 'hidden'; i1.name = 'no'; i1.value = no;
  var i2 = document.createElement('input'); i2.type = 'hidden'; i2.name = 'guestPassword'; i2.value = pw;
  f.appendChild(i1); f.appendChild(i2);
  document.body.appendChild(f);
  f.submit();
}
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
