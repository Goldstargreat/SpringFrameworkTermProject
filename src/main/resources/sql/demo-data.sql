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

-- ===== 주문 12건 : 배송 상태가 골고루 보이도록 =====
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

-- [입고] kim02 : 보조배터리 10000mAh x2
INSERT INTO orders (member_id, receiver_name, receiver_phone, shipping_address, status, total_price, ordered_at) VALUES
((SELECT id FROM members WHERE login_id = 'kim02'), '김영희', '010-3456-7890', '경기도 성남시 분당구 판교역로 235', 'WAREHOUSED', 39800, '2026-11-23 17:02:00.000000');
SET @order_id = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, order_price, quantity) VALUES
(@order_id, (SELECT id FROM products WHERE name = '보조배터리 10000mAh'), 19900, 2);

-- [주문접수] lee03 : 오버핏 맨투맨 x2, 요가 매트 10mm x1
INSERT INTO orders (member_id, receiver_name, receiver_phone, shipping_address, status, total_price, ordered_at) VALUES
((SELECT id FROM members WHERE login_id = 'lee03'), '이민수', '010-4567-8901', '부산광역시 해운대구 해운대로 570', 'ORDER_RECEIVED', 71700, '2026-11-24 08:15:00.000000');
SET @order_id = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, order_price, quantity) VALUES
(@order_id, (SELECT id FROM products WHERE name = '오버핏 맨투맨'), 24900, 2),
(@order_id, (SELECT id FROM products WHERE name = '요가 매트 10mm'), 21900, 1);

-- [주문접수] hong07 : 특란 30구 x2, 스테인리스 프라이팬 28cm x1
INSERT INTO orders (member_id, receiver_name, receiver_phone, shipping_address, status, total_price, ordered_at) VALUES
((SELECT id FROM members WHERE login_id = 'hong07'), '홍길동', '010-7890-1234', '인천광역시 연수구 컨벤시아대로 165', 'ORDER_RECEIVED', 50700, '2026-11-24 09:44:00.000000');
SET @order_id = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, order_price, quantity) VALUES
(@order_id, (SELECT id FROM products WHERE name = '특란 30구'), 8900, 2),
(@order_id, (SELECT id FROM products WHERE name = '스테인리스 프라이팬 28cm'), 32900, 1);

-- [주문취소] park04 : LED 무드등 x1
INSERT INTO orders (member_id, receiver_name, receiver_phone, shipping_address, status, total_price, ordered_at) VALUES
((SELECT id FROM members WHERE login_id = 'park04'), '박지은', '02-345-6789', '서울특별시 마포구 월드컵북로 396', 'CANCELED', 19900, '2026-11-23 20:36:00.000000');
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

-- ===== 재고 차감 : 앱에서 주문할 때처럼, 취소되지 않은 주문의 수량만큼 재고를 줄인다 =====
UPDATE products p
JOIN (SELECT oi.product_id, SUM(oi.quantity) AS qty
      FROM order_items oi
      JOIN orders o ON o.id = oi.order_id
      JOIN members m ON m.id = o.member_id
      WHERE m.login_id IN ('hong01','kim02','lee03','park04','choi05','jung06','hong07') AND o.status <> 'CANCELED'
      GROUP BY oi.product_id) d ON d.product_id = p.id
SET p.stock = p.stock - d.qty;

-- 확인
SELECT id, login_id, name, phone FROM members WHERE login_id IN ('hong01','kim02','lee03','park04','choi05','jung06','hong07');
SELECT o.id, m.login_id, o.status, o.total_price FROM orders o JOIN members m ON m.id = o.member_id WHERE m.login_id IN ('hong01','kim02','lee03','park04','choi05','jung06','hong07');
