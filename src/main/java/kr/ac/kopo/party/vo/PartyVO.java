package kr.ac.kopo.party.vo;

public class PartyVO {

	private int no;
	private String writerId;
	private String writerNickname;
	private String title;
	private String content;
	private String roleType;   // 탱커/딜러/지원가 (필터 겸용)
	private String tier;       // 브론즈~챔피언 (필터 겸용)
	private String micYn;      // Y/N
	private String mainHero;
	private String status;     // RECRUITING/CLOSED
	private String regDate;

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	public String getWriterId() {
		return writerId;
	}

	public void setWriterId(String writerId) {
		this.writerId = writerId;
	}

	public String getWriterNickname() {
		return writerNickname;
	}

	public void setWriterNickname(String writerNickname) {
		this.writerNickname = writerNickname;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getRoleType() {
		return roleType;
	}

	public void setRoleType(String roleType) {
		this.roleType = roleType;
	}

	public String getTier() {
		return tier;
	}

	public void setTier(String tier) {
		this.tier = tier;
	}

	public String getMicYn() {
		return micYn;
	}

	public void setMicYn(String micYn) {
		this.micYn = micYn;
	}

	public String getMainHero() {
		return mainHero;
	}

	public void setMainHero(String mainHero) {
		this.mainHero = mainHero;
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
}
