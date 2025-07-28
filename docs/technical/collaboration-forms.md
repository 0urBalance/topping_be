# Collaboration Form System

## Auto-Selection Functionality

- **Store-Product Mapping**: `CollaborationController` generates JSON data mapping stores to their products using `ObjectMapper` 
- **Dynamic Population**: When user selects a partner store, JavaScript automatically populates category field and product list
- **Data Flow**: `generateStoreDataJson()` method creates store-product relationships for client-side consumption
- **Event Handling**: Enhanced JavaScript with comprehensive console logging for debugging form interactions
- **Template Integration**: JSON injection approach replaces complex nested Thymeleaf syntax to prevent parsing errors

## Enhanced Calendar UI

- **Custom Date Picker**: Modern calendar component with CSS framework variables and design tokens
- **Quick Actions**: Preset buttons for common date ranges (1 week, 2 weeks, 1 month)
- **Framework Compliance**: Uses `base.css` variables for consistent styling and animations
- **Keyboard Support**: Arrow key navigation and Enter/Escape key handling
- **Validation**: Client-side and server-side date range validation with user-friendly error messages

## Controller Implementation

- **Entity Creation**: Fixed critical bug where POST `/collaborations/apply` only validated but never saved entities
- **CollaborationProposal Persistence**: Implemented complete entity creation with proper field mapping
- **Dual Entity Support**: Handles both legacy `Collaboration` entities and new `CollaborationProposal` entities
- **Validation Layer**: Comprehensive validation for required fields, date ranges, and store/user relationships
- **Error Handling**: Specific error codes and redirect URLs for different validation failures

## MyPage Integration

- **Unified Applications View**: `MyPageController` displays both `Collaboration` and `CollaborationProposal` entities
- **Status Management**: Proper handling of PENDING, ACCEPTED, REJECTED statuses for both entity types
- **Data Aggregation**: Applications page (`/mypage/applications`) shows comprehensive view of all user submissions
- **Statistics**: Updated dashboard counters to include proposal counts alongside collaboration counts

## Technical Resolution Details

- **Thymeleaf Parsing Fix**: Resolved "Malformed template: unnamed element is never closed" error
- **JSON Injection Approach**: Replaced `th:if` and `th:each` within JavaScript with server-side JSON generation
- **Product Selection Bug**: Fixed "store selection not populating products" with enhanced event handling
- **MyPage Display Bug**: Fixed "proposals not appearing in applications" with proper entity querying
- **Role-Based Forms**: Different form behavior for ROLE_USER vs ROLE_BUSINESS_OWNER with appropriate data loading

## API Endpoints

- **Form Display**: `GET /collaborations/apply` - Role-based form rendering with store-product data
- **Form Submission**: `POST /collaborations/apply` - Complete entity creation and validation
- **MyPage Integration**: `GET /mypage/applications` - Unified view of all collaboration submissions
- **Error Handling**: Redirect-based error reporting with specific error codes

## Best Practices

- **Collaboration Forms**: Use JSON injection approach for store-product data: `generateStoreDataJson()` + `th:inline="javascript"`
- **Auto-Selection**: Implement comprehensive console logging for debugging dynamic form population
- **Form Validation**: Always implement both client-side and server-side validation for collaboration forms
- **Collaboration Forms**: Use server-side JSON generation with `ObjectMapper` for complex store-product data instead of nested Thymeleaf loops