# Android-Github-Widget
Android-Github-Widget

# GitHub 컨트리뷰션 위젯

이 앱은 GitHub 사용자의 컨트리뷰션 현황을 안드로이드 위젯으로 표시합니다. 

## 토큰 설정 방법

### 방법 1: 앱 빌드 시 토큰 입력 (권장)

앱을 처음 빌드할 때 GitHub 토큰을 포함시키는 방법입니다. 이 방법을 사용하면 앱 설치 후 별도로 토큰을 입력할 필요가 없습니다.

1. `app/build.gradle.kts` 파일을 열고 다음 줄을 찾습니다:
   ```kotlin
   buildConfigField("String", "GITHUB_API_TOKEN", "\"YOUR_GITHUB_TOKEN_HERE\"")
   ```

2. `YOUR_GITHUB_TOKEN_HERE` 부분을 실제 GitHub 토큰으로 교체합니다:
   ```kotlin
   buildConfigField("String", "GITHUB_API_TOKEN", "\"ghp_1234567890abcdefghijklmnopqrst\"")
   ```

3. 앱을 빌드하고 실행합니다.

### 방법 2: 앱 내에서 설정

이미 설치된 앱에서 GitHub 토큰을 설정하는 방법입니다.

1. 앱을 실행합니다.
2. 메인 화면에서 "GitHub 토큰 설정" 버튼을 탭합니다.
3. 발급받은 GitHub 토큰을 입력하고 "저장" 버튼을 탭합니다.

## GitHub 토큰 발급 방법

1. GitHub 계정으로 로그인합니다.
2. 오른쪽 상단의 프로필 > Settings를 클릭합니다.
3. 왼쪽 사이드바 하단의 'Developer settings'를 클릭합니다.
4. 'Personal access tokens' > 'Tokens (classic)'을 선택합니다.
5. 'Generate new token'을 클릭합니다.
6. 토큰 이름을 입력하고 'repo' 스코프를 선택합니다.
7. 'Generate token'을 클릭하고 생성된 토큰을 복사합니다.

**주의**: 토큰은 생성 시 한 번만 표시되므로 꼭 안전한 곳에 복사해두세요!

## 기능

- 실시간 GitHub 컨트리뷰션 현황 확인
- 홈 화면 위젯으로 간편하게 확인
- 주기적 자동 데이터 동기화
- 오늘의 컨트리뷰션이 없을 경우 알림 기능

## 프로젝트 구조 및 구성 요소

### 패키지 구조

```
app/src/main/java/com/example/myapplication/
├── api/                 - API 통신 관련 클래스
│   ├── GitHubApiClient.kt    - Retrofit 클라이언트 설정
│   └── GitHubService.kt      - GitHub API 인터페이스
├── model/               - 데이터 모델 클래스
│   ├── Repository.kt         - GitHub 저장소 데이터 모델
│   └── User.kt               - GitHub 사용자 데이터 모델
├── repository/          - 데이터 처리 계층
│   └── GitHubRepository.kt   - API 호출 및 비즈니스 로직 처리
├── util/                - 유틸리티 클래스
│   └── Constants.kt          - 상수 정의
├── ContributionData.kt  - 컨트리뷰션 데이터 모델
├── ContributionHelper.kt - 컨트리뷰션 데이터 처리 도우미
├── GitHubSyncWorker.kt  - 백그라운드 동기화 작업
├── GitHubWidgetProvider.kt - 앱 위젯 제공자
├── MainActivity.kt      - 메인 액티비티
├── NotificationUtils.kt - 알림 관련 유틸리티
└── RepoAdapter.kt       - 저장소 목록 어댑터
```

### 주요 컴포넌트 역할

#### 1. API 통신 계층
- **GitHubApiClient**: Retrofit을 사용하여 GitHub API 서버와 통신하도록 설정하는 객체입니다. 인증 토큰 관리도 담당합니다.
- **GitHubService**: GitHub REST API 엔드포인트를 정의한 인터페이스입니다. 사용자 정보, 저장소 목록, 이벤트 조회 API를 포함합니다.

#### 2. 데이터 모델
- **User**: GitHub 사용자 정보를 담는 데이터 클래스입니다.
- **Repository**: GitHub 저장소 정보를 담는 데이터 클래스입니다.
- **ContributionData**: 사용자의 컨트리뷰션 통계 정보를 담는 데이터 클래스입니다.

#### 3. 데이터 처리 계층
- **GitHubRepository**: API 호출 및 응답 처리 기능을 제공합니다. 컨트리뷰션 계산 로직도 포함합니다.
- **ContributionHelper**: 컨트리뷰션 데이터 처리를 위한 유틸리티 메서드를 제공합니다.

#### 4. UI 컴포넌트
- **MainActivity**: 앱의 주 화면입니다. 사용자 정보와 저장소 목록을 표시하고, 사용자 설정을 관리합니다.
- **RepoAdapter**: RecyclerView에 GitHub 저장소 목록을 표시하는 어댑터입니다.
- **GitHubWidgetProvider**: 홈 화면에 위젯을 제공하는 컴포넌트입니다.

#### 5. 백그라운드 작업
- **GitHubSyncWorker**: WorkManager를 사용하여 주기적으로 GitHub 데이터를 동기화하는 작업자입니다.
- **NotificationUtils**: 알림 채널 생성 및 알림 표시 기능을 제공합니다.

#### 6. 유틸리티
- **Constants**: API 토큰, 기본 사용자명, 기본 URL 등의 상수를 저장합니다.

### 데이터 흐름

1. **사용자 인터랙션**: 사용자가 MainActivity에서 새로고침, 사용자명 변경, 토큰 설정 등의 작업을 수행합니다.
2. **데이터 요청**: MainActivity는 GitHubRepository를 통해 데이터를 요청합니다.
3. **API 호출**: GitHubRepository는 GitHubApiClient와 GitHubService를 사용하여 GitHub API를 호출합니다.
4. **데이터 처리**: 받아온 데이터는 적절한 모델 객체로 변환되어 MainActivity로 전달됩니다.
5. **UI 업데이트**: MainActivity는 받은 데이터로 UI를 업데이트합니다.
6. **위젯 업데이트**: GitHubWidgetProvider가 주기적으로 또는 수동으로 위젯을 업데이트합니다.
7. **백그라운드 동기화**: GitHubSyncWorker가 주기적으로 데이터를 동기화하고 필요시 알림을 표시합니다.

### 기술 스택

- **언어**: Kotlin
- **네트워크 통신**: Retrofit2
- **비동기 처리**: Kotlin Coroutines
- **백그라운드 작업**: WorkManager
- **이미지 로딩**: Glide
- **UI 컴포넌트**: AndroidX (RecyclerView, CardView 등)
