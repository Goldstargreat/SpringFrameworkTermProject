package kr.ac.kopo.gnuyog.goldstarmall.service;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.repository.MemberRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// 회원 서비스 : 회원가입, 로그인 처리
@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // 생성자 주입 : 스프링이 필요한 객체를 알아서 넣어 준다
    public MemberService(MemberRepository memberRepository, BCryptPasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
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
}
