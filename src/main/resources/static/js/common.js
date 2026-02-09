// 헤더
const header = document.querySelector('header');
const gnb = document.querySelector('.gnb');
const mains = document.querySelectorAll('.gnb>li>a')
const subs = document.querySelectorAll('.gnb>li .sub');

gnb.addEventListener('mouseenter', () => {
    subs.forEach(sub => {
        sub.style.height = sub.scrollHeight + 'px';
        mains.forEach(main => main.classList.add('active'));
    });
    header.classList.add('active');
});

gnb.addEventListener('mouseleave', () => {
    subs.forEach(sub => {
        sub.style.height = 0;
        mains.forEach(main => main.classList.remove('active'));
    });
    header.classList.remove('active');
});


