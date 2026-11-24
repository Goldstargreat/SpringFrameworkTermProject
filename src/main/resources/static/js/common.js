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

// 4) 전화번호 하이픈 자동 입력 : data-phone-format 이 붙은 입력칸은 숫자만 입력해도 "010-1234-5678" 처럼 바뀐다
//    서버의 MemberValidator.normalizePhone 과 같은 규칙 (11자리 : 3-4-4, 10자리 : 3-3-4)
//    입력을 편하게 해 주는 기능일 뿐이고, 진짜 형식 검사와 중복 검사는 서버에서 한다
function formatPhone(value) {
    var digits = value.replace(/[^0-9]/g, '').substring(0, 11); // 숫자만, 최대 11자리
    if (digits.length < 4) {
        return digits;                                                     // 010
    }
    if (digits.length < 7) {
        return digits.substring(0, 3) + '-' + digits.substring(3);         // 010-123
    }
    if (digits.length < 11) {
        return digits.substring(0, 3) + '-' + digits.substring(3, 6) + '-' + digits.substring(6); // 010-123-4567
    }
    return digits.substring(0, 3) + '-' + digits.substring(3, 7) + '-' + digits.substring(7);     // 010-1234-5678
}

document.querySelectorAll('input[data-phone-format]').forEach(function (input) {
    // 이미 들어 있는 값(회원 정보에서 채운 번호 등)도 형식을 맞춰 둔다
    input.value = formatPhone(input.value);

    input.addEventListener('input', function () {
        // 가운데를 고칠 때 커서가 맨 끝으로 튀지 않도록,
        // 커서 앞에 숫자가 몇 개 있었는지 기억했다가 바뀐 값에서 같은 자리로 커서를 옮긴다
        var digitsBeforeCursor = input.value.substring(0, input.selectionStart).replace(/[^0-9]/g, '').length;
        var formatted = formatPhone(input.value);
        input.value = formatted;

        var cursor = 0;
        var count = 0;
        while (cursor < formatted.length && count < digitsBeforeCursor) {
            if (/[0-9]/.test(formatted[cursor])) {
                count++;
            }
            cursor++;
        }
        input.setSelectionRange(cursor, cursor);
    });
});
