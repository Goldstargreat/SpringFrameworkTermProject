package kr.ac.kopo.gnuyog.goldstarmall.repository;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// 회원 리포지토리
// JpaRepository<엔티티, PK타입> 을 상속하면 save, findById, findAll, delete 같은 기본 기능이 자동으로 생긴다
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 메서드 이름만으로 SQL 이 만들어진다
    // → select * from members where login_id = ?
    // 해당 아이디가 없으면 null 을 돌려준다
    Member findByLoginId(String loginId);

    // → select * from members where phone = ?
    // 전화번호 중복 검사에 쓴다 (없으면 null)
    Member findByPhone(String phone);

    // (관리자) 전화번호 일부로 회원 검색
    // DB에는 "010-1234-5678" 처럼 하이픈이 들어가 있으므로 하이픈을 뺀 숫자끼리 비교한다
    // → "5678", "12345678", "010-1234" 어떤 식으로 입력해도 찾을 수 있다
    @Query("select m from Member m where replace(m.phone, '-', '') like concat('%', :digits, '%') order by m.id")
    List<Member> searchByPhoneDigits(@Param("digits") String digits);
}
