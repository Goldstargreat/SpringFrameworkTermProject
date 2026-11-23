package kr.ac.kopo.gnuyog.goldstarmall.service;

import kr.ac.kopo.gnuyog.goldstarmall.domain.CartItem;
import kr.ac.kopo.gnuyog.goldstarmall.domain.ShippingStatus;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Order;
import kr.ac.kopo.gnuyog.goldstarmall.domain.OrderItem;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import kr.ac.kopo.gnuyog.goldstarmall.repository.CartItemRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.MemberRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// 주문 서비스 : 주문 생성, 조회, 취소, 배송 상태 변경
@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;

    public OrderService(OrderRepository orderRepository,
                        CartItemRepository cartItemRepository,
                        MemberRepository memberRepository) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.memberRepository = memberRepository;
    }

    // 주문 생성 : 장바구니에 담긴 상품 전체를 주문한다
    // @Transactional 덕분에 중간에 오류(재고 부족 등)가 나면 앞에서 한 작업이 모두 취소(rollback)된다
    public Order createOrder(Long memberId, String receiverName, String receiverPhone, String shippingAddress) {
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            throw new IllegalStateException("회원 정보를 찾을 수 없습니다.");
        }

        // 1) 장바구니 조회
        List<CartItem> cartItems = cartItemRepository.findByMember(member);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("장바구니가 비어 있습니다.");
        }

        // 2) 주문 객체 만들기
        Order order = new Order();
        order.setMember(member);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setShippingAddress(shippingAddress);
        order.setStatus(ShippingStatus.ORDER_RECEIVED);
        order.setOrderedAt(LocalDateTime.now());

        // 3) 장바구니 항목을 하나씩 주문 상품으로 옮기면서 재고를 줄인다
        int totalPrice = 0;
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            // 재고 확인
            if (product.getStock() < cartItem.getQuantity()) {
                throw new IllegalStateException(product.getName() + " 상품의 재고가 부족합니다.");
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
        orderRepository.save(order);

        // 5) 주문이 끝났으니 장바구니 비우기
        cartItemRepository.deleteAll(cartItems);

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
            throw new IllegalStateException("주문을 찾을 수 없습니다.");
        }
        if (!order.getMember().getId().equals(memberId)) {
            throw new IllegalStateException("본인의 주문만 볼 수 있습니다.");
        }
        return order;
    }

    // 주문 취소 : 아직 '주문접수' 상태일 때만 가능하고, 줄였던 재고를 다시 돌려놓는다
    public void cancelOrder(Long memberId, Long orderId) {
        Order order = findMyOrder(memberId, orderId);
        if (order.getStatus() != ShippingStatus.ORDER_RECEIVED) {
            throw new IllegalStateException("이미 물류센터에 입고된 주문은 취소할 수 없습니다.");
        }

        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.setStock(product.getStock() + orderItem.getQuantity());
        }
        order.setStatus(ShippingStatus.CANCELED);
    }

    // (관리자) 전체 주문 목록
    @Transactional(readOnly = true)
    public List<Order> findAllOrders() {
        return orderRepository.findAllByOrderByIdDesc();
    }

    // (관리자) 배송 상태를 다음 단계로 넘긴다 : 주문접수 → 입고 → 출고 → 배송중 → 배송완료
    public void moveToNextStatus(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new IllegalStateException("주문을 찾을 수 없습니다.");
        }

        ShippingStatus current = order.getStatus();
        ShippingStatus next = current.next();
        if (next == current) {
            throw new IllegalStateException("더 이상 진행할 수 없는 주문입니다. (현재 상태: " + current.getLabel() + ")");
        }
        order.setStatus(next);
    }
}
