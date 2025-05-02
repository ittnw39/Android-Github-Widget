package com.example.myapplication

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.example.myapplication.repository.GitHubRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek

// 4x2 위젯용 Provider 클래스
class GitHubWidgetProvider4x2 : AppWidgetProvider() {

    companion object {
        private const val TAG = "GitHubWidgetProvider4x2"
        const val ACTION_UPDATE_WIDGET_4x2 = "com.example.myapplication.ACTION_UPDATE_WIDGET_4x2"
        // GITHUB_USERNAME 및 cellIds는 GitHubWidgetProvider4x3의 companion object 참조
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { updateAppWidget(context, appWidgetManager, it) }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle?) {
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET_4x2) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, GitHubWidgetProvider4x2::class.java)
            onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(componentName))
        }
    }

    @SuppressLint("RemoteViewLayout")
    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        Log.d(TAG, "updateAppWidget started for ID: $appWidgetId")
        val views = RemoteViews(context.packageName, R.layout.github_widget_4x2)
        val requestCode = appWidgetId

        val refreshIntent = Intent(context, GitHubWidgetProvider4x2::class.java).apply {
            action = ACTION_UPDATE_WIDGET_4x2
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context, requestCode, refreshIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.refresh_button, refreshPendingIntent)

        val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_SHOW_USERNAME_DIALOG, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val mainActivityPendingIntent = PendingIntent.getActivity(
            context, requestCode, mainActivityIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.widget_root, mainActivityPendingIntent)

        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Coroutine started for ID: $appWidgetId")
            try {
                val repository = GitHubRepository()
                val currentYear = LocalDate.now().year
                val previousYear = currentYear - 1
                val username = GitHubWidgetProvider4x3.GITHUB_USERNAME
                if (username.isEmpty()) {
                    Log.w(TAG, "GitHub username is empty. Cannot update widget.")
                    CoroutineScope(Dispatchers.Main).launch {
                        views.setTextViewText(R.id.widget_title, "사용자 설정 필요")
                        views.setTextViewText(R.id.total_contributions, "-")
                        views.setTextViewText(R.id.today_contributions, "-")
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                    return@launch
                }
                Log.d(TAG, "Username: $username, Year: $currentYear")

                // 현재 연도 데이터 가져오기
                val (currentTotalCount, currentYearContributions) = repository.getContributionYearData(username, currentYear)
                // 작년 데이터 가져오기
                val (_, previousYearContributions) = repository.getContributionYearData(username, previousYear)

                // 데이터 병합
                val combinedContributionsByDay = mutableMapOf<String, Int>()
                combinedContributionsByDay.putAll(previousYearContributions)
                combinedContributionsByDay.putAll(currentYearContributions)

                Log.d(TAG, "Data fetched for ID: $appWidgetId - Combined Days: ${combinedContributionsByDay.size}")

                Log.d(TAG, "Calling updateContributionGrid for ID: $appWidgetId")
                updateContributionGrid(views, combinedContributionsByDay)
                Log.d(TAG, "Finished updateContributionGrid for ID: $appWidgetId")

                CoroutineScope(Dispatchers.Main).launch {
                    Log.d(TAG, "Updating UI on Main thread for ID: $appWidgetId")
                    val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                    // 오늘 카운트는 병합된 데이터에서 찾음
                    val todayCount = combinedContributionsByDay[todayDate] ?: 0
                    views.setTextViewText(R.id.widget_title, username)
                    views.setTextViewText(R.id.today_contributions, todayCount.toString())
                    // 전체 카운트는 현재 연도 기준으로 표시 (혹은 필요시 합산)
                    views.setTextViewText(R.id.total_contributions, currentTotalCount.toString())

                    Log.d(TAG, "Calling appWidgetManager.updateAppWidget for ID: $appWidgetId")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                    Log.d(TAG, "Finished appWidgetManager.updateAppWidget for ID: $appWidgetId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "4x2 위젯 업데이트 실패 for ID: $appWidgetId", e)
                CoroutineScope(Dispatchers.Main).launch {
                    views.setTextViewText(R.id.widget_title, "오류 발생 (4x2)")
                    views.setTextViewText(R.id.today_contributions, "?")
                    views.setTextViewText(R.id.total_contributions, "?")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }

    private fun updateContributionGrid(views: RemoteViews, contributionsData: Map<String, Int>) {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val cellIds = GitHubWidgetProvider4x3.cellIds
        val maxDays = GitHubWidgetProvider4x3.MAX_DAYS
        val numRows = 7
        val numCols = (maxDays + 6) / 7 // 총 열 개수 계산 (21)

        if (cellIds.size < maxDays) {
            Log.w(TAG, "Cell IDs size (${cellIds.size}) is less than MAX_DAYS ($maxDays). Cannot update grid properly.")
            // return
        }

        // 모든 셀을 기본 색상으로 초기화
        for (id in cellIds) {
             try {
                 views.setInt(id, "setBackgroundColor", Color.parseColor("#EEEEEE"))
             } catch (e: Exception) {
                 Log.e(TAG, "Error initializing cell ID: $id", e)
             }
        }

        val todayDayOfWeek = (today.dayOfWeek.value - 1 + 7) % 7

        for (dayIndex in 0 until maxDays) {
            val currentDate = today.minusDays(dayIndex.toLong())
            val dateStr = currentDate.format(formatter)
            val contributions = contributionsData[dateStr] ?: 0

            val row = (currentDate.dayOfWeek.value - 1 + 7) % 7
            val weeksAgo = java.time.temporal.ChronoUnit.WEEKS.between(currentDate.with(DayOfWeek.MONDAY), today.with(DayOfWeek.MONDAY)).toInt()
            val col = (numCols - 1) - weeksAgo

            val cellIndex = col * numRows + row

            if (col >= 0 && cellIndex >= 0 && cellIndex < cellIds.size) {
                 val cellId = cellIds[cellIndex]
                try {
                    val color = when {
                        contributions == 0 -> Color.parseColor("#EEEEEE")
                        contributions < 3 -> Color.parseColor("#9BE9A8")
                        contributions < 5 -> Color.parseColor("#40C463")
                        contributions < 10 -> Color.parseColor("#30A14E")
                        else -> Color.parseColor("#216E39")
                    }
                    views.setInt(cellId, "setBackgroundColor", color)
                 } catch (e: Exception) {
                     Log.e(TAG, "Error setting color for cell index $cellIndex (ID: $cellId), date: $dateStr", e)
                 }
            } else {
                 Log.w(TAG, "Calculated invalid cell index: $cellIndex for date: $dateStr (row: $row, col: $col)")
            }
        }
    }
} 