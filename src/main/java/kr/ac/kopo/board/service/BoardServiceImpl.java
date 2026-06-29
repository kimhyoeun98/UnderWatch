package kr.ac.kopo.board.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.ac.kopo.board.dao.BoardDAO;
import kr.ac.kopo.board.vo.BoardCategoryVO;
import kr.ac.kopo.board.vo.BoardVO;
import kr.ac.kopo.board.vo.SearchVO;

@Service
public class BoardServiceImpl implements BoardService {

	@Autowired
	private BoardDAO boardDAO;

	@Override
	public List<BoardCategoryVO> getCategories() {
		return boardDAO.selectAllCategories();
	}

	@Override
	public void search(SearchVO searchVO) {
		int total = boardDAO.selectTotalCount(searchVO);
		searchVO.setTotalCount(total);
		// currentPage가 전체 페이지를 넘으면 마지막 페이지로 조정
		if (searchVO.getCurrentPage() > searchVO.getTotalPage() && searchVO.getTotalPage() > 0) {
			searchVO.setCurrentPage(searchVO.getTotalPage());
		}
	}

	@Override
	public List<BoardVO> getList(SearchVO searchVO) {
		return boardDAO.selectList(searchVO);
	}

	@Override
	public BoardVO getDetail(int no) {
		return boardDAO.selectOne(no);
	}

	@Override
	public void increaseView(int no) {
		boardDAO.increaseViewCnt(no);
	}

	@Override
	public void write(BoardVO board) {
		boardDAO.insert(board);
	}

	@Override
	public void edit(BoardVO board) {
		boardDAO.update(board);
	}

	@Override
	public void delete(int no) {
		boardDAO.delete(no);
	}

	@Override
	public void vote(int boardNo, String memberId, String type) {
		String current = boardDAO.getVoteType(memberId, boardNo);
		if (current == null) {
			// 신규 투표
			boardDAO.insertVote(memberId, boardNo, type);
			adjustCount(boardNo, type, +1);
		} else if (current.equals(type)) {
			// 같은 버튼 다시 → 취소
			boardDAO.deleteVote(memberId, boardNo);
			adjustCount(boardNo, type, -1);
		} else {
			// 반대 버튼 → 전환
			boardDAO.updateVote(memberId, boardNo, type);
			adjustCount(boardNo, current, -1);
			adjustCount(boardNo, type, +1);
		}
	}

	private void adjustCount(int boardNo, String type, int delta) {
		if ("L".equals(type)) {
			if (delta > 0) boardDAO.increaseLike(boardNo); else boardDAO.decreaseLike(boardNo);
		} else {
			if (delta > 0) boardDAO.increaseDislike(boardNo); else boardDAO.decreaseDislike(boardNo);
		}
	}

	@Override
	public String getMyVote(int boardNo, String memberId) {
		return boardDAO.getVoteType(memberId, boardNo);
	}

	@Override
	public List<BoardVO> getMyPosts(String writerId) {
		return boardDAO.selectByWriter(writerId);
	}

	@Override
	public void updateImage(int no, String stored, String orig) {
		boardDAO.updateImage(no, stored, orig);
	}

	@Override
	public void addCategory(BoardCategoryVO category) {
		boardDAO.insertCategory(category);
	}

	@Override
	public void editCategory(BoardCategoryVO category) {
		boardDAO.updateCategory(category);
	}

	@Override
	public void removeCategory(int no) {
		boardDAO.deleteCategory(no);
	}
}
