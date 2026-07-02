package kr.ac.kopo.report.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.ac.kopo.report.dao.ReportDAO;
import kr.ac.kopo.report.vo.ReportVO;

@Service
public class ReportServiceImpl implements ReportService {

	@Autowired
	private ReportDAO reportDAO;

	@Override
	public boolean report(String targetType, int targetNo, String reporterId, String reason) {
		// 게시글/댓글은 같은 대상 중복 신고 방지. 쪽지(M)는 대화별로 매번 접수 허용
		if (!"M".equals(targetType) && reportDAO.existsReport(targetType, targetNo, reporterId) > 0) {
			return false;
		}
		ReportVO r = new ReportVO();
		r.setTargetType(targetType);
		r.setTargetNo(targetNo);
		r.setReporterId(reporterId);
		r.setReason(reason);
		reportDAO.insert(r);
		return true;
	}

	@Override
	public List<ReportVO> getReports() {
		return reportDAO.selectAll();
	}

	@Override
	public ReportVO getReport(int no) {
		return reportDAO.selectByNo(no);
	}

	@Override
	public void resolve(int no) {
		reportDAO.updateStatus(no, "RESOLVED");
	}
}
