package kr.ac.kopo.member.service;

import java.util.List;

import kr.ac.kopo.member.vo.MemberVO;

public interface MemberService {

	void register(MemberVO member);
	MemberVO findById(String id);
	boolean isIdDuplicate(String id);
	boolean isNicknameDuplicate(String nickname);
	boolean isEmailDuplicate(String email);
	void updateInfo(MemberVO member);
	void changePassword(String id, String newRawPassword);

	// A-01 관리자
	List<MemberVO> getAllMembers();
	void changeStatus(String id, String status);
	void changeRole(String id, String role);

	// M-06 프로필 사진
	void updateProfileImg(String id, String profileImg);

	// M-07 닉네임 변경 주기
	boolean canChangeNickname(String id);
	void markNicknameChanged(String id);

	// M-09 얼굴 로그인 (LBPH 직접 구현)
	boolean saveFace(String id, String imageData);   // 얼굴 등록, 인식 실패 시 false
	boolean hasFace(String id);
	/** 입력 얼굴과 가장 가까운(임계값 이내) 회원 id 반환, 없으면 null */
	String matchFace(String imageData);
}
