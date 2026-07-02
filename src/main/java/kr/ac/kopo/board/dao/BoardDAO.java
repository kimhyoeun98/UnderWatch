package kr.ac.kopo.board.dao;

import java.util.List;

import kr.ac.kopo.board.vo.BoardCategoryVO;
import kr.ac.kopo.board.vo.BoardVO;
import kr.ac.kopo.board.vo.SearchVO;

public interface BoardDAO {

	List<BoardCategoryVO> selectAllCategories();
	int selectTotalCount(SearchVO search);
	List<BoardVO> selectList(SearchVO search);
	BoardVO selectOne(int no);
	void insert(BoardVO board);
	void update(BoardVO board);
	void delete(int no);
	void increaseViewCnt(int no);

	// I-03 추천/비추천 (토글)
	String getVoteType(String memberId, int boardNo);
	void insertVote(String memberId, int boardNo, String type);
	void updateVote(String memberId, int boardNo, String type);
	void deleteVote(String memberId, int boardNo);
	void increaseLike(int boardNo);
	void decreaseLike(int boardNo);
	void increaseDislike(int boardNo);
	void decreaseDislike(int boardNo);

	// B-02 게스트 글 비밀번호 검증
	String selectGuestPassword(int no);

	// M-05 내가 쓴 글
	List<BoardVO> selectByWriter(String writerId);

	// B-04 이미지
	void updateImage(int no, String stored, String orig);

	// A-02 카테고리 관리
	void insertCategory(BoardCategoryVO category);
	void updateCategory(BoardCategoryVO category);
	void deleteCategory(int no);
}
