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
//
// 순서
// 1) 후보 모으기 : 상품 이름을 단어로 나누고, 단어 안에서 검색어와 길이가 비슷한 조각을 모은다
//    예) 검색어 '감치'(2글자) → '포기김치' 에서 '포기', '기김', '김치', '포기김', '기김치' ...
// 2) 얼마나 다른지 세기 : 한글을 자모로 풀어서(감치 = ㄱㅏㅁㅊㅣ) 몇 개를 고쳐야 같아지는지 센다
//    키보드에서 바로 옆 키를 잘못 누른 오타는 덜 틀린 것으로 친다
//    예) '감치' → 김치(ㅏ↔ㅣ, K 와 L 은 옆 키) 6점, 갈치(ㅁ↔ㄹ, A 와 F 는 먼 키) 10점 → 김치를 추천
// 3) 가장 덜 다른 후보를 고른다 (너무 많이 달라야 하면 추천하지 않는다)
@Service
@Transactional(readOnly = true)
public class SearchSuggestionService {

    // 추천할 수 있는 검색어 길이 (한 글자는 비슷한 단어가 너무 많고, 너무 길면 후보가 너무 많아진다)
    private static final int MIN_KEYWORD_LENGTH = 2;
    private static final int MAX_KEYWORD_LENGTH = 20;

    // 틀린 정도(점수) : 자모 1개를 넣기/빼기/바꾸기 = 10점, 옆 키(또는 Shift 만 다른 키)로 바꾸기 = 6점
    // 0.6 같은 소수 대신 정수로 센다 (소수는 더하다 보면 2.2000000000000002 처럼 어긋날 수 있다)
    private static final int KEY_COST = 10;
    private static final int NEAR_KEY_COST = 6;

    private final ProductRepository productRepository;

    public SearchSuggestionService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // 추천 검색어 (비슷한 것이 없으면 null)
    public String suggest(String keyword) {
        // 너무 짧은 검색어는 상품을 불러오기 전에 걸러낸다
        if (keyword == null || keyword.trim().length() < MIN_KEYWORD_LENGTH) {
            return null;
        }
        List<String> productNames = new ArrayList<>();
        for (Product product : productRepository.findAll()) {
            productNames.add(product.getName());
        }
        return suggest(keyword, productNames);
    }

    // 상품 이름 목록에서 추천 검색어 고르기 (DB 없이 테스트할 수 있게 따로 둔다)
    static String suggest(String keyword, List<String> productNames) {
        if (keyword == null) {
            return null;
        }
        // 띄어쓰기는 빼고, 영어는 소문자로 비교한다
        String target = keyword.replaceAll("\\s+", "").toLowerCase();
        if (target.length() < MIN_KEYWORD_LENGTH || target.length() > MAX_KEYWORD_LENGTH) {
            return null;
        }
        String targetJamo = toJamo(target);
        int maxCost = maxCost(targetJamo.length());

        Candidate best = null;
        Map<String, Integer> pieces = collectPieces(productNames, target.length());
        for (String piece : pieces.keySet()) {
            // 검색어와 똑같으면 추천할 필요가 없다
            if (piece.toLowerCase().equals(target)) {
                continue;
            }
            int cost = editCost(targetJamo, toJamo(piece.toLowerCase()));
            if (cost > maxCost) {
                continue;
            }
            Candidate candidate = new Candidate(piece, cost, pieces.get(piece));
            if (best == null || candidate.isBetterThan(best, target.length())) {
                best = candidate;
            }
        }
        return best == null ? null : best.word;
    }

    // ===== 1) 후보 모으기 =====

    // 상품 이름들에서 검색어와 길이가 비슷한(한 글자 짧은 것 ~ 한 글자 긴 것) 조각을 모은다
    // 돌려주는 값 : 조각 → 그 조각이 들어 있는 상품 수 (많은 상품에 들어 있을수록 흔한 단어다)
    private static Map<String, Integer> collectPieces(List<String> productNames, int targetLength) {
        int minLength = Math.max(MIN_KEYWORD_LENGTH, targetLength - 1);
        int maxLength = targetLength + 1;

        Map<String, Integer> pieceCounts = new HashMap<>();
        for (String name : productNames) {
            // 한 상품 안에서는 같은 조각을 1번만 센다
            Set<String> piecesInName = new HashSet<>();
            // 글자와 숫자가 아닌 것(띄어쓰기, 괄호 등)을 기준으로 단어를 나눈다. 예) '포기김치 3kg' → '포기김치', '3kg'
            for (String word : name.split("[^\\p{L}\\p{N}]+")) {
                for (int length = minLength; length <= maxLength; length++) {
                    for (int start = 0; start + length <= word.length(); start++) {
                        piecesInName.add(word.substring(start, start + length));
                    }
                }
            }
            for (String piece : piecesInName) {
                int count = pieceCounts.getOrDefault(piece, 0);
                pieceCounts.put(piece, count + 1);
            }
        }
        return pieceCounts;
    }

    // 추천 후보 하나 : 조각, 틀린 정도(점수), 그 조각이 들어 있는 상품 수
    private static class Candidate {
        final String word;
        final int cost;
        final int productCount;

        Candidate(String word, int cost, int productCount) {
            this.word = word;
            this.cost = cost;
            this.productCount = productCount;
        }

        // 이 후보가 other 보다 나은지
        // 1) 덜 틀린 것  2) 검색어와 글자 수가 같은 것  3) 더 많은 상품에 들어 있는 것  4) 가나다순으로 앞인 것
        boolean isBetterThan(Candidate other, int targetLength) {
            if (cost != other.cost) {
                return cost < other.cost;
            }
            boolean sameLength = word.length() == targetLength;
            boolean otherSameLength = other.word.length() == targetLength;
            if (sameLength != otherSameLength) {
                return sameLength;
            }
            if (productCount != other.productCount) {
                return productCount > other.productCount;
            }
            return word.compareTo(other.word) < 0;
        }
    }

    // ===== 2) 얼마나 다른지 세기 =====

    // 허용하는 틀린 정도 : 짧은 검색어일수록 조금만 틀려야 추천한다
    // 예) 두 글자(자모 4~6개) → 자모 1개, 세~네 글자(자모 7~12개) → 2개, 그보다 길면 3개
    private static int maxCost(int jamoLength) {
        if (jamoLength <= 6) {
            return KEY_COST;
        }
        if (jamoLength <= 12) {
            return KEY_COST * 2;
        }
        return KEY_COST * 3;
    }

    // 편집 거리 : a 를 b 로 만들려면 자모를 얼마나 넣고/빼고/바꿔야 하는지 (점수의 합이 가장 작은 방법)
    // table[i][j] = a 의 앞 i 글자를 b 의 앞 j 글자로 만드는 데 드는 점수
    static int editCost(String a, String b) {
        int[][] table = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            table[i][0] = i * KEY_COST;            // a 의 글자를 전부 뺀다
        }
        for (int j = 0; j <= b.length(); j++) {
            table[0][j] = j * KEY_COST;            // b 의 글자를 전부 넣는다
        }
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int replace = table[i - 1][j - 1] + replaceCost(a.charAt(i - 1), b.charAt(j - 1));
                int delete = table[i - 1][j] + KEY_COST;
                int insert = table[i][j - 1] + KEY_COST;
                table[i][j] = Math.min(replace, Math.min(delete, insert));
            }
        }
        return table[a.length()][b.length()];
    }

    // 자모 하나를 다른 자모로 바꾸는 점수 : 같으면 0, 키보드에서 옆 키면 6, 아니면 10
    static int replaceCost(char a, char b) {
        if (a == b) {
            return 0;
        }
        if (isNearKey(a, b)) {
            return NEAR_KEY_COST;
        }
        return KEY_COST;
    }

    // ===== 한글 =====

    private static final char FIRST_HANGUL = '가';   // 0xAC00
    private static final char LAST_HANGUL = '힣';    // 0xD7A3

    // 초성 19개, 중성 21개, 종성 28개(0번은 받침 없음)
    private static final String CHOSEONG = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ";
    private static final String JUNGSEONG = "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ";
    private static final String JONGSEONG = " ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ";

    private static boolean isHangul(char c) {
        return c >= FIRST_HANGUL && c <= LAST_HANGUL;
    }

    // 한글 글자를 자모로 푼다 (한글이 아닌 글자는 그대로 둔다)
    // 한글 글자 번호 = (초성 번호 x 21 + 중성 번호) x 28 + 종성 번호   예) "김치" → "ㄱㅣㅁㅊㅣ"
    static String toJamo(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!isHangul(c)) {
                result.append(c);
                continue;
            }
            int number = c - FIRST_HANGUL;
            int cho = number / (21 * 28);
            int jung = (number % (21 * 28)) / 28;
            int jong = number % 28;
            result.append(CHOSEONG.charAt(cho));
            result.append(JUNGSEONG.charAt(jung));
            if (jong > 0) {
                result.append(JONGSEONG.charAt(jong));
            }
        }
        return result.toString();
    }

    // 목적격 조사 : 받침이 있으면 '을', 없으면 '를', 한글로 끝나지 않으면 '을(를)'
    // 예) "떡" → "을", "김치" → "를", "3kg" → "을(를)"
    public static String objectParticle(String word) {
        if (word == null || word.isEmpty()) {
            return "을(를)";
        }
        char last = word.charAt(word.length() - 1);
        if (!isHangul(last)) {
            return "을(를)";
        }
        int jong = (last - FIRST_HANGUL) % 28;
        if (jong > 0) {
            return "을";
        }
        return "를";
    }

    // ===== 두벌식 키보드 =====

    // 키보드 세 줄 (왼쪽부터)
    //   윗줄    Q W E R T Y U I O P  →  ㅂ ㅈ ㄷ ㄱ ㅅ ㅛ ㅕ ㅑ ㅐ ㅔ
    //   가운데줄 A S D F G H J K L    →  ㅁ ㄴ ㅇ ㄹ ㅎ ㅗ ㅓ ㅏ ㅣ
    //   아랫줄   Z X C V B N M        →  ㅋ ㅌ ㅊ ㅍ ㅠ ㅜ ㅡ
    private static final String[] KEY_ROWS = {"ㅂㅈㄷㄱㅅㅛㅕㅑㅐㅔ", "ㅁㄴㅇㄹㅎㅗㅓㅏㅣ", "ㅋㅌㅊㅍㅠㅜㅡ"};

    // Shift 를 누르면 나오는 자모와 원래 키 (예: ㄲ 은 ㄱ 키에 Shift)
    private static final String SHIFT_JAMO = "ㅃㅉㄸㄲㅆㅒㅖ";
    private static final String BASE_JAMO = "ㅂㅈㄷㄱㅅㅐㅔ";

    // 두 자모가 같은 키(Shift 만 다름)이거나 바로 옆 키인지
    static boolean isNearKey(char a, char b) {
        char keyA = withoutShift(a);
        char keyB = withoutShift(b);
        if (keyA == keyB) {
            return true;                            // 예) ㄱ 과 ㄲ
        }
        int rowA = findRow(keyA);
        int rowB = findRow(keyB);
        if (rowA < 0 || rowB < 0) {
            return false;                           // ㅘ, ㄳ 처럼 키 하나로 치지 않는 자모나 한글이 아닌 글자
        }
        int colA = KEY_ROWS[rowA].indexOf(keyA);
        int colB = KEY_ROWS[rowB].indexOf(keyB);

        // 같은 줄 : 왼쪽이나 오른쪽 바로 옆
        if (rowA == rowB) {
            return Math.abs(colA - colB) == 1;
        }
        // 위아래 줄 : 키보드는 아래 줄로 갈수록 오른쪽으로 반 칸씩 밀려 있다
        // 그래서 아래 줄 n 번째 키는 윗줄 n 번째, n+1 번째 키와 붙어 있다 (예: ㅁ(A) 은 ㅂ(Q), ㅈ(W) 와 붙어 있다)
        if (Math.abs(rowA - rowB) == 1) {
            int lowerCol = rowA > rowB ? colA : colB;
            int upperCol = rowA > rowB ? colB : colA;
            return upperCol == lowerCol || upperCol == lowerCol + 1;
        }
        return false;
    }

    // Shift 로 나오는 자모면 원래 키로 바꾼다 (ㄲ → ㄱ). 아니면 그대로
    private static char withoutShift(char c) {
        int index = SHIFT_JAMO.indexOf(c);
        if (index < 0) {
            return c;
        }
        return BASE_JAMO.charAt(index);
    }

    // 키보드 몇 번째 줄에 있는 키인지 (0: 윗줄, 1: 가운데 줄, 2: 아랫줄, 없으면 -1)
    private static int findRow(char key) {
        for (int row = 0; row < KEY_ROWS.length; row++) {
            if (KEY_ROWS[row].indexOf(key) >= 0) {
                return row;
            }
        }
        return -1;
    }
}
