package kr.ac.kopo.comment.vo;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class CommentVO {

	private int no;
	private int boardNo;
	private String writerId;
	private String writerNickname;
	private Integer parentNo;

	@NotEmpty(message = "내용을 입력하세요.")
	@Size(max = 1000, message = "댓글은 1000자 이내로 입력하세요.")
	private String content;

	private String isDeleted;
	private String regDate;
	private String boardTitle;   // M-05 활동내역: 댓글이 달린 글 제목
	private List<CommentVO> children = new ArrayList<>();

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	public int getBoardNo() {
		return boardNo;
	}

	public void setBoardNo(int boardNo) {
		this.boardNo = boardNo;
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

	public Integer getParentNo() {
		return parentNo;
	}

	public void setParentNo(Integer parentNo) {
		this.parentNo = parentNo;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
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

	public String getBoardTitle() {
		return boardTitle;
	}

	public void setBoardTitle(String boardTitle) {
		this.boardTitle = boardTitle;
	}

	public List<CommentVO> getChildren() {
		return children;
	}

	public void setChildren(List<CommentVO> children) {
		this.children = children;
	}
}
