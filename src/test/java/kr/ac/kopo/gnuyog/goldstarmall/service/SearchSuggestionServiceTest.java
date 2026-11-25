package kr.ac.kopo.gnuyog.goldstarmall.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// 검색어 추천 테스트 (DB 없이 상품 이름 목록만으로 확인한다)
class SearchSuggestionServiceTest {

    private static final List<String> NAMES = List.of(
            "제주 삼다수 2L x 6개", "신라면 5개입", "국내산 사과 2kg", "무선 블루투스 이어폰",
            "포기김치 3kg", "제주 은갈치 2마리", "런닝화", "오버핏 맨투맨", "기모 후드티", "데님 일자 청바지");

    @Test
    void 모음을_잘못_치면_비슷한_단어를_추천한다() {
        assertEquals("김치", SearchSuggestionService.suggest("감치", NAMES));
        assertEquals("사과", SearchSuggestionService.suggest("사가", NAMES));
    }

    @Test
    void 똑같이_한_글자_차이면_키보드에서_옆_키로_틀린_쪽을_추천한다() {
        // 감치 : 김치(ㅏ↔ㅣ, K 옆 L) 와 갈치(ㅁ↔ㄹ, A 와 F) 모두 1글자 차이
        assertEquals("김치", SearchSuggestionService.suggest("감치", NAMES));
        // 김치 대신 깁치 (ㅁ↔ㅂ 은 A 와 Q 로 위아래로 붙은 키)
        assertEquals("김치", SearchSuggestionService.suggest("깁치", NAMES));
    }

    @Test
    void 받침을_빼먹거나_더_쳐도_추천한다() {
        assertEquals("신라면", SearchSuggestionService.suggest("신라며", NAMES));
        assertEquals("맨투맨", SearchSuggestionService.suggest("맨투매", NAMES));
    }

    @Test
    void 긴_단어는_두_군데_틀려도_추천한다() {
        assertEquals("블루투스", SearchSuggestionService.suggest("블로토스", NAMES));
    }

    @Test
    void 전혀_다른_검색어는_추천하지_않는다() {
        assertNull(SearchSuggestionService.suggest("냉장고", NAMES));
        assertNull(SearchSuggestionService.suggest("자동차", NAMES));
    }

    @Test
    void 한_글자는_추천하지_않는다() {
        assertNull(SearchSuggestionService.suggest("감", NAMES));
    }

    @Test
    void 한글을_자모로_푼다() {
        assertEquals("ㄱㅣㅁㅊㅣ", SearchSuggestionService.toJamo("김치"));
        assertEquals("ㄸㅓㄱ", SearchSuggestionService.toJamo("떡"));
        assertEquals("ㅅㅏㄹㅏㅁㅕㄴ3kg", SearchSuggestionService.toJamo("사라면3kg"));
    }

    @Test
    void 키보드에서_옆_키인지_안다() {
        // 같은 줄 옆 키
        assertTrue(SearchSuggestionService.isNearKey('ㅏ', 'ㅣ'));   // K, L
        // 위아래 줄 : 가운데 줄 A(ㅁ) 는 윗줄 Q(ㅂ), W(ㅈ) 와 붙어 있다
        assertTrue(SearchSuggestionService.isNearKey('ㅁ', 'ㅂ'));
        assertTrue(SearchSuggestionService.isNearKey('ㅈ', 'ㅁ'));
        // 아랫줄 Z(ㅋ) 는 가운데 줄 A(ㅁ), S(ㄴ) 와 붙어 있다
        assertTrue(SearchSuggestionService.isNearKey('ㅋ', 'ㄴ'));
        // Shift 만 다른 같은 키
        assertTrue(SearchSuggestionService.isNearKey('ㄱ', 'ㄲ'));
        assertTrue(SearchSuggestionService.isNearKey('ㅐ', 'ㅒ'));
        assertTrue(SearchSuggestionService.isNearKey('ㅖ', 'ㅔ'));
        // 먼 키
        assertFalse(SearchSuggestionService.isNearKey('ㅁ', 'ㄹ'));   // A, F
        assertFalse(SearchSuggestionService.isNearKey('ㅋ', 'ㅂ'));   // Z, Q (두 줄 차이)
        // 키 하나로 치지 않는 자모
        assertFalse(SearchSuggestionService.isNearKey('ㅘ', 'ㅏ'));
    }

    @Test
    void 틀린_정도를_점수로_센다() {
        assertEquals(0, SearchSuggestionService.editCost("ㄱㅣㅁㅊㅣ", "ㄱㅣㅁㅊㅣ"));
        assertEquals(6, SearchSuggestionService.editCost("ㄱㅏㅁㅊㅣ", "ㄱㅣㅁㅊㅣ"));    // 옆 키로 바꾸기
        assertEquals(10, SearchSuggestionService.editCost("ㄱㅏㅁㅊㅣ", "ㄱㅏㄹㅊㅣ"));   // 먼 키로 바꾸기
        assertEquals(10, SearchSuggestionService.editCost("ㅅㅣㄴㄹㅏㅁㅕ", "ㅅㅣㄴㄹㅏㅁㅕㄴ")); // 하나 넣기
    }

    @Test
    void 받침에_따라_을_를을_고른다() {
        assertEquals("를", SearchSuggestionService.objectParticle("김치"));
        assertEquals("을", SearchSuggestionService.objectParticle("신라면"));
        assertEquals("을(를)", SearchSuggestionService.objectParticle("3kg"));
    }
}
