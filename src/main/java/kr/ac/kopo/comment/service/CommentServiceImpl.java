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

	@Override
	public String getWriterId(int no) {
		CommentVO c = commentDAO.selectOne(no);
		return c == null ? null : c.getWriterId();
	}

	@Override
	public void vote(int commentNo, String memberId, String type) {
		String current = commentDAO.getVoteType(memberId, commentNo);
		if (current == null) {
			commentDAO.insertVote(memberId, commentNo, type);
			adjustCount(commentNo, type, +1);
		} else if (current.equals(type)) {
			commentDAO.deleteVote(memberId, commentNo);   // 같은 버튼 → 취소
			adjustCount(commentNo, type, -1);
		} else {
			commentDAO.updateVote(memberId, commentNo, type);   // 반대 버튼 → 전환
			adjustCount(commentNo, current, -1);
			adjustCount(commentNo, type, +1);
		}
	}

	private void adjustCount(int commentNo, String type, int delta) {
		if ("L".equals(type)) {
			if (delta > 0) commentDAO.increaseLike(commentNo); else commentDAO.decreaseLike(commentNo);
		} else {
			if (delta > 0) commentDAO.increaseDislike(commentNo); else commentDAO.decreaseDislike(commentNo);
		}
	}
}
