package kr.ac.kopo.comment.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.ac.kopo.comment.vo.CommentVO;

@Repository
public class CommentDAOImpl implements CommentDAO {

	@Autowired
	private SqlSessionTemplate sqlSessionTemplate;

	@Override
	public List<CommentVO> selectByBoardNo(int boardNo) {
		return sqlSessionTemplate.selectList("comment.dao.CommentDAO.selectByBoardNo", boardNo);
	}

	@Override
	public void insert(CommentVO comment) {
		sqlSessionTemplate.insert("comment.dao.CommentDAO.insert", comment);
	}

	@Override
	public void delete(int no) {
		sqlSessionTemplate.update("comment.dao.CommentDAO.delete", no);
	}

	@Override
	public CommentVO selectOne(int no) {
		return sqlSessionTemplate.selectOne("comment.dao.CommentDAO.selectOne", no);
	}

	@Override
	public List<CommentVO> selectByWriter(String writerId) {
		return sqlSessionTemplate.selectList("comment.dao.CommentDAO.selectByWriter", writerId);
	}

	// I-03 댓글 추천/비추천
	@Override
	public String getVoteType(String memberId, int commentNo) {
		Map<String, Object> p = new HashMap<>();
		p.put("memberId", memberId);
		p.put("commentNo", commentNo);
		return sqlSessionTemplate.selectOne("comment.dao.CommentDAO.getVoteType", p);
	}

	@Override
	public void insertVote(String memberId, int commentNo, String type) {
		Map<String, Object> p = new HashMap<>();
		p.put("memberId", memberId);
		p.put("commentNo", commentNo);
		p.put("type", type);
		sqlSessionTemplate.insert("comment.dao.CommentDAO.insertVote", p);
	}

	@Override
	public void updateVote(String memberId, int commentNo, String type) {
		Map<String, Object> p = new HashMap<>();
		p.put("memberId", memberId);
		p.put("commentNo", commentNo);
		p.put("type", type);
		sqlSessionTemplate.update("comment.dao.CommentDAO.updateVote", p);
	}

	@Override
	public void deleteVote(String memberId, int commentNo) {
		Map<String, Object> p = new HashMap<>();
		p.put("memberId", memberId);
		p.put("commentNo", commentNo);
		sqlSessionTemplate.delete("comment.dao.CommentDAO.deleteVote", p);
	}

	@Override
	public void increaseLike(int commentNo) {
		sqlSessionTemplate.update("comment.dao.CommentDAO.increaseLike", commentNo);
	}

	@Override
	public void decreaseLike(int commentNo) {
		sqlSessionTemplate.update("comment.dao.CommentDAO.decreaseLike", commentNo);
	}

	@Override
	public void increaseDislike(int commentNo) {
		sqlSessionTemplate.update("comment.dao.CommentDAO.increaseDislike", commentNo);
	}

	@Override
	public void decreaseDislike(int commentNo) {
		sqlSessionTemplate.update("comment.dao.CommentDAO.decreaseDislike", commentNo);
	}
}
