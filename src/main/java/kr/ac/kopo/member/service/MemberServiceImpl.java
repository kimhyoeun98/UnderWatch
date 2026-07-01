package kr.ac.kopo.member.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
	 * 본인은 보통 0.5 이상, 남은 0.3 미만으로 나온다. 콘솔의 cos 로그를 보고 조정한다.
	 */
	private static final double COS_THRESHOLD = 0.38;

	@Override
	public boolean saveFace(String id, String imageData) {
		double[] emb = faceApi.embed(imageData);
		if (emb == null) {
			return false; // 얼굴 미검출 또는 API 오류
		}
		memberDAO.updateFaceDescriptor(id, encode(emb));   // 임베딩 JSON 을 ow_member 에 저장
		return true;
	}

	@Override
	public boolean hasFace(String id) {
		return memberDAO.countFace(id) > 0;
	}

	@Override
	public String matchFace(String imageData) {
		double[] probe = faceApi.embed(imageData);
		if (probe == null) {
			return null;
		}
		// 등록된 전 회원 임베딩과 1:N 코사인 유사도 비교 → 최고 유사도 회원 선택
		String bestId = null;
		double bestCos = -1.0;
		for (MemberVO m : memberDAO.selectAllFaces()) {
			double[] v = decode(m.getFaceDescriptor());
			if (v == null) {
				continue;
			}
			double cos = cosine(probe, v);
			if (cos > bestCos) {
				bestCos = cos;
				bestId = m.getId();
			}
		}
		System.out.println("[FaceLogin] best=" + bestId + " cos=" + bestCos);
		return bestCos >= COS_THRESHOLD ? bestId : null;
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
}
