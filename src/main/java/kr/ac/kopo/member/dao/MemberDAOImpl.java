package kr.ac.kopo.member.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.ac.kopo.member.vo.MemberVO;

@Repository
public class MemberDAOImpl implements MemberDAO {

	@Autowired
	private SqlSessionTemplate sqlSessionTemplate;

	@Override
	public void insert(MemberVO member) {
		sqlSessionTemplate.insert("member.dao.MemberDAO.insert", member);
	}

	@Override
	public MemberVO selectById(String id) {
		return sqlSessionTemplate.selectOne("member.dao.MemberDAO.selectById", id);
	}

	@Override
	public boolean existsId(String id) {
		Integer cnt = sqlSessionTemplate.selectOne("member.dao.MemberDAO.existsId", id);
		return cnt != null && cnt > 0;
	}

	@Override
	public boolean existsNickname(String nickname) {
		Integer cnt = sqlSessionTemplate.selectOne("member.dao.MemberDAO.existsNickname", nickname);
		return cnt != null && cnt > 0;
	}

	@Override
	public boolean existsEmail(String email) {
		Integer cnt = sqlSessionTemplate.selectOne("member.dao.MemberDAO.existsEmail", email);
		return cnt != null && cnt > 0;
	}

	@Override
	public void updateInfo(MemberVO member) {
		sqlSessionTemplate.update("member.dao.MemberDAO.updateInfo", member);
	}

	@Override
	public void updatePassword(MemberVO member) {
		sqlSessionTemplate.update("member.dao.MemberDAO.updatePassword", member);
	}

	@Override
	public List<MemberVO> selectAll() {
		return sqlSessionTemplate.selectList("member.dao.MemberDAO.selectAll");
	}

	@Override
	public void updateStatus(String id, String status) {
		Map<String, Object> p = new HashMap<>();
		p.put("id", id);
		p.put("status", status);
		sqlSessionTemplate.update("member.dao.MemberDAO.updateStatus", p);
	}

	@Override
	public void updateRole(String id, String role) {
		Map<String, Object> p = new HashMap<>();
		p.put("id", id);
		p.put("role", role);
		sqlSessionTemplate.update("member.dao.MemberDAO.updateRole", p);
	}

	// M-08 등급 점수 재계산
	@Override
	public void recalcGradePoint(String id) {
		sqlSessionTemplate.update("member.dao.MemberDAO.recalcGradePoint", id);
	}

	// M-10 계정 탈퇴
	@Override
	public void requestWithdraw(String id) {
		sqlSessionTemplate.update("member.dao.MemberDAO.requestWithdraw", id);
	}

	@Override
	public void cancelWithdraw(String id) {
		sqlSessionTemplate.update("member.dao.MemberDAO.cancelWithdraw", id);
	}

	@Override
	public List<String> selectExpiredWithdrawals() {
		return sqlSessionTemplate.selectList("member.dao.MemberDAO.selectExpiredWithdrawals");
	}

	@Override
	public void purgeMember(String id) {
		sqlSessionTemplate.delete("member.dao.MemberDAO.purgeMember", id);
	}

	@Override
	public void updateProfileImg(String id, String profileImg) {
		Map<String, Object> p = new HashMap<>();
		p.put("id", id);
		p.put("profileImg", profileImg);
		sqlSessionTemplate.update("member.dao.MemberDAO.updateProfileImg", p);
	}

	@Override
	public int canChangeNickname(String id) {
		Integer ok = sqlSessionTemplate.selectOne("member.dao.MemberDAO.canChangeNickname", id);
		return ok == null ? 1 : ok;
	}

	@Override
	public void updateNicknameChanged(String id) {
		sqlSessionTemplate.update("member.dao.MemberDAO.updateNicknameChanged", id);
	}

	// M-03 소셜 로그인
	@Override
	public MemberVO selectByProvider(String provider, String providerId) {
		Map<String, Object> p = new HashMap<>();
		p.put("provider", provider);
		p.put("providerId", providerId);
		return sqlSessionTemplate.selectOne("member.dao.MemberDAO.selectByProvider", p);
	}

	@Override
	public void insertSocial(MemberVO member) {
		sqlSessionTemplate.insert("member.dao.MemberDAO.insertSocial", member);
	}

	// M-09 얼굴 로그인
	@Override
	public void updateFaceDescriptor(String id, String faceDescriptor) {
		Map<String, Object> p = new HashMap<>();
		p.put("id", id);
		p.put("faceDescriptor", faceDescriptor);
		sqlSessionTemplate.update("member.dao.MemberDAO.updateFaceDescriptor", p);
	}

	@Override
	public String selectFaceDescriptor(String id) {
		return sqlSessionTemplate.selectOne("member.dao.MemberDAO.selectFaceDescriptor", id);
	}

	@Override
	public List<MemberVO> selectAllFaces() {
		return sqlSessionTemplate.selectList("member.dao.MemberDAO.selectAllFaces");
	}

	@Override
	public int countFace(String id) {
		Integer cnt = sqlSessionTemplate.selectOne("member.dao.MemberDAO.countFace", id);
		return cnt == null ? 0 : cnt;
	}
}
