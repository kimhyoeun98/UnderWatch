package kr.ac.kopo.report.dao;

import java.util.List;

import kr.ac.kopo.report.vo.ReportVO;

public interface ReportDAO {

	int existsReport(String targetType, int targetNo, String reporterId);
	void insert(ReportVO report);
	List<ReportVO> selectAll();
	ReportVO selectByNo(int no);
	void updateStatus(int no, String status);
}
