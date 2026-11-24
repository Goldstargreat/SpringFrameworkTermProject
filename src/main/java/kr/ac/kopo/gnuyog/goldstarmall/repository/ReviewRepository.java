package kr.ac.kopo.gnuyog.goldstarmall.repository;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// 상품평 리포지토리
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 한 상품의 상품평을 최신순으로 → select * from reviews where product_id = ? order by id desc
    List<Review> findByProductOrderByIdDesc(Product product);

    // 이 회원이 이 상품에 상품평을 이미 썼는지 (상품 하나에 한 번만 쓸 수 있다)
    boolean existsByMemberAndProduct(Member member, Product product);

    // 한 상품의 평균 별점 (상품평이 없으면 null)
    @Query("select avg(r.rating) from Review r where r.product = :product")
    Double averageRating(@Param("product") Product product);

    // (관리자) 리뷰/문의 화면 : 전체 최신순, 코멘트를 아직 안 단 것만 최신순, 코멘트 대기 개수
    List<Review> findAllByOrderByIdDesc();
    List<Review> findByAdminCommentIsNullOrderByIdDesc();
    long countByAdminCommentIsNull();

    // (회원 알림) 내 상품평 중 관리자 코멘트가 달린 것 (코멘트 단 날짜 최신순)
    List<Review> findByMemberAndAdminCommentIsNotNullOrderByCommentedAtDesc(Member member);

    // (회원 알림) 아직 확인하지 않은 관리자 코멘트 개수
    @Query("select count(r) from Review r where r.member = :member and r.adminComment is not null"
            + " and (r.commentRead is null or r.commentRead = false)")
    long countUnreadComments(@Param("member") Member member);

    // 회원/상품을 지울 때 함께 지우기 위해
    List<Review> findByMember(Member member);

    List<Review> findByProduct(Product product);
}
