/* ===== AUTH-SPECIFIC JAVASCRIPT ===== */

// Authentication-related functions and modal handling

/* ===== EMAIL CHECK FUNCTIONALITY ===== */

function openEmailCheckModal() {
    const modal = document.getElementById('emailCheckModal');
    if (modal) {
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
        
        const emailInput = document.getElementById('emailCheckInput');
        if (emailInput) {
            emailInput.focus();
        }
    }
}

function closeEmailCheckModal() {
    const modal = document.getElementById('emailCheckModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
        
        // Reset form
        const emailInput = document.getElementById('emailCheckInput');
        const resultDiv = document.getElementById('emailCheckResult');
        const loadingDiv = document.getElementById('emailCheckLoading');
        const submitBtn = document.getElementById('emailCheckSubmit');
        
        if (emailInput) emailInput.value = '';
        if (resultDiv) resultDiv.style.display = 'none';
        if (loadingDiv) loadingDiv.style.display = 'none';
        if (submitBtn) submitBtn.disabled = false;
    }
}

async function checkEmail(event) {
    event.preventDefault();

    const email = document.getElementById('emailCheckInput').value.trim();
    const submitBtn = document.getElementById('emailCheckSubmit');
    const resultDiv = document.getElementById('emailCheckResult');
    const loadingDiv = document.getElementById('emailCheckLoading');

    if (!email) {
        showEmailResult('이메일 주소를 입력해주세요.', 'error');
        return;
    }

    if (!isValidEmail(email)) {
        showEmailResult('올바른 이메일 형식이 아닙니다.', 'error');
        return;
    }

    // Show loading
    loadingDiv.style.display = 'flex';
    resultDiv.style.display = 'none';
    submitBtn.disabled = true;

    try {
        const response = await fetch('/api/auth/check-email', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email: email })
        });

        const data = await response.json();
        
        loadingDiv.style.display = 'none';
        submitBtn.disabled = false;

        if (data.success) {
            if (data.data.exists) {
                showEmailResult(data.message, 'success');
            } else {
                showEmailResult(data.message, 'not-found');
            }
        } else {
            showEmailResult(data.message, 'error');
        }
    } catch (error) {
        loadingDiv.style.display = 'none';
        submitBtn.disabled = false;
        console.error('Network Error:', error);
        showEmailResult('네트워크 오류가 발생했습니다. 다시 시도해주세요.', 'error');
    }
}

function showEmailResult(message, type) {
    const resultDiv = document.getElementById('emailCheckResult');
    if (!resultDiv) return;

    resultDiv.textContent = message;
    resultDiv.style.display = 'block';

    // Reset classes
    resultDiv.className = 'result-message';

    // Add appropriate styling
    if (type === 'success') {
        resultDiv.style.backgroundColor = 'var(--success-light)';
        resultDiv.style.color = 'var(--success-color)';
        resultDiv.style.border = '1px solid var(--success-border)';
    } else if (type === 'not-found') {
        resultDiv.style.backgroundColor = 'var(--warning-light)';
        resultDiv.style.color = 'var(--warning-color)';
        resultDiv.style.border = '1px solid var(--warning-border)';
    } else {
        resultDiv.style.backgroundColor = 'var(--error-light)';
        resultDiv.style.color = 'var(--error-color)';
        resultDiv.style.border = '1px solid var(--error-border)';
    }
}

/* ===== SOCIAL LOGIN FUNCTIONS ===== */

function loginWithKakao() {
    // Get Kakao API key from template or meta tag
    const restApiKey = document.querySelector('meta[name="kakao-rest-api-key"]')?.getAttribute('content') || 
                     window.kakaoRestApiKey || '';
    const redirectUri = document.querySelector('meta[name="kakao-redirect-uri"]')?.getAttribute('content') || 
                       window.kakaoRedirectUri || '';

    if (!restApiKey || !redirectUri) {
        console.error('Kakao login configuration missing');
        showError('카카오 로그인 설정이 올바르지 않습니다.');
        return;
    }

    const kakaoAuthUrl = `https://kauth.kakao.com/oauth/authorize` +
        `?client_id=${restApiKey}` +
        `&redirect_uri=${encodeURIComponent(redirectUri)}` +
        `&response_type=code`;

    window.location.href = kakaoAuthUrl;
}

function loginWithNaver() {
    showError('네이버 로그인 기능은 준비 중입니다.');
}

function loginWithGoogle() {
    showError('구글 로그인 기능은 준비 중입니다.');
}

/* ===== SIGNUP FORM HANDLING ===== */

class SignupFormManager {
    constructor() {
        this.currentStep = 1;
        this.formData = {};
        this.init();
    }

    init() {
        // Initialize step-specific functionality
        this.initStepHandlers();
        this.initValidation();
    }

    initStepHandlers() {
        // Step 1: User Type Selection
        const userTypeBtns = document.querySelectorAll('.user-type-btn');
        userTypeBtns.forEach(btn => {
            btn.addEventListener('click', (e) => {
                this.handleUserTypeSelection(e.target);
            });
        });

        // Step 2: Gender Selection
        const genderBtns = document.querySelectorAll('.gender-btn');
        genderBtns.forEach(btn => {
            btn.addEventListener('click', (e) => {
                this.handleGenderSelection(e.target);
            });
        });

        // Birth date validation
        const birthInputs = document.querySelectorAll('#birthYear, #birthMonth, #birthDay');
        birthInputs.forEach(input => {
            input.addEventListener('input', () => {
                this.validateBirthDate();
                this.validateForm();
            });
        });

        // Region/City selection
        const regionSelect = document.getElementById('regionSelect');
        if (regionSelect) {
            regionSelect.addEventListener('change', (e) => {
                this.handleRegionChange(e.target.value);
            });
        }

        const citySelect = document.getElementById('citySelect');
        if (citySelect) {
            citySelect.addEventListener('change', (e) => {
                this.handleCityChange(e.target.value);
            });
        }
    }

    initValidation() {
        // Form input validation
        const inputs = document.querySelectorAll('input[required]');
        inputs.forEach(input => {
            input.addEventListener('input', () => {
                this.validateForm();
            });
        });
    }

    handleUserTypeSelection(button) {
        // Remove active class from all buttons
        document.querySelectorAll('.user-type-btn').forEach(btn => {
            btn.classList.remove('selected');
            btn.classList.add('outline');
        });

        // Add active class to selected button
        button.classList.remove('outline');
        button.classList.add('selected');

        const selectedUserType = button.dataset.type;
        this.formData.userType = selectedUserType;

        // Auto-navigate to next step after selection
        setTimeout(() => {
            this.navigateToNextStep(selectedUserType);
        }, 300);
    }

    handleGenderSelection(button) {
        // Remove active class from all buttons
        document.querySelectorAll('.gender-btn').forEach(btn => {
            btn.classList.remove('selected');
        });

        // Add active class to selected button
        button.classList.add('selected');

        this.formData.gender = button.dataset.gender;
        this.validateForm();
    }

    validateBirthDate() {
        const year = document.getElementById('birthYear')?.value;
        const month = document.getElementById('birthMonth')?.value;
        const day = document.getElementById('birthDay')?.value;

        if (!year || !month || !day) return false;

        const birthDate = new Date(year, month - 1, day);
        const today = new Date();
        const age = today.getFullYear() - birthDate.getFullYear();

        // Basic validation
        if (age < 14 || age > 100) {
            this.showFieldError('birthYear', '올바른 생년월일을 입력해주세요.');
            return false;
        }

        this.clearFieldError('birthYear');
        return true;
    }

    async handleRegionChange(regionName) {
        const citySelect = document.getElementById('citySelect');
        
        if (!citySelect) return;

        // Reset city selection
        citySelect.innerHTML = '<option value="">시/군/구를 선택하세요</option>';
        citySelect.disabled = true;

        if (!regionName) {
            citySelect.innerHTML = '<option value="">먼저 지역을 선택하세요</option>';
            this.validateForm();
            return;
        }

        try {
            const response = await fetch(`/api/sggcode/cities?region=${encodeURIComponent(regionName)}`);
            const data = await response.json();

            if (data.success) {
                data.cities.forEach(city => {
                    const option = document.createElement('option');
                    option.value = city.name;
                    option.textContent = city.name;
                    option.dataset.code = city.code;
                    citySelect.appendChild(option);
                });
                citySelect.disabled = false;
                this.hideFieldError('cityError');
            } else {
                this.showFieldError('cityError', data.message || '시/군/구 정보를 불러올 수 없습니다.');
            }
        } catch (error) {
            console.error('Error loading cities:', error);
            this.showFieldError('cityError', '시/군/구 정보를 불러올 수 없습니다.');
        }

        this.validateForm();
    }

    handleCityChange(cityName) {
        const citySelect = document.getElementById('citySelect');
        const selectedOption = citySelect.options[citySelect.selectedIndex];
        const cityCode = selectedOption.dataset.code || '';

        this.formData.selectedCityCode = cityCode;
        
        const hiddenInput = document.getElementById('selectedCityCode');
        if (hiddenInput) {
            hiddenInput.value = cityCode;
        }

        this.validateForm();
    }

    validateForm() {
        const nextBtn = document.getElementById('nextBtn');
        if (!nextBtn) return;

        let isValid = true;

        // Check all required fields based on current step
        if (this.currentStep === 2) {
            const username = document.getElementById('username')?.value.trim();
            const birthYear = document.getElementById('birthYear')?.value;
            const birthMonth = document.getElementById('birthMonth')?.value;
            const birthDay = document.getElementById('birthDay')?.value;
            const gender = document.querySelector('.gender-btn.selected')?.dataset.gender;
            const region = document.getElementById('regionSelect')?.value;
            const city = document.getElementById('citySelect')?.value;
            const cityCode = document.getElementById('selectedCityCode')?.value;

            isValid = username && birthYear && birthMonth && birthDay && 
                     gender && region && city && cityCode && 
                     this.validateBirthDate();
        }

        if (isValid) {
            nextBtn.classList.add('active');
            nextBtn.style.cursor = 'pointer';
            nextBtn.disabled = false;
        } else {
            nextBtn.classList.remove('active');
            nextBtn.style.cursor = 'not-allowed';
            nextBtn.disabled = true;
        }
    }

    showFieldError(fieldId, message) {
        const errorElement = document.getElementById(fieldId + 'Error') || 
                           document.querySelector(`[data-error="${fieldId}"]`);
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.style.display = 'block';
            errorElement.classList.add('invalid-feedback');
        }
    }

    clearFieldError(fieldId) {
        const errorElement = document.getElementById(fieldId + 'Error') || 
                           document.querySelector(`[data-error="${fieldId}"]`);
        if (errorElement) {
            errorElement.style.display = 'none';
        }
    }

    hideFieldError(fieldId) {
        this.clearFieldError(fieldId);
    }

    async navigateToNextStep(userType) {
        try {
            const response = await fetch('/signup/step/1', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userType: userType
                })
            });

            const result = await response.text();
            
            if (result === 'success') {
                window.location.href = '/signup/step/2';
            } else {
                showError('오류가 발생했습니다. 다시 시도해주세요.');
            }
        } catch (error) {
            console.error('Error:', error);
            showError('오류가 발생했습니다. 다시 시도해주세요.');
        }
    }

    async submitStep2Form(formData) {
        try {
            const response = await fetch('/signup/step/2', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData)
            });

            const result = await response.text();
            
            if (result === 'success') {
                window.location.href = '/signup/step/3';
            } else {
                showError('오류가 발생했습니다. 다시 시도해주세요.');
            }
        } catch (error) {
            console.error('Error:', error);
            showError('오류가 발생했습니다. 다시 시도해주세요.');
        }
    }
}

/* ===== FORM SUBMISSION HANDLERS ===== */

// Step 2 form submission
function handleStep2Submit(event) {
    event.preventDefault();

    const formData = {
        username: document.getElementById('username').value.trim(),
        birthYear: document.getElementById('birthYear').value,
        birthMonth: document.getElementById('birthMonth').value,
        birthDay: document.getElementById('birthDay').value,
        gender: document.querySelector('.gender-btn.selected')?.dataset.gender,
        region: document.getElementById('selectedCityCode').value
    };

    // Validation
    if (!formData.username || !formData.birthYear || !formData.birthMonth || 
        !formData.birthDay || !formData.gender || !formData.region) {
        showError('모든 필드를 입력해주세요.');
        return;
    }

    signupManager.submitStep2Form(formData);
}

/* ===== POLICY MODAL FUNCTIONS (AUTH-SPECIFIC) ===== */

async function openTermsModal() {
    const modal = document.getElementById('termsModal');
    const modalBody = document.getElementById('termsModalBody');

    if (!modal) return;

    modal.style.display = 'block';
    document.body.style.overflow = 'hidden';

    // Load terms content if not already loaded
    if (!modalBody.dataset.loaded) {
        try {
            const response = await fetch('/policy/terms-modal');
            const html = await response.text();
            modalBody.innerHTML = html;
            modalBody.dataset.loaded = 'true';
        } catch (error) {
            console.error('Failed to load terms:', error);
            modalBody.innerHTML = '<div class="alert alert-error">약관을 불러오는 중 오류가 발생했습니다. 다시 시도해주세요.</div>';
        }
    }
}

function closeTermsModal() {
    const modal = document.getElementById('termsModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
    }
}

async function openPrivacyModal() {
    const modal = document.getElementById('privacyModal');
    const modalBody = document.getElementById('privacyModalBody');

    if (!modal) return;

    modal.style.display = 'block';
    document.body.style.overflow = 'hidden';

    // Load privacy content if not already loaded
    if (!modalBody.dataset.loaded) {
        try {
            const response = await fetch('/policy/privacy-modal');
            const html = await response.text();
            modalBody.innerHTML = html;
            modalBody.dataset.loaded = 'true';
        } catch (error) {
            console.error('Failed to load privacy policy:', error);
            modalBody.innerHTML = '<div class="alert alert-error">개인정보처리방침을 불러오는 중 오류가 발생했습니다. 다시 시도해주세요.</div>';
        }
    }
}

function closePrivacyModal() {
    const modal = document.getElementById('privacyModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
    }
}

/* ===== UTILITY FUNCTIONS ===== */

function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

function showError(message) {
    // Simple error display - can be enhanced
    if (window.notify) {
        window.notify.error(message);
    } else {
        alert(message);
    }
}

function showSuccess(message) {
    // Simple success display - can be enhanced
    if (window.notify) {
        window.notify.success(message);
    } else {
        alert(message);
    }
}

/* ===== INITIALIZATION ===== */

let signupManager;

document.addEventListener('DOMContentLoaded', function() {
    // Initialize signup form manager
    signupManager = new SignupFormManager();

    // Add modal close handlers
    window.addEventListener('click', function(event) {
        const termsModal = document.getElementById('termsModal');
        const privacyModal = document.getElementById('privacyModal');
        const emailCheckModal = document.getElementById('emailCheckModal');

        if (event.target === termsModal) {
            closeTermsModal();
        }
        if (event.target === privacyModal) {
            closePrivacyModal();
        }
        if (event.target === emailCheckModal) {
            closeEmailCheckModal();
        }
    });

    // Add keyboard handlers
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            closeEmailCheckModal();
            closeTermsModal();
            closePrivacyModal();
        }
    });

    // Set up form submission handlers
    const step2Form = document.getElementById('step2Form');
    if (step2Form) {
        step2Form.addEventListener('submit', handleStep2Submit);
    }

    console.log('Auth Common JS loaded successfully');
});

/* ===== MAKE FUNCTIONS GLOBALLY AVAILABLE ===== */

window.openEmailCheckModal = openEmailCheckModal;
window.closeEmailCheckModal = closeEmailCheckModal;
window.checkEmail = checkEmail;
window.loginWithKakao = loginWithKakao;
window.loginWithNaver = loginWithNaver;
window.loginWithGoogle = loginWithGoogle;
window.openTermsModal = openTermsModal;
window.closeTermsModal = closeTermsModal;
window.openPrivacyModal = openPrivacyModal;
window.closePrivacyModal = closePrivacyModal;