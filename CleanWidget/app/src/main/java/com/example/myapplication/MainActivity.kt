package com.example.myapplication

import android.content.Context
import android.os.Bundle
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
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 저장된 사용자명 불러오기
        loadSavedUsername()

        // 알림 채널 생성
        NotificationUtils.createNotificationChannel(this)

        // 주기적 작업 스케줄링
        schedulePeriodicSync()

        // UI 요소 초기화
        setupViews()

        // GitHub 데이터 로드
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
            GitHubWidgetProvider.updateWidgets(this)
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

                    // 위젯 제공자의 사용자명도 업데이트
                    GitHubWidgetProvider.GITHUB_USERNAME = newUsername

                    // 데이터 새로고침
                    loadGitHubData()
                    GitHubWidgetProvider.updateWidgets(this)

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
            GitHubWidgetProvider.GITHUB_USERNAME = savedUsername
        } else {
            // 저장된 사용자명이 없으면 입력 다이얼로그 표시
            showFirstTimeUsernameDialog()
        }
    }

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
        
        // 다이얼로그가 표시된 후 버튼 동작 설정
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val newUsername = etUsername.text.toString().trim()
                if (newUsername.isNotEmpty()) {
                    // 사용자명 저장 및 적용
                    username = newUsername
                    saveUsername(newUsername)
                    GitHubWidgetProvider.GITHUB_USERNAME = newUsername
                    
                    // 데이터 로드
                    loadGitHubData()
                    GitHubWidgetProvider.updateWidgets(this@MainActivity)
                    
                    // 다이얼로그 닫기
                    dialog.dismiss()
                } else {
                    // 빈 입력 시 에러 메시지 표시
                    Toast.makeText(this@MainActivity, "사용자명을 입력해주세요", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        dialog.show()
    }
}
