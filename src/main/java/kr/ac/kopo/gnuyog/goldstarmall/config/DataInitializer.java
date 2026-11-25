package kr.ac.kopo.gnuyog.goldstarmall.config;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.repository.MemberRepository;
import kr.ac.kopo.gnuyog.goldstarmall.service.GradeService;
import kr.ac.kopo.gnuyog.goldstarmall.service.OrderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// 초기 데이터 등록 클래스
// CommandLineRunner 를 구현하면 서버가 켜진 직후 run() 이 한 번 자동 실행된다
// 상품 데이터는 MySQL 쿼리 콘솔에서 resources/sql/products.sql 을 실행해 직접 넣는다
// 관리자 비밀번호는 BCrypt 로 암호화해야 해서 SQL 로 넣기 어렵기 때문에 관리자 계정만 여기서 만든다
@Component
public class DataInitializer implements CommandLineRunner
{
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final OrderService orderService;
    private final GradeService gradeService;

    public DataInitializer(MemberRepository memberRepository, BCryptPasswordEncoder passwordEncoder,
                           OrderService orderService, GradeService gradeService)
    {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.orderService = orderService;
        this.gradeService = gradeService;
    }

    @Override
    public void run(String... args)
    {
        // 관리자 계정이 없으면 하나 만들어 둔다 (아이디: admin / 비밀번호: admin1234)
        if (memberRepository.findByLoginId("admin") == null)
        {
            Member admin = new Member();
            admin.setLoginId("admin");
            admin.setPassword(passwordEncoder.encode("admin1234"));
            admin.setName("관리자");
            admin.setPhone("010-0000-0000");
            admin.setAddress("GoldstarMall");
            admin.setRole(Member.ROLE_ADMIN);
            admin.setCreatedAt(LocalDateTime.now());
            memberRepository.save(admin);
        }

        // 주문번호가 비어 있는 주문(시연용 SQL 로 넣은 주문 등)에 기본 주문번호를 채운다
        orderService.fillMissingOrderNumbers();

        // 회원 등급을 누적 구매액에 맞게 다시 정한다 (시연용 SQL 로 넣은 배송완료 주문도 반영)
        gradeService.refreshAllGrades();
    }
}
