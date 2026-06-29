package kr.ac.kopo.game.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * G-01 게임 정보 — OverFast API(무료, 키 불필요) 연동.
 * OverFast는 영문 이름만 제공하므로 한글명은 매핑 테이블로 보강한다.
 */
@Service
public class GameService {

	private static final String BASE = "https://overfast-api.tekrop.fr";

	private static final Map<String, String> GAMEMODE_KR = Map.ofEntries(
		Map.entry("escort",            "화물 호송"),
		Map.entry("hybrid",            "혼합"),
		Map.entry("control",           "쟁탈"),
		Map.entry("push",              "밀기"),
		Map.entry("flashpoint",        "거점 장악"),
		Map.entry("clash",             "격전"),
		Map.entry("capture-the-flag",  "깃발 쟁탈전"),
		Map.entry("deathmatch",        "데스매치"),
		Map.entry("team-deathmatch",   "팀 데스매치"),
		Map.entry("elimination",       "섬멸전"),
		Map.entry("assault",           "거점 점령")
	);

	private static final Map<String, String> MAP_KR = Map.ofEntries(
		// 일반 전장
		Map.entry("Antarctic Peninsula",  "남극 반도"),
		Map.entry("Ayutthaya",            "아유타야"),
		Map.entry("Black Forest",         "검은 숲"),
		Map.entry("Blizzard World",       "블리자드 월드"),
		Map.entry("Busan",                "부산"),
		Map.entry("Castillo",             "카스티요"),
		Map.entry("Château Guillard",     "샤토 기야르"),
		Map.entry("Circuit Royal",        "서킷 로얄"),
		Map.entry("Colosseo",             "콜로세오"),
		Map.entry("Dorado",               "도라도"),
		Map.entry("Ecopoint: Antarctica", "탐사 기지: 남극"),
		Map.entry("Eichenwalde",          "아이헨발데"),
		Map.entry("Esperança",            "에스페란사"),
		Map.entry("Hanamura",             "하나무라"),
		Map.entry("Hanaoka",              "하나오카"),
		Map.entry("Havana",               "하바나"),
		Map.entry("Hollywood",            "할리우드"),
		Map.entry("Horizon Lunar Colony", "호라이즌 달 기지"),
		Map.entry("Ilios",                "일리오스"),
		Map.entry("Junkertown",           "정크타운"),
		Map.entry("Kanezaka",             "카네자카"),
		Map.entry("King's Row",           "왕의 길"),   // 직선 아포스트로피
		Map.entry("King’s Row",           "왕의 길"),   // 곡선 아포스트로피(U+2019)
		Map.entry("Lijiang Tower",        "리장 타워"),
		Map.entry("Malevento",            "말레벤토"),
		Map.entry("Midtown",              "미드타운"),
		Map.entry("Necropolis",           "네크로폴리스"),
		Map.entry("Nepal",                "네팔"),
		Map.entry("New Junk City",        "뉴 정크 시티"),
		Map.entry("New Queen Street",     "뉴 퀸 스트리트"),
		Map.entry("Numbani",              "눔바니"),
		Map.entry("Oasis",                "오아시스"),
		Map.entry("Paraíso",              "파라이소"),
		Map.entry("Paris",                "파리"),
		Map.entry("Petra",                "페트라"),
		Map.entry("Rialto",               "리알토"),
		Map.entry("Route 66",             "66번 국도"),
		Map.entry("Runasapi",             "루나사피"),
		Map.entry("Samoa",                "사모아"),
		Map.entry("Shambali Monastery",   "샴발리 수도원"),
		Map.entry("Suravasa",             "수라바사"),
		Map.entry("Temple of Anubis",     "아누비스 신전"),
		Map.entry("Throne of Anubis",     "아누비스의 왕좌"),
		Map.entry("Volskaya Industries",  "볼스카야 인더스트리"),
		Map.entry("Watchpoint: Gibraltar","감시 기지: 지브롤터"),
		// 신규/스타디움 전장
		Map.entry("Aatlis",               "아틀리스"),
		Map.entry("Arena Victoriae",      "승리의 투기장"),
		Map.entry("Gogadoro",             "고가도로"),
		Map.entry("Place Lacroix",        "십자 광장"),
		Map.entry("Redwood Dam",          "레드우드 제방"),
		Map.entry("Wuxing University",    "오행 대학"),
		// 기타
		Map.entry("Workshop Chamber",     "워크샵 방"),
		Map.entry("Workshop Expanse",     "워크샵 광장"),
		Map.entry("Workshop Green Screen","워크샵 그린 스크린"),
		Map.entry("Workshop Island",      "워크샵 섬")
	);

	private static final Map<String, String> KR_NAME = Map.ofEntries(
		Map.entry("ana", "아나"),                 Map.entry("ashe", "애쉬"),
		Map.entry("baptiste", "바티스트"),         Map.entry("bastion", "바스티온"),
		Map.entry("brigitte", "브리기테"),         Map.entry("cassidy", "캐서디"),
		Map.entry("dva", "D.Va"),                 Map.entry("doomfist", "둠피스트"),
		Map.entry("echo", "에코"),                 Map.entry("genji", "겐지"),
		Map.entry("hanzo", "한조"),               Map.entry("hazard", "해저드"),
		Map.entry("illari", "일리아리"),           Map.entry("junker-queen", "정커 퀸"),
		Map.entry("junkrat", "정크랫"),            Map.entry("juno", "주노"),
		Map.entry("kiriko", "키리코"),             Map.entry("lifeweaver", "라이프위버"),
		Map.entry("lucio", "루시우"),              Map.entry("mauga", "마우가"),
		Map.entry("mei", "메이"),                  Map.entry("mercy", "메르시"),
		Map.entry("moira", "모이라"),              Map.entry("orisa", "오리사"),
		Map.entry("pharah", "파라"),               Map.entry("ramattra", "라마트라"),
		Map.entry("reaper", "리퍼"),               Map.entry("reinhardt", "라인하르트"),
		Map.entry("roadhog", "로드호그"),          Map.entry("sigma", "시그마"),
		Map.entry("sojourn", "소전"),              Map.entry("soldier-76", "솔저: 76"),
		Map.entry("sombra", "솜브라"),             Map.entry("symmetra", "시메트라"),
		Map.entry("torbjorn", "토르비욘"),         Map.entry("tracer", "트레이서"),
		Map.entry("venture", "벤처"),              Map.entry("widowmaker", "위도우메이커"),
		Map.entry("winston", "윈스턴"),            Map.entry("wrecking-ball", "레킹볼"),
		Map.entry("zarya", "자리야"),              Map.entry("zenyatta", "젠야타")
	);

	private final HttpClient client = HttpClient.newHttpClient();
	private final ObjectMapper mapper = new ObjectMapper();

	/** 영웅 목록 (role: tank/damage/support) — 각 항목에 nameKr 추가 */
	public List<Map<String, Object>> getHeroes(String role) {
		try {
			String url = BASE + "/heroes?locale=ko-kr";
			if (role != null && !role.isBlank()) {
				url += "&role=" + role;
			}
			HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
			String body = client.send(req, HttpResponse.BodyHandlers.ofString()).body();
			List<Map<String, Object>> heroes = mapper.readValue(body, new TypeReference<List<Map<String, Object>>>() {});
			for (Map<String, Object> h : heroes) {
				h.put("nameKr", krName(h.get("key"), h.get("name")));
			}
			return heroes;
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	/** 영웅 상세 — nameKr 추가 */
	public Map<String, Object> getHero(String key) {
		try {
			HttpRequest req = HttpRequest.newBuilder()
					.uri(URI.create(BASE + "/heroes/" + key + "?locale=ko-kr")).GET().build();
			String body = client.send(req, HttpResponse.BodyHandlers.ofString()).body();
			Map<String, Object> hero = mapper.readValue(body, new TypeReference<Map<String, Object>>() {});
			hero.put("nameKr", krName(key, hero.get("name")));
			return hero;
		} catch (Exception e) {
			return null;
		}
	}

	/** 맵 목록 — 맵 이름·게임 모드 한글명 보강 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getMaps() {
		try {
			HttpRequest req = HttpRequest.newBuilder()
					.uri(URI.create(BASE + "/maps?locale=ko-kr")).GET().build();
			String body = client.send(req, HttpResponse.BodyHandlers.ofString()).body();
			List<Map<String, Object>> maps = mapper.readValue(body, new TypeReference<List<Map<String, Object>>>() {});
			for (Map<String, Object> m : maps) {
				// 맵 이름 한글 변환
				String engName = String.valueOf(m.getOrDefault("name", ""));
				m.put("nameKr", MAP_KR.getOrDefault(engName, engName));

				// 게임 모드 한글 변환
				Object gm = m.get("gamemodes");
				if (gm instanceof List) {
					List<String> krModes = new ArrayList<>();
					for (Object mode : (List<?>) gm) {
						krModes.add(GAMEMODE_KR.getOrDefault(String.valueOf(mode), String.valueOf(mode)));
					}
					m.put("gamemodesKr", krModes);
				}
			}
			return maps;
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	/** 디버그용 — 실제로 어떤 응답이 오는지 확인 */
	public String getPatchesDebug() {
		StringBuilder sb = new StringBuilder();
		try {
			org.jsoup.Connection.Response resp = Jsoup.connect("https://overwatch.blizzard.com/ko-kr/news/patch-notes/")
					.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36")
					.header("Accept", "text/html,application/xhtml+xml")
					.header("Accept-Language", "ko-KR,ko;q=0.9")
					.timeout(15000)
					.ignoreHttpErrors(true)
					.execute();

			sb.append("=== HTTP 상태: ").append(resp.statusCode()).append(" ").append(resp.statusMessage()).append("\n\n");

			Document doc = resp.parse();
			Element nextDataEl = doc.getElementById("__NEXT_DATA__");

			if (nextDataEl == null) {
				sb.append("=== __NEXT_DATA__ 없음 ===\n");
				sb.append("--- body 첫 2000자 ---\n");
				sb.append(doc.body() != null ? doc.body().html().substring(0, Math.min(2000, doc.body().html().length())) : "(empty)");
			} else {
				String json = nextDataEl.html();
				sb.append("=== __NEXT_DATA__ 발견! 길이=").append(json.length()).append(" ===\n");
				// props.pageProps 의 최상위 키 목록
				try {
					JsonNode root = mapper.readTree(json);
					JsonNode pageProps = root.path("props").path("pageProps");
					sb.append("pageProps 키 목록: ");
					pageProps.fieldNames().forEachRemaining(k -> sb.append(k).append(", "));
					sb.append("\n\n");
					sb.append("JSON 앞 3000자:\n").append(json, 0, Math.min(3000, json.length()));
				} catch (Exception e2) {
					sb.append("JSON 파싱 실패: ").append(e2.getMessage()).append("\n");
					sb.append("원본 앞 2000자:\n").append(json, 0, Math.min(2000, json.length()));
				}
			}
		} catch (Exception e) {
			sb.append("예외 발생: ").append(e.getClass().getName()).append(": ").append(e.getMessage());
		}
		return sb.toString();
	}

	/**
	 * 공식 패치 노트 목록 — Blizzard 패치노트 페이지의 __NEXT_DATA__ JSON을 파싱.
	 * 반환 필드: title, date, url, thumbnail
	 */
	public List<Map<String, Object>> getPatches() {
		try {
			Document doc = Jsoup.connect("https://overwatch.blizzard.com/ko-kr/news/patch-notes/")
					.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
					.timeout(12000)
					.get();

			Element nextDataEl = doc.getElementById("__NEXT_DATA__");
			if (nextDataEl == null) return new ArrayList<>();

			JsonNode root    = mapper.readTree(nextDataEl.html());
			JsonNode patches = findPatchArray(root);
			if (patches == null || !patches.isArray()) return new ArrayList<>();

			List<Map<String, Object>> result = new ArrayList<>();
			for (JsonNode p : patches) {
				Map<String, Object> m = new LinkedHashMap<>();
				m.put("title",     text(p, "title", "patch_title", "header"));
				m.put("date",      text(p, "publish_date", "date", "patch_date", "published_at"));
				m.put("url",       patchUrl(p));
				m.put("thumbnail", thumb(p));
				result.add(m);
			}
			return result;
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	/** __NEXT_DATA__ 트리에서 패치 배열을 탐색 — Blizzard 구조 변경에 대비해 여러 경로 시도 */
	private JsonNode findPatchArray(JsonNode root) {
		// 시도할 경로 목록
		String[][] paths = {
			{"props", "pageProps", "patchData"},
			{"props", "pageProps", "blizzardNewsData", "news"},
			{"props", "pageProps", "articleList"},
			{"props", "pageProps", "articles"},
			{"props", "pageProps", "news"},
			{"props", "pageProps", "data", "articles"},
			{"props", "pageProps", "initialState", "patches"},
		};
		for (String[] path : paths) {
			JsonNode node = root;
			for (String key : path) node = node == null ? null : node.path(key);
			if (node != null && !node.isMissingNode() && node.isArray() && node.size() > 0) {
				return node;
			}
		}
		// 못 찾으면 트리 전체를 BFS로 탐색해서 배열 중 title 필드를 가진 것을 반환
		return searchForPatchArray(root, 0);
	}

	private JsonNode searchForPatchArray(JsonNode node, int depth) {
		if (depth > 6 || node == null) return null;
		if (node.isArray() && node.size() > 0) {
			JsonNode first = node.get(0);
			if (first != null && (first.has("title") || first.has("patch_title") || first.has("slug"))) {
				return node;
			}
		}
		if (node.isObject()) {
			for (JsonNode child : node) {
				JsonNode found = searchForPatchArray(child, depth + 1);
				if (found != null) return found;
			}
		}
		return null;
	}

	private String text(JsonNode n, String... keys) {
		for (String k : keys) {
			JsonNode v = n.path(k);
			if (!v.isMissingNode() && !v.isNull() && v.isTextual()) return v.asText();
		}
		return "";
	}

	private String patchUrl(JsonNode p) {
		// blizzard_url, url, link, slug 순서로 시도
		for (String k : new String[]{"blizzard_url", "url", "link"}) {
			String v = text(p, k);
			if (!v.isEmpty()) return v;
		}
		String slug = text(p, "slug", "id");
		if (!slug.isEmpty()) {
			return "https://overwatch.blizzard.com/ko-kr/news/patch-notes/" + slug + "/";
		}
		return "https://overwatch.blizzard.com/ko-kr/news/patch-notes/";
	}

	private String thumb(JsonNode p) {
		JsonNode t = p.path("thumbnail");
		if (!t.isMissingNode()) {
			if (t.isTextual()) return t.asText();
			String url = text(t, "url", "src", "uri");
			if (!url.isEmpty()) return url;
		}
		return text(p, "image", "thumbnail_url", "cover_image");
	}

	private String krName(Object key, Object fallback) {
		String k = (key == null) ? "" : String.valueOf(key);
		return KR_NAME.getOrDefault(k, fallback == null ? k : String.valueOf(fallback));
	}
}
