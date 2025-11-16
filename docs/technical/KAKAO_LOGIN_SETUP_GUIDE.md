# 카카오 로그인 설정 가이드

## 🔧 환경 설정 확인

### 1. 환경 변수 확인 ✅
`.env` 파일에 다음 설정이 올바르게 구성되어 있습니다:
```
KAKAO_REST_API_KEY=029da9a6ae0298cd1a32dd1d6af2f932
KAKAO_REDIRECT_URI=http://35.237.128.174:8080/api/user/kakao/callback
```

### 2. 카카오 개발자 콘솔 필수 설정 사항

**카카오 개발자 콘솔 (developers.kakao.com)에서 확인할 사항들:**

#### A. 앱 설정
1. **내 애플리케이션** → **앱 선택** → **앱 키**
   - REST API 키: `029da9a6ae0298cd1a32dd1d6af2f932` 확인

#### B. 플랫폼 설정
2. **플랫폼** → **Web 플랫폼 등록**
   - 사이트 도메인: `http://35.237.128.174:8080` 등록 필수

#### C. 카카오 로그인 설정
3. **제품 설정** → **카카오 로그인**
   - 카카오 로그인 **활성화 ON**
   - **Redirect URI 등록**: `http://35.237.128.174:8080/api/user/kakao/callback`

#### D. 동의항목 설정
4. **제품 설정** → **카카오 로그인** → **동의항목**
   - **닉네임**: 필수 또는 선택 동의
   - **카카오계정(이메일)**: 선택 동의 권장
   - 프로필 사진: 선택 동의 (선택사항)

### 3. 환경별 설정 확인

#### 개발 환경 (.env)
```properties
KAKAO_REDIRECT_URI=http://localhost:8080/api/user/kakao/callback
```

#### 프로덕션 환경 (현재 설정)
```properties
KAKAO_REDIRECT_URI=http://35.237.128.174:8080/api/user/kakao/callback
```

## 🚨 트러블슈팅

### 일반적인 문제와 해결책

1. **"redirect_uri_mismatch" 오류**
   - 카카오 개발자 콘솔에서 Redirect URI가 정확히 등록되어 있는지 확인
   - 프로토콜(http/https), 포트번호까지 정확히 일치해야 함

2. **"invalid_client" 오류**
   - REST API 키가 올바른지 확인
   - 앱이 서비스 상태인지 확인

3. **"insufficient_scope" 오류**
   - 필요한 동의항목이 설정되어 있는지 확인
   - 닉네임, 이메일 동의항목 확인

4. **사용자 정보가 저장되지 않는 경우**
   - 이메일 동의항목이 선택 동의로 설정되어 있는지 확인
   - 사용자가 동의하지 않을 경우 임시 이메일이 생성됩니다

## 🔍 설정 검증 방법

### 1. 로그 확인
```bash
# 애플리케이션 로그에서 카카오 로그인 관련 로그 확인
tail -f logs/application.log | grep -i kakao
```

### 2. 카카오 로그인 테스트
1. `/auth/login` 페이지 접근
2. "카카오로 로그인" 버튼 클릭
3. 카카오 인증 페이지로 정상 이동하는지 확인
4. 인증 후 홈페이지로 리다이렉트되는지 확인

### 3. 사용자 정보 저장 확인
```sql
-- DB에서 카카오 로그인한 사용자 확인
SELECT * FROM users WHERE kakao_id IS NOT NULL;
```

## 📋 체크리스트

- [ ] 카카오 개발자 콘솔에서 REST API 키 일치 확인
- [ ] Web 플랫폼에 사이트 도메인 등록
- [ ] 카카오 로그인 활성화
- [ ] Redirect URI 정확히 등록
- [ ] 닉네임, 이메일 동의항목 설정
- [ ] 로컬/프로덕션 환경별 설정 분리
- [ ] 로그인 플로우 테스트 완료
- [ ] 사용자 정보 DB 저장 확인

## 🔗 참고 링크

- [카카오 개발자 센터](https://developers.kakao.com/)
- [카카오 로그인 REST API 가이드](https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api)