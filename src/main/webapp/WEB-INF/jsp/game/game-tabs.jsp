<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<div class="ow-game-tabs mb-4">
  <a href="${pageContext.request.contextPath}/game/heroes"
     class="ow-game-tab ${pageTitle == '영웅 정보' ? 'active' : ''}">영웅</a>
  <a href="${pageContext.request.contextPath}/game/maps"
     class="ow-game-tab ${pageTitle == '맵 정보' ? 'active' : ''}">맵</a>
  <a href="${pageContext.request.contextPath}/game/patches"
     class="ow-game-tab ${pageTitle == '패치 노트' ? 'active' : ''}">패치 노트</a>
</div>
