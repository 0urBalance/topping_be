# Kakao Social Login Configuration Fix

## Overview
Fixed redirect URI configuration inconsistency in Kakao social login implementation to support environment-specific configurations.

## Issues Resolved

### 1. Hardcoded Redirect URI Problem
**Before:**
- AuthController: `http://topping.cloud/api/user/kakao/callback` 
- KakaoService: `http://35.231.208.65:8080/api/user/kakao/callback`
- Inconsistent URLs causing OAuth callback failures

**After:**
- Centralized configuration using environment variables
- Environment-specific default values
- Consistent redirect URI across all components

### 2. Environment Configuration

#### New Environment Variables
```bash
# .env file
KAKAO_REST_API_KEY=your-kakao-rest-api-key
KAKAO_REDIRECT_URI=http://localhost:8080/api/user/kakao/callback
```

#### Application Properties Configuration
```properties
# application.properties (development)
KAKAO.REST-API-KEY=${KAKAO_REST_API_KEY}
KAKAO.REDIRECT-URI=${KAKAO_REDIRECT_URI:http://localhost:8080/api/user/kakao/callback}

# application-prod.properties (production)
KAKAO.REDIRECT-URI=${KAKAO_REDIRECT_URI:http://topping.cloud/api/user/kakao/callback}

# application-docker.properties (docker environment)
KAKAO.REDIRECT-URI=${KAKAO_REDIRECT_URI:http://35.231.208.65:8080/api/user/kakao/callback}
```

## Implementation Changes

### 1. KakaoService.java Updates
```java
@Value("${KAKAO.REST-API-KEY}")
private String kakaoRestApiKey;

@Value("${KAKAO.REDIRECT-URI}")
private String kakaoRedirectUri;

// Updated to use configurable redirect URI
body.add("redirect_uri", kakaoRedirectUri);
```

### 2. AuthController.java Updates
```java
@Value("${KAKAO.REST-API-KEY}")
private String kakaoRestApiKey;

@Value("${KAKAO.REDIRECT-URI}")
private String kakaoRedirectUri;

@GetMapping("/auth/login")
public String loginPage(Model model) {
    model.addAttribute("kakaoRestApiKey", kakaoRestApiKey);
    model.addAttribute("kakaoRedirectUri", kakaoRedirectUri);
    return "auth/login";
}
```

### 3. Test Coverage
Added `KakaoServiceConfigurationTest.java` to verify:
- Service injection works properly
- Configuration values are loaded correctly
- KakaoUserInfoDto validation logic

## Benefits

1. **Environment Flexibility**: Each deployment environment can have its own redirect URI
2. **Configuration Consistency**: Single source of truth for redirect URI configuration
3. **Maintainability**: No more hardcoded URLs in the source code
4. **Testing**: Easier to test with different configurations

## Deployment Notes

### For Development
```bash
KAKAO_REDIRECT_URI=http://localhost:8080/api/user/kakao/callback
```

### For Production
```bash
KAKAO_REDIRECT_URI=http://topping.cloud/api/user/kakao/callback
```

### For Docker/Staging
```bash
KAKAO_REDIRECT_URI=http://35.231.208.65:8080/api/user/kakao/callback
```

## Validation

1. ✅ Code compiles without errors
2. ✅ Build process completes successfully
3. ✅ Environment-specific configurations properly loaded
4. ✅ No hardcoded URLs remaining in the codebase
5. ✅ Test coverage for configuration validation

## Migration Steps

1. Update `.env` file with `KAKAO_REDIRECT_URI` variable
2. Ensure Kakao Developer Console has matching redirect URI configured
3. Deploy with environment-specific redirect URI settings
4. Verify OAuth callback works in each environment

## Impact
- **Breaking Change**: No
- **Backward Compatibility**: Yes (default values provided)
- **Testing Required**: OAuth flow testing in each environment
- **Documentation Updated**: This file and .env.example

The Kakao social login feature is now properly configured for multi-environment deployment with consistent, maintainable configuration management.