# 작업 목록 (Task List)

## 완료된 작업

- [x] README.md 업데이트 (코드 베이스 반영)
- [x] 오픈소스 라이선스 라이브러리 적용 및 메뉴 연결
- [x] 다양한 크기(1x1, 2x1, 3x1, 4x1, 4x2, 4x3)의 위젯 추가
- ~~[x] 위젯 업데이트 로직 공통화 (WidgetUpdateUtils 사용)~~ (롤백)
- [x] 위젯 기능 안정화 (개별 업데이트 로직 복원)
- [x] 위젯 클릭 시 메인 앱 실행 (사용자 설정은 앱 내부에서)
- [x] 위젯 설정 액티비티 제거 (앱 데이터 공유 방식)
- [x] 위젯 그래프 요일 정렬 및 표시 기간 확장 (작년 데이터 포함)
- [x] 위젯 그래프 레이아웃 개선 (크기별 기간 조정, 중앙 정렬)
- [x] 기본 프로젝트 구조 설정 (Kotlin, AndroidX)
- [x] GitHub API 연동 (Retrofit/OkHttp 또는 Ktor 사용) - GraphQL API 사용
- [x] 위젯 레이아웃 디자인 (1x1, 2x1, 3x1, 4x1, 4x2, 4x3)
- [x] 각 위젯 크기별 Provider 구현 (`AppWidgetProvider`)
- [x] GitHub 데이터 로딩 및 파싱 로직 구현 (Repository 패턴 사용)
- [x] Contribution 데이터 시각화 (GridView 또는 커스텀 뷰)
- [x] MainActivity 구현 (리포지토리 목록, 컨트리뷰션 현황 표시)
- [x] WorkManager를 이용한 주기적 백그라운드 동기화 (`GitHubSyncWorker`)
- [x] 사용자명 설정 기능 (SharedPreferences 사용)
- [x] 라이선스 정보 표시 (Google Play Services OSS 라이선스 라이브러리 사용)
- [x] 알림 기능 추가 (오늘 기여 없을 시)
- [x] 위젯 새로고침 로직 리팩토링:
    - [x] 4x2, 4x3 위젯 새로고침 버튼 클릭 시 브로드캐스트 전송
    - [x] 각 Provider의 `onReceive`에서 브로드캐스트 수신하여 `WorkManager` 작업(`GitHubSyncWorker`) 실행
    - [x] `GitHubSyncWorker` 완료 후 `MainActivity.updateAllWidgets` 호출
    - [x] `MainActivity.updateAllWidgets`에서 모든 활성 위젯의 `RemoteViews`를 직접 업데이트하고 `AppWidgetManager`로 갱신

## 진행 중인 작업


## 예정된 작업
- 위젯 미리보기 이미지 추가 (선택 사항)
- 위젯 관련 상수 관리 개선 (선택 사항)
- 알림 기능 구현 및 테스트 (오늘 컨트리뷰션 없을 시) 