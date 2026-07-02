package kr.ac.kopo.comment.dao;

import java.util.List;

import kr.ac.kopo.comment.vo.CommentVO;

public interface CommentDAO {

	List<CommentVO> selectByBoardNo(int boardNo);
	void insert(CommentVO comment);
	void delete(int no);
	CommentVO selectOne(int no);
	List<CommentVO> selectByWriter(String writerId);   // M-05

	// I-03 댓글 추천/비추천 (토글)
	String getVoteType(String memberId, int commentNo);
	void insertVote(String memberId, int commentNo, String type);
	void updateVote(String memberId, int commentNo, String type);
	void deleteVote(String memberId, int commentNo);
	void increaseLike(int commentNo);
	void decreaseLike(int commentNo);
	void increaseDislike(int commentNo);
	void decreaseDislike(int commentNo);
}
