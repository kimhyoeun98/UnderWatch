package kr.ac.kopo.comment.service;

import java.util.List;

import kr.ac.kopo.comment.vo.CommentVO;

public interface CommentService {

	List<CommentVO> getComments(int boardNo);
	void write(CommentVO comment);
	void delete(int no, String requesterId);
	List<CommentVO> getMyComments(String writerId);   // M-05
	void blind(int no);                               // A-03 관리자 블라인드
	String getWriterId(int no);                       // M-11 알림 대상(댓글 작성자) 조회
	void vote(int commentNo, String memberId, String type);   // I-03 추천/비추천 토글
}
