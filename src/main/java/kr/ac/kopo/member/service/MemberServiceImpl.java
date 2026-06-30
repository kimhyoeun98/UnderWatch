package kr.ac.kopo.member.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import kr.ac.kopo.member.dao.MemberDAO;
import kr.ac.kopo.member.face.EigenFaceRecognizer;
import kr.ac.kopo.member.face.FaceStore;
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
		memberDAO.insert(member);
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

	// ===== M-09 얼굴 로그인 (Eigenfaces/PCA · 모델 영속화) =====

	@Autowired
	private EigenFaceRecognizer faceRecognizer;

	@Autowired
	private FaceStore faceStore;

	@Override
	public boolean saveFace(String id, String imageData) {
		int[] face = faceRecognizer.extractFace(imageData);
		if (face == null) {
			return false; // 이미지 디코드/추출 실패
		}
		faceStore.saveSample(id, imageData);   // 원본 얼굴 저장(재학습용)

		if (faceStore.modelExists()) {
			// 모델이 이미 있으면: 기존 고유공간에 새 얼굴만 투영해 벡터 저장
			EigenFaceRecognizer.Model model =
					new EigenFaceRecognizer.Model(faceStore.loadMean(), faceStore.loadEigenfaces());
			double[] w = faceRecognizer.project(model, face);
			if (w == null) {
				return false;
			}
			faceStore.saveVector(id, w);
		} else {
			// 모델이 없으면: 샘플이 2명 이상 모였을 때 초기 PCA 모델을 1회 학습
			buildInitialModelIfPossible();
			// 아직 모델이 없다면(등록 1명) PCA 대신 정규화 픽셀 벡터를 폴백 저장해
			// '등록됨' 상태로 만들고 1명만으로도 얼굴 로그인이 되게 한다.
			// 2번째 등록 때 buildInitialModelIfPossible()가 전원 PCA 벡터로 덮어쓴다.
			if (!faceStore.modelExists()) {
				double[] raw = faceRecognizer.rawVector(face);
				if (raw == null) {
					return false;
				}
				faceStore.saveVector(id, raw);
			}
		}
		return true;
	}

	/** 초기 PCA 모델 학습 — 샘플이 2개 이상이면 평균·고유얼굴 공간을 만들고 전원 투영 벡터를 저장한다. */
	private void buildInitialModelIfPossible() {
		List<String> okIds = new ArrayList<>();
		List<int[]> faces = new ArrayList<>();
		for (String sid : faceStore.sampleIds()) {
			int[] v = faceRecognizer.extractFaceFromBytes(faceStore.sampleBytes(sid));
			if (v != null) {
				okIds.add(sid);
				faces.add(v);
			}
		}
		EigenFaceRecognizer.Model model = faceRecognizer.buildModel(faces);
		if (model == null) {
			return; // 아직 2명 미만 — 다음 등록 때 다시 시도
		}
		faceStore.saveModel(model.mean, model.eigenfaces);
		for (int i = 0; i < okIds.size(); i++) {
			faceStore.saveVector(okIds.get(i), faceRecognizer.project(model, faces.get(i)));
		}
	}

	@Override
	public boolean hasFace(String id) {
		return faceStore.vectorExists(id);
	}

	@Override
	public String matchFace(String imageData) {
		int[] probe = faceRecognizer.extractFace(imageData);
		if (probe == null) {
			return null;
		}

		// 비교에 쓸 프로브 벡터와 임계값을 모드에 따라 결정한다.
		// - PCA 모델 있음(2명 이상): 고유공간 투영 벡터 + MATCH_THRESHOLD
		// - 모델 없음(1명, 폴백):   정규화 픽셀 벡터 + RAW_MATCH_THRESHOLD
		double[] pw;
		double threshold;
		String mode;
		if (faceStore.modelExists()) {
			EigenFaceRecognizer.Model model =
					new EigenFaceRecognizer.Model(faceStore.loadMean(), faceStore.loadEigenfaces());
			pw = faceRecognizer.project(model, probe);
			threshold = EigenFaceRecognizer.MATCH_THRESHOLD;
			mode = "PCA";
		} else {
			pw = faceRecognizer.rawVector(probe);
			threshold = EigenFaceRecognizer.RAW_MATCH_THRESHOLD;
			mode = "RAW";
		}
		if (pw == null) {
			return null;
		}

		// 저장된 회원 벡터들과 1:N 최근접 비교(길이가 다른 벡터는 distance가 무한대라 자동 제외)
		String bestId = null;
		double bestDist = Double.MAX_VALUE;
		for (Map.Entry<String, double[]> e : faceStore.allVectors().entrySet()) {
			double d = faceRecognizer.distance(pw, e.getValue());
			if (d < bestDist) {
				bestDist = d;
				bestId = e.getKey();
			}
		}
		System.out.println("[FaceLogin/" + mode + "] best=" + bestId + " dist=" + bestDist);
		return bestDist <= threshold ? bestId : null;
	}
}
