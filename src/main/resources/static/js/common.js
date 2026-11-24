// 골드스타몰 공통 자바스크립트
// 모든 화면 꼬리말(fragments/layout :: footer)에서 불러온다
// HTML 에는 data-* 속성만 적어 두고, 실제 동작은 이 파일이 맡는다
// (예전에는 onclick="...", onsubmit="..." 처럼 HTML 안에 직접 적었던 코드)

// 1) 확인 창 : data-confirm="문구" 가 붙은 폼은 제출하기 전에 확인 창을 띄운다
//    "취소"를 누르면 제출하지 않는다 (회원/상품/주문 삭제, 회원 탈퇴)
document.querySelectorAll('form[data-confirm]').forEach(function (form) {
    form.addEventListener('submit', function (event) {
        if (!confirm(form.dataset.confirm)) {
            event.preventDefault();
        }
    });
});

// 2) 주소 채우기 : data-fill-target="입력칸 id" 가 붙은 라디오 버튼을 고르면
//    그 버튼의 값(주소)을 해당 입력칸에 채워 넣는다 (장바구니 주문서의 기본/추가 배송지 선택)
document.querySelectorAll('input[data-fill-target]').forEach(function (radio) {
    radio.addEventListener('change', function () {
        var target = document.getElementById(radio.dataset.fillTarget);
        if (target) {
            target.value = radio.value;
        }
    });
});

// 3) 이미지 숨기기 : data-remove-on-error 가 붙은 이미지는 파일이 없으면 지운다
//    (상품 사진이 없는 상품은 깨진 그림 대신 빈칸으로 보이게)
//    이 스크립트가 실행되기 전에 이미 불러오기에 실패한 이미지도 있으므로 두 경우를 모두 처리한다
document.querySelectorAll('img[data-remove-on-error]').forEach(function (img) {
    if (img.complete && img.naturalWidth === 0) {
        // 이미 실패한 이미지
        img.remove();
    } else {
        // 아직 불러오는 중인 이미지
        img.addEventListener('error', function () {
            img.remove();
        });
    }
});
