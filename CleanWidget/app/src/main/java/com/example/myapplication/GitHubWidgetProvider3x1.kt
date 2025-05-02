package com.example.myapplication

// 필요한 import 구문들 추가
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

class GitHubWidgetProvider3x1 : AppWidgetProvider() {

    companion object {
        private const val TAG = "GitHubWidgetProvider3x1"
        const val ACTION_UPDATE_WIDGET_3x1 = "com.example.myapplication.ACTION_UPDATE_WIDGET_3x1"
        private const val MAX_DAYS_3x1 = 126 // 18주
        private val cellIds_3x1 = listOf(
             R.id.grid_cell_0, R.id.grid_cell_1, R.id.grid_cell_2, R.id.grid_cell_3, R.id.grid_cell_4, R.id.grid_cell_5, R.id.grid_cell_6,
             R.id.grid_cell_7, R.id.grid_cell_8, R.id.grid_cell_9, R.id.grid_cell_10, R.id.grid_cell_11, R.id.grid_cell_12, R.id.grid_cell_13,
             R.id.grid_cell_14, R.id.grid_cell_15, R.id.grid_cell_16, R.id.grid_cell_17, R.id.grid_cell_18, R.id.grid_cell_19, R.id.grid_cell_20,
             R.id.grid_cell_21, R.id.grid_cell_22, R.id.grid_cell_23, R.id.grid_cell_24, R.id.grid_cell_25, R.id.grid_cell_26, R.id.grid_cell_27,
             R.id.grid_cell_28, R.id.grid_cell_29, R.id.grid_cell_30, R.id.grid_cell_31, R.id.grid_cell_32, R.id.grid_cell_33, R.id.grid_cell_34,
             R.id.grid_cell_35, R.id.grid_cell_36, R.id.grid_cell_37, R.id.grid_cell_38, R.id.grid_cell_39, R.id.grid_cell_40, R.id.grid_cell_41,
             R.id.grid_cell_42, R.id.grid_cell_43, R.id.grid_cell_44, R.id.grid_cell_45, R.id.grid_cell_46, R.id.grid_cell_47, R.id.grid_cell_48,
             R.id.grid_cell_49, R.id.grid_cell_50, R.id.grid_cell_51, R.id.grid_cell_52, R.id.grid_cell_53, R.id.grid_cell_54, R.id.grid_cell_55,
             R.id.grid_cell_56, R.id.grid_cell_57, R.id.grid_cell_58, R.id.grid_cell_59, R.id.grid_cell_60, R.id.grid_cell_61, R.id.grid_cell_62,
             R.id.grid_cell_63, R.id.grid_cell_64, R.id.grid_cell_65, R.id.grid_cell_66, R.id.grid_cell_67, R.id.grid_cell_68, R.id.grid_cell_69,
             R.id.grid_cell_70, R.id.grid_cell_71, R.id.grid_cell_72, R.id.grid_cell_73, R.id.grid_cell_74, R.id.grid_cell_75, R.id.grid_cell_76,
             R.id.grid_cell_77, R.id.grid_cell_78, R.id.grid_cell_79, R.id.grid_cell_80, R.id.grid_cell_81, R.id.grid_cell_82, R.id.grid_cell_83,
             R.id.grid_cell_84, R.id.grid_cell_85, R.id.grid_cell_86, R.id.grid_cell_87, R.id.grid_cell_88, R.id.grid_cell_89, R.id.grid_cell_90,
             R.id.grid_cell_91, R.id.grid_cell_92, R.id.grid_cell_93, R.id.grid_cell_94, R.id.grid_cell_95, R.id.grid_cell_96, R.id.grid_cell_97,
             R.id.grid_cell_98, R.id.grid_cell_99, R.id.grid_cell_100, R.id.grid_cell_101, R.id.grid_cell_102, R.id.grid_cell_103, R.id.grid_cell_104,
             R.id.grid_cell_105, R.id.grid_cell_106, R.id.grid_cell_107, R.id.grid_cell_108, R.id.grid_cell_109, R.id.grid_cell_110, R.id.grid_cell_111,
             R.id.grid_cell_112, R.id.grid_cell_113, R.id.grid_cell_114, R.id.grid_cell_115, R.id.grid_cell_116, R.id.grid_cell_117, R.id.grid_cell_118,
             R.id.grid_cell_119, R.id.grid_cell_120, R.id.grid_cell_121, R.id.grid_cell_122, R.id.grid_cell_123, R.id.grid_cell_124, R.id.grid_cell_125
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
        if (intent.action == ACTION_UPDATE_WIDGET_3x1) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, GitHubWidgetProvider3x1::class.java)
            onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(componentName))
        }
    }

    @SuppressLint("RemoteViewLayout")
    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.github_widget_3x1)
        val requestCode = appWidgetId

        val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_SHOW_USERNAME_DIALOG, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val mainActivityPendingIntent = PendingIntent.getActivity(
            context, requestCode, mainActivityIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.widget_root, mainActivityPendingIntent)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = GitHubRepository()
                val currentYear = LocalDate.now().year
                val previousYear = currentYear - 1
                val username = GitHubWidgetProvider4x3.GITHUB_USERNAME
                 if (username.isEmpty()) {
                    Log.w(TAG, "GitHub username is empty. Cannot update widget.")
                    return@launch
                }
                val (_, currentYearContributions) = repository.getContributionYearData(username, currentYear)
                val (_, previousYearContributions) = repository.getContributionYearData(username, previousYear)
                val combinedContributionsByDay = mutableMapOf<String, Int>()
                combinedContributionsByDay.putAll(previousYearContributions)
                combinedContributionsByDay.putAll(currentYearContributions)

                updateContributionGrid(views, combinedContributionsByDay)

                CoroutineScope(Dispatchers.Main).launch {
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                 Log.e(TAG, "3x1 위젯 업데이트 실패", e)
            }
        }
    }

    private fun updateContributionGrid(views: RemoteViews, contributionsData: Map<String, Int>) {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val cellIds = cellIds_3x1
        val maxDays = MAX_DAYS_3x1
        val numRows = 7
        val numCols = (maxDays + 6) / 7

        if (cellIds.size < maxDays) { Log.w(TAG, "Cell IDs size mismatch"); return }

        for (id in cellIds) { try { views.setInt(id, "setBackgroundColor", Color.parseColor("#EEEEEE")) } catch (e: Exception) { Log.e(TAG, "Init err $id",e)} }

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
                 } catch (e: Exception) { Log.e(TAG, "SetColor err $cellIndex $cellId $dateStr",e) }
            } else { Log.w(TAG, "Invalid index $cellIndex $dateStr r$row c$col") }
        }
    }
} 