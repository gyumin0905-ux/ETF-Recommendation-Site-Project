package com.example.etfsj.controller;

import com.example.etfsj.domain.*;
import com.example.etfsj.repository.EtfMetaRepository; // [규민] 추가: 종목 정보 리포지토리
import com.example.etfsj.repository.EtfPriceRepository;
import com.example.etfsj.repository.UserRepository;
import com.example.etfsj.service.EventService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    // [규민] ETF 시세 데이터를 가져오기 위해 리포지토리 연결
    private final EtfPriceRepository etfPriceRepository;

    // [규민] ETF 종목 정보(Meta)를 가져오기 위해 리포지토리 연결 (새로 추가됨)
    private final EtfMetaRepository etfMetaRepository;

    // [규민] 이벤트 관리를 위한 서비스 연결
    private final EventService eventService;

    /**
     * [규민] 관리자 대시보드 메인 페이지
     */
    @GetMapping
    public String adminHome(HttpSession session, Model model) {
        // 1️⃣ 로그인 체크
        User sessionUser = (User) session.getAttribute("loginUser");
        if (sessionUser == null) return "redirect:/login";

        // 2️⃣ 관리자 권한 체크
        User currentUser = userRepository.findById(sessionUser.getId()).orElse(null);
        if (currentUser == null || !currentUser.isAdmin()) return "redirect:/";

        // ===============================
        // 3️⃣ 대시보드용 데이터 조회
        // ===============================

        // 🔹 최근 가입 회원 (최신 5명)
        List<User> recentUsers = userRepository
                .findTop5ByOrderByIdDesc();

        // 🔹 최근 추가된 ETF Meta (최신 5개)
        List<EtfMeta> recentMetas = etfMetaRepository
                .findTop5ByOrderByIsinCdDesc();

        // 🔹 최근 ETF 시세 (최신 5개)
        List<EtfPrice> recentPrices = etfPriceRepository
                .findTop5ByOrderByBasDtDesc();

        // 🔹 최근 이벤트 (최신 5개)
        List<Event> recentEvents = eventService
                .getRecentEvents(5);

        // ===============================
        // 4️⃣ Model에 담기 (index.html에서 사용)
        // ===============================
        model.addAttribute("users", recentUsers);
        model.addAttribute("metas", recentMetas);
        model.addAttribute("prices", recentPrices);
        model.addAttribute("events", recentEvents);

        model.addAttribute("currentUser", currentUser);

        // 5️⃣ 관리자 대시보드 페이지
        return "admin/index";
    }

    /**
     * [규민] 전체 회원 목록 조회 페이지
     */
    @GetMapping("/users")
    public String userList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String risk,
            @RequestParam(defaultValue = "0") int page,
            HttpSession session,
            Model model
    ) {
        User sessionUser = (User) session.getAttribute("loginUser");
        if (sessionUser == null) return "redirect:/login";

        User currentUser = userRepository.findById(sessionUser.getId()).orElse(null);
        if (currentUser == null || !currentUser.isAdmin()) return "redirect:/";

        // 🔥 빈 문자열 → null
        keyword = (keyword != null && !keyword.isBlank()) ? keyword : null;
        role = (role != null && !role.isBlank()) ? role : null;
        risk = (risk != null && !risk.isBlank()) ? risk : null;

        Pageable pageable = PageRequest.of(page, 20, Sort.by("id").descending());

        Page<User> resultPage =
                userRepository.searchUsers(keyword, role, risk, pageable);

        model.addAttribute("users", resultPage.getContent());
        model.addAttribute("page", resultPage);

        // 🔥 검색값 유지
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", role);
        model.addAttribute("risk", risk);
        model.addAttribute("currentUser", currentUser);

        return "admin/users";
    }

    /**
     *
     * [세영] 회원 수정 페이지 접근 및 수정 페이지
     */

    @GetMapping("/users/edit")
    public String editUser(
            @RequestParam("id") Long id,
            HttpSession session,
            Model model
    ) {
        // 1. 로그인/권한 체크
        User sessionUser = (User) session.getAttribute("loginUser");
        if (sessionUser == null) return "redirect:/login";

        User currentUser = userRepository.findById(sessionUser.getId()).orElse(null);
        if (currentUser == null || !currentUser.isAdmin()) return "redirect:/";

        // 2. 본인은 수정 불가 (원하면 막고 알림 주기)
        if (currentUser.getId().equals(id)) {
            // 그냥 목록으로 돌려보내기
            return "redirect:/admin/users";
        }

        // 3. 수정 대상 회원 조회 (UserRepository 활용)
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다. id=" + id));

        // 4. 모델에 담아서 폼으로 전달
        model.addAttribute("user", targetUser);
        model.addAttribute("currentUser", currentUser); // ★ 수정됨

        return "admin/user-edit";  // 🔥 새로 만들 edit용 템플릿
    }

    @PostMapping("/users/edit")
    public String updateUser(
            @RequestParam("id") Long id,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String userRole,
            @RequestParam String experience,
            @RequestParam String asset,
            @RequestParam String goal,
            @RequestParam String risk,
            @RequestParam String knowledge,
            HttpSession session,
            Model model
    ) {
        // 1. 로그인/권한 체크
        User sessionUser = (User) session.getAttribute("loginUser");
        if (sessionUser == null) return "redirect:/login";

        User currentUser = userRepository.findById(sessionUser.getId()).orElse(null);
        if (currentUser == null || !currentUser.isAdmin()) return "redirect:/";

        // 2. 자기 자신은 role 수정 금지
        boolean isSelf = currentUser.getId().equals(id);

        // 3. 수정 대상 조회
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다. id=" + id));

        // 4. 정보 수정
        targetUser.setUsername(username);
        targetUser.setEmail(email);
        targetUser.setExperience(experience);
        targetUser.setAsset(asset);
        targetUser.setGoal(goal);
        targetUser.setRisk(risk);
        targetUser.setKnowledge(knowledge);

        // 5. ROLE 변경 (본인은 수정 불가)
        if (!isSelf && userRole != null) {
            targetUser.setUserRole(userRole);
        }

        userRepository.save(targetUser);

        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete")
    public String deleteUser(@RequestParam Long id, HttpSession session) {
        User sessionUser = (User) session.getAttribute("loginUser");
        if (sessionUser == null) return "redirect:/login";

        User currentUser = userRepository.findById(sessionUser.getId()).orElse(null);
        if (currentUser == null || !currentUser.isAdmin()) return "redirect:/";

        if (currentUser.getId().equals(id)) return "redirect:/admin/users";

        userRepository.deleteById(id);

        return "redirect:/admin/users";
    }

    // ==========================================
    // [규민] ETF 시세(Price) 데이터 관리
    // ==========================================

    // ==========================================
    // [규민] ETF 시세(Price) 데이터 관리 // [신준] 날짜 기준 검색, 페이징 처리 추가
    // ==========================================

    @GetMapping("/etf")
    public String etfList(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String isin,
            @RequestParam(defaultValue = "0") int page,
            HttpSession session,
            Model model
    ) {
        User sessionUser = (User) session.getAttribute("loginUser");
        if (sessionUser == null) return "redirect:/login";

        User currentUser = userRepository.findById(sessionUser.getId()).orElse(null);
        if (currentUser == null || !currentUser.isAdmin()) return "redirect:/";

        Page<EtfPrice> resultPage;

        boolean hasDate =
                startDate != null && endDate != null &&
                        !startDate.isBlank() && !endDate.isBlank();

        boolean hasIsin =
                isin != null && !isin.isBlank();

        // ===============================
        // 🔥 날짜 문자열 정리 (필요 시)
        // ===============================
        String startDt = null;
        String endDt = null;

        if (hasDate) {
            startDt = startDate.replace("-", "");
            endDt   = endDate.replace("-", "");

            // 날짜가 거꾸로 들어오면 보정
            if (startDt.compareTo(endDt) > 0) {
                String tmp = startDt;
                startDt = endDt;
                endDt = tmp;

                String tmp2 = startDate;
                startDate = endDate;
                endDate = tmp2;
            }
        }

        // ===============================
        // 🔥 페이징 & 정렬
        // ===============================
        Pageable pageable;

        // ===============================
        // 🔥 조건 분기
        // ===============================

        // 1️⃣ 날짜 + ISIN
        if (hasDate && hasIsin) {

            pageable = PageRequest.of(page, 20, Sort.by("basDt").ascending());

            resultPage = etfPriceRepository
                    .findByBasDtBetweenAndIsinCd(startDt, endDt, isin, pageable);

        }
        // 2️⃣ 날짜만
        else if (hasDate) {

            pageable = PageRequest.of(page, 20, Sort.by("basDt").ascending());

            resultPage = etfPriceRepository
                    .findByBasDtBetween(startDt, endDt, pageable);

        }
        // 3️⃣ ISIN만
        else if (hasIsin) {

            pageable = PageRequest.of(page, 20, Sort.by("basDt").descending());

            resultPage = etfPriceRepository
                    .findByIsinCd(isin, pageable);

        }
        // 4️⃣ 전체 조회
        else {

            pageable = PageRequest.of(page, 20, Sort.by("basDt").descending());

            resultPage = etfPriceRepository.findAll(pageable);
        }

        // ===============================
        // 🔥 검색값 유지
        // ===============================
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("isin", isin);

        model.addAttribute("etfPrices", resultPage.getContent());
        model.addAttribute("page", resultPage);

        return "admin/etf";
    }

    @GetMapping("/etf/edit")
    public String editEtf(@RequestParam("isin") String isin,
                          @RequestParam("date") String date,
                          Model model) {
        EtfPriceId id = new EtfPriceId(date, isin);
        EtfPrice etfPrice = etfPriceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ETF 데이터가 없습니다."));

        model.addAttribute("etf", etfPrice);
        return "admin/etf-form";
    }

    @PostMapping("/etf/save")
    public String saveEtf(@ModelAttribute EtfPrice etfPrice) {
        etfPriceRepository.save(etfPrice);
        return "redirect:/admin/etf";
    }

    @GetMapping("/etf/delete")
    public String deleteEtf(@RequestParam("isin") String isin,
                            @RequestParam("date") String date) {
        EtfPriceId id = new EtfPriceId(date, isin);
        etfPriceRepository.deleteById(id);
        return "redirect:/admin/etf";
    }

    // ==========================================
    // [규민] ETF 종목 정보(Meta) 관리 기능 (여기부터 새로 추가함)
    // ==========================================

    /**
     * [규민] ETF 종목 정보 목록 조회
     */
    @GetMapping("/meta")
    public String metaList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String theme,
            @RequestParam(required = false) Integer riskLevel,
            @RequestParam(required = false) String expenseLevel,
            @RequestParam(defaultValue = "0") int page,
            HttpSession session,
            Model model
    ) {
        // 1️⃣ 로그인 / 권한 체크
        User sessionUser = (User) session.getAttribute("loginUser");
        if (sessionUser == null) return "redirect:/login";

        User currentUser = userRepository.findById(sessionUser.getId()).orElse(null);
        if (currentUser == null || !currentUser.isAdmin()) return "redirect:/";

        // 2️⃣ 페이징
        Pageable pageable = PageRequest.of(page, 20, Sort.by("isinCd").descending());

        // 3️⃣ 빈 문자열 → null 처리 (중요)
        keyword = (keyword != null && !keyword.isBlank()) ? keyword : null;
        theme = (theme != null && !theme.isBlank()) ? theme : null;
        expenseLevel = (expenseLevel != null && !expenseLevel.isBlank()) ? expenseLevel : null;

        // 4️⃣ 검색 실행
        Page<EtfMeta> resultPage = etfMetaRepository.searchEtfDetail(
                keyword, theme, riskLevel, expenseLevel, pageable
        );

        // 5️⃣ 셀렉트 박스용 데이터
        model.addAttribute("themes", etfMetaRepository.findDistinctThemes());

        // 6️⃣ 결과 + 검색값 유지
        model.addAttribute("etfMetas", resultPage.getContent());
        model.addAttribute("page", resultPage);

        model.addAttribute("keyword", keyword);
        model.addAttribute("theme", theme);
        model.addAttribute("riskLevel", riskLevel);
        model.addAttribute("expenseLevel", expenseLevel);

        return "admin/etf-meta";
    }


    /**
     * [규민] ETF 종목 정보 수정/등록 페이지 이동
     */
    @GetMapping("/meta/edit")
    public String editMeta(@RequestParam(value = "id", required = false) String etfId,
                           Model model) {
        EtfMeta etfMeta;
        if (etfId != null) {
            // 수정: ID가 있으면 DB에서 찾아옴
            etfMeta = etfMetaRepository.findById(etfId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 종목이 없습니다."));
        } else {
            // 등록: ID가 없으면 빈 객체 생성 (신규 등록용)
            etfMeta = new EtfMeta();
        }

        model.addAttribute("meta", etfMeta);
        return "admin/etf-meta-form"; // etf-meta-form.html로 이동
    }

    /**
     * [규민] ETF 종목 정보 저장 실행
     */
    @PostMapping("/meta/save")
    public String saveMeta(@ModelAttribute EtfMeta etfMeta) {
        etfMetaRepository.save(etfMeta);
        return "redirect:/admin/meta";
    }

    /**
     * [규민] ETF 종목 정보 삭제 실행
     */
    @GetMapping("/meta/delete")
    public String deleteMeta(@RequestParam("id") String etfId) {
        etfMetaRepository.deleteById(etfId);
        return "redirect:/admin/meta";
    }

    // ==========================================
    // [규민] 이벤트 관리 섹션 추가
    // ==========================================

    /**
     * [규민] 이벤트 관리 목록 페이지
     */
    @GetMapping("/events")
    public String eventList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            HttpSession session,
            Model model
    ) {
        // 🔐 권한 체크
        User sessionUser = (User) session.getAttribute("loginUser");
        if (sessionUser == null) return "redirect:/login";

        User currentUser = userRepository.findById(sessionUser.getId()).orElse(null);
        if (currentUser == null || !currentUser.isAdmin()) return "redirect:/";

        // 🔥 빈 문자열 → null
        keyword = (keyword != null && !keyword.isBlank()) ? keyword : null;
        status = (status != null && !status.isBlank()) ? status : null;

        // ✅ 람다용 final 변수
        final String finalStatus = status;

        Pageable pageable = PageRequest.of(page, 20);

        Page<Event> resultPage =
                eventService.searchEvents(keyword, startDate, endDate, pageable);

        // 🔥 status 필터링 (계산값)
        List<Event> filtered =
                status == null
                        ? resultPage.getContent()
                        : resultPage.getContent().stream()
                        .filter(e -> e.getStatus().equals(finalStatus))
                        .toList();

        model.addAttribute("events", filtered);
        model.addAttribute("page", resultPage);

        // 검색값 유지
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "admin/events";
    }

    /**
     * [규민] 이벤트 등록 폼 이동
     */
    @GetMapping("/events/new")
    public String eventForm(Model model) {
        model.addAttribute("event", new Event());
        return "admin/event-form";
    }

    /**
     * [규민] 이벤트 수정 페이지 이동 (ID로 조회하여 폼 채우기)
     */
    @GetMapping("/events/{id}/edit")
    public String editEvent(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.getEvent(id));
        return "admin/event-form";
    }

    /**
     * [규민] 이벤트 저장 (추가/수정)
     */
    @PostMapping("/events/save")
    public String saveEvent(@ModelAttribute Event event) {
        eventService.saveEvent(event);
        return "redirect:/admin/events";
    }

    /**
     * [규민] 이벤트 삭제
     */
    @PostMapping("/events/{id}/delete")
    public String deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return "redirect:/admin/events";
    }
}