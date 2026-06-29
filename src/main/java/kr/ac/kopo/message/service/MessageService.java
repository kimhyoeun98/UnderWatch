package kr.ac.kopo.message.service;

import java.util.List;

import kr.ac.kopo.message.vo.ConversationVO;
import kr.ac.kopo.message.vo.MessageVO;

public interface MessageService {

	void send(String senderId, String receiverId, String content);
	List<MessageVO> getReceived(String receiverId);
	List<MessageVO> getSent(String senderId);
	void read(int no, String receiverId);
	int countUnread(String receiverId);
	List<ConversationVO> getConversations(String myId);
	List<MessageVO> getThread(String myId, String partnerId);
	void readAll(String myId, String partnerId);
	void deleteForMe(int no, String myId);
	void leaveConversation(String myId, String partnerId);
}
