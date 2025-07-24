// Store Detail Page JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // Initialize page
    initializePage();
});

// Global variables for image gallery
let currentImageIndex = 0;
const galleryImages = [
    '/image/topping_L.png',
    '/image/topping_M.png',
    '/image/topping_S.png'
];

// Initialize page functionality
function initializePage() {
    // Add padding to body for fixed bottom bar
    document.body.style.paddingBottom = '90px';
    
    // Set up event listeners
    setupEventListeners();
    
    // Initialize tooltips if Bootstrap is available
    if (typeof bootstrap !== 'undefined') {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    }
}

// Set up event listeners
function setupEventListeners() {
    // Collaboration form submission
    const collaborationForm = document.getElementById('collaborationForm');
    if (collaborationForm) {
        collaborationForm.addEventListener('submit', handleCollaborationSubmit);
    }
    
    // Keyboard navigation for image gallery
    document.addEventListener('keydown', function(e) {
        const modal = document.getElementById('imageGalleryModal');
        if (modal && modal.classList.contains('show')) {
            if (e.key === 'ArrowLeft') {
                prevImage();
            } else if (e.key === 'ArrowRight') {
                nextImage();
            }
        }
    });
}

// Toggle collaboration list
function toggleCollaborationList() {
    const collaborationList = document.getElementById('collaboration-list');
    const chevron = document.getElementById('collab-chevron');
    
    if (collaborationList.style.display === 'none' || collaborationList.style.display === '') {
        collaborationList.style.display = 'block';
        collaborationList.classList.add('show');
        chevron.classList.add('active');
    } else {
        collaborationList.style.display = 'none';
        collaborationList.classList.remove('show');
        chevron.classList.remove('active');
    }
}

// Open map modal
function openMapModal() {
    const modal = new bootstrap.Modal(document.getElementById('mapModal'));
    modal.show();
}

// Open image gallery modal
function openImageGallery(index) {
    currentImageIndex = index;
    updateGalleryImage();
    const modal = new bootstrap.Modal(document.getElementById('imageGalleryModal'));
    modal.show();
}

// Update gallery image
function updateGalleryImage() {
    const galleryImage = document.getElementById('galleryImage');
    if (galleryImage && galleryImages[currentImageIndex]) {
        galleryImage.src = galleryImages[currentImageIndex];
        galleryImage.alt = `가게 사진 ${currentImageIndex + 1}`;
    }
}

// Navigate to previous image
function prevImage() {
    currentImageIndex = (currentImageIndex - 1 + galleryImages.length) % galleryImages.length;
    updateGalleryImage();
}

// Navigate to next image
function nextImage() {
    currentImageIndex = (currentImageIndex + 1) % galleryImages.length;
    updateGalleryImage();
}

// Open menu detail modal
function openMenuDetail(menuName) {
    const modal = new bootstrap.Modal(document.getElementById('menuDetailModal'));
    const modalTitle = document.getElementById('menuModalTitle');
    const modalBody = document.getElementById('menuModalBody');
    
    modalTitle.textContent = menuName;
    
    // Mock menu details - replace with actual data
    const menuDetails = getMenuDetails(menuName);
    modalBody.innerHTML = `
        <div class="menu-detail-content">
            <img src="${menuDetails.image}" alt="${menuName}" class="menu-detail-image">
            <div class="menu-detail-info">
                <h4>${menuName}</h4>
                <p class="menu-description">${menuDetails.description}</p>
                <div class="menu-price">${menuDetails.price}</div>
                <div class="menu-reviews">리뷰 ${menuDetails.reviews}개</div>
                <div class="menu-ingredients">
                    <h6>주요 재료</h6>
                    <p>${menuDetails.ingredients}</p>
                </div>
            </div>
        </div>
    `;
    
    modal.show();
}

// Get menu details (mock data)
function getMenuDetails(menuName) {
    const menuData = {
        '옥수수크림라떼': {
            image: '/image/topping_M.png',
            description: '진한 옥수수와 부드러운 크림이 어우러진 라떼입니다. 달콤하고 고소한 맛이 일품입니다.',
            price: '7,000원',
            reviews: 39,
            ingredients: '에스프레소, 옥수수 시럽, 우유, 휘핑크림'
        },
        '아메리카노': {
            image: '/image/topping_S.png',
            description: '깔끔하고 고소한 맛이 일품인 아메리카노입니다. 원두 본연의 맛을 느낄 수 있습니다.',
            price: '5,500원',
            reviews: 552,
            ingredients: '에스프레소, 물'
        },
        '크로와상샌드위치': {
            image: '/image/topping_L.png',
            description: '바삭한 크로와상에 신선한 채소와 고급 치즈가 들어간 샌드위치입니다.',
            price: '10,900원',
            reviews: 108,
            ingredients: '크로와상, 치즈, 양상추, 토마토, 햄'
        },
        '리치에이드': {
            image: '/image/topping_M.png',
            description: '상큼하고 달콤한 리치의 맛을 느낄 수 있는 에이드입니다.',
            price: '7,300원',
            reviews: 37,
            ingredients: '리치 시럽, 탄산수, 얼음'
        },
        '차이버블샌드위치': {
            image: '/image/topping_S.png',
            description: '특제 소스와 차이버블이 들어간 독특한 샌드위치입니다.',
            price: '23,000원',
            reviews: 8,
            ingredients: '식빵, 차이버블, 특제소스, 채소'
        },
        '바닐라라떼': {
            image: '/image/topping_L.png',
            description: '진한 에스프레소와 달콤한 바닐라의 조화가 완벽한 라떼입니다.',
            price: '6,500원',
            reviews: 63,
            ingredients: '에스프레소, 우유, 바닐라 시럽'
        }
    };
    
    return menuData[menuName] || {
        image: '/image/topping_L.png',
        description: '메뉴에 대한 자세한 정보가 준비 중입니다.',
        price: '0원',
        reviews: 0,
        ingredients: '정보 준비중'
    };
}

// Open collaboration modal
function openCollaborationModal() {
    const modal = new bootstrap.Modal(document.getElementById('collaborationModal'));
    modal.show();
}

// Handle collaboration form submission
function handleCollaborationSubmit(e) {
    e.preventDefault();
    
    const message = document.getElementById('message').value.trim();
    if (!message) {
        alert('요청 메시지를 입력해주세요.');
        return;
    }
    
    // Show loading state
    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;
    submitBtn.innerHTML = '<span class="spinner"></span>처리중...';
    submitBtn.disabled = true;
    
    // Simulate API call
    setTimeout(() => {
        alert('콜라보 요청이 성공적으로 전송되었습니다!');
        
        // Reset form
        document.getElementById('message').value = '';
        
        // Reset button
        submitBtn.textContent = originalText;
        submitBtn.disabled = false;
        
        // Close modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('collaborationModal'));
        modal.hide();
    }, 1500);
}

// Toggle like status
function toggleLike() {
    const likeBtn = document.querySelector('.like-btn');
    const likeCount = likeBtn.querySelector('.count');
    let currentCount = parseInt(likeCount.textContent);
    
    if (likeBtn.classList.contains('active')) {
        // Unlike
        likeBtn.classList.remove('active');
        likeCount.textContent = currentCount - 1;
        showToast('좋아요를 취소했습니다.', 'info');
    } else {
        // Like
        likeBtn.classList.add('active');
        likeCount.textContent = currentCount + 1;
        showToast('좋아요를 눌렀습니다!', 'success');
    }
    
    // TODO: Send API request to update like status
    // updateLikeStatus(storeId, likeBtn.classList.contains('active'));
}

// Toggle wishlist status
function toggleWishlist() {
    // Check if user is authenticated
    const isAuthenticated = document.querySelector('body').dataset.authenticated === 'true';
    
    if (!isAuthenticated) {
        if (confirm('찜하기 기능을 사용하려면 로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?')) {
            window.location.href = '/auth/login';
        }
        return;
    }
    
    const wishlistBtn = document.querySelector('.wishlist-btn, .wishlist-toggle-btn');
    const wishlistCount = wishlistBtn.querySelector('.count');
    let currentCount = parseInt(wishlistCount?.textContent || '0');
    
    if (wishlistBtn.classList.contains('active') || wishlistBtn.classList.contains('wishlisted')) {
        // Remove from wishlist
        wishlistBtn.classList.remove('active', 'wishlisted');
        if (wishlistCount) {
            wishlistCount.textContent = currentCount - 1;
        }
        showToast('찜 목록에서 제거했습니다.', 'info');
    } else {
        // Add to wishlist
        wishlistBtn.classList.add('active', 'wishlisted');
        if (wishlistCount) {
            wishlistCount.textContent = currentCount + 1;
        }
        showToast('찜 목록에 추가했습니다!', 'success');
    }
    
    // TODO: Send API request to update wishlist status
    // updateWishlistStatus(storeId, wishlistBtn.classList.contains('active'));
}

// Show toast notification
function showToast(message, type = 'info') {
    // Create toast element
    const toast = document.createElement('div');
    toast.className = `toast-notification toast-${type}`;
    toast.textContent = message;
    
    // Add styles
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
    
    // Set background color based on type
    const colors = {
        success: '#059669',
        error: '#dc2626',
        warning: '#856404',
        info: '#2563eb'
    };
    toast.style.backgroundColor = colors[type] || colors.info;
    
    // Add to DOM
    document.body.appendChild(toast);
    
    // Animate in
    setTimeout(() => {
        toast.style.opacity = '1';
        toast.style.transform = 'translateX(0)';
    }, 100);
    
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

// Handle image loading errors
document.addEventListener('error', function(e) {
    if (e.target.tagName === 'IMG') {
        e.target.src = '/image/topping_L.png'; // Fallback image
        e.target.alt = 'Image not available';
    }
}, true);

// Smooth scroll to sections
function scrollToSection(sectionId) {
    const section = document.getElementById(sectionId);
    if (section) {
        section.scrollIntoView({
            behavior: 'smooth',
            block: 'start'
        });
    }
}

// Handle window resize
window.addEventListener('resize', function() {
    // Adjust modal sizes if needed
    const openModals = document.querySelectorAll('.modal.show');
    openModals.forEach(modal => {
        // Force recalculation of modal positioning
        if (typeof bootstrap !== 'undefined') {
            const modalInstance = bootstrap.Modal.getInstance(modal);
            if (modalInstance) {
                modalInstance.handleUpdate();
            }
        }
    });
});

// Lazy loading for images
function setupLazyLoading() {
    if ('IntersectionObserver' in window) {
        const images = document.querySelectorAll('img[data-src]');
        const imageObserver = new IntersectionObserver((entries, observer) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const img = entry.target;
                    img.src = img.dataset.src;
                    img.classList.remove('lazy');
                    imageObserver.unobserve(img);
                }
            });
        });
        
        images.forEach(img => imageObserver.observe(img));
    }
}

// Initialize lazy loading when DOM is ready
document.addEventListener('DOMContentLoaded', setupLazyLoading);

// Add CSS for menu detail modal
const menuDetailStyles = `
    .menu-detail-content {
        display: flex;
        gap: 20px;
        flex-direction: column;
    }
    
    .menu-detail-image {
        width: 100%;
        height: 200px;
        object-fit: cover;
        border-radius: 8px;
    }
    
    .menu-detail-info h4 {
        font-size: 1.5rem;
        font-weight: 600;
        color: #333;
        margin-bottom: 10px;
    }
    
    .menu-description {
        color: #666;
        margin-bottom: 15px;
        line-height: 1.6;
    }
    
    .menu-price {
        font-size: 1.3rem;
        font-weight: 600;
        color: #ff6b35;
        margin-bottom: 8px;
    }
    
    .menu-reviews {
        color: #999;
        margin-bottom: 20px;
    }
    
    .menu-ingredients h6 {
        font-weight: 600;
        color: #333;
        margin-bottom: 8px;
    }
    
    .menu-ingredients p {
        color: #666;
        margin: 0;
    }
    
    @media (min-width: 768px) {
        .menu-detail-content {
            flex-direction: row;
        }
        
        .menu-detail-image {
            width: 200px;
            height: 200px;
            flex-shrink: 0;
        }
    }
`;

// Add styles to document
const styleSheet = document.createElement('style');
styleSheet.textContent = menuDetailStyles;
document.head.appendChild(styleSheet);