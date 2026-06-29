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

/**
 * M-09 얼굴 인식 — Eigenfaces(PCA) (모델 영속화 버전).
 *
 * 외부 라이브러리/학습모델 없이 순수 JDK 만으로:
 *   1) [학습 1회] 초기 등록 얼굴들로 평균 얼굴 + 고유얼굴(주성분) 공간을 만든다(buildModel).
 *   2) [등록]    이후 새 얼굴은 "기존 고유공간에 투영"만 해서 가중치 벡터를 얻는다(project).
 *   3) [인식]    입력 얼굴을 투영한 뒤, 저장된 회원 벡터들과 최근접(유클리드) 비교한다.
 *
 * 핵심인 고유분해(Jacobi 회전법)까지 직접 구현했다. 딥러닝이 아닌 고전 통계 기법이다.
 */
@Component
public class EigenFaceRecognizer {

	/** 얼굴 정규화 한 변의 픽셀 수 (64x64 = 4096차원) */
	public static final int SIZE = 64;
	public static final int DIM = SIZE * SIZE;
	/** 사용할 주성분(고유얼굴) 최대 개수 */
	public static final int MAX_COMPONENTS = 10;

	/**
	 * 같은 사람으로 인정하는 최대 거리(고유공간, 유클리드). 환경/조명에 따라 달라지므로
	 * 로그인 시 콘솔에 찍히는 dist 값을 보고 "같은 사람"과 "다른 사람" 사이로 조정한다.
	 */
	public static final double MATCH_THRESHOLD = 3.5;

	/** 학습된 PCA 모델: 평균 얼굴 + 고유얼굴(정규화 K개) */
	public static class Model {
		public final double[] mean;        // 길이 DIM
		public final double[][] eigenfaces; // K x DIM
		public Model(double[] mean, double[][] eigenfaces) {
			this.mean = mean;
			this.eigenfaces = eigenfaces;
		}
		public boolean valid() {
			return mean != null && mean.length == DIM && eigenfaces != null && eigenfaces.length > 0;
		}
	}

	// ===== 이미지 → 정규화 얼굴 벡터 =====

	/** data URL/base64 문자열 → 정규화 얼굴 벡터(int[DIM], 0~255). 디코드 실패 시 null */
	public int[] extractFace(String imageData) {
		try {
			String base64 = imageData.contains(",")
					? imageData.substring(imageData.indexOf(',') + 1)
					: imageData;
			return extractFaceFromBytes(Base64.getDecoder().decode(base64.trim()));
		} catch (Exception e) {
			return null;
		}
	}

	/** 이미지 바이트(jpg/png) → 정규화 얼굴 벡터(int[DIM]). 디코드 실패 시 null */
	public int[] extractFaceFromBytes(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		try {
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
			if (img == null) {
				return null;
			}
			int[][] gray = toGrayResized(img, SIZE, SIZE);
			equalize(gray, SIZE, SIZE);
			int[] v = new int[DIM];
			for (int y = 0; y < SIZE; y++) {
				for (int x = 0; x < SIZE; x++) v[y * SIZE + x] = gray[y][x];
			}
			return v;
		} catch (Exception e) {
			return null;
		}
	}

	// ===== PCA 학습 / 투영 / 비교 =====

	/**
	 * 얼굴 벡터들로 PCA 모델(평균 + 고유얼굴)을 학습한다. 2개 이상 필요(미만이면 null).
	 * 소표본 트릭: 공분산 D×D 대신 L = AAᵀ(M×M)의 고유분해(Jacobi)로 계산한다.
	 */
	public Model buildModel(List<int[]> faces) {
		int m = faces.size();
		if (m < 2) {
			return null;
		}
		double[][] G = new double[m][];
		for (int i = 0; i < m; i++) {
			if (faces.get(i) == null || faces.get(i).length != DIM) {
				return null;
			}
			G[i] = scale(faces.get(i));
		}
		// 평균 얼굴 + 중심화
		double[] mean = new double[DIM];
		for (double[] g : G) {
			for (int k = 0; k < DIM; k++) mean[k] += g[k];
		}
		for (int k = 0; k < DIM; k++) mean[k] /= m;
		double[][] A = new double[m][DIM];
		for (int i = 0; i < m; i++) {
			for (int k = 0; k < DIM; k++) A[i][k] = G[i][k] - mean[k];
		}
		// L = AAᵀ (M×M) 고유분해
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

		int K = Math.min(MAX_COMPONENTS, m - 1);
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
		return new Model(mean, Arrays.copyOf(U, kept));
	}

	/** 얼굴 벡터를 모델의 고유공간에 투영 → 가중치 벡터(double[K]). 실패 시 null */
	public double[] project(Model model, int[] face) {
		if (model == null || !model.valid() || face == null || face.length != DIM) {
			return null;
		}
		double[] f = scale(face);
		double[] phi = new double[DIM];
		for (int k = 0; k < DIM; k++) phi[k] = f[k] - model.mean[k];
		int K = model.eigenfaces.length;
		double[] w = new double[K];
		for (int c = 0; c < K; c++) w[c] = dot(model.eigenfaces[c], phi);
		return w;
	}

	/** 두 가중치 벡터의 유클리드 거리. 길이가 다르면 무한대 */
	public double distance(double[] a, double[] b) {
		if (a == null || b == null || a.length != b.length) {
			return Double.MAX_VALUE;
		}
		double s = 0;
		for (int i = 0; i < a.length; i++) {
			double d = a[i] - b[i];
			s += d * d;
		}
		return Math.sqrt(s);
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

	// ===== 이미지 전처리 =====

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
