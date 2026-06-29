package kr.ac.kopo.comment.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.ac.kopo.comment.dao.CommentDAO;
import kr.ac.kopo.comment.vo.CommentVO;

@Service
public class CommentServiceImpl implements CommentService {

	@Autowired
	private CommentDAO commentDAO;

	@Override
	public List<CommentVO> getComments(int boardNo) {
		List<CommentVO> flat = commentDAO.selectByBoardNo(boardNo);
		// parent_no 기준으로 계층 구조 조립
		List<CommentVO> roots = new ArrayList<>();
		for (CommentVO c : flat) {
			if (c.getParentNo() == null) {
				roots.add(c);
			} else {
				for (CommentVO parent : flat) {
					if (parent.getNo() == c.getParentNo()) {
						parent.getChildren().add(c);
						break;
					}
				}
			}
		}
		return roots;
	}

	@Override
	public void write(CommentVO comment) {
		commentDAO.insert(comment);
	}

	@Override
	public void delete(int no, String requesterId) {
		CommentVO comment = commentDAO.selectOne(no);
		if (comment != null && comment.getWriterId().equals(requesterId)) {
			commentDAO.delete(no);
		}
	}

	@Override
	public List<CommentVO> getMyComments(String writerId) {
		return commentDAO.selectByWriter(writerId);
	}

	@Override
	public void blind(int no) {
		commentDAO.delete(no);
	}
}
