<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="${board.title}" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container py-4">

  <c:if test="${not empty reportMsg}">
    <div class="alert alert-info">${reportMsg}</div>
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
      <div class="d-flex justify-content-between text-muted small mb-3">
        <span>작성자: <strong class="text-warning"><c:out value="${board.writerNickname}"/></strong></span>
        <span>조회 ${board.viewCnt}</span>
      </div>
      <div class="board-content rounded p-3">
        <c:out value="${board.content}"/>
      </div>
      <!-- B-04 첨부 이미지 -->
      <c:if test="${not empty board.imageStored}">
        <div class="text-center my-3">
          <img src="${pageContext.request.contextPath}/upload/${board.imageStored}"
               class="img-fluid rounded" style="max-height:500px" alt="첨부 이미지">
          <div class="mt-1">
            <a href="${pageContext.request.contextPath}/board/download?no=${board.no}"
               class="btn btn-outline-secondary btn-sm">이미지 다운로드</a>
          </div>
        </div>
      </c:if>
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

      <c:if test="${pageContext.request.userPrincipal != null}">
        <c:set var="loginId" value="${pageContext.request.userPrincipal.name}" />
        <c:if test="${board.writerId eq loginId or isAdmin}">
          <div>
            <c:if test="${board.writerId eq loginId}">
              <a href="${pageContext.request.contextPath}/board/edit?no=${board.no}" class="btn btn-outline-primary btn-sm">수정</a>
            </c:if>
            <form method="post" action="${pageContext.request.contextPath}/board/delete"
                  style="display:inline" onsubmit="return confirm('정말 삭제하시겠습니까?')">
              <input type="hidden" name="no" value="${board.no}">
              <button type="submit" class="btn btn-outline-danger btn-sm">삭제</button>
            </form>
          </div>
        </c:if>
      </c:if>
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
            <div class="mb-3 ${comment.isDeleted eq 'Y' ? 'text-muted' : ''}">
              <div class="d-flex justify-content-between">
                <strong class="text-warning"><c:out value="${comment.writerNickname}"/></strong>
                <small class="text-muted">${comment.regDate}</small>
              </div>
              <p class="mb-1"><c:out value="${comment.content}"/></p>
              <div class="d-flex gap-2">
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
                <div class="comment-reply mt-2 ${child.isDeleted eq 'Y' ? 'text-muted' : ''}">
                  <div class="d-flex justify-content-between">
                    <strong class="text-warning"><c:out value="${child.writerNickname}"/></strong>
                    <small class="text-muted">${child.regDate}</small>
                  </div>
                  <p class="mb-1"><c:out value="${child.content}"/></p>
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
function toggleReply(id) {
  const el = document.getElementById(id);
  el.style.display = el.style.display === 'none' ? 'block' : 'none';
}
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
