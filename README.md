# Android-Github-Widget
Android-Github-Widget

# GitHub 컨트리뷰션 위젯

- GitHub 사용자 이름만 입력하면 해당 사용자의 컨트리뷰션 현황을 볼 수 있습니다. 
- 개발 또는 빈번한 사용 시에는 GitHub API 요청 제한을 피하기 위해 개인 액세스 토큰(PAT) 설정이 권장됩니다.
- 홈 화면 위젯으로 간편하게 확인

## 기능

- GitHub 사용자 이름만으로 컨트리뷰션 현황 확인
- 홈 화면 위젯으로 간편하게 확인 (**다양한 크기 지원: 1x1, 2x1, 3x1, 4x1, 4x2, 4x3**)
- 주기적 자동 데이터 동기화 (WorkManager 사용)
- 오늘의 컨트리뷰션이 없을 경우 알림 기능 (**예정**)
- 연도별 컨트리뷰션 데이터 확인 기능
- API 호출 최적화로 효율적인 데이터 로딩

## 첫 실행 및 사용자 변경

1. 위젯을 처음 추가하면 GitHub 사용자 이름 설정 화면이 나타납니다.
2. 원하는 GitHub 사용자 이름을 입력하고 저장하면 위젯이 생성됩니다.
3. 위젯을 클릭하면 사용자 이름 설정 화면이 다시 나타납니다.
4. 사용자 변경은 앱 메인 화면의 "사용자 변경" 버튼을 통해서도 가능합니다.
5. 한 번 설정한 사용자 이름은 앱과 모든 위젯에 공유되어 유지됩니다.

## 개발자 정보

### API 인증 설정 (개발자용)

앱은 GitHub API를 사용하여 데이터를 가져옵니다. GitHub API는 인증 없이도 사용할 수 있지만 시간당 요청 제한(60회)이 있습니다. 개발 또는 테스트 시 이 제한에 빠르게 도달할 수 있으므로, 개발자 모드에서는 개인 액세스 토큰(PAT)을 사용하는 것이 좋습니다.

1. GitHub에서 개인 액세스 토큰 발급:
   - GitHub 계정 → Settings → Developer settings → Personal access tokens → Tokens (classic)
   - Generate new token (classic) 선택
   - 권한: `public_repo`, `read:user` 선택 (컨트리뷰션 및 사용자 정보 조회에 필요)

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
│   ├── GitHubApiClient.kt    - Retrofit 클라이언트 설정 (REST & GraphQL)
│   ├── GitHubGraphQLService.kt - GitHub GraphQL API 인터페이스
│   └── GitHubService.kt      - GitHub REST API 인터페이스
├── model/               - 데이터 모델 클래스
│   ├── graphql/           - GraphQL 응답 관련 데이터 클래스
│   │   └── ContributionCalendar.kt
│   ├── ContributionCalendarResponse.kt - GraphQL 응답 모델
│   ├── Repository.kt         - GitHub 저장소 데이터 모델
│   └── User.kt               - GitHub 사용자 데이터 모델
├── repository/          - 데이터 처리 계층
│   └── GitHubRepository.kt   - API 호출 및 비즈니스 로직 처리
├── ui/                  - UI 관련 클래스 (Adapter, Custom View 등)
│   ├── ContributionGridView.kt - 컨트리뷰션 그래프 커스텀 뷰
│   └── RepoAdapter.kt       - 저장소 목록 어댑터
├── util/                - 유틸리티 클래스
│   ├── Constants.kt          - 상수 정의
│   └── NotificationUtils.kt - 알림 관련 유틸리티
├── workers/             - 백그라운드 작업 관련 클래스
│   └── GitHubSyncWorker.kt  - 백그라운드 동기화 작업
├── ContributionData.kt  - 컨트리뷰션 데이터 모델 (앱 내부 사용)
├── ContributionHelper.kt - 컨트리뷰션 데이터 처리 도우미
├── GitHubWidgetProvider1x1.kt - 1x1 위젯 제공자
├── GitHubWidgetProvider2x1.kt - 2x1 위젯 제공자
├── GitHubWidgetProvider3x1.kt - 3x1 위젯 제공자
├── GitHubWidgetProvider4x1.kt - 4x1 위젯 제공자
├── GitHubWidgetProvider4x2.kt - 4x2 위젯 제공자
├── GitHubWidgetProvider4x3.kt - 4x3 위젯 제공자
├── MainActivity.kt      - 메인 액티비티
└── WidgetConfigureActivity.kt - 위젯 설정 액티비티
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
- **GitHubWidgetProvider***: 각 크기별 홈 화면 위젯을 제공하고 업데이트하는 컴포넌트입니다. (1x1, 2x1, 3x1, 4x1, 4x2, 4x3)
- **WidgetConfigureActivity**: 위젯을 처음 추가할 때 사용자 이름을 설정하는 액티비티입니다.

#### 5. 백그라운드 작업
- **GitHubSyncWorker**: WorkManager를 사용하여 주기적으로 GitHub 데이터를 동기화하는 작업자입니다.
- **NotificationUtils**: 알림 채널 생성 기능을 제공합니다. (알림 표시 기능은 추후 구현 예정)

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
   - 위젯 처음 추가 시 사용자명 설정 기능 추가
   - 위젯 클릭 시 사용자명 변경 화면 바로 표시
   - 다양한 위젯 크기 지원 (1x1 ~ 4x3)
   - 위젯 그래프 요일 정렬 및 표시 기간 확장
   - 연도 선택 기능으로 과거 컨트리뷰션 조회 가능

### 기술 스택

- **언어**: Kotlin
- **네트워크 통신**: Retrofit2 (REST & GraphQL)
- **비동기 처리**: Kotlin Coroutines
- **백그라운드 작업**: WorkManager
- **JSON 파싱**: Gson
- **이미지 로딩**: Glide
- **UI 컴포넌트**: AndroidX (RecyclerView, CardView, Spinner, ConstraintLayout), Custom View
- **의존성 주입**: (사용하지 않음)

## 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 

## 주요 기능

*   다양한 크기의 GitHub Contribution 그래프 위젯 제공 (1x1 ~ 4x3)
*   사용자별 GitHub 리포지토리 목록 및 연간/일일 Contribution 정보 표시 (MainActivity)
*   WorkManager를 이용한 주기적인 백그라운드 데이터 동기화
*   GitHub 사용자명 변경 기능
*   오늘 Contribution이 없을 경우 알림 제공
*   오픈소스 라이선스 정보 제공

## 위젯 업데이트 방식

*   **주기적 업데이트:** `WorkManager` (`GitHubSyncWorker`)가 주기적으로 백그라운드에서 실행되어 GitHub 데이터를 가져옵니다.
*   **수동 새로고침 (4x2, 4x3 위젯):** 위젯의 새로고침 버튼을 클릭하면 해당 위젯 Provider (`GitHubWidgetProvider4x2`, `GitHubWidgetProvider4x3`)로 브로드캐스트가 전송됩니다. Provider의 `onReceive` 메서드는 이 브로드캐스트를 수신하여 `WorkManager`에 `GitHubSyncWorker` 작업을 즉시 실행하도록 요청(enqueue)합니다.
*   **UI 갱신:** `GitHubSyncWorker` 작업이 성공적으로 완료되면, `MainActivity`의 `updateAllWidgets` 정적 함수를 호출합니다. 이 함수는 최신 데이터를 기반으로 모든 활성화된 위젯들의 `RemoteViews`를 직접 업데이트하고 `AppWidgetManager`를 통해 화면을 갱신합니다. 이를 통해 모든 위젯이 일관된 데이터를 표시하도록 보장합니다. 