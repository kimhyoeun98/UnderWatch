package kr.ac.kopo.visit.service;

import java.util.List;

import kr.ac.kopo.visit.vo.VisitVO;

public interface VisitService {

	void recordVisit();
	List<VisitVO> getRecent();
}
