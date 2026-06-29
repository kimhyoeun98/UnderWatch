package kr.ac.kopo.visit.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.ac.kopo.visit.dao.VisitDAO;
import kr.ac.kopo.visit.vo.VisitVO;

@Service
public class VisitServiceImpl implements VisitService {

	@Autowired
	private VisitDAO visitDAO;

	@Override
	public void recordVisit() {
		visitDAO.recordVisit();
	}

	@Override
	public List<VisitVO> getRecent() {
		return visitDAO.selectRecent();
	}
}
