package kr.ac.kopo.gnuyog.goldstarmall.service;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Order;
import kr.ac.kopo.gnuyog.goldstarmall.exception.BusinessException;
import kr.ac.kopo.gnuyog.goldstarmall.exception.NotFoundException;
import kr.ac.kopo.gnuyog.goldstarmall.repository.CartItemRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.InquiryRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.MemberRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.OrderRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.ReviewRepository;
import kr.ac.kopo.gnuyog.goldstarmall.validator.MemberValidator;
import org.springframework.data.domain.Sort;
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
    private final ReviewRepository reviewRepository;
    private final InquiryRepository inquiryRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // 생성자 주입 : 스프링이 필요한 객체를 알아서 넣어 준다
    public MemberService(MemberRepository memberRepository,
                         CartItemRepository cartItemRepository,
                         OrderRepository orderRepository,
                         ReviewRepository reviewRepository,
                         InquiryRepository inquiryRepository,
                         BCryptPasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.inquiryRepository = inquiryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원가입
    public Member join(String loginId, String password, String name, String phone, String address) {
        // 1) 아이디 중복 검사
        Member found = memberRepository.findByLoginId(loginId);
        if (found != null) {
            throw new BusinessException("이미 사용 중인 아이디입니다.");
        }

        // 2) 전화번호 중복 검사 (이름은 같을 수 있어도 전화번호는 사람마다 다르다)
        phone = MemberValidator.normalizePhone(phone);
        checkDuplicatePhone(phone, null);

        // 3) 회원 객체를 만들고 값 채우기
        Member member = new Member();
        member.setLoginId(loginId);
        // 비밀번호는 그대로 저장하지 않고 BCrypt 로 암호화해서 저장한다
        member.setPassword(passwordEncoder.encode(password));
        member.setName(name);
        member.setPhone(phone);
        member.setAddress(address);
        member.setRole("USER");
        member.setCreatedAt(LocalDateTime.now());

        // 4) DB에 저장 (insert into members ...)
        return memberRepository.save(member);
    }

    // 아이디 사용 가능 여부 (회원가입 화면의 "중복 확인" 버튼에서 사용)
    @Transactional(readOnly = true)
    public boolean isLoginIdAvailable(String loginId) {
        return memberRepository.findByLoginId(loginId) == null;
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

    // 내 정보 수정 : 이름, 전화번호, 기본 배송지, 추가 배송지 (아이디는 바꿀 수 없다)
    public void updateInfo(Long memberId, String name, String phone, String address, String secondAddress) {
        MemberValidator.validateName(name);
        phone = MemberValidator.normalizePhone(phone);
        // 내 번호 그대로 두는 건 괜찮고, 다른 회원이 쓰는 번호로 바꾸는 것만 막는다
        checkDuplicatePhone(phone, memberId);
        Member member = findById(memberId);
        // 일반 회원은 "관리자" 같은 이름으로 바꿀 수 없다 (관리자 계정은 원래 이름이 "관리자"라서 검사하지 않는다)
        if (!"ADMIN".equals(member.getRole())) {
            MemberValidator.checkReservedName(name);
        }
        // 트랜잭션 안에서 값을 바꾸면 JPA 가 알아서 update SQL 을 실행한다 (변경 감지)
        member.setName(name.trim());
        member.setPhone(phone);
        member.setAddress(address);
        member.setSecondAddress(secondAddress);
    }

    // 전화번호 중복 검사 : 다른 회원이 이미 쓰는 번호면 예외
    // myId : 정보 수정일 때 내 회원 번호 (회원가입일 때는 null)
    private void checkDuplicatePhone(String phone, Long myId) {
        Member found = memberRepository.findByPhone(phone);
        if (found != null && !found.getId().equals(myId)) {
            throw new BusinessException("이미 가입된 전화번호입니다.");
        }
    }

    // ===== 아이디 찾기 / 비밀번호 찾기 =====
    // 이메일, 문자 발송 기능이 없으므로 "이름 + 전화번호"로 본인 확인을 한다
    // 전화번호는 회원마다 다르므로(중복 불가) 전화번호로 회원 한 명을 정확히 찾을 수 있다

    // 아이디 찾기 : 이름과 전화번호가 맞으면 아이디를 일부 가려서 돌려준다 (예: goldstar → gold****)
    @Transactional(readOnly = true)
    public String findLoginId(String name, String phone) {
        Member member = findByNameAndPhone(name, phone);
        return maskLoginId(member.getLoginId());
    }

    // 비밀번호 찾기 1단계 : 아이디, 이름, 전화번호가 모두 맞는지 확인하고 회원 번호를 돌려준다
    @Transactional(readOnly = true)
    public Long verifyForPasswordReset(String loginId, String name, String phone) {
        Member member = findByNameAndPhone(name, phone);
        if (loginId == null || !member.getLoginId().equals(loginId.trim())) {
            throw new BusinessException("입력한 정보와 일치하는 회원이 없습니다.");
        }
        // 관리자 계정은 이름/전화번호만으로 비밀번호를 바꿀 수 없게 막는다
        if ("ADMIN".equals(member.getRole())) {
            throw new BusinessException("관리자 계정은 비밀번호 찾기를 이용할 수 없습니다.");
        }
        return member.getId();
    }

    // 비밀번호 찾기 2단계 : 본인 확인을 통과한 회원의 비밀번호를 새로 정한다
    public void resetPassword(Long memberId, String newPassword, String newPasswordConfirm) {
        Member member = findById(memberId);
        if (member == null) {
            throw new NotFoundException("회원을 찾을 수 없습니다.");
        }
        MemberValidator.validatePassword(newPassword, newPasswordConfirm);
        member.setPassword(passwordEncoder.encode(newPassword));
    }

    // 이름 + 전화번호로 회원 찾기 (없으면 예외)
    private Member findByNameAndPhone(String name, String phone) {
        MemberValidator.validateName(name);
        Member member = memberRepository.findByPhone(MemberValidator.normalizePhone(phone));
        if (member == null || !member.getName().equals(name.trim())) {
            // 이름이 틀렸는지 전화번호가 틀렸는지는 알려 주지 않는다 (남의 정보를 알아내는 데 쓰이지 않도록)
            throw new BusinessException("입력한 정보와 일치하는 회원이 없습니다.");
        }
        return member;
    }

    // 아이디 가리기 : 앞쪽 절반만 보여 주고 나머지는 * 로 바꾼다 (최소 2글자는 보여 준다)
    // 예) goldstar → gold****, admin → ad***, test → te**
    private String maskLoginId(String loginId) {
        int visible = Math.max(2, loginId.length() / 2);
        return loginId.substring(0, visible) + "*".repeat(loginId.length() - visible);
    }

    // 비밀번호 변경 : 현재 비밀번호가 맞아야 바꿀 수 있다
    public void changePassword(Long memberId, String currentPassword, String newPassword, String newPasswordConfirm) {
        Member member = findById(memberId);
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new BusinessException("현재 비밀번호가 올바르지 않습니다.");
        }
        MemberValidator.validatePassword(newPassword, newPasswordConfirm);
        member.setPassword(passwordEncoder.encode(newPassword));
    }

    // 회원 탈퇴 : 본인 확인(비밀번호) 후 회원 삭제
    public void withdraw(Long memberId, String password) {
        Member member = findById(memberId);
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new BusinessException("비밀번호가 올바르지 않습니다.");
        }
        deleteMember(memberId);
    }

    // (관리자) 전체 회원 목록
    @Transactional(readOnly = true)
    public List<Member> findAllMembers() {
        return memberRepository.findAll();
    }

    // (관리자) 회원 검색 : 검색 기준(type)에 따라 일부만 맞아도 찾는다
    //   all   : 이름, 아이디, 전화번호 중 하나라도 맞으면
    //   name  : 이름
    //   loginId : 아이디
    //   phone : 전화번호 (숫자만 뽑아서 비교, 숫자가 하나도 없으면 결과 없음)
    // 검색어가 비어 있으면 전체 목록
    @Transactional(readOnly = true)
    public List<Member> searchMembers(String type, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            // 회원번호 순서로 (화면의 순번이 항상 같은 순서로 붙도록)
            return memberRepository.findAll(Sort.by("id"));
        }
        String text = keyword.trim();
        String digits = text.replaceAll("[^0-9]", "");

        if ("name".equals(type)) {
            return memberRepository.findByNameContainingOrderByIdAsc(text);
        }
        if ("loginId".equals(type)) {
            return memberRepository.findByLoginIdContainingOrderByIdAsc(text);
        }
        if ("phone".equals(type)) {
            if (digits.isEmpty()) {
                return List.of();
            }
            return memberRepository.searchByPhoneDigits(digits);
        }
        return memberRepository.searchAll(text, digits);
    }

    // 회원 삭제 : 회원 탈퇴와 관리자의 회원 삭제가 함께 쓴다
    // 관리자 계정이거나, 배송이 끝나지 않은 주문이 있으면 삭제할 수 없다
    public void deleteMember(Long memberId) {
        Member member = findById(memberId);
        if (member == null) {
            throw new NotFoundException("회원을 찾을 수 없습니다.");
        }
        if ("ADMIN".equals(member.getRole())) {
            throw new BusinessException("관리자 계정은 삭제할 수 없습니다.");
        }

        // 1) 진행 중인 주문(배송완료, 주문취소가 아닌 주문)이 있는지 확인
        List<Order> orders = orderRepository.findByMemberOrderByIdDesc(member);
        for (Order order : orders) {
            if (!order.getStatus().isFinished()) {
                throw new BusinessException("아직 배송이 끝나지 않은 주문이 있습니다. 배송이 끝난 뒤 다시 시도해 주세요.");
            }
        }

        // 2) 회원을 가리키는 데이터부터 지운다 (외래키 때문에 순서가 중요하다)
        //    주문을 지우면 cascade 설정으로 주문 상품도 함께 지워진다
        cartItemRepository.deleteAll(cartItemRepository.findByMember(member));
        reviewRepository.deleteAll(reviewRepository.findByMember(member));
        inquiryRepository.deleteAll(inquiryRepository.findByMember(member));
        orderRepository.deleteAll(orders);

        // 3) 마지막으로 회원 삭제
        memberRepository.delete(member);
    }
}
