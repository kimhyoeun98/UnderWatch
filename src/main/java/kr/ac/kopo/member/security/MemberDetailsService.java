package kr.ac.kopo.member.security;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import kr.ac.kopo.member.dao.MemberDAO;
import kr.ac.kopo.member.vo.MemberVO;

@Service("memberDetailsService")
public class MemberDetailsService implements UserDetailsService {

	private static final DateTimeFormatter WITHDRAW_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Autowired
	private MemberDAO memberDAO;

	@Override
	public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
		MemberVO member = memberDAO.selectById(id);
		if (member == null) {
			throw new UsernameNotFoundException("존재하지 않는 아이디: " + id);
		}

		// 정지 계정: 잠금 principal 로 반환 → LockedException 유발(실패 핸들러가 안내 문구로 분기)
		boolean locked = "SUSPENDED".equals(member.getStatus());

		// 탈퇴 계정: 7일 이내면 로그인 허용(성공 시 LoginSuccessHandler 가 복구),
		// 7일이 지났으면 곧 스케줄러가 삭제할 대상이므로 로그인 차단.
		if ("WITHDRAWN".equals(member.getStatus()) && isWithdrawExpired(member.getWithdrawAt())) {
			throw new UsernameNotFoundException("탈퇴 유예기간이 지난 계정: " + id);
		}

		// 폼 로그인도 소셜 로그인과 동일한 공용 principal 을 사용한다(속성은 빈 맵).
		return new OwUserPrincipal(
			member.getId(),
			member.getPassword(),
			member.getRole(),
			Map.of(),
			locked
		);
	}

	/** 탈퇴 신청 후 7일이 지났으면 true(복구 불가). withdrawAt 이 없으면 만료로 간주하지 않음. */
	private boolean isWithdrawExpired(String withdrawAt) {
		if (withdrawAt == null || withdrawAt.isBlank()) {
			return false;
		}
		try {
			LocalDateTime requested = LocalDateTime.parse(withdrawAt, WITHDRAW_FMT);
			return requested.plusDays(7).isBefore(LocalDateTime.now());
		} catch (Exception e) {
			return false;   // 파싱 실패 시 안전하게 복구 허용 쪽으로
		}
	}
}
