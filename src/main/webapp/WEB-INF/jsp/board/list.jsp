<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="게시판" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container py-4">

  <!-- 검색 바 -->
  <form method="get" action="${pageContext.request.contextPath}/board/list" class="row g-2 mb-3">
    <input type="hidden" name="categoryNo" value="${search.categoryNo}">
    <div class="col-auto">
      <select name="searchType" class="form-select form-select-sm">
        <option value="titleContent" ${search.searchType == 'titleContent' ? 'selected' : ''}>제목+내용</option>
        <option value="title"        ${search.searchType == 'title'        ? 'selected' : ''}>제목</option>
        <option value="content"      ${search.searchType == 'content'      ? 'selected' : ''}>내용</option>
        <option value="writer"       ${search.searchType == 'writer'       ? 'selected' : ''}>작성자</option>
      </select>
    </div>
    <div class="col">
      <input type="text" name="keyword" value="${search.keyword}" class="form-control form-control-sm" placeholder="검색어">
    </div>
    <div class="col-auto">
      <button type="submit" class="btn btn-warning btn-sm">검색</button>
    </div>
  </form>

  <!-- 게시글 테이블 -->
  <div class="card shadow-sm">
    <div class="card-body p-0">
      <table class="table table-hover align-middle mb-0">
        <thead>
          <tr>
            <th style="width:60px">번호</th>
            <th style="width:100px">카테고리</th>
            <th>제목</th>
            <th style="width:110px">작성자</th>
            <th style="width:100px">날짜</th>
            <th style="width:60px">조회</th>
          </tr>
        </thead>
        <tbody>
          <c:choose>
            <c:when test="${empty list}">
              <tr><td colspan="6" class="text-center py-4 text-muted">게시글이 없습니다.</td></tr>
            </c:when>
            <c:otherwise>
              <c:forEach var="post" items="${list}">
                <tr>
                  <td>${post.no}</td>
                  <td><span class="badge bg-secondary">${post.categoryName}</span></td>
                  <td>
                    <a href="${pageContext.request.contextPath}/board/detail?no=${post.no}&currentPage=${search.currentPage}&categoryNo=${search.categoryNo}&searchType=${search.searchType}&keyword=${search.keyword}"
                       class="text-decoration-none">
                      <c:out value="${post.title}"/>
                      <c:if test="${post.commentCnt > 0}">
                        <span class="text-primary ms-1 small">[${post.commentCnt}]</span>
                      </c:if>
                    </a>
                  </td>
                  <td><c:out value="${post.writerNickname}"/></td>
                  <td class="text-muted small">${post.regDate}</td>
                  <td class="text-muted">${post.viewCnt}</td>
                </tr>
              </c:forEach>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>
  </div>

  <!-- 페이징 + 글쓰기 -->
  <div class="d-flex justify-content-between align-items-center mt-3">

    <nav>
      <ul class="pagination pagination-sm mb-0">
        <c:if test="${search.hasPrevBlock}">
          <li class="page-item">
            <a class="page-link" href="${pageContext.request.contextPath}/board/list?currentPage=${search.startPage - 1}&categoryNo=${search.categoryNo}&searchType=${search.searchType}&keyword=${search.keyword}">&laquo;</a>
          </li>
        </c:if>
        <c:forEach begin="${search.startPage}" end="${search.endPage}" var="p">
          <li class="page-item ${p == search.currentPage ? 'active' : ''}">
            <a class="page-link" href="${pageContext.request.contextPath}/board/list?currentPage=${p}&categoryNo=${search.categoryNo}&searchType=${search.searchType}&keyword=${search.keyword}">${p}</a>
          </li>
        </c:forEach>
        <c:if test="${search.hasNextBlock}">
          <li class="page-item">
            <a class="page-link" href="${pageContext.request.contextPath}/board/list?currentPage=${search.endPage + 1}&categoryNo=${search.categoryNo}&searchType=${search.searchType}&keyword=${search.keyword}">&raquo;</a>
          </li>
        </c:if>
      </ul>
    </nav>

    <small class="text-muted">총 ${search.totalCount}건</small>

    <c:if test="${pageContext.request.userPrincipal != null}">
      <a href="${pageContext.request.contextPath}/board/write" class="btn btn-warning btn-sm">글쓰기</a>
    </c:if>
  </div>

</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
