package kr.ac.kopo.member.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import kr.ac.kopo.member.dao.MemberDAO;
import kr.ac.kopo.member.face.EigenFaceRecognizer;
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

	// ===== M-09 얼굴 로그인 (Eigenfaces/PCA) =====

	@Autowired
	private EigenFaceRecognizer faceRecognizer;

	@Override
	public boolean saveFace(String id, String imageData) {
		int[] face = faceRecognizer.extractFace(imageData);
		if (face == null) {
			return false; // 이미지 디코드/추출 실패
		}
		memberDAO.updateFaceDescriptor(id, faceRecognizer.toJson(face));
		return true;
	}

	@Override
	public boolean hasFace(String id) {
		return memberDAO.countFace(id) > 0;
	}

	@Override
	public String matchFace(String imageData) {
		int[] probe = faceRecognizer.extractFace(imageData);
		if (probe == null) {
			return null;
		}
		// 등록된 얼굴들을 모아 PCA 고유공간에서 1:N 비교 (recognize 내부에서 최근접 거리 로그)
		List<String> ids = new ArrayList<>();
		List<int[]> faces = new ArrayList<>();
		for (MemberVO m : memberDAO.selectAllFaces()) {
			int[] f = faceRecognizer.parse(m.getFaceDescriptor());
			if (f != null && f.length == EigenFaceRecognizer.DIM) {   // 옛 형식(LBPH 등) 데이터는 건너뜀
				ids.add(m.getId());
				faces.add(f);
			}
		}
		return faceRecognizer.recognize(probe, ids, faces);
	}
}
