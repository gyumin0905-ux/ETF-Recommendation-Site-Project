
const menuBtn = document.querySelectorAll('.event-menu li');

const listIng = document.querySelectorAll('.event-list .list-ing');
const listEnd = document.querySelectorAll('.event-list .list-end');


// 탭 버튼
for (let i = 0; i < menuBtn.length; i++) {
    menuBtn[i].addEventListener('click', function() {
        menuBtn.forEach(btn => btn.classList.remove('active'));
        this.classList.add('active');
    })
}

menuBtn[0].addEventListener('click', () => {
    listIng.forEach(list => list.classList.add('active'));
    listEnd.forEach(list => list.classList.add('active'));
})

menuBtn[1].addEventListener('click', () => {
    listIng.forEach(list => list.classList.add('active'));
    listEnd.forEach(list => list.classList.remove('active'));
})

menuBtn[2].addEventListener('click', () => {
    listIng.forEach(list => list.classList.remove('active'));
    listEnd.forEach(list => list.classList.add('active'));
})


