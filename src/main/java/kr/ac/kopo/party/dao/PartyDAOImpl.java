package kr.ac.kopo.party.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.ac.kopo.party.vo.PartyVO;

@Repository
public class PartyDAOImpl implements PartyDAO {

	@Autowired
	private SqlSessionTemplate sqlSessionTemplate;

	@Override
	public List<PartyVO> selectList(PartyVO filter) {
		return sqlSessionTemplate.selectList("party.dao.PartyDAO.selectList", filter);
	}

	@Override
	public PartyVO selectOne(int no) {
		return sqlSessionTemplate.selectOne("party.dao.PartyDAO.selectOne", no);
	}

	@Override
	public void insert(PartyVO party) {
		sqlSessionTemplate.insert("party.dao.PartyDAO.insert", party);
	}

	@Override
	public void updateStatus(int no, String status) {
		Map<String, Object> p = new HashMap<>();
		p.put("no", no);
		p.put("status", status);
		sqlSessionTemplate.update("party.dao.PartyDAO.updateStatus", p);
	}
}
