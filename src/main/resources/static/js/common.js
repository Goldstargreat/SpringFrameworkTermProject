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
//    서버의 MemberValidator.normalizePhone 과 같은 규칙
//    - 서울(02) : 앞자리 2자리. 02-123-4567(9자리), 02-1234-5678(10자리)
//    - 그 외    : 앞자리 3자리. 031-123-4567(10자리), 010-1234-5678(11자리)
//    입력을 편하게 해 주는 기능일 뿐이고, 진짜 형식 검사와 중복 검사는 서버에서 한다
function formatPhone(value) {
    var digits = value.replace(/[^0-9]/g, '');
    var seoul = digits.indexOf('02') === 0;
    var p = seoul ? 2 : 3;                  // 앞자리 길이
    var max = seoul ? 10 : 11;              // 최대 자리 수
    digits = digits.substring(0, max);

    if (digits.length <= p) {
        return digits;                                                         // 02, 010
    }
    if (digits.length <= p + 3) {
        return digits.substring(0, p) + '-' + digits.substring(p);             // 02-123, 010-123
    }
    if (digits.length < max) {
        // 가운데 3자리 : 02-123-4567, 031-123-4567 (입력 중인 010-123-4 도 여기)
        return digits.substring(0, p) + '-' + digits.substring(p, p + 3) + '-' + digits.substring(p + 3);
    }
    // 가운데 4자리 : 02-1234-5678, 010-1234-5678
    return digits.substring(0, p) + '-' + digits.substring(p, p + 4) + '-' + digits.substring(p + 4);
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

// 5) 중복 확인 버튼 : data-check-url 이 붙은 버튼을 누르면 새로고침 없이 서버에 물어보고 결과를 보여 준다
//    data-check-url    : 물어볼 주소 (예: /members/check-login-id)
//    data-check-input  : 검사할 입력칸 id   (그 값을 ?loginId=... 처럼 입력칸 name 으로 보낸다)
//    data-check-result : 결과 문구를 넣을 칸 id
//    서버는 {"available": true/false, "message": "..."} 형태의 JSON 을 돌려준다
document.querySelectorAll('button[data-check-url]').forEach(function (button) {
    var input = document.getElementById(button.dataset.checkInput);
    var result = document.getElementById(button.dataset.checkResult);

    function showResult(text, ok) {
        result.textContent = text;
        result.className = 'check-message ' + (ok ? 'ok' : 'fail');
    }

    button.addEventListener('click', function () {
        var url = button.dataset.checkUrl + '?' + encodeURIComponent(input.name) + '=' + encodeURIComponent(input.value.trim());
        // fetch : 페이지를 그대로 둔 채 서버와 데이터만 주고받는다 (AJAX)
        fetch(url)
            .then(function (response) { return response.json(); })
            .then(function (data) { showResult(data.message, data.available); })
            .catch(function () { showResult('확인하지 못했습니다. 잠시 후 다시 시도해 주세요.', false); });
    });

    // 확인한 뒤에 아이디를 바꾸면 예전 결과는 지운다 (다시 확인해야 한다)
    input.addEventListener('input', function () {
        result.textContent = '';
        result.className = 'check-message';
    });
});
