package kr.ac.kopo.message.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.ac.kopo.message.dao.MessageDAO;
import kr.ac.kopo.message.vo.ConversationVO;
import kr.ac.kopo.message.vo.MessageVO;

@Service
public class MessageServiceImpl implements MessageService {

	@Autowired
	private MessageDAO messageDAO;

	@Override
	public void send(String senderId, String receiverId, String content) {
		MessageVO m = new MessageVO();
		m.setSenderId(senderId);
		m.setReceiverId(receiverId);
		m.setContent(content);
		messageDAO.insert(m);
	}

	@Override
	public List<MessageVO> getReceived(String receiverId) {
		return messageDAO.selectReceived(receiverId);
	}

	@Override
	public List<MessageVO> getSent(String senderId) {
		return messageDAO.selectSent(senderId);
	}

	@Override
	public void read(int no, String receiverId) {
		messageDAO.markRead(no, receiverId);
	}

	@Override
	public int countUnread(String receiverId) {
		return messageDAO.countUnread(receiverId);
	}

	@Override
	public List<ConversationVO> getConversations(String myId) {
		return messageDAO.selectConversations(myId);
	}

	@Override
	public List<MessageVO> getThread(String myId, String partnerId) {
		return messageDAO.selectThread(myId, partnerId);
	}

	@Override
	public void readAll(String myId, String partnerId) {
		messageDAO.markAllRead(myId, partnerId);
	}

	@Override
	public void deleteForMe(int no, String myId) {
		messageDAO.deleteForMe(no, myId);
	}

	@Override
	public void leaveConversation(String myId, String partnerId) {
		messageDAO.leaveConversation(myId, partnerId);
	}
}
