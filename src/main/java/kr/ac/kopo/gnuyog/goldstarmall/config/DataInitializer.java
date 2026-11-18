package kr.ac.kopo.gnuyog.goldstarmall.config;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import kr.ac.kopo.gnuyog.goldstarmall.repository.MemberRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// 초기 데이터 등록 클래스
// CommandLineRunner 를 구현하면 서버가 켜진 직후 run() 이 한 번 자동 실행된다
@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(ProductRepository productRepository,
                           MemberRepository memberRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 상품이 하나도 없을 때만 샘플 상품을 넣는다 (서버를 다시 켜도 중복으로 들어가지 않게)
        if (productRepository.count() == 0) {
            productRepository.save(new Product("제주 삼다수 2L x 6개", "식품", 6480, 100, "제주 화산암반수로 만든 생수 6개 묶음입니다.", "💧"));
            productRepository.save(new Product("신라면 5개입", "식품", 4100, 80, "얼큰한 국물의 대표 라면 5개 묶음입니다.", "🍜"));
            productRepository.save(new Product("국내산 사과 2kg", "식품", 15900, 30, "아삭하고 달콤한 국내산 사과입니다.", "🍎"));
            productRepository.save(new Product("3겹 화장지 30롤", "생활용품", 13900, 60, "부드럽고 도톰한 3겹 화장지입니다.", "🧻"));
            productRepository.save(new Product("액체 세탁세제 3L", "생활용품", 11500, 45, "찬물에도 잘 녹는 액체 세탁세제입니다.", "🧴"));
            productRepository.save(new Product("주방 수세미 10개", "생활용품", 3900, 120, "흠집 걱정 없는 부드러운 수세미입니다.", "🧽"));
            productRepository.save(new Product("무선 블루투스 이어폰", "가전", 39900, 25, "소음 차단 기능이 있는 무선 이어폰입니다.", "🎧"));
            productRepository.save(new Product("미니 선풍기", "가전", 12900, 40, "USB 충전식 휴대용 선풍기입니다.", "🌀"));
            productRepository.save(new Product("전기 주전자 1.7L", "가전", 24900, 3, "빠르게 끓는 스테인리스 전기 주전자입니다.", "🫖"));
            productRepository.save(new Product("기본 면 티셔츠", "패션", 9900, 70, "사계절 입기 좋은 순면 티셔츠입니다.", "👕"));
            productRepository.save(new Product("러닝화", "패션", 49900, 15, "가볍고 쿠션감 좋은 러닝화입니다.", "👟"));
            productRepository.save(new Product("캔버스 백팩", "패션", 29900, 0, "노트북 수납이 가능한 캔버스 백팩입니다.", "🎒"));
        }

        // 관리자 계정이 없으면 하나 만들어 둔다 (아이디: admin / 비밀번호: admin1234)
        if (memberRepository.findByLoginId("admin") == null) {
            Member admin = new Member();
            admin.setLoginId("admin");
            admin.setPassword(passwordEncoder.encode("admin1234"));
            admin.setName("관리자");
            admin.setPhone("010-0000-0000");
            admin.setAddress("골드스타몰 물류센터");
            admin.setRole("ADMIN");
            admin.setCreatedAt(LocalDateTime.now());
            memberRepository.save(admin);
        }
    }
}
