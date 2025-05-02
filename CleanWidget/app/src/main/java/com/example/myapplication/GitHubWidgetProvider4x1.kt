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

// 4x1 위젯용 Provider 클래스
class GitHubWidgetProvider4x1 : AppWidgetProvider() {

    companion object {
        private const val TAG = "GitHubWidgetProvider4x1"
        const val ACTION_UPDATE_WIDGET_4x1 = "com.example.myapplication.ACTION_UPDATE_WIDGET_4x1"
        // GITHUB_USERNAME은 공통으로 사용하므로 여기서는 선언하지 않음 (GitHubWidgetProvider4x3의 companion object 참조)
        // 수정: 25주(175일) 데이터 표시
        private const val MAX_DAYS_4x1 = 175
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
            R.id.grid_cell_98, R.id.grid_cell_99, R.id.grid_cell_100, R.id.grid_cell_101, R.id.grid_cell_102, R.id.grid_cell_103, R.id.grid_cell_104,
            R.id.grid_cell_105, R.id.grid_cell_106, R.id.grid_cell_107, R.id.grid_cell_108, R.id.grid_cell_109, R.id.grid_cell_110, R.id.grid_cell_111,
            R.id.grid_cell_112, R.id.grid_cell_113, R.id.grid_cell_114, R.id.grid_cell_115, R.id.grid_cell_116, R.id.grid_cell_117, R.id.grid_cell_118,
            R.id.grid_cell_119, R.id.grid_cell_120, R.id.grid_cell_121, R.id.grid_cell_122, R.id.grid_cell_123, R.id.grid_cell_124, R.id.grid_cell_125,
            R.id.grid_cell_126, R.id.grid_cell_127, R.id.grid_cell_128, R.id.grid_cell_129, R.id.grid_cell_130, R.id.grid_cell_131, R.id.grid_cell_132,
            R.id.grid_cell_133, R.id.grid_cell_134, R.id.grid_cell_135, R.id.grid_cell_136, R.id.grid_cell_137, R.id.grid_cell_138, R.id.grid_cell_139,
            R.id.grid_cell_140, R.id.grid_cell_141, R.id.grid_cell_142, R.id.grid_cell_143, R.id.grid_cell_144, R.id.grid_cell_145, R.id.grid_cell_146,
            R.id.grid_cell_147, R.id.grid_cell_148, R.id.grid_cell_149, R.id.grid_cell_150, R.id.grid_cell_151, R.id.grid_cell_152, R.id.grid_cell_153,
            R.id.grid_cell_154, R.id.grid_cell_155, R.id.grid_cell_156, R.id.grid_cell_157, R.id.grid_cell_158, R.id.grid_cell_159, R.id.grid_cell_160,
            R.id.grid_cell_161, R.id.grid_cell_162, R.id.grid_cell_163, R.id.grid_cell_164, R.id.grid_cell_165, R.id.grid_cell_166, R.id.grid_cell_167,
            R.id.grid_cell_168, R.id.grid_cell_169, R.id.grid_cell_170, R.id.grid_cell_171, R.id.grid_cell_172, R.id.grid_cell_173, R.id.grid_cell_174
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
        val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
            // 추가: 다이얼로그 표시 extra
            putExtra(MainActivity.EXTRA_SHOW_USERNAME_DIALOG, true)
            // Activity가 이미 떠 있을 때 새 Intent를 받도록 플래그 추가
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val mainActivityPendingIntent = PendingIntent.getActivity(
            context, requestCode, mainActivityIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.widget_root, mainActivityPendingIntent)

        // 데이터 로딩 및 그리드 업데이트
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = GitHubRepository()
                val currentYear = LocalDate.now().year
                val previousYear = currentYear - 1
                val username = GitHubWidgetProvider4x3.GITHUB_USERNAME
                 if (username.isEmpty()) {
                    Log.w(TAG, "GitHub username is empty. Cannot update widget.")
                    // 사용자 이름 없으면 업데이트 중단 (혹은 빈 그리드 표시)
                    return@launch
                }

                // 현재 연도 데이터 가져오기
                val (_, currentYearContributions) = repository.getContributionYearData(username, currentYear)
                // 작년 데이터 가져오기
                val (_, previousYearContributions) = repository.getContributionYearData(username, previousYear)

                // 데이터 병합
                val combinedContributionsByDay = mutableMapOf<String, Int>()
                combinedContributionsByDay.putAll(previousYearContributions)
                combinedContributionsByDay.putAll(currentYearContributions)

                // 그리드 업데이트 호출 (병합된 데이터 사용)
                updateContributionGrid(views, combinedContributionsByDay)

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

    // 그리드 업데이트 로직 수정: 요일 정렬 반영
    private fun updateContributionGrid(views: RemoteViews, contributionsData: Map<String, Int>) {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val cellIds = cellIds_4x1
        val maxDays = MAX_DAYS_4x1
        val numRows = 7
        val numCols = (maxDays + 6) / 7 // 총 열 개수 계산 (올림)

        if (cellIds.size < maxDays) { // ID 개수 확인
            Log.w(TAG, "Cell IDs size (${cellIds.size}) is less than MAX_DAYS ($maxDays). Cannot update grid properly.")
            // return // 또는 일부만 표시
        }

        // 모든 셀을 기본 색상으로 초기화
        for (id in cellIds) {
             try {
                 views.setInt(id, "setBackgroundColor", Color.parseColor("#EEEEEE"))
             } catch (e: Exception) {
                 Log.e(TAG, "Error initializing cell ID: $id", e)
             }
        }

        // 오늘 날짜의 요일 (0=월요일, 6=일요일)
        val todayDayOfWeek = (today.dayOfWeek.value - 1 + 7) % 7

        for (dayIndex in 0 until maxDays) {
            val currentDate = today.minusDays(dayIndex.toLong())
            val dateStr = currentDate.format(formatter)
            val contributions = contributionsData[dateStr] ?: 0

            // 해당 날짜의 row, col 계산
            val row = (currentDate.dayOfWeek.value - 1 + 7) % 7
            val weeksAgo = java.time.temporal.ChronoUnit.WEEKS.between(currentDate.with(DayOfWeek.MONDAY), today.with(DayOfWeek.MONDAY)).toInt()
            val col = (numCols - 1) - weeksAgo // 가장 오른쪽 열이 0주 전

            val cellIndex = col * numRows + row

            if (col >= 0 && cellIndex >= 0 && cellIndex < cellIds.size) {
                 val cellId = cellIds[cellIndex]
                try {
                    val color = when {
                        contributions == 0 -> Color.parseColor("#EEEEEE") // 이미 초기화했지만, 데이터 없는 날 명시적 처리
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