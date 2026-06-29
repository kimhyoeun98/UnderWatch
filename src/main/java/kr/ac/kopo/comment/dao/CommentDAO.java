package kr.ac.kopo.comment.dao;

import java.util.List;

import kr.ac.kopo.comment.vo.CommentVO;

public interface CommentDAO {

	List<CommentVO> selectByBoardNo(int boardNo);
	void insert(CommentVO comment);
	void delete(int no);
	CommentVO selectOne(int no);
	List<CommentVO> selectByWriter(String writerId);   // M-05
}
