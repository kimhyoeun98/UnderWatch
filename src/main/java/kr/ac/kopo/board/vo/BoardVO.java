package kr.ac.kopo.board.vo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class BoardVO {

	private int no;
	private int categoryNo;
	private String categoryName;
	private String writerId;
	private String writerNickname;

	@NotEmpty(message = "제목을 입력하세요.")
	@Size(max = 200, message = "제목은 200자 이내로 입력하세요.")
	private String title;

	@NotEmpty(message = "내용을 입력하세요.")
	private String content;

	private int viewCnt;
	private int likeCnt;
	private int dislikeCnt;
	private int commentCnt;
	private String isDeleted;
	private String regDate;
	private String updateDate;
	private String imageStored;   // B-04 저장 파일명
	private String imageOrig;     // B-04 원본 파일명
	private String guestName;     // B-02 비로그인 작성자 표시 이름
	private String guestPassword; // B-02 비로그인 작성자 비밀번호(BCrypt 해시)

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	public int getCategoryNo() {
		return categoryNo;
	}

	public void setCategoryNo(int categoryNo) {
		this.categoryNo = categoryNo;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
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

	public int getViewCnt() {
		return viewCnt;
	}

	public void setViewCnt(int viewCnt) {
		this.viewCnt = viewCnt;
	}

	public int getLikeCnt() {
		return likeCnt;
	}

	public void setLikeCnt(int likeCnt) {
		this.likeCnt = likeCnt;
	}

	public int getDislikeCnt() {
		return dislikeCnt;
	}

	public void setDislikeCnt(int dislikeCnt) {
		this.dislikeCnt = dislikeCnt;
	}

	public int getCommentCnt() {
		return commentCnt;
	}

	public void setCommentCnt(int commentCnt) {
		this.commentCnt = commentCnt;
	}

	public String getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
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

	public String getImageStored() {
		return imageStored;
	}

	public void setImageStored(String imageStored) {
		this.imageStored = imageStored;
	}

	public String getImageOrig() {
		return imageOrig;
	}

	public void setImageOrig(String imageOrig) {
		this.imageOrig = imageOrig;
	}

	public String getGuestName() {
		return guestName;
	}

	public void setGuestName(String guestName) {
		this.guestName = guestName;
	}

	public String getGuestPassword() {
		return guestPassword;
	}

	public void setGuestPassword(String guestPassword) {
		this.guestPassword = guestPassword;
	}
}
