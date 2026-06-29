package kr.ac.kopo.member.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import kr.ac.kopo.member.dao.MemberDAO;
import kr.ac.kopo.member.face.LbphFaceRecognizer;
import kr.ac.kopo.member.vo.MemberVO;

@Service
public class MemberServiceImpl implements MemberService {

	@Autowired
	private MemberDAO memberDAO;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Override
	public void register(MemberVO member) {
		member.setPassword(passwordEncoder.encode(member.getPassword()));
		memberDAO.insert(member);
	}

	@Override
	public MemberVO findById(String id) {
		return memberDAO.selectById(id);
	}

	@Override
	public boolean isIdDuplicate(String id) {
		return memberDAO.existsId(id);
	}

	@Override
	public boolean isNicknameDuplicate(String nickname) {
		return memberDAO.existsNickname(nickname);
	}

	@Override
	public boolean isEmailDuplicate(String email) {
		return memberDAO.existsEmail(email);
	}

	@Override
	public void updateInfo(MemberVO member) {
		memberDAO.updateInfo(member);
	}

	@Override
	public void changePassword(String id, String newRawPassword) {
		MemberVO member = new MemberVO();
		member.setId(id);
		member.setPassword(passwordEncoder.encode(newRawPassword));
		memberDAO.updatePassword(member);
	}

	@Override
	public List<MemberVO> getAllMembers() {
		return memberDAO.selectAll();
	}

	@Override
	public void changeStatus(String id, String status) {
		memberDAO.updateStatus(id, status);
	}

	@Override
	public void changeRole(String id, String role) {
		memberDAO.updateRole(id, role);
	}

	@Override
	public void updateProfileImg(String id, String profileImg) {
		memberDAO.updateProfileImg(id, profileImg);
	}

	@Override
	public boolean canChangeNickname(String id) {
		return memberDAO.canChangeNickname(id) > 0;
	}

	@Override
	public void markNicknameChanged(String id) {
		memberDAO.updateNicknameChanged(id);
	}

	// ===== M-09 얼굴 로그인 (LBPH 직접 구현) =====

	/** 같은 사람으로 인정하는 최대 거리(카이제곱, 0~2). 낮을수록 엄격 — 환경에 맞게 조정. */
	private static final double MATCH_THRESHOLD = 0.45;

	@Autowired
	private LbphFaceRecognizer faceRecognizer;

	@Override
	public boolean saveFace(String id, String imageData) {
		int[] feature = faceRecognizer.extract(imageData);
		if (feature == null) {
			return false; // 이미지 디코드/추출 실패
		}
		memberDAO.updateFaceDescriptor(id, faceRecognizer.toJson(feature));
		return true;
	}

	@Override
	public boolean hasFace(String id) {
		return memberDAO.countFace(id) > 0;
	}

	@Override
	public String matchFace(String imageData) {
		int[] probe = faceRecognizer.extract(imageData);
		if (probe == null) {
			return null;
		}
		String bestId = null;
		double bestDist = Double.MAX_VALUE;
		for (MemberVO m : memberDAO.selectAllFaces()) {
			int[] stored = faceRecognizer.parse(m.getFaceDescriptor());
			double dist = faceRecognizer.distance(probe, stored);
			if (dist < bestDist) {
				bestDist = dist;
				bestId = m.getId();
			}
		}
		// 임계값 조정에 참고할 수 있도록 최근접 거리를 로그로 남긴다
		System.out.println("[FaceLogin] best=" + bestId + " dist=" + bestDist);
		return bestDist <= MATCH_THRESHOLD ? bestId : null;
	}
}
