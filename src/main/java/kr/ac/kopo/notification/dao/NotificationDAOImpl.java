package kr.ac.kopo.notification.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.ac.kopo.notification.vo.NotificationVO;

@Repository
public class NotificationDAOImpl implements NotificationDAO {

	@Autowired
	private SqlSessionTemplate sqlSessionTemplate;

	@Override
	public void insert(NotificationVO notification) {
		sqlSessionTemplate.insert("notification.dao.NotificationDAO.insert", notification);
	}

	@Override
	public List<NotificationVO> selectByUser(String userId) {
		return sqlSessionTemplate.selectList("notification.dao.NotificationDAO.selectByUser", userId);
	}

	@Override
	public void markAllRead(String userId) {
		sqlSessionTemplate.update("notification.dao.NotificationDAO.markAllRead", userId);
	}

	@Override
	public int countUnread(String userId) {
		Integer cnt = sqlSessionTemplate.selectOne("notification.dao.NotificationDAO.countUnread", userId);
		return cnt == null ? 0 : cnt;
	}
}
