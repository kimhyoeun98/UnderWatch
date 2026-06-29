package kr.ac.kopo.report.service;

import java.util.List;

import kr.ac.kopo.report.vo.ReportVO;

public interface ReportService {

	boolean report(String targetType, int targetNo, String reporterId, String reason);   // false=중복신고
	List<ReportVO> getReports();
	void resolve(int no);
}
