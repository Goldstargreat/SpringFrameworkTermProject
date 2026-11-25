package kr.ac.kopo.gnuyog.goldstarmall.service;

import kr.ac.kopo.gnuyog.goldstarmall.domain.CartItem;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Order;
import kr.ac.kopo.gnuyog.goldstarmall.domain.OrderItem;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import kr.ac.kopo.gnuyog.goldstarmall.domain.ProductOption;
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

// 주문 서비스 : 주문 생성, 조회, 취소, 반품, (관리자) 주문 거절 / 배송 상태 변경 / 검색 / 주문번호 / 삭제
// 클래스에 @Transactional 이 붙어 있어서, 메서드 하나가 끝날 때까지 한 묶음으로 처리된다
// (중간에 오류가 나면 그 메서드에서 한 작업이 모두 취소(rollback)된다)
@Service
@Transactional
public class OrderService {

    // 주문번호 최대 글자 수 (Order 의 order_number 컬럼 길이)
    public static final int ORDER_NUMBER_MAX_LENGTH = 30;

    // 주문번호에 쓸 수 있는 글자 : 영문, 숫자, 하이픈(-), 밑줄(_)
    private static final String ORDER_NUMBER_PATTERN = "[A-Za-z0-9_-]+";

    // 반품 사유, 거절 사유 등의 최대 글자 수
    private static final int REASON_MAX_LENGTH = 200;

    // 주문일시 검색에 쓰는 형식 (화면에 보이는 형식과 같다)
    private static final DateTimeFormatter ORDERED_AT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final GradeService gradeService;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository,
                        CartItemRepository cartItemRepository,
                        MemberRepository memberRepository,
                        ProductRepository productRepository,
                        GradeService gradeService,
                        ProductService productService) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.gradeService = gradeService;
        this.productService = productService;
    }

    // ===== 주문하기 =====

    // 장바구니 주문 : 장바구니에 담긴 상품 전체를 주문한다
    // 중간에 재고가 모자라면 오류가 나고, 앞에서 줄인 재고도 모두 원래대로 돌아간다
    public Order createOrder(Long memberId, String receiverName, String receiverPhone, String shippingAddress) {
        Member member = getMember(memberId);

        // 1) 장바구니 조회
        List<CartItem> cartItems = cartItemRepository.findByMember(member);
        if (cartItems.isEmpty()) {
            throw new BusinessException("장바구니가 비어 있습니다.");
        }

        // 2) 주문 만들기
        Order order = newOrder(member, receiverName, receiverPhone, shippingAddress);

        // 3) 장바구니 항목을 하나씩 주문 상품으로 옮기면서 재고를 줄인다
        int totalPrice = 0;
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            ProductOption option = cartItem.getOption();
            int quantity = cartItem.getQuantity();

            // 재고 확인 (옵션이 있으면 그 옵션의 재고)
            if (cartItem.getAvailableStock() < quantity) {
                String optionText = "";
                if (option != null) {
                    optionText = " (옵션: " + option.getName() + ")";
                }
                throw new BusinessException(product.getName() + " 상품의 재고가 부족합니다." + optionText);
            }
            decreaseStock(product, option, quantity);

            OrderItem orderItem = newOrderItem(product, option, quantity);
            order.addOrderItem(orderItem);
            totalPrice = totalPrice + orderItem.getTotalPrice();
        }

        // 4) 상품 금액 합계에서 등급 할인을 뺀 금액이 결제 금액이다
        applyGradeDiscount(order, member, totalPrice);

        // 5) 저장하고 주문번호 붙이기 (cascade 설정으로 주문 상품들도 함께 저장된다)
        saveWithOrderNumber(order);

        // 6) 주문이 끝났으니 장바구니 비우기
        cartItemRepository.deleteAll(cartItems);
        return order;
    }

    // 바로 주문 : 상품 상세 화면에서 상품 1개만 바로 주문한다
    // 장바구니를 거치지 않으므로, 장바구니에 담아 둔 다른 상품은 그대로 남는다
    public Order createDirectOrder(Long memberId, Long productId, Long optionId, int quantity,
                                   String receiverName, String receiverPhone, String shippingAddress) {
        Member member = getMember(memberId);
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            throw new NotFoundException("상품을 찾을 수 없습니다.");
        }

        // 1) 옵션, 수량, 재고 확인
        ProductOption option = productService.resolveOption(product, optionId);
        checkDirectOrderQuantity(product, option, quantity);

        // 2) 재고 줄이기
        decreaseStock(product, option, quantity);

        // 3) 주문 + 주문 상품 1개 만들기
        Order order = newOrder(member, receiverName, receiverPhone, shippingAddress);
        OrderItem orderItem = newOrderItem(product, option, quantity);
        order.addOrderItem(orderItem);
        applyGradeDiscount(order, member, orderItem.getTotalPrice());

        // 4) 저장하고 주문번호 붙이기
        saveWithOrderNumber(order);
        return order;
    }

    // 바로 주문 수량 검사 : 1개 이상, 남은 재고 이하 (주문서 화면을 보여 줄 때도 쓴다)
    // 옵션을 골랐으면 그 옵션의 재고로 검사한다
    public void checkDirectOrderQuantity(Product product, ProductOption option, int quantity) {
        if (product.getStock() == 0) {
            throw new BusinessException("일시 품절된 상품입니다.");
        }
        if (quantity < 1) {
            throw new BusinessException("수량은 1개 이상이어야 합니다.");
        }
        if (option != null) {
            if (quantity > option.getStock()) {
                throw new BusinessException(product.getName() + " : " + option.getStockShortageMessage());
            }
            return;
        }
        if (quantity > product.getStock()) {
            throw new BusinessException(product.getName() + " : " + product.getStockShortageMessage());
        }
    }

    // 주문 만들기 : 받는 사람 정보를 담고 '주문접수' 상태로 시작한다
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

    // 주문 상품 만들기
    // 주문 가격 = 주문할 때의 판매 가격 (오늘의 할인 적용). 요일이 바뀌어도 주문 금액은 그대로 남는다
    private OrderItem newOrderItem(Product product, ProductOption option, int quantity) {
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setOption(option);
        orderItem.setOrderPrice(product.getSalePrice());
        orderItem.setQuantity(quantity);
        return orderItem;
    }

    // 재고 줄이기 : 옵션이 있으면 옵션 재고도 함께 줄인다 (상품 재고 = 옵션 재고의 합계를 유지)
    // 변경 감지로 update SQL 이 실행된다
    private void decreaseStock(Product product, ProductOption option, int quantity) {
        product.setStock(product.getStock() - quantity);
        if (option != null) {
            option.setStock(option.getStock() - quantity);
        }
    }

    // 등급 할인 적용 : 주문할 때의 등급과 할인 금액을 주문에 저장하고, 결제 금액 = 상품 금액 - 할인
    private void applyGradeDiscount(Order order, Member member, int originalPrice) {
        int discount = member.getGrade().discountOf(originalPrice);
        order.setOrderGrade(member.getGrade());
        order.setDiscountAmount(discount);
        order.setTotalPrice(originalPrice - discount);
    }

    // 주문 저장 후 기본 주문번호 붙이기 (저장해야 id 가 생기고, 기본 주문번호는 그 id 로 만든다)
    private void saveWithOrderNumber(Order order) {
        orderRepository.save(order);
        assignDefaultOrderNumber(order);
    }

    // ===== 내 주문 =====

    // 내 주문 목록 (최신순, 내가 삭제한 주문은 빼고)
    @Transactional(readOnly = true)
    public List<Order> findMyOrders(Long memberId) {
        Member member = memberRepository.findById(memberId).orElse(null);
        return orderRepository.findVisibleByMember(member);
    }

    // 내 주문 1건 조회 (본인 주문이 아니거나, 내가 삭제한 주문이면 오류)
    @Transactional(readOnly = true)
    public Order findMyOrder(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || order.isHiddenByMember()) {
            throw new NotFoundException("주문을 찾을 수 없습니다.");
        }
        if (!order.getMember().getId().equals(memberId)) {
            throw new BusinessException("본인의 주문만 볼 수 있습니다.");
        }
        return order;
    }

    // 주문 취소 : 배송완료 전(주문접수, 입고, 출고, 배송중)까지 가능하다. 결제 금액을 환불하고 재고를 돌려놓는다
    // 환불한 금액을 돌려준다 (화면에 "N원 환불" 안내를 보여 주기 위해)
    public int cancelOrder(Long memberId, Long orderId) {
        Order order = findMyOrder(memberId, orderId);
        if (!order.getStatus().isCancelable()) {
            throw new BusinessException("배송완료되었거나 이미 취소/거절된 주문은 취소할 수 없습니다." + currentStatusText(order));
        }
        return refund(order, ShippingStatus.CANCELED);
    }

    // 내 주문내역에서 삭제 : 배송완료, 주문취소, 주문거절로 끝난 주문만 가능하다
    // 실제로 지우지 않고 회원 화면에서만 숨긴다 (등급, 상품평 자격, 관리자 주문 관리는 그대로)
    // 삭제한 주문의 주문번호를 돌려준다 (화면 안내 메시지용)
    public String hideMyOrder(Long memberId, Long orderId) {
        Order order = findMyOrder(memberId, orderId);
        checkFinished(order);
        order.setHiddenByMember(true);
        return order.getDisplayNumber();
    }

    // ===== 반품 : 배송완료 후 14일 이내, 하자 없는 상품만 =====
    // 손님이 신청(반품신청) → 관리자가 돌려받은 상품을 검수 → 승인(반품완료, 환불) 또는 거절(배송완료로 되돌림)

    // 반품 신청 : 반품 사유와 "하자 없음" 확인이 꼭 있어야 한다. 신청한 주문의 주문번호를 돌려준다
    public String requestReturn(Long memberId, Long orderId, String reason, boolean noDefectConfirmed) {
        Order order = findMyOrder(memberId, orderId);
        if (order.getStatus() != ShippingStatus.DELIVERED) {
            throw new BusinessException("배송완료된 주문만 반품을 신청할 수 있습니다." + currentStatusText(order));
        }
        if (order.getReturnRejectReason() != null) {
            throw new BusinessException("반품이 거절된 주문은 다시 신청할 수 없습니다.");
        }
        if (!order.isReturnable()) {
            throw new BusinessException("반품 신청 기간(배송완료 후 " + Order.RETURN_PERIOD_DAYS + "일)이 지났습니다.");
        }
        String checkedReason = checkReason(reason, "반품 사유", "");
        if (!noDefectConfirmed) {
            throw new BusinessException("상품에 하자(파손, 오염, 사용 흔적, 구성품 누락)가 없는 경우에만 반품할 수 있습니다. 확인란에 체크해 주세요.");
        }
        order.setReturnReason(checkedReason);
        order.setReturnRequestedAt(LocalDateTime.now());
        order.setStatus(ShippingStatus.RETURN_REQUESTED);
        return order.getDisplayNumber();
    }

    // (관리자) 반품 승인 : 결제 금액 전체를 환불하고 재고를 돌려놓는다. 누적 구매액에서 빠지므로 등급을 다시 계산한다
    public int approveReturn(Long orderId) {
        Order order = findReturnRequested(orderId);
        int refund = refund(order, ShippingStatus.RETURNED);
        gradeService.refreshGrade(order.getMember());
        return refund;
    }

    // (관리자) 반품 거절 : 검수 결과 하자가 있는 등의 이유로 거절한다. 사유가 꼭 있어야 하고, 배송완료 상태로 되돌린다
    public void rejectReturn(Long orderId, String reason) {
        Order order = findReturnRequested(orderId);
        order.setReturnRejectReason(checkReason(reason, "반품 거절 사유", " (손님에게 보여집니다)"));
        order.setStatus(ShippingStatus.DELIVERED);
    }

    // (관리자) 반품 신청 상태인 주문 찾기 (없거나 반품신청 상태가 아니면 오류)
    private Order findReturnRequested(Long orderId) {
        Order order = getOrder(orderId);
        if (order.getStatus() != ShippingStatus.RETURN_REQUESTED) {
            throw new BusinessException("반품 신청된 주문이 아닙니다." + currentStatusText(order));
        }
        return order;
    }

    // (관리자) 처리를 기다리는 반품 신청 개수 (화면 상단 '주문관리' 옆 빨간 숫자)
    @Transactional(readOnly = true)
    public long countPendingReturns() {
        return orderRepository.countByStatus(ShippingStatus.RETURN_REQUESTED);
    }

    // ===== 환불 =====

    // 환불 처리 : 주문취소, 주문거절, 반품승인이 함께 쓴다. 환불한 금액을 돌려준다
    // 1) 줄였던 재고를 다시 돌려놓고  2) 결제 금액 전체를 환불 기록으로 남기고  3) 상태를 바꾼다
    private int refund(Order order, ShippingStatus newStatus) {
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.setStock(product.getStock() + orderItem.getQuantity());
            // 옵션 상품이면 그 옵션의 재고도 돌려놓는다
            ProductOption option = orderItem.getOption();
            if (option != null) {
                option.setStock(option.getStock() + orderItem.getQuantity());
            }
        }
        order.setRefundAmount(order.getTotalPrice());
        order.setRefundedAt(LocalDateTime.now());
        order.setStatus(newStatus);
        return order.getTotalPrice();
    }

    // ===== (관리자) 주문 처리 =====

    // 주문 거절 : 출고 전(주문접수, 입고)까지만 가능하고, 거절 사유가 꼭 있어야 한다
    // 손님이 이미 결제했으므로 거절하면 반드시 환불한다. 환불한 금액을 돌려준다
    public int rejectOrder(Long orderId, String reason) {
        Order order = getOrder(orderId);
        if (!order.getStatus().isRejectable()) {
            throw new BusinessException("출고 전(주문접수, 입고) 주문만 거절할 수 있습니다." + currentStatusText(order));
        }
        order.setRejectReason(checkReason(reason, "거절 사유", " (손님에게 보여집니다)"));
        return refund(order, ShippingStatus.REJECTED);
    }

    // 배송 상태를 다음 단계로 넘긴다 : 주문접수 → 입고 → 출고 → 배송중 → 배송완료
    public void moveToNextStatus(Long orderId) {
        Order order = getOrder(orderId);

        ShippingStatus current = order.getStatus();
        ShippingStatus next = current.next();
        if (next == current) {
            throw new BusinessException("더 이상 진행할 수 없는 주문입니다." + currentStatusText(order));
        }
        order.setStatus(next);

        if (next == ShippingStatus.DELIVERED) {
            // 배송완료 일시를 남긴다 (반품 신청 기한 14일을 이 날짜부터 센다)
            order.setDeliveredAt(LocalDateTime.now());
            // 누적 구매액이 늘어나므로 주문한 회원의 등급을 다시 계산한다
            gradeService.refreshGrade(order.getMember());
        }
    }

    // 주문 삭제 : 배송완료, 주문취소, 주문거절로 끝난 주문만 지울 수 있다
    public void deleteOrder(Long orderId) {
        Order order = getOrder(orderId);
        checkFinished(order);
        // cascade 설정으로 주문 상품(order_items)도 함께 지워진다
        orderRepository.delete(order);
    }

    // ===== (관리자) 주문 목록 / 검색 =====

    // 전체 주문 목록 (최신 주문부터)
    @Transactional(readOnly = true)
    public List<Order> findAllOrders() {
        return orderRepository.findAllByOrderByIdDesc();
    }

    // 주문 검색 : 검색 기준(type)에 따라 일부만 맞아도 찾는다 (최신 주문부터)
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
        if (!hasText(keyword)) {
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

    // 주문 하나가 검색 조건에 맞는지 (주문번호와 아이디는 대소문자를 가리지 않는다)
    private boolean matches(Order order, String type, String text) {
        String lowerText = text.toLowerCase();
        Member member = order.getMember();

        boolean byNumber = order.getDisplayNumber().toLowerCase().contains(lowerText);
        boolean byMember = member.getName().contains(text)
                || member.getLoginId().toLowerCase().contains(lowerText);
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

    // ===== 주문번호 =====

    // (관리자) 주문번호 고치기 : 영문, 숫자, 하이픈(-), 밑줄(_)만, 30글자까지, 다른 주문과 겹치면 안 된다
    // 기본키(id)는 그대로라서 주문 상품, 손님의 주문내역 연결은 바뀌지 않는다. 바뀐 주문번호를 돌려준다
    public String updateOrderNumber(Long orderId, String newNumber) {
        Order order = getOrder(orderId);
        if (!hasText(newNumber)) {
            throw new BusinessException("주문번호를 입력해 주세요.");
        }
        String number = newNumber.trim();
        if (number.length() > ORDER_NUMBER_MAX_LENGTH) {
            throw new BusinessException("주문번호는 " + ORDER_NUMBER_MAX_LENGTH + "글자까지 쓸 수 있습니다.");
        }
        if (!number.matches(ORDER_NUMBER_PATTERN)) {
            throw new BusinessException("주문번호에는 영문, 숫자, 하이픈(-), 밑줄(_)만 쓸 수 있습니다.");
        }
        // 다른 주문이 이미 쓰는 번호인지 (아직 주문번호가 없는 주문은 화면에 보이는 기본 번호와 비교한다)
        for (Order other : orderRepository.findAll()) {
            boolean isOtherOrder = !other.getId().equals(orderId);
            if (isOtherOrder && other.getDisplayNumber().equalsIgnoreCase(number)) {
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
        if (order == null) {
            return null;
        }
        return order.getDisplayNumber();
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

    // ===== 도우미 =====

    // 회원 1명 조회 (없으면 예외)
    private Member getMember(Long memberId) {
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            throw new NotFoundException("회원 정보를 찾을 수 없습니다.");
        }
        return member;
    }

    // 주문 1건 조회 (없으면 예외)
    private Order getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new NotFoundException("주문을 찾을 수 없습니다.");
        }
        return order;
    }

    // 끝난 주문(배송완료, 주문취소, 주문거절 등)인지 확인 (진행 중이면 삭제할 수 없다)
    private void checkFinished(Order order) {
        if (!order.getStatus().isFinished()) {
            throw new BusinessException("진행 중인 주문은 삭제할 수 없습니다." + currentStatusText(order));
        }
    }

    // 사유 검사 : 비어 있으면 안 되고 REASON_MAX_LENGTH 글자까지. 앞뒤 공백을 지운 사유를 돌려준다
    // 예) checkReason(" ", "반품 사유", "") → "반품 사유를 입력해 주세요."
    private String checkReason(String reason, String reasonName, String emptyHint) {
        if (!hasText(reason)) {
            throw new BusinessException(reasonName + "를 입력해 주세요." + emptyHint);
        }
        String trimmed = reason.trim();
        if (trimmed.length() > REASON_MAX_LENGTH) {
            throw new BusinessException(reasonName + "는 " + REASON_MAX_LENGTH + "글자까지 쓸 수 있습니다.");
        }
        return trimmed;
    }

    // 오류 메시지 뒤에 붙이는 지금 상태  예) " (현재 상태: 배송중)"
    private String currentStatusText(Order order) {
        return " (현재 상태: " + order.getStatus().getLabel() + ")";
    }

    // 값이 있는지 (null 이거나 공백뿐이면 없는 것)
    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
