document.addEventListener('DOMContentLoaded', () => {
    const mainpostBtn = document.querySelector('.postBtn');
    if (mainpostBtn) {
        mainpostBtn.addEventListener('click', (e) => {
            e.preventDefault();
            const isConfirm = confirm('게시글을 등록하시겠습니까?');
            if (isConfirm) {
                window.location.href = 'communitywrite.html';
            }
        });
    }
});
