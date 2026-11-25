package kr.ac.kopo.gnuyog.goldstarmall.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    void 받침에_따라_을_를을_고른다() {
        assertEquals("를", SearchSuggestionService.objectParticle("김치"));
        assertEquals("을", SearchSuggestionService.objectParticle("신라면"));
        assertEquals("을(를)", SearchSuggestionService.objectParticle("3kg"));
    }
}
