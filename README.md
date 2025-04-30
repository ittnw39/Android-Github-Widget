# Android-Github-Widget
Android-Github-Widget

# GitHub 컨트리뷰션 위젯

이 앱은 GitHub 사용자의 컨트리뷰션 현황을 안드로이드 위젯으로 표시합니다. 
별도의 토큰 설정 없이 **GitHub 사용자 이름만 입력하면** 누구나 사용할 수 있습니다.

## 기능

- GitHub 사용자 이름만으로 컨트리뷰션 현황 확인
- 홈 화면 위젯으로 간편하게 확인
- 주기적 자동 데이터 동기화
- 오늘의 컨트리뷰션이 없을 경우 알림 기능 (옵션)

## 사용자 변경 방법

1. 앱을 실행합니다.
2. 메인 화면에서 "사용자 변경" 버튼을 탭합니다.
3. 조회하고 싶은 GitHub 사용자 이름을 입력하고 "저장" 버튼을 탭합니다.

## 프로젝트 구조 및 구성 요소

### 패키지 구조

```
app/src/main/java/com/example/myapplication/
├── api/                 - API 통신 관련 클래스
│   ├── GitHubApiClient.kt    - Retrofit 클라이언트 설정
│   ├── GitHubGraphQLService.kt - GitHub GraphQL API 인터페이스
│   └── GitHubService.kt      - GitHub REST API 인터페이스
├── model/               - 데이터 모델 클래스
│   ├── Repository.kt         - GitHub 저장소 데이터 모델
│   └── User.kt               - GitHub 사용자 데이터 모델
├── repository/          - 데이터 처리 계층
│   └── GitHubRepository.kt   - API 호출 및 비즈니스 로직 처리
├── util/                - 유틸리티 클래스
│   └── Constants.kt          - 상수 정의
├── ContributionData.kt  - 컨트리뷰션 데이터 모델
├── ContributionHelper.kt - 컨트리뷰션 데이터 처리 도우미
├── ContributionGridView.kt - 컨트리뷰션 그래프 커스텀 뷰
├── GitHubSyncWorker.kt  - 백그라운드 동기화 작업
├── GitHubWidgetProvider.kt - 앱 위젯 제공자
├── MainActivity.kt      - 메인 액티비티
├── NotificationUtils.kt - 알림 관련 유틸리티
└── RepoAdapter.kt       - 저장소 목록 어댑터
```

### 주요 컴포넌트 역할

#### 1. API 통신 계층
- **GitHubApiClient**: Retrofit을 사용하여 GitHub API 서버와 통신하도록 설정하는 객체입니다.
- **GitHubService**: GitHub REST API 엔드포인트 (사용자 정보, 저장소 목록, 이벤트 조회)를 정의한 인터페이스입니다.
- **GitHubGraphQLService**: GitHub GraphQL API 엔드포인트 (컨트리뷰션 데이터 조회)를 정의한 인터페이스입니다.

#### 2. 데이터 모델
- **User**: GitHub 사용자 정보를 담는 데이터 클래스입니다.
- **Repository**: GitHub 저장소 정보를 담는 데이터 클래스입니다.
- **ContributionData**: 사용자의 컨트리뷰션 통계 정보를 담는 데이터 클래스입니다.

#### 3. 데이터 처리 계층
- **GitHubRepository**: API 호출 및 응답 처리 기능을 제공합니다. REST API 및 GraphQL API를 모두 사용하여 데이터를 가져오고, 컨트리뷰션 계산 로직도 포함합니다.
- **ContributionHelper**: 컨트리뷰션 데이터 처리를 위한 유틸리티 메서드를 제공합니다.

#### 4. UI 컴포넌트
- **MainActivity**: 앱의 주 화면입니다. 사용자 컨트리뷰션 그래프, 오늘/전체 컨트리뷰션 수, 리포지토리 목록을 표시하고, 사용자 변경 기능을 제공합니다.
- **RepoAdapter**: RecyclerView에 GitHub 저장소 목록을 표시하는 어댑터입니다.
- **ContributionGridView**: 컨트리뷰션 데이터를 시각적인 그리드 형태로 표시하는 커스텀 뷰입니다.
- **GitHubWidgetProvider**: 홈 화면에 컨트리뷰션 현황 위젯을 제공하는 컴포넌트입니다.

#### 5. 백그라운드 작업
- **GitHubSyncWorker**: WorkManager를 사용하여 주기적으로 GitHub 데이터를 동기화하는 작업자입니다.
- **NotificationUtils**: 알림 채널 생성 및 알림 표시 기능을 제공합니다.

#### 6. 유틸리티
- **Constants**: 기본 사용자명, API 기본 URL 등의 상수를 저장합니다. (API 토큰 상수는 제거됨)

### 데이터 흐름

1. **사용자 인터랙션**: 사용자가 MainActivity에서 새로고침, 사용자명 변경 등의 작업을 수행합니다.
2. **데이터 요청**: MainActivity는 GitHubRepository를 통해 데이터를 요청합니다.
3. **API 호출**: GitHubRepository는 GitHubApiClient, GitHubService, GitHubGraphQLService를 사용하여 GitHub API (REST 및 GraphQL)를 호출합니다.
4. **데이터 처리**: 받아온 데이터는 적절한 모델 객체로 변환되어 MainActivity로 전달됩니다.
5. **UI 업데이트**: MainActivity는 받은 데이터로 UI (컨트리뷰션 그래프, 텍스트, 리포지토리 목록)를 업데이트합니다.
6. **위젯 업데이트**: GitHubWidgetProvider가 주기적으로 또는 수동으로 위젯을 업데이트합니다.
7. **백그라운드 동기화**: GitHubSyncWorker가 주기적으로 데이터를 동기화하고 필요시 알림을 표시합니다.

### 기술 스택

- **언어**: Kotlin
- **네트워크 통신**: Retrofit2 (REST & GraphQL)
- **비동기 처리**: Kotlin Coroutines
- **백그라운드 작업**: WorkManager
- **JSON 파싱**: Gson
- **UI 컴포넌트**: AndroidX (RecyclerView, CardView, Spinner), Custom View 