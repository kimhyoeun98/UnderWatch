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
		if (reportDAO.existsReport(targetType, targetNo, reporterId) > 0) {
			return false;   // 같은 대상 중복 신고 방지
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
	public void resolve(int no) {
		reportDAO.updateStatus(no, "RESOLVED");
	}
}
