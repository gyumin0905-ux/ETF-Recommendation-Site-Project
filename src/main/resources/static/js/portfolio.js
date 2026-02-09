document.addEventListener('DOMContentLoaded', () => {
  console.log('portfolio.js 정상 로드');

  /* ======================
     [1] 헤더 & GNB
  ====================== */
  const header = document.querySelector('header');
  const gnb = document.querySelector('.gnb');
  const subs = document.querySelectorAll('.gnb > li .sub');

  if (gnb && header) {
    gnb.addEventListener('mouseenter', () => {
      subs.forEach(sub => { sub.style.height = sub.scrollHeight + 'px'; });
      header.classList.add('active');
    });

    gnb.addEventListener('mouseleave', () => {
      subs.forEach(sub => { sub.style.height = 0; });
      header.classList.remove('active');
    });
  }

  /* ======================
     [2] 포트폴리오 데이터
  ====================== */
  const allPortfolioData = [
    {
      title: '안정형',
      subtitle: 'S&P 500, 단기채 위주',
      labels: ['S&P 500', '단기채', '국내채권', '골드', '기타'],
      data: [30, 25, 20, 15, 10],
      backgroundColors: ['#BD3C3C', '#D15A5A', '#D85A5A', '#F09393', '#F09389']
    },
    {
      title: '성장 집중형',
      subtitle: 'IT, 기술주, 신흥국 주식',
      labels: ['기술주', '신흥국 주식', '바이오', '부동산', '기타'],
      data: [40, 30, 15, 10, 5],
      backgroundColors: ['#DD58A8', '#E86BB6', '#df7db7', '#f59ed2', '#F3B1D9']
    },
    {
      title: '해외 투자형',
      subtitle: '해외 주식, 글로벌 채권',
      labels: ['미국 주식', '유럽 주식', '아시아 주식', '글로벌 채권', '원자재'],
      data: [35, 20, 20, 15, 10],
      backgroundColors: ['#316d31', '#417e41', '#55aa55', '#80c080', '#9dcf9d']
    },
    {
      title: '국내 투자형',
      subtitle: '국내 주식, 국내 채권',
      labels: ['KOSPI 200', '코스닥 150', '국내채권', '부동산 리츠', '원자재'],
      data: [35, 25, 20, 10, 10],
      backgroundColors: ['#87CEEB', '#ADD8E6', '#B0E0E6', '#AFEEEE', '#b0faff']
    },
    {
      title: '균형형',
      subtitle: '주식, 채권, 대체 투자 균형',
      labels: ['KOSPI 200', '인도', '고배당', '단기채', '장기국고채'],
      data: [25, 15, 20, 20, 20],
      backgroundColors: ['#ffc917', '#ffd51c', '#ffdb4c', '#F6D35E', '#F5D97C']
    }
  ];

  const charts = [];

  /* ======================
     [3] 차트 생성
  ====================== */
  function createAllCharts() {
    // Chart.js / plugin 로드 확인(에러 방지)
    if (typeof Chart === 'undefined') {
      console.error('Chart.js가 로드되지 않았습니다.');
      return;
    }

    const hasDatalabels = typeof ChartDataLabels !== 'undefined';

    allPortfolioData.forEach((portfolio, index) => {
      const canvas = document.getElementById(`portfolioChart${index}`);
      if (!canvas) return;

      const ctx = canvas.getContext('2d');

      const chart = new Chart(ctx, {
        type: 'pie',
        data: {
          labels: portfolio.labels,
          datasets: [{
            data: portfolio.data,
            backgroundColor: portfolio.backgroundColors,
            borderWidth: 2,
            borderColor: 'rgba(255,255,255,0.9)'
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          layout: { padding: 6 },
          animation: { duration: 700, easing: 'easeOutQuart' },
          plugins: {
            legend: { display: false },
            tooltip: { enabled: true, padding: 10 },
            datalabels: hasDatalabels ? {
              color: '#1a1a1a',
              font: { weight: '400', size: 14 },
              formatter: (value, ctx) => {
                const label = ctx.chart.data.labels[ctx.dataIndex];
                return `${label}\n${value}%`;
              },
              textAlign: 'center'
            } : undefined
          }
        },
        plugins: hasDatalabels ? [ChartDataLabels] : []
      });

      charts.push(chart);
    });
  }

  /* ======================
     [4] 상세 정보 업데이트
  ====================== */
  function updateDetail(index) {
    const selected = allPortfolioData[index];
    const titleEl = document.getElementById('detailTitle');
    const listEl = document.getElementById('detailList');
    const detailBox = document.getElementById('detailInfoBox');

    if (!selected || !titleEl || !listEl || !detailBox) return;

    detailBox.classList.remove('animate');
    void detailBox.offsetWidth;

    titleEl.innerHTML = `
      ${selected.title}
      <span style="
        display:block;
        margin-top:6px;
        font-size:14px;
        font-weight:600;
        color:#6b7280;
      ">
        ${selected.subtitle}
      </span>
    `;

    listEl.innerHTML = selected.labels.map((label, i) => `
      <li>
        <span style="
          background-color:${selected.backgroundColors[i]};
          width:12px;
          height:12px;
          border-radius:50%;
          display:inline-block;
          flex: 0 0 auto;
        "></span>
        <span style="flex:1;">${label}</span>
        <strong style="flex:0 0 auto;">${selected.data[i]}%</strong>
      </li>
    `).join('');

    detailBox.classList.add('animate');
  }

  /* ======================
     [5] 차트 활성화
  ====================== */
  function activateChart(index) {
    const items = document.querySelectorAll('.chart-item-wrapper');
    items.forEach(item => item.classList.remove('chart-on'));

    const current = document.querySelector(`.chart-item-wrapper.pos${index + 1}`);
    if (current) current.classList.add('chart-on');

    updateDetail(index);
  }

  // 혹시 HTML에서 필요하면 쓸 수 있게만 열어둠(지금은 onclick 제거 권장)
  window.activateChart = activateChart;

  /* ======================
     [6] 차트 클릭 이벤트 (JS에서만)
  ====================== */
  const wrappers = document.querySelectorAll('.chart-item-wrapper');
  wrappers.forEach((wrapper, index) => {
    wrapper.addEventListener('click', () => activateChart(index));
  });

  /* ======================
     [7] 탭 메뉴
  ====================== */
  const tabs = document.querySelectorAll('.tab-menu li');
  const contents = document.querySelectorAll('.content-wrapper > div');
  const tabMenu = document.querySelector('.tab-menu');
  const defaultTarget = tabMenu?.dataset.defaultTab || 'content1';

  function switchTab(target) {
    tabs.forEach(t => t.classList.remove('tab-on'));
    contents.forEach(c => c.classList.remove('current'));

    document.querySelector(`.tab-menu li[data-target="${target}"]`)?.classList.add('tab-on');
    document.querySelector(`.${target}`)?.classList.add('current');

    // 차트 탭 켜질 때 렌더링 보정
    if (target === 'content2') {
      setTimeout(() => {
        charts.forEach(chart => chart.resize());
      }, 140);
    }
  }

  tabs.forEach(tab => {
    tab.addEventListener('click', () => switchTab(tab.dataset.target));
  });

  /* ======================
     [8] 초기 실행
  ====================== */
  createAllCharts();
  switchTab(defaultTarget);
  activateChart(0);
});
