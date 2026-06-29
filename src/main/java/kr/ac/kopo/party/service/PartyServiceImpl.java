package kr.ac.kopo.party.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.ac.kopo.party.dao.PartyDAO;
import kr.ac.kopo.party.vo.PartyVO;

@Service
public class PartyServiceImpl implements PartyService {

	@Autowired
	private PartyDAO partyDAO;

	@Override
	public List<PartyVO> getList(PartyVO filter) {
		return partyDAO.selectList(filter);
	}

	@Override
	public PartyVO getDetail(int no) {
		return partyDAO.selectOne(no);
	}

	@Override
	public void write(PartyVO party) {
		partyDAO.insert(party);
	}

	@Override
	public void toggleStatus(int no, String requesterId) {
		PartyVO party = partyDAO.selectOne(no);
		if (party == null || !party.getWriterId().equals(requesterId)) {
			return; // 본인 글만
		}
		String next = "RECRUITING".equals(party.getStatus()) ? "CLOSED" : "RECRUITING";
		partyDAO.updateStatus(no, next);
	}
}
