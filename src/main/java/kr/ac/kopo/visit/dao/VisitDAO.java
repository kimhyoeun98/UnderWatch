package kr.ac.kopo.visit.dao;

import java.util.List;

import kr.ac.kopo.visit.vo.VisitVO;

public interface VisitDAO {

	void recordVisit();              // 오늘 방문수 +1 (MERGE)
	List<VisitVO> selectRecent();    // 최근 일별 통계
}
