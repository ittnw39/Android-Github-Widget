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

class GitHubWidgetProvider4x3 : AppWidgetProvider() {

    companion object {
        private const val TAG = "GitHubWidgetProvider4x3"
        const val ACTION_REFRESH_WIDGET_4x3 = "com.example.myapplication.ACTION_REFRESH_WIDGET_4x3"
        var GITHUB_USERNAME = ""
        internal const val MAX_DAYS = 147

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
            R.id.grid_cell_45, R.id.grid_cell_46, R.id.grid_cell_47, R.id.grid_cell_48, R.id.grid_cell_49,
            R.id.grid_cell_50, R.id.grid_cell_51, R.id.grid_cell_52, R.id.grid_cell_53, R.id.grid_cell_54,
            R.id.grid_cell_55, R.id.grid_cell_56, R.id.grid_cell_57, R.id.grid_cell_58, R.id.grid_cell_59,
            R.id.grid_cell_60, R.id.grid_cell_61, R.id.grid_cell_62, R.id.grid_cell_63, R.id.grid_cell_64,
            R.id.grid_cell_65, R.id.grid_cell_66, R.id.grid_cell_67, R.id.grid_cell_68, R.id.grid_cell_69,
            R.id.grid_cell_70, R.id.grid_cell_71, R.id.grid_cell_72, R.id.grid_cell_73, R.id.grid_cell_74,
            R.id.grid_cell_75, R.id.grid_cell_76, R.id.grid_cell_77, R.id.grid_cell_78, R.id.grid_cell_79,
            R.id.grid_cell_80, R.id.grid_cell_81, R.id.grid_cell_82, R.id.grid_cell_83, R.id.grid_cell_84,
            R.id.grid_cell_85, R.id.grid_cell_86, R.id.grid_cell_87, R.id.grid_cell_88, R.id.grid_cell_89,
            R.id.grid_cell_90, R.id.grid_cell_91, R.id.grid_cell_92, R.id.grid_cell_93, R.id.grid_cell_94,
            R.id.grid_cell_95, R.id.grid_cell_96, R.id.grid_cell_97, R.id.grid_cell_98, R.id.grid_cell_99,
            R.id.grid_cell_100, R.id.grid_cell_101, R.id.grid_cell_102, R.id.grid_cell_103, R.id.grid_cell_104,
            R.id.grid_cell_105, R.id.grid_cell_106, R.id.grid_cell_107, R.id.grid_cell_108, R.id.grid_cell_109,
            R.id.grid_cell_110, R.id.grid_cell_111, R.id.grid_cell_112, R.id.grid_cell_113, R.id.grid_cell_114,
            R.id.grid_cell_115, R.id.grid_cell_116, R.id.grid_cell_117, R.id.grid_cell_118, R.id.grid_cell_119,
            R.id.grid_cell_120, R.id.grid_cell_121, R.id.grid_cell_122, R.id.grid_cell_123, R.id.grid_cell_124,
            R.id.grid_cell_125, R.id.grid_cell_126, R.id.grid_cell_127, R.id.grid_cell_128, R.id.grid_cell_129,
            R.id.grid_cell_130, R.id.grid_cell_131, R.id.grid_cell_132, R.id.grid_cell_133, R.id.grid_cell_134,
            R.id.grid_cell_135, R.id.grid_cell_136, R.id.grid_cell_137, R.id.grid_cell_138, R.id.grid_cell_139,
            R.id.grid_cell_140, R.id.grid_cell_141, R.id.grid_cell_142, R.id.grid_cell_143, R.id.grid_cell_144,
            R.id.grid_cell_145, R.id.grid_cell_146
        )

        @JvmStatic
        fun updateContributionGrid(
            views: RemoteViews,
            contributionsData: Map<String, Int>,
            context: Context,
            appWidgetId: Int
        ) {
            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val numRows = 7
            val numCols = (MAX_DAYS + numRows - 1) / numRows

            if (cellIds.size < MAX_DAYS) {
                Log.w(TAG, "Cell IDs size < MAX_DAYS, widget ID: $appWidgetId")
                return
            }

            cellIds.forEach {
                views.setInt(it, "setBackgroundColor", Color.parseColor("#EEEEEE"))
            }

            val startOfWeek = DayOfWeek.MONDAY

            for (dayIndex in 0 until MAX_DAYS) {
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
                        Log.e(TAG, "Error setting color at $cellIndex ($dateStr) widget: $appWidgetId", e)
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

        if (intent.action == ACTION_REFRESH_WIDGET_4x3) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val syncWorkRequest = OneTimeWorkRequestBuilder<GitHubSyncWorker>()
                    .addTag("WIDGET_REFRESH_4x3_$appWidgetId")
                    .build()
                WorkManager.getInstance(context.applicationContext).enqueue(syncWorkRequest)
            }
        }
    }

    @SuppressLint("RemoteViewLayout")
    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        val requestCode = appWidgetId

        val refreshIntent = Intent(context, GitHubWidgetProvider4x3::class.java).apply {
            action = ACTION_REFRESH_WIDGET_4x3
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
            requestCode + 10000,
            mainActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.widget_root, mainActivityPendingIntent)

        if (!NetworkUtils.isNetworkAvailable(context)) {
            views.setTextViewText(R.id.widget_title, "네트워크 연결 필요")
            views.setTextViewText(R.id.today_contributions, "-")
            views.setTextViewText(R.id.total_contributions, "-")
        } else if (GITHUB_USERNAME.isEmpty()) {
            views.setTextViewText(R.id.widget_title, "사용자 설정 필요")
            views.setTextViewText(R.id.today_contributions, "-")
            views.setTextViewText(R.id.total_contributions, "-")
        } else {
            views.setTextViewText(R.id.widget_title, GITHUB_USERNAME)
            views.setTextViewText(R.id.today_contributions, "...")
            views.setTextViewText(R.id.total_contributions, "...")
            cellIds.forEach {
                views.setInt(it, "setBackgroundColor", Color.parseColor("#EEEEEE"))
            }
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
