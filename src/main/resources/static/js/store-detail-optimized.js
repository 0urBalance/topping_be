// Store Detail Optimized JavaScript - Performance Focused
class StoreDetailManager {
    constructor() {
        this.currentImageIndex = 0;
        this.galleryImages = [];
        this.init();
    }

    init() {
        // Use single DOMContentLoaded listener
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => this.setupPage());
        } else {
            this.setupPage();
        }
    }

    setupPage() {
        this.initializeGallery();
        this.setupEventDelegation();
        this.setupLazyLoading();
        this.setupBackgroundImage();
        this.addBodyPadding();
    }

    // 🖼️ Gallery Management
    initializeGallery() {
        const imageElements = document.querySelectorAll('.gallery-trigger img');
        this.galleryImages = Array.from(imageElements).map(img => img.src);
        
        // Fallback images if none exist
        if (this.galleryImages.length === 0) {
            this.galleryImages = ['/image/topping_L.png'];
        }
    }

    // 🎯 Event Delegation - Single event listener for multiple elements
    setupEventDelegation() {
        // Use event delegation for better performance
        document.addEventListener('click', (e) => {
            const target = e.target.closest('[data-action]') || e.target;
            
            // Gallery triggers
            if (target.classList.contains('gallery-trigger')) {
                const index = parseInt(target.dataset.index, 10) || 0;
                this.openImageGallery(index);
            }
            
            // Product cards
            else if (target.closest('.clickable-product')) {
                const card = target.closest('.clickable-product');
                const productUuid = card.dataset.productUuid;
                if (productUuid && !target.closest('button')) {
                    this.navigateToProduct(productUuid);
                }
            }
            
            // Action buttons
            else if (target.classList.contains('wishlist-btn') || target.closest('.wishlist-btn')) {
                e.preventDefault();
                this.toggleStoreLike(target.closest('.wishlist-btn'));
            }
            else if (target.classList.contains('share-btn') || target.closest('.share-btn')) {
                e.preventDefault();
                this.shareStore();
            }
            else if (target.classList.contains('collaboration-btn') || target.closest('.collaboration-btn')) {
                e.preventDefault();
                this.handleCollaborationRequest(target.closest('.collaboration-btn'));
            }
            
            // Collaboration list toggle
            else if (target.closest('.collab-badge')) {
                this.toggleCollaborationList();
            }
            else if (target.closest('.toggle-btn')) {
                this.toggleCollaborationList();
            }
        });

        // Keyboard navigation
        document.addEventListener('keydown', (e) => {
            const modal = document.getElementById('imageGalleryModal');
            if (modal && modal.classList.contains('show')) {
                if (e.key === 'ArrowLeft') this.prevImage();
                else if (e.key === 'ArrowRight') this.nextImage();
                else if (e.key === 'Escape') this.closeImageGallery();
            }
        });
    }

    // 🚀 Lazy Loading Implementation
    setupLazyLoading() {
        if ('IntersectionObserver' in window) {
            const imageObserver = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        const img = entry.target;
                        this.loadImage(img);
                        imageObserver.unobserve(img);
                    }
                });
            }, {
                rootMargin: '50px' // Load images 50px before they enter viewport
            });

            // Observe all lazy-load images
            document.querySelectorAll('.lazy-load').forEach(img => {
                imageObserver.observe(img);
            });
        } else {
            // Fallback for browsers without IntersectionObserver
            document.querySelectorAll('.lazy-load').forEach(img => {
                this.loadImage(img);
            });
        }
    }

    loadImage(img) {
        img.addEventListener('load', () => {
            img.classList.add('loaded');
        });

        img.addEventListener('error', () => {
            img.src = '/image/topping_L.png'; // Fallback image
            img.classList.add('loaded');
        });

        // Trigger loading if not already loaded
        if (img.dataset.src) {
            img.src = img.dataset.src;
        } else {
            img.classList.add('loaded');
        }
    }

    // 🎨 Dynamic Background Setup
    setupBackgroundImage() {
        const heroSection = document.querySelector('.hero-section');
        if (heroSection) {
            const backgroundImage = heroSection.style.backgroundImage;
            if (!backgroundImage || backgroundImage === 'url()') {
                heroSection.style.backgroundImage = 'url(/image/topping_L.png)';
            }
        }
    }

    addBodyPadding() {
        document.body.style.paddingBottom = '90px';
    }

    // 🖼️ Gallery Actions
    openImageGallery(index) {
        this.currentImageIndex = index;
        this.updateGalleryImage();
        const modal = this.getOrCreateModal('imageGalleryModal');
        if (modal && typeof bootstrap !== 'undefined') {
            const modalInstance = new bootstrap.Modal(modal);
            modalInstance.show();
        }
    }

    updateGalleryImage() {
        const galleryImage = document.getElementById('galleryImage');
        if (galleryImage && this.galleryImages[this.currentImageIndex]) {
            galleryImage.src = this.galleryImages[this.currentImageIndex];
            galleryImage.alt = `가게 사진 ${this.currentImageIndex + 1}`;
        }
    }

    prevImage() {
        this.currentImageIndex = (this.currentImageIndex - 1 + this.galleryImages.length) % this.galleryImages.length;
        this.updateGalleryImage();
    }

    nextImage() {
        this.currentImageIndex = (this.currentImageIndex + 1) % this.galleryImages.length;
        this.updateGalleryImage();
    }

    closeImageGallery() {
        const modal = document.getElementById('imageGalleryModal');
        if (modal && typeof bootstrap !== 'undefined') {
            const modalInstance = bootstrap.Modal.getInstance(modal);
            if (modalInstance) modalInstance.hide();
        }
    }

    // 🔗 Navigation
    navigateToProduct(productUuid) {
        if (productUuid) {
            window.location.href = `/products/${productUuid}`;
        }
    }

    // 💝 Social Actions
    async toggleStoreLike(button) {
        // Check authentication first
        if (!this.isAuthenticated()) {
            const currentUrl = encodeURIComponent(window.location.href);
            if (confirm('좋아요 기능을 사용하려면 로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?')) {
                window.location.href = `/auth/login?redirect=${currentUrl}`;
            }
            return;
        }

        const storeId = button.dataset.storeId;
        if (!storeId) {
            this.showToast('스토어 정보를 찾을 수 없습니다.', 'error');
            return;
        }

        // Disable button during request
        const originalDisabled = button.disabled;
        button.disabled = true;

        try {
            const response = await fetch(`/stores/api/${storeId}/like`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            const data = await response.json();

            if (data.success) {
                // Update UI based on API response
                const icon = button.querySelector('.material-symbols-outlined');
                const likeCountSpan = button.querySelector('.like-count');
                
                if (data.data.isLiked) {
                    button.classList.add('liked');
                    icon.textContent = 'favorite';
                    this.showToast('좋아요를 눌렀습니다!', 'success');
                } else {
                    button.classList.remove('liked');
                    icon.textContent = 'favorite_border';
                    this.showToast('좋아요를 취소했습니다.', 'info');
                }
                
                // Update like count
                if (likeCountSpan) {
                    likeCountSpan.textContent = data.data.likeCount;
                }
            } else {
                if (response.status === 401) {
                    const currentUrl = encodeURIComponent(window.location.href);
                    window.location.href = `/auth/login?redirect=${currentUrl}`;
                } else {
                    this.showToast(data.message || '좋아요 처리 중 오류가 발생했습니다.', 'error');
                }
            }
        } catch (error) {
            console.error('Like toggle error:', error);
            this.showToast('네트워크 오류가 발생했습니다. 다시 시도해주세요.', 'error');
        } finally {
            button.disabled = originalDisabled;
        }
    }

    shareStore() {
        const storeTitle = document.querySelector('h1')?.textContent || '가게';
        
        if (navigator.share) {
            navigator.share({
                title: `${storeTitle} - 토핑`,
                text: '이 가게를 확인해보세요!',
                url: window.location.href
            }).then(() => {
                this.showToast('링크가 공유되었습니다!', 'success');
            }).catch((error) => {
                // User cancelled sharing or error occurred
                if (error.name !== 'AbortError') {
                    this.fallbackShare();
                }
            });
        } else {
            this.fallbackShare();
        }
    }

    fallbackShare() {
        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(window.location.href).then(() => {
                this.showToast('링크가 복사되었습니다!', 'success');
            }).catch(() => {
                this.legacyCopyFallback();
            });
        } else {
            this.legacyCopyFallback();
        }
    }

    legacyCopyFallback() {
        try {
            // Fallback for older browsers
            const textArea = document.createElement('textarea');
            textArea.value = window.location.href;
            textArea.style.position = 'fixed';
            textArea.style.left = '-999999px';
            textArea.style.top = '-999999px';
            document.body.appendChild(textArea);
            textArea.focus();
            textArea.select();
            
            const successful = document.execCommand('copy');
            document.body.removeChild(textArea);
            
            if (successful) {
                this.showToast('링크가 복사되었습니다!', 'success');
            } else {
                this.showToast('링크 복사에 실패했습니다. 주소를 수동으로 복사해주세요.', 'error');
            }
        } catch (err) {
            console.error('Copy fallback failed:', err);
            this.showToast('링크 복사에 실패했습니다. 주소를 수동으로 복사해주세요.', 'error');
        }
    }

    // 🤝 Collaboration
    handleCollaborationRequest(button) {
        const storeId = button.dataset.storeId;
        if (storeId) {
            window.location.href = `/collaborations/apply?storeId=${storeId}`;
        } else {
            this.showToast('스토어 정보를 찾을 수 없습니다.', 'error');
        }
    }

    openCollaborationModal() {
        const modal = this.getOrCreateModal('collaborationModal');
        if (modal && typeof bootstrap !== 'undefined') {
            const modalInstance = new bootstrap.Modal(modal);
            modalInstance.show();
        }
    }

    toggleCollaborationList() {
        const collabInfo = document.querySelector('.collab-info');
        const collabList = document.getElementById('collab-list');
        
        if (!collabInfo || !collabList) return;
        
        // Toggle the open class on the collab-info container
        collabInfo.classList.toggle('open');
        
        // Toggle the display of the list
        const isHidden = collabList.style.display === 'none';
        collabList.style.display = isHidden ? 'block' : 'none';
        
        // Update aria attributes for accessibility
        const toggleIcon = collabInfo.querySelector('.toggle-icon');
        if (toggleIcon) {
            toggleIcon.setAttribute('aria-expanded', isHidden ? 'true' : 'false');
        }
    }

    // 🔧 Utilities
    isAuthenticated() {
        // Check if user is authenticated
        return document.body.dataset.authenticated === 'true' ||
               document.querySelector('meta[name="user-authenticated"]')?.content === 'true';
    }

    getOrCreateModal(modalId) {
        let modal = document.getElementById(modalId);
        if (!modal) {
            console.warn(`Modal with ID '${modalId}' not found`);
        }
        return modal;
    }

    showToast(message, type = 'info') {
        // Reuse existing toast system or create simple one
        const existingToast = document.querySelector('.toast-notification');
        if (existingToast) {
            existingToast.remove();
        }

        const toast = document.createElement('div');
        toast.className = `toast-notification toast-${type}`;
        toast.textContent = message;
        
        // Styles
        Object.assign(toast.style, {
            position: 'fixed',
            top: '20px',
            right: '20px',
            padding: '12px 20px',
            borderRadius: '8px',
            color: 'white',
            fontWeight: '500',
            zIndex: '9999',
            opacity: '0',
            transform: 'translateX(100px)',
            transition: 'all 0.3s ease-in-out'
        });
        
        // Type-specific colors
        const colors = {
            success: '#059669',
            error: '#dc2626',
            warning: '#856404',
            info: '#2563eb'
        };
        toast.style.backgroundColor = colors[type] || colors.info;
        
        document.body.appendChild(toast);
        
        // Animate in
        requestAnimationFrame(() => {
            toast.style.opacity = '1';
            toast.style.transform = 'translateX(0)';
        });
        
        // Remove after 3 seconds
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100px)';
            setTimeout(() => {
                if (toast.parentNode) {
                    toast.parentNode.removeChild(toast);
                }
            }, 300);
        }, 3000);
    }
}

// Initialize when script loads
const storeDetailManager = new StoreDetailManager();

// Legacy support for existing function calls
window.shareStore = () => storeDetailManager.shareStore();
window.openCollaborationModal = () => storeDetailManager.openCollaborationModal();
window.toggleCollaborationList = () => storeDetailManager.toggleCollaborationList();
window.toggleCollabList = () => storeDetailManager.toggleCollaborationList(); // Alias for template onclick
window.openProductDetail = (uuid) => storeDetailManager.navigateToProduct(uuid);

// Export for module use
if (typeof module !== 'undefined' && module.exports) {
    module.exports = StoreDetailManager;
}