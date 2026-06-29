package kr.ac.kopo.report.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.ac.kopo.report.vo.ReportVO;

@Repository
public class ReportDAOImpl implements ReportDAO {

	@Autowired
	private SqlSessionTemplate sqlSessionTemplate;

	@Override
	public int existsReport(String targetType, int targetNo, String reporterId) {
		Map<String, Object> p = new HashMap<>();
		p.put("targetType", targetType);
		p.put("targetNo", targetNo);
		p.put("reporterId", reporterId);
		Integer cnt = sqlSessionTemplate.selectOne("report.dao.ReportDAO.existsReport", p);
		return cnt == null ? 0 : cnt;
	}

	@Override
	public void insert(ReportVO report) {
		sqlSessionTemplate.insert("report.dao.ReportDAO.insert", report);
	}

	@Override
	public List<ReportVO> selectAll() {
		return sqlSessionTemplate.selectList("report.dao.ReportDAO.selectAll");
	}

	@Override
	public void updateStatus(int no, String status) {
		Map<String, Object> p = new HashMap<>();
		p.put("no", no);
		p.put("status", status);
		sqlSessionTemplate.update("report.dao.ReportDAO.updateStatus", p);
	}
}
