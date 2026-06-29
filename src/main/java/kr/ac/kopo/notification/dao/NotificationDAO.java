package kr.ac.kopo.notification.dao;

import java.util.List;

import kr.ac.kopo.notification.vo.NotificationVO;

public interface NotificationDAO {

	void insert(NotificationVO notification);
	List<NotificationVO> selectByUser(String userId);
	void markAllRead(String userId);
	int countUnread(String userId);
}
