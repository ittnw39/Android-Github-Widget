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
        val views = RemoteViews(context.packageName, R.layout.github_widget_4x2)
        val requestCode = appWidgetId

        val refreshIntent = Intent(context, GitHubWidgetProvider4x2::class.java).apply {
            action = ACTION_UPDATE_WIDGET_4x2
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context, requestCode, refreshIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.refresh_button, refreshPendingIntent)

        val mainActivityIntent = Intent(context, MainActivity::class.java)
        val mainActivityPendingIntent = PendingIntent.getActivity(
            context, requestCode, mainActivityIntent, PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, mainActivityPendingIntent)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = GitHubRepository()
                val year = LocalDate.now().year
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
                val (totalCount, contributionsByDay) = repository.getContributionYearData(username, year)

                updateContributionGrid(views, contributionsByDay)

                CoroutineScope(Dispatchers.Main).launch {
                    val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                    val todayCount = contributionsByDay[todayDate] ?: 0
                    views.setTextViewText(R.id.widget_title, username)
                    views.setTextViewText(R.id.today_contributions, todayCount.toString())
                    views.setTextViewText(R.id.total_contributions, totalCount.toString())

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                Log.e(TAG, "4x2 위젯 업데이트 실패", e)
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

        if (cellIds.isEmpty()) {
            Log.w(TAG, "Cell IDs are empty. Cannot update contribution grid.")
            return
        }

        for (i in cellIds.indices) {
            try {
                val date = today.minusDays((maxDays - i - 1).toLong())
                val dateStr = date.format(formatter)
                val contributions = contributionsData[dateStr] ?: 0

                val color = when {
                    contributions == 0 -> Color.parseColor("#EEEEEE")
                    contributions < 3 -> Color.parseColor("#9BE9A8")
                    contributions < 5 -> Color.parseColor("#40C463")
                    contributions < 10 -> Color.parseColor("#30A14E")
                    else -> Color.parseColor("#216E39")
                }
                if (i < cellIds.size) {
                    views.setInt(cellIds[i], "setBackgroundColor", color)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating cell index $i (ID: ${cellIds.getOrNull(i)} )", e)
            }
        }
    }
} 