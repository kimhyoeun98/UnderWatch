# 얼굴 인식 로그인 

**LBPH(Local Binary Patterns Histogram)** 알고리즘을 사용하여 구현한 1:N 얼굴 로그인 문서입니다.

---

## 1. 동작 개요

```text
[등록]
마이페이지 → 웹캠 프레임 캡처(브라우저) → base64 이미지 전송
        → 서버 LBPH 특징 추출 → DB 저장

[로그인]
로그인 화면 → 웹캠 프레임 캡처 → base64 이미지 전송
        → 서버 LBPH 특징 추출 → 1:N 비교 → 세션 로그인
```

브라우저는 **이미지만** 보내고, 얼굴 특징 추출과 비교는 전부 서버(Java)에서 합니다.

---

## 2. 구성 요소

| 위치                              | 역할                              |
| ------------------------------- | ------------------------------- |
| `add_face.sql`                  | `face_descriptor`(CLOB) 컬럼 추가   |
| `LbphFaceRecognizer`            | **LBPH** (특징 추출 + 거리 비교)  |
| `MemberServiceImpl`             | 얼굴 등록 저장 / 1:N 매칭               |
| `MemberController`              | 얼굴 등록 / 얼굴 로그인 요청 처리           |
| `faceLogin.jsp`                 | 얼굴 로그인 화면 (웹캠 캡처)              |
| `mypage.jsp`                    | 얼굴 등록 UI (웹캠 캡처)               |
| `spring-security.xml`           | 얼굴 로그인 URL 접근 권한 설정            |

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

## 4. 서버 — LBPH 특징 추출 (`LbphFaceRecognizer`)

JDK(`ImageIO`, `AWT`)로 처리합니다.

1. base64 이미지 디코드 → `BufferedImage`
2. 흑백 변환 + 128×128 리사이즈
3. 히스토그램 평활화(조명 보정)
4. **LBP 코드**: 각 픽셀의 3×3 이웃 8개를 중심값과 비교(크거나 같으면 1) → 8비트 코드(0~255)
5. 이미지를 8×8 셀로 나눠 셀별 256-bin 히스토그램을 이어붙임 → 길이 16384 특징 벡터

```java
int code = 0;
code |= (gray[y-1][x-1] >= c ? 1 : 0) << 7;
code |= (gray[y-1][x]   >= c ? 1 : 0) << 6;
// ... 8개 이웃 ...
hist[cell * 256 + code]++;
```

추출한 히스토그램은 JSON 문자열로 `face_descriptor`(CLOB)에 저장합니다.

---

## 5. 서버 — 1:N 매칭

등록된 모든 얼굴과 **카이제곱 거리**로 비교해 가장 가까운 회원을 찾고, 임계값 이내일 때만 로그인합니다.

```java
private static final double MATCH_THRESHOLD = 0.45;   // 카이제곱(0~2), 낮을수록 엄격

public String matchFace(String imageData) {
    int[] probe = faceRecognizer.extract(imageData);
    String bestId = null;
    double bestDist = Double.MAX_VALUE;
    for (MemberVO m : memberDAO.selectAllFaces()) {
        double dist = faceRecognizer.distance(probe, faceRecognizer.parse(m.getFaceDescriptor()));
        if (dist < bestDist) { bestDist = dist; bestId = m.getId(); }
    }
    return bestDist <= MATCH_THRESHOLD ? bestId : null;
}
```

* 거리 = 두 히스토그램을 확률분포로 정규화한 뒤 카이제곱(`Σ (a-b)²/(a+b)`)
* `matchFace`는 최근접 거리를 콘솔에 로그로 남기므로, 그 값을 보고 `MATCH_THRESHOLD`를 환경에 맞게 조정하면 됩니다.

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

| 메서드  | URL                      | 권한        | 파라미터          | 설명             |
| ---- | ------------------------ | --------- | ------------- | -------------- |
| GET  | `/member/face/login`     | permitAll | -             | 얼굴 로그인 화면      |
| POST | `/member/face/loginProc` | permitAll | `image`       | 얼굴 매칭 후 로그인    |
| POST | `/member/face/register`  | 인증 필요     | `image`       | 본인 얼굴 등록       |

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

LBPH는 직접 구현이 쉽고 학습이 필요 없는 대신, 딥러닝 방식보다 정확도가 낮습니다.

* 얼굴 정렬(가이드 박스에 맞추기)에 의존 — 각도/표정/조명 변화에 민감
* 사진 스푸핑 가능, 라이브니스 검출 없음
* 등록자가 많아질수록 오인식 가능성 증가

---

## 10. LBPH 사용 이유

* PCA/LDA는 픽셀 명암값 자체를 다뤄 조명에 취약, LBPH는 이웃 픽셀보다 밝은가/어두운가 에 상대 비교만 합니다.
* PCA/LDA는 전체 회원 얼굴로 벡터를 만들기 때문에 새 사용자가 등록될 때 마다 모델 전체를 다시 계산해야함, LBPH는 각 얼굴을 독립적으로 히스토그램화하므로 그 사람 것만 저장하면 됩니다. 
* Fisherfaces(LDA)는 한 사람당 사진 2장 이상이 있어야 동작함(클래스 내 분산 계산 필요), LBPH는 1장이면 등록, 인식 가능합니다.

