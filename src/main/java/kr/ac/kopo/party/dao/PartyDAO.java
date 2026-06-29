package kr.ac.kopo.party.dao;

import java.util.List;

import kr.ac.kopo.party.vo.PartyVO;

public interface PartyDAO {

	List<PartyVO> selectList(PartyVO filter);
	PartyVO selectOne(int no);
	void insert(PartyVO party);
	void updateStatus(int no, String status);
}
