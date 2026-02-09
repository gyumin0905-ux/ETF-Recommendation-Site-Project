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



// 섹션1: 검색
window.addEventListener('DOMContentLoaded', () => {
  const bannerElements = document.querySelectorAll('#search .transition-box>*');

  bannerElements.forEach(el => {
    el.style.transform = 'translateY(0px)';
    el.style.opacity = 1;
  })

})


const inputText = "관심있는 ETF 상품명이나 코드를 입력해보세요.";
const typingSpeed = 110;
const deletingSpeed = 70;
const delay = 1200;
const restartDelay = 1000;

let charIndex = 0;
let isDeleting = false;
let timer;

function typing() {
  const typingInput = document.querySelector('#search .search-bar');

  if (!isDeleting) {
    charIndex++;
  } else {
    charIndex--;
  }

  typingInput.placeholder = inputText.substring(0, charIndex);

  // 타이핑 완료 → 삭제 시작 전 딜레이
  if (!isDeleting && charIndex === inputText.length) {
    timer = setTimeout(() => {
      isDeleting = true;
      typing();
    }, delay);
    return;
  }

  // 삭제 완료 → 다시 타이핑 시작 전 딜레이
  if (isDeleting && charIndex === 0) {
    timer = setTimeout(() => {
      isDeleting = false;
      typing();
    }, restartDelay);
    return;
  }

  timer = setTimeout(typing, isDeleting ? deletingSpeed : typingSpeed);
}

typing();



// 섹션3: 추천 ETF
const recommendBtn = document.querySelectorAll('#recommend .left .tab-btn li')
const recommendCont = document.querySelectorAll('#recommend .right .etf-list')
const bookmark = document.querySelectorAll('#recommend .right .etf-list .bookmark');

for (let i = 0; i < recommendBtn.length; i++) {
  recommendBtn[i].addEventListener('click', function() {
    recommendBtn.forEach(btn => btn.classList.remove('active'));
    this.classList.add('active');
    recommendCont.forEach(cont => cont.classList.remove('active'));
    recommendCont[i].classList.add('active');
  });
}

// bookmark.forEach(function(img) {
  // img.addEventListener('mouseenter', function() {
  //   this.src = 'images/bookmark-full.png';
  // });
  // img.addEventListener('mouseleave', function() {
  //   this.src = 'images/bookmark-empty.png';
  // });
//   img.addEventListener('click', function() {
//     this.src = 'images/bookmark-full.png';
//   })
// });

bookmark.forEach(img => {
  let isActive = false;

  img.addEventListener('mouseenter', () => {
    if (!isActive) {
      img.src = 'images/bookmark-full.png';
    }
  });

  img.addEventListener('mouseleave', () => {
    if (!isActive) {
      img.src = 'images/bookmark-empty.png';
    }
  });

  img.addEventListener('click', e => {
    e.preventDefault();
    e.stopPropagation();

    isActive = !isActive;
    img.src = isActive
      ? 'images/bookmark-full.png'
      : 'images/bookmark-empty.png';
  });
});



// 섹션4: 뉴스 swiper.js
new Swiper('.news-swiper', {
    slidesPerView: 4,
    spaceBetween: 20,

    loop: true,              // 🔥 이제 켜도 됨
    loopFillGroupWithBlank: true,

    autoplay: {
        delay: 3000,
        disableOnInteraction: false
    },

    navigation: {
        nextEl: '.swiper-button-next',
        prevEl: '.swiper-button-prev'
    },

    speed: 600
});


