# 얼굴 인식 로그인

**Eigenfaces(PCA, 주성분 분석)** 를 구현한 1:N 얼굴 로그인 문서입니다. 외부 라이브러리·학습모델 없이, 등록된 얼굴들로부터 "고유얼굴 공간"을 학습해 인식합니다.

> 같은 파이프라인의 LBPH 버전(`LbphFaceRecognizer`)도 대체 구현으로 보유하고 있습니다(11장 참고).

---

## 1. 동작 개요

```text
[모델 학습 — 처음 1회]
초기 등록 얼굴들 → 평균 얼굴 + 고유얼굴(주성분) 공간 계산 → /face-data/model 에 저장

[등록]
마이페이지 → 웹캠 캡처 → 이미지 전송
        → 기존 PCA 모델에 새 얼굴 투영 → /face-data/vectors/{id}.vec 저장

[로그인]
로그인 화면 → 웹캠 캡처 → 이미지 전송
        → 입력 얼굴 투영 → 저장된 회원 벡터들과 1:N 최근접 비교 → 세션 로그인
```

브라우저는 **이미지만** 보내고, 특징 추출·학습·비교는 전부 서버(Java)에서 합니다.

---

## 2. 구성 요소

| 위치                   | 역할                                  |
| -------------------- | ----------------------------------- |
| `EigenFaceRecognizer`| **PCA 구현** (정규화·학습·투영·비교 수학)  |
| `FaceStore`          | 모델·샘플·벡터 파일 영속화(`/face-data`)     |
| `LbphFaceRecognizer` | LBPH 대체 구현(미사용, fallback)            |
| `MemberServiceImpl`  | 얼굴 등록 저장 / 1:N 매칭                    |
| `MemberController`   | 얼굴 등록 / 얼굴 로그인 요청 처리                |
| `faceLogin.jsp`      | 얼굴 로그인 화면 (웹캠 캡처)                   |
| `mypage.jsp`         | 얼굴 등록 UI (웹캠 캡처)                    |
| `spring-security.xml`| 얼굴 로그인 URL 접근 권한 설정                 |

---

## 3. 클라이언트 처리 (외부 라이브러리 없음)

웹캠 영상의 중앙 정사각형을 잘라 base64 JPEG 문자열로 서버에 보냅니다. 화면의 점선 가이드 박스 안에 얼굴을 맞추게 해서 별도의 얼굴 탐지를 생략합니다.

```js
function captureSquare(size) {
  var side = Math.min(video.videoWidth, video.videoHeight);
  var sx = (video.videoWidth - side) / 2, sy = (video.videoHeight - side) / 2;
  var canvas = document.createElement('canvas');
  canvas.width = size; canvas.height = size;
  canvas.getContext('2d').drawImage(video, sx, sy, side, side, 0, 0, size, size);
  return canvas.toDataURL('image/jpeg', 0.92);   // → 'image' 파라미터로 POST
}
```

---

## 4. 서버 — 얼굴 정규화 (`EigenFaceRecognizer.extractFace`)

JDK(`ImageIO`, `AWT`)로 처리합니다.

1. base64 이미지 디코드 → `BufferedImage`
2. 흑백 변환 + **64×64 리사이즈** → 4096차원
3. 히스토그램 평활화(조명 보정)
4. 0~255 명도 벡터(`int[4096]`) 생성

이 정규화 벡터가 PCA의 입력(등록 얼굴)이 됩니다. 원본 이미지는 `samples/`에, 학습 후 투영 벡터는 `vectors/{id}.vec`에 `FaceStore`가 저장합니다(5장).

---

## 5. 서버 — PCA 모델 학습(1회) + 등록 + 인식

**모델 학습(buildModel) — 처음 한 번**
1. 초기 등록 얼굴들의 **평균 얼굴** 계산 → 각 얼굴을 평균에서 빼서 **중심화**(행렬 A)
2. 차원이 커서(4096) 공분산 `AᵀA`(4096×4096) 대신 **소표본 트릭** — `L = AAᵀ`(M×M, M=등록 인원)의 고유분해
3. `L`의 고유분해는 **Jacobi 회전법**으로 계산 → 상위 고유벡터로 **고유얼굴**을 만든다(`고유얼굴 = A·v`, 정규화)
4. 평균·고유얼굴을 `/face-data/model`(mean.dat, eigenfaces.dat)에 저장

**등록(saveFace)** — 모델이 이미 있으면 **기존 고유공간에 새 얼굴만 투영**해 `{id}.vec` 저장(재학습 없음)

**인식(matchFace)**
5. 입력 얼굴을 고유공간에 **투영**(가중치 벡터)
6. 저장된 회원 벡터들과 가장 가까운(유클리드) 회원 선택 → 임계값 이내면 로그인

```java
public static final double MATCH_THRESHOLD = 3.5;  // 고유공간 거리, 환경에 맞게 조정

Model model = new Model(faceStore.loadMean(), faceStore.loadEigenfaces());
double[] pw = recognizer.project(model, probe);       // 입력 얼굴 투영
for (var e : faceStore.allVectors().entrySet()) {     // 저장된 회원 벡터들과 비교
    double d = recognizer.distance(pw, e.getValue());
    ...
}
```

* 모델은 **처음 1회만 학습**해 파일로 저장하고, 이후 새 등록자는 **기존 공간에 투영만** 합니다(재학습 안 함). 등록자가 크게 늘면 모델을 다시 만들어 주는 게 정확도에 좋습니다.
* `matchFace`는 최근접 거리를 콘솔(`[FaceLogin/PCA] dist=...`)에 남기므로, 그 값을 보고 같은 사람/다른 사람 사이로 `MATCH_THRESHOLD`를 조정하면 됩니다.

---

## 6. 수동 세션 로그인

얼굴 로그인은 비밀번호 인증을 거치지 않으므로 `SecurityContext`를 직접 생성합니다.

```java
UserDetails principal = memberDetailsService.loadUserByUsername(matchedId);
var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
var context = SecurityContextHolder.createEmptyContext();
context.setAuthentication(auth);
SecurityContextHolder.setContext(context);
request.getSession(true).setAttribute(
    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
```

---

## 7. 엔드포인트

| 메서드  | URL                      | 권한        | 파라미터  | 설명          |
| ---- | ------------------------ | --------- | ----- | ----------- |
| GET  | `/member/face/login`     | permitAll | -     | 얼굴 로그인 화면   |
| POST | `/member/face/loginProc` | permitAll | `image` | 얼굴 매칭 후 로그인 |
| POST | `/member/face/register`  | 인증 필요     | `image` | 본인 얼굴 등록    |

---

## 8. 보안 컨텍스트 제약

브라우저의 `getUserMedia`는 보안 컨텍스트에서만 동작합니다.

| 주소                             | 카메라   |
| ------------------------------ | ----- |
| `http://localhost:8080`        | 가능    |
| `http://underwatch.local:8080` | 차단 가능 |

운영 환경에서는 HTTPS 적용이 필요합니다.

---

## 9. 한계

Eigenfaces(PCA)는 구현·학습이 명확한 대신, 딥러닝 방식보다 정확도가 낮습니다.

* 얼굴 정렬(가이드 박스에 맞추기)에 의존 — 각도/표정/조명 변화에 민감
* 사진 스푸핑 가능, 라이브니스 검출 없음
* 한 사람당 1장 등록이면 인식력이 약함(여러 장일수록 좋음)
* 새 회원이 등록될 때마다 고유공간을 다시 학습해야 함

---

## 10. PCA(Eigenfaces) 선정 이유

* **모델 학습 과정이 분명함** — 등록 얼굴들로 평균 얼굴 + 고유얼굴(주성분) 공간을 계산하는 학습 단계가 코드에 그대로 드러남
* **외부 모델/라이브러리 없이 구현** — 핵심인 고유분해(Jacobi)까지 포함 → "가져온 게 아니라 만든 것"

---

## 11. 대체 구현 — LBPH

`LbphFaceRecognizer`로 LBPH(Local Binary Patterns Histogram)도 구현되어 있습니다(현재 미사용). PCA와 달리 학습이 필요 없고 조명에 강하며 1장 등록에 유리하지만, "모델 학습" 과정이 드러나지 않습니다. `MemberServiceImpl`의 주입 타입만 바꾸면 즉시 전환됩니다.

---

## 12. 기존 등록 데이터 주의

얼굴 데이터는 이제 DB(`face_descriptor`)가 아니라 **파일(`/face-data`)** 에 저장됩니다. 이전 방식으로 등록한 회원은 **얼굴을 다시 등록**해야 하며, **첫 2명이 등록되면 초기 PCA 모델이 학습**됩니다(그 전까지는 얼굴 로그인 불가).
