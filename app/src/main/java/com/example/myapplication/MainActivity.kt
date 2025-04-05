package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RepoAdapter
    private lateinit var tvTodayContributions: TextView
    private lateinit var tvTotalContributions: TextView
    private lateinit var contributionGridView: ContributionGridView
    private lateinit var btnRefresh: Button
    private lateinit var btnChangeUser: Button
    private lateinit var btnSetToken: Button

    private val gitHubRepository = GitHubRepository()
    private var username = Constants.DEFAULT_GITHUB_USERNAME // 기본값을 Constants에서 가져옴

    companion object {
        private const val PREFS_NAME = "GitHubWidgetPrefs"
        private const val KEY_USERNAME = "username"
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

        // GitHub 토큰 설정 버튼
        btnSetToken = findViewById(R.id.btn_set_token)
        btnSetToken.setOnClickListener {
            showSetTokenDialog()
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

                // 컨트리뷰션 데이터 불러오기
                val contributionData = gitHubRepository.getUserContributions(username)

                // UI 업데이트
                tvTodayContributions.text = contributionData.todayContributions.toString()
                tvTotalContributions.text = contributionData.totalContributions.toString()
                
                // 컨트리뷰션 그리드 업데이트
                contributionGridView.setContributionData(contributionData.contributionsByDay)

                // 성공 메시지
                Toast.makeText(this@MainActivity,
                    "GitHub 데이터가 업데이트되었습니다",
                    Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity,
                    "데이터 로드 실패: ${e.message}",
                    Toast.LENGTH_LONG).show()
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

    private fun showSetTokenDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_set_token, null)
        val etToken = dialogView.findViewById<EditText>(R.id.et_token)

        // 현재 토큰 설정 (토큰이 있는 경우)
        val currentToken = GitHubApiClient.getSavedToken(this)
        if (currentToken.isNotEmpty()) {
            etToken.setText(currentToken)
        }

        AlertDialog.Builder(this)
            .setTitle("GitHub 토큰 설정")
            .setView(dialogView)
            .setPositiveButton("저장") { _, _ ->
                val token = etToken.text.toString().trim()

                // 토큰 저장
                GitHubApiClient.saveToken(this, token)

                // 확인 메시지
                val message = if (token.isEmpty()) {
                    "GitHub 토큰이 제거되었습니다"
                } else {
                    "GitHub 토큰이 설정되었습니다"
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                // 데이터 새로고침
                loadGitHubData()
                GitHubWidgetProvider.updateWidgets(this)
            }
            .setNegativeButton("취소", null)
            .setNeutralButton("토큰 발급 방법") { _, _ ->
                showTokenHelpDialog()
            }
            .show()
    }

    private fun showTokenHelpDialog() {
        AlertDialog.Builder(this)
            .setTitle("GitHub 토큰 발급 방법")
            .setMessage(
                "1. GitHub 계정으로 로그인합니다.\n" +
                        "2. 오른쪽 상단의 프로필 > Settings를 클릭합니다.\n" +
                        "3. 왼쪽 사이드바 하단의 'Developer settings'를 클릭합니다.\n" +
                        "4. 'Personal access tokens' > 'Tokens (classic)'을 선택합니다.\n" +
                        "5. 'Generate new token'을 클릭합니다.\n" +
                        "6. 토큰 이름을 입력하고 'repo' 스코프를 선택합니다.\n" +
                        "7. 'Generate token'을 클릭하고 생성된 토큰을 복사합니다.\n\n" +
                        "* 발급받은 토큰은 처음 한 번만 표시되므로 잘 복사해두세요!"
            )
            .setPositiveButton("확인", null)
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
            // 기본값이 설정되지 않았다면 Constants 값 사용
            GitHubWidgetProvider.GITHUB_USERNAME = username
        }
    }
}
