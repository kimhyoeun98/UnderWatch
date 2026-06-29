package kr.ac.kopo.party.service;

import java.util.List;

import kr.ac.kopo.party.vo.PartyVO;

public interface PartyService {

	List<PartyVO> getList(PartyVO filter);
	PartyVO getDetail(int no);
	void write(PartyVO party);
	void toggleStatus(int no, String requesterId);   // P-03 모집중<->완료 (본인만)
}
