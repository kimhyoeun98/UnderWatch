package kr.ac.kopo.member.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kr.ac.kopo.member.service.MemberService;

/**
 * M-10 계정 탈퇴: 탈퇴 신청 후 7일이 지난 회원을 주기적으로 실제 삭제한다.
 * 매일 새벽 4시에 실행.
 */
@Component
public class WithdrawScheduler {

	@Autowired
	private MemberService memberService;

	@Scheduled(cron = "0 0 4 * * *")
	public void purgeExpired() {
		int deleted = memberService.purgeExpiredWithdrawals();
		if (deleted > 0) {
			System.out.println("[WithdrawScheduler] 탈퇴 7일 경과 회원 " + deleted + "명 삭제 완료");
		}
	}
}
