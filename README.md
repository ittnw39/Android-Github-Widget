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
- 연도별 컨트리뷰션 데이터 확인 기능
- API 호출 최적화로 효율적인 데이터 로딩

## 첫 실행 및 사용자 변경

1. 앱을 처음 실행하면 GitHub 사용자 이름 입력 창이 나타납니다.
2. 원하는 GitHub 사용자 이름을 입력하면 해당 사용자의 컨트리뷰션 데이터가 로드됩니다.
3. 사용자 변경은 메인 화면의 "사용자 변경" 버튼을 통해 언제든지 가능합니다.
4. 한 번 설정한 사용자 이름은 앱을 재시작해도 유지됩니다.

## 개발자 정보

### API 인증 설정 (개발자용)

앱은 GitHub API를 사용하여 데이터를 가져옵니다. GitHub API는 인증 없이도 사용할 수 있지만 시간당 요청 제한(60회)이 있습니다. 개발 또는 테스트 시 이 제한에 빠르게 도달할 수 있으므로, 개발자 모드에서는 개인 액세스 토큰(PAT)을 사용하는 것이 좋습니다.

1. GitHub에서 개인 액세스 토큰 발급:
   - GitHub 계정 → Settings → Developer settings → Personal access tokens → Tokens (classic)
   - Generate new token (classic) 선택
   - 권한: `read:user` 선택 (최소 권한만 부여)

2. 토큰 설정 방법:
   - `local.properties` 파일에 다음 라인 추가:
     ```
     github.token=YOUR_TOKEN_HERE
     ```
   - 이 파일은 .gitignore에 포함되어 있어 저장소에 업로드되지 않습니다.

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
- **GitHubApiClient**: Retrofit을 사용하여 GitHub API 서버와 통신하도록 설정하는 객체입니다. 개발자 토큰이 설정된 경우 인증 헤더를 추가합니다.
- **GitHubService**: GitHub REST API 엔드포인트 (사용자 정보, 저장소 목록)를 정의한 인터페이스입니다.
- **GitHubGraphQLService**: GitHub GraphQL API 엔드포인트 (컨트리뷰션 데이터 조회)를 정의한 인터페이스입니다.

#### 2. 데이터 모델
- **User**: GitHub 사용자 정보를 담는 데이터 클래스입니다.
- **Repository**: GitHub 저장소 정보를 담는 데이터 클래스입니다.
- **ContributionData**: 사용자의 컨트리뷰션 통계 정보를 담는 데이터 클래스입니다.

#### 3. 데이터 처리 계층
- **GitHubRepository**: API 호출 및 응답 처리 기능을 제공합니다. GraphQL API를 사용하여 효율적으로 컨트리뷰션 데이터를 가져옵니다.
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
- **Constants**: API 기본 URL 등의 상수를 저장합니다.

### 최근 개선사항

1. **API 호출 최적화**:
   - 여러 개의 API 호출을 하나의 GraphQL 쿼리로 통합하여 효율성 향상
   - 연간 컨트리뷰션 데이터를 한 번의 호출로 가져오는 `getContributionYearData` 메서드 구현

2. **개발자 토큰 지원**:
   - API 속도 제한 문제 해결을 위한 개발자 토큰 지원 추가
   - BuildConfig를 통한 안전한 토큰 관리

3. **사용자 경험 개선**:
   - 첫 실행 시 사용자명 입력 다이얼로그 자동 표시
   - 하드코딩된 사용자명 제거로 앱 범용성 향상
   - 연도 선택 기능으로 과거 컨트리뷰션 조회 가능

### 기술 스택

- **언어**: Kotlin
- **네트워크 통신**: Retrofit2 (REST & GraphQL)
- **비동기 처리**: Kotlin Coroutines
- **백그라운드 작업**: WorkManager
- **JSON 파싱**: Gson
- **UI 컴포넌트**: AndroidX (RecyclerView, CardView, Spinner), Custom View 

## 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 