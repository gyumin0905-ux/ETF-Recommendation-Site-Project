document.addEventListener("DOMContentLoaded", () => {
    const simForm = document.getElementById("simForm");
    const runBtn = document.getElementById("runBtn");
    const resetBtn = document.getElementById("resetBtn");

    const monthlyAmount = document.getElementById("monthlyAmount");
    const startDate = document.getElementById("startDate");
    const endDate = document.getElementById("endDate");

    const amountHint = document.getElementById("amountHint");
    const startHint = document.getElementById("startHint");
    const endHint = document.getElementById("endHint");

    const resultSection = document.getElementById("resultSection");

    const sumPeriod = document.getElementById("sumPeriod");
    const sumInvested = document.getElementById("sumInvested");
    const sumTotal = document.getElementById("sumTotal");

    const metricInvested = document.getElementById("metricInvested");
    const metricFinal = document.getElementById("metricFinal");
    const metricRate = document.getElementById("metricRate");

    // ===== 모달 =====
    const openRiskBtn = document.getElementById("openRiskBtn");
    const riskModal = document.getElementById("riskModal");

    // ===== 탭(ul/li 구조) =====
    const tabLinks = document.querySelectorAll(".tab-btn li a");
    const tabViews = document.querySelectorAll(".tab-view > li");

    const formatNumber = (num) => {
        if (Number.isNaN(num)) return "-";
        return num.toLocaleString("ko-KR");
    };

    const showHint = (el, show) => {
        if (!el) return;
        if (show) el.classList.add("is-show");
        else el.classList.remove("is-show");
    };

    const onlyDigits = (value) => {
        return value.replace(/[^\d]/g, "");
    };

    const parseAmount = (value) => {
        const cleaned = onlyDigits(value);
        if (!cleaned) return NaN;
        return Number(cleaned);
    };

    const diffMonths = (start, end) => {
        const sy = start.getFullYear();
        const sm = start.getMonth();
        const ey = end.getFullYear();
        const em = end.getMonth();
        return (ey - sy) * 12 + (em - sm) + 1;
    };

    const smoothScrollTo = (el) => {
        el.scrollIntoView({ behavior: "smooth", block: "start" });
    };

    // =========================
    // 월투자금 입력 콤마 처리
    // =========================
    monthlyAmount.addEventListener("input", () => {
        const digits = onlyDigits(monthlyAmount.value);

        if (!digits) {
            monthlyAmount.value = "";
            showHint(amountHint, false);
            return;
        }

        monthlyAmount.value = formatNumber(Number(digits));
        showHint(amountHint, false);
    });

    // =========================
    // 탭 전환(ul/li 방식)
    // =========================
    const setActiveTab = (tabId) => {
        tabLinks.forEach((link) => {
            link.parentElement.classList.toggle("active", link.dataset.tab === tabId);
        });

        tabViews.forEach((view) => {
            view.classList.toggle("active", view.id === tabId);
        });
    };

    tabLinks.forEach((link) => {
        link.addEventListener("click", (e) => {
            e.preventDefault();
            setActiveTab(link.dataset.tab);
        });
    });

    // =========================
    // Reset
    // =========================
    resetBtn.addEventListener("click", () => {
        simForm.reset();
        monthlyAmount.value = "";

        showHint(amountHint, false);
        showHint(startHint, false);
        showHint(endHint, false);

        resultSection.classList.add("is-hidden");
        setActiveTab("tab1");
    });

    // =========================
    // 모달 열기/닫기
    // =========================
    const openModal = () => {
        riskModal.classList.remove("is-hidden");
        riskModal.setAttribute("aria-hidden", "false");
        document.body.style.overflow = "hidden";
    };

    const closeModal = () => {
        riskModal.classList.add("is-hidden");
        riskModal.setAttribute("aria-hidden", "true");
        document.body.style.overflow = "";
    };

    openRiskBtn.addEventListener("click", openModal);

    riskModal.addEventListener("click", (e) => {
        const t = e.target;
        if (t && t.dataset && t.dataset.close === "true") {
            closeModal();
        }
    });

    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape" && !riskModal.classList.contains("is-hidden")) {
            closeModal();
        }
    });

    // =========================
    // Submit (시뮬레이션 실행)
    // =========================
    simForm.addEventListener("submit", async (e) => {
        e.preventDefault(); // ✅ 이게 핵심

        const amount = parseAmount(monthlyAmount.value);
        const s = startDate.value;
        const ed = endDate.value;

        const isAmountOk = Number.isFinite(amount) && amount > 0;
        const isStartOk = !!s;
        const isEndOk = !!ed;

        showHint(amountHint, !isAmountOk);
        showHint(startHint, !isStartOk);
        showHint(endHint, !isEndOk);

        if (!isAmountOk || !isStartOk || !isEndOk) {
            return;
        }

        const sDate = new Date(s);
        const eDate = new Date(ed);

        if (sDate > eDate) {
            showHint(endHint, true);
            endHint.textContent = "종료일은 시작일 이후여야 해요.";
            return;
        } else {
            endHint.textContent = "종료일을 선택해 주세요.";
        }

        // ✅ 백엔드로 보낼 페이로드
        const payload = {
            productCode: "TIGER_US_SP",
            monthlyAmount: amount,
            startDate: s,
            endDate: ed
        };

        runBtn.disabled = true;
        runBtn.textContent = "계산 중...";

        try {
            // =========================
            // ✅ 백엔드 연결 구간 (여기만 바꾸면 됨)
            // =========================
            // const res = await fetch("/api/simulations", {
            //     method: "POST",
            //     headers: { "Content-Type": "application/json" },
            //     body: JSON.stringify(payload)
            // });
            //
            // if (!res.ok) throw new Error("Simulation API failed");
            // const data = await res.json();

            // ✅ 더미 계산(동작 확인용)
            const months = diffMonths(sDate, eDate);
            const investedTotal = amount * months;
            const finalValue = Math.round(investedTotal * 1.12);
            const rate = ((finalValue - investedTotal) / investedTotal) * 100;

            const data = {
                months,
                investedTotal,
                totalValue: finalValue,
                finalValue,
                rate,
                oneLine: "가격이 내려가도 꾸준히 투자하면 결과가 나올 수 있어요"
            };

            // ===== 결과 출력 =====
            sumPeriod.textContent = `${data.months}개월`;
            sumInvested.textContent = `${formatNumber(data.investedTotal)}원`;
            sumTotal.textContent = `${formatNumber(data.totalValue)}원`;

            metricInvested.textContent = `${formatNumber(data.investedTotal)}원`;
            metricFinal.textContent = `${formatNumber(data.finalValue)}원`;
            metricRate.textContent = `${data.rate.toFixed(2)}%`;

            // ✅ (중요) resultLine 전체 덮지 말고 .mmm만 변경
            const mmm = document.querySelector("#resultLine .mmm");
            if (mmm) {
                mmm.textContent = data.oneLine;
            }

            // 결과 열기 + 탭 초기화 + 스크롤
            resultSection.classList.remove("is-hidden");
            setActiveTab("tab1");
            smoothScrollTo(resultSection);

            // ✅ 차트 렌더링 연결 위치(백엔드 붙이면 여기에서)
        } catch (err) {
            alert("시뮬레이션 데이터를 불러오지 못했어요. 백엔드 연결 상태를 확인해 주세요.");
            console.error(err, payload);
        } finally {
            runBtn.disabled = false;
            runBtn.textContent = "시뮬레이션 시작하기";
        }

        // 결과 영역 표시
        resultSection.classList.remove("is-hidden");

// 결과 위치로 스크롤
        resultSection.scrollIntoView({ behavior: "smooth" });
    });
});
