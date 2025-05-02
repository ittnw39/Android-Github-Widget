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

class GitHubWidgetProvider4x3 : AppWidgetProvider() {

    companion object {
        private const val TAG = "GitHubWidgetProvider4x3"
        const val ACTION_UPDATE_WIDGET_4x3 = "com.example.myapplication.ACTION_UPDATE_WIDGET_4x3"
        var GITHUB_USERNAME = ""
        internal const val MAX_DAYS = 49

        val cellIds = listOf(
            R.id.grid_cell_0, R.id.grid_cell_1, R.id.grid_cell_2, R.id.grid_cell_3, R.id.grid_cell_4,
            R.id.grid_cell_5, R.id.grid_cell_6, R.id.grid_cell_7, R.id.grid_cell_8, R.id.grid_cell_9,
            R.id.grid_cell_10, R.id.grid_cell_11, R.id.grid_cell_12, R.id.grid_cell_13, R.id.grid_cell_14,
            R.id.grid_cell_15, R.id.grid_cell_16, R.id.grid_cell_17, R.id.grid_cell_18, R.id.grid_cell_19,
            R.id.grid_cell_20, R.id.grid_cell_21, R.id.grid_cell_22, R.id.grid_cell_23, R.id.grid_cell_24,
            R.id.grid_cell_25, R.id.grid_cell_26, R.id.grid_cell_27, R.id.grid_cell_28, R.id.grid_cell_29,
            R.id.grid_cell_30, R.id.grid_cell_31, R.id.grid_cell_32, R.id.grid_cell_33, R.id.grid_cell_34,
            R.id.grid_cell_35, R.id.grid_cell_36, R.id.grid_cell_37, R.id.grid_cell_38, R.id.grid_cell_39,
            R.id.grid_cell_40, R.id.grid_cell_41, R.id.grid_cell_42, R.id.grid_cell_43, R.id.grid_cell_44,
            R.id.grid_cell_45, R.id.grid_cell_46, R.id.grid_cell_47, R.id.grid_cell_48
        )
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { updateAppWidget(context, appWidgetManager, it) }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle?) {
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET_4x3) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, GitHubWidgetProvider4x3::class.java)
            onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(componentName))
        }
    }

    @SuppressLint("RemoteViewLayout")
    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        val requestCode = appWidgetId

        val refreshIntent = Intent(context, GitHubWidgetProvider4x3::class.java).apply {
            action = ACTION_UPDATE_WIDGET_4x3
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
                if (GITHUB_USERNAME.isEmpty()) {
                    Log.w(TAG, "GitHub username is empty. Cannot update widget.")
                    CoroutineScope(Dispatchers.Main).launch {
                        views.setTextViewText(R.id.widget_title, "사용자 설정 필요")
                        views.setTextViewText(R.id.total_contributions, "-")
                        views.setTextViewText(R.id.today_contributions, "-")
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                    return@launch
                }
                val (totalCount, contributionsByDay) = repository.getContributionYearData(GITHUB_USERNAME, year)

                updateContributionGrid(views, contributionsByDay)

                CoroutineScope(Dispatchers.Main).launch {
                    val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                    val todayCount = contributionsByDay[todayDate] ?: 0
                    views.setTextViewText(R.id.widget_title, GITHUB_USERNAME)
                    views.setTextViewText(R.id.today_contributions, todayCount.toString())
                    views.setTextViewText(R.id.total_contributions, totalCount.toString())

                    appWidgetManager.updateAppWidget(appWidgetId, views)

                    if (!contributionsByDay.containsKey(todayDate) || todayCount == 0) {
                        NotificationUtils.showContributionReminder(context)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "4x3 위젯 업데이트 실패", e)
                CoroutineScope(Dispatchers.Main).launch {
                    views.setTextViewText(R.id.widget_title, "오류 발생 (4x3)")
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

        if (cellIds.isEmpty()) {
            Log.w(TAG, "Cell IDs are empty. Cannot update contribution grid.")
            return
        }

        for (i in cellIds.indices) {
            try {
                val date = today.minusDays((MAX_DAYS - i - 1).toLong())
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
