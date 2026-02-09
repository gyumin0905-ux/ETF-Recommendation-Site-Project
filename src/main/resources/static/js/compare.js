$(document).ready(function () {


$(".tab-btn>li").on("click", function (e) {
    e.preventDefault();
    let i = $(this).index();

    $(".tab-view>li").hide();
    $(".tab-view>li").eq(i).show();

    $(".tab-btn>li").removeClass("active");
    $(".tab-btn>li").eq(i).addClass("active");
});


const $items = $(".compareList .top10content > ul > li");
const $boxA = $(".section1Wrap .comparecontent .boxA");
const $boxB = $(".section1Wrap .comparecontent .boxB");
const $btn = $(".section1Wrap .compareBtn");
const $resultBox = $(".section2Wrap .resultBox");

let selected = []; // 선택된 li들을 저장(jQuery 객체)

// 결과는 처음 숨김
$resultBox.hide();

// 버튼 상태 초기화
setBtnState();
renderBoxes();

// ✅ li 클릭 이벤트: 선택/해제
$items.on("click", function () {
    const $li = $(this);

    // 이미 선택된 항목이면 해제
    const idx = selected.findIndex(el => el.is($li));
    if (idx > -1) {
    selected.splice(idx, 1);
    $li.removeClass("is-selected");

    renderBoxes();
    setBtnState();
    closeResultIfNotReady(); /*여기수정됨*/
    return;
    }

    // 2개 초과 선택 방지
    if (selected.length >= 2) {
    alert("비교는 2개까지만 선택할 수 있어요!");
    return;
    }

    // 새로 선택
    selected.push($li);
    $li.addClass("is-selected");

    renderBoxes();
    setBtnState();
    // 선택은 늘어나는 거라 result 닫을 필요 없음
});

// ✅ box 클릭 → 해당 박스 선택 취소 + resultBox 자동 닫기
$boxA.on("click", function () {
    if (!selected[0]) return;

    selected[0].removeClass("is-selected");
    selected.splice(0, 1);

    renderBoxes();
    setBtnState();
    closeResultIfNotReady(); /*여기수정됨*/
});

$boxB.on("click", function () {
    if (!selected[1]) return;

    selected[1].removeClass("is-selected");
    selected.splice(1, 1);

    renderBoxes();
    setBtnState();
    closeResultIfNotReady(); /*여기수정됨*/
});

// ✅ 비교하기 버튼 클릭 → (2개 선택 완료일 때만) resultBox 보여주기
$btn.on("click", function () {
    if (selected.length !== 2) {
    alert("비교할 상품 2개를 먼저 선택해주세요!");
    return;
    }

    // 보여주기 + 스크롤 이동
    $resultBox.stop(true, true).slideDown(400, function () {
    $("html, body").animate({ scrollTop: $resultBox.offset().top - 80 }, 500);
    });
});


function renderBoxes() {
    const emptyHTML = `<p class="boxintxt">아래에서 상품을 선택해주세요.</p>`;

    // boxA
    if (selected[0]) $boxA.html(makeBoxHTML(selected[0]));
    else $boxA.html(emptyHTML);

    // boxB
    if (selected[1]) $boxB.html(makeBoxHTML(selected[1]));
    else $boxB.html(emptyHTML);
}

function makeBoxHTML($li) {
    const title = $li.find(".prutit").text().trim();
    const code = $li.find(".code").text().trim();

    return `
    <div class="picked">
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

function closeResultIfNotReady() { /*여기수정됨*/
    if (selected.length < 2) {
    $resultBox.stop(true, true).slideUp(300);
    }
}


const $proLis = $("#RecommendWrap .proList > li"); /*여기수정됨*/

// 초기 활성: 2번째 li가 기본으로 on 되도록 강제(HTML에 on이 없어도 동작) /*여기수정됨*/
$proLis.removeClass("on");
$proLis.eq(1).addClass("on"); // 0=첫번째, 1=두번째 /*여기수정됨*/

// 클릭하면 활성 이동 /*여기수정됨*/
$proLis.on("click", function () {
    $proLis.removeClass("on");
    $(this).addClass("on");
});

});
