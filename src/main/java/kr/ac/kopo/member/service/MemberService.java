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

	// M-08 등급: 글 작성/추천 발생 시 점수 컬럼 재계산
	void recalcGrade(String id);

	// M-10 계정 탈퇴 (비밀번호 확인 후 7일 보관 → 삭제)
	boolean withdraw(String id, String rawPassword);   // 비밀번호 검증 후 탈퇴 표시, 실패 시 false
	int purgeExpiredWithdrawals();                     // 7일 경과분 실제 삭제, 삭제 건수 반환
	void reactivateOnLogin(String id);                 // 탈퇴 유예 중 재로그인 시 ACTIVE 복구

	// M-06 프로필 사진
	void updateProfileImg(String id, String profileImg);

	// M-07 닉네임 변경 주기
	boolean canChangeNickname(String id);
	void markNicknameChanged(String id);

	// M-09 얼굴 로그인
	boolean saveFace(String id, String imageData);   // 얼굴 등록(최대 3장 누적), 인식 실패 시 false
	boolean hasFace(String id);
	int faceCount(String id);                        // 등록된 얼굴 사진 수(0~3)
	/** 입력 얼굴과 가장 가까운(임계값 이내) 회원 id 반환, 없으면 null */
	String matchFace(String imageData);
}
