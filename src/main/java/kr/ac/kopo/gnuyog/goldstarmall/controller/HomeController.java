package kr.ac.kopo.gnuyog.goldstarmall.controller;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import kr.ac.kopo.gnuyog.goldstarmall.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

// 메인 화면(상품 목록)과 상품 상세 화면 컨트롤러
@Controller
public class HomeController {

    private final ProductService productService;

    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    // 메인 화면 : 상품 목록
    // 예) /                  → 전체 상품
    //     /?category=식품    → 식품만
    //     /?keyword=라면     → 이름에 '라면'이 들어간 상품
    @GetMapping("/")
    public String home(@RequestParam(name = "category", required = false) String category,
                       @RequestParam(name = "keyword", required = false) String keyword,
                       Model model) {
        List<Product> products = productService.findProducts(category, keyword);

        // 화면 상단에 보여 줄 카테고리 메뉴
        List<String> categories = new ArrayList<String>();
        categories.add("식품");
        categories.add("생활용품");
        categories.add("가전");
        categories.add("패션");

        // model 에 담은 값은 Thymeleaf 화면에서 ${이름} 으로 꺼내 쓴다
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("keyword", keyword);
        return "index"; // templates/index.html
    }

    // 상품 상세 화면
    @GetMapping("/products/{productId}")
    public String productDetail(@PathVariable("productId") Long productId, Model model, RedirectAttributes redirectAttributes) {
        Product product = productService.findById(productId);
        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "상품을 찾을 수 없습니다.");
            return "redirect:/";
        }
        model.addAttribute("product", product);
        return "product/detail"; // templates/product/detail.html
    }
}
