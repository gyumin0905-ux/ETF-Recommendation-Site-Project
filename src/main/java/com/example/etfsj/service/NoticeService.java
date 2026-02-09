package com.example.etfsj.service;

import com.example.etfsj.domain.Notice;
import com.example.etfsj.domain.User;
import com.example.etfsj.repository.NoticeRepository;
import com.example.etfsj.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    public List<Notice> getNoticeList(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return noticeRepository.findByTitleContainingOrContentContainingOrderByIdDesc(keyword, keyword);
        } else {
            return noticeRepository.findAllByOrderByIdDesc();
        }
    }

    @Transactional
    public Notice getNotice(Long id) {
        Notice notice = noticeRepository.findByIdWithUser(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        notice.setViews(notice.getViews() + 1);
        return notice;
    }

    @Transactional
    public void createNewsFromLink(String url, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보 없음"));

        String title = "제목 없음";
        String content = "";
        String thumbnailUrl = null;

        try {
            // 1. 연결 설정 (타임아웃 넉넉하게)
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36")
                    .timeout(30000)
                    .get();

            // 2. 제목 & 썸네일 추출
            title = getMetaTag(doc, "og:title");
            if (title == null) title = doc.title();

            thumbnailUrl = getMetaTag(doc, "og:image");

            // 3. 🔥 [핵심] 본문 선택자 대폭 추가 (매일경제, 한경, 조선 등 대응)
            String[] contentSelectors = {
                    // 네이버, 다음 포털
                    "#dic_area", "#articeBody", "#newsEndContents", ".news_end", ".newsct_article",
                    ".article_view", "div[itemprop='articleBody']",

                    // 주요 언론사 (매일경제, 한국경제, 조선일보 등)
                    ".art_txt", ".txt_article", ".article-body", ".article_body",
                    "#article-view-content-div", ".detail-body", ".view_con",
                    ".news_body_area", ".read_txt", ".article_txt",

                    // 최후의 수단 (범용 태그)
                    "article", ".post-content", ".entry-content"
            };

            Element bodyElement = null;

            // 선택자 순회하며 본문 찾기
            for (String selector : contentSelectors) {
                Elements candidates = doc.select(selector);
                for (Element el : candidates) {
                    // 광고 박스나 짧은 요약글은 거르고, 진짜 본문(길이 50자 이상)만 선택
                    if (el.text().length() > 50) {
                        bodyElement = el;
                        break;
                    }
                }
                if (bodyElement != null) break;
            }

            if (bodyElement != null) {
                // 4. 🔥 [청소] 쓸데없는 태그 강력 삭제
                bodyElement.select(
                        "script, style, iframe, button, " +
                                ".img_desc, figcaption, " + // 이미지 캡션
                                ".relation_news, .related_article, .box_image, " + // 관련기사, 이미지 박스
                                ".byline, .reporter_area, .copyright, " + // 기자 정보
                                ".ad, .advertisement, .banner, " + // 광고
                                ".btn_area, .function_wrap, .util_box, .sns_share, " + // 공유 버튼
                                ".reply, .comment, .u_cbox, " + // 댓글
                                ".font_change, .fontsize, .view_option, .size_set, " + // 글자 크기 설정
                                ".media_end_head_top, .media_end_head_fontsize_layer, " + // 네이버 상단
                                ".article_tool, .util_view, .arv_001" // 언론사별 툴바
                ).remove();

                // 5. [포맷팅] 줄바꿈 살리기
                bodyElement.select("br").append("\\n");
                bodyElement.select("p, div, li, h3, h4").prepend("\\n\\n");

                String rawText = bodyElement.text();

                // 6. [다듬기] 지저분한 공백 정리
                content = rawText.replaceAll("\\\\n", "\n").trim();
                content = content.replaceAll("\n{3,}", "\n\n"); // 너무 긴 공백 줄이기

            } else {
                // 본문 못 찾으면 요약문이라도 (없는 것보단 나음)
                String description = getMetaTag(doc, "og:description");
                content = (description != null) ? "[요약] " + description : "본문을 가져오지 못했습니다. 원문 링크를 확인하세요.";
            }

        } catch (Exception e) {
            e.printStackTrace();
            title = "뉴스 불러오기 실패";
            content = "오류가 발생했습니다: " + e.getMessage();
        }

        // DB 저장 에러 방지 (제목 길이 제한)
        if (title != null && title.length() > 200) title = title.substring(0, 197) + "...";

        Notice notice = Notice.createNews(title, content, url, thumbnailUrl, user);
        noticeRepository.save(notice);
    }

    private String getMetaTag(Document doc, String property) {
        Element element = doc.select("meta[property=" + property + "]").first();
        return (element != null) ? element.attr("content") : null;
    }

    @Transactional
    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }

    public List<Notice> getLatestNews() {
        return noticeRepository.findAllByOrderByCreatedDateDesc();
    }
}