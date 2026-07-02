package kr.ac.kopo.message.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.ac.kopo.message.vo.ConversationVO;
import kr.ac.kopo.message.vo.MessageVO;

@Repository
public class MessageDAOImpl implements MessageDAO {

	@Autowired
	private SqlSessionTemplate sqlSessionTemplate;

	@Override
	public void insert(MessageVO message) {
		sqlSessionTemplate.insert("message.dao.MessageDAO.insert", message);
	}

	@Override
	public List<MessageVO> selectReceived(String receiverId) {
		return sqlSessionTemplate.selectList("message.dao.MessageDAO.selectReceived", receiverId);
	}

	@Override
	public List<MessageVO> selectSent(String senderId) {
		return sqlSessionTemplate.selectList("message.dao.MessageDAO.selectSent", senderId);
	}

	@Override
	public void markRead(int no, String receiverId) {
		Map<String, Object> p = new HashMap<>();
		p.put("no", no);
		p.put("receiverId", receiverId);
		sqlSessionTemplate.update("message.dao.MessageDAO.markRead", p);
	}

	@Override
	public void markAllRead(String myId, String partnerId) {
		Map<String, Object> p = new HashMap<>();
		p.put("myId", myId);
		p.put("partnerId", partnerId);
		sqlSessionTemplate.update("message.dao.MessageDAO.markAllRead", p);
	}

	@Override
	public int countUnread(String receiverId) {
		Integer cnt = sqlSessionTemplate.selectOne("message.dao.MessageDAO.countUnread", receiverId);
		return cnt == null ? 0 : cnt;
	}

	@Override
	public List<ConversationVO> selectConversations(String myId) {
		return sqlSessionTemplate.selectList("message.dao.MessageDAO.selectConversations", myId);
	}

	@Override
	public List<MessageVO> selectThread(String myId, String partnerId) {
		Map<String, Object> p = new HashMap<>();
		p.put("myId", myId);
		p.put("partnerId", partnerId);
		return sqlSessionTemplate.selectList("message.dao.MessageDAO.selectThread", p);
	}

	@Override
	public List<MessageVO> selectThreadForAdmin(String userA, String userB) {
		Map<String, Object> p = new HashMap<>();
		p.put("userA", userA);
		p.put("userB", userB);
		return sqlSessionTemplate.selectList("message.dao.MessageDAO.selectThreadForAdmin", p);
	}

	@Override
	public void deleteForMe(int no, String myId) {
		Map<String, Object> p = new HashMap<>();
		p.put("no", no);
		p.put("myId", myId);
		sqlSessionTemplate.update("message.dao.MessageDAO.deleteForMe", p);
	}

	@Override
	public void leaveConversation(String myId, String partnerId) {
		Map<String, Object> p = new HashMap<>();
		p.put("myId", myId);
		p.put("partnerId", partnerId);
		sqlSessionTemplate.update("message.dao.MessageDAO.leaveConversation", p);
	}
}
