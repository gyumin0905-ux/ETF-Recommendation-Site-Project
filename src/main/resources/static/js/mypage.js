Chart.register(ChartDataLabels);


// ✅ 기본값(백엔드 데이터 없을 때도 깨지지 않게)
const DEFAULT_DONUT_DATA = {
    items: [
    { label: "ETF", value: 35, color: "#3F6F60" },
    { label: "부동산", value: 30, color: "#8DBB4C" },
    { label: "예적금", value: 15, color: "#CFE7CF" },
    { label: "채권", value: 10, color: "#4DAA7B" },
    { label: "펀드", value: 10, color: "#9FE48E" }
    ]
};

let donutChart = null;

function normalizeDonutData(raw) {
    const data = raw && Array.isArray(raw.items) ? raw : DEFAULT_DONUT_DATA;

    // value를 숫자로 강제 + 0 이상만
    const items = data.items
    .map(it => ({
        label: String(it.label ?? ""),
        value: Number(it.value ?? 0),
        color: it.color ? String(it.color) : "#3F6F60"
    }))
    .filter(it => it.label && it.value > 0);

    // 합계 0 방지
    if (!items.length) return DEFAULT_DONUT_DATA.items;

    return items;
}

function renderDonutChart(rawData) {
    const items = normalizeDonutData(rawData);

    const labels = items.map(i => i.label);
    const values = items.map(i => i.value);
    const colors = items.map(i => i.color);

    const ctx = document.getElementById("donut-chart");

    // ✅ 재렌더(데이터 갱신) 대비: 기존 차트 있으면 제거
    if (donutChart) donutChart.destroy();

    donutChart = new Chart(ctx, {
    type: "doughnut",
    data: {
        labels,
        datasets: [{
        data: values,
        backgroundColor: colors,
        borderColor: "#F7F3EA",
        borderWidth: 2,
        hoverOffset: 6
        }]
    },
    options: {
        responsive: false,
        cutout: "55%",
        plugins: {
        legend: { display: false },
        tooltip: { enabled: false },
        datalabels: {
            formatter: (value, context) => {
            const label = context.chart.data.labels[context.dataIndex];
            return `${label} - ${value}%`;
            },
            color: "#111",
            font: { size: 14, weight: "600" },
            anchor: "center",
            align: "center",
            clamp: true
        }
        }
    }
    });
}
