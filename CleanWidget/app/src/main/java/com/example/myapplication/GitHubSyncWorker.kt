package com.example.myapplication

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.repository.GitHubRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GitHubSyncWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {


    companion object {
        private const val TAG = "GitHubSyncWorker"

        private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val currentTime = LocalDateTime.now().format(dateFormatter)
            Log.d(TAG, "백그라운드 동기화 작업 시작 - $currentTime")
            
            // GitHub 리포지토리 인스턴스 생성
            val repository = GitHubRepository()
            
            // 컨트리뷰션 데이터 가져오기 (수정: GitHubWidgetProvider4x3 참조)
            val username = GitHubWidgetProvider4x3.GITHUB_USERNAME
            if (username.isEmpty()) {
                Log.w(TAG, "Username is empty, skipping sync.")
                return@withContext Result.success() // Or Result.failure() if this is an error state
            }
            val year = LocalDate.now().year
            val (totalCount, contributionsByDay) = repository.getContributionYearData(username, year)
            
            // 위젯 업데이트 (수정: MainActivity.updateAllWidgets 호출)
            // GitHubWidgetProvider.updateWidgets(applicationContext)
            MainActivity.updateAllWidgets(applicationContext)
            
            // 오늘 컨트리뷰션이 없으면 알림 생성
            val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            val todayCount = contributionsByDay[todayDate] ?: 0
            
            if (todayCount == 0) {
                // 마지막 알림 시간 확인
                val lastNotificationTime = getLastNotificationTime()
                val now = System.currentTimeMillis()
                
                // 마지막 알림으로부터 6시간이 지났거나 첫 알림인 경우에만 알림 표시
                if (lastNotificationTime == 0L || (now - lastNotificationTime) > 6 * 60 * 60 * 1000) {
                    NotificationUtils.showContributionReminder(applicationContext)
                    saveLastNotificationTime(now)
                }
            }
            
            Log.d(TAG, "백그라운드 동기화 작업 완료 - $currentTime")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "백그라운드 동기화 작업 실패", e)
            // 실패 시 재시도 정책 적용
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    private fun getLastNotificationTime(): Long {
        val prefs = applicationContext.getSharedPreferences("GitHubWidgetPrefs", Context.MODE_PRIVATE)
        return prefs.getLong("last_notification_time", 0L)
    }
    
    private fun saveLastNotificationTime(time: Long) {
        val prefs = applicationContext.getSharedPreferences("GitHubWidgetPrefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("last_notification_time", time).apply()
    }
}