package kr.ac.kopo.member.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import kr.ac.kopo.member.vo.MemberVO;

public interface MemberDAO {

	void insert(MemberVO member);
	MemberVO selectById(String id);
	boolean existsId(String id);
	boolean existsNickname(String nickname);
	boolean existsEmail(String email);
	void updateInfo(MemberVO member);
	void updatePassword(MemberVO member);

	// A-01 관리자: 회원 관리
	List<MemberVO> selectAll();
	void updateStatus(String id, String status);
	void updateRole(String id, String role);

	// M-08 등급 점수 컬럼 갱신 (글수*10 + 받은추천*5 + 받은조회*1)
	void recalcGradePoint(String id);

	// M-10 계정 탈퇴 (7일 보관 후 삭제)
	void requestWithdraw(String id);            // status=WITHDRAWN, withdraw_at=SYSDATE
	void cancelWithdraw(String id);             // 탈퇴 철회(재로그인 시 복구)
	List<String> selectExpiredWithdrawals();    // 탈퇴 후 7일 경과 회원 id
	void purgeMember(String id);                // 회원 + 연관 데이터 일괄 삭제

	// M-06 프로필 사진
	void updateProfileImg(String id, String profileImg);

	// M-07 닉네임 변경 주기
	int canChangeNickname(String id);   // 1=가능, 0=불가
	void updateNicknameChanged(String id);

	// M-03 소셜 로그인
	MemberVO selectByProvider(@Param("provider") String provider,
							  @Param("providerId") String providerId);
	void insertSocial(MemberVO member);

	// M-09 얼굴 로그인
	void updateFaceDescriptor(@Param("id") String id,
							  @Param("faceDescriptor") String faceDescriptor);
	String selectFaceDescriptor(String id);   // 현재 저장된 얼굴 임베딩 JSON(없으면 null)
	List<MemberVO> selectAllFaces();    // 얼굴 등록된 회원 전체(id + faceDescriptor)
	int countFace(String id);           // 본인 얼굴 등록 여부(1/0)
}
