package kr.ac.kopo.notification.service;

import java.util.List;

import kr.ac.kopo.notification.vo.NotificationVO;

public interface NotificationService {

	void notify(String userId, String content, String link);
	List<NotificationVO> getList(String userId);
	void readAll(String userId);
	int countUnread(String userId);
}
