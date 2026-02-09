    document.addEventListener('DOMContentLoaded', () => {
    const filterButtons = document.querySelectorAll('button[name="asset-cont"]');

    const syncFilterBtn = (btn) => {
        const checkIcon = btn.querySelector('i');
        const selected = btn.classList.contains('is-selected');
        if (checkIcon) checkIcon.classList.toggle('opacity-0', !selected);
    };

    const allBtn = document.querySelector('button[name="asset-cont"][data-val="all"]');

    filterButtons.forEach((btn) => {
        syncFilterBtn(btn);

        btn.addEventListener('click', () => {
        const isAll = btn.dataset.val === 'all';

        if (isAll) {
            filterButtons.forEach((b) => {
            b.classList.add('is-selected');
            syncFilterBtn(b);
            });
            return;
        }

        btn.classList.toggle('is-selected');
        syncFilterBtn(btn);

        if (allBtn) {
            const others = Array.from(filterButtons).filter((b) => b.dataset.val !== 'all');
            const allSelected = others.length > 0 && others.every((b) => b.classList.contains('is-selected'));

            allBtn.classList.toggle('is-selected', allSelected);
            syncFilterBtn(allBtn);
        }
        });
    });

    const submitBtn = document.getElementById('submitFilter');
    if (submitBtn) {
        submitBtn.addEventListener('click', () => {
        const selectedAssets = Array.from(
            document.querySelectorAll('button[name="asset-cont"].is-selected')
        ).map((btn) => btn.dataset.val);

        console.log('선택된 자산:', selectedAssets);
        });
    }

    const regionChecks = document.querySelectorAll('input[type="checkbox"][name="region"]');
    const regionAll = document.querySelector('input[type="checkbox"][name="region"][data-val="all"]');

    if (regionChecks.length && regionAll) {
        const regionOthers = () => Array.from(regionChecks).filter((c) => c !== regionAll);

        const syncRegionAll = () => {
        const others = regionOthers();
        const allSelected = others.length > 0 && others.every((c) => c.checked);
        regionAll.checked = allSelected;
        };

        regionAll.addEventListener('change', () => {
        regionOthers().forEach((c) => (c.checked = regionAll.checked));
        });

        regionOthers().forEach((c) => {
        c.addEventListener('change', () => {
            syncRegionAll();
        });
        });

        syncRegionAll();
    }

    const searchWrap = document.querySelector('.searchBg');
    const searchInput = searchWrap?.querySelector('input[type="search"]');
    const relatedList = searchWrap?.querySelector('.related-keywords');

    if (!searchInput || !relatedList) return;

    const dummyKeywords = ['주식', '채권', 'ETF', '혼합', '테마', '커버드콜', '월배당'];

    const renderKeywords = (items) => {
        relatedList.innerHTML = '';

        items.forEach((text) => {
        const li = document.createElement('li');
        li.textContent = text;
        li.className = 'p-3 text-gray-700 hover:bg-gray-100 cursor-pointer';

        li.addEventListener('mousedown', (e) => {
            e.preventDefault();
            searchInput.value = text;
            relatedList.classList.add('hidden');
        });

        relatedList.appendChild(li);
        });
    };

    relatedList.classList.add('hidden');

    searchInput.addEventListener('input', () => {
        const query = searchInput.value.trim();

        if (!query) {
        relatedList.classList.add('hidden');
        relatedList.innerHTML = '';
        return;
        }

        const filtered = dummyKeywords.filter((k) => k.includes(query));

        if (filtered.length) {
        renderKeywords(filtered);
        relatedList.classList.remove('hidden');
        } else {
        relatedList.classList.add('hidden');
        relatedList.innerHTML = '';
        }
    });

    searchInput.addEventListener('focus', () => {
        const query = searchInput.value.trim();
        if (!query) return;

        const filtered = dummyKeywords.filter((k) => k.includes(query));
        if (filtered.length) {
        renderKeywords(filtered);
        relatedList.classList.remove('hidden');
        }
    });

    document.addEventListener('click', (e) => {
        if (!e.target.closest('.searchBg')) relatedList.classList.add('hidden');
    });

    searchInput.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') relatedList.classList.add('hidden');
    });
    });

    document.querySelectorAll('.starreal-wrap').forEach((wrap) => {
    wrap.onclick = (e) => {
        e.preventDefault();
        wrap.classList.toggle('on');
    };
    });