
if (!Array.isArray(window.SERVER_TOP5) || window.SERVER_TOP5.length === 0) {
    window.SERVER_TOP5 = [
        { name: "TESLA INC", weight: 40 },
        { name: "현대차", weight: 15 },
        { name: "로보티즈", weight: 15 },
        { name: "에스피지", weight: 15 },
        { name: "HL만도", weight: 15 }
    ];
}
document.addEventListener('DOMContentLoaded', () => {
    const tabButtons = document.querySelectorAll('.tab-category .tab-btn');
    const tabPanels = document.querySelectorAll('.tab-panels .tab-cont');

    if (!tabButtons.length || !tabPanels.length) return;

    const showTab = (tabId) => {
        tabButtons.forEach(btn => {
        btn.classList.toggle('on', btn.dataset.tab === tabId);
        });

        tabPanels.forEach(panel => {
        panel.classList.toggle('on', panel.id === tabId);
        });

        if (tabId === 'container') {
        renderAll();
        }
    };

    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
        const tabId = btn.dataset.tab;
        if (!tabId) return;
        showTab(tabId);
        });
    });

    const defaultBtn = document.querySelector('.tab-category .tab-btn.on') || tabButtons[0];
    showTab(defaultBtn.dataset.tab);

    renderAll();
    });


    const getTop5 = () => Array.isArray(window.SERVER_TOP5) ? window.SERVER_TOP5 : [];

    let topChart = null;
    const COLORS = ["#4E7664", "#5CB389", "#A3E496", "#92D050", "#76AF4B"];

    const chartLabelPlugin = {
    id: 'chartLabelPlugin',
    afterDraw(chart) {
        const { ctx, data } = chart;
        ctx.save();

        chart.getDatasetMeta(0).data.forEach((arc, i) => {
        const angle = (arc.startAngle + arc.endAngle) / 2;
        const radius = (arc.innerRadius + arc.outerRadius) / 2;
        const x = arc.x + Math.cos(angle) * radius;
        const y = arc.y + Math.sin(angle) * radius;

        const label = data.labels[i] || '';
        const [name, percent] = label.split(" - ");

        ctx.fillStyle = "#fff";
        ctx.textAlign = "center";
        ctx.textBaseline = "middle";

        ctx.font = "bold 12px sans-serif";
        ctx.fillText(name || '', x, y - 8);

        ctx.font = "11px sans-serif";
        ctx.fillText(percent || '', x, y + 10);
        });

        ctx.restore();
    }
    };

    function renderChart(top5) {
    const canvas = document.getElementById("top-chart");
    if (!canvas) return;

    const labels = top5.map(i => `${i.name} - ${i.weight}%`);
    const values = top5.map(i => i.weight);

    const ctx = canvas.getContext("2d");

    if (topChart) {
        topChart.destroy();
        topChart = null;
    }

    topChart = new Chart(ctx, {
        type: "doughnut",
        data: {
        labels,
        datasets: [{
            data: values,
            backgroundColor: COLORS,
            borderColor: "#fff",
            borderWidth: 2
        }]
        },
        options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: "45%",
        plugins: { legend: { display: false } }
        },
        plugins: [chartLabelPlugin]
    });
    }

    function renderList(top5) {
    const ul = document.getElementById("top5-list");
    if (!ul) return;

    ul.innerHTML = "";
    top5.forEach(item => {
        const li = document.createElement("li");
        li.innerHTML = `
        <span class="name">${item.name}</span>
        <span class="weight">${item.weight}%</span>
        `;
        ul.appendChild(li);
    });
    }

    function renderAll() {
    const top5 = getTop5();
    renderChart(top5);
    renderList(top5);
    }


    async function downloadPDF() {
    try {
        const { jsPDF } = window.jspdf;
        const setup = document.getElementById("setup");
        if (!setup) throw new Error("#setup 없음");
        if (!window.html2canvas) throw new Error("html2canvas 없음");
        if (!jsPDF) throw new Error("jsPDF 없음");

        const iframe = document.createElement("iframe");
        iframe.style.position = "fixed";
        iframe.style.left = "-9999px";
        iframe.style.top = "0";
        iframe.style.width = "1400px";
        iframe.style.height = "900px";
        document.body.appendChild(iframe);

        const doc = iframe.contentDocument;
        doc.open();
        doc.write(`
        <!doctype html>
        <html>
        <head>
            <meta charset="utf-8" />
            <style>
            body { margin: 0; background: #fff; font-family: sans-serif; }
            /* setup 레이아웃 최소 복원 */
            .chart-container-custom { display: flex; gap: 20px; align-items: center; padding: 20px; }
            .chart-center-area { width: 450px; height: 450px; }
            .chart-box { height: 420px; }
            .chart-right-list h3 { margin: 0 0 10px; font-size: 20px; font-weight: 700; color: #2f855a; }
            #top5-list { list-style: none; padding: 0; margin: 0; width: 300px; }
            #top5-list li { display:flex; justify-content:space-between; padding:14px 0; border-bottom:1px solid #e5e7eb; }
            .name { font-weight: 700; }
            .weight { font-weight: 800; color: #2f855a; }
            </style>
        </head>
        <body></body>
        </html>
        `);
        doc.close();

        const clone = setup.cloneNode(true);
        doc.body.appendChild(clone);

        const srcCanvas = setup.querySelector("#top-chart");
        const dstCanvas = clone.querySelector("#top-chart");
        if (srcCanvas && dstCanvas) {
        dstCanvas.width = srcCanvas.width;
        dstCanvas.height = srcCanvas.height;
        dstCanvas.getContext("2d").drawImage(srcCanvas, 0, 0);
        }

        await new Promise(r => requestAnimationFrame(r));

        const canvas = await html2canvas(clone, {
        scale: 2,
        backgroundColor: "#ffffff",
        useCORS: true
        });

        document.body.removeChild(iframe);

        const imgData = canvas.toDataURL("image/png");
        const pdf = new jsPDF("p", "mm", "a4");

        const contentW = 170;
        const contentH = (canvas.height * contentW) / canvas.width;
        const x = (210 - contentW) / 2;
        const y = 20;

        pdf.addImage(imgData, "PNG", x, y, contentW, contentH);
        pdf.save("ETF_구성정보_TOP5.pdf");
    } catch (e) {
        console.error(e);
        alert(e.message || e);
    }
}