<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="${partnerNickname}와의 대화" scope="request" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<style>
.chat-wrap {
  max-width: 640px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 130px);
}
.chat-header {
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #dee2e6;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #b2c7d9;
}

/* 상대방 메시지 (왼쪽) */
.msg-their {
  display: flex;
  align-items: flex-end;
  gap: 6px;
  justify-content: flex-start;
}
.msg-their .msg-bubble {
  background: #fff;
  color: #212529;
  border-radius: 0 18px 18px 18px;
  padding: 9px 13px;
  max-width: 60%;
  font-size: .92rem;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
  box-shadow: 0 1px 2px rgba(0,0,0,.12);
}
.msg-their .msg-time {
  font-size: .7rem;
  color: rgba(0,0,0,.45);
  margin-bottom: 2px;
  flex-shrink: 0;
}

/* 내 메시지 (오른쪽) */
.msg-mine {
  display: flex;
  align-items: flex-end;
  gap: 6px;
  justify-content: flex-end;
}
.msg-mine .msg-bubble {
  background: #fee500;
  color: #191919;
  border-radius: 18px 0 18px 18px;
  padding: 9px 13px;
  max-width: 60%;
  font-size: .92rem;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}
.msg-mine .msg-time {
  font-size: .7rem;
  color: rgba(0,0,0,.45);
  margin-bottom: 2px;
  flex-shrink: 0;
}

/* 삭제 버튼 */
.msg-delete-btn {
  display: none;
  background: none;
  border: none;
  color: rgba(0,0,0,.35);
  font-size: .85rem;
  cursor: pointer;
  padding: 0 3px;
  line-height: 1;
  align-self: center;
  flex-shrink: 0;
}
.msg-delete-btn:hover { color: #e74c3c; }
.msg-mine:hover  .msg-delete-btn,
.msg-their:hover .msg-delete-btn { display: block; }

/* 아바타 */
.msg-avatar {
  width: 36px; height: 36px;
  border-radius: 50%;
  background: #f8a100;
  color: #fff;
  font-weight: 700;
  font-size: .85rem;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  align-self: flex-start;
}

/* 입력창 */
.chat-input-area {
  padding: 10px 12px;
  background: #fff;
  border-top: 1px solid #dee2e6;
  flex-shrink: 0;
}
.chat-input-area form {
  display: flex;
  gap: 8px;
  align-items: flex-end;
}
.chat-input-area textarea {
  flex: 1;
  resize: none;
  border-radius: 20px;
  padding: 8px 14px;
  font-size: .92rem;
  border: 1px solid #dee2e6;
  line-height: 1.4;
  max-height: 100px;
  overflow-y: auto;
}
.chat-input-area textarea:focus {
  outline: none;
  border-color: #f8a100;
  box-shadow: 0 0 0 2px rgba(248,161,0,.2);
}
.chat-input-area button {
  border-radius: 20px;
  padding: 8px 18px;
  font-weight: 600;
  flex-shrink: 0;
}
</style>

<div class="container-fluid py-3">
  <div class="chat-wrap">

    <!-- 헤더 -->
    <div class="chat-header">
      <a href="${pageContext.request.contextPath}/message" style="text-decoration:none; color:#555; font-size:1.2rem;">&#8592;</a>
      <div class="msg-avatar" style="width:36px;height:36px;font-size:.85rem;">${fn:substring(partnerNickname, 0, 1)}</div>
      <span class="fw-semibold flex-grow-1"><c:out value="${partnerNickname}"/></span>
      <form id="leaveForm" method="post" action="${pageContext.request.contextPath}/message/leave" style="margin:0">
        <input type="hidden" name="partnerId" value="${partnerId}">
        <button type="submit" class="btn btn-outline-secondary btn-sm"
                onclick="return leaveChat(this.form)">
          나가기
        </button>
      </form>
    </div>

    <!-- 메시지 목록 -->
    <div class="chat-messages" id="chatMessages">
      <c:choose>
        <c:when test="${empty messages}">
          <div class="text-center my-auto" style="color:rgba(0,0,0,.45); font-size:.9rem;">
            아직 주고받은 메시지가 없습니다.<br>먼저 말을 걸어보세요!
          </div>
        </c:when>
        <c:otherwise>
          <c:forEach var="m" items="${messages}">
            <c:choose>
              <c:when test="${m.senderId eq myId}">
                <%-- 내 메시지: 오른쪽 --%>
                <div class="msg-mine">
                  <form method="post" action="${pageContext.request.contextPath}/message/delete" style="display:contents">
                    <input type="hidden" name="no"        value="${m.no}">
                    <input type="hidden" name="partnerId" value="${partnerId}">
                    <button type="submit" class="msg-delete-btn" title="삭제"
                            onclick="return confirm('이 메시지를 삭제하시겠습니까?\n(상대방에게는 그대로 남습니다)')">✕</button>
                  </form>
                  <span class="msg-time">${m.regDate}</span>
                  <div class="msg-bubble"><c:out value="${m.content}"/></div>
                </div>
              </c:when>
              <c:otherwise>
                <%-- 상대 메시지: 왼쪽 --%>
                <div class="msg-their">
                  <div class="msg-avatar">${fn:substring(partnerNickname, 0, 1)}</div>
                  <div>
                    <div style="font-size:.78rem; font-weight:600; color:#333; margin-bottom:3px;">
                      <c:out value="${m.senderNickname}"/>
                    </div>
                    <div style="display:flex; align-items:flex-end; gap:6px;">
                      <div class="msg-bubble"><c:out value="${m.content}"/></div>
                      <span class="msg-time">${m.regDate}</span>
                      <form method="post" action="${pageContext.request.contextPath}/message/delete" style="display:contents">
                        <input type="hidden" name="no"        value="${m.no}">
                        <input type="hidden" name="partnerId" value="${partnerId}">
                        <button type="submit" class="msg-delete-btn" title="삭제"
                                onclick="return confirm('이 메시지를 삭제하시겠습니까?\n(상대방에게는 그대로 남습니다)')">✕</button>
                      </form>
                    </div>
                  </div>
                </div>
              </c:otherwise>
            </c:choose>
          </c:forEach>
        </c:otherwise>
      </c:choose>
    </div>

    <!-- 입력창 -->
    <div class="chat-input-area">
      <form method="post" action="${pageContext.request.contextPath}/message/write" id="chatForm">
        <input type="hidden" name="receiverId" value="${partnerId}">
        <input type="hidden" name="redirectTo"  value="chat">
        <textarea name="content" id="chatInput" rows="1"
                  placeholder="메시지를 입력하세요…" required></textarea>
        <button type="submit" class="btn btn-warning">전송</button>
      </form>
    </div>

  </div>
</div>

<script>
var ctx = '${pageContext.request.contextPath}';
var partnerId = '${partnerId}';

// R-03 대화 나가기: 나가기 전에 신고 여부를 물어본다
function leaveChat(form) {
  if (!confirm('대화방을 나가시겠습니까?\n(상대방의 대화 내용은 유지됩니다)')) {
    return false;
  }
  if (confirm('나가기 전에 이 대화를 신고하시겠습니까?')) {
    var reason = prompt('신고 사유를 입력하세요 (선택)') || '';
    var body = new URLSearchParams();
    body.append('targetType', 'M');
    body.append('partnerId', partnerId);
    body.append('reason', reason);
    // 신고 접수 후 대화방 나가기 (신고 실패해도 나가기는 진행)
    fetch(ctx + '/report', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: body.toString()
    }).then(function () { form.submit(); }, function () { form.submit(); });
    return false;   // fetch 완료 후 직접 submit
  }
  return true;      // 신고 없이 바로 나가기
}

// 최하단 스크롤
var chatEl = document.getElementById('chatMessages');
if (chatEl) chatEl.scrollTop = chatEl.scrollHeight;

// Enter 전송 / Shift+Enter 줄바꿈
document.getElementById('chatInput').addEventListener('keydown', function(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault();
    document.getElementById('chatForm').requestSubmit();
  }
});
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
