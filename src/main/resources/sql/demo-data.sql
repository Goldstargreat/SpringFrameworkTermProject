-- 골드스타몰 시연용 회원/주문 데이터
-- 사용법 : products.sql 로 상품을 넣은 뒤, MySQL 쿼리 콘솔에서 이 파일 전체를 한 번만 실행한다
--          (두 번 실행하면 아이디/전화번호 중복으로 회원 INSERT 가 실패한다)
-- 회원 비밀번호는 모두 demo1234 (BCrypt 로 암호화된 값을 그대로 넣는다)
-- 상품은 번호 대신 이름으로 찾는다 : DB 마다 상품 번호가 다를 수 있기 때문
-- 주문 상품(order_items)은 바로 앞에서 넣은 주문의 번호(LAST_INSERT_ID())로 연결한다

USE goldstarmall;

-- ===== 회원 7명 (hong01 과 hong07 은 동명이인 홍길동) =====
INSERT INTO members (login_id, password, name, phone, address, second_address, role, created_at) VALUES
('hong01', '$2a$10$UOJ4qRxLQJfWnUN1J9hKWey2X9yINOCa06cK0F9SsB5KIWwT5p9VS', '홍길동', '010-2345-6789', '서울특별시 강남구 테헤란로 123', '서울특별시 중구 세종대로 110 (회사)', 'USER', '2026-10-02 14:12:00.000000'),
('kim02', '$2a$10$FWDe7EqJYBFDC/vCPTI5qua8/PhKhHnm67uK1ZKOtnuooPsX8S87W', '김영희', '010-3456-7890', '경기도 성남시 분당구 판교역로 235', NULL, 'USER', '2026-10-08 09:41:00.000000'),
('lee03', '$2a$10$6dAicL65XKXh0GSOuA/qmemAxecORe9ewbsPG562n7IldXaEuaj/y', '이민수', '010-4567-8901', '부산광역시 해운대구 해운대로 570', NULL, 'USER', '2026-10-15 20:03:00.000000'),
('park04', '$2a$10$n/Q.oKVxyFBag2RsfDB3x./4aNT9kOUgeX/9PqXTRvZtBtnctuLtq', '박지은', '02-345-6789', '서울특별시 마포구 월드컵북로 396', '경기도 고양시 일산동구 중앙로 1275 (본가)', 'USER', '2026-10-21 11:27:00.000000'),
('choi05', '$2a$10$/8.DUJL193dwc2LWRMiSsOD7pOV/jlCLrt/CJASE00jRBN995A5o6', '최현우', '010-5678-9012', '대전광역시 유성구 대학로 99', NULL, 'USER', '2026-10-30 18:55:00.000000'),
('jung06', '$2a$10$SyL3NVjJP9ZELrDtOIUXLOcqOo7qailnRWonOq6KL8uOwckjDNgL2', '정다은', '010-6789-0123', '광주광역시 북구 용봉로 77', NULL, 'USER', '2026-11-06 13:08:00.000000'),
('hong07', '$2a$10$glJIZHLiKi4hfjUgCZ8CgepOr2NSeRLTh17hLxE.RpN.wW82a4yFi', '홍길동', '010-7890-1234', '인천광역시 연수구 컨벤시아대로 165', NULL, 'USER', '2026-11-17 22:30:00.000000');

-- ===== 주문 17건 : 배송 상태가 골고루 보이도록 (주문취소, 주문거절 포함) =====
-- [배송완료] hong01 : 신라면 5개입 x3, 제주 삼다수 2L x 6개 x1
INSERT INTO orders (member_id, receiver_name, receiver_phone, shipping_address, status, total_price, ordered_at) VALUES
((SELECT id FROM members WHERE login_id = 'hong01'), '홍길동', '010-2345-6789', '서울특별시 강남구 테헤란로 123', 'DELIVERED', 18780, '2026-11-12 10:21:00.000000');
SET @order_id = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, order_price, quantity) VALUES
(@order_id, (SELECT id FROM products WHERE name = '신라면 5개입'), 4100, 3),
(@order_id, (SELECT id FROM products WHERE name = '제주 삼다수 2L x 6개'), 6480, 1);

-- [배송완료] kim02 : 수분 크림 50ml x1, 시트 마스크팩 10매 x2
INSERT INTO orders (member_id, receiver_name, receiver_phone, shipping_address, status, total_price, ordered_at) VALUES
((SELECT id FROM members WHERE login_id = 'kim02'), '김영희', '010-3456-7890', '경기도 성남시 분당구 판교역로 235', 'DELIVERED', 44700, '2026-11-13 21:47:00.000000');
SET @order_id = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, order_price, quantity) VALUES
(@order_id, (SELECT id FROM products WHERE name = '수분 크림 50ml'), 18900, 1),
(@order_id, (SELECT id FROM products WHERE name = '시트 마스크팩 10매'), 12900, 2);

-- [배송완료] lee03 : 러닝화 x1
INSERT INTO orders (member_id, receiver_name, receiver_phone, shipping_address, status, total_price, ordered_at) VALUES
((SELECT id FROM members WHERE login_id = 'lee03'), '이민수', '010-4567-8901', '부산광역시 해운대구 해운대로 570', 'DELIVERED', 49900, '2026-11-15 08:30:00.000000');
SET @order_id = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, order_price, quantity) VALUES
(@order_id, (SELECT id FROM products WHERE name = '러닝화'), 49900, 1);

-- [배송완료] kim02 : 2인용 원터치 텐트 x2, 스테인리스 냄비 3종 세트 x1, 플로럴 향수 50ml x2
-- → 누적 구매액 326,200원 (30만 원 이상) 이라 골드 등급이 된다. 주문할 때는 일반 등급이라 할인 없음
INSERT INTO orders (member_id, receiver_name, receiver_phone, shipping_address, status, total_price, ordered_at) VALUES
((SELECT id FROM members WHERE login_id = 'kim02'), '김영희', '010-3456-7890', '경기도 성남시 분당구 판교역로 235', 'DELIVERED', 281500, '2026-11-16 15:40:00.000000');
SET @order_id = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, order_price, quantity) VALUES
(@order_id, (SELECT id FROM products WHERE name = '2인용 원터치 텐트'), 69900, 2),
(@order_id, (SELECT id FROM products WHERE name = '스테인리스 냄비 3종 세트'), 49900, 1),
(@order_id, (SELECT id FROM products WHERE name = '플로럴 향수 50ml'), 45900, 2);

-- [배송완료] lee03 : 블루투스 스피커 x1, RC 자동차 x1
-- → 누적 구매액 141,700원 (10만 원 이상) 이라 실버 등급이 된다. 주문할 때는 일반 등급이라 할인 없음
INSERT INTO orders (member_id, receiver_name, receiver_phone, shipping_address, status, total_price, ordered_at) VALUES
((SELECT id FROM members WHERE login_id = 'lee03'), '이민수', '010-4567-8901', '부산광역시 해운대구 해운대로 570', 'DELIVERED', 91800, '2026-11-17 21:05:00.000000');
SET @order_id = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, order_price, quantity) VALUES
(@order_id, (SELECT id FROM products WHERE name = '블루투스 스피커'), 45900, 1),
(@order_id, (SELECT id FROM products WHERE name = 'RC 자동차'), 45900, 1);

-- [배송중] park04 : 1000피스 직소 퍼즐 x1, 보드게임 패밀리 세트 x1
INSERT INTO orders (member_id, receiver_name, receiver_phone, shipping_address, status, total_price, ordered_at) VALUES
((SELECT id FROM members WHERE login_id = 'park04'), '박지은', '02-345-6789', '경기도 고양시 일산동구 중앙로 1275 (본가)', 'IN_DELIVERY', 47800, '2026-11-20 19:12:00.000000');
SET @order_id = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, order_price, quantity) VALUES
(@order_id, (SELECT id FROM products WHERE name = '1000피스 직소 퍼즐'), 17900, 1),
(@order_id, (SELECT id FROM products WHERE name = '보드게임 패밀리 세트'), 29900, 1);

-- [배송중] choi05 : 종합비타민 90정 x1, 오메가3 60캡슐 x2
INSERT INTO orders (member_id, receiver_name, receiver_phone, shipping_address, status, total_price, ordered_at) VALUES
((SELECT id FROM members WHERE login_id = 'choi05'), '최현우', '010-5678-9012', '대전광역시 유성구 대학로 99', 'IN_DELIVERY', 69700, '2026-11-21 12:05:00.000000');
SET @order_id = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, order_price, quantity) VALUES
(@order_id, (SELECT id FROM products WHERE name = '종합비타민 90정'), 19900, 1),
(@order_id, (SELECT id FROM products WHERE name = '오메가3 60캡슐'), 24900, 2);

-- [출고] hong01 : 무선 블루투스 이어폰 x1
INSERT INTO orders (member_id, receiver_name, receiver_phone, shipping_address, status, total_price, ordered_at) VALUES
((SELECT id FROM members WHERE login_id = 'hong01'), '홍길동', '010-2345-6789', '서울특별시 중구 세종대로 110 (회사)', 'SHIPPED', 39900, '2026-11-22 15:40:00.000000');
SET @order_id = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, order_price, quantity) VALUES
(@order_id, (SELECT id FROM products WHERE name = '무선 블루투스 이어폰'), 39900, 1);

-- [출고] jung06 : 강아지 사료 2kg x1, 강아지 수제 간식 x1
INSERT INTO orders (member_id, receiver_name, receiver_phone, shipping_address, status, total_price, ordered_at) VALUES
((SELECT id FROM members WHERE login_id = 'jung06'), '정다은', '010-6789-0123', '광주광역시 북구 용봉로 77', 'SHIPPED', 33800, '2026-11-22 23:18:00.000000');
SET @order_id = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, order_price, quantity) VALUES
(@order_id, (SELECT id FROM products WHERE name = '강아지 사료 2kg'), 24900, 1),
(@order_id, (SELECT id FROM products WHERE name = '강아지 수제 간식'), 8900, 1);

-- [입고] kim02 : 보조배터리 10000mAh x2 → 골드가 된 뒤의 주문이라 골드 할인(5%) 1,990원 (39,800 → 37,810원)
INSERT INTO orders (member_id, receiver_name, receiver_phone, shipping_address, status, total_price, ordered_at, discount_amount, order_grade) VALUES
((SELECT id FROM members WHERE login_id = 'kim02'), '김영희', '010-3456-7890', '경기도 성남시 분당구 판교역로 235', 'WAREHOUSED', 37810, '2026-11-23 17:02:00.000000', 1990, 'GOLD');
SET @order_id = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, order_price, quantity) VALUES
(@order_id, (SELECT id FROM products WHERE name = '보조배터리 10000mAh'), 19900, 2);

-- [주문접수] lee03 : 오버핏 맨투맨 x2, 요가 매트 10mm x1 → 실버가 된 뒤의 주문이라 실버 할인(3%) 2,151원 (71,700 → 69,549원)
INSERT INTO orders (member_id, receiver_name, receiver_phone, shipping_address, status, total_price, ordered_at, discount_amount, order_grade) VALUES
((SELECT id FROM members WHERE login_id = 'lee03'), '이민수', '010-4567-8901', '부산광역시 해운대구 해운대로 570', 'ORDER_RECEIVED', 69549, '2026-11-24 08:15:00.000000', 2151, 'SILVER');
SET @order_id = LAST_INSERT_ID();
-- 맨투맨은 옵션 상품이라 옵션(블랙 / L)도 함께 넣는다
INSERT INTO order_items (order_id, product_id, option_id, order_price, quantity) VALUES
(@order_id, (SELECT id FROM products WHERE name = '오버핏 맨투맨'),
 (SELECT o.id FROM product_options o JOIN products p ON p.id = o.product_id WHERE p.name = '오버핏 맨투맨' AND o.name = '블랙 / L'), 24900, 2),
(@order_id, (SELECT id FROM products WHERE name = '요가 매트 10mm'), NULL, 21900, 1);

-- [주문접수] hong07 : 특란 30구 x2, 스테인리스 프라이팬 28cm x1
INSERT INTO orders (member_id, receiver_name, receiver_phone, shipping_address, status, total_price, ordered_at) VALUES
((SELECT id FROM members WHERE login_id = 'hong07'), '홍길동', '010-7890-1234', '인천광역시 연수구 컨벤시아대로 165', 'ORDER_RECEIVED', 50700, '2026-11-24 09:44:00.000000');
SET @order_id = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, order_price, quantity) VALUES
(@order_id, (SELECT id FROM products WHERE name = '특란 30구'), 8900, 2),
(@order_id, (SELECT id FROM products WHERE name = '스테인리스 프라이팬 28cm'), 32900, 1);

-- [주문취소] park04 : LED 무드등 x1
-- 손님이 직접 취소한 주문이라 결제 금액 전체가 환불된 기록이 남는다
INSERT INTO orders (member_id, receiver_name, receiver_phone, shipping_address, status, total_price, ordered_at, refund_amount, refunded_at) VALUES
((SELECT id FROM members WHERE login_id = 'park04'), '박지은', '02-345-6789', '서울특별시 마포구 월드컵북로 396', 'CANCELED', 19900, '2026-11-23 20:36:00.000000', 19900, '2026-11-23 20:41:00.000000');
SET @order_id = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, order_price, quantity) VALUES
(@order_id, (SELECT id FROM products WHERE name = 'LED 무드등'), 19900, 1);

-- [주문접수] jung06 : 젤펜 12색 세트 x2, A4 복사용지 500매 x3
INSERT INTO orders (member_id, receiver_name, receiver_phone, shipping_address, status, total_price, ordered_at) VALUES
((SELECT id FROM members WHERE login_id = 'jung06'), '정다은', '010-6789-0123', '광주광역시 북구 용봉로 77', 'ORDER_RECEIVED', 31500, '2026-11-24 10:05:00.000000');
SET @order_id = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, order_price, quantity) VALUES
(@order_id, (SELECT id FROM products WHERE name = '젤펜 12색 세트'), 6900, 2),
(@order_id, (SELECT id FROM products WHERE name = 'A4 복사용지 500매'), 5900, 3);

-- [주문거절] choi05 : 스마트 워치 x2 → 관리자가 재고 부족으로 거절하고 전액 환불
INSERT INTO orders (member_id, receiver_name, receiver_phone, shipping_address, status, total_price, ordered_at, refund_amount, refunded_at, reject_reason) VALUES
((SELECT id FROM members WHERE login_id = 'choi05'), '최현우', '010-5678-9012', '대전광역시 유성구 대학로 99', 'REJECTED', 258000, '2026-11-23 16:20:00.000000', 258000, '2026-11-23 17:05:00.000000', '창고 재고 확인 결과 2개를 보내 드릴 수 없어 주문을 거절합니다. 죄송합니다.');
SET @order_id = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, order_price, quantity) VALUES
(@order_id, (SELECT id FROM products WHERE name = '스마트 워치'), 129000, 2);

-- [배송완료] hong01 : 애플 아이폰 16 128GB x1 → 누적 구매액이 100만 원을 넘어 VIP 등급이 된다 (서버를 켤 때 자동 계산)
INSERT INTO orders (member_id, receiver_name, receiver_phone, shipping_address, status, total_price, ordered_at) VALUES
((SELECT id FROM members WHERE login_id = 'hong01'), '홍길동', '010-2345-6789', '서울특별시 중구 세종대로 110 (회사)', 'DELIVERED', 1250000, '2026-11-14 13:02:00.000000');
SET @order_id = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, order_price, quantity) VALUES
(@order_id, (SELECT id FROM products WHERE name = '애플 아이폰 16 128GB'), 1250000, 1);

-- [주문접수] hong01 : 무선 블루투스 이어폰 x1 → VIP 가 된 뒤의 주문이라 VIP 할인(10%) 3,990원을 받았다
INSERT INTO orders (member_id, receiver_name, receiver_phone, shipping_address, status, total_price, ordered_at, discount_amount, order_grade) VALUES
((SELECT id FROM members WHERE login_id = 'hong01'), '홍길동', '010-2345-6789', '서울특별시 강남구 테헤란로 123', 'ORDER_RECEIVED', 35910, '2026-11-24 11:20:00.000000', 3990, 'VIP');
SET @order_id = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, order_price, quantity) VALUES
(@order_id, (SELECT id FROM products WHERE name = '무선 블루투스 이어폰'), 39900, 1);

-- ===== 회원 등급 : SQL 로 넣지 않는다. 서버를 켤 때 누적 구매액(배송완료 주문)으로 자동으로 정해진다 =====

-- ===== 재고 차감 : 앱에서 주문할 때처럼, 취소/거절되지 않은 주문의 수량만큼 재고를 줄인다 =====
UPDATE products p
JOIN (SELECT oi.product_id, SUM(oi.quantity) AS qty
      FROM order_items oi
      JOIN orders o ON o.id = oi.order_id
      JOIN members m ON m.id = o.member_id
      WHERE m.login_id IN ('hong01','kim02','lee03','park04','choi05','jung06','hong07') AND o.status NOT IN ('CANCELED', 'REJECTED')
      GROUP BY oi.product_id) d ON d.product_id = p.id
SET p.stock = p.stock - d.qty;
-- 옵션 상품은 옵션 재고도 함께 줄인다 (상품 재고 = 옵션 재고 합계를 유지)
UPDATE product_options po
JOIN (SELECT oi.option_id, SUM(oi.quantity) AS qty
      FROM order_items oi
      JOIN orders o ON o.id = oi.order_id
      JOIN members m ON m.id = o.member_id
      WHERE oi.option_id IS NOT NULL
        AND m.login_id IN ('hong01','kim02','lee03','park04','choi05','jung06','hong07') AND o.status NOT IN ('CANCELED', 'REJECTED')
      GROUP BY oi.option_id) d ON d.option_id = po.id
SET po.stock = po.stock - d.qty;

-- ===== 상품평 : 배송완료된 주문의 상품에만 쓸 수 있으므로, 배송완료 주문(hong01, kim02, lee03)의 상품에 단다 =====
-- (reviews, inquiries 테이블은 앱을 한 번 실행하면 JPA 가 만들어 준다)
INSERT INTO reviews (member_id, product_id, rating, content, created_at) VALUES
((SELECT id FROM members WHERE login_id = 'hong01'), (SELECT id FROM products WHERE name = '신라면 5개입'), 5,
 '국물이 얼큰하고 면발이 쫄깃해요. 역시 라면은 신라면! 재구매 의사 있습니다.', '2026-11-21 19:12:00.000000'),
((SELECT id FROM members WHERE login_id = 'hong01'), (SELECT id FROM products WHERE name = '제주 삼다수 2L x 6개'), 4,
 '배송이 빨랐고 포장도 튼튼했습니다. 무거운 물을 집 앞까지 받아서 편해요.', '2026-11-21 19:15:00.000000'),
((SELECT id FROM members WHERE login_id = 'kim02'), (SELECT id FROM products WHERE name = '수분 크림 50ml'), 5,
 '건성 피부인데 하루 종일 촉촉해요. 향도 은은해서 좋습니다.', '2026-11-22 22:40:00.000000'),
((SELECT id FROM members WHERE login_id = 'kim02'), (SELECT id FROM products WHERE name = '시트 마스크팩 10매'), 3,
 '무난해요. 에센스 양은 넉넉한데 시트가 조금 얇은 편입니다.', '2026-11-22 22:45:00.000000'),
((SELECT id FROM members WHERE login_id = 'lee03'), (SELECT id FROM products WHERE name = '러닝화'), 4,
 '쿠션감이 좋아서 오래 뛰어도 발이 편합니다. 평소 신는 사이즈로 주문하면 딱 맞아요.', '2026-11-23 08:30:00.000000');

-- ===== 관리자 코멘트 : 상품평 2개에 관리자가 코멘트를 단다 =====
UPDATE reviews SET admin_comment = '소중한 상품평 감사합니다! 앞으로도 신선한 상품으로 찾아뵙겠습니다.', commented_at = '2026-11-22 09:00:00.000000'
WHERE member_id = (SELECT id FROM members WHERE login_id = 'hong01') AND product_id = (SELECT id FROM products WHERE name = '신라면 5개입');
UPDATE reviews SET admin_comment = '시트가 얇게 느껴지셨군요. 의견 전달하여 더 좋은 상품으로 준비하겠습니다.', commented_at = '2026-11-23 10:10:00.000000'
WHERE member_id = (SELECT id FROM members WHERE login_id = 'kim02') AND product_id = (SELECT id FROM products WHERE name = '시트 마스크팩 10매');

-- ===== 상품문의 : 답변완료 2개, 답변대기 2개 =====
INSERT INTO inquiries (member_id, product_id, question, answer, created_at, answered_at) VALUES
((SELECT id FROM members WHERE login_id = 'kim02'), (SELECT id FROM products WHERE name = '신라면 5개입'),
 '유통기한이 언제까지인가요?', '제조일로부터 6개월이며, 출고 기준 4개월 이상 남은 상품을 보내 드립니다. 감사합니다.',
 '2026-11-22 10:20:00.000000', '2026-11-22 14:05:00.000000'),
((SELECT id FROM members WHERE login_id = 'park04'), (SELECT id FROM products WHERE name = '신라면 5개입'),
 '한 번에 여러 묶음을 주문하면 박스로 포장되나요?', NULL,
 '2026-11-24 09:10:00.000000', NULL),
((SELECT id FROM members WHERE login_id = 'choi05'), (SELECT id FROM products WHERE name = '러닝화'),
 '발볼이 넓은 편인데 한 치수 크게 주문해야 할까요?', '발볼이 넓으시면 5mm 크게 주문하시는 것을 권해 드립니다. 받은 날로부터 7일 이내 교환 가능합니다.',
 '2026-11-23 11:00:00.000000', '2026-11-23 15:30:00.000000'),
((SELECT id FROM members WHERE login_id = 'hong07'), (SELECT id FROM products WHERE name = '수분 크림 50ml'),
 '민감성 피부도 사용할 수 있나요?', NULL,
 '2026-11-24 08:45:00.000000', NULL);

-- ===== 주문번호 : 앱에서 주문할 때처럼 "주문날짜-id 4자리" 로 붙인다 (예: 20261124-0012) =====
-- (이 줄을 빼먹어도 앱을 다시 켜면 비어 있는 주문번호가 자동으로 채워진다)
UPDATE orders SET order_number = CONCAT(DATE_FORMAT(ordered_at, '%Y%m%d'), '-', LPAD(id, 4, '0'))
WHERE order_number IS NULL;

-- 확인
SELECT id, login_id, name, phone FROM members WHERE login_id IN ('hong01','kim02','lee03','park04','choi05','jung06','hong07');
SELECT o.id, m.login_id, o.status, o.total_price FROM orders o JOIN members m ON m.id = o.member_id WHERE m.login_id IN ('hong01','kim02','lee03','park04','choi05','jung06','hong07');
SELECT r.id, m.login_id, p.name, r.rating FROM reviews r JOIN members m ON m.id = r.member_id JOIN products p ON p.id = r.product_id;
SELECT q.id, m.login_id, p.name, q.answer IS NOT NULL AS answered FROM inquiries q JOIN members m ON m.id = q.member_id JOIN products p ON p.id = q.product_id;
