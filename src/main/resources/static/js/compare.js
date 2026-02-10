$(document).ready(function () {

    const $items = $(".compareList .top10content > ul > li");
    const $boxA = $(".section1Wrap .comparecontent .boxA");
    const $boxB = $(".section1Wrap .comparecontent .boxB");
    const $btn = $(".section1Wrap .compareBtn");
    const $resultBox = $(".resultBox");

    let selected = [];

    // 초기화
    $resultBox.hide();
    setBtnState();
    renderBoxes();

    // 1. TOP 10 리스트 클릭
    $items.on("click", function () {
        const $li = $(this);
        const idx = selected.findIndex(el => el.is($li));

        if (idx > -1) {
            selected.splice(idx, 1);
            $li.removeClass("is-selected");
        } else {
            if (selected.length >= 2) {
                alert("비교는 2개까지만 선택할 수 있어요!");
                return;
            }
            selected.push($li);
            $li.addClass("is-selected");
        }
        renderBoxes();
        setBtnState();
    });

    // 2. 박스 클릭 (해제)
    $boxA.on("click", function () { handleBoxClick(0); });
    $boxB.on("click", function () { handleBoxClick(1); });

    function handleBoxClick(index) {
        if (!selected[index]) return;
        selected[index].removeClass("is-selected");
        selected.splice(index, 1);
        renderBoxes();
        setBtnState();
        if (selected.length < 2) $resultBox.slideUp();
    }

    // 3. 비교하기 버튼 클릭
    $btn.on("click", function () {
        if (selected.length !== 2) {
            alert("비교할 상품 2개를 먼저 선택해주세요!");
            return;
        }

        const isin1 = selected[0].data("isin");
        const isin2 = selected[1].data("isin");

        // 결과창 열기 + 스크롤 이동
        $resultBox.stop(true, true).slideDown(400);
        // 결과창이 버튼 바로 아래에 열리므로 조금 더 아래로 스크롤
        $("html, body").animate({ scrollTop: $resultBox.offset().top - 120 }, 500);

        // 차트 로딩
        if (typeof loadReturnChartInto === 'function') {
            loadReturnChartInto("resultCompareChart", isin1, isin2, "3M", "line");
        }

        // 결과값 로딩
        $("#compareResultArea").html('<div style="text-align:center;padding:50px;">로딩중...</div>');
        $("#compareResultArea").load(
            `/compare/result?isin=${encodeURIComponent(isin1)}&isin=${encodeURIComponent(isin2)} .compare-result-wrap`
        );
    });

    function renderBoxes() {
        const emptyHTML = `<p class="boxintxt">아래에서 상품을 선택해주세요.</p>`;
        if (selected[0]) $boxA.html(makeBoxHTML(selected[0])).addClass('picked');
        else $boxA.html(emptyHTML).removeClass('picked');

        if (selected[1]) $boxB.html(makeBoxHTML(selected[1])).addClass('picked');
        else $boxB.html(emptyHTML).removeClass('picked');
    }

    function makeBoxHTML($li) {
        const title = $li.find(".prutit").text().trim();
        const code = $li.find(".code").text().trim();
        return `
            <div class="">
                <p class="picked-title">${title}</p>
                <p class="picked-code">${code}</p>
            </div>
        `;
    }

    function setBtnState() {
        const ok = selected.length === 2;
        $btn.prop("disabled", !ok);
        if (!ok) $btn.addClass("is-disabled");
        else $btn.removeClass("is-disabled");
    }

    // ==========================================
    // 오늘의 추천 비교 (우측 리스트 클릭)
    // ==========================================
    const $proLis = $(".proList > li");

    if ($proLis.length > 0) {
        fillDetail($proLis.eq(0));
    }

    $proLis.on("click", function () {
        $proLis.removeClass("on");
        $(this).addClass("on");
        fillDetail($(this));
    });

    function fillDetail($li) {
        $("#leftName").text($li.data("left-name"));
        $("#leftTheme").text($li.data("left-theme"));
        $("#leftRisk").text($li.data("left-risk"));
        $("#leftIsin").text($li.data("left-isin"));

        $("#rightName").text($li.data("right-name"));
        $("#rightTheme").text($li.data("right-theme"));
        $("#rightRisk").text($li.data("right-risk"));
        $("#rightIsin").text($li.data("right-isin"));

        if (typeof loadReturnChartInto === 'function') {
            loadReturnChartInto("compareReturnChart", $li.data("left-isin"), $li.data("right-isin"), "1Y", "line");
        }
    }
});