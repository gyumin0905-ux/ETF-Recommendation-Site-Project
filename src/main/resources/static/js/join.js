/* ===== 이메일 도메인 직접입력 ===== */
const domainSelect = document.querySelector(".email-domain-select");
const domainInput  = document.querySelector(".email-domain-input");

if(domainSelect) {
    domainSelect.addEventListener("change", () => {
        if (domainSelect.value === "direct") {
            domainInput.disabled = false;
            domainInput.value = "";
            domainInput.focus();
        } else {
            domainInput.disabled = true;
            // disabled여도 값은 읽을 수 있게 처리됨
            domainInput.value = domainSelect.value;
        }
    });
}

/* ===== 약관 동의 ===== */
const agreeAll    = document.getElementById("agreeAll");
const agreeChecks = document.querySelectorAll(".agree-check");
const signupBtn   = document.getElementById("btnSignup"); // ID 변경됨

// 전체동의
if(agreeAll) {
    agreeAll.addEventListener("change", () => {
        agreeChecks.forEach(chk => chk.checked = agreeAll.checked);
    });
}

// 개별동의
if(agreeChecks) {
    agreeChecks.forEach(chk => {
        chk.addEventListener("change", () => {
            if(agreeAll) {
                // 하나라도 체크 안되어 있으면 전체동의 해제
                agreeAll.checked = [...agreeChecks].every(c => c.checked);
            }
        });
    });
}

/* =========================================
   [추가] 아이디 중복 확인 로직
   ========================================= */
const btnDup = document.querySelector(".btn-dup");       // 중복확인 버튼
// 주의: HTML input에 name="username"이 꼭 있어야 함
const idInput = document.querySelector("input[name=username]");

// 중복확인 통과 여부 저장 (false면 가입 막음)
let isIdChecked = false;

if (btnDup && idInput) {
    // 1. 아이디 입력창 내용 바뀌면 다시 검사받게 초기화
    idInput.addEventListener("input", () => {
        isIdChecked = false;
        btnDup.style.backgroundColor = ""; // 버튼 색 원래대로
        btnDup.innerText = "중복확인";
    });

    // 2. 버튼 클릭 시 서버로 요청
    btnDup.addEventListener("click", () => {
        const username = idInput.value.trim();

        // 빈값 체크
        if (!username) {
            alert("아이디를 입력해주세요.");
            idInput.focus();
            return;
        }

        // 길이 체크 (선택사항)
        if (username.length < 6 || username.length > 12) {
            alert("아이디는 6~12자 이내로 입력해주세요.");
            return;
        }

        // 서버(API)로 "이 아이디 있나요?" 물어보기
        fetch(`/api/check-username?username=${username}`)
            .then(response => response.json()) // true(중복) 또는 false(사용가능) 옴
            .then(isDuplicate => {
                if (isDuplicate) {
                    alert("❌ 이미 사용 중인 아이디입니다.");
                    idInput.value = ""; // 초기화
                    idInput.focus();
                    isIdChecked = false;
                } else {
                    alert("✅ 사용 가능한 아이디입니다!");
                    isIdChecked = true;
                    // (옵션) 버튼 모양 변경해서 완료 티내기
                    btnDup.style.backgroundColor = "#055C4D";
                    btnDup.style.color = "white";
                    btnDup.innerText = "확인완료";
                }
            })
            .catch(err => {
                console.error(err);
                alert("서버 통신 오류가 발생했습니다.");
            });
    });
}


/* ===== [수정] 회원가입 버튼 클릭 시 검증 및 전송 ===== */
if(signupBtn) {
    signupBtn.addEventListener("click", () => {

        // 0. 아이디 중복확인 했는지 체크
        if (typeof isIdChecked !== 'undefined' && !isIdChecked) {
            alert("아이디 중복 확인을 진행해주세요.");
            if(idInput) idInput.focus();
            return;
        }

        // 1. 필수 약관 체크 확인
        const requiredChecks = document.querySelectorAll(".agree-check[data-required='true']");
        const allChecked = [...requiredChecks].every(c => c.checked);

        if (!allChecked) {
            alert("필수 약관에 동의해주세요.");
            return;
        }

        // 2. 이메일 합치기 (아이디 + @ + 도메인)
        const emailId = document.querySelector(".email-id").value;
        const emailDomain = document.querySelector(".email-domain-input").value;

        if(!emailId || !emailDomain) {
            alert("이메일을 정확히 입력해주세요.");
            return;
        }

        const fullEmail = emailId + "@" + emailDomain;
        document.getElementById("realEmail").value = fullEmail; // hidden input에 주입

        // 3. 비밀번호 일치 확인
        const pw = document.getElementById("password").value;
        const pwConfirm = document.getElementById("passwordConfirm").value;

        if(pw !== pwConfirm) {
            alert("비밀번호가 일치하지 않습니다.");
            return;
        }

        // 4. 진짜 전송 (Submit)
        document.getElementById("signupForm").submit();
    });
}