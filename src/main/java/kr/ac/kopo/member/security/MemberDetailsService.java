package kr.ac.kopo.member.security;

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

	@Autowired
	private MemberDAO memberDAO;

	@Override
	public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
		MemberVO member = memberDAO.selectById(id);
		if (member == null) {
			throw new UsernameNotFoundException("존재하지 않는 아이디: " + id);
		}
		if ("SUSPENDED".equals(member.getStatus())) {
			throw new UsernameNotFoundException("정지된 계정: " + id);
		}
		// 폼 로그인도 소셜 로그인과 동일한 공용 principal 을 사용한다(속성은 빈 맵).
		return new OwUserPrincipal(
			member.getId(),
			member.getPassword(),
			member.getRole(),
			Map.of()
		);
	}
}
