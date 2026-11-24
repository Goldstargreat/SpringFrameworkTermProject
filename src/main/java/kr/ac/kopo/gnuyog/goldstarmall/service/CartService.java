package kr.ac.kopo.gnuyog.goldstarmall.service;

import kr.ac.kopo.gnuyog.goldstarmall.domain.CartItem;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import kr.ac.kopo.gnuyog.goldstarmall.exception.BusinessException;
import kr.ac.kopo.gnuyog.goldstarmall.exception.NotFoundException;
import kr.ac.kopo.gnuyog.goldstarmall.repository.CartItemRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.MemberRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 장바구니 서비스 : 담기, 조회, 수량 변경, 삭제
@Service
@Transactional
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    public CartService(CartItemRepository cartItemRepository,
                       MemberRepository memberRepository,
                       ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
    }

    // 장바구니에 상품 담기
    public void addToCart(Long memberId, Long productId, int quantity) {
        if (quantity < 1) {
            throw new BusinessException("수량은 1개 이상이어야 합니다.");
        }

        Member member = memberRepository.findById(memberId).orElse(null);
        Product product = productRepository.findById(productId).orElse(null);
        if (member == null || product == null) {
            throw new NotFoundException("회원 또는 상품 정보를 찾을 수 없습니다.");
        }

        // 이미 담은 상품이면 수량만 더하고, 처음 담는 상품이면 새 항목을 만든다
        CartItem cartItem = cartItemRepository.findByMemberAndProduct(member, product);
        int newQuantity = quantity;
        if (cartItem != null) {
            newQuantity = cartItem.getQuantity() + quantity;
        }

        // 재고보다 많이 담을 수 없다
        if (newQuantity > product.getStock()) {
            throw new BusinessException(product.getStockShortageMessage());
        }

        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setMember(member);
            cartItem.setProduct(product);
        }
        cartItem.setQuantity(newQuantity);
        cartItemRepository.save(cartItem);
    }

    // 내 장바구니 목록
    @Transactional(readOnly = true)
    public List<CartItem> findCartItems(Long memberId) {
        Member member = memberRepository.findById(memberId).orElse(null);
        return cartItemRepository.findByMember(member);
    }

    // 장바구니 총 금액 계산
    public int calculateTotalPrice(List<CartItem> cartItems) {
        int total = 0;
        for (CartItem cartItem : cartItems) {
            total = total + cartItem.getTotalPrice();
        }
        return total;
    }

    // 수량 변경
    public void updateQuantity(Long memberId, Long cartItemId, int quantity) {
        CartItem cartItem = findMyCartItem(memberId, cartItemId);
        if (quantity < 1) {
            throw new BusinessException("수량은 1개 이상이어야 합니다.");
        }
        if (quantity > cartItem.getProduct().getStock()) {
            throw new BusinessException(cartItem.getProduct().getStockShortageMessage());
        }
        // 트랜잭션 안에서 값을 바꾸면 JPA 가 알아서 update SQL 을 실행한다 (변경 감지)
        cartItem.setQuantity(quantity);
    }

    // 항목 삭제
    public void deleteCartItem(Long memberId, Long cartItemId) {
        CartItem cartItem = findMyCartItem(memberId, cartItemId);
        cartItemRepository.delete(cartItem);
    }

    // 장바구니 항목을 찾고, 로그인한 회원의 것이 맞는지 확인한다 (남의 장바구니를 건드리지 못하게)
    private CartItem findMyCartItem(Long memberId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElse(null);
        if (cartItem == null) {
            throw new NotFoundException("장바구니 항목을 찾을 수 없습니다.");
        }
        if (!cartItem.getMember().getId().equals(memberId)) {
            throw new BusinessException("본인의 장바구니만 수정할 수 있습니다.");
        }
        return cartItem;
    }
}
