package kr.ac.kopo.member.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class MemberVO {

	private String id;

	@NotEmpty(message = "비밀번호를 입력하세요.")
	@Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
	private String password;

	@NotEmpty(message = "닉네임을 입력하세요.")
	@Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
	private String nickname;

	@NotEmpty(message = "이름을 입력하세요.")
	private String name;

	// 하이픈은 선택. 숫자만 입력해도 통과(서버에서 표준 형식으로 정규화 저장). 예: 01012345678 또는 010-1234-5678
	@Pattern(regexp = "^$|^0\\d{1,2}-?\\d{3,4}-?\\d{4}$", message = "전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)")
	private String phone;

	@NotEmpty(message = "이메일을 입력하세요.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	private String email;

	private String role;
	private int gradePoint;
	private String profileImg;
	private String status;
	private String regDate;
	private String updateDate;

	// M-10 탈퇴 신청 시각(yyyy-MM-dd HH:mm:ss). 미탈퇴면 null. 재로그인 복구 7일 판정에 사용
	private String withdrawAt;

	// M-03 소셜 로그인 (google / kakao / naver, 일반가입은 null)
	private String provider;
	private String providerId;

	// M-09 얼굴 로그인 특징벡터(128차원 JSON 배열 문자열)
	private String faceDescriptor;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public int getGradePoint() {
		return gradePoint;
	}

	public void setGradePoint(int gradePoint) {
		this.gradePoint = gradePoint;
	}

	public String getProfileImg() {
		return profileImg;
	}

	public void setProfileImg(String profileImg) {
		this.profileImg = profileImg;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRegDate() {
		return regDate;
	}

	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	public String getWithdrawAt() {
		return withdrawAt;
	}

	public void setWithdrawAt(String withdrawAt) {
		this.withdrawAt = withdrawAt;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	public String getFaceDescriptor() {
		return faceDescriptor;
	}

	public void setFaceDescriptor(String faceDescriptor) {
		this.faceDescriptor = faceDescriptor;
	}
}
