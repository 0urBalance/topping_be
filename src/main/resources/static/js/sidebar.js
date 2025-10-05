// Sidebar Navigation JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM Content Loaded - Initializing sidebar');
    initializeSidebar();
});

function initializeSidebar() {
    // Set up event listeners
    setupEventListeners();
    
    // Handle active menu item highlighting - DISABLED: Let Thymeleaf handle active classes
    // highlightActiveMenuItem();
    
    // Initialize image press effects
    initializeImagePressEffects();
    
    // Handle responsive behavior
    handleResponsiveLayout();
}

function setupEventListeners() {
    // Mobile menu toggle
    const mobileMenuToggle = document.getElementById('mobileMenuToggle');
    console.log('Mobile menu toggle element:', mobileMenuToggle);
    if (mobileMenuToggle) {
        console.log('Adding click event listener to mobile menu toggle');
        mobileMenuToggle.addEventListener('click', function(e) {
            console.log('Mobile menu toggle clicked!', e);
            toggleSidebar();
        });
    } else {
        console.error('Mobile menu toggle element not found!');
    }
    
    // Sidebar overlay
    const sidebarOverlay = document.getElementById('sidebarOverlay');
    if (sidebarOverlay) {
        sidebarOverlay.addEventListener('click', closeSidebar);
    }
    
    // Keyboard navigation
    document.addEventListener('keydown', handleKeyboardNavigation);
    
    // Window resize handling
    window.addEventListener('resize', handleWindowResize);
    
    // Close sidebar when clicking on menu items on mobile
    const menuLinks = document.querySelectorAll('.menu-link, .auth-link');
    menuLinks.forEach(link => {
        link.addEventListener('click', function() {
            if (window.innerWidth <= 768) {
                closeSidebar();
            }
        });
    });
}

// Image Press Effects Functions
function initializeImagePressEffects() {
    const menuIcons = document.querySelectorAll('.menu-icon[data-default][data-pressed]');
    console.log('🎨 Initializing image press effects for', menuIcons.length, 'icons');
    
    menuIcons.forEach((icon, index) => {
        console.log(`🔧 Setting up icon ${index + 1}:`, icon.dataset.menu, {
            default: icon.dataset.default,
            pressed: icon.dataset.pressed,
            hasDefault: !!icon.dataset.default,
            hasPressed: !!icon.dataset.pressed
        });
        
        // Add press effect event listeners
        addImagePressListeners(icon);
    });
    
    // Set initial active state for current page
    console.log('🎯 Setting initial active state');
    setActiveMenuImage();
}

function addImagePressListeners(icon) {
    if (!icon) {
        console.error('❌ addImagePressListeners: icon is null or undefined');
        return;
    }
    
    console.log('🎯 Adding press listeners to menu:', icon.dataset.menu);
    
    // Mouse events
    icon.addEventListener('mousedown', () => setImagePressed(icon, true));
    icon.addEventListener('mouseup', () => setImagePressed(icon, false));
    icon.addEventListener('mouseleave', () => setImagePressed(icon, false));
    
    // Touch events for mobile
    icon.addEventListener('touchstart', (e) => {
        e.preventDefault();
        setImagePressed(icon, true);
    });
    icon.addEventListener('touchend', (e) => {
        e.preventDefault();
        setImagePressed(icon, false);
    });
    icon.addEventListener('touchcancel', () => setImagePressed(icon, false));
    
    // Prevent drag
    icon.addEventListener('dragstart', (e) => e.preventDefault());
}

function setImagePressed(icon, pressed) {
    if (!icon) {
        console.error('❌ setImagePressed: icon is null or undefined');
        return;
    }
    
    if (pressed) {
        if (icon.dataset.pressed) {
            icon.src = icon.dataset.pressed;
            icon.classList.add('pressed');
            console.log('👆 Icon pressed:', icon.dataset.menu, icon.src);
        } else {
            console.error('❌ Missing pressed image data for icon:', icon.dataset.menu);
        }
    } else {
        // Remove pressed class immediately
        icon.classList.remove('pressed');
        console.log('👆 Icon released:', icon.dataset.menu);
        
        // Reset to current page state after a short delay
        setTimeout(() => {
            setActiveMenuImage(); // This will set the correct image based on current page
        }, 100);
    }
}

function isActiveMenuItem(icon) {
    const menuItem = icon.closest('.menu-item');
    const link = menuItem.querySelector('a');
    
    // Standard check for active class - rely on Thymeleaf to set this correctly
    return link && link.classList.contains('active');
}

function setActiveMenuImage() {
    const currentPath = window.location.pathname;
    const menuIcons = document.querySelectorAll('.menu-icon[data-menu]');
    console.log('🔍 setActiveMenuImage called, currentPath:', currentPath);
    console.log('📊 Found menu icons:', menuIcons.length);
    
    // First, reset all icons to default state
    menuIcons.forEach(icon => {
        if (icon.dataset.default) {
            icon.src = icon.dataset.default;
            icon.classList.remove('active-menu');
        }
    });
    
    // Then, find and activate only the correct menu item
    menuIcons.forEach(icon => {
        const menuType = icon.dataset.menu;
        let isActive = false;
        
        // Determine if this menu should be active based on current path
        switch (menuType) {
            case 'home':
                isActive = currentPath === '/' || currentPath === '/home';
                break;
            case 'explore':
                isActive = currentPath.startsWith('/explore');
                break;
            case 'chat':
                isActive = currentPath.startsWith('/chat');
                break;
            case 'mypage':
                isActive = currentPath.startsWith('/mypage');
                break;
            case 'signup':
                isActive = currentPath.startsWith('/signup');
                break;
            default:
                console.warn('⚠️ Unknown menu type:', menuType);
        }
        
        console.log('🔍 Menu check:', menuType, 'isActive:', isActive, 'path:', currentPath);
        
        // Set image state only for the active menu
        if (isActive) {
            if (icon.dataset.pressed) {
                console.log('✅ Setting active icon for menu:', menuType, 'pressed image:', icon.dataset.pressed);
                icon.src = icon.dataset.pressed;
                icon.classList.add('active-menu');
            } else {
                console.error('❌ Missing pressed image data for menu:', menuType);
            }
        }
    });
}

function resetAllMenuImages() {
    const menuIcons = document.querySelectorAll('.menu-icon[data-default]');
    menuIcons.forEach(icon => {
        icon.src = icon.dataset.default;
        icon.classList.remove('pressed', 'active-menu');
    });
}

function toggleSidebar() {
    console.log('toggleSidebar function called');
    const sidebar = document.getElementById('sidebar');
    const mobileMenuToggle = document.getElementById('mobileMenuToggle');
    const sidebarOverlay = document.getElementById('sidebarOverlay');
    
    console.log('Sidebar element:', sidebar);
    console.log('Sidebar has active class:', sidebar?.classList.contains('active'));
    
    if (sidebar && sidebar.classList.contains('active')) {
        console.log('Closing sidebar');
        closeSidebar();
    } else {
        console.log('Opening sidebar');
        openSidebar();
    }
}

function openSidebar() {
    console.log('openSidebar function called');
    const sidebar = document.getElementById('sidebar');
    const mobileMenuToggle = document.getElementById('mobileMenuToggle');
    const sidebarOverlay = document.getElementById('sidebarOverlay');
    
    console.log('Elements found - sidebar:', !!sidebar, 'toggle:', !!mobileMenuToggle, 'overlay:', !!sidebarOverlay);
    
    if (sidebar) sidebar.classList.add('active');
    if (mobileMenuToggle) mobileMenuToggle.classList.add('active');
    if (sidebarOverlay) sidebarOverlay.classList.add('active');
    
    // Prevent body scroll when sidebar is open on mobile
    if (window.innerWidth <= 768) {
        document.body.style.overflow = 'hidden';
    }
    
    // Focus first menu item for accessibility
    const firstMenuItem = sidebar.querySelector('.menu-link');
    if (firstMenuItem) {
        setTimeout(() => firstMenuItem.focus(), 300);
    }
}

function closeSidebar() {
    const sidebar = document.getElementById('sidebar');
    const mobileMenuToggle = document.getElementById('mobileMenuToggle');
    const sidebarOverlay = document.getElementById('sidebarOverlay');
    
    sidebar.classList.remove('active');
    mobileMenuToggle.classList.remove('active');
    sidebarOverlay.classList.remove('active');
    
    // Restore body scroll
    document.body.style.overflow = '';
    
    // Return focus to toggle button
    if (mobileMenuToggle) {
        mobileMenuToggle.focus();
    }
}

function handleKeyboardNavigation(e) {
    const sidebar = document.getElementById('sidebar');
    
    // ESC key closes sidebar on mobile
    if (e.key === 'Escape' && sidebar.classList.contains('active')) {
        closeSidebar();
        return;
    }
    
    // Handle arrow key navigation within sidebar
    if (document.activeElement && isWithinSidebar(document.activeElement)) {
        const menuItems = Array.from(sidebar.querySelectorAll('.menu-link, .auth-link'));
        const currentIndex = menuItems.indexOf(document.activeElement);
        
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            const nextIndex = (currentIndex + 1) % menuItems.length;
            menuItems[nextIndex].focus();
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            const prevIndex = (currentIndex - 1 + menuItems.length) % menuItems.length;
            menuItems[prevIndex].focus();
        }
    }
}

function isWithinSidebar(element) {
    const sidebar = document.getElementById('sidebar');
    return sidebar.contains(element);
}

function highlightActiveMenuItem() {
    const currentPath = window.location.pathname;
    const menuLinks = document.querySelectorAll('.menu-link, .auth-link');
    
    menuLinks.forEach(link => {
        const href = link.getAttribute('href');
        
        // Remove existing active classes
        link.classList.remove('active');
        
        // Add active class for exact matches or path starts with href
        if (href && (currentPath === href || 
            (href !== '/' && currentPath.startsWith(href)))) {
            link.classList.add('active');
        }
    });
    
    // Special handling for home page
    if (currentPath === '/' || currentPath === '/home') {
        const logoLink = document.querySelector('.logo-link');
        if (logoLink) {
            logoLink.classList.add('active');
        }
    }
    
    // Update menu images after highlighting active items - DISABLED
    // setActiveMenuImage();
}

function handleResponsiveLayout() {
    const handleResize = () => {
        const sidebar = document.getElementById('sidebar');
        const sidebarOverlay = document.getElementById('sidebarOverlay');
        
        if (window.innerWidth > 768) {
            // Desktop: always show sidebar, remove mobile classes
            sidebar.classList.remove('active');
            sidebarOverlay.classList.remove('active');
            document.body.style.overflow = '';
        } else {
            // Mobile: hide sidebar by default
            if (!sidebar.classList.contains('active')) {
                sidebar.classList.remove('active');
            }
        }
    };
    
    // Initial check
    handleResize();
    
    // Add to window resize handler
    window.addEventListener('resize', handleResize);
}

function handleWindowResize() {
    // Debounce resize events
    clearTimeout(window.resizeTimeout);
    window.resizeTimeout = setTimeout(() => {
        handleResponsiveLayout();
    }, 150);
}

// Utility functions for external use
window.sidebarUtils = {
    open: openSidebar,
    close: closeSidebar,
    toggle: toggleSidebar,
    
    // Method to programmatically set active menu item
    setActiveMenuItem: function(href) {
        const menuLinks = document.querySelectorAll('.menu-link, .auth-link');
        menuLinks.forEach(link => {
            link.classList.remove('active');
            if (link.getAttribute('href') === href) {
                link.classList.add('active');
            }
        });
        // Update images after setting active menu item - Let Thymeleaf handle active classes
        // setActiveMenuImage();
    },
    
    // Method to add loading state to menu item
    setMenuItemLoading: function(href, loading = true) {
        const menuLink = document.querySelector(`[href="${href}"]`);
        if (menuLink) {
            if (loading) {
                menuLink.classList.add('loading');
                menuLink.style.pointerEvents = 'none';
            } else {
                menuLink.classList.remove('loading');
                menuLink.style.pointerEvents = '';
            }
        }
    },
    
    // Method to manually refresh menu image states
    refreshMenuImages: function() {
        setActiveMenuImage();
    },
    
    // Method to reset all menu images to default state
    resetMenuImages: function() {
        resetAllMenuImages();
    }
};

// Auto-close sidebar when clicking outside on mobile
document.addEventListener('click', function(e) {
    const sidebar = document.getElementById('sidebar');
    const mobileMenuToggle = document.getElementById('mobileMenuToggle');
    
    if (window.innerWidth <= 768 && 
        sidebar.classList.contains('active') &&
        !sidebar.contains(e.target) && 
        !mobileMenuToggle.contains(e.target)) {
        closeSidebar();
    }
});

// Handle touch gestures for mobile sidebar
let touchStartX = 0;
let touchStartY = 0;

document.addEventListener('touchstart', function(e) {
    touchStartX = e.touches[0].clientX;
    touchStartY = e.touches[0].clientY;
});

document.addEventListener('touchmove', function(e) {
    if (window.innerWidth > 768) return;
    
    const touchCurrentX = e.touches[0].clientX;
    const touchCurrentY = e.touches[0].clientY;
    const diffX = touchCurrentX - touchStartX;
    const diffY = touchCurrentY - touchStartY;
    
    // Horizontal swipe detection
    if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > 50) {
        const sidebar = document.getElementById('sidebar');
        
        // Swipe right from left edge to open sidebar
        if (diffX > 0 && touchStartX < 50 && !sidebar.classList.contains('active')) {
            openSidebar();
        }
        // Swipe left to close sidebar
        else if (diffX < -50 && sidebar.classList.contains('active')) {
            closeSidebar();
        }
    }
});

// Intersection Observer for menu item animations
if ('IntersectionObserver' in window) {
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -20px 0px'
    };
    
    const menuObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateX(0)';
            }
        });
    }, observerOptions);
    
    // Observe menu items for entrance animations
    document.addEventListener('DOMContentLoaded', () => {
        const menuItems = document.querySelectorAll('.menu-item');
        menuItems.forEach((item, index) => {
            item.style.opacity = '0';
            item.style.transform = 'translateX(-20px)';
            item.style.transition = `opacity 0.3s ease ${index * 0.1}s, transform 0.3s ease ${index * 0.1}s`;
            menuObserver.observe(item);
        });
    });
}

// Performance optimization: use requestAnimationFrame for smooth animations
function smoothToggleSidebar() {
    requestAnimationFrame(() => {
        toggleSidebar();
    });
}

// Export functions for global access
window.toggleSidebar = smoothToggleSidebar;
window.openSidebar = openSidebar;
window.closeSidebar = closeSidebar;

// Enhanced sidebar functionality - work with inline function
if (!window.toggleSidebar) {
    window.toggleSidebar = function() {
        console.log('Sidebar.js toggleSidebar called');
        if (typeof smoothToggleSidebar === 'function') {
            smoothToggleSidebar();
        } else {
            console.log('Using basic toggle sidebar');
            const sidebar = document.getElementById('sidebar');
            const overlay = document.getElementById('sidebarOverlay');
            const toggle = document.getElementById('mobileMenuToggle');
            
            if (sidebar) {
                const isActive = sidebar.classList.contains('active');
                if (isActive) {
                    sidebar.classList.remove('active');
                    if (overlay) overlay.classList.remove('active');
                    if (toggle) toggle.classList.remove('active');
                    document.body.style.overflow = '';
                } else {
                    sidebar.classList.add('active');
                    if (overlay) overlay.classList.add('active');
                    if (toggle) toggle.classList.add('active');
                    document.body.style.overflow = 'hidden';
                }
            }
        }
    };
}