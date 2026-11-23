package kr.ac.kopo.gnuyog.goldstarmall.service;

import kr.ac.kopo.gnuyog.goldstarmall.domain.ShippingStatus;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Order;
import kr.ac.kopo.gnuyog.goldstarmall.repository.CartItemRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.MemberRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.OrderRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// 회원 서비스 : 회원가입, 로그인, 정보 수정, 비밀번호 변경, 탈퇴
@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // 생성자 주입 : 스프링이 필요한 객체를 알아서 넣어 준다
    public MemberService(MemberRepository memberRepository,
                         CartItemRepository cartItemRepository,
                         OrderRepository orderRepository,
                         BCryptPasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원가입
    public Member join(String loginId, String password, String name, String phone, String address) {
        // 1) 아이디 중복 검사
        Member found = memberRepository.findByLoginId(loginId);
        if (found != null) {
            throw new IllegalStateException("이미 사용 중인 아이디입니다.");
        }

        // 2) 회원 객체를 만들고 값 채우기
        Member member = new Member();
        member.setLoginId(loginId);
        // 비밀번호는 그대로 저장하지 않고 BCrypt 로 암호화해서 저장한다
        member.setPassword(passwordEncoder.encode(password));
        member.setName(name);
        member.setPhone(phone);
        member.setAddress(address);
        member.setRole("USER");
        member.setCreatedAt(LocalDateTime.now());

        // 3) DB에 저장 (insert into members ...)
        return memberRepository.save(member);
    }

    // 로그인 : 성공하면 회원 객체, 실패하면 null 을 돌려준다
    @Transactional(readOnly = true)
    public Member login(String loginId, String password) {
        // 1) 아이디로 회원 찾기
        Member member = memberRepository.findByLoginId(loginId);
        if (member == null) {
            return null;
        }

        // 2) 입력한 비밀번호와 DB의 암호화된 비밀번호 비교
        boolean matched = passwordEncoder.matches(password, member.getPassword());
        if (!matched) {
            return null;
        }
        return member;
    }

    // 회원 번호로 회원 찾기 (없으면 null)
    @Transactional(readOnly = true)
    public Member findById(Long memberId) {
        return memberRepository.findById(memberId).orElse(null);
    }

    // 내 정보 수정 : 이름, 전화번호, 기본 배송지 (아이디는 바꿀 수 없다)
    public void updateInfo(Long memberId, String name, String phone, String address) {
        if (name.trim().isEmpty()) {
            throw new IllegalStateException("이름을 입력해 주세요.");
        }
        Member member = findById(memberId);
        // 트랜잭션 안에서 값을 바꾸면 JPA 가 알아서 update SQL 을 실행한다 (변경 감지)
        member.setName(name.trim());
        member.setPhone(phone);
        member.setAddress(address);
    }

    // 비밀번호 변경 : 현재 비밀번호가 맞아야 바꿀 수 있다
    public void changePassword(Long memberId, String currentPassword, String newPassword, String newPasswordConfirm) {
        Member member = findById(memberId);
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new IllegalStateException("현재 비밀번호가 올바르지 않습니다.");
        }
        if (newPassword.length() < 4) {
            throw new IllegalStateException("새 비밀번호는 4글자 이상이어야 합니다.");
        }
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new IllegalStateException("새 비밀번호와 비밀번호 확인이 서로 다릅니다.");
        }
        member.setPassword(passwordEncoder.encode(newPassword));
    }

    // 회원 탈퇴 : 배송이 끝나지 않은 주문이 있으면 탈퇴할 수 없다
    public void withdraw(Long memberId, String password) {
        Member member = findById(memberId);
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalStateException("비밀번호가 올바르지 않습니다.");
        }
        if ("ADMIN".equals(member.getRole())) {
            throw new IllegalStateException("관리자 계정은 탈퇴할 수 없습니다.");
        }

        // 1) 진행 중인 주문(배송완료, 주문취소가 아닌 주문)이 있는지 확인
        List<Order> orders = orderRepository.findByMemberOrderByIdDesc(member);
        for (Order order : orders) {
            ShippingStatus status = order.getStatus();
            if (status != ShippingStatus.DELIVERED && status != ShippingStatus.CANCELED) {
                throw new IllegalStateException("배송 중인 주문이 있어 탈퇴할 수 없습니다. 배송이 끝난 뒤 다시 시도해 주세요.");
            }
        }

        // 2) 회원을 가리키는 데이터부터 지운다 (외래키 때문에 순서가 중요하다)
        //    주문을 지우면 cascade 설정으로 주문 상품도 함께 지워진다
        cartItemRepository.deleteAll(cartItemRepository.findByMember(member));
        orderRepository.deleteAll(orders);

        // 3) 마지막으로 회원 삭제
        memberRepository.delete(member);
    }
}
