package kr.ac.kopo.gnuyog.goldstarmall.repository;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Inquiry;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 상품문의 리포지토리
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    // 한 상품의 문의를 최신순으로 → select * from inquiries where product_id = ? order by id desc
    List<Inquiry> findByProductOrderByIdDesc(Product product);

    // 회원/상품을 지울 때 함께 지우기 위해
    List<Inquiry> findByMember(Member member);

    List<Inquiry> findByProduct(Product product);
}
