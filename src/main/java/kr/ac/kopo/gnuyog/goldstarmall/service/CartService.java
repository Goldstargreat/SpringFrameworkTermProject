package kr.ac.kopo.gnuyog.goldstarmall.service;

import kr.ac.kopo.gnuyog.goldstarmall.domain.CartItem;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import kr.ac.kopo.gnuyog.goldstarmall.domain.ProductOption;
import kr.ac.kopo.gnuyog.goldstarmall.exception.BusinessException;
import kr.ac.kopo.gnuyog.goldstarmall.exception.NotFoundException;
import kr.ac.kopo.gnuyog.goldstarmall.repository.CartItemRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.MemberRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 장바구니 서비스 : 담기, 조회, 수량 변경, 삭제
// 같은 상품이라도 옵션이 다르면 다른 항목이다. 재고보다 많이 담을 수 없다
@Service
@Transactional
public class CartService {

    // 수량이 1개보다 적을 때의 메시지
    private static final String QUANTITY_TOO_SMALL = "수량은 1개 이상이어야 합니다.";

    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public CartService(CartItemRepository cartItemRepository,
                       MemberRepository memberRepository,
                       ProductRepository productRepository,
                       ProductService productService) {
        this.cartItemRepository = cartItemRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.productService = productService;
    }

    // ===== 담기 =====

    // 장바구니에 상품 담기 (옵션이 있는 상품이면 optionId 로 옵션을 골라야 한다)
    public void addToCart(Long memberId, Long productId, Long optionId, int quantity) {
        checkQuantity(quantity);

        // 1) 회원, 상품, 옵션 확인
        Member member = memberRepository.findById(memberId).orElse(null);
        Product product = productRepository.findById(productId).orElse(null);
        if (member == null || product == null) {
            throw new NotFoundException("회원 또는 상품 정보를 찾을 수 없습니다.");
        }
        ProductOption option = productService.resolveOption(product, optionId);

        // 2) 이미 담은 상품(같은 옵션)이면 그 항목에 수량을 더하고, 처음 담는 상품이면 새 항목을 만든다
        CartItem cartItem = cartItemRepository.findByMemberAndProductAndOption(member, product, option);
        if (cartItem == null) {
            cartItem = new CartItem();   // 새 항목의 수량은 0 에서 시작한다
            cartItem.setMember(member);
            cartItem.setProduct(product);
            cartItem.setOption(option);
        }
        int newQuantity = cartItem.getQuantity() + quantity;

        // 3) 재고보다 많이 담을 수 없다 (옵션이 있으면 그 옵션의 재고)
        if (newQuantity > cartItem.getAvailableStock()) {
            throw new BusinessException(cartItem.getStockShortageMessage());
        }

        cartItem.setQuantity(newQuantity);
        cartItemRepository.save(cartItem);
    }

    // ===== 조회 =====

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

    // ===== 수량 변경 / 삭제 =====

    // 수량 변경
    public void updateQuantity(Long memberId, Long cartItemId, int quantity) {
        CartItem cartItem = findMyCartItem(memberId, cartItemId);
        checkQuantity(quantity);
        if (quantity > cartItem.getAvailableStock()) {
            throw new BusinessException(cartItem.getStockShortageMessage());
        }
        // 트랜잭션 안에서 값을 바꾸면 JPA 가 알아서 update SQL 을 실행한다 (변경 감지)
        cartItem.setQuantity(quantity);
    }

    // 항목 삭제
    public void deleteCartItem(Long memberId, Long cartItemId) {
        CartItem cartItem = findMyCartItem(memberId, cartItemId);
        cartItemRepository.delete(cartItem);
    }

    // ===== 공통 =====

    // 수량은 1개 이상이어야 한다
    private void checkQuantity(int quantity) {
        if (quantity < 1) {
            throw new BusinessException(QUANTITY_TOO_SMALL);
        }
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
