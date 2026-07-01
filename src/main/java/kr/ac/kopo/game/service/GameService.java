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

	/**
	 * G-04 전적검색 — 배틀태그로 플레이어 요약(랭크/인도점수/아바타) 조회.
	 * 입력 "Name#1234" → OverFast player_id "Name-1234". 비공개/없음이면 error 맵 반환.
	 */
	public Map<String, Object> getPlayerSummary(String battleTag) {
		if (battleTag == null || battleTag.isBlank()) {
			return null;
		}
		String playerId = battleTag.trim().replace('#', '-');
		try {
			HttpRequest req = HttpRequest.newBuilder()
					.uri(URI.create(BASE + "/players/" + playerId + "/summary"))
					.header("Accept", "application/json").GET().build();
			HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
			if (resp.statusCode() != 200) {
				return Map.of("error", true, "status", resp.statusCode());
			}
			Map<String, Object> summary = mapper.readValue(resp.body(),
					new TypeReference<Map<String, Object>>() {});
			if (summary == null || summary.get("username") == null) {
				return Map.of("error", true, "status", 404);
			}
			return summary;
		} catch (Exception e) {
			return Map.of("error", true, "status", -1);
		}
	}

	/** 영웅 → 역할(tank/damage/support). 막대 색상 구분용. 모르는 영웅은 빈 문자열. */
	private static final Map<String, String> HERO_ROLE = Map.ofEntries(
		Map.entry("dva", "tank"),          Map.entry("doomfist", "tank"),     Map.entry("junker-queen", "tank"),
		Map.entry("mauga", "tank"),        Map.entry("orisa", "tank"),        Map.entry("ramattra", "tank"),
		Map.entry("reinhardt", "tank"),    Map.entry("roadhog", "tank"),      Map.entry("sigma", "tank"),
		Map.entry("winston", "tank"),      Map.entry("wrecking-ball", "tank"),Map.entry("zarya", "tank"),
		Map.entry("hazard", "tank"),
		Map.entry("ashe", "damage"),       Map.entry("bastion", "damage"),    Map.entry("cassidy", "damage"),
		Map.entry("echo", "damage"),       Map.entry("genji", "damage"),      Map.entry("hanzo", "damage"),
		Map.entry("junkrat", "damage"),    Map.entry("mei", "damage"),        Map.entry("pharah", "damage"),
		Map.entry("reaper", "damage"),     Map.entry("sojourn", "damage"),    Map.entry("soldier-76", "damage"),
		Map.entry("sombra", "damage"),     Map.entry("symmetra", "damage"),   Map.entry("torbjorn", "damage"),
		Map.entry("tracer", "damage"),     Map.entry("venture", "damage"),    Map.entry("widowmaker", "damage"),
		Map.entry("freja", "damage"),
		Map.entry("ana", "support"),       Map.entry("baptiste", "support"),  Map.entry("brigitte", "support"),
		Map.entry("illari", "support"),    Map.entry("juno", "support"),      Map.entry("kiriko", "support"),
		Map.entry("lifeweaver", "support"),Map.entry("lucio", "support"),     Map.entry("mercy", "support"),
		Map.entry("moira", "support"),     Map.entry("zenyatta", "support")
	);

	/**
	 * G-04 전적검색 — 빠른대전·경쟁전 두 모드의 종합/영웅별 전적을 한 번에 JSON 으로 반환한다.
	 * 화면(JS)에서 모드 토글 + 지표별 내림차순 정렬을 한다. 비공개/없음이면 "null".
	 */
	public String getPlayerStatsJson(String battleTag) {
		if (battleTag == null || battleTag.isBlank()) {
			return "null";
		}
		String playerId = battleTag.trim().replace('#', '-');
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("quickplay", fetchStats(playerId, "quickplay"));
		result.put("competitive", fetchStats(playerId, "competitive"));
		try {
			return mapper.writeValueAsString(result);
		} catch (Exception e) {
			return "null";
		}
	}

	/** 한 게임모드의 stats/summary 를 받아 general + 영웅별 전 지표 리스트로 가공. 없으면 null. */
	@SuppressWarnings("unchecked")
	private Map<String, Object> fetchStats(String playerId, String gamemode) {
		try {
			HttpRequest req = HttpRequest.newBuilder()
					.uri(URI.create(BASE + "/players/" + playerId + "/stats/summary?gamemode=" + gamemode))
					.header("Accept", "application/json").GET().build();
			HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
			if (resp.statusCode() != 200) {
				return null;
			}
			Map<String, Object> data = mapper.readValue(resp.body(),
					new TypeReference<Map<String, Object>>() {});
			if (data == null) {
				return null;
			}
			Map<String, Object> general = (Map<String, Object>) data.get("general");
			Map<String, Object> heroes = (Map<String, Object>) data.get("heroes");
			if (general == null || heroes == null) {
				return null;
			}

			List<Map<String, Object>> list = new ArrayList<>();
			for (Map.Entry<String, Object> e : heroes.entrySet()) {
				Map<String, Object> h = (Map<String, Object>) e.getValue();
				if (h == null) {
					continue;
				}
				Map<String, Object> avg = (Map<String, Object>) h.getOrDefault("average", Map.of());
				Map<String, Object> item = new LinkedHashMap<>();
				item.put("key", e.getKey());
				item.put("nameKr", krName(e.getKey(), e.getKey()));
				item.put("role", HERO_ROLE.getOrDefault(e.getKey(), ""));
				item.put("timePlayed", num(h.get("time_played")));
				item.put("gamesPlayed", num(h.get("games_played")));
				item.put("gamesWon", num(h.get("games_won")));
				item.put("winrate", num(h.get("winrate")));
				item.put("kda", num(h.get("kda")));
				item.put("elims", num(avg.get("eliminations")));    // 10분당 평균
				item.put("assists", num(avg.get("assists")));
				item.put("deaths", num(avg.get("deaths")));
				item.put("damage", num(avg.get("damage")));
				item.put("healing", num(avg.get("healing")));
				list.add(item);
			}

			Map<String, Object> out = new LinkedHashMap<>();
			out.put("general", general);
			out.put("generalTime", formatDuration(general.get("time_played")));
			out.put("heroes", list);
			return out;
		} catch (Exception e) {
			return null;
		}
	}

	/** Object → double (숫자가 아니면 0). */
	private double num(Object o) {
		return (o instanceof Number n) ? n.doubleValue() : 0d;
	}

	/** 초 → "H:MM:SS" 문자열. */
	private String formatDuration(Object secondsObj) {
		if (!(secondsObj instanceof Number)) {
			return "-";
		}
		long s = ((Number) secondsObj).longValue();
		return String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60);
	}

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

	/**
	 * 공식 패치 노트 목록 — Blizzard 패치노트 페이지에서 패치 제목(h3.PatchNotes-patchTitle)을 파싱한다.
	 * 페이지가 단일 문서(최신 패치 본문 + 날짜별 제목) 구조라, 링크는 공식 패치노트 페이지로 연결한다.
	 * (예전 __NEXT_DATA__ JSON 구조는 Blizzard 페이지 개편으로 더 이상 존재하지 않는다.)
	 * 반환 필드: title, date, url, thumbnail
	 */
	public List<Map<String, Object>> getPatches() {
		final String base = "https://overwatch.blizzard.com/ko-kr/news/patch-notes/";
		try {
			Document doc = Jsoup.connect(base)
					.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
					.header("Accept-Language", "ko-KR,ko;q=0.9")
					.timeout(12000)
					.get();

			List<Map<String, Object>> result = new ArrayList<>();
			// 패치 블록마다 제목/날짜/앵커가 함께 들어있다. 블록 단위로 묶어야 서로 어긋나지 않는다.
			for (Element block : doc.select(".PatchNotes-patch")) {
				Element titleEl = block.selectFirst("h3.PatchNotes-patchTitle");
				if (titleEl == null) {
					continue;
				}
				String full = titleEl.text().trim();
				if (full.isEmpty()) {
					continue;
				}
				// 제목 끝의 날짜(적용일)는 떼어내고, 표시 날짜는 블록의 게시일(.PatchNotes-date)을 쓴다 — 앵커 날짜와 일치
				String title = full.split(" - ", 2)[0].trim();

				Element dateEl = block.selectFirst(".PatchNotes-date");
				String date = (dateEl != null && !dateEl.text().isBlank())
						? dateEl.text().trim()
						: (full.contains(" - ") ? full.split(" - ", 2)[1].trim() : "");

				// 개별 패치로 바로 가는 딥링크 (#patch-YYYY-MM-DD)
				Element anchor = block.selectFirst(".anchor[id]");
				String url = (anchor != null && !anchor.id().isEmpty())
						? base + "#" + anchor.id()
						: base;

				Map<String, Object> m = new LinkedHashMap<>();
				m.put("title", title);
				m.put("date", date);
				m.put("url", url);
				m.put("thumbnail", "");   // 페이지에서 패치별 썸네일을 제공하지 않음
				result.add(m);
			}
			return result;
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	private String krName(Object key, Object fallback) {
		String k = (key == null) ? "" : String.valueOf(key);
		return KR_NAME.getOrDefault(k, fallback == null ? k : String.valueOf(fallback));
	}
}
