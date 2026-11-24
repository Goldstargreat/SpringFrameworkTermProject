package kr.ac.kopo.gnuyog.goldstarmall.repository;

import kr.ac.kopo.gnuyog.goldstarmall.domain.CartItem;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import kr.ac.kopo.gnuyog.goldstarmall.domain.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 장바구니 리포지토리
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 한 회원의 장바구니 전체 → select * from cart_items where member_id = ?
    List<CartItem> findByMember(Member member);

    // 한 회원이 특정 상품(+옵션)을 이미 담았는지 확인 (없으면 null)
    // option 이 null 이면 "option_id is null" 로 찾는다 (옵션이 없는 상품)
    CartItem findByMemberAndProductAndOption(Member member, Product product, ProductOption option);

    // 특정 상품이 담긴 모든 장바구니 항목 (상품을 삭제할 때 함께 지우기 위해)
    List<CartItem> findByProduct(Product product);
}
