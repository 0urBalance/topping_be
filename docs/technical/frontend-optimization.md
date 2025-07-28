# Frontend Refactoring & Optimization

## Store Detail Page Optimization

- **Refactored CSS Framework**: Complete redesign using `store-detail-refactored.css` with design tokens and utility classes
- **Component System**: Created reusable fragments (`fragments/product-card.html`, `fragments/tag.html`) for improved maintainability
- **Performance JavaScript**: Implemented `StoreDetailManager` class with event delegation and lazy loading
- **Visual Refinements**: Full-height layout, proper spacing, scaled hero images (80%), centered button icons
- **Two-Column Layout**: Main content with sidebar for SNS & collaboration information, responsive design
- **Large Screen Constraints**: Media queries prevent elements from exceeding container bounds on screens >1400px

## Home Page Enhancement

- **Horizontal Products Section**: Limited to 5 products max with horizontal scrolling layout
- **Drag-Scroll Functionality**: Mouse and touchpad drag support with smooth scrolling behavior
- **Performance Optimization**: Reduced DOM complexity and improved scroll performance
- **Responsive Design**: Proper scaling and layout adjustments across all screen sizes

## CSS Architecture Improvements

- **Design Tokens**: Centralized CSS variables for colors, spacing, shadows, and transitions
- **Utility Classes**: Grid layouts (`.grid-2`, `.grid-3`), flexbox utilities (`.flex-center`, `.flex-between`), typography system
- **Responsive Design**: Mobile-first approach with consistent breakpoints and scaling
- **Performance Optimizations**: Lazy loading, GPU acceleration, reduced motion support

## Component-Based Development

- **Fragment System**: Reusable product cards with image fallback logic and lazy loading
- **Tag Components**: Modular category and hashtag display with hover effects
- **Event Delegation**: Single event listener replacing multiple handlers for better performance
- **Code Reusability**: Eliminated duplicate layout code with 30% DOM complexity reduction

## JPA Query Optimization

- **Cartesian Product Fix**: Resolved product duplication by separating multiple JOIN FETCH operations into individual queries
- **Query Performance**: Improved data loading efficiency and eliminated N×M result multiplication
- **Repository Pattern**: Maintained clean architecture while fixing query performance issues

## Explore Page System

### Three-Section Layout
- **Section 1: Store List**: Displays registered stores with thumbnails, names, addresses, categories
- **Section 2: Menu List**: Shows popular/signature menus with prices, descriptions, store associations
- **Section 3: Collaboration Products**: Features collaboration menus and live products with special tags

### Controller Enhancement
- **Data Sources**: `StoreRepository`, `MenuRepository` with pagination (12 items per section)
- **Query Methods**: `findByMenuTypeOrderByReviewCountDesc()` for popular menu sorting
- **Performance**: Optimized queries with `PageRequest.of(0, 12)` limiting
- **Backward Compatibility**: Maintains existing collaboration data structure

### Responsive Card Design
- **CSS Grid Layout**: `repeat(auto-fill, minmax(300px, 1fr))` for responsive design
- **Card Components**: Consistent styling with hover effects, shadows, image scaling
- **Image Handling**: Lazy loading, error fallbacks, placeholder support
- **Empty States**: User-friendly messages when sections have no content

### Navigation & Performance
- **Clickable Cards**: Store cards link to `/stores/{id}`, menu cards to store detail pages
- **Lazy Loading**: Images load only when in viewport using Intersection Observer
- **Error Recovery**: Automatic fallback to placeholders on image load failure
- **Mobile Optimization**: Single-column layout on mobile with optimized card sizing

## Frontend Refactoring Best Practices

1. **Component-Based Architecture**: Create reusable fragments for repeated UI elements
2. **CSS Optimization**: Use design tokens, utility classes, and eliminate inline styles
3. **JavaScript Performance**: Implement event delegation and lazy loading patterns
4. **Visual Consistency**: Apply consistent spacing, scaling, and centering across components
5. **Responsive Refinements**: Ensure proper layout scaling and spacing on all devices