-- 골드스타몰 상품 데이터
-- 사용법 : 서버를 한 번 실행해 테이블을 만든 뒤, MySQL 쿼리 콘솔에서 이 파일 전체를 실행한다
-- id 는 AUTO_INCREMENT 라서 넣지 않아도 1번부터 자동으로 붙는다

USE goldstarmall;

INSERT INTO products (name, category, price, stock, description, icon) VALUES
('제주 삼다수 2L x 6개', '식품', 6480, 100, '제주 화산암반수로 만든 생수 6개 묶음입니다.', '💧'),
('신라면 5개입', '식품', 4100, 80, '얼큰한 국물의 대표 라면 5개 묶음입니다.', '🍜'),
('국내산 사과 2kg', '식품', 15900, 30, '아삭하고 달콤한 국내산 사과입니다.', '🍎'),
('3겹 화장지 30롤', '생활용품', 13900, 60, '부드럽고 도톰한 3겹 화장지입니다.', '🧻'),
('액체 세탁세제 3L', '생활용품', 11500, 45, '찬물에도 잘 녹는 액체 세탁세제입니다.', '🧴'),
('주방 수세미 10개', '주방용품', 3900, 120, '흠집 걱정 없는 부드러운 수세미입니다.', '🧽'),
('무선 블루투스 이어폰', '가전디지털', 39900, 25, '소음 차단 기능이 있는 무선 이어폰입니다.', '🎧'),
('미니 선풍기', '가전디지털', 12900, 40, 'USB 충전식 휴대용 선풍기입니다.', '🌀'),
('전기 주전자 1.7L', '가전디지털', 24900, 3, '빠르게 끓는 스테인리스 전기 주전자입니다.', '🫖'),
('기능성 티셔츠', '패션의류', 9900, 70, '땀을 빠르게 흡수하고 말려 주는 가벼운 기능성 티셔츠입니다.', '👕'),
('러닝화', '스포츠/레저', 49900, 15, '가볍고 쿠션감 좋은 러닝화입니다.', '👟'),
('노트북 백팩', '잡화', 29900, 0, '노트북 수납이 가능한 캔버스 백팩입니다.', '🎒');

-- 추가 상품 (13번 ~ 32번) : 모든 카테고리에 상품이 2개 이상 보이도록 추가
INSERT INTO products (name, category, price, stock, description, icon) VALUES
('데님 일자 청바지', '패션의류', 39900, 35, '어디에나 잘 어울리는 기본 일자핏 청바지입니다.', '👖'),
('면 볼캡', '잡화', 15900, 50, '가볍고 통기성 좋은 면 소재 모자입니다.', '🧢'),
('수분 크림 50ml', '뷰티', 18900, 60, '건조한 피부에 촉촉함을 채워 주는 수분 크림입니다.', '🧴'),
('촉촉 립스틱', '뷰티', 12000, 4, '발색이 선명하고 오래 유지되는 립스틱입니다.', '💄'),
('스테인리스 프라이팬 28cm', '주방용품', 32900, 20, '열전도가 좋은 통삼중 프라이팬입니다.', '🍳'),
('LED 무드등', '홈인테리어', 19900, 30, '밝기 조절이 되는 침실용 무드등입니다.', '💡'),
('미니 화분 세트', '홈인테리어', 14900, 25, '책상 위에 두기 좋은 작은 화분 3개 세트입니다.', '🪴'),
('요가 매트 10mm', '스포츠/레저', 21900, 40, '두툼하고 미끄럼 방지가 되는 요가 매트입니다.', '🧘'),
('차량용 방향제', '자동차용품', 8900, 80, '은은한 향이 오래가는 송풍구형 방향제입니다.', '🚗'),
('차량용 휴대폰 거치대', '자동차용품', 13900, 45, '한 손으로 끼우고 뺄 수 있는 거치대입니다.', '📱'),
('클래식 명곡 모음 CD', '음반/dvd', 16900, 15, '누구나 아는 클래식 명곡 20곡을 담은 CD입니다.', '💿'),
('애니메이션 DVD 세트', '음반/dvd', 29900, 0, '온 가족이 함께 보는 애니메이션 DVD 3편 세트입니다.', '📀'),
('1000피스 직소 퍼즐', '완구/취미', 17900, 22, '완성하면 액자로 걸 수 있는 풍경 퍼즐입니다.', '🧩'),
('창작 블록 세트', '완구/취미', 34900, 18, '마음대로 조립하는 500피스 블록 장난감입니다.', '🧱'),
('젤펜 12색 세트', '문구/오피스', 6900, 100, '부드럽게 써지는 0.5mm 젤펜 12색입니다.', '🖊️'),
('A4 복사용지 500매', '문구/오피스', 5900, 90, '걸림 없이 잘 인쇄되는 80g A4 용지입니다.', '📄'),
('강아지 사료 2kg', '반려동물용품', 24900, 30, '소화가 잘되는 전 연령용 강아지 사료입니다.', '🐶'),
('고양이 스크래쳐', '반려동물용품', 9900, 3, '골판지로 만든 고양이 발톱 긁개입니다.', '🐱'),
('종합비타민 90정', '헬스/건강식품', 19900, 55, '하루 한 알로 챙기는 종합비타민입니다.', '💊'),
('단백질 쉐이크 1kg', '헬스/건강식품', 35900, 25, '물에 잘 녹는 초코맛 단백질 파우더입니다.', '🥤');

-- 추가 상품 (33번 ~ 92번) : 카테고리마다 4개씩 더 넣어 카테고리당 6~7개가 되도록 추가
INSERT INTO products (name, category, price, stock, description, icon) VALUES
-- 패션의류
('오버핏 맨투맨', '패션의류', 24900, 40, '도톰한 기모 안감으로 따뜻한 오버핏 맨투맨입니다.', '👕'),
('경량 패딩 조끼', '패션의류', 34900, 25, '가볍고 따뜻해 겉옷 안에 입기 좋은 패딩 조끼입니다.', '🧥'),
('플리츠 롱스커트', '패션의류', 29900, 18, '움직일 때마다 주름이 살아나는 롱스커트입니다.', '👗'),
('수면 양말 5켤레', '패션의류', 9900, 2, '부드럽고 포근한 극세사 수면 양말 묶음입니다.', '🧦'),
-- 잡화
('가죽 반지갑', '잡화', 39900, 20, '카드와 지폐를 깔끔하게 정리하는 소가죽 반지갑입니다.', '👛'),
('3단 자동 우산', '잡화', 14900, 60, '버튼 하나로 펴고 접는 튼튼한 자동 우산입니다.', '☂️'),
('미러 선글라스', '잡화', 25900, 15, '자외선을 차단하는 가벼운 미러 렌즈 선글라스입니다.', '🕶️'),
('니트 목도리', '잡화', 17900, 0, '겨울철 목을 따뜻하게 감싸는 니트 목도리입니다.', '🧣'),
-- 뷰티
('촉촉 선크림 SPF50+', '뷰티', 16900, 70, '끈적임 없이 가볍게 발리는 데일리 선크림입니다.', '🧴'),
('시트 마스크팩 10매', '뷰티', 12900, 90, '하루 한 장으로 피부에 수분을 채워 주는 마스크팩입니다.', '🧖'),
('네일 컬러 5종 세트', '뷰티', 15900, 30, '빨리 마르고 오래가는 네일 컬러 5가지 세트입니다.', '💅'),
('플로럴 향수 50ml', '뷰티', 45900, 5, '은은한 꽃향기가 오래 남는 오 드 퍼퓸입니다.', '🌸'),
-- 식품
('즉석밥 210g x 12개', '식품', 14900, 80, '전자레인지에 2분이면 완성되는 즉석밥 묶음입니다.', '🍚'),
('1등급 우유 1L x 2개', '식품', 5980, 50, '신선한 1등급 원유로 만든 우유 2개 묶음입니다.', '🥛'),
('특란 30구', '식품', 8900, 40, '크고 신선한 특란 한 판입니다.', '🥚'),
('제주 감귤 3kg', '식품', 19900, 3, '새콤달콤한 제철 제주 감귤입니다.', '🍊'),
-- 주방용품
('스테인리스 냄비 3종 세트', '주방용품', 49900, 15, '찌개, 라면, 국 요리에 두루 쓰는 냄비 세트입니다.', '🍲'),
('원목 도마', '주방용품', 18900, 35, '칼자국이 덜 남는 단단한 원목 도마입니다.', '🪵'),
('밀폐용기 10종 세트', '주방용품', 22900, 50, '반찬과 식재료를 신선하게 보관하는 밀폐용기 세트입니다.', '🥡'),
('세라믹 머그컵 2개', '주방용품', 12900, 0, '커피와 차를 담기 좋은 심플한 머그컵 세트입니다.', '☕'),
-- 생활용품
('대용량 물티슈 10팩', '생활용품', 12900, 100, '도톰하고 촉촉한 순한 물티슈 묶음입니다.', '🧻'),
('해피바스 로즈 바디워시 500g 1개', '생활용품', 9900, 55, '은은한 장미 향이 오래 남는 해피바스 로즈 바디워시입니다.', '🧼'),
('미세모 칫솔 8개입', '생활용품', 7900, 70, '잇몸에 부드러운 미세모 칫솔 묶음입니다.', '🪥'),
('항균 수건 5장', '생활용품', 19900, 4, '흡수력이 좋고 빨리 마르는 호텔식 수건 세트입니다.', '🛁'),
-- 홈인테리어
('암막 커튼 2장', '홈인테리어', 39900, 20, '햇빛을 차단해 숙면을 돕는 암막 커튼입니다.', '🪟'),
('극세사 러그 140x200', '홈인테리어', 34900, 12, '거실을 포근하게 꾸며 주는 극세사 러그입니다.', '🟫'),
('벽걸이 무소음 시계', '홈인테리어', 15900, 40, '초침 소리가 나지 않는 깔끔한 벽시계입니다.', '🕰️'),
('리넨 쿠션 커버 2개', '홈인테리어', 11900, 0, '자연스러운 질감의 리넨 쿠션 커버 세트입니다.', '🛋️'),
-- 가전디지털
('보조배터리 10000mAh', '가전디지털', 19900, 60, '가볍고 고속 충전을 지원하는 보조배터리입니다.', '🔋'),
('무선 마우스', '가전디지털', 15900, 45, '클릭 소리가 작은 저소음 무선 마우스입니다.', '🖱️'),
('블루투스 스피커', '가전디지털', 45900, 18, '작지만 풍성한 소리를 내는 방수 스피커입니다.', '🔊'),
('스마트 워치', '가전디지털', 129000, 2, '걸음 수, 심박수, 알림을 확인하는 스마트 워치입니다.', '⌚'),
-- 스포츠/레저
('덤벨 5kg 2개', '스포츠/레저', 32900, 25, '집에서 근력 운동하기 좋은 코팅 덤벨 한 쌍입니다.', '🏋️'),
('2인용 원터치 텐트', '스포츠/레저', 69900, 8, '펼치기만 하면 설치되는 캠핑용 텐트입니다.', '⛺'),
('입문용 배드민턴 라켓 세트', '스포츠/레저', 24900, 30, '라켓 2자루와 셔틀콕이 들어 있는 세트입니다.', '🏸'),
('보온 스포츠 물병 750ml', '스포츠/레저', 13900, 0, '찬물은 차갑게, 따뜻한 물은 따뜻하게 지켜 주는 물병입니다.', '🧴'),
-- 자동차용품
('차량용 고속 충전기', '자동차용품', 12900, 50, '시거잭에 꽂아 두 대를 동시에 충전하는 충전기입니다.', '🔌'),
('무선 차량용 청소기', '자동차용품', 39900, 15, '좌석 틈새 먼지까지 빨아들이는 무선 청소기입니다.', '🧹'),
('메모리폼 목쿠션', '자동차용품', 14900, 35, '장거리 운전에도 목이 편한 메모리폼 쿠션입니다.', '💺'),
('발수 코팅 와이퍼 2개', '자동차용품', 19900, 3, '비 오는 날 시야를 깨끗하게 해 주는 와이퍼입니다.', '🌧️'),
-- 음반/dvd
('재즈 베스트 앨범 CD', '음반/dvd', 15900, 20, '카페에서 듣기 좋은 재즈 명곡 모음 CD입니다.', '🎷'),
('어린이 동요 CD', '음반/dvd', 12900, 30, '아이와 함께 따라 부르는 인기 동요 40곡입니다.', '🎵'),
('[블루레이] 밴드 오브 브라더스 : 리패키지 (6disc)', '음반/dvd', 24900, 10, '제2차 세계대전 이지 중대의 여정을 그린 HBO 10부작 미니시리즈를 6장의 블루레이에 담은 리패키지입니다.', '🎬'),
('LP 레코드 팝 명곡집', '음반/dvd', 39900, 0, '따뜻한 아날로그 소리로 듣는 팝 명곡 LP입니다.', '📻'),
-- 완구/취미
('RC 자동차', '완구/취미', 45900, 12, '리모컨으로 빠르게 달리는 충전식 RC 자동차입니다.', '🏎️'),
('보드게임 패밀리 세트', '완구/취미', 29900, 20, '온 가족이 함께 즐기는 보드게임 3종 세트입니다.', '🎲'),
('곰돌이 봉제 인형', '완구/취미', 19900, 40, '부드럽고 포근한 곰돌이 인형입니다.', '🧸'),
('수채화 물감 24색 세트', '완구/취미', 22900, 4, '붓과 팔레트가 함께 들어 있는 수채화 세트입니다.', '🎨'),
-- 문구/오피스
('스프링 노트 5권', '문구/오피스', 7900, 80, '줄 간격이 넉넉한 A5 스프링 노트 묶음입니다.', '📓'),
('형광펜 6색 세트', '문구/오피스', 4900, 100, '번지지 않고 선명한 형광펜 6색입니다.', '🖍️'),
('스테이플러 + 심 세트', '문구/오피스', 8900, 45, '종이 20장도 한 번에 찍히는 스테이플러입니다.', '📎'),
('2027 탁상 달력', '문구/오피스', 6900, 0, '일정을 적기 좋은 넓은 칸의 탁상 달력입니다.', '📅'),
-- 반려동물용품
('고양이 모래 10L', '반려동물용품', 15900, 40, '잘 뭉치고 먼지가 적은 벤토나이트 모래입니다.', '🐈'),
('강아지 산책 리드줄', '반려동물용품', 12900, 35, '손잡이가 편하고 튼튼한 산책용 리드줄입니다.', '🦮'),
('반려동물 쿠션 방석', '반려동물용품', 24900, 15, '푹신하고 세탁이 쉬운 반려동물 방석입니다.', '🛏️'),
('강아지 수제 간식', '반려동물용품', 8900, 2, '닭가슴살로 만든 무첨가 수제 간식입니다.', '🦴'),
-- 헬스/건강식품
('오메가3 60캡슐', '헬스/건강식품', 24900, 40, '하루 두 캡슐로 챙기는 오메가3입니다.', '🐟'),
('유산균 30포', '헬스/건강식품', 29900, 50, '장 건강을 위한 분말형 유산균입니다.', '🦠'),
('홍삼 스틱 30포', '헬스/건강식품', 39900, 20, '언제 어디서나 간편하게 먹는 홍삼 스틱입니다.', '🌿'),
('비타민C 1000mg 100정', '헬스/건강식품', 14900, 3, '하루 한 알로 채우는 고함량 비타민C입니다.', '🍋');

-- 상품 정보 (제조사/브랜드, 원산지) : 상품 상세 화면의 "상품 정보" 표에 나온다
-- 실제 브랜드 상품(삼다수, 신라면)은 실제 제조사, 신선식품은 '국내 산지 농가',
-- 나머지는 쇼핑몰 자체 브랜드인 '골드스타 PB' 로 넣는다
-- (MySQL Workbench 의 safe update 모드에서는 WHERE 에 키가 없다고 막힐 수 있다 : SET SQL_SAFE_UPDATES = 0; 을 먼저 실행)
UPDATE products SET manufacturer = '제주특별자치도개발공사', origin = '국내산 (제주)' WHERE name = '제주 삼다수 2L x 6개';
UPDATE products SET manufacturer = '농심', origin = '대한민국' WHERE name = '신라면 5개입';
UPDATE products SET manufacturer = '국내 산지 농가', origin = '국내산 (경상북도)' WHERE name = '국내산 사과 2kg';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국 (쌀 : 국내산)' WHERE name = '즉석밥 210g x 12개';
UPDATE products SET manufacturer = '골드스타 PB', origin = '국내산' WHERE name = '1등급 우유 1L x 2개';
UPDATE products SET manufacturer = '국내 산지 농가', origin = '국내산' WHERE name = '특란 30구';
UPDATE products SET manufacturer = '국내 산지 농가', origin = '국내산 (제주)' WHERE name = '제주 감귤 3kg';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '3겹 화장지 30롤';
UPDATE products SET manufacturer = '라이온코리아 (비트)', origin = '대한민국' WHERE name = '액체 세탁세제 3L';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '대용량 물티슈 10팩';
UPDATE products SET manufacturer = '아모레퍼시픽 (해피바스)', origin = '대한민국' WHERE name = '해피바스 로즈 바디워시 500g 1개';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '미세모 칫솔 8개입';
UPDATE products SET manufacturer = '골드스타 PB', origin = '베트남' WHERE name = '항균 수건 5장';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '주방 수세미 10개';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '스테인리스 프라이팬 28cm';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '스테인리스 냄비 3종 세트';
UPDATE products SET manufacturer = '골드스타 PB', origin = '베트남' WHERE name = '원목 도마';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '밀폐용기 10종 세트';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '세라믹 머그컵 2개';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '무선 블루투스 이어폰';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '미니 선풍기';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '전기 주전자 1.7L';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '보조배터리 10000mAh';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '무선 마우스';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '블루투스 스피커';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '스마트 워치';
UPDATE products SET manufacturer = '골드스타 PB', origin = '베트남' WHERE name = '기능성 티셔츠';
UPDATE products SET manufacturer = '골드스타 PB', origin = '베트남' WHERE name = '데님 일자 청바지';
UPDATE products SET manufacturer = '골드스타 PB', origin = '베트남' WHERE name = '오버핏 맨투맨';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '경량 패딩 조끼';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '플리츠 롱스커트';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '수면 양말 5켤레';
UPDATE products SET manufacturer = '골드스타 PB', origin = '베트남' WHERE name = '러닝화';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '요가 매트 10mm';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '덤벨 5kg 2개';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '2인용 원터치 텐트';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '입문용 배드민턴 라켓 세트';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '보온 스포츠 물병 750ml';
UPDATE products SET manufacturer = '에스티글로벌 (일상공식)', origin = '중국' WHERE name = '노트북 백팩';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '면 볼캡';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '가죽 반지갑';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '3단 자동 우산';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '미러 선글라스';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '니트 목도리';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '수분 크림 50ml';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '촉촉 립스틱';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '촉촉 선크림 SPF50+';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '시트 마스크팩 10매';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '네일 컬러 5종 세트';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '플로럴 향수 50ml';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = 'LED 무드등';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '미니 화분 세트';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '암막 커튼 2장';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '극세사 러그 140x200';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '벽걸이 무소음 시계';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '리넨 쿠션 커버 2개';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '차량용 방향제';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '차량용 휴대폰 거치대';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '차량용 고속 충전기';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '무선 차량용 청소기';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '메모리폼 목쿠션';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '발수 코팅 와이퍼 2개';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '클래식 명곡 모음 CD';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '애니메이션 DVD 세트';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '재즈 베스트 앨범 CD';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '어린이 동요 CD';
UPDATE products SET manufacturer = 'HBO', origin = '미국' WHERE name = '[블루레이] 밴드 오브 브라더스 : 리패키지 (6disc)';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = 'LP 레코드 팝 명곡집';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '1000피스 직소 퍼즐';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '창작 블록 세트';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = 'RC 자동차';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '보드게임 패밀리 세트';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '곰돌이 봉제 인형';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '수채화 물감 24색 세트';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '젤펜 12색 세트';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = 'A4 복사용지 500매';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '스프링 노트 5권';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '형광펜 6색 세트';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '스테이플러 + 심 세트';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '2027 탁상 달력';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '강아지 사료 2kg';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '고양이 스크래쳐';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '고양이 모래 10L';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '강아지 산책 리드줄';
UPDATE products SET manufacturer = '골드스타 PB', origin = '중국' WHERE name = '반려동물 쿠션 방석';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '강아지 수제 간식';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '종합비타민 90정';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '단백질 쉐이크 1kg';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '오메가3 60캡슐';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '유산균 30포';
UPDATE products SET manufacturer = '골드스타 PB', origin = '국내산' WHERE name = '홍삼 스틱 30포';
UPDATE products SET manufacturer = '골드스타 PB', origin = '대한민국' WHERE name = '비타민C 1000mg 100정';

-- 추가 상품 : 가전디지털 스마트폰 (실제 브랜드 상품이라 실제 제조사를 넣는다)
-- 맨 끝에 따로 넣어야 앞 상품들의 번호(id)가 밀리지 않는다 (상품 사진이 상품번호.jpg 로 연결되기 때문)
INSERT INTO products (name, category, price, stock, description, icon, manufacturer, origin) VALUES
('삼성 갤럭시 S25 256GB', '가전디지털', 1155000, 10, '6.2인치 디스플레이와 고성능 카메라를 갖춘 삼성 갤럭시 스마트폰입니다.', '📱', '삼성전자', '베트남'),
('애플 아이폰 16 128GB', '가전디지털', 1250000, 10, '6.1인치 디스플레이와 카메라 컨트롤 버튼을 갖춘 애플 아이폰입니다.', '📱', 'Apple', '중국');

-- 추가 상품 : 식품 10개 (맨 끝에 넣어 앞 상품들의 번호(id)가 밀리지 않게 한다)
INSERT INTO products (name, category, price, stock, description, icon, manufacturer, origin) VALUES
('성주 참외 1.5kg', '식품', 17900, 25, '아삭하고 달콤한 성주 참외입니다. 4~6과가 들어 있습니다.', '🍈', '국내 산지 농가', '국내산 (경상북도 성주)'),
('고창 수박 7kg', '식품', 24900, 12, '당도 선별을 거친 큼직한 수박입니다.', '🍉', '국내 산지 농가', '국내산 (전라북도 고창)'),
('논산 딸기 500g', '식품', 12900, 20, '새콤달콤한 설향 딸기입니다. 받으신 날 바로 드시면 가장 맛있습니다.', '🍓', '국내 산지 농가', '국내산 (충청남도 논산)'),
('바나나 1.2kg', '식품', 4980, 60, '부드럽고 달콤한 필리핀산 바나나 한 송이입니다.', '🍌', '골드스타 PB', '필리핀'),
('한돈 삼겹살 500g', '식품', 13900, 30, '구이용으로 두툼하게 썬 국내산 냉장 삼겹살입니다.', '🥓', '골드스타 PB', '국내산'),
('1등급 한우 불고기 400g', '식품', 22900, 15, '양념하기 좋게 얇게 썬 1등급 한우 불고기용입니다.', '🥩', '골드스타 PB', '국내산 (한우)'),
('무항생제 닭가슴살 1kg', '식품', 11900, 40, '다이어트, 운동 식단에 좋은 냉동 닭가슴살입니다.', '🍗', '골드스타 PB', '국내산'),
('손질 고등어 3팩', '식품', 9900, 35, '뼈와 내장을 손질해 바로 구워 먹기 좋은 노르웨이산 고등어입니다.', '🐟', '골드스타 PB', '노르웨이'),
('국산 콩 두부 300g x 3개', '식품', 5400, 45, '국산 콩으로 만든 부드러운 찌개용 두부입니다.', '🧈', '골드스타 PB', '대한민국 (콩 : 국내산)'),
('포기김치 3kg', '식품', 19900, 20, '국내산 배추와 고춧가루로 담근 포기김치입니다.', '🥬', '골드스타 PB', '대한민국 (배추, 고춧가루 : 국내산)');

-- 추가 상품 : 가전디지털 10개 (맨 끝에 넣어 앞 상품들의 번호(id)가 밀리지 않게 한다)
INSERT INTO products (name, category, price, stock, description, icon, manufacturer, origin, fast_delivery) VALUES
('15.6인치 사무용 노트북', '가전디지털', 689000, 8, '인텔 프로세서, 16GB 메모리, 512GB SSD를 갖춘 가벼운 사무용 노트북입니다.', '💻', '골드스타 PB', '중국', 1),
('11인치 태블릿 128GB', '가전디지털', 329000, 12, '동영상 감상과 인터넷 강의에 좋은 11인치 태블릿입니다.', '📲', '골드스타 PB', '중국', 1),
('27인치 QHD 모니터', '가전디지털', 249000, 10, '선명한 QHD 해상도와 75Hz 화면의 27인치 모니터입니다.', '🖥️', '골드스타 PB', '중국', 1),
('무선 키보드 + 마우스 세트', '가전디지털', 32900, 35, '조용한 키감의 무선 키보드와 마우스 세트입니다.', '⌨️', '골드스타 PB', '중국', 1),
('노이즈캔슬링 헤드폰', '가전디지털', 159000, 15, '주변 소음을 줄여 주는 무선 노이즈캔슬링 헤드폰입니다. 최대 30시간 재생됩니다.', '🎧', '골드스타 PB', '중국', 1),
('미러리스 카메라 렌즈킷', '가전디지털', 899000, 5, '2,400만 화소 센서와 기본 줌 렌즈가 함께 들어 있는 미러리스 카메라입니다.', '📷', '골드스타 PB', '일본', 0),
('로봇 청소기', '가전디지털', 399000, 7, '물걸레 기능과 자동 충전 기능을 갖춘 로봇 청소기입니다.', '🤖', '골드스타 PB', '중국', 0),
('에어프라이어 5.5L', '가전디지털', 89900, 20, '기름 없이 바삭하게 조리하는 5.5L 대용량 에어프라이어입니다.', '🍟', '골드스타 PB', '중국', 1),
('공기청정기 (거실용)', '가전디지털', 219000, 9, '헤파 필터로 미세먼지를 걸러 주는 거실용 공기청정기입니다.', '🌬️', '골드스타 PB', '대한민국', 1),
('스마트 LED 전구 2개', '가전디지털', 19900, 50, '스마트폰 앱으로 밝기와 색을 바꿀 수 있는 LED 전구입니다.', '💡', '골드스타 PB', '중국', 1);

-- 추가 상품 : 문구/오피스 20개 (맨 끝에 넣어 앞 상품들의 번호(id)가 밀리지 않게 한다)
INSERT INTO products (name, category, price, stock, description, icon, manufacturer, origin, fast_delivery) VALUES
('4B 연필 12자루', '문구/오피스', 3900, 120, '스케치와 필기에 좋은 부드러운 4B 연필 12자루입니다.', '✏️', '골드스타 PB', '대한민국', 1),
('지우개 5개입', '문구/오피스', 2500, 150, '가루가 적게 나오고 잘 지워지는 지우개 5개입니다.', '🧽', '골드스타 PB', '대한민국', 1),
('샤프 0.5mm + 샤프심 세트', '문구/오피스', 5900, 80, '손에 잘 맞는 고무 그립 샤프와 0.5mm 샤프심 세트입니다.', '✏️', '골드스타 PB', '대한민국', 1),
('수성 사인펜 24색', '문구/오피스', 7900, 60, '번지지 않고 발색이 선명한 수성 사인펜 24색입니다.', '🖌️', '골드스타 PB', '대한민국', 1),
('유성 매직 마커 10개', '문구/오피스', 6500, 70, '종이, 비닐, 상자에 모두 잘 써지는 유성 매직 10개입니다.', '🖍️', '골드스타 PB', '대한민국', 1),
('화이트보드 마커 4색', '문구/오피스', 4900, 75, '잘 지워지는 화이트보드 전용 마커 4색 세트입니다.', '🖊️', '골드스타 PB', '대한민국', 1),
('포스트잇 메모지 12패드', '문구/오피스', 8900, 90, '붙였다 뗐다 할 수 있는 정사각형 메모지 12패드입니다.', '🗒️', '골드스타 PB', '대한민국', 1),
('3단 파일 서랍 정리함', '문구/오피스', 15900, 25, 'A4 서류를 종류별로 정리할 수 있는 3단 서랍입니다.', '🗄️', '골드스타 PB', '중국', 1),
('클리어 파일 20매', '문구/오피스', 4500, 100, '서류가 구겨지지 않게 보관하는 투명 A4 클리어 파일 20매입니다.', '📁', '골드스타 PB', '대한민국', 1),
('2공 바인더 A4 3권', '문구/오피스', 9900, 40, '구멍 뚫은 서류를 묶어 보관하는 A4 2공 바인더 3권입니다.', '📒', '골드스타 PB', '대한민국', 1),
('투명 테이프 + 커터 세트', '문구/오피스', 5500, 85, '잘 끊어지는 테이프 커터와 투명 테이프 3개 세트입니다.', '🩹', '골드스타 PB', '대한민국', 1),
('사무용 가위 2개', '문구/오피스', 4900, 65, '손잡이가 편한 스테인리스 사무용 가위 2개입니다.', '✂️', '골드스타 PB', '중국', 1),
('강력 딱풀 5개입', '문구/오피스', 3900, 110, '종이가 잘 붙고 손에 묻지 않는 고체 풀 5개입니다.', '🧴', '골드스타 PB', '대한민국', 1),
('30cm 자 + 각도기 세트', '문구/오피스', 2900, 95, '투명 30cm 자, 삼각자, 각도기가 들어 있는 세트입니다.', '📏', '골드스타 PB', '대한민국', 1),
('공학용 계산기', '문구/오피스', 19900, 30, '삼각함수, 통계 계산이 되는 학생용 공학 계산기입니다.', '🧮', '골드스타 PB', '중국', 1),
('2027 다이어리 (만년형)', '문구/오피스', 12900, 45, '날짜를 직접 적어 언제 시작해도 되는 만년형 다이어리입니다.', '📔', '골드스타 PB', '대한민국', 1),
('전동 연필깎이', '문구/오피스', 14900, 20, '연필을 넣기만 하면 깎아 주는 건전지식 전동 연필깎이입니다.', '✏️', '골드스타 PB', '중국', 1),
('라벨 스티커 A4 10매', '문구/오피스', 6900, 55, '이름표, 주소 라벨로 쓰기 좋은 A4 라벨 스티커 10매입니다.', '🏷️', '골드스타 PB', '대한민국', 1),
('편지 봉투 50매', '문구/오피스', 3500, 80, '서류, 편지를 넣기 좋은 흰색 규격 봉투 50매입니다.', '✉️', '골드스타 PB', '대한민국', 1),
('세단기 (문서 파쇄기)', '문구/오피스', 59900, 6, '개인정보가 담긴 서류와 카드를 잘게 잘라 주는 문서 파쇄기입니다.', '🗑️', '골드스타 PB', '중국', 0);

-- 로켓 배송 : 처음에는 모든 상품을 로켓 배송으로 해 둔다 (관리자가 상품관리 화면에서 상품마다 켜고 끈다)
UPDATE products SET fast_delivery = 1;
-- 예외 : 고가/대형 상품은 로켓 배송을 하지 않는다
UPDATE products SET fast_delivery = 0 WHERE name IN ('미러리스 카메라 렌즈킷', '로봇 청소기', '세단기 (문서 파쇄기)');

-- ===== 상품 옵션 : 노트북 백팩 (크기 x 색상 4가지, 가격은 모두 같다) =====
-- 옵션 상품은 옵션마다 재고를 따로 세고, 상품 재고는 옵션 재고의 합계로 맞춘다
-- product_options 테이블은 서버를 한 번 실행하면 만들어진다
INSERT INTO product_options (product_id, name, stock)
SELECT id, '15인치 / 화이트', 5 FROM products WHERE name = '노트북 백팩';
INSERT INTO product_options (product_id, name, stock)
SELECT id, '15인치 / 블랙', 7 FROM products WHERE name = '노트북 백팩';
INSERT INTO product_options (product_id, name, stock)
SELECT id, '17인치 / 화이트', 0 FROM products WHERE name = '노트북 백팩';
INSERT INTO product_options (product_id, name, stock)
SELECT id, '17인치 / 블랙', 4 FROM products WHERE name = '노트북 백팩';
-- 상품 재고 = 옵션 재고 합계 (5 + 7 + 0 + 4 = 16)
UPDATE products SET stock = (SELECT SUM(o.stock) FROM product_options o WHERE o.product_id = products.id)
WHERE name = '노트북 백팩';

-- 확인
SELECT * FROM products;
SELECT * FROM product_options;
