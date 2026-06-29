package kr.ac.kopo.member.face;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * M-09 얼굴 인식 — Eigenfaces(PCA) 구현.
 *
 * 외부 라이브러리/학습모델 없이 순수 JDK 만으로, 등록된 얼굴들로부터
 * "고유얼굴(주성분) 공간을 직접 학습"하고 그 공간에서 얼굴을 비교한다.
 *
 * 처리 과정:
 *   1) base64 이미지 → 흑백 → {@value #SIZE}x{@value #SIZE} 리사이즈 → 히스토그램 평활화 (얼굴 벡터)
 *   2) [학습] 등록 얼굴들의 평균 얼굴 계산 → 중심화
 *   3) [학습] 소표본 PCA: L = AᵀA(M×M)의 고유분해(Jacobi) → 고유얼굴 = A·v
 *   4) [인식] 입력 얼굴과 등록 얼굴들을 고유얼굴 공간에 투영 → 최근접(유클리드) 회원 선택
 *
 * 딥러닝이 아닌 고전 통계 기법이며, 저장값은 정규화 얼굴 벡터(int[]) 자체다.
 */
@Component
public class EigenFaceRecognizer {

	/** 얼굴 정규화 한 변의 픽셀 수 (64x64 = 4096차원) */
	public static final int SIZE = 64;
	public static final int DIM = SIZE * SIZE;
	/** 사용할 주성분(고유얼굴) 최대 개수 */
	public static final int MAX_COMPONENTS = 10;

	/**
	 * 같은 사람으로 인정하는 최대 거리(고유얼굴 공간, 유클리드). 환경/조명에 따라 달라지므로
	 * matchFace 호출 시 콘솔에 찍히는 dist 값을 보고 "같은 사람"과 "다른 사람" 사이로 조정한다.
	 */
	public static final double MATCH_THRESHOLD = 3.5;
	/** 등록 얼굴이 1장뿐일 때 쓰는 직접 비교(정규화 픽셀) 임계값 */
	public static final double SINGLE_THRESHOLD = 9.0;

	private final ObjectMapper mapper = new ObjectMapper();

	/** 이미지(data URL/base64) → 정규화 얼굴 벡터(int[DIM], 0~255). 저장용. */
	public int[] extractFace(String imageData) {
		BufferedImage img = decode(imageData);
		if (img == null) {
			return null;
		}
		int[][] gray = toGrayResized(img, SIZE, SIZE);
		equalize(gray, SIZE, SIZE);
		int[] v = new int[DIM];
		for (int y = 0; y < SIZE; y++) {
			for (int x = 0; x < SIZE; x++) {
				v[y * SIZE + x] = gray[y][x];
			}
		}
		return v;
	}

	public String toJson(int[] v) {
		try {
			return mapper.writeValueAsString(v);
		} catch (Exception e) {
			return null;
		}
	}

	public int[] parse(String json) {
		try {
			return mapper.readValue(json, int[].class);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 입력 얼굴(probe)을 등록 얼굴들(gallery)과 PCA 고유공간에서 비교해 가장 가까운 회원 id를 반환한다.
	 * 임계값 이내가 없으면 null. 최근접 거리는 콘솔 로그로 남긴다(임계값 조정용).
	 */
	public String recognize(int[] probe, List<String> ids, List<int[]> gallery) {
		if (probe == null || probe.length != DIM || ids.isEmpty()) {
			return null;
		}
		int m = ids.size();
		double[] p = scale(probe);
		double[][] G = new double[m][];
		for (int i = 0; i < m; i++) {
			G[i] = scale(gallery.get(i));
		}

		// [학습 1] 평균 얼굴
		double[] mean = new double[DIM];
		for (double[] g : G) {
			for (int k = 0; k < DIM; k++) mean[k] += g[k];
		}
		for (int k = 0; k < DIM; k++) mean[k] /= m;

		// 중심화
		double[][] A = new double[m][DIM];
		for (int i = 0; i < m; i++) {
			for (int k = 0; k < DIM; k++) A[i][k] = G[i][k] - mean[k];
		}
		double[] phi = new double[DIM];
		for (int k = 0; k < DIM; k++) phi[k] = p[k] - mean[k];

		int K = Math.min(MAX_COMPONENTS, m - 1);
		if (K < 1) {
			// 등록 얼굴 1장 — 고유공간을 만들 수 없어 정규화 픽셀로 직접 비교
			double d = euclid(p, G[0], DIM);
			System.out.println("[FaceLogin/PCA] single-face dist=" + d);
			return d <= SINGLE_THRESHOLD ? ids.get(0) : null;
		}

		// [학습 2] 소표본 PCA: L = AAᵀ (M×M) 의 고유분해
		double[][] L = new double[m][m];
		for (int i = 0; i < m; i++) {
			for (int j = i; j < m; j++) {
				double s = dot(A[i], A[j]);
				L[i][j] = s;
				L[j][i] = s;
			}
		}
		double[] eval = new double[m];
		double[][] evec = new double[m][m];
		jacobi(L, eval, evec);
		Integer[] order = new Integer[m];
		for (int i = 0; i < m; i++) order[i] = i;
		Arrays.sort(order, (x, y) -> Double.compare(eval[y], eval[x]));

		// [학습 3] 상위 고유얼굴 = A·v (정규화)
		double[][] U = new double[K][];
		int kept = 0;
		for (int c = 0; c < K; c++) {
			int idx = order[c];
			if (eval[idx] <= 1e-8) {
				break;
			}
			double[] u = new double[DIM];
			for (int i = 0; i < m; i++) {
				double w = evec[i][idx];
				for (int k = 0; k < DIM; k++) u[k] += w * A[i][k];
			}
			double nrm = Math.sqrt(dot(u, u));
			if (nrm < 1e-12) {
				continue;
			}
			for (int k = 0; k < DIM; k++) u[k] /= nrm;
			U[kept++] = u;
		}
		if (kept < 1) {
			return null;
		}

		// [인식] 고유공간 투영 후 최근접
		double[] wp = project(phi, U, kept);
		String bestId = null;
		double bestDist = Double.MAX_VALUE;
		for (int i = 0; i < m; i++) {
			double[] wi = project(A[i], U, kept);
			double d = euclid(wp, wi, kept);
			if (d < bestDist) {
				bestDist = d;
				bestId = ids.get(i);
			}
		}
		System.out.println("[FaceLogin/PCA] best=" + bestId + " dist=" + bestDist + " (eigen K=" + kept + ")");
		return bestDist <= MATCH_THRESHOLD ? bestId : null;
	}

	// ===== 선형대수 =====

	private double[] scale(int[] v) {
		double[] d = new double[v.length];
		for (int i = 0; i < v.length; i++) d[i] = v[i] / 255.0;
		return d;
	}

	private double dot(double[] a, double[] b) {
		double s = 0;
		for (int i = 0; i < a.length; i++) s += a[i] * b[i];
		return s;
	}

	private double euclid(double[] a, double[] b, int n) {
		double s = 0;
		for (int i = 0; i < n; i++) {
			double d = a[i] - b[i];
			s += d * d;
		}
		return Math.sqrt(s);
	}

	private double[] project(double[] phi, double[][] U, int k) {
		double[] w = new double[k];
		for (int c = 0; c < k; c++) w[c] = dot(U[c], phi);
		return w;
	}

	/** 대칭행렬 Jacobi 고유분해 — d=고유값, v의 열=고유벡터 */
	private void jacobi(double[][] in, double[] d, double[][] v) {
		int n = d.length;
		double[][] a = new double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				a[i][j] = in[i][j];
				v[i][j] = (i == j) ? 1.0 : 0.0;
			}
		}
		for (int sweep = 0; sweep < 100; sweep++) {
			double off = 0;
			for (int i = 0; i < n; i++) {
				for (int j = i + 1; j < n; j++) off += a[i][j] * a[i][j];
			}
			if (off < 1e-18) {
				break;
			}
			for (int p = 0; p < n - 1; p++) {
				for (int q = p + 1; q < n; q++) {
					if (Math.abs(a[p][q]) < 1e-300) {
						continue;
					}
					double theta = (a[q][q] - a[p][p]) / (2 * a[p][q]);
					double t = Math.signum(theta) / (Math.abs(theta) + Math.sqrt(theta * theta + 1));
					if (theta == 0) t = 1;
					double c = 1 / Math.sqrt(t * t + 1);
					double s = t * c;
					for (int k = 0; k < n; k++) { double kp = a[k][p], kq = a[k][q]; a[k][p] = c * kp - s * kq; a[k][q] = s * kp + c * kq; }
					for (int k = 0; k < n; k++) { double pk = a[p][k], qk = a[q][k]; a[p][k] = c * pk - s * qk; a[q][k] = s * pk + c * qk; }
					for (int k = 0; k < n; k++) { double kp = v[k][p], kq = v[k][q]; v[k][p] = c * kp - s * kq; v[k][q] = s * kp + c * kq; }
				}
			}
		}
		for (int i = 0; i < n; i++) d[i] = a[i][i];
	}

	// ===== 이미지 전처리 (LBPH 인식기와 동일 방식) =====

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

	private void equalize(int[][] gray, int w, int h) {
		int[] hist = new int[256];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) hist[gray[y][x]]++;
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
			return;
		}
		int[] lut = new int[256];
		for (int i = 0; i < 256; i++) {
			int val = (int) Math.round((double) (cdf[i] - cdfMin) / denom * 255);
			lut[i] = Math.max(0, Math.min(255, val));
		}
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) gray[y][x] = lut[gray[y][x]];
		}
	}
}
