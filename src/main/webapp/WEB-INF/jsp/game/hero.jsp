<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="영웅 상세" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container py-4">
  <c:choose>
    <c:when test="${empty hero}">
      <div class="card shadow-sm"><div class="card-body text-center text-muted py-5">
        영웅 정보를 불러올 수 없습니다.
      </div></div>
    </c:when>
    <c:otherwise>

      <c:set var="roleClass" value="${hero.role == 'tank' ? 'role-tank' : (hero.role == 'damage' ? 'role-damage' : (hero.role == 'support' ? 'role-support' : ''))}" />
      <c:set var="roleLabel" value="${hero.role == 'tank' ? '탱커' : (hero.role == 'damage' ? '딜러' : (hero.role == 'support' ? '지원가' : hero.role))}" />

      <!-- 상단 배너 -->
      <div class="ow-hero-detail ${roleClass}">
        <div class="ow-hero-detail-body">
          <div class="row g-3 align-items-center">
            <c:if test="${not empty hero.portrait}">
              <div class="col-auto">
                <img src="${hero.portrait}" alt="${hero.nameKr}" class="ow-hero-portrait">
              </div>
            </c:if>
            <div class="col">
              <div class="d-flex align-items-center gap-2 mb-1 flex-wrap">
                <h2 class="mb-0" style="color:var(--ow-orange);">${hero.nameKr}</h2>
                <c:if test="${not empty roleClass}">
                  <span class="ow-role-badge ${roleClass}">${roleLabel}</span>
                </c:if>
              </div>
              <c:if test="${not empty hero.location}">
                <p class="text-muted small mb-2">&#128205; ${hero.location}</p>
              </c:if>
              <c:if test="${not empty hero.hitpoints}">
                <div class="ow-hp">
                  <c:if test="${hero.hitpoints.health > 0}">
                    <span class="ow-hp-pill ow-hp-health">체력 ${hero.hitpoints.health}</span>
                  </c:if>
                  <c:if test="${hero.hitpoints.armor > 0}">
                    <span class="ow-hp-pill ow-hp-armor">방어구 ${hero.hitpoints.armor}</span>
                  </c:if>
                  <c:if test="${hero.hitpoints.shields > 0}">
                    <span class="ow-hp-pill ow-hp-shield">보호막 ${hero.hitpoints.shields}</span>
                  </c:if>
                  <c:if test="${not empty hero.hitpoints.total}">
                    <span class="ow-hp-pill">총 ${hero.hitpoints.total}</span>
                  </c:if>
                </div>
              </c:if>
            </div>
          </div>
          <c:if test="${not empty hero.description}">
            <p class="mt-3 mb-0">${hero.description}</p>
          </c:if>
        </div>
      </div>

      <!-- 배경 이야기 -->
      <c:if test="${not empty hero.story.summary}">
        <div class="card shadow-sm mb-4">
          <div class="card-header"><span class="ow-section-title">배경 이야기</span></div>
          <div class="card-body"><p class="mb-0">${hero.story.summary}</p></div>
        </div>
      </c:if>

      <!-- 스킬 -->
      <c:if test="${not empty hero.abilities}">
        <h5 class="ow-section-title mb-3">스킬</h5>
        <div class="row g-3">
          <c:forEach var="ab" items="${hero.abilities}">
            <div class="col-md-6">
              <div class="ow-ability-card d-flex gap-3 align-items-start">
                <c:if test="${not empty ab.icon}">
                  <img src="${ab.icon}" alt="">
                </c:if>
                <div>
                  <strong class="text-warning">${ab.name}</strong>
                  <p class="mb-0 small">${ab.description}</p>
                </div>
              </div>
            </div>
          </c:forEach>
        </div>
      </c:if>

    </c:otherwise>
  </c:choose>

  <a href="${pageContext.request.contextPath}/game/heroes" class="btn btn-outline-secondary btn-sm mt-4">&#8592; 영웅 목록</a>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
