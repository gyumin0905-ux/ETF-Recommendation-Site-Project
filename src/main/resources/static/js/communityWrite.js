//Post 버튼
document.addEventListener('DOMContentLoaded', () => {
    const mainpostBtn = document.querySelector('.postBtn');
    if (mainpostBtn) {
        mainpostBtn.addEventListener('click', function() {
            const isConfirm = confirm('게시글을 등록하시겠습니까?');
            if (isConfirm) {
                alert('게시되었습니다.');
                window.location.href = 'communitydetail.html';
            }
        });
    }

    // 목록보기 버튼 클릭
    const communityListBtn = document.querySelector('.mbtn');
    if (communityListBtn) {
        communityListBtn.addEventListener('click', function() {
            window.location.href = 'community.html';
        });
    }
});