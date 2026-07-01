package kr.ac.kopo.member.face;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * M-09 얼굴 인식 외부 API 클라이언트.
 * 파이썬 FastAPI 서버(InsightFace: RetinaFace 검출 + ArcFace 임베딩)를 호출해
 * 이미지 1장에서 512차원 정규화 임베딩을 받아온다. 임베딩은 L2 정규화돼 있어
 * 두 벡터의 내적이 곧 코사인 유사도가 된다(인식/비교는 Spring 쪽에서 수행).
 */
@Component
public class FaceApiClient {

	/** 파이썬 얼굴 API 주소. 기본 http://127.0.0.1:8000 (프로퍼티로 override 가능) */
	@Value("${face.api.url:http://127.0.0.1:8000}")
	private String baseUrl;

	private final RestTemplate rest;

	public FaceApiClient() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(3000);
		factory.setReadTimeout(15000);   // 첫 호출은 모델 로딩으로 느릴 수 있음
		this.rest = new RestTemplate(factory);
	}

	/**
	 * 이미지(data URL/base64)에서 얼굴 임베딩(512차원)을 추출한다.
	 * 얼굴을 못 찾거나 서버 오류면 null.
	 */
	@SuppressWarnings("unchecked")
	public double[] embed(String imageData) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, Object>> req = new HttpEntity<>(Map.of("image", imageData), headers);

			Map<String, Object> resp = rest.postForObject(baseUrl + "/embed", req, Map.class);
			if (resp == null || !Boolean.TRUE.equals(resp.get("success"))) {
				return null;
			}
			List<Number> emb = (List<Number>) resp.get("embedding");
			if (emb == null || emb.isEmpty()) {
				return null;
			}
			double[] v = new double[emb.size()];
			for (int i = 0; i < v.length; i++) {
				v[i] = emb.get(i).doubleValue();
			}
			return v;
		} catch (Exception e) {
			System.out.println("[FaceApi] embed 실패: " + e.getMessage());
			return null;
		}
	}
}
