package kr.ac.kopo.notification.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.ac.kopo.notification.dao.NotificationDAO;
import kr.ac.kopo.notification.vo.NotificationVO;
import kr.ac.kopo.realtime.NotifyWebSocketHandler;

@Service
public class NotificationServiceImpl implements NotificationService {

	@Autowired
	private NotificationDAO notificationDAO;

	@Autowired
	private NotifyWebSocketHandler push;

	@Override
	public void notify(String userId, String content, String link) {
		NotificationVO n = new NotificationVO();
		n.setUserId(userId);
		n.setContent(content);
		n.setLink(link);
		notificationDAO.insert(n);
		// 대상자가 접속 중이면 안 읽은 알림 배지를 즉시 갱신
		push.sendBadge(userId, "notification", notificationDAO.countUnread(userId));
	}

	@Override
	public List<NotificationVO> getList(String userId) {
		return notificationDAO.selectByUser(userId);
	}

	@Override
	public void readAll(String userId) {
		notificationDAO.markAllRead(userId);
	}

	@Override
	public int countUnread(String userId) {
		return notificationDAO.countUnread(userId);
	}
}
