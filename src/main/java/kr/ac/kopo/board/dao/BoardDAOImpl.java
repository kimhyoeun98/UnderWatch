package kr.ac.kopo.board.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.ac.kopo.board.vo.BoardCategoryVO;
import kr.ac.kopo.board.vo.BoardVO;
import kr.ac.kopo.board.vo.SearchVO;

@Repository
public class BoardDAOImpl implements BoardDAO {

	@Autowired
	private SqlSessionTemplate sqlSessionTemplate;

	@Override
	public List<BoardCategoryVO> selectAllCategories() {
		return sqlSessionTemplate.selectList("board.dao.BoardDAO.selectAllCategories");
	}

	@Override
	public int selectTotalCount(SearchVO search) {
		return sqlSessionTemplate.selectOne("board.dao.BoardDAO.selectTotalCount", search);
	}

	@Override
	public List<BoardVO> selectList(SearchVO search) {
		return sqlSessionTemplate.selectList("board.dao.BoardDAO.selectList", search);
	}

	@Override
	public BoardVO selectOne(int no) {
		return sqlSessionTemplate.selectOne("board.dao.BoardDAO.selectOne", no);
	}

	@Override
	public void insert(BoardVO board) {
		sqlSessionTemplate.insert("board.dao.BoardDAO.insert", board);
	}

	@Override
	public void update(BoardVO board) {
		sqlSessionTemplate.update("board.dao.BoardDAO.update", board);
	}

	@Override
	public void delete(int no) {
		sqlSessionTemplate.update("board.dao.BoardDAO.delete", no);
	}

	@Override
	public void increaseViewCnt(int no) {
		sqlSessionTemplate.update("board.dao.BoardDAO.increaseViewCnt", no);
	}

	@Override
	public String getVoteType(String memberId, int boardNo) {
		Map<String, Object> p = new HashMap<>();
		p.put("memberId", memberId);
		p.put("boardNo", boardNo);
		return sqlSessionTemplate.selectOne("board.dao.BoardDAO.getVoteType", p);
	}

	@Override
	public void insertVote(String memberId, int boardNo, String type) {
		Map<String, Object> p = new HashMap<>();
		p.put("memberId", memberId);
		p.put("boardNo", boardNo);
		p.put("type", type);
		sqlSessionTemplate.insert("board.dao.BoardDAO.insertVote", p);
	}

	@Override
	public void updateVote(String memberId, int boardNo, String type) {
		Map<String, Object> p = new HashMap<>();
		p.put("memberId", memberId);
		p.put("boardNo", boardNo);
		p.put("type", type);
		sqlSessionTemplate.update("board.dao.BoardDAO.updateVote", p);
	}

	@Override
	public void deleteVote(String memberId, int boardNo) {
		Map<String, Object> p = new HashMap<>();
		p.put("memberId", memberId);
		p.put("boardNo", boardNo);
		sqlSessionTemplate.delete("board.dao.BoardDAO.deleteVote", p);
	}

	@Override
	public void increaseLike(int boardNo) {
		sqlSessionTemplate.update("board.dao.BoardDAO.increaseLike", boardNo);
	}

	@Override
	public void decreaseLike(int boardNo) {
		sqlSessionTemplate.update("board.dao.BoardDAO.decreaseLike", boardNo);
	}

	@Override
	public void increaseDislike(int boardNo) {
		sqlSessionTemplate.update("board.dao.BoardDAO.increaseDislike", boardNo);
	}

	@Override
	public void decreaseDislike(int boardNo) {
		sqlSessionTemplate.update("board.dao.BoardDAO.decreaseDislike", boardNo);
	}

	@Override
	public String selectGuestPassword(int no) {
		return sqlSessionTemplate.selectOne("board.dao.BoardDAO.selectGuestPassword", no);
	}

	@Override
	public List<BoardVO> selectByWriter(String writerId) {
		return sqlSessionTemplate.selectList("board.dao.BoardDAO.selectByWriter", writerId);
	}

	@Override
	public void updateImage(int no, String stored, String orig) {
		Map<String, Object> p = new HashMap<>();
		p.put("no", no);
		p.put("stored", stored);
		p.put("orig", orig);
		sqlSessionTemplate.update("board.dao.BoardDAO.updateImage", p);
	}

	@Override
	public void insertCategory(BoardCategoryVO category) {
		sqlSessionTemplate.insert("board.dao.BoardDAO.insertCategory", category);
	}

	@Override
	public void updateCategory(BoardCategoryVO category) {
		sqlSessionTemplate.update("board.dao.BoardDAO.updateCategory", category);
	}

	@Override
	public void deleteCategory(int no) {
		sqlSessionTemplate.delete("board.dao.BoardDAO.deleteCategory", no);
	}
}
