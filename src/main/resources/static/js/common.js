/* ===== COMMON JAVASCRIPT FUNCTIONS ===== */

// Global utility functions and common modal handling

/* ===== MODAL MANAGEMENT SYSTEM ===== */

class ModalManager {
    constructor() {
        this.openModals = new Set();
        this.init();
    }

    init() {
        // Close modal on escape key
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.closeTopModal();
            }
        });

        // Close modal on backdrop click
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('modal')) {
                this.closeModal(e.target.id);
            }
        });
    }

    openModal(modalId) {
        const modal = document.getElementById(modalId);
        if (!modal) {
            console.error(`Modal with id '${modalId}' not found`);
            return;
        }

        modal.style.display = 'block';
        this.openModals.add(modalId);
        document.body.style.overflow = 'hidden';
        
        // Focus management for accessibility
        const firstFocusable = modal.querySelector('input, button, select, textarea, [tabindex]:not([tabindex="-1"])');
        if (firstFocusable) {
            firstFocusable.focus();
        }

        // Trigger custom event
        modal.dispatchEvent(new CustomEvent('modalOpened', { detail: { modalId } }));
    }

    closeModal(modalId) {
        const modal = document.getElementById(modalId);
        if (!modal) return;

        modal.style.display = 'none';
        this.openModals.delete(modalId);

        if (this.openModals.size === 0) {
            document.body.style.overflow = 'auto';
        }

        // Trigger custom event
        modal.dispatchEvent(new CustomEvent('modalClosed', { detail: { modalId } }));
    }

    closeTopModal() {
        if (this.openModals.size > 0) {
            const modalId = Array.from(this.openModals).pop();
            this.closeModal(modalId);
        }
    }

    closeAllModals() {
        this.openModals.forEach(modalId => {
            this.closeModal(modalId);
        });
    }
}

// Global modal manager instance
const modalManager = new ModalManager();

/* ===== POLICY MODAL FUNCTIONS ===== */

async function openTermsModal() {
    const modal = document.getElementById('termsModal');
    const modalBody = document.getElementById('termsModalBody');

    modalManager.openModal('termsModal');

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
    modalManager.closeModal('termsModal');
}

async function openPrivacyModal() {
    const modal = document.getElementById('privacyModal');
    const modalBody = document.getElementById('privacyModalBody');

    modalManager.openModal('privacyModal');

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
    modalManager.closeModal('privacyModal');
}

/* ===== CONFIRMATION MODAL ===== */

function showConfirmation(options = {}) {
    const {
        title = '확인',
        message = '정말로 진행하시겠습니까?',
        confirmText = '확인',
        cancelText = '취소',
        onConfirm = () => {},
        onCancel = () => {}
    } = options;

    const modal = document.getElementById('confirmationModal');
    const titleElement = document.getElementById('confirmationTitle');
    const messageElement = document.getElementById('confirmationMessage');
    const confirmBtn = document.getElementById('confirmationConfirmBtn');

    if (!modal) {
        console.error('Confirmation modal not found');
        return;
    }

    titleElement.textContent = title;
    messageElement.textContent = message;
    confirmBtn.textContent = confirmText;

    // Remove existing event listeners
    const newConfirmBtn = confirmBtn.cloneNode(true);
    confirmBtn.parentNode.replaceChild(newConfirmBtn, confirmBtn);

    // Add new event listeners
    newConfirmBtn.addEventListener('click', () => {
        modalManager.closeModal('confirmationModal');
        onConfirm();
    });

    modalManager.openModal('confirmationModal');
}

function closeConfirmationModal() {
    modalManager.closeModal('confirmationModal');
}

/* ===== SUCCESS/ERROR MODALS ===== */

function showSuccess(message = '작업이 성공적으로 완료되었습니다.') {
    const modal = document.getElementById('successModal');
    const messageElement = document.getElementById('successMessage');

    if (modal && messageElement) {
        messageElement.textContent = message;
        modalManager.openModal('successModal');
    }
}

function closeSuccessModal() {
    modalManager.closeModal('successModal');
}

function showError(message = '오류가 발생했습니다. 다시 시도해주세요.') {
    const modal = document.getElementById('errorModal');
    const messageElement = document.getElementById('errorMessage');

    if (modal && messageElement) {
        messageElement.textContent = message;
        modalManager.openModal('errorModal');
    }
}

function closeErrorModal() {
    modalManager.closeModal('errorModal');
}

/* ===== LOADING MODAL ===== */

function showLoading(message = '처리 중입니다...') {
    const modal = document.getElementById('loadingModal');
    const messageElement = document.getElementById('loadingMessage');

    if (modal && messageElement) {
        messageElement.textContent = message;
        modalManager.openModal('loadingModal');
    }
}

function hideLoading() {
    modalManager.closeModal('loadingModal');
}

/* ===== UTILITY FUNCTIONS ===== */

// Debounce function for search inputs
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Throttle function for scroll events
function throttle(func, limit) {
    let inThrottle;
    return function() {
        const args = arguments;
        const context = this;
        if (!inThrottle) {
            func.apply(context, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    }
}

// Format date for display
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}

// Format time for display
function formatTime(dateString) {
    const date = new Date(dateString);
    return date.toLocaleTimeString('ko-KR', {
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Format number with commas
function formatNumber(num) {
    if (typeof num !== 'number') return num;
    return num.toLocaleString('ko-KR');
}

// Truncate text with ellipsis
function truncateText(text, maxLength) {
    if (!text || text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
}

// Validate email format
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

// Validate phone number (Korean format)
function isValidPhone(phone) {
    const phoneRegex = /^01[016789]-?\d{3,4}-?\d{4}$/;
    return phoneRegex.test(phone.replace(/\s/g, ''));
}

// Copy text to clipboard
async function copyToClipboard(text) {
    try {
        await navigator.clipboard.writeText(text);
        showSuccess('클립보드에 복사되었습니다.');
        return true;
    } catch (err) {
        console.error('Failed to copy: ', err);
        showError('복사에 실패했습니다.');
        return false;
    }
}

/* ===== API HELPER FUNCTIONS ===== */

// Generic API request function
async function apiRequest(url, options = {}) {
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const config = { ...defaultOptions, ...options };

    // Add CSRF token if available
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    
    if (csrfToken && csrfHeader) {
        config.headers[csrfHeader] = csrfToken;
    }

    try {
        const response = await fetch(url, config);
        
        // Handle non-JSON responses
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            const data = await response.json();
            
            if (!response.ok) {
                throw new Error(data.message || `HTTP error! status: ${response.status}`);
            }
            
            return data;
        } else {
            const text = await response.text();
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            return text;
        }
    } catch (error) {
        console.error('API request failed:', error);
        throw error;
    }
}

// GET request helper
async function apiGet(url) {
    return apiRequest(url, { method: 'GET' });
}

// POST request helper
async function apiPost(url, data) {
    return apiRequest(url, {
        method: 'POST',
        body: JSON.stringify(data)
    });
}

// PUT request helper
async function apiPut(url, data) {
    return apiRequest(url, {
        method: 'PUT',
        body: JSON.stringify(data)
    });
}

// DELETE request helper
async function apiDelete(url) {
    return apiRequest(url, { method: 'DELETE' });
}

/* ===== FORM VALIDATION HELPERS ===== */

class FormValidator {
    constructor(form) {
        this.form = form;
        this.errors = new Map();
    }

    addRule(fieldName, validator, errorMessage) {
        if (!this.errors.has(fieldName)) {
            this.errors.set(fieldName, []);
        }
        
        const field = this.form.querySelector(`[name="${fieldName}"]`);
        if (field) {
            field.addEventListener('blur', () => {
                this.validateField(fieldName, validator, errorMessage);
            });
        }
        
        return this;
    }

    validateField(fieldName, validator, errorMessage) {
        const field = this.form.querySelector(`[name="${fieldName}"]`);
        const errorContainer = this.form.querySelector(`[data-error="${fieldName}"]`);
        
        if (!field) return false;

        const isValid = validator(field.value);
        
        if (isValid) {
            field.classList.remove('is-invalid');
            field.classList.add('is-valid');
            if (errorContainer) {
                errorContainer.textContent = '';
                errorContainer.style.display = 'none';
            }
        } else {
            field.classList.remove('is-valid');
            field.classList.add('is-invalid');
            if (errorContainer) {
                errorContainer.textContent = errorMessage;
                errorContainer.style.display = 'block';
            }
        }

        return isValid;
    }

    validateAll() {
        let isFormValid = true;
        // Implementation would validate all registered fields
        return isFormValid;
    }
}

/* ===== NOTIFICATION SYSTEM ===== */

class NotificationManager {
    constructor() {
        this.container = this.createContainer();
        document.body.appendChild(this.container);
    }

    createContainer() {
        const container = document.createElement('div');
        container.id = 'notification-container';
        container.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
            display: flex;
            flex-direction: column;
            gap: 10px;
            max-width: 400px;
        `;
        return container;
    }

    show(message, type = 'info', duration = 5000) {
        const notification = document.createElement('div');
        notification.className = `alert alert-${type}`;
        notification.style.cssText = `
            padding: 15px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            animation: slideInRight 0.3s ease;
            cursor: pointer;
        `;
        notification.textContent = message;

        // Add close functionality
        notification.addEventListener('click', () => {
            this.remove(notification);
        });

        this.container.appendChild(notification);

        // Auto remove
        if (duration > 0) {
            setTimeout(() => {
                this.remove(notification);
            }, duration);
        }

        return notification;
    }

    remove(notification) {
        notification.style.animation = 'slideOutRight 0.3s ease';
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }

    success(message, duration) {
        return this.show(message, 'success', duration);
    }

    error(message, duration) {
        return this.show(message, 'error', duration);
    }

    warning(message, duration) {
        return this.show(message, 'warning', duration);
    }

    info(message, duration) {
        return this.show(message, 'info', duration);
    }
}

// Global notification manager
const notify = new NotificationManager();

/* ===== INITIALIZATION ===== */

document.addEventListener('DOMContentLoaded', function() {
    // Add slide animations CSS if not already present
    if (!document.querySelector('#notification-animations')) {
        const style = document.createElement('style');
        style.id = 'notification-animations';
        style.textContent = `
            @keyframes slideInRight {
                from {
                    transform: translateX(100%);
                    opacity: 0;
                }
                to {
                    transform: translateX(0);
                    opacity: 1;
                }
            }
            
            @keyframes slideOutRight {
                from {
                    transform: translateX(0);
                    opacity: 1;
                }
                to {
                    transform: translateX(100%);
                    opacity: 0;
                }
            }
        `;
        document.head.appendChild(style);
    }

    // Initialize any page-specific functionality
    console.log('Topping Common JS loaded successfully');
});

/* ===== EXPORTS (for module use) ===== */

// Make functions available globally
window.Topping = {
    modalManager,
    showConfirmation,
    showSuccess,
    showError,
    showLoading,
    hideLoading,
    notify,
    FormValidator,
    debounce,
    throttle,
    formatDate,
    formatTime,
    formatNumber,
    truncateText,
    isValidEmail,
    isValidPhone,
    copyToClipboard,
    apiGet,
    apiPost,
    apiPut,
    apiDelete
};