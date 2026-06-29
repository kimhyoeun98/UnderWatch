package kr.ac.kopo.board.service;

import java.util.List;

import kr.ac.kopo.board.vo.BoardCategoryVO;
import kr.ac.kopo.board.vo.BoardVO;
import kr.ac.kopo.board.vo.SearchVO;

public interface BoardService {

	List<BoardCategoryVO> getCategories();
	void search(SearchVO searchVO);                          // totalCount 세팅 후 list 조회
	List<BoardVO> getList(SearchVO searchVO);
	BoardVO getDetail(int no);
	void increaseView(int no);
	void write(BoardVO board);
	void edit(BoardVO board);
	void delete(int no);
	void vote(int boardNo, String memberId, String type);    // I-03 토글(추천/비추천/취소/전환)
	String getMyVote(int boardNo, String memberId);          // 'L'/'D'/null
	List<BoardVO> getMyPosts(String writerId);               // M-05
	void updateImage(int no, String stored, String orig);    // B-04

	// A-02 카테고리 관리
	void addCategory(BoardCategoryVO category);
	void editCategory(BoardCategoryVO category);
	void removeCategory(int no);
}
