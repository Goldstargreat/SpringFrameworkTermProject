package kr.ac.kopo.gnuyog.goldstarmall.controller;

import kr.ac.kopo.gnuyog.goldstarmall.domain.DailyDeal;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import kr.ac.kopo.gnuyog.goldstarmall.config.SessionConst;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Inquiry;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Review;
import kr.ac.kopo.gnuyog.goldstarmall.service.InquiryService;
import kr.ac.kopo.gnuyog.goldstarmall.service.ProductService;
import kr.ac.kopo.gnuyog.goldstarmall.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

// 홈 화면, 상품 목록 화면, 상품 상세 화면 컨트롤러
@Controller
public class HomeController {

    private final ProductService productService;
    private final ReviewService reviewService;
    private final InquiryService inquiryService;

    public HomeController(ProductService productService, ReviewService reviewService, InquiryService inquiryService) {
        this.productService = productService;
        this.reviewService = reviewService;
        this.inquiryService = inquiryService;
    }

    // 홈 화면 : 배너, 카테고리 바로가기, 인기 상품 / 로켓 배송 / 신상품 / 품절 임박 섹션
    // 예전 주소(/?category=식품, /?keyword=라면)로 들어오면 상품 목록 화면으로 보낸다 (조건은 그대로)
    @GetMapping("/")
    public String home(@RequestParam(name = "category", required = false) String category,
                       @RequestParam(name = "keyword", required = false) String keyword,
                       @RequestParam(name = "page", required = false) String page,
                       Model model) {
        if (category != null || keyword != null || page != null) {
            UriComponentsBuilder link = UriComponentsBuilder.fromPath("/products");
            if (category != null) {
                link.queryParam("category", category);
            }
            if (keyword != null) {
                link.queryParam("keyword", keyword);
            }
            if (page != null) {
                link.queryParam("page", page);
            }
            return "redirect:" + link.encode().build().toUriString();
        }

        model.addAttribute("categories", productService.getCategories());
        // 오늘의 할인 : 배너 문구, 주간 할인표(월~일), 할인 상품 섹션
        model.addAttribute("todayDeal", DailyDeal.today());
        model.addAttribute("weeklyDeals", DailyDeal.values());
        model.addAttribute("dealProducts", productService.findTodayDealProducts());
        model.addAttribute("popularProducts", productService.findPopularProducts());
        model.addAttribute("fastDeliveryProducts", productService.findFastDeliveryProducts());
        model.addAttribute("newProducts", productService.findNewProducts());
        model.addAttribute("lowStockProducts", productService.findLowStockProducts());
        return "home"; // templates/home.html
    }

    // 상품 목록 화면
    // 예) /products                    → 전체 상품
    //     /products?category=식품      → 식품만
    //     /products?keyword=라면       → 이름에 '라면'이 들어간 상품
    //     /products?category=식품&page=2 → 식품의 2페이지 (한 페이지에 ProductService.PAGE_SIZE 개)
    @GetMapping("/products")
    public String list(@RequestParam(name = "category", required = false) String category,
                       @RequestParam(name = "keyword", required = false) String keyword,
                       @RequestParam(name = "page", required = false) String page,
                       Model model) {
        // page 는 숫자가 아니거나 없으면 1페이지로 본다 (주소창에 page=abc 를 쳐도 오류가 나지 않게)
        int pageNumber = 1;
        try {
            if (page != null) {
                pageNumber = Integer.parseInt(page.trim());
            }
        } catch (NumberFormatException e) {
            pageNumber = 1;
        }

        Page<Product> productPage = productService.findProductPage(category, keyword, pageNumber);
        List<Product> products = productPage.getContent();

        // 페이지 번호 버튼 : 지금 페이지를 가운데에 두고 최대 5개까지 보여 준다 (예: 3 4 [5] 6 7)
        int currentPage = productPage.getNumber() + 1;          // 0부터 세는 번호를 1부터로
        int totalPages = productPage.getTotalPages();
        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(totalPages, startPage + 4);
        startPage = Math.max(1, endPage - 4);

        // 페이지 버튼 링크의 앞부분 : 값이 있는 조건(카테고리, 검색어)만 붙인다
        // 예) "/products?category=식품&page=" 뒤에 페이지 번호만 이어 붙이면 된다 (한글은 %EC%8B%9D... 처럼 인코딩)
        UriComponentsBuilder link = UriComponentsBuilder.fromPath("/products");
        if (keyword != null && !keyword.trim().isEmpty()) {
            link.queryParam("keyword", keyword.trim());
        } else if (category != null && !category.trim().isEmpty()) {
            link.queryParam("category", category);
        }
        String pageLinkBase = link.queryParam("page", "").encode().build().toUriString();

        // 화면 상단에 보여 줄 카테고리 메뉴
        List<String> categories = productService.getCategories();

        // model 에 담은 값은 Thymeleaf 화면에서 ${이름} 으로 꺼내 쓴다
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("keyword", keyword);
        // 페이지 나누기 정보
        model.addAttribute("totalCount", productPage.getTotalElements());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("pageLinkBase", pageLinkBase);
        return "index"; // templates/index.html
    }

    // 상품 상세 화면
    @GetMapping("/products/{productId}")
    public String productDetail(@PathVariable("productId") Long productId,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Product product = productService.findById(productId);
        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "상품을 찾을 수 없습니다.");
            return "redirect:/";
        }
        model.addAttribute("product", product);

        // 로그인한 회원 정보 (상품평/문의의 삭제 버튼, 관리자 답변 폼을 보여 줄지 정한다)
        Long loginMemberId = (Long) session.getAttribute(SessionConst.LOGIN_MEMBER_ID);
        model.addAttribute("loginMemberId", loginMemberId);
        model.addAttribute("isAdmin", "ADMIN".equals(session.getAttribute(SessionConst.LOGIN_MEMBER_ROLE)));

        // 상품평 탭 : 목록, 평균 별점, 작성할 수 없는 이유(작성할 수 있으면 null)
        List<Review> reviews = reviewService.findReviews(product);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", reviewService.averageRating(product));
        model.addAttribute("reviewBlockedReason", reviewService.cannotWriteReason(loginMemberId, product));

        // 상품문의 탭 : 목록
        List<Inquiry> inquiries = inquiryService.findInquiries(product);
        model.addAttribute("inquiries", inquiries);
        return "product/detail"; // templates/product/detail.html
    }
}
