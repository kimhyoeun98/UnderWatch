package kr.ac.kopo.member.face;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;

/**
 * M-09 얼굴 인식 파일 저장소 — PCA 모델·샘플·벡터를 디스크에 영속화한다.
 *
 * <pre>
 * face-data/
 *   model/    mean.dat, eigenfaces.dat   ← 1회 학습한 PCA 모델
 *   samples/  {id}_1.png                 ← 등록 얼굴 원본(재학습용)
 *   vectors/  {id}.vec                   ← 회원별 고유공간 투영 벡터
 * </pre>
 */
@Component
public class FaceStore {

	private static final String BASE = "D:/Serv/ServM/face-data/";

	private final File modelDir = new File(BASE, "model");
	private final File samplesDir = new File(BASE, "samples");
	private final File vectorsDir = new File(BASE, "vectors");

	private void ensureDirs() {
		modelDir.mkdirs();
		samplesDir.mkdirs();
		vectorsDir.mkdirs();
	}

	// ===== 샘플(원본 얼굴) =====

	/** data URL/base64 이미지를 PNG 로 samples/{id}_1.png 에 저장 */
	public void saveSample(String id, String imageData) {
		ensureDirs();
		try {
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(decode(imageData)));
			if (img != null) {
				ImageIO.write(img, "png", new File(samplesDir, id + "_1.png"));
			}
		} catch (Exception e) {
			// 저장 실패는 무시(로그성)
		}
	}

	/** 샘플이 저장된 회원 id 목록 */
	public List<String> sampleIds() {
		ensureDirs();
		List<String> ids = new ArrayList<>();
		File[] files = samplesDir.listFiles((d, n) -> n.endsWith("_1.png"));
		if (files != null) {
			for (File f : files) ids.add(f.getName().substring(0, f.getName().length() - "_1.png".length()));
		}
		return ids;
	}

	public byte[] sampleBytes(String id) {
		try {
			return Files.readAllBytes(new File(samplesDir, id + "_1.png").toPath());
		} catch (Exception e) {
			return null;
		}
	}

	// ===== 모델 =====

	public boolean modelExists() {
		return new File(modelDir, "mean.dat").exists() && new File(modelDir, "eigenfaces.dat").exists();
	}

	public void saveModel(double[] mean, double[][] eigenfaces) {
		ensureDirs();
		writeVector(new File(modelDir, "mean.dat"), mean);
		writeMatrix(new File(modelDir, "eigenfaces.dat"), eigenfaces);
	}

	public double[] loadMean() {
		return readVector(new File(modelDir, "mean.dat"));
	}

	public double[][] loadEigenfaces() {
		return readMatrix(new File(modelDir, "eigenfaces.dat"));
	}

	// ===== 회원별 투영 벡터 =====

	public void saveVector(String id, double[] weights) {
		ensureDirs();
		writeVector(new File(vectorsDir, id + ".vec"), weights);
	}

	public boolean vectorExists(String id) {
		return new File(vectorsDir, id + ".vec").exists();
	}

	/** 모든 회원 벡터 (id → weights) */
	public Map<String, double[]> allVectors() {
		ensureDirs();
		Map<String, double[]> map = new LinkedHashMap<>();
		File[] files = vectorsDir.listFiles((d, n) -> n.endsWith(".vec"));
		if (files != null) {
			for (File f : files) {
				double[] w = readVector(f);
				if (w != null) {
					map.put(f.getName().substring(0, f.getName().length() - ".vec".length()), w);
				}
			}
		}
		return map;
	}

	// ===== 바이너리 입출력 (의존성 없는 직렬화) =====

	private void writeVector(File f, double[] v) {
		try (DataOutputStream out = new DataOutputStream(new FileOutputStream(f))) {
			out.writeInt(v.length);
			for (double d : v) out.writeDouble(d);
		} catch (Exception e) {
			// 무시(로그성)
		}
	}

	private double[] readVector(File f) {
		try (DataInputStream in = new DataInputStream(new FileInputStream(f))) {
			int n = in.readInt();
			double[] v = new double[n];
			for (int i = 0; i < n; i++) v[i] = in.readDouble();
			return v;
		} catch (Exception e) {
			return null;
		}
	}

	private void writeMatrix(File f, double[][] m) {
		try (DataOutputStream out = new DataOutputStream(new FileOutputStream(f))) {
			out.writeInt(m.length);
			out.writeInt(m.length > 0 ? m[0].length : 0);
			for (double[] row : m) {
				for (double d : row) out.writeDouble(d);
			}
		} catch (Exception e) {
			// 무시(로그성)
		}
	}

	private double[][] readMatrix(File f) {
		try (DataInputStream in = new DataInputStream(new FileInputStream(f))) {
			int rows = in.readInt();
			int cols = in.readInt();
			double[][] m = new double[rows][cols];
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) m[i][j] = in.readDouble();
			}
			return m;
		} catch (Exception e) {
			return null;
		}
	}

	private byte[] decode(String imageData) {
		String base64 = imageData.contains(",")
				? imageData.substring(imageData.indexOf(',') + 1)
				: imageData;
		return Base64.getDecoder().decode(base64.trim());
	}
}
