package kr.ac.kopo.visit.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.ac.kopo.visit.vo.VisitVO;

@Repository
public class VisitDAOImpl implements VisitDAO {

	@Autowired
	private SqlSessionTemplate sqlSessionTemplate;

	@Override
	public void recordVisit() {
		sqlSessionTemplate.update("visit.dao.VisitDAO.recordVisit");
	}

	@Override
	public List<VisitVO> selectRecent() {
		return sqlSessionTemplate.selectList("visit.dao.VisitDAO.selectRecent");
	}
}
