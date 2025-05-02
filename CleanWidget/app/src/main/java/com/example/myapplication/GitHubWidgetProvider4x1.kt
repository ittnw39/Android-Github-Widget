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

// 4x1 위젯용 Provider 클래스
class GitHubWidgetProvider4x1 : AppWidgetProvider() {

    companion object {
        private const val TAG = "GitHubWidgetProvider4x1"
        const val ACTION_UPDATE_WIDGET_4x1 = "com.example.myapplication.ACTION_UPDATE_WIDGET_4x1"
        // GITHUB_USERNAME은 공통으로 사용하므로 여기서는 선언하지 않음 (GitHubWidgetProvider4x3의 companion object 참조)
        // 수정: 15주(105일) 데이터 표시
        private const val MAX_DAYS_4x1 = 105
        private val cellIds_4x1 = listOf(
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
            R.id.grid_cell_98, R.id.grid_cell_99, R.id.grid_cell_100, R.id.grid_cell_101, R.id.grid_cell_102, R.id.grid_cell_103, R.id.grid_cell_104
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
        if (intent.action == ACTION_UPDATE_WIDGET_4x1) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, GitHubWidgetProvider4x1::class.java)
            onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(componentName))
        }
    }

    @SuppressLint("RemoteViewLayout")
    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.github_widget_4x1)
        val requestCode = appWidgetId

        // 메인 액티비티 클릭 이동 (루트 레이아웃 클릭)
        val mainActivityIntent = Intent(context, MainActivity::class.java)
        val mainActivityPendingIntent = PendingIntent.getActivity(
            context, requestCode, mainActivityIntent, PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, mainActivityPendingIntent)

        // 데이터 로딩 및 그리드 업데이트
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = GitHubRepository()
                val year = LocalDate.now().year
                val username = GitHubWidgetProvider4x3.GITHUB_USERNAME
                 if (username.isEmpty()) {
                    Log.w(TAG, "GitHub username is empty. Cannot update widget.")
                    // 사용자 이름 없으면 업데이트 중단 (혹은 빈 그리드 표시)
                    return@launch
                }
                val (_, contributionsByDay) = repository.getContributionYearData(username, year)

                // 그리드 업데이트 호출 (수정된 로직 사용)
                updateContributionGrid(views, contributionsByDay)

                // UI 업데이트 (그리드만)
                CoroutineScope(Dispatchers.Main).launch {
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                Log.e(TAG, "4x1 위젯 업데이트 실패", e)
                // 오류 시 특별한 처리는 하지 않음 (기존 상태 유지 또는 빈 상태)
            }
        }
    }

    // 그리드 업데이트 로직 수정
    private fun updateContributionGrid(views: RemoteViews, contributionsData: Map<String, Int>) {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        // 수정: 4x1 전용 cellIds와 MAX_DAYS 사용
        val cellIds = cellIds_4x1
        val maxDays = MAX_DAYS_4x1

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