# Topping (토핑) - 협업 매칭 플랫폼 Backend

협업 프로젝트를 찾고 파트너를 매칭하는 플랫폼의 백엔드 서비스입니다.

## 주요 기능

### 🏠 홈페이지
- 최근 등록된 프로젝트 6개 표시
- 간단한 네비게이션 제공

### 📋 프로젝트 관리
- **프로젝트 목록**: 모든 활성 프로젝트 조회
- **프로젝트 등록**: 새로운 협업 프로젝트 등록
- **프로젝트 상세**: 프로젝트 세부 정보 및 작성자 정보 표시
- 카테고리별 분류 (웹 개발, 모바일 앱, 게임 개발, 디자인, 마케팅, 기타)

### 🤝 협업 관리
- **협업 신청**: 프로젝트에 대한 협업 신청
- **협업 현황**: 모든 협업 신청 내역 조회
- **신청 승인/거절**: 프로젝트 작성자의 협업 신청 관리
- 협업 상태 관리 (PENDING, ACCEPTED, REJECTED)

## 기술 스택

- **Framework**: Spring Boot 3.5.3
- **Java**: 17
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA
- **Security**: Spring Security
- **Template Engine**: Thymeleaf
- **Build Tool**: Gradle

## API 엔드포인트

### REST API
- `GET /products/api` - 모든 프로젝트 조회
- `GET /products/api/{id}` - 특정 프로젝트 조회
- `GET /collaborations/api` - 모든 협업 조회
- `GET /collaborations/api/user/{userId}` - 특정 사용자의 협업 조회

### 웹 페이지
- `GET /` - 홈페이지
- `GET /products` - 프로젝트 목록
- `GET /products/create` - 프로젝트 등록 폼
- `POST /products/create` - 프로젝트 등록 처리
- `GET /products/{id}` - 프로젝트 상세
- `GET /collaborations` - 협업 현황
- `GET /collaborations/apply/{productId}` - 협업 신청 폼
- `POST /collaborations/apply` - 협업 신청 처리
- `POST /collaborations/{id}/accept` - 협업 승인
- `POST /collaborations/{id}/reject` - 협업 거절

## 데이터 모델

### User (사용자)
- UUID 기반 Primary Key
- 이메일, 사용자명, 비밀번호
- Role 기반 권한 관리

### Product (프로젝트)
- UUID 기반 Primary Key
- 제목, 설명, 카테고리, 이미지 URL
- 작성자 정보 (User 참조)
- 활성화 상태 관리

### Collaboration (협업)
- UUID 기반 Primary Key
- 프로젝트 및 신청자 정보
- 협업 신청 메시지
- 상태 관리 (PENDING, ACCEPTED, REJECTED)
