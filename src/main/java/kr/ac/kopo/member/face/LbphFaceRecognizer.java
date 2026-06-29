package kr.ac.kopo.member.face;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * M-09 얼굴 인식 — LBPH(Local Binary Patterns Histogram)
 *
 * 외부 라이브러리/학습모델(face-api.js 등)을 쓰지 않고, 순수 JDK(ImageIO/AWT)만으로
 * 얼굴 이미지에서 특징(히스토그램)을 추출하고 비교한다.
 *
 * 처리 과정:
 *   1) base64 이미지 디코드 → BufferedImage
 *   2) 흑백 변환 + {@value #SIZE}x{@value #SIZE} 리사이즈
 *   3) 히스토그램 평활화(조명 보정)
 *   4) LBP 코드 계산(3x3 이웃 비교 → 0~255)
 *   5) {@value #GRID}x{@value #GRID} 셀로 나눠 셀별 256-bin 히스토그램을 이어붙임
 *   비교는 정규화 후 카이제곱 거리(낮을수록 유사).
 */
@Component
public class LbphFaceRecognizer {

	/** 얼굴을 정규화할 한 변의 픽셀 수 */
	public static final int SIZE = 128;
	/** 공간 정보를 담기 위한 셀 격자 (GRID x GRID) */
	public static final int GRID = 8;
	/** LBP 코드 가짓수 (8비트 = 0~255) */
	public static final int BINS = 256;

	private final ObjectMapper mapper = new ObjectMapper();

	/**
	 * 얼굴 이미지(data URL 또는 순수 base64)에서 LBPH 특징을 추출한다.
	 * @return 길이 GRID*GRID*BINS 의 히스토그램, 디코드 실패 시 null
	 */
	public int[] extract(String imageData) {
		BufferedImage img = decode(imageData);
		if (img == null) {
			return null;
		}
		int[][] gray = toGrayResized(img, SIZE, SIZE);
		equalize(gray, SIZE, SIZE);
		return lbphHistogram(gray, SIZE, SIZE);
	}

	/** 히스토그램 → JSON 문자열(DB 저장용) */
	public String toJson(int[] hist) {
		try {
			return mapper.writeValueAsString(hist);
		} catch (Exception e) {
			return null;
		}
	}

	/** JSON 문자열 → 히스토그램 (face-api 시절의 실수 벡터 등 형식이 다르면 null) */
	public int[] parse(String json) {
		try {
			return mapper.readValue(json, int[].class);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 두 히스토그램의 카이제곱 거리 — 각자를 확률분포로 정규화 후 계산하므로 0~2 범위.
	 * 낮을수록 비슷한 얼굴이다.
	 */
	public double distance(int[] a, int[] b) {
		if (a == null || b == null || a.length != b.length) {
			return Double.MAX_VALUE;
		}
		double sumA = 0, sumB = 0;
		for (int v : a) sumA += v;
		for (int v : b) sumB += v;
		if (sumA == 0 || sumB == 0) {
			return Double.MAX_VALUE;
		}
		double chi = 0;
		for (int i = 0; i < a.length; i++) {
			double pa = a[i] / sumA;
			double pb = b[i] / sumB;
			double denom = pa + pb;
			if (denom > 0) {
				double diff = pa - pb;
				chi += (diff * diff) / denom;
			}
		}
		return chi;
	}

	/** data URL("data:image/...;base64,XXXX") 또는 순수 base64 → BufferedImage */
	private BufferedImage decode(String imageData) {
		try {
			String base64 = imageData.contains(",")
					? imageData.substring(imageData.indexOf(',') + 1)
					: imageData;
			byte[] bytes = Base64.getDecoder().decode(base64.trim());
			return ImageIO.read(new ByteArrayInputStream(bytes));
		} catch (Exception e) {
			return null;
		}
	}

	/** 흑백 변환 + 지정 크기 리사이즈 → 2차원 명도 배열(0~255) */
	private int[][] toGrayResized(BufferedImage src, int w, int h) {
		BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = dst.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(src, 0, 0, w, h, null);
		g.dispose();

		int[][] gray = new int[h][w];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int rgb = dst.getRGB(x, y);
				int r = (rgb >> 16) & 0xff;
				int gg = (rgb >> 8) & 0xff;
				int b = rgb & 0xff;
				gray[y][x] = (int) Math.round(0.299 * r + 0.587 * gg + 0.114 * b);
			}
		}
		return gray;
	}

	/** 히스토그램 평활화 — 명암 분포를 펴서 조명 차이에 덜 민감하게 만든다 */
	private void equalize(int[][] gray, int w, int h) {
		int[] hist = new int[256];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				hist[gray[y][x]]++;
			}
		}
		int total = w * h;
		int[] cdf = new int[256];
		int acc = 0;
		for (int i = 0; i < 256; i++) {
			acc += hist[i];
			cdf[i] = acc;
		}
		int cdfMin = 0;
		for (int i = 0; i < 256; i++) {
			if (cdf[i] > 0) { cdfMin = cdf[i]; break; }
		}
		int denom = total - cdfMin;
		if (denom <= 0) {
			return; // 단색 이미지 등 — 평활화 생략
		}
		int[] lut = new int[256];
		for (int i = 0; i < 256; i++) {
			int v = (int) Math.round((double) (cdf[i] - cdfMin) / denom * 255);
			lut[i] = Math.max(0, Math.min(255, v));
		}
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				gray[y][x] = lut[gray[y][x]];
			}
		}
	}

	/** LBP 코드 계산 후 셀별 히스토그램을 이어붙인다 */
	private int[] lbphHistogram(int[][] gray, int w, int h) {
		int cellW = w / GRID;
		int cellH = h / GRID;
		int[] hist = new int[GRID * GRID * BINS];

		// 테두리(이웃이 부족한 픽셀)는 제외
		for (int y = 1; y < h - 1; y++) {
			for (int x = 1; x < w - 1; x++) {
				int c = gray[y][x];
				int code = 0;
				code |= (gray[y - 1][x - 1] >= c ? 1 : 0) << 7;
				code |= (gray[y - 1][x]     >= c ? 1 : 0) << 6;
				code |= (gray[y - 1][x + 1] >= c ? 1 : 0) << 5;
				code |= (gray[y][x + 1]     >= c ? 1 : 0) << 4;
				code |= (gray[y + 1][x + 1] >= c ? 1 : 0) << 3;
				code |= (gray[y + 1][x]     >= c ? 1 : 0) << 2;
				code |= (gray[y + 1][x - 1] >= c ? 1 : 0) << 1;
				code |= (gray[y][x - 1]     >= c ? 1 : 0);

				int cx = Math.min(x / cellW, GRID - 1);
				int cy = Math.min(y / cellH, GRID - 1);
				int cell = cy * GRID + cx;
				hist[cell * BINS + code]++;
			}
		}
		return hist;
	}
}
