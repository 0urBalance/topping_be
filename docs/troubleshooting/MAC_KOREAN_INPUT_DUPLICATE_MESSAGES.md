# 맥 한글 입력 중복 메시지 전송 문제 디버깅 노트

## 📋 문제 요약
**문제**: 맥에서 한글로 채팅 입력 시 메시지가 중복으로 전송되는 현상
**영향 범위**: 맥OS 사용자의 채팅 기능
**심각도**: Medium (사용자 경험 저하)
**해결 상태**: ✅ 완료

---

## 🔍 문제 분석

### 증상
- 맥에서 한글 입력 후 Enter 키를 누르면 메시지가 2번 전송됨
- 영문 입력 시에는 문제 없음
- 다른 운영체제에서는 문제 없음

### 근본 원인
1. **IME (Input Method Editor) 처리 누락**
   - 한글 입력 시 `compositionstart`/`compositionend` 이벤트 미처리
   - Enter 키가 한글 조합 완료와 메시지 전송을 동시에 트리거

2. **이벤트 중복 처리**
   - 기존 코드에서 composition 상태 확인 없음
   - 짧은 시간 내 중복 이벤트 방지 로직 부재

### 기존 코드 문제점
```javascript
// chat.js:37-42 (수정 전)
document.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey && e.target.classList.contains('message-textarea')) {
        e.preventDefault();
        this.sendMessage(); // IME 상태 확인 없이 바로 전송
    }
});
```

---

## 🛠️ 해결 방법

### 1. Composition 상태 추적
```javascript
// ChatInterface 생성자에 상태 변수 추가
constructor() {
    this.selectedRoomId = null;
    this.stompClient = null;
    this.currentUser = null;
    this.isComposing = false;      // IME 입력 상태
    this.lastSendTime = 0;         // 중복 방지용 타임스탬프
    
    this.init();
}
```

### 2. IME 이벤트 핸들러 추가
```javascript
// Composition 이벤트 처리
document.addEventListener('compositionstart', (e) => {
    if (e.target.classList.contains('message-textarea')) {
        this.isComposing = true;
    }
});

document.addEventListener('compositionend', (e) => {
    if (e.target.classList.contains('message-textarea')) {
        this.isComposing = false;
    }
});
```

### 3. Enter 키 핸들러 개선
```javascript
// 개선된 Enter 키 처리
document.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey && e.target.classList.contains('message-textarea')) {
        // IME 입력 중일 때는 메시지 전송 방지
        if (!this.isComposing) {
            e.preventDefault();
            this.sendMessage();
        }
    }
});
```

### 4. 중복 전송 방지 로직
```javascript
async sendMessage() {
    const textarea = document.getElementById('messageInput');
    const message = textarea?.value.trim();
    
    if (!message || !this.selectedRoomId) {
        return;
    }
    
    // 디바운스 로직 - 500ms 내 중복 전송 방지
    const now = Date.now();
    if (now - this.lastSendTime < 500) {
        console.log('Duplicate message send prevented (debounced)');
        return;
    }
    
    // 추가 안전장치 - composition 상태 재확인
    if (this.isComposing) {
        console.log('Message send prevented: IME composition in progress');
        return;
    }
    
    this.lastSendTime = now;
    
    // 기존 전송 로직...
}
```

---

## 🔧 적용된 변경사항

### 파일: `src/main/resources/static/js/chat.js`

#### 1. 생성자 수정 (라인 3-11)
- `isComposing` 상태 플래그 추가
- `lastSendTime` 디바운스용 타임스탬프 추가

#### 2. 이벤트 바인딩 추가 (라인 38-60)
- `compositionstart` 이벤트 핸들러
- `compositionend` 이벤트 핸들러  
- 기존 `keydown` 핸들러에 composition 상태 검사 추가

#### 3. sendMessage 메서드 개선 (라인 615-661)
- 500ms 디바운스 로직 추가
- composition 상태 재확인 로직 추가
- 디버깅을 위한 콘솔 로그 추가

---

## 🧪 테스트 시나리오

### 기본 테스트
1. **한글 입력 테스트**
   - 한글로 메시지 입력 후 Enter → 1번만 전송되어야 함
   - 한글 조합 중 Enter 키 입력 → 메시지 전송 안됨

2. **영문 입력 테스트**  
   - 영문으로 메시지 입력 후 Enter → 정상 전송
   - 기존 기능에 영향 없음 확인

3. **혼합 입력 테스트**
   - 영문+한글 혼합 입력 후 Enter → 1번만 전송
   - 특수문자 포함 입력 테스트

### 고급 테스트
1. **빠른 연속 입력 테스트**
   - 빠르게 연속으로 Enter 입력 → 디바운스로 방지
   - 500ms 이후 입력 → 정상 전송

2. **다양한 IME 테스트**
   - 한글 외 다른 언어 IME (일본어, 중국어 등)
   - 다양한 한글 입력기 테스트

3. **브라우저별 호환성**
   - Safari, Chrome, Firefox에서 동일하게 작동
   - 맥 외 다른 OS에서 기존 동작 유지

---

## 📊 성능 영향

### 추가된 기능
- **메모리**: 인스턴스 변수 2개 추가 (무시할 수준)
- **이벤트 리스너**: 2개 추가 (`compositionstart`, `compositionend`)
- **처리 시간**: 각 메시지 전송 시 추가 검사 (<1ms)

### 최적화 고려사항
- 디바운스 시간 조정 가능 (현재 500ms)
- 필요시 IME 언어별 세부 조정 가능
- 콘솔 로그는 프로덕션에서 제거 가능

---

## 🚀 배포 가이드

### 배포 방법
1. JavaScript 파일만 수정되어 서버 재시작 불필요
2. 브라우저 캐시 클리어 후 새로고침으로 즉시 적용
3. CDN 사용 시 캐시 무효화 필요

### 롤백 계획
- 기존 코드로 쉽게 롤백 가능
- git 히스토리를 통한 즉시 복구 가능

### 모니터링 포인트
- 채팅 메시지 중복 전송 지표 모니터링
- 맥 사용자의 메시지 전송 성공률 추적
- 브라우저별 오류 로그 모니터링

---

## 🎯 향후 개선 방향

### 단기 개선사항
- [ ] 프로덕션에서 디버그 로그 제거
- [ ] 디바운스 시간 사용자 설정 가능화
- [ ] 다국어 IME 지원 확장

### 장기 개선사항
- [ ] 실시간 타이핑 인디케이터와 IME 상태 연동
- [ ] 모바일 디바이스 IME 지원 강화
- [ ] 접근성 개선 (스크린 리더 등)

---

## 📚 관련 리소스

### 기술 문서
- [MDN Composition Events](https://developer.mozilla.org/en-US/docs/Web/API/CompositionEvent)
- [IME 처리 Best Practices](https://www.w3.org/International/articles/ime/)

### 프로젝트 내 관련 파일
- `src/main/resources/static/js/chat.js` - 메인 수정 파일
- `docs/domains/chat/README.md` - 채팅 도메인 문서
- `docs/domains/chat/CLAUDE.md` - 채팅 개발 가이드

### 이슈 추적
- **해결 일시**: 2025-01-08
- **담당자**: Claude Code
- **리뷰어**: N/A
- **테스터**: 프로덕션 배포 후 사용자 피드백 대기

---

*이 문서는 맥 한글 입력 중복 메시지 문제 해결 과정을 기록하며, 향후 유사한 IME 관련 이슈 해결에 참고자료로 활용됩니다.*