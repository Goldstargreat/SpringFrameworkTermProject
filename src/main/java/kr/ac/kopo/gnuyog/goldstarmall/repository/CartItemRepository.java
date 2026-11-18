package kr.ac.kopo.gnuyog.goldstarmall.repository;

import kr.ac.kopo.gnuyog.goldstarmall.domain.CartItem;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 장바구니 리포지토리
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 한 회원의 장바구니 전체 → select * from cart_items where member_id = ?
    List<CartItem> findByMember(Member member);

    // 한 회원이 특정 상품을 이미 담았는지 확인 (없으면 null)
    CartItem findByMemberAndProduct(Member member, Product product);
}
