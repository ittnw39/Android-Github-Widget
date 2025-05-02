package com.example.myapplication

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapplication.util.NetworkUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GitHubWidgetProvider4x2 : AppWidgetProvider() {

    companion object {
        private const val TAG = "GitHubWidgetProvider4x2"
        const val ACTION_REFRESH_WIDGET_4x2 = "com.example.myapplication.ACTION_REFRESH_WIDGET_4x2"

        @JvmStatic
        fun updateContributionGrid(
            views: RemoteViews,
            contributionsData: Map<String, Int>,
            context: Context,
            appWidgetId: Int
        ) {
            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val cellIds = GitHubWidgetProvider4x3.cellIds
            val maxDays = GitHubWidgetProvider4x3.MAX_DAYS
            val numRows = 7
            val numCols = (maxDays + numRows - 1) / numRows

            if (cellIds.size < maxDays) {
                Log.w(TAG, "Cell IDs size mismatch for 4x2 W:$appWidgetId")
                return
            }

            for (id in cellIds) {
                try {
                    views.setInt(id, "setBackgroundColor", Color.parseColor("#EEEEEE"))
                } catch (e: Exception) {}
            }

            val startOfWeek = DayOfWeek.MONDAY

            for (dayIndex in 0 until maxDays) {
                val currentDate = today.minusDays(dayIndex.toLong())
                val dateStr = currentDate.format(formatter)
                val contributions = contributionsData[dateStr] ?: 0

                val row = (currentDate.dayOfWeek.value - startOfWeek.value + 7) % 7
                val col = (numCols - 1) - java.time.temporal.ChronoUnit.WEEKS.between(
                    currentDate.with(startOfWeek), today.with(startOfWeek)
                ).toInt()
                val cellIndex = col * numRows + row

                if (col >= 0 && cellIndex in cellIds.indices) {
                    try {
                        val color = getContributionColor(contributions)
                        views.setInt(cellIds[cellIndex], "setBackgroundColor", color)
                    } catch (e: Exception) {
                        Log.e(TAG, "SetColor err 4x2 $cellIndex ${cellIds[cellIndex]} $dateStr W:$appWidgetId", e)
                    }
                }
            }
        }

        private fun getContributionColor(contributions: Int): Int {
            return Color.parseColor(
                when {
                    contributions == 0 -> "#EEEEEE"
                    contributions < 3 -> "#9BE9A8"
                    contributions < 5 -> "#40C463"
                    contributions < 10 -> "#30A14E"
                    else -> "#216E39"
                }
            )
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

        if (intent.action == ACTION_REFRESH_WIDGET_4x2) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val syncWorkRequest = OneTimeWorkRequestBuilder<GitHubSyncWorker>()
                    .addTag("WIDGET_REFRESH_4x2_$appWidgetId")
                    .build()
                WorkManager.getInstance(context.applicationContext).enqueue(syncWorkRequest)
            }
        }
    }

    @SuppressLint("RemoteViewLayout")
    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.github_widget_4x2)
        val requestCode = appWidgetId

        val refreshIntent = Intent(context, GitHubWidgetProvider4x2::class.java).apply {
            action = ACTION_REFRESH_WIDGET_4x2
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context.applicationContext,
            requestCode,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.refresh_button, refreshPendingIntent)

        val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainActivityPendingIntent = PendingIntent.getActivity(
            context.applicationContext,
            requestCode + 10005,
            mainActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.widget_root, mainActivityPendingIntent)

        if (!NetworkUtils.isNetworkAvailable(context)) {
            views.setTextViewText(R.id.widget_title, "\uB124\uD2B8\uC6CC\uD06C \uC5F0\uACB0 \uD544\uC694")
            views.setTextViewText(R.id.today_contributions, "-")
            views.setTextViewText(R.id.total_contributions, "-")
        } else if (GitHubWidgetProvider4x3.GITHUB_USERNAME.isEmpty()) {
            views.setTextViewText(R.id.widget_title, "\uC0AC\uC6A9\uC790 \uC124\uC815 \uD544\uC694")
            views.setTextViewText(R.id.today_contributions, "-")
            views.setTextViewText(R.id.total_contributions, "-")
        } else {
            views.setTextViewText(R.id.widget_title, GitHubWidgetProvider4x3.GITHUB_USERNAME)
            views.setTextViewText(R.id.today_contributions, "...")
            views.setTextViewText(R.id.total_contributions, "...")
            GitHubWidgetProvider4x3.cellIds.forEach {
                views.setInt(it, "setBackgroundColor", Color.parseColor("#EEEEEE"))
            }
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
