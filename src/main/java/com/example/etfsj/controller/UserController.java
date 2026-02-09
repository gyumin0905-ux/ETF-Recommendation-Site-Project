package com.example.etfsj.controller;

import com.example.etfsj.domain.EtfMeta;
import com.example.etfsj.domain.Event;
import com.example.etfsj.domain.User;
import com.example.etfsj.dto.EtfListDto;
import com.example.etfsj.repository.UserRepository;
import com.example.etfsj.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor // [규민] 생성자 주입 (Autowired 생략 가능)
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    private MyPageService myPageService;

    @Autowired
    private EventService eventService;

    @Autowired
    private UserRepository userRepository; //  [규민] 중복 확인용 리포지토리 주입

    private final BoardService boardService; // [규민] 게시판 서비스 추가

    private final EtfCompanyService etfCompanyService;

    private final NoticeService noticeService;

    // ===============================
    //  [규민] 아이디 중복 확인 API
    // 프론트(JS)에서 호출: /api/check-username?username=abc
    // ===============================
    @GetMapping("/api/check-username")
    @ResponseBody
    public boolean checkUsername(@RequestParam String username) {
        // 있으면 true(중복), 없으면 false(사용가능)
        return userRepository.existsByUsername(username);
    }

    // ===============================
    // 메인 페이지
    // ===============================
    @GetMapping("/")
    public String index(HttpSession session, Model model) {

        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser != null) {
            model.addAttribute("loginUser", loginUser);
        }

        // 이벤트
        List<Event> events = eventService.getEventList();
        model.addAttribute("latestEvents", events.stream().limit(4).toList());

        // ✅ 추천 검색어 (ETF 회사명 랜덤 5개)
        model.addAttribute("recommendCompanies",
                etfCompanyService.getRandomCompanies(5));

        model.addAttribute("bannerEtfs", searchService.getBannerEtfs());

        model.addAttribute("latestNews", noticeService.getLatestNews());

        // 🔥 추천 ETF (테마별 4개)
        model.addAttribute("recommendOverseas",
                searchService.getRecommendByTheme("해외주식", 6));

        model.addAttribute("recommendDomestic",
                searchService.getRecommendByTheme("국내주식", 6));

        model.addAttribute("recommendBond",
                searchService.getRecommendByTheme("채권/금리", 6));

        model.addAttribute("recommendSafe",
                searchService.getRecommendSafe(6));

        return "index";
    }

    // ===============================
    // ETF 목록 / 검색 + 페이징
    // ===============================
    @GetMapping("/etf/list")
    public String etfList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String theme,
            @RequestParam(required = false) Integer riskLevel,
            @RequestParam(required = false) String expenseLevel,
            HttpSession session,
            Model model
    ) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser != null) model.addAttribute("loginUser", loginUser);

        Pageable pageable = PageRequest.of(page, size);

        // 🔥 이 부분 추가 (Pageable 생성 전 or 후 상관 없음)
        keyword = (keyword != null && keyword.isBlank()) ? null : keyword;
        theme = (theme != null && theme.isBlank()) ? null : theme;
        expenseLevel = (expenseLevel != null && expenseLevel.isBlank()) ? null : expenseLevel;

        Page<EtfListDto> result = searchService.searchList(
                keyword,
                theme,
                riskLevel,
                expenseLevel,
                pageable
        );

        int blockSize = 10;
        int currentPage = result.getNumber();     // 0-base
        int totalPages  = result.getTotalPages();

        int startPage = (currentPage / blockSize) * blockSize;
        int endPage   = Math.min(startPage + blockSize - 1, totalPages - 1);

        model.addAttribute("list", result.getContent());
        model.addAttribute("page", result);

        model.addAttribute("currentPage", currentPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("totalPages", totalPages);

        model.addAttribute("keyword", keyword);
        model.addAttribute("theme", theme);
        model.addAttribute("riskLevel", riskLevel);
        model.addAttribute("expenseLevel", expenseLevel);

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("keyword", keyword);
        queryParams.put("theme", theme);
        queryParams.put("riskLevel", riskLevel);
        queryParams.put("expenseLevel", expenseLevel);

        model.addAttribute("queryParams", queryParams);

        model.addAttribute("themes", List.of(
                "국내주식","단기자금/MMF","원자재",
                "채권/금리","파생(인버스)",
                "해외주식","해외주식(미국)"
        ));

        return "etf-list";
    }

    // ===============================
    // 로그인
    // ===============================
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            Model model,
            HttpSession session
    ) {
        try {
            User loginUser = userService.login(email, password);
            session.setAttribute("loginUser", loginUser);
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("loginError", e.getMessage());
            return "login";
        }
    }

    // ===============================
    // 회원가입 (기본 정보만 → 바로 index)
    // ===============================
    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model
    ) {
        try {
            User user = userService.signupAndReturn(username, email, password);
            session.setAttribute("loginUser", user);
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("signupError", e.getMessage());
            return "signup";
        }
    }

    // ===============================
    // 투자 성향 / ETF 지식 설정 (언제든)
    // ===============================
    @GetMapping("/settings/investment")
    public String investmentSettings(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        model.addAttribute("user", loginUser);
        return "investment_profile";
    }

    @PostMapping("/settings/investment")
    public String updateInvestmentSettings(
            @RequestParam String experience,
            @RequestParam String asset,
            @RequestParam String goal,
            @RequestParam String risk,
            @RequestParam String knowledge,
            HttpSession session
    ) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        userService.updateSurvey(
                loginUser.getId(),
                experience,
                asset,
                goal,
                risk,
                knowledge
        );

        // 세션 최신화
        session.setAttribute(
                "loginUser",
                userService.findById(loginUser.getId())
        );

        return "redirect:/";
    }

    // ===============================
    // 로그아웃
    // ===============================
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // ===============================
    // 마이페이지
    // ===============================
    @GetMapping("/mypage")
    public String mypage(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        model.addAttribute("user", loginUser);
        // 1. 내가 북마크한 ETF
        model.addAttribute(
                "bookmarkedEtfs",
                myPageService.getBookmarkedEtfs(loginUser.getId())
        );

        // 2. [규민] 내가 쓴 글 목록 추가
        model.addAttribute(
                "myBoards",
                boardService.getMyBoardList(loginUser.getId())
        );

        return "mypage";
    }

    @GetMapping("/mypage/edit")
    public String editMyPage(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        model.addAttribute("user", loginUser);
        return "mypage_edit";
    }

    @PostMapping("/mypage/edit")
    public String updateMyPage(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam(required = false) String currentPassword,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String confirmPassword,
            @RequestParam String experience,
            @RequestParam String asset,
            @RequestParam String goal,
            @RequestParam String risk,
            @RequestParam String knowledge,
            HttpSession session,
            Model model
    ) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        try {
            userService.updateUserWithPasswordCheck(
                    loginUser.getId(),
                    username,
                    email,
                    currentPassword,
                    newPassword,
                    confirmPassword,
                    experience,
                    asset,
                    goal,
                    risk,
                    knowledge
            );

            session.setAttribute(
                    "loginUser",
                    userService.findById(loginUser.getId())
            );

            return "redirect:/mypage?success";

        } catch (IllegalArgumentException e) {

            model.addAttribute("user", loginUser);

            switch (e.getMessage()) {

                case "CURRENT_PASSWORD_REQUIRED":
                    model.addAttribute(
                            "currentPasswordError",
                            "현재 비밀번호를 입력해주세요"
                    );
                    break;

                case "CURRENT_PASSWORD_MISMATCH":
                    model.addAttribute(
                            "currentPasswordError",
                            "현재 비밀번호가 다릅니다"
                    );
                    break;

                case "NEW_PASSWORD_INVALID":
                    model.addAttribute(
                            "confirmPasswordError",
                            "새 비밀번호와 새 비밀번호 확인을 확인 해주세요"
                    );
                    break;

                default:
                    model.addAttribute(
                            "error",
                            "정보 수정 중 오류가 발생했습니다."
                    );
            }

            return "mypage_edit";
        }
    }
}
