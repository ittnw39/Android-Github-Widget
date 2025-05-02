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
import com.example.myapplication.util.NetworkUtils

class GitHubSyncWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "GitHubSyncWorker"
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val currentTime = LocalDateTime.now().format(dateFormatter)
            Log.d(TAG, "백그라운드 동기화 작업 시작 - $currentTime")

            if (!NetworkUtils.isNetworkAvailable(applicationContext)) {
                Log.w(TAG, "네트워크 연결 안 됨. 재시도 예정.")
                return@withContext Result.retry()
            }

            val repository = GitHubRepository()
            val username = GitHubWidgetProvider4x3.GITHUB_USERNAME
            if (username.isEmpty()) {
                Log.w(TAG, "Username is empty, skipping sync.")
                return@withContext Result.success()
            }

            val year = LocalDate.now().year
            val (totalCount, contributionsByDay) = repository.getContributionYearData(username, year)

            MainActivity.updateAllWidgets(applicationContext)

            val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            val todayCount = contributionsByDay[todayDate] ?: 0

            if (todayCount == 0) {
                val lastNotificationTime = getLastNotificationTime()
                val now = System.currentTimeMillis()

                if (lastNotificationTime == 0L || (now - lastNotificationTime) > 6 * 60 * 60 * 1000) {
                    NotificationUtils.showContributionReminder(applicationContext)
                    saveLastNotificationTime(now)
                }
            }

            Log.d(TAG, "백그라운드 동기화 작업 완료 - $currentTime")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "백그라운드 동기화 작업 실패 - Error: ${e.message}", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
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
