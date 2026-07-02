package kr.ac.kopo.member.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import kr.ac.kopo.member.dao.MemberDAO;
import kr.ac.kopo.member.face.FaceApiClient;
import kr.ac.kopo.member.vo.MemberVO;

@Service
public class MemberServiceImpl implements MemberService {

	@Autowired
	private MemberDAO memberDAO;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Override
	public void register(MemberVO member) {
		member.setPassword(passwordEncoder.encode(member.getPassword()));
		member.setPhone(normalizePhone(member.getPhone()));   // 하이픈 유무와 무관하게 표준 형식으로 저장
		memberDAO.insert(member);
	}

	/** 전화번호를 숫자만 추출해 표준 하이픈 형식(010-1234-5678 / 02-123-4567 등)으로 정규화. */
	private String normalizePhone(String phone) {
		if (phone == null) {
			return null;
		}
		String d = phone.replaceAll("[^0-9]", "");
		if (d.isEmpty()) {
			return phone;   // 미입력(선택 항목)
		}
		if (d.length() == 11) {                       // 010-1234-5678
			return d.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
		}
		if (d.length() == 10 && d.startsWith("02")) { // 02-1234-5678
			return d.replaceFirst("(\\d{2})(\\d{4})(\\d{4})", "$1-$2-$3");
		}
		if (d.length() == 10) {                       // 010-123-4567 / 031-123-4567
			return d.replaceFirst("(\\d{3})(\\d{3})(\\d{4})", "$1-$2-$3");
		}
		if (d.length() == 9 && d.startsWith("02")) {  // 02-123-4567
			return d.replaceFirst("(\\d{2})(\\d{3})(\\d{4})", "$1-$2-$3");
		}
		return phone;   // 예외 길이는 입력값 유지(패턴 검증에서 이미 걸러짐)
	}

	@Override
	public MemberVO findById(String id) {
		return memberDAO.selectById(id);
	}

	@Override
	public boolean isIdDuplicate(String id) {
		return memberDAO.existsId(id);
	}

	@Override
	public boolean isNicknameDuplicate(String nickname) {
		return memberDAO.existsNickname(nickname);
	}

	@Override
	public boolean isEmailDuplicate(String email) {
		return memberDAO.existsEmail(email);
	}

	@Override
	public void updateInfo(MemberVO member) {
		memberDAO.updateInfo(member);
	}

	@Override
	public void changePassword(String id, String newRawPassword) {
		MemberVO member = new MemberVO();
		member.setId(id);
		member.setPassword(passwordEncoder.encode(newRawPassword));
		memberDAO.updatePassword(member);
	}

	@Override
	public List<MemberVO> getAllMembers() {
		return memberDAO.selectAll();
	}

	@Override
	public void changeStatus(String id, String status) {
		memberDAO.updateStatus(id, status);
	}

	@Override
	public void changeRole(String id, String role) {
		memberDAO.updateRole(id, role);
	}

	@Override
	public void recalcGrade(String id) {
		if (id != null) {
			memberDAO.recalcGradePoint(id);
		}
	}

	@Override
	public boolean withdraw(String id, String rawPassword) {
		MemberVO member = memberDAO.selectById(id);
		if (member == null || member.getPassword() == null) {
			return false;   // 소셜 전용 계정 등 비밀번호가 없으면 이 경로로는 탈퇴 불가
		}
		if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
			return false;   // 비밀번호 불일치
		}
		memberDAO.requestWithdraw(id);   // status=WITHDRAWN, withdraw_at=SYSDATE (7일 후 삭제 대상)
		return true;
	}

	@Override
	public void reactivateOnLogin(String id) {
		// WITHDRAWN 상태에서만 복구된다(cancelWithdraw 의 WHERE 절이 상태를 확인).
		// 7일 경과분은 MemberDetailsService 에서 로그인 자체가 막히므로 여기 도달하지 않는다.
		memberDAO.cancelWithdraw(id);
	}

	@Override
	public int purgeExpiredWithdrawals() {
		int count = 0;
		for (String id : memberDAO.selectExpiredWithdrawals()) {
			try {
				memberDAO.purgeMember(id);
				count++;
			} catch (Exception e) {
				// 일부 회원 정리 실패해도 나머지는 계속 진행(다음 주기에 재시도)
				System.out.println("[Withdraw] purge 실패: " + id + " - " + e.getMessage());
			}
		}
		return count;
	}

	@Override
	public void updateProfileImg(String id, String profileImg) {
		memberDAO.updateProfileImg(id, profileImg);
	}

	@Override
	public boolean canChangeNickname(String id) {
		return memberDAO.canChangeNickname(id) > 0;
	}

	@Override
	public void markNicknameChanged(String id) {
		memberDAO.updateNicknameChanged(id);
	}

	// ===== M-09 얼굴 로그인 (외부 API: InsightFace ArcFace 임베딩 + 코사인 비교) =====

	@Autowired
	private FaceApiClient faceApi;

	/**
	 * 같은 사람으로 인정하는 최소 코사인 유사도. ArcFace(정규화 임베딩) 기준
	 * 본인은 보통 0.5 이상, 다른 사람은 0.3 미만으로 나온다. 그 사이(0.3~0.5)는
	 * 닮은 사람이 걸리는 애매 구간이라, 오수락은 막으면서 본인 거절은 줄이도록
	 * 기본값을 0.45 로 둔다. face.match.threshold 프로퍼티로 cos 로그를 보며 조정.
	 */
	@Value("${face.match.threshold:0.45}")
	private double cosThreshold;

	/**
	 * 1:N 매칭에서 1위와 2위 유사도의 최소 격차. 닮은 두 사람이 모두 임계값을 넘고
	 * 점수마저 비슷하면(격차 &lt; margin) 누구인지 단정할 수 없으므로 거절한다.
	 */
	@Value("${face.match.margin:0.06}")
	private double cosMargin;

	/** 한 계정에 저장하는 얼굴 사진(임베딩) 최대 개수. 여러 각도를 저장해 인식률을 높인다. */
	private static final int MAX_FACES = 3;

	@Override
	public boolean saveFace(String id, String imageData) {
		double[] emb = faceApi.embed(imageData);
		if (emb == null) {
			return false; // 얼굴 미검출 또는 API 오류
		}
		// 기존 임베딩에 이어붙이되 최대 MAX_FACES 장 유지(초과 시 가장 오래된 것부터 제거)
		List<double[]> faces = decodeMulti(memberDAO.selectFaceDescriptor(id));
		faces.add(emb);
		while (faces.size() > MAX_FACES) {
			faces.remove(0);
		}
		memberDAO.updateFaceDescriptor(id, encodeMulti(faces));
		return true;
	}

	@Override
	public boolean hasFace(String id) {
		return memberDAO.countFace(id) > 0;
	}

	@Override
	public int faceCount(String id) {
		return decodeMulti(memberDAO.selectFaceDescriptor(id)).size();
	}

	@Override
	public String matchFace(String imageData) {
		double[] probe = faceApi.embed(imageData);
		if (probe == null) {
			return null;
		}
		// 등록된 전 회원과 1:N 비교. 회원마다 여러 장(최대 3) 등록될 수 있으므로
		// 그 회원의 임베딩 중 최고 유사도를 그 회원의 점수로 삼는다 → 1위/2위 회원 추적
		String bestId = null;
		double bestCos = -1.0;
		double secondCos = -1.0;
		for (MemberVO m : memberDAO.selectAllFaces()) {
			double memberCos = -1.0;
			for (double[] v : decodeMulti(m.getFaceDescriptor())) {
				double cos = cosine(probe, v);
				if (cos > memberCos) {
					memberCos = cos;
				}
			}
			if (memberCos < 0) {
				continue;
			}
			if (memberCos > bestCos) {
				secondCos = bestCos;
				bestCos = memberCos;
				bestId = m.getId();
			} else if (memberCos > secondCos) {
				secondCos = memberCos;
			}
		}
		System.out.println("[FaceLogin] best=" + bestId + " cos=" + bestCos + " second=" + secondCos
				+ " (threshold=" + cosThreshold + ", margin=" + cosMargin + ")");

		// ① 유사도가 임계값 미만이면 등록 안 된/다른 사람 → 거절
		if (bestCos < cosThreshold) {
			return null;
		}
		// ② 2위도 임계값을 넘고 1위와 격차가 작으면(닮은 사람이 함께 걸림) 단정 불가 → 거절
		if (secondCos >= cosThreshold && (bestCos - secondCos) < cosMargin) {
			System.out.println("[FaceLogin] 1·2위 격차 부족으로 거절: " + (bestCos - secondCos));
			return null;
		}
		return bestId;
	}

	/** 정규화 임베딩 간 코사인 유사도(= 내적). 길이가 다르면 -1. */
	private double cosine(double[] a, double[] b) {
		if (a == null || b == null || a.length != b.length) {
			return -1.0;
		}
		double dot = 0;
		for (int i = 0; i < a.length; i++) dot += a[i] * b[i];
		return dot;
	}

	/** double[] → "[v1,v2,...]" JSON 문자열(의존성 없는 단순 직렬화). */
	private String encode(double[] v) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < v.length; i++) {
			if (i > 0) sb.append(',');
			sb.append(v[i]);
		}
		return sb.append(']').toString();
	}

	/** "[v1,v2,...]" → double[]. 파싱 실패 시 null. */
	private double[] decode(String json) {
		if (json == null || json.length() < 2) {
			return null;
		}
		try {
			String body = json.trim();
			body = body.substring(body.indexOf('[') + 1, body.lastIndexOf(']'));
			if (body.trim().isEmpty()) {
				return null;
			}
			String[] parts = body.split(",");
			double[] v = new double[parts.length];
			for (int i = 0; i < parts.length; i++) v[i] = Double.parseDouble(parts[i].trim());
			return v;
		} catch (Exception e) {
			return null;
		}
	}

	/** List&lt;double[]&gt; → "[[..],[..]]" (임베딩 여러 장을 하나의 JSON 으로). */
	private String encodeMulti(List<double[]> list) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) sb.append(',');
			sb.append(encode(list.get(i)));
		}
		return sb.append(']').toString();
	}

	/**
	 * 저장된 얼굴 JSON 을 임베딩 목록으로 파싱.
	 * 신규 형식 "[[..],[..]]" 은 여러 장, 구(舊) 단일 형식 "[..]" 은 한 장으로 호환 처리한다.
	 */
	private List<double[]> decodeMulti(String json) {
		List<double[]> out = new ArrayList<>();
		if (json == null) {
			return out;
		}
		String s = json.trim();
		if (s.length() < 2 || s.charAt(0) != '[') {
			return out;
		}
		int i = 1;
		while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
		if (i < s.length() && s.charAt(i) == '[') {
			// 신규: 최상위 대괄호 안의 각 "[..]" 그룹을 하나의 임베딩으로 분리
			int depth = 0, start = -1;
			for (int k = 0; k < s.length(); k++) {
				char c = s.charAt(k);
				if (c == '[') {
					if (depth == 1) start = k;
					depth++;
				} else if (c == ']') {
					depth--;
					if (depth == 1 && start >= 0) {
						double[] e = decode(s.substring(start, k + 1));
						if (e != null) out.add(e);
						start = -1;
					}
				}
			}
		} else {
			// 구 단일 형식
			double[] e = decode(s);
			if (e != null) out.add(e);
		}
		return out;
	}
}
