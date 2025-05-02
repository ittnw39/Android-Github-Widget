package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.BackoffPolicy
import androidx.work.WorkRequest
import com.example.myapplication.api.GitHubApiClient
import com.example.myapplication.repository.GitHubRepository
import com.example.myapplication.util.Constants
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.widget.RemoteViews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RepoAdapter
    private lateinit var tvTodayContributions: TextView
    private lateinit var tvTotalContributions: TextView
    private lateinit var contributionGridView: ContributionGridView
    private lateinit var btnRefresh: Button
    private lateinit var btnChangeUser: Button

    private val gitHubRepository = GitHubRepository()
    private var username = Constants.DEFAULT_GITHUB_USERNAME // 기본값을 Constants에서 가져옴

    companion object {
        private const val PREFS_NAME = "GitHubWidgetPrefs"
        private const val KEY_USERNAME = "username"
        private lateinit var yearSpinner: Spinner
        private var selectedYear = LocalDate.now().year
        private const val TAG_UPDATE_WIDGETS = "UpdateAllWidgets"

        // 모든 GitHub 위젯 업데이트를 트리거하는 함수 (WorkManager 완료 시 호출됨)
        fun updateAllWidgets(context: Context) {
            // AppWidgetManager 인스턴스 가져오기
            val appWidgetManager = AppWidgetManager.getInstance(context)
            // GitHubRepository 인스턴스 생성 (데이터 로딩용)
            val repository = GitHubRepository()
            // 현재 저장된 사용자 이름 가져오기 (MainActivity 인스턴스 없이 접근하기 위해 Provider 변수 사용)
            val username = GitHubWidgetProvider4x3.GITHUB_USERNAME

            if (username.isEmpty()) {
                Log.w(TAG_UPDATE_WIDGETS, "Username is empty, cannot update widgets.")
                // 필요하다면 위젯에 "사용자 설정 필요" 메시지 표시 로직 추가 가능
                return
            }

            // 코루틴을 사용하여 백그라운드에서 데이터 로딩 및 UI 업데이트
            // 주의: 이 함수는 Worker에서 호출되므로, 자체적인 코루틴 스코프가 필요할 수 있음
            // GlobalScope는 앱 생명주기와 무관하게 동작하므로 주의 필요. 앱 종료 시 취소 안됨.
            // 여기서는 간단히 lifecycleScope를 사용하지만, Application Context 기반의 스코프가 더 적절할 수 있음.
            // 또는 호출하는 Worker의 CoroutineScope를 사용하는 것이 가장 좋음.
            // 여기서는 Context가 Application Context라고 가정하고 GlobalScope + Dispatchers.IO 사용 시도.
            kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
                try {
                    Log.d(TAG_UPDATE_WIDGETS, "Starting widget update process for user: $username")
                    // 현재 연도와 이전 연도 데이터 가져오기
                    val currentYear = LocalDate.now().year
                    val previousYear = currentYear - 1

                    val (currentTotalCount, currentYearContributions) = repository.getContributionYearData(username, currentYear)
                    val (_, previousYearContributions) = repository.getContributionYearData(username, previousYear)

                    // 데이터 병합
                    val combinedContributionsByDay = mutableMapOf<String, Int>()
                    combinedContributionsByDay.putAll(previousYearContributions)
                    combinedContributionsByDay.putAll(currentYearContributions)

                    Log.d(TAG_UPDATE_WIDGETS, "Data fetched. Total days: ${combinedContributionsByDay.size}, Current year total: $currentTotalCount")

                    // 오늘 날짜 기여도 계산
                    val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                    val todayCount = combinedContributionsByDay[todayDate] ?: 0

                    // 각 위젯 타입별 업데이트
                    updateWidgetType(context, appWidgetManager, GitHubWidgetProvider1x1::class.java, R.layout.github_widget_1x1, combinedContributionsByDay, username, todayCount, currentTotalCount)
                    updateWidgetType(context, appWidgetManager, GitHubWidgetProvider2x1::class.java, R.layout.github_widget_2x1, combinedContributionsByDay, username, todayCount, currentTotalCount)
                    updateWidgetType(context, appWidgetManager, GitHubWidgetProvider3x1::class.java, R.layout.github_widget_3x1, combinedContributionsByDay, username, todayCount, currentTotalCount)
                    updateWidgetType(context, appWidgetManager, GitHubWidgetProvider4x1::class.java, R.layout.github_widget_4x1, combinedContributionsByDay, username, todayCount, currentTotalCount)
                    updateWidgetType(context, appWidgetManager, GitHubWidgetProvider4x2::class.java, R.layout.github_widget_4x2, combinedContributionsByDay, username, todayCount, currentTotalCount)
                    updateWidgetType(context, appWidgetManager, GitHubWidgetProvider4x3::class.java, R.layout.widget_layout, combinedContributionsByDay, username, todayCount, currentTotalCount)

                    Log.d(TAG_UPDATE_WIDGETS, "Widget update process finished.")

                } catch (e: Exception) {
                    Log.e(TAG_UPDATE_WIDGETS, "Error updating widgets: ${e.message}", e)
                    // 오류 발생 시 위젯에 오류 메시지 표시 (선택 사항)
                    updateWidgetsWithError(context, appWidgetManager, e)
                }
            }
        }

        // 위젯 타입별 업데이트 로직 (제네릭 사용)
        private fun <T : AppWidgetProvider> updateWidgetType(
            context: Context,
            appWidgetManager: AppWidgetManager,
            providerClass: Class<T>,
            layoutId: Int,
            contributionsData: Map<String, Int>,
            username: String,
            todayCount: Int,
            totalCount: Int
        ) {
            val componentName = ComponentName(context, providerClass)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            Log.d(TAG_UPDATE_WIDGETS, "Updating ${providerClass.simpleName} widgets: ${appWidgetIds.joinToString()}")

            val providerInstance = try {
                providerClass.getDeclaredConstructor().newInstance()
            } catch (e: Exception) {
                Log.e(TAG_UPDATE_WIDGETS, "Failed to instantiate ${providerClass.simpleName}", e)
                return
            }

            appWidgetIds.forEach { appWidgetId ->
                try {
                    val views = RemoteViews(context.packageName, layoutId)

                    // 텍스트 뷰 업데이트 (해당 위젯에 ID가 존재하는 경우에만 시도)
                    updateTextViewIfExists(views, R.id.widget_title, username)
                    updateTextViewIfExists(views, R.id.today_contributions, todayCount.toString())
                    updateTextViewIfExists(views, R.id.total_contributions, totalCount.toString())

                    // 그리드 업데이트 (Provider의 internal 함수 호출)
                    // 각 Provider 클래스에 따라 updateContributionGrid 호출 방식이 다를 수 있으므로 확인 필요
                    // 여기서는 모든 Provider가 동일한 시그니처를 가진다고 가정
                    // getDeclaredMethod를 사용하여 internal 함수 접근 시도 (리플렉션)
                    try {
                        val updateGridMethod = providerClass.getDeclaredMethod(
                            "updateContributionGrid",
                            RemoteViews::class.java,
                            Map::class.java,
                            Context::class.java,
                            Int::class.java
                        )
                        updateGridMethod.isAccessible = true // internal 접근 허용
                        updateGridMethod.invoke(providerInstance, views, contributionsData, context, appWidgetId)
                    } catch (e: NoSuchMethodException) {
                         Log.e(TAG_UPDATE_WIDGETS, "updateContributionGrid method not found in ${providerClass.simpleName}", e)
                         // 대체: 각 Provider별로 직접 캐스팅하여 호출
                         when (providerInstance) {
                             is GitHubWidgetProvider1x1 -> providerInstance.updateContributionGrid(views, contributionsData, context, appWidgetId)
                             is GitHubWidgetProvider2x1 -> providerInstance.updateContributionGrid(views, contributionsData, context, appWidgetId)
                             is GitHubWidgetProvider3x1 -> providerInstance.updateContributionGrid(views, contributionsData, context, appWidgetId)
                             is GitHubWidgetProvider4x1 -> providerInstance.updateContributionGrid(views, contributionsData, context, appWidgetId)
                             is GitHubWidgetProvider4x2 -> GitHubWidgetProvider4x2.updateContributionGrid(views, contributionsData, context, appWidgetId)
                             is GitHubWidgetProvider4x3 -> GitHubWidgetProvider4x3.updateContributionGrid(views, contributionsData, context, appWidgetId)
                             // 다른 위젯 Provider 추가
                         }
                    } catch (e: Exception) {
                        Log.e(TAG_UPDATE_WIDGETS, "Error invoking updateContributionGrid for ${providerClass.simpleName} ID $appWidgetId", e)
                    }

                    // 위젯 UI 갱신
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                    Log.d(TAG_UPDATE_WIDGETS, "Successfully updated ${providerClass.simpleName} ID $appWidgetId")
                } catch (e: Exception) {
                    Log.e(TAG_UPDATE_WIDGETS, "Failed to update ${providerClass.simpleName} ID $appWidgetId", e)
                }
            }
        }

        // 오류 발생 시 위젯 업데이트 함수
        private fun updateWidgetsWithError(context: Context, appWidgetManager: AppWidgetManager, error: Exception) {
            val errorMessage = "업데이트 오류"
            val errorDetails = "?"

            val widgetProviders = listOf(
                GitHubWidgetProvider1x1::class.java to R.layout.github_widget_1x1,
                GitHubWidgetProvider2x1::class.java to R.layout.github_widget_2x1,
                GitHubWidgetProvider3x1::class.java to R.layout.github_widget_3x1,
                GitHubWidgetProvider4x1::class.java to R.layout.github_widget_4x1,
                GitHubWidgetProvider4x2::class.java to R.layout.github_widget_4x2,
                GitHubWidgetProvider4x3::class.java to R.layout.widget_layout
            )

            widgetProviders.forEach { (providerClass, layoutId) ->
                val componentName = ComponentName(context, providerClass)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                appWidgetIds.forEach { appWidgetId ->
                    try {
                        val views = RemoteViews(context.packageName, layoutId)
                        updateTextViewIfExists(views, R.id.widget_title, errorMessage)
                        updateTextViewIfExists(views, R.id.today_contributions, errorDetails)
                        updateTextViewIfExists(views, R.id.total_contributions, errorDetails)
                        // 오류 시 그리드는 기본 상태로 둘 수 있음 (또는 특정 색상으로 초기화)
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    } catch (e: Exception) {
                        Log.e(TAG_UPDATE_WIDGETS, "Failed to update widget ID $appWidgetId with error message", e)
                    }
                }
            }
        }

        // RemoteViews에 특정 ID의 TextView가 존재할 경우 업데이트하는 헬퍼 함수
        private fun updateTextViewIfExists(views: RemoteViews, viewId: Int, text: String) {
            // RemoteViews에는 ID 존재 여부를 직접 확인하는 API가 없으므로,
            // 레이아웃 XML을 파싱하거나, ID 목록을 관리하는 방식이 필요하나 복잡함.
            // 여기서는 일단 setTextViewText를 호출하고, ID가 없으면 내부적으로 무시될 것으로 가정.
            // 또는 try-catch로 감싸서 특정 예외 처리 가능.
            try {
                 // ID가 0이 아닌 경우에만 시도 (ID가 0이면 유효하지 않음)
                if (viewId != 0) {
                     views.setTextViewText(viewId, text)
                 }
            } catch (e: Exception) {
                 // Log.w(TAG_UPDATE_WIDGETS, "Failed to set text for view ID $viewId (might not exist in layout)", e)
                 // ID가 없는 경우 발생하는 예외는 아님. 다른 문제일 수 있음.
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 알림 채널 생성
        NotificationUtils.createNotificationChannel(this)

        // 주기적 작업 스케줄링
        schedulePeriodicSync()

        // UI 요소 초기화
        setupViews()

        // 저장된 사용자명 불러오기 (첫 실행 시 다이얼로그 표시 가능성 복원)
        loadSavedUsername()
        // GitHub 데이터 로드 (항상 호출)
        loadGitHubData()
    }

    private fun setupViews() {
        // RecyclerView 초기화
        recyclerView = findViewById(R.id.recycler_view)
        adapter = RepoAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 컨트리뷰션 그리드 뷰 초기화
        contributionGridView = findViewById(R.id.contribution_grid_view)
        
        // 컨트리뷰션 정보 TextView
        tvTodayContributions = findViewById(R.id.today_contributions)
        tvTotalContributions = findViewById(R.id.total_contributions)

        // 새로고침 버튼
        btnRefresh = findViewById(R.id.btn_refresh)
        btnRefresh.setOnClickListener {
            loadGitHubData()
            updateAllWidgets(this)
        }

        // 사용자 변경 버튼 설정
        btnChangeUser = findViewById(R.id.btn_change_user)
        btnChangeUser.setOnClickListener {
            showChangeUsernameDialog()
        }

        yearSpinner = findViewById(R.id.year_spinner)
        val currentYear = LocalDate.now().year
        val years = (currentYear downTo (currentYear - 5)).toList()
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearSpinner.adapter = yearAdapter

        yearSpinner.setSelection(0) // 기본은 현재 연도
        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedYear = years[position]
                loadGitHubData()  // 연도 선택 시 다시 로드
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadGitHubData() {
        // 로딩 표시
        Toast.makeText(this, "데이터 로딩 중...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                // 리포지토리 데이터 불러오기
                val repos = gitHubRepository.getUserRepositories(username)
                adapter.submitList(repos)

                // GraphQL 통합 호출로 연도별 컨트리뷰션 데이터 가져오기
                val (totalCount, contributionsByDay) = gitHubRepository.getContributionYearData(
                    username,
                    selectedYear
                )

                // 오늘 날짜 컨트리뷰션 수 추출
                val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                val todayCount = contributionsByDay[todayDate] ?: 0

                // UI 업데이트
                tvTodayContributions.text = todayCount.toString()
                tvTotalContributions.text = totalCount.toString()
                contributionGridView.setContributionData(contributionsByDay)

                // 성공 메시지
                Toast.makeText(
                    this@MainActivity,
                    "GitHub 데이터가 업데이트되었습니다",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@MainActivity,
                    "데이터 로드 실패: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun schedulePeriodicSync() {
        // 3시간마다 동기화하는 주기적 작업 생성
        val syncRequest = PeriodicWorkRequestBuilder<GitHubSyncWorker>(
            3, TimeUnit.HOURS,
            15, TimeUnit.MINUTES  // 유연한 윈도우 추가
        ).setBackoffCriteria(
            BackoffPolicy.LINEAR,
            WorkRequest.MIN_BACKOFF_MILLIS,
            TimeUnit.MILLISECONDS
        ).build()

        // 유니크 작업으로 등록 (중복 실행 방지)
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "github_sync_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            syncRequest
        )
    }

    private fun showChangeUsernameDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_username, null)
        val etUsername = dialogView.findViewById<EditText>(R.id.et_username)
        etUsername.setText(username)

        AlertDialog.Builder(this)
            .setTitle("GitHub 사용자명 변경")
            .setView(dialogView)
            .setPositiveButton("저장") { _, _ ->
                val newUsername = etUsername.text.toString().trim()
                if (newUsername.isNotEmpty()) {
                    // 사용자명 변경 및 저장
                    username = newUsername
                    saveUsername(newUsername)

                    // 위젯 제공자의 사용자명도 업데이트 (공통 변수 사용)
                    GitHubWidgetProvider4x3.GITHUB_USERNAME = newUsername

                    // 데이터 새로고침
                    loadGitHubData()
                    updateAllWidgets(this)

                    Toast.makeText(this, "사용자명이 변경되었습니다: $newUsername", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "사용자명을 입력해주세요", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun saveUsername(username: String) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_USERNAME, username).apply()
    }

    private fun loadSavedUsername() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedUsername = prefs.getString(KEY_USERNAME, null)
        if (!savedUsername.isNullOrEmpty()) {
            username = savedUsername
            GitHubWidgetProvider4x3.GITHUB_USERNAME = savedUsername
        } else {
            // 사용자 이름 없으면 첫 실행 다이얼로그 표시 (복원)
            showFirstTimeUsernameDialog()
        }
    }

    // 첫 실행 시 사용자명 입력 다이얼로그 (복원 및 유지)
    private fun showFirstTimeUsernameDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_username, null)
        val etUsername = dialogView.findViewById<EditText>(R.id.et_username)
        
        val dialog = AlertDialog.Builder(this)
            .setTitle("GitHub 사용자명 입력")
            .setMessage("GitHub Contribution을 표시할 사용자명을 입력해주세요.")
            .setView(dialogView)
            .setCancelable(false) // 뒤로가기 버튼으로 닫기 방지
            .setPositiveButton("확인", null) // 나중에 리스너 설정
            .create()
        
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val newUsername = etUsername.text.toString().trim()
                if (newUsername.isNotEmpty()) {
                    username = newUsername
                    saveUsername(newUsername)
                    GitHubWidgetProvider4x3.GITHUB_USERNAME = newUsername
                    loadGitHubData()
                    updateAllWidgets(this@MainActivity)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this@MainActivity, "사용자명을 입력해주세요", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_licenses -> {
                // OssLicensesMenuActivity 시작
                startActivity(Intent(this, OssLicensesMenuActivity::class.java))
                // 라이선스 화면 제목 설정 (선택 사항)
                 OssLicensesMenuActivity.setActivityTitle(getString(R.string.license_title))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
