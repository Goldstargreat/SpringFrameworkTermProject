package kr.ac.kopo.gnuyog.goldstarmall.service;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import kr.ac.kopo.gnuyog.goldstarmall.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 검색어 추천 : 검색 결과가 없을 때 "혹시 '김치'를 찾으시나요?" 처럼 비슷한 검색어를 알려 준다
// 방법
// 1) 상품 이름을 단어로 나누고, 단어 안의 글자 조각(검색어와 길이가 비슷한 것)을 후보로 모은다
//    예) '포기김치 3kg' → '포기김치', '3kg' → '포기', '기김', '김치', '포기김', ...
// 2) 한글은 자모(ㄱ, ㅏ, ㅁ ...)로 풀어서 몇 글자를 고치면 같아지는지(편집 거리) 센다
//    예) 감치 = ㄱㅏㅁㅊㅣ, 김치 = ㄱㅣㅁㅊㅣ → ㅏ 를 ㅣ 로 1번만 고치면 같다
//    오타는 보통 키보드에서 옆 키를 눌러서 생기므로, 두벌식 자판에서 이웃한 키끼리 바뀐 것은 덜 틀린 것으로 친다
//    예) '감치' 는 김치(ㅏ↔ㅣ, 옆 키)와 갈치(ㅁ↔ㄹ, 먼 키) 둘 다 1글자 차이지만 김치를 추천한다
// 3) 덜 틀린 후보를 고른다 (너무 많이 고쳐야 하면 추천하지 않는다)
@Service
@Transactional(readOnly = true)
public class SearchSuggestionService {

    // 이보다 긴 검색어는 추천하지 않는다 (후보 조각이 너무 많아진다)
    private static final int MAX_KEYWORD_LENGTH = 20;

    // 비용은 소수(0.6)가 아니라 정수로 센다 (double 을 == 로 비교하면 0.6+0.6+1 이 2.2000000000000002 처럼 어긋날 수 있다)
    // 글자 1개를 넣기/빼기/다른 키로 바꾸기 = 10, 키보드에서 이웃한 키(또는 Shift 만 다른 같은 키, 예: ㄱ/ㄲ)로 바꾸기 = 6
    private static final int KEY_COST = 10;
    private static final int NEAR_KEY_COST = 6;

    // 두벌식 자판 : 자모 → 키 위치 {줄, 칸}. 윗줄(QWERTY), 가운데 줄(ASDF), 아랫줄(ZXCV)
    private static final String[] KEY_ROWS = {"ㅂㅈㄷㄱㅅㅛㅕㅑㅐㅔ", "ㅁㄴㅇㄹㅎㅗㅓㅏㅣ", "ㅋㅌㅊㅍㅠㅜㅡ"};
    // 쌍자음(ㄲ ㄸ ㅃ ㅆ ㅉ)과 ㅒ, ㅖ 는 윗줄에서 Shift 를 누른 같은 키다
    // 윗줄 칸 번호 : ㅂ0 ㅈ1 ㄷ2 ㄱ3 ㅅ4 ㅛ5 ㅕ6 ㅑ7 ㅐ8 ㅔ9
    // Shift 칸 번호 : ㅃ0 ㅉ1 ㄸ2 ㄲ3 ㅆ4 (빈칸 5,6,7) ㅒ8 ㅖ9  → ㅆ 뒤에 공백 3개
    private static final String SHIFT_ROW = "ㅃㅉㄸㄲㅆ   ㅒㅖ";
    private static final Map<Character, int[]> KEY_POSITIONS = new HashMap<>();

    static {
        for (int row = 0; row < KEY_ROWS.length; row++) {
            for (int col = 0; col < KEY_ROWS[row].length(); col++) {
                KEY_POSITIONS.put(KEY_ROWS[row].charAt(col), new int[]{row, col});
            }
        }
        for (int col = 0; col < SHIFT_ROW.length(); col++) {
            char c = SHIFT_ROW.charAt(col);
            if (c != ' ') {
                KEY_POSITIONS.put(c, new int[]{0, col});
            }
        }
    }

    // 초성/중성/종성 번호 → 자모 글자 (종성 0번은 받침 없음)
    private static final String CHOSEONG = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ";
    private static final String JUNGSEONG = "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ";
    private static final String JONGSEONG = " ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ";

    private final ProductRepository productRepository;

    public SearchSuggestionService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // 추천 검색어 (비슷한 것이 없으면 null)
    public String suggest(String keyword) {
        if (keyword == null) {
            return null;
        }
        String typed = keyword.trim();
        // 한 글자는 비슷한 단어가 너무 많아서 추천하지 않는다
        if (typed.length() < 2 || typed.length() > MAX_KEYWORD_LENGTH) {
            return null;
        }
        List<String> names = new ArrayList<>();
        for (Product product : productRepository.findAll()) {
            names.add(product.getName());
        }
        return suggest(typed, names);
    }

    // 상품 이름 목록에서 추천 검색어 고르기 (DB 없이 테스트할 수 있게 따로 둔다)
    static String suggest(String typed, List<String> productNames) {
        String target = typed.replaceAll("\\s+", "").toLowerCase();
        if (target.length() < 2) {
            return null;
        }
        String targetJamo = toJamo(target);
        int maxDistance = maxDistance(targetJamo.length());

        // 후보 조각 → 그 조각이 들어 있는 상품 수 (많이 쓰이는 단어를 먼저 추천한다)
        Map<String, Integer> candidates = new HashMap<>();
        for (String name : productNames) {
            Set<String> inThisProduct = new HashSet<>();
            for (String word : name.split("[^\\p{L}\\p{N}]+")) {
                for (int len = target.length() - 1; len <= target.length() + 1; len++) {
                    if (len < 2 || len > word.length()) {
                        continue;
                    }
                    for (int start = 0; start + len <= word.length(); start++) {
                        inThisProduct.add(word.substring(start, start + len));
                    }
                }
            }
            // 한 상품에서는 같은 조각을 1번만 센다
            for (String piece : inThisProduct) {
                if (candidates.containsKey(piece)) {
                    candidates.put(piece, candidates.get(piece) + 1);
                } else {
                    candidates.put(piece, 1);
                }
            }
        }

        String best = null;
        int bestDistance = Integer.MAX_VALUE;
        int bestCount = 0;
        for (Map.Entry<String, Integer> entry : candidates.entrySet()) {
            String piece = entry.getKey();
            String lower = piece.toLowerCase();
            // 검색어와 똑같으면 추천할 필요가 없다
            if (lower.equals(target)) {
                continue;
            }
            int distance = editDistance(targetJamo, toJamo(lower));
            if (distance > maxDistance) {
                continue;
            }
            // 1) 덜 고쳐도 되는 것  2) 검색어와 글자 수가 같은 것  3) 더 많은 상품에 들어 있는 것  4) 가나다순
            if (best == null
                    || distance < bestDistance
                    || (distance == bestDistance && compareCandidate(piece, entry.getValue(), best, bestCount, target.length()) < 0)) {
                best = piece;
                bestDistance = distance;
                bestCount = entry.getValue();
            }
        }
        return best;
    }

    // 거리가 같을 때 어느 후보가 더 나은지 (음수면 a 가 더 낫다)
    private static int compareCandidate(String a, int aCount, String b, int bCount, int targetLength) {
        boolean aSameLength = a.length() == targetLength;
        boolean bSameLength = b.length() == targetLength;
        if (aSameLength != bSameLength) {
            return aSameLength ? -1 : 1;
        }
        if (aCount != bCount) {
            return bCount - aCount;
        }
        return a.compareTo(b);
    }

    // 고쳐도 되는 정도 : 짧은 검색어일수록 조금만 고쳐야 한다 (KEY_COST 10 = 자모 1개)
    // 예) 두 글자(자모 4~6개) → 자모 1개, 세~네 글자 → 2개, 그보다 길면 3개
    private static int maxDistance(int jamoLength) {
        if (jamoLength <= 6) {
            return KEY_COST;
        }
        if (jamoLength <= 12) {
            return KEY_COST * 2;
        }
        return KEY_COST * 3;
    }

    // 한글 음절을 자모로 푼다 (한글이 아닌 글자는 그대로 둔다)
    // 예) "김치" → "ㄱㅣㅁㅊㅣ"
    static String toJamo(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 0xAC00 && c <= 0xD7A3) {
                int index = c - 0xAC00;
                result.append(CHOSEONG.charAt(index / (21 * 28)));
                result.append(JUNGSEONG.charAt((index % (21 * 28)) / 28));
                int jong = index % 28;
                if (jong > 0) {
                    result.append(JONGSEONG.charAt(jong));     // 받침이 있을 때만
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    // 편집 거리 : a 를 b 로 만들려면 얼마나 넣고/빼고/바꿔야 하는지 (넣기·빼기 = KEY_COST, 바꾸기 = replaceCost)
    static int editDistance(String a, String b) {
        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            previous[j] = j * KEY_COST;
        }
        for (int i = 1; i <= a.length(); i++) {
            current[0] = i * KEY_COST;
            for (int j = 1; j <= b.length(); j++) {
                int replace = previous[j - 1] + replaceCost(a.charAt(i - 1), b.charAt(j - 1));
                int delete = previous[j] + KEY_COST;
                int insert = current[j - 1] + KEY_COST;
                current[j] = Math.min(replace, Math.min(delete, insert));
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[b.length()];
    }

    // 글자 하나를 다른 글자로 바꾸는 비용 : 같으면 0, 키보드에서 이웃한 키면 NEAR_KEY_COST, 아니면 KEY_COST
    static int replaceCost(char a, char b) {
        if (a == b) {
            return 0;
        }
        int[] keyA = KEY_POSITIONS.get(a);
        int[] keyB = KEY_POSITIONS.get(b);
        if (keyA == null || keyB == null) {
            return KEY_COST;                 // ㅘ, ㄳ 처럼 키 하나로 치지 않는 자모나 한글이 아닌 글자
        }
        return isNearKey(keyA, keyB) ? NEAR_KEY_COST : KEY_COST;
    }

    // 같은 키(Shift 만 다름)이거나 바로 옆 키인지
    // 자판은 아래 줄로 갈수록 오른쪽으로 반 칸씩 밀려 있어서, 아래 줄 c 칸은 윗줄 c 칸, c+1 칸과 붙어 있다
    private static boolean isNearKey(int[] a, int[] b) {
        int rowDiff = a[0] - b[0];
        int colDiff = a[1] - b[1];
        if (rowDiff == 0) {
            return Math.abs(colDiff) <= 1;
        }
        if (rowDiff == 1) {                   // a 가 아래 줄
            return colDiff == 0 || colDiff == -1;
        }
        if (rowDiff == -1) {                  // a 가 윗줄
            return colDiff == 0 || colDiff == 1;
        }
        return false;
    }

    // "김치" → "를", "떡" → "을" (받침이 있으면 을, 없으면 를. 한글이 아니면 "을(를)")
    public static String objectParticle(String word) {
        if (word == null || word.isEmpty()) {
            return "을(를)";
        }
        char last = word.charAt(word.length() - 1);
        if (last < 0xAC00 || last > 0xD7A3) {
            return "을(를)";
        }
        return (last - 0xAC00) % 28 > 0 ? "을" : "를";
    }
}