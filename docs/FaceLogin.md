# 얼굴 인식 로그인

현재 활성 구현은 Spring MVC 애플리케이션이 별도 Python FastAPI 얼굴 서버를 호출해 얼굴 임베딩을 받고,
Spring 쪽에서 DB에 저장된 임베딩들과 1:N 비교를 수행하는 구조입니다.

## 1. 동작 개요

```text
[등록]
마이페이지 → 웹캠 캡처 → Spring `/member/face/register`
        → FaceApiClient → Python FastAPI `/embed`
        → 512차원 정규화 임베딩 반환
        → `ow_member.face_descriptor`에 최대 3장 저장

[로그인]
로그인 화면 → 웹캠 캡처 → Spring `/member/face/loginProc`
        → FaceApiClient → Python FastAPI `/embed`
        → 등록된 모든 회원 임베딩과 코사인 유사도 비교
        → 임계값과 1위/2위 격차를 통과하면 세션 로그인
```

브라우저는 얼굴 이미지만 전송합니다. 얼굴 검출과 임베딩 추출은 Python 얼굴 API가 담당하고,
회원 식별, 임계값 판단, 로그인 세션 생성은 Spring 애플리케이션이 담당합니다.

---

## 2. 구성 요소

| 위치 | 역할 |
| --- | --- |
| `FaceApiClient` | Python FastAPI 얼굴 서버 호출. 기본 주소는 `http://127.0.0.1:8000` |
| `MemberServiceImpl` | 임베딩 저장, 전체 회원 1:N 비교, 임계값/마진 판단 |
| `MemberController` | 얼굴 등록과 얼굴 로그인 엔드포인트 |
| `memberMapper.xml` | `face_descriptor` CLOB 조회/저장 |
| `faceLogin.jsp` | 얼굴 로그인 화면, 웹캠 캡처 |
| `mypage.jsp` | 얼굴 등록 UI |
| `EigenFaceRecognizer`, `FaceStore`, `LbphFaceRecognizer` | 과거/대체 실험 구현. 현재 서비스 흐름에서는 사용하지 않음 |

---

## 3. 얼굴 API 연동

`FaceApiClient`는 `face.api.url` 프로퍼티를 읽고, `/embed`로 JSON 요청을 보냅니다.

```json
{
  "image": "data:image/jpeg;base64,..."
}
```

응답은 다음 형태를 기대합니다.

```json
{
  "success": true,
  "embedding": [0.0123, -0.0456]
}
```

Python 서버는 InsightFace 계열 모델로 얼굴을 검출하고 ArcFace 임베딩을 반환하는 전제입니다.
반환 임베딩은 L2 정규화되어 있어 두 벡터의 내적을 코사인 유사도로 사용할 수 있습니다.

---

## 4. 등록 데이터

`ow_member.face_descriptor`에는 한 회원의 얼굴 임베딩 목록을 JSON 문자열로 저장합니다.
한 계정당 최대 3장까지 보관하며, 초과하면 오래된 임베딩부터 제거합니다.

```text
[
  [512차원 임베딩 #1],
  [512차원 임베딩 #2],
  [512차원 임베딩 #3]
]
```

여러 장을 저장하는 이유는 조명, 표정, 각도 차이를 조금 더 견디기 위해서입니다.

---

## 5. 1:N 매칭 로직

로그인 시 새로 받은 임베딩을 모든 등록 회원의 임베딩과 비교합니다.
회원이 여러 장을 등록한 경우 그 회원의 최고 유사도를 대표 점수로 사용합니다.

판정 기준은 두 가지입니다.

| 설정 | 기본값 | 의미 |
| --- | --- | --- |
| `face.match.threshold` | `0.45` | 같은 사람으로 인정할 최소 코사인 유사도 |
| `face.match.margin` | `0.06` | 1위와 2위 후보 사이에 필요한 최소 격차 |

1위 유사도가 임계값보다 낮으면 거절합니다. 2위도 임계값을 넘고 1위와 점수 차이가 작으면,
닮은 사용자를 잘못 로그인시키지 않기 위해 거절합니다.

콘솔에는 다음 형식의 로그가 남습니다.

```text
[FaceLogin] best=user1 cos=0.62 second=0.31 (threshold=0.45, margin=0.06)
```

---

## 6. 수동 세션 로그인

얼굴 로그인은 비밀번호 인증을 거치지 않으므로 일치 회원을 찾은 뒤 `SecurityContext`를 직접 생성합니다.
폼 로그인, OAuth 로그인과 같은 `OwUserPrincipal`을 사용하므로 Controller와 JSP는 로그인 방식을 구분하지 않습니다.

---

## 7. 엔드포인트

| 메서드 | URL | 권한 | 파라미터 | 설명 |
| --- | --- | --- | --- | --- |
| GET | `/member/face/login` | permitAll | - | 얼굴 로그인 화면 |
| POST | `/member/face/loginProc` | permitAll | `image` | 얼굴 매칭 후 로그인 |
| POST | `/member/face/register` | 인증 필요 | `image` | 본인 얼굴 등록 |

---

## 8. 실행 조건과 주의사항

* Spring 애플리케이션 실행 전에 Python 얼굴 API 서버가 `face.api.url`에서 응답해야 합니다.
* 얼굴 API 첫 호출은 모델 로딩 때문에 느릴 수 있어 읽기 타임아웃을 15초로 두었습니다.
* 브라우저의 `getUserMedia`는 보안 컨텍스트에서만 안정적으로 동작하므로 로컬 개발은 `http://localhost:8080` 접속을 권장합니다.
* 운영 환경에서는 HTTPS 적용이 필요합니다.

---

## 9. 한계

* 사진 스푸핑을 막는 라이브니스 검출은 없습니다.
* 얼굴 API 서버가 내려가면 등록과 로그인이 실패합니다.
* 임계값은 카메라, 조명, 모델 버전에 따라 조정이 필요합니다.
* 개인정보성 생체 데이터에 해당하므로 운영 환경에서는 암호화와 별도 보관 정책이 필요합니다.
