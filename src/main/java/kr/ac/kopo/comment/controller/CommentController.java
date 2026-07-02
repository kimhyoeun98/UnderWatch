package kr.ac.kopo.comment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.ac.kopo.board.service.BoardService;
import kr.ac.kopo.board.vo.BoardVO;
import kr.ac.kopo.comment.service.CommentService;
import kr.ac.kopo.comment.vo.CommentVO;
import kr.ac.kopo.notification.service.NotificationService;

@Controller
public class CommentController {

	@Autowired
	private CommentService commentService;

	@Autowired
	private BoardService boardService;

	@Autowired
	private NotificationService notificationService;

	/**
	 * 댓글등록
	 * POST /comment/write
	 */
	@PostMapping("/comment/write")
	public String write(@AuthenticationPrincipal UserDetails userDetails,
						@RequestParam("boardNo") int boardNo,
						@RequestParam(name = "parentNo", required = false) Integer parentNo,
						@RequestParam("content") String content) {
		CommentVO comment = new CommentVO();
		comment.setBoardNo(boardNo);
		comment.setWriterId(userDetails.getUsername());
		comment.setParentNo(parentNo);
		comment.setContent(content);
		commentService.write(comment);

		// R-02 알림: 글 작성자에게 (본인 댓글이거나 비로그인 게스트 글이면 생략)
		BoardVO board = boardService.getDetail(boardNo);
		if (board != null && board.getWriterId() != null
				&& !board.getWriterId().equals(userDetails.getUsername())) {
			notificationService.notify(board.getWriterId(),
				userDetails.getUsername() + "님이 회원님의 글에 댓글을 남겼습니다.",
				"/board/detail?no=" + boardNo);
		}
		return "redirect:/board/detail?no=" + boardNo;
	}

	/**
	 * 댓글삭제
	 * POST /comment/delete
	 */
	@PostMapping("/comment/delete")
	public String delete(@AuthenticationPrincipal UserDetails userDetails,
						 @RequestParam("no") int no,
						 @RequestParam("boardNo") int boardNo) {
		commentService.delete(no, userDetails.getUsername());
		return "redirect:/board/detail?no=" + boardNo;
	}

	/**
	 * I-03 댓글 추천
	 * POST /comment/like
	 */
	@PostMapping("/comment/like")
	public String like(@AuthenticationPrincipal UserDetails userDetails,
					   @RequestParam("no") int no,
					   @RequestParam("boardNo") int boardNo) {
		commentService.vote(no, userDetails.getUsername(), "L");
		return "redirect:/board/detail?no=" + boardNo + "#comment-" + no;
	}

	/**
	 * I-03 댓글 비추천
	 * POST /comment/dislike
	 */
	@PostMapping("/comment/dislike")
	public String dislike(@AuthenticationPrincipal UserDetails userDetails,
						 @RequestParam("no") int no,
						 @RequestParam("boardNo") int boardNo) {
		commentService.vote(no, userDetails.getUsername(), "D");
		return "redirect:/board/detail?no=" + boardNo + "#comment-" + no;
	}
}
