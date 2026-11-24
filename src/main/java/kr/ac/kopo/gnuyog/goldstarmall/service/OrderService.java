package kr.ac.kopo.gnuyog.goldstarmall.service;

import kr.ac.kopo.gnuyog.goldstarmall.domain.CartItem;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Order;
import kr.ac.kopo.gnuyog.goldstarmall.domain.OrderItem;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import kr.ac.kopo.gnuyog.goldstarmall.domain.ShippingStatus;
import kr.ac.kopo.gnuyog.goldstarmall.exception.BusinessException;
import kr.ac.kopo.gnuyog.goldstarmall.exception.NotFoundException;
import kr.ac.kopo.gnuyog.goldstarmall.repository.CartItemRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.MemberRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.OrderRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// 주문 서비스 : 주문 생성, 조회, 취소, 배송 상태 변경
@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        CartItemRepository cartItemRepository,
                        MemberRepository memberRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
    }

    // 주문 생성 : 장바구니에 담긴 상품 전체를 주문한다
    // @Transactional 덕분에 중간에 오류(재고 부족 등)가 나면 앞에서 한 작업이 모두 취소(rollback)된다
    public Order createOrder(Long memberId, String receiverName, String receiverPhone, String shippingAddress) {
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            throw new NotFoundException("회원 정보를 찾을 수 없습니다.");
        }

        // 1) 장바구니 조회
        List<CartItem> cartItems = cartItemRepository.findByMember(member);
        if (cartItems.isEmpty()) {
            throw new BusinessException("장바구니가 비어 있습니다.");
        }

        // 2) 주문 객체 만들기
        Order order = newOrder(member, receiverName, receiverPhone, shippingAddress);

        // 3) 장바구니 항목을 하나씩 주문 상품으로 옮기면서 재고를 줄인다
        int totalPrice = 0;
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            // 재고 확인
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BusinessException(product.getName() + " 상품의 재고가 부족합니다.");
            }
            // 재고 차감 (변경 감지로 update SQL 실행)
            product.setStock(product.getStock() - cartItem.getQuantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setOrderPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            order.addOrderItem(orderItem);

            totalPrice = totalPrice + orderItem.getTotalPrice();
        }
        order.setTotalPrice(totalPrice);

        // 4) 주문 저장 (cascade 설정으로 주문 상품들도 함께 저장된다)
        //    저장해야 id 가 생기므로, 저장한 뒤 그 id 로 기본 주문번호를 붙인다
        orderRepository.save(order);
        assignDefaultOrderNumber(order);

        // 5) 주문이 끝났으니 장바구니 비우기
        cartItemRepository.deleteAll(cartItems);

        return order;
    }

    // 바로 주문 : 상품 상세 화면에서 상품 1개만 바로 주문한다
    // 장바구니를 거치지 않으므로, 장바구니에 담아 둔 다른 상품은 그대로 남는다
    public Order createDirectOrder(Long memberId, Long productId, int quantity,
                                   String receiverName, String receiverPhone, String shippingAddress) {
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            throw new NotFoundException("회원 정보를 찾을 수 없습니다.");
        }
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            throw new NotFoundException("상품을 찾을 수 없습니다.");
        }

        // 1) 수량, 재고 확인
        checkDirectOrderQuantity(product, quantity);

        // 2) 재고 차감 (변경 감지로 update SQL 실행)
        product.setStock(product.getStock() - quantity);

        // 3) 주문 + 주문 상품 1개 만들기
        Order order = newOrder(member, receiverName, receiverPhone, shippingAddress);
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setOrderPrice(product.getPrice());
        orderItem.setQuantity(quantity);
        order.addOrderItem(orderItem);
        order.setTotalPrice(orderItem.getTotalPrice());

        // 4) 저장 후 기본 주문번호 붙이기
        orderRepository.save(order);
        assignDefaultOrderNumber(order);
        return order;
    }

    // 바로 주문 수량 검사 : 1개 이상, 남은 재고 이하 (주문서 화면을 보여 줄 때도 쓴다)
    public void checkDirectOrderQuantity(Product product, int quantity) {
        if (product.getStock() == 0) {
            throw new BusinessException("일시 품절된 상품입니다.");
        }
        if (quantity < 1) {
            throw new BusinessException("수량은 1개 이상이어야 합니다.");
        }
        if (quantity > product.getStock()) {
            throw new BusinessException(product.getName() + " : " + product.getStockShortageMessage());
        }
    }

    // 주문 객체 만들기 : 장바구니 주문과 바로 주문이 함께 쓴다
    private Order newOrder(Member member, String receiverName, String receiverPhone, String shippingAddress) {
        Order order = new Order();
        order.setMember(member);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setShippingAddress(shippingAddress);
        order.setStatus(ShippingStatus.ORDER_RECEIVED);
        order.setOrderedAt(LocalDateTime.now());
        return order;
    }

    // 내 주문 목록 (최신순)
    @Transactional(readOnly = true)
    public List<Order> findMyOrders(Long memberId) {
        Member member = memberRepository.findById(memberId).orElse(null);
        return orderRepository.findByMemberOrderByIdDesc(member);
    }

    // 내 주문 1건 조회 (본인 주문이 아니면 오류)
    @Transactional(readOnly = true)
    public Order findMyOrder(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new NotFoundException("주문을 찾을 수 없습니다.");
        }
        if (!order.getMember().getId().equals(memberId)) {
            throw new BusinessException("본인의 주문만 볼 수 있습니다.");
        }
        return order;
    }

    // 주문 취소 : 아직 '주문접수' 상태일 때만 가능하다. 결제 금액을 환불하고 재고를 돌려놓는다
    // 환불한 금액을 돌려준다 (화면에 "N원 환불" 안내를 보여 주기 위해)
    public int cancelOrder(Long memberId, Long orderId) {
        Order order = findMyOrder(memberId, orderId);
        if (order.getStatus() != ShippingStatus.ORDER_RECEIVED) {
            throw new BusinessException("이미 물류센터에 입고된 주문은 취소할 수 없습니다.");
        }
        return refund(order, ShippingStatus.CANCELED);
    }

    // (관리자) 주문 거절 : 출고 전(주문접수, 입고)까지만 가능하고, 거절 사유가 꼭 있어야 한다
    // 손님이 이미 결제했으므로 거절하면 반드시 환불한다. 환불한 금액을 돌려준다
    public int rejectOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new NotFoundException("주문을 찾을 수 없습니다.");
        }
        if (!order.getStatus().isRejectable()) {
            throw new BusinessException("출고 전(주문접수, 입고) 주문만 거절할 수 있습니다. (현재 상태: " + order.getStatus().getLabel() + ")");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("거절 사유를 입력해 주세요. (손님에게 보여집니다)");
        }
        if (reason.trim().length() > 200) {
            throw new BusinessException("거절 사유는 200글자까지 쓸 수 있습니다.");
        }
        order.setRejectReason(reason.trim());
        return refund(order, ShippingStatus.REJECTED);
    }

    // 환불 처리 : 주문취소와 주문거절이 함께 쓴다
    // 1) 줄였던 재고를 다시 돌려놓고  2) 결제 금액 전체를 환불 기록으로 남기고  3) 상태를 바꾼다
    private int refund(Order order, ShippingStatus newStatus) {
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.setStock(product.getStock() + orderItem.getQuantity());
        }
        order.setRefundAmount(order.getTotalPrice());
        order.setRefundedAt(LocalDateTime.now());
        order.setStatus(newStatus);
        return order.getTotalPrice();
    }

    // (관리자) 전체 주문 목록
    @Transactional(readOnly = true)
    public List<Order> findAllOrders() {
        return orderRepository.findAllByOrderByIdDesc();
    }

    // (관리자) 주문 검색 : 검색 기준(type)에 따라 일부만 맞아도 찾는다 (최신 주문부터)
    //   all     : 아래 네 가지 중 하나라도 맞으면
    //   number  : 주문번호의 일부 (예: 20261124, 0012)
    //   member  : 주문자 이름 또는 아이디 (예: 홍길동, hong01)
    //   date    : 주문일시 "yyyy-MM-dd HH:mm" 의 일부 (예: 2026-11-24, 11-24, 2026-11)
    //   address : 배송지 (예: 마포구)
    // 검색어가 비어 있으면 전체 주문
    // 주문 수가 많지 않은 쇼핑몰이라, 전체를 불러와서 자바에서 조건에 맞는 것만 골라낸다
    @Transactional(readOnly = true)
    public List<Order> searchOrders(String type, String keyword) {
        List<Order> all = orderRepository.findAllByOrderByIdDesc();
        if (keyword == null || keyword.trim().isEmpty()) {
            return all;
        }
        String text = keyword.trim();
        List<Order> result = new ArrayList<>();
        for (Order order : all) {
            if (matches(order, type, text)) {
                result.add(order);
            }
        }
        return result;
    }

    // 주문 하나가 검색 조건에 맞는지
    private boolean matches(Order order, String type, String text) {
        boolean byNumber = order.getDisplayNumber().toLowerCase().contains(text.toLowerCase());
        boolean byMember = order.getMember().getName().contains(text)
                || order.getMember().getLoginId().toLowerCase().contains(text.toLowerCase());
        boolean byDate = order.getOrderedAt() != null
                && order.getOrderedAt().format(ORDERED_AT_FORMAT).contains(text);
        boolean byAddress = order.getShippingAddress().contains(text);

        if ("number".equals(type)) {
            return byNumber;
        }
        if ("member".equals(type)) {
            return byMember;
        }
        if ("date".equals(type)) {
            return byDate;
        }
        if ("address".equals(type)) {
            return byAddress;
        }
        return byNumber || byMember || byDate || byAddress;
    }

    // 주문번호 최대 글자 수 (Order 의 order_number 컬럼 길이)
    public static final int ORDER_NUMBER_MAX_LENGTH = 30;

    // (관리자) 주문번호 고치기 : 영문, 숫자, 하이픈(-), 밑줄(_)만, 30글자까지, 다른 주문과 겹치면 안 된다
    // 기본키(id)는 그대로라서 주문 상품, 손님의 주문내역 연결은 바뀌지 않는다. 바뀐 주문번호를 돌려준다
    public String updateOrderNumber(Long orderId, String newNumber) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new NotFoundException("주문을 찾을 수 없습니다.");
        }
        if (newNumber == null || newNumber.trim().isEmpty()) {
            throw new BusinessException("주문번호를 입력해 주세요.");
        }
        String number = newNumber.trim();
        if (number.length() > ORDER_NUMBER_MAX_LENGTH) {
            throw new BusinessException("주문번호는 " + ORDER_NUMBER_MAX_LENGTH + "글자까지 쓸 수 있습니다.");
        }
        if (!number.matches("[A-Za-z0-9_-]+")) {
            throw new BusinessException("주문번호에는 영문, 숫자, 하이픈(-), 밑줄(_)만 쓸 수 있습니다.");
        }
        // 다른 주문이 이미 쓰는 번호인지 (아직 주문번호가 없는 주문은 화면에 보이는 기본 번호와 비교한다)
        for (Order other : orderRepository.findAll()) {
            if (!other.getId().equals(orderId) && other.getDisplayNumber().equalsIgnoreCase(number)) {
                throw new BusinessException("이미 다른 주문이 쓰고 있는 주문번호입니다: " + number);
            }
        }
        order.setOrderNumber(number);
        return number;
    }

    // (관리자) 주문 id 로 화면에 보이는 주문번호 찾기 (처리 결과 메시지에 쓴다, 없으면 null)
    @Transactional(readOnly = true)
    public String findOrderNumber(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        return order == null ? null : order.getDisplayNumber();
    }

    // 주문번호가 비어 있는 주문에 기본 주문번호를 채운다 (서버를 켤 때 DataInitializer 가 부른다)
    // 시연용 SQL(demo-data.sql)처럼 앱을 거치지 않고 넣은 주문, 이 기능이 생기기 전의 주문이 대상이다
    public void fillMissingOrderNumbers() {
        for (Order order : orderRepository.findByOrderNumberIsNull()) {
            assignDefaultOrderNumber(order);
        }
    }

    // 기본 주문번호 붙이기 : 관리자가 다른 주문에 같은 번호를 이미 붙여 두었다면 뒤에 -1, -2 ... 를 붙여 겹치지 않게 한다
    private void assignDefaultOrderNumber(Order order) {
        String base = order.makeDefaultOrderNumber();
        String number = base;
        int suffix = 1;
        while (orderRepository.findByOrderNumber(number) != null) {
            number = base + "-" + suffix;
            suffix++;
        }
        order.setOrderNumber(number);
    }

    // 주문일시 검색에 쓰는 형식 (화면에 보이는 형식과 같다)
    private static final DateTimeFormatter ORDERED_AT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // (관리자) 배송 상태를 다음 단계로 넘긴다 : 주문접수 → 입고 → 출고 → 배송중 → 배송완료
    public void moveToNextStatus(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new NotFoundException("주문을 찾을 수 없습니다.");
        }

        ShippingStatus current = order.getStatus();
        ShippingStatus next = current.next();
        if (next == current) {
            throw new BusinessException("더 이상 진행할 수 없는 주문입니다. (현재 상태: " + current.getLabel() + ")");
        }
        order.setStatus(next);
    }

    // (관리자) 주문 삭제 : 배송완료, 주문취소, 주문거절로 끝난 주문만 지울 수 있다
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new NotFoundException("주문을 찾을 수 없습니다.");
        }
        if (!order.getStatus().isFinished()) {
            throw new BusinessException("진행 중인 주문은 삭제할 수 없습니다. (현재 상태: " + order.getStatus().getLabel() + ")");
        }
        // cascade 설정으로 주문 상품(order_items)도 함께 지워진다
        orderRepository.delete(order);
    }
}
