/* ===== MAIN LAYOUT COMMON JAVASCRIPT ===== */

// Main layout specific functionality and authenticated user features

/* ===== USER SESSION MANAGEMENT ===== */

class UserSessionManager {
    constructor() {
        this.user = null;
        this.init();
    }

    async init() {
        try {
            await this.loadUserInfo();
            this.setupSessionChecks();
        } catch (error) {
            console.error('Failed to initialize user session:', error);
        }
    }

    async loadUserInfo() {
        try {
            const response = await fetch('/api/user/info');
            if (response.ok) {
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    this.user = await response.json();
                    this.updateUI();
                } else {
                    console.warn('User info endpoint returned non-JSON response, user likely not authenticated');
                    this.user = null;
                }
            } else if (response.status === 401) {
                this.user = null;
            } else {
                console.warn('Failed to load user info, status:', response.status);
                this.user = null;
            }
        } catch (error) {
            console.warn('Failed to load user info (network/parse error):', error.message);
            this.user = null;
        }
    }

    updateUI() {
        if (!this.user) return;

        // Update any user-specific UI elements
        const userNameElements = document.querySelectorAll('[data-user-name]');
        userNameElements.forEach(el => {
            el.textContent = this.user.username || '사용자';
        });

        const userEmailElements = document.querySelectorAll('[data-user-email]');
        userEmailElements.forEach(el => {
            el.textContent = this.user.email || '';
        });
    }

    setupSessionChecks() {
        // Check session every 5 minutes
        setInterval(() => {
            this.checkSession();
        }, 5 * 60 * 1000);
    }

    async checkSession() {
        try {
            const response = await fetch('/api/session/check');
            if (!response.ok) {
                this.handleSessionExpiry();
            } else {
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                } else {
                    console.warn('Session check returned non-JSON response');
                    this.handleSessionExpiry();
                }
            }
        } catch (error) {
            console.warn('Session check failed (network error):', error.message);
        }
    }

    handleSessionExpiry() {
        window.Topping.showConfirmation({
            title: '세션 만료',
            message: '로그인 세션이 만료되었습니다. 다시 로그인하시겠습니까?',
            confirmText: '로그인',
            cancelText: '취소',
            onConfirm: () => {
                window.location.href = '/auth/login';
            }
        });
    }

    isAuthenticated() {
        return this.user !== null;
    }

    getUser() {
        return this.user;
    }
}

/* ===== NAVIGATION HELPERS ===== */

class NavigationManager {
    constructor() {
        this.init();
    }

    init() {
        this.setupActiveStates();
        this.setupBreadcrumbs();
        this.setupBackButton();
    }

    setupActiveStates() {
        const currentPath = window.location.pathname;
        const navLinks = document.querySelectorAll('.menu-link, .tab-link');

        navLinks.forEach(link => {
            const href = link.getAttribute('href');
            if (href && currentPath.startsWith(href)) {
                link.classList.add('active');
            }
        });
    }

    setupBreadcrumbs() {
        const breadcrumbContainer = document.querySelector('.breadcrumb');
        if (!breadcrumbContainer) return;

        const pathSegments = window.location.pathname.split('/').filter(segment => segment);
        const breadcrumbs = this.generateBreadcrumbs(pathSegments);

        breadcrumbContainer.innerHTML = breadcrumbs.map(crumb => {
            if (crumb.active) {
                return `<span class="breadcrumb-item active">${crumb.text}</span>`;
            } else {
                return `<a href="${crumb.href}" class="breadcrumb-item">${crumb.text}</a>`;
            }
        }).join('');
    }

    generateBreadcrumbs(segments) {
        const breadcrumbs = [{ text: '홈', href: '/', active: false }];
        
        const routeMap = {
            'mypage': '마이페이지',
            'store': '매장',
            'product': '상품',
            'collaboration': '콜라보',
            'support': '고객지원'
        };

        let currentPath = '';
        segments.forEach((segment, index) => {
            currentPath += '/' + segment;
            const isLast = index === segments.length - 1;
            
            breadcrumbs.push({
                text: routeMap[segment] || segment,
                href: currentPath,
                active: isLast
            });
        });

        return breadcrumbs;
    }

    setupBackButton() {
        const backButtons = document.querySelectorAll('[data-back-button]');
        backButtons.forEach(button => {
            button.addEventListener('click', () => {
                if (window.history.length > 1) {
                    window.history.back();
                } else {
                    window.location.href = '/';
                }
            });
        });
    }

    navigateTo(url) {
        window.location.href = url;
    }

    openInNewTab(url) {
        window.open(url, '_blank');
    }
}

/* ===== DATA TABLE MANAGEMENT ===== */

class DataTableManager {
    constructor(tableSelector, options = {}) {
        this.table = document.querySelector(tableSelector);
        this.options = {
            sortable: true,
            filterable: true,
            paginated: true,
            pageSize: 10,
            ...options
        };
        this.data = [];
        this.filteredData = [];
        this.currentPage = 1;
        this.sortField = null;
        this.sortDirection = 'asc';

        if (this.table) {
            this.init();
        }
    }

    init() {
        this.loadData();
        if (this.options.sortable) this.setupSorting();
        if (this.options.filterable) this.setupFiltering();
        if (this.options.paginated) this.setupPagination();
    }

    async loadData() {
        const dataUrl = this.table.dataset.dataUrl;
        if (!dataUrl) return;

        try {
            const response = await fetch(dataUrl);
            this.data = await response.json();
            this.filteredData = [...this.data];
            this.render();
        } catch (error) {
            console.error('Failed to load table data:', error);
            this.showError('데이터를 불러오는데 실패했습니다.');
        }
    }

    setupSorting() {
        const headers = this.table.querySelectorAll('th[data-sort]');
        headers.forEach(header => {
            header.style.cursor = 'pointer';
            header.addEventListener('click', () => {
                const field = header.dataset.sort;
                this.sort(field);
            });
        });
    }

    sort(field) {
        if (this.sortField === field) {
            this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
        } else {
            this.sortField = field;
            this.sortDirection = 'asc';
        }

        this.filteredData.sort((a, b) => {
            const aVal = a[field];
            const bVal = b[field];

            if (aVal < bVal) return this.sortDirection === 'asc' ? -1 : 1;
            if (aVal > bVal) return this.sortDirection === 'asc' ? 1 : -1;
            return 0;
        });

        this.render();
    }

    setupFiltering() {
        const filterInput = document.querySelector('[data-table-filter]');
        if (!filterInput) return;

        const debouncedFilter = window.Topping.debounce((query) => {
            this.filter(query);
        }, 300);

        filterInput.addEventListener('input', (e) => {
            debouncedFilter(e.target.value);
        });
    }

    filter(query) {
        if (!query.trim()) {
            this.filteredData = [...this.data];
        } else {
            this.filteredData = this.data.filter(item => {
                return Object.values(item).some(value => 
                    String(value).toLowerCase().includes(query.toLowerCase())
                );
            });
        }
        
        this.currentPage = 1;
        this.render();
    }

    setupPagination() {
        // Pagination setup would go here
        // This is a simplified version
    }

    render() {
        if (!this.table) return;

        const tbody = this.table.querySelector('tbody');
        if (!tbody) return;

        if (this.filteredData.length === 0) {
            tbody.innerHTML = '<tr><td colspan="100%" class="text-center">데이터가 없습니다.</td></tr>';
            return;
        }

        const startIndex = (this.currentPage - 1) * this.options.pageSize;
        const endIndex = startIndex + this.options.pageSize;
        const pageData = this.filteredData.slice(startIndex, endIndex);

        tbody.innerHTML = pageData.map(row => this.renderRow(row)).join('');
    }

    renderRow(data) {
        // This should be customized based on the table structure
        return `<tr>${Object.values(data).map(value => `<td>${value}</td>`).join('')}</tr>`;
    }

    showError(message) {
        const tbody = this.table.querySelector('tbody');
        if (tbody) {
            tbody.innerHTML = `<tr><td colspan="100%" class="text-center text-danger">${message}</td></tr>`;
        }
    }
}

/* ===== FORM ENHANCEMENT ===== */

class FormEnhancer {
    constructor(form) {
        this.form = form;
        this.validator = new window.Topping.FormValidator(form);
        this.init();
    }

    init() {
        this.setupAutoSave();
        this.setupFileUploads();
        this.setupDependentFields();
        this.setupSubmitHandling();
    }

    setupAutoSave() {
        if (!this.form.dataset.autoSave) return;

        const inputs = this.form.querySelectorAll('input, select, textarea');
        inputs.forEach(input => {
            input.addEventListener('change', window.Topping.debounce(() => {
                this.autoSave();
            }, 1000));
        });
    }

    async autoSave() {
        const formData = new FormData(this.form);
        const data = Object.fromEntries(formData.entries());

        try {
            await window.Topping.apiPost('/api/form/autosave', {
                formId: this.form.id,
                data: data
            });
            
            this.showSaveIndicator('저장됨');
        } catch (error) {
            console.error('Auto-save failed:', error);
        }
    }

    showSaveIndicator(message) {
        // Show a small indicator that form was saved
        const indicator = document.createElement('div');
        indicator.className = 'save-indicator';
        indicator.textContent = message;
        indicator.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: var(--success-color);
            color: white;
            padding: 8px 16px;
            border-radius: 4px;
            font-size: 14px;
            z-index: 1000;
            animation: fadeInOut 2s ease;
        `;

        document.body.appendChild(indicator);
        setTimeout(() => {
            document.body.removeChild(indicator);
        }, 2000);
    }

    setupFileUploads() {
        const fileInputs = this.form.querySelectorAll('input[type="file"]');
        fileInputs.forEach(input => {
            input.addEventListener('change', (e) => {
                this.handleFileUpload(e.target);
            });
        });
    }

    handleFileUpload(input) {
        const files = Array.from(input.files);
        if (files.length === 0) return;

        // Basic file validation
        const maxSize = 5 * 1024 * 1024; // 5MB
        const allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];

        const validFiles = files.filter(file => {
            if (file.size > maxSize) {
                window.Topping.notify.error(`${file.name} 파일이 너무 큽니다. (최대 5MB)`);
                return false;
            }

            if (!allowedTypes.includes(file.type)) {
                window.Topping.notify.error(`${file.name} 파일 형식이 지원되지 않습니다.`);
                return false;
            }

            return true;
        });

        if (validFiles.length !== files.length) {
            // Reset input if some files were invalid
            input.value = '';
        }
    }

    setupDependentFields() {
        const dependentFields = this.form.querySelectorAll('[data-depends-on]');
        dependentFields.forEach(field => {
            const dependsOn = field.dataset.dependsOn;
            const parentField = this.form.querySelector(`[name="${dependsOn}"]`);

            if (parentField) {
                parentField.addEventListener('change', () => {
                    this.updateDependentField(field, parentField.value);
                });
            }
        });
    }

    updateDependentField(field, parentValue) {
        const condition = field.dataset.condition;
        const conditionValue = field.dataset.conditionValue;

        let shouldShow = false;

        switch (condition) {
            case 'equals':
                shouldShow = parentValue === conditionValue;
                break;
            case 'not-equals':
                shouldShow = parentValue !== conditionValue;
                break;
            case 'contains':
                shouldShow = parentValue.includes(conditionValue);
                break;
            default:
                shouldShow = !!parentValue;
        }

        field.style.display = shouldShow ? 'block' : 'none';
        if (!shouldShow) {
            // Clear field value when hidden
            if (field.type === 'checkbox' || field.type === 'radio') {
                field.checked = false;
            } else {
                field.value = '';
            }
        }
    }

    setupSubmitHandling() {
        this.form.addEventListener('submit', (e) => {
            e.preventDefault();
            this.handleSubmit();
        });
    }

    async handleSubmit() {
        if (!this.validator.validateAll()) {
            window.Topping.notify.error('입력값을 확인해주세요.');
            return;
        }

        const submitButton = this.form.querySelector('button[type="submit"]');
        const originalText = submitButton.textContent;

        try {
            submitButton.disabled = true;
            submitButton.textContent = '처리 중...';

            const formData = new FormData(this.form);
            const action = this.form.action;
            const method = this.form.method.toUpperCase();

            let response;
            if (method === 'POST') {
                response = await fetch(action, {
                    method: 'POST',
                    body: formData
                });
            } else {
                response = await fetch(action, {
                    method: method,
                    body: formData
                });
            }

            if (response.ok) {
                window.Topping.notify.success('성공적으로 저장되었습니다.');
                
                const redirect = this.form.dataset.successRedirect;
                if (redirect) {
                    setTimeout(() => {
                        window.location.href = redirect;
                    }, 1000);
                }
            } else {
                throw new Error('서버 오류가 발생했습니다.');
            }

        } catch (error) {
            console.error('Form submission failed:', error);
            window.Topping.notify.error('저장 중 오류가 발생했습니다.');
        } finally {
            submitButton.disabled = false;
            submitButton.textContent = originalText;
        }
    }
}

/* ===== GLOBAL INSTANCES ===== */

let userSession;
let navigationManager;

/* ===== INITIALIZATION ===== */

document.addEventListener('DOMContentLoaded', function() {
    // Initialize managers
    userSession = new UserSessionManager();
    navigationManager = new NavigationManager();

    // Enhance all forms
    const forms = document.querySelectorAll('form[data-enhance]');
    forms.forEach(form => {
        new FormEnhancer(form);
    });

    // Initialize data tables
    const tables = document.querySelectorAll('table[data-enhance]');
    tables.forEach(table => {
        new DataTableManager(`#${table.id}`, {
            sortable: table.dataset.sortable !== 'false',
            filterable: table.dataset.filterable !== 'false',
            paginated: table.dataset.paginated !== 'false'
        });
    });

    // Add CSS animations if not present
    if (!document.querySelector('#main-animations')) {
        const style = document.createElement('style');
        style.id = 'main-animations';
        style.textContent = `
            @keyframes fadeInOut {
                0% { opacity: 0; transform: translateY(-10px); }
                10%, 90% { opacity: 1; transform: translateY(0); }
                100% { opacity: 0; transform: translateY(-10px); }
            }
            
            .loading-overlay {
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(255, 255, 255, 0.8);
                display: flex;
                align-items: center;
                justify-content: center;
                z-index: 9999;
            }
        `;
        document.head.appendChild(style);
    }

});

/* ===== EXPORTS ===== */

// Make classes available globally
window.Topping = {
    ...window.Topping,
    UserSessionManager,
    NavigationManager,
    DataTableManager,
    FormEnhancer,
    userSession: () => userSession,
    navigate: (url) => navigationManager.navigateTo(url)
};