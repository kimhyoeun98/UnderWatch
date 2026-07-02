package kr.ac.kopo.message.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import kr.ac.kopo.message.vo.ConversationVO;
import kr.ac.kopo.message.vo.MessageVO;

public interface MessageDAO {

	void insert(MessageVO message);
	List<MessageVO> selectReceived(String receiverId);
	List<MessageVO> selectSent(String senderId);
	void markRead(int no, String receiverId);
	void markAllRead(String myId, String partnerId);
	int countUnread(String receiverId);
	List<ConversationVO> selectConversations(String myId);
	List<MessageVO> selectThread(String myId, String partnerId);
	List<MessageVO> selectThreadForAdmin(@Param("userA") String userA, @Param("userB") String userB);
	void deleteForMe(int no, String myId);
	void leaveConversation(String myId, String partnerId);
}
