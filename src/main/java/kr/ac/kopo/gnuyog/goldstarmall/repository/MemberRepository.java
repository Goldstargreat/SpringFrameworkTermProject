package kr.ac.kopo.gnuyog.goldstarmall.repository;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

// 회원 리포지토리
// JpaRepository<엔티티, PK타입> 을 상속하면 save, findById, findAll, delete 같은 기본 기능이 자동으로 생긴다
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 메서드 이름만으로 SQL 이 만들어진다
    // → select * from members where login_id = ?
    // 해당 아이디가 없으면 null 을 돌려준다
    Member findByLoginId(String loginId);
}
