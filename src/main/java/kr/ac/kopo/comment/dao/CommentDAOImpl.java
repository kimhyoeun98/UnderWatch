package kr.ac.kopo.comment.dao;

import java.util.List;

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
}
