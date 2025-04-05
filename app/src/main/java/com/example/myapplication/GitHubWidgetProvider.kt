package com.example.myapplication

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.example.myapplication.repository.GitHubRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GitHubWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val TAG = "GitHubWidgetProvider"
        const val ACTION_UPDATE_WIDGET = "com.example.myapplication.ACTION_UPDATE_WIDGET"
        var GITHUB_USERNAME = "ittnw39"
        private const val MAX_DAYS = 49

        val cellIds = listOf(
            R.id.grid_cell_0, R.id.grid_cell_1, R.id.grid_cell_2, R.id.grid_cell_3, R.id.grid_cell_4, R.id.grid_cell_5, R.id.grid_cell_6,
            R.id.grid_cell_7, R.id.grid_cell_8, R.id.grid_cell_9, R.id.grid_cell_10, R.id.grid_cell_11, R.id.grid_cell_12, R.id.grid_cell_13,
            R.id.grid_cell_14, R.id.grid_cell_15, R.id.grid_cell_16, R.id.grid_cell_17, R.id.grid_cell_18, R.id.grid_cell_19, R.id.grid_cell_20,
            R.id.grid_cell_21, R.id.grid_cell_22, R.id.grid_cell_23, R.id.grid_cell_24, R.id.grid_cell_25, R.id.grid_cell_26, R.id.grid_cell_27,
            R.id.grid_cell_28, R.id.grid_cell_29, R.id.grid_cell_30, R.id.grid_cell_31, R.id.grid_cell_32, R.id.grid_cell_33, R.id.grid_cell_34,
            R.id.grid_cell_35, R.id.grid_cell_36, R.id.grid_cell_37, R.id.grid_cell_38, R.id.grid_cell_39, R.id.grid_cell_40, R.id.grid_cell_41,
            R.id.grid_cell_42, R.id.grid_cell_43, R.id.grid_cell_44, R.id.grid_cell_45, R.id.grid_cell_46, R.id.grid_cell_47, R.id.grid_cell_48
        )

        @JvmStatic
        fun updateWidgets(context: Context) {
            val intent = Intent(context, GitHubWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            context.sendBroadcast(intent)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { updateAppWidget(context, appWidgetManager, it) }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle?) {
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, GitHubWidgetProvider::class.java))
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    @SuppressLint("RemoteViewLayout")
    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        val refreshIntent = Intent(context, GitHubWidgetProvider::class.java).apply {
            action = ACTION_UPDATE_WIDGET
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context, 0, refreshIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.refresh_button, refreshPendingIntent)

        val mainActivityIntent = Intent(context, MainActivity::class.java)
        val mainActivityPendingIntent = PendingIntent.getActivity(
            context, 0, mainActivityIntent, PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_title, mainActivityPendingIntent)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = GitHubRepository()
                val contributionData = repository.getUserContributions(GITHUB_USERNAME)

                CoroutineScope(Dispatchers.Main).launch {
                    views.setTextViewText(R.id.widget_title, "$GITHUB_USERNAME 컨트리뷰션")
                    views.setTextViewText(R.id.today_contributions, contributionData.todayContributions.toString())
                    views.setTextViewText(R.id.total_contributions, contributionData.totalContributions.toString())

                    updateContributionGrid(context, views, contributionData.contributionsByDay)
                    appWidgetManager.updateAppWidget(appWidgetId, views)

                    if (!contributionData.hasTodayContributions()) {
                        NotificationUtils.showContributionReminder(context)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "위젯 업데이트 실패", e)
            }
        }
    }

    private fun updateContributionGrid(context: Context, views: RemoteViews, contributionsData: Map<String, Int>) {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        for (i in cellIds.indices) {
            val date = today.minusDays((MAX_DAYS - i - 1).toLong())
            val dateStr = date.format(formatter)
            val contributions = contributionsData[dateStr] ?: 0

            val colorResId = when {
                contributions == 0 -> android.R.color.darker_gray
                contributions < 3 -> R.color.github_green_light
                contributions < 5 -> R.color.github_green_medium
                contributions < 10 -> R.color.github_green_dark
                else -> R.color.github_green_extreme
            }

            views.setInt(cellIds[i], "setBackgroundResource", colorResId)
        }
    }


}