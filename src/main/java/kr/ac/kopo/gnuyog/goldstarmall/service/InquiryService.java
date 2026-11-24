package kr.ac.kopo.gnuyog.goldstarmall.service;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Inquiry;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import kr.ac.kopo.gnuyog.goldstarmall.exception.BusinessException;
import kr.ac.kopo.gnuyog.goldstarmall.exception.NotFoundException;
import kr.ac.kopo.gnuyog.goldstarmall.repository.InquiryRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.MemberRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// 상품문의 서비스 : 문의 조회, 질문 쓰기, (관리자) 답변, 삭제
@Service
@Transactional
public class InquiryService {

    // 질문/답변 최대 글자 수 (Inquiry 의 컬럼 길이)
    public static final int TEXT_MAX_LENGTH = 500;

    private final InquiryRepository inquiryRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    public InquiryService(InquiryRepository inquiryRepository,
                          MemberRepository memberRepository,
                          ProductRepository productRepository) {
        this.inquiryRepository = inquiryRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
    }

    // (관리자) 모든 문의 (최신순). pendingOnly 면 답변을 아직 안 단 것만
    @Transactional(readOnly = true)
    public List<Inquiry> findAllForAdmin(boolean pendingOnly) {
        return pendingOnly ? inquiryRepository.findByAnswerIsNullOrderByIdDesc()
                           : inquiryRepository.findAllByOrderByIdDesc();
    }

    // (관리자) 답변을 아직 안 단 문의 개수
    @Transactional(readOnly = true)
    public long countPending() {
        return inquiryRepository.countByAnswerIsNull();
    }

    // 한 상품의 문의 목록 (최신순)
    @Transactional(readOnly = true)
    public List<Inquiry> findInquiries(Product product) {
        return inquiryRepository.findByProductOrderByIdDesc(product);
    }

    // 질문 쓰기 : 로그인한 일반 회원이면 누구나 쓸 수 있다 (관리자는 질문하지 않고 답변한다)
    public Inquiry ask(Long memberId, Long productId, String question) {
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            throw new NotFoundException("회원 정보를 찾을 수 없습니다.");
        }
        if ("ADMIN".equals(member.getRole())) {
            throw new BusinessException("관리자는 상품문의를 쓸 수 없습니다. 회원의 문의에 답변해 주세요.");
        }
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            throw new NotFoundException("상품을 찾을 수 없습니다.");
        }
        String text = checkText(question, "문의 내용");

        Inquiry inquiry = new Inquiry();
        inquiry.setMember(member);
        inquiry.setProduct(product);
        inquiry.setQuestion(text);
        inquiry.setCreatedAt(LocalDateTime.now());
        return inquiryRepository.save(inquiry);
    }

    // (관리자) 답변 달기 : 이미 답변이 있으면 새 답변으로 고친다. 상품 번호를 돌려준다
    public Long answer(Long inquiryId, String answer) {
        Inquiry inquiry = findInquiry(inquiryId);
        inquiry.setAnswer(checkText(answer, "답변 내용"));
        inquiry.setAnsweredAt(LocalDateTime.now());
        return inquiry.getProduct().getId();
    }

    // 문의 삭제 : 질문한 본인이나 관리자만 지울 수 있다. 상품 번호를 돌려준다
    public Long delete(Long memberId, boolean admin, Long inquiryId) {
        Inquiry inquiry = findInquiry(inquiryId);
        if (!admin && !inquiry.getMember().getId().equals(memberId)) {
            throw new BusinessException("본인이 쓴 문의만 삭제할 수 있습니다.");
        }
        Long productId = inquiry.getProduct().getId();
        inquiryRepository.delete(inquiry);
        return productId;
    }

    private Inquiry findInquiry(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId).orElse(null);
        if (inquiry == null) {
            throw new NotFoundException("문의를 찾을 수 없습니다.");
        }
        return inquiry;
    }

    // 글 검사 : 비어 있지 않고 최대 글자 수 이하. 앞뒤 공백을 지운 글을 돌려준다
    private String checkText(String text, String label) {
        if (text == null || text.trim().isEmpty()) {
            throw new BusinessException(label + "을 입력해 주세요.");
        }
        if (text.trim().length() > TEXT_MAX_LENGTH) {
            throw new BusinessException(label + "은 " + TEXT_MAX_LENGTH + "글자까지 쓸 수 있습니다.");
        }
        return text.trim();
    }
}
