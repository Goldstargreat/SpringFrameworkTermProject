package kr.ac.kopo.gnuyog.goldstarmall.controller;

import jakarta.servlet.http.HttpSession;
import kr.ac.kopo.gnuyog.goldstarmall.config.SessionConst;
import kr.ac.kopo.gnuyog.goldstarmall.domain.DailyDeal;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import kr.ac.kopo.gnuyog.goldstarmall.service.InquiryService;
import kr.ac.kopo.gnuyog.goldstarmall.service.ProductService;
import kr.ac.kopo.gnuyog.goldstarmall.service.ReviewService;
import kr.ac.kopo.gnuyog.goldstarmall.service.SearchSuggestionService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

// 홈 화면, 상품 목록 화면, 상품 상세 화면 컨트롤러
// model 에 담은 값은 Thymeleaf 화면에서 ${이름} 으로 꺼내 쓴다
@Controller
public class HomeController {

    // 페이지 번호 버튼을 최대 몇 개까지 보여 줄지 (예: 3 4 [5] 6 7)
    private static final int PAGE_BUTTON_COUNT = 5;

    private final ProductService productService;
    private final ReviewService reviewService;
    private final InquiryService inquiryService;
    private final SearchSuggestionService searchSuggestionService;

    public HomeController(ProductService productService, ReviewService reviewService, InquiryService inquiryService,
                          SearchSuggestionService searchSuggestionService) {
        this.productService = productService;
        this.reviewService = reviewService;
        this.inquiryService = inquiryService;
        this.searchSuggestionService = searchSuggestionService;
    }

    // ===== 홈 화면 =====

    // 홈 화면 : 배너, 카테고리 바로가기, 오늘의 할인 / 인기 상품 / 로켓 배송 / 신상품 / 품절 임박 섹션
    @GetMapping("/")
    public String home(@RequestParam(name = "category", required = false) String category,
                       @RequestParam(name = "keyword", required = false) String keyword,
                       @RequestParam(name = "page", required = false) String page,
                       Model model) {
        // 예전 주소(/?category=식품, /?keyword=라면)로 들어오면 상품 목록 화면으로 보낸다 (조건은 그대로)
        if (category != null || keyword != null || page != null) {
            return "redirect:" + oldHomeLinkToProductList(category, keyword, page);
        }

        model.addAttribute("categories", productService.getCategories());
        // 오늘의 할인 : 배너 문구, 주간 할인표(월~일), 할인 상품
        model.addAttribute("todayDeal", DailyDeal.today());
        model.addAttribute("weeklyDeals", DailyDeal.values());
        model.addAttribute("dealProducts", productService.findTodayDealProducts());
        // 상품 섹션들
        model.addAttribute("popularProducts", productService.findPopularProducts());
        model.addAttribute("fastDeliveryProducts", productService.findFastDeliveryProducts());
        model.addAttribute("newProducts", productService.findNewProducts());
        model.addAttribute("lowStockProducts", productService.findLowStockProducts());
        return "home"; // templates/home.html
    }

    // 예전 홈 주소의 조건을 상품 목록 주소로 옮긴다  예) /?keyword=라면 → /products?keyword=라면
    private String oldHomeLinkToProductList(String category, String keyword, String page) {
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
        return link.encode().build().toUriString();
    }

    // ===== 상품 목록 화면 =====

    // 상품 목록 화면
    // 예) /products                      → 전체 상품
    //     /products?category=식품        → 식품만
    //     /products?keyword=라면         → 이름에 '라면'이 들어간 상품
    //     /products?category=식품&page=2 → 식품의 2페이지 (한 페이지에 ProductService.PAGE_SIZE 개)
    @GetMapping("/products")
    public String list(@RequestParam(name = "category", required = false) String category,
                       @RequestParam(name = "keyword", required = false) String keyword,
                       @RequestParam(name = "page", required = false) String page,
                       Model model) {
        int pageNumber = toPageNumber(page);
        Page<Product> productPage = productService.findProductPage(category, keyword, pageNumber);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("totalCount", productPage.getTotalElements());
        // 화면 상단 카테고리 메뉴와 지금 고른 조건
        model.addAttribute("categories", productService.getCategories());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("keyword", keyword);

        // 검색 결과가 없으면 비슷한 검색어를 추천한다 (예: '감치' → "혹시 '김치'를 찾으시나요?")
        if (hasText(keyword) && productPage.getTotalElements() == 0) {
            addSearchSuggestion(keyword, model);
        }

        addPageButtons(productPage, model);
        model.addAttribute("pageLinkBase", pageLinkBase(category, keyword));
        return "index"; // templates/index.html
    }

    // 주소의 page 값을 페이지 번호로 바꾼다
    // 없거나 숫자가 아니면 1페이지로 본다 (주소창에 page=abc 를 쳐도 오류가 나지 않게)
    private int toPageNumber(String page) {
        if (page == null) {
            return 1;
        }
        try {
            return Integer.parseInt(page.trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    // 추천 검색어와 그 뒤에 붙일 조사(을/를)를 담는다 (추천할 것이 없으면 아무것도 담지 않는다)
    private void addSearchSuggestion(String keyword, Model model) {
        String suggestion = searchSuggestionService.suggest(keyword);
        if (suggestion == null) {
            return;
        }
        model.addAttribute("suggestion", suggestion);
        model.addAttribute("suggestionParticle", SearchSuggestionService.objectParticle(suggestion));
    }

    // 페이지 번호 버튼 : 지금 페이지를 가운데에 두고 최대 PAGE_BUTTON_COUNT 개까지 보여 준다
    // 예) 전체 10쪽, 지금 5쪽 → 3 4 [5] 6 7 / 지금 1쪽 → [1] 2 3 4 5 / 지금 10쪽 → 6 7 8 9 [10]
    private void addPageButtons(Page<Product> productPage, Model model) {
        int currentPage = productPage.getNumber() + 1;    // Spring 은 0부터 세므로 화면용으로 1을 더한다
        int totalPages = productPage.getTotalPages();

        // 1) 지금 페이지 앞에 2개를 둔다 (1보다 작아지면 1부터)
        int startPage = Math.max(1, currentPage - PAGE_BUTTON_COUNT / 2);
        // 2) 시작부터 5개를 놓는다 (마지막 페이지를 넘으면 마지막까지만)
        int endPage = Math.min(totalPages, startPage + PAGE_BUTTON_COUNT - 1);
        // 3) 끝에 가까워서 5개가 안 되면 시작을 앞으로 당긴다
        startPage = Math.max(1, endPage - PAGE_BUTTON_COUNT + 1);

        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
    }

    // 페이지 버튼 링크의 앞부분 : 뒤에 페이지 번호만 이어 붙이면 된다
    // 예) "/products?category=식품&page=" (한글은 %EC%8B%9D... 처럼 인코딩된다)
    // 검색어가 있으면 검색어로, 없으면 카테고리로 찾으므로 둘 중 하나만 붙인다 (ProductService 와 같은 순서)
    private String pageLinkBase(String category, String keyword) {
        UriComponentsBuilder link = UriComponentsBuilder.fromPath("/products");
        if (hasText(keyword)) {
            link.queryParam("keyword", keyword.trim());
        } else if (hasText(category)) {
            link.queryParam("category", category);
        }
        link.queryParam("page", "");
        return link.encode().build().toUriString();
    }

    // 값이 있는지 (null 이거나 공백뿐이면 없는 것)
    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    // ===== 상품 상세 화면 =====

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
        boolean isAdmin = "ADMIN".equals(session.getAttribute(SessionConst.LOGIN_MEMBER_ROLE));
        model.addAttribute("loginMemberId", loginMemberId);
        model.addAttribute("isAdmin", isAdmin);

        // 상품평 탭 : 목록, 평균 별점, 작성할 수 없는 이유 (작성할 수 있으면 null)
        model.addAttribute("reviews", reviewService.findReviews(product));
        model.addAttribute("averageRating", reviewService.averageRating(product));
        model.addAttribute("reviewBlockedReason", reviewService.cannotWriteReason(loginMemberId, product));

        // 상품문의 탭 : 목록
        model.addAttribute("inquiries", inquiryService.findInquiries(product));
        return "product/detail"; // templates/product/detail.html
    }
}
