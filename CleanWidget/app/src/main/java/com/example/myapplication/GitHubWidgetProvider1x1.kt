package com.example.myapplication

// 필요한 import 구문들 추가
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
// import android.content.ComponentName // 사용되지 않음
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
// import com.example.myapplication.repository.GitHubRepository // 직접 사용 안 함
import com.example.myapplication.util.NetworkUtils
// import kotlinx.coroutines.CoroutineScope // 직접 사용 안 함
// import kotlinx.coroutines.Dispatchers // 직접 사용 안 함
// import kotlinx.coroutines.launch // 직접 사용 안 함
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek

class GitHubWidgetProvider1x1 : AppWidgetProvider() {

    companion object {
        private const val TAG = "GitHubWidgetProvider1x1"
        // ACTION_UPDATE_WIDGET_1x1 제거
        // const val ACTION_UPDATE_WIDGET_1x1 = "com.example.myapplication.ACTION_UPDATE_WIDGET_1x1"
        private const val MAX_DAYS_1x1 = 28 // 4주
        private val cellIds_1x1 = listOf(
            R.id.grid_cell_0, R.id.grid_cell_1, R.id.grid_cell_2, R.id.grid_cell_3, R.id.grid_cell_4, R.id.grid_cell_5, R.id.grid_cell_6,
            R.id.grid_cell_7, R.id.grid_cell_8, R.id.grid_cell_9, R.id.grid_cell_10, R.id.grid_cell_11, R.id.grid_cell_12, R.id.grid_cell_13,
            R.id.grid_cell_14, R.id.grid_cell_15, R.id.grid_cell_16, R.id.grid_cell_17, R.id.grid_cell_18, R.id.grid_cell_19, R.id.grid_cell_20,
            R.id.grid_cell_21, R.id.grid_cell_22, R.id.grid_cell_23, R.id.grid_cell_24, R.id.grid_cell_25, R.id.grid_cell_26, R.id.grid_cell_27
        )
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { updateAppWidget(context, appWidgetManager, it) }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle?) {
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    // onReceive 제거
    /*
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // ... 기존 코드 제거 ...
    }
    */

    @SuppressLint("RemoteViewLayout")
    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.github_widget_1x1)
        val requestCode = appWidgetId // 고유 ID

        // --- 메인 액티비티 실행 버튼 설정 ---
        val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        // 고유 requestCode 사용 (appWidgetId + 10001)
        val mainActivityRequestCode = requestCode + 10001
        val mainActivityPendingIntent = PendingIntent.getActivity(
            context.applicationContext,
            mainActivityRequestCode,
            mainActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.widget_root, mainActivityPendingIntent)
        // --- 메인 액티비티 실행 버튼 설정 끝 ---

        // --- 초기 UI 설정 ---
        // 1x1 위젯은 텍스트 뷰가 없으므로, 그리드만 초기화
        initializeContributionGrid(views)
        appWidgetManager.updateAppWidget(appWidgetId, views)
        // --- 초기 UI 설정 끝 ---

        // --- Coroutine을 이용한 직접 데이터 로딩 제거 ---
        /*
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.w(TAG, "Network not available for widget ID: $appWidgetId")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // ... 데이터 로딩 로직 제거 ...
                updateContributionGrid(views, combinedContributionsByDay) // 직접 호출 안 함

                CoroutineScope(Dispatchers.Main).launch {
                    appWidgetManager.updateAppWidget(appWidgetId, views) // 이미 위에서 호출됨
                }
            } catch (e: Exception) {
                Log.e(TAG, "1x1 위젯 업데이트 실패 - Error: ${e.message}", e)
            }
        }
        */
    }

    // 그리드 초기화 함수
    private fun initializeContributionGrid(views: RemoteViews) {
        if (cellIds_1x1.size < MAX_DAYS_1x1) {
            Log.w(TAG, "Cell IDs size mismatch for 1x1 init")
            return
        }
        for (id in cellIds_1x1) {
            try {
                views.setInt(id, "setBackgroundColor", Color.parseColor("#EEEEEE"))
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing 1x1 cell ID: $id", e)
            }
        }
    }

    // 이 함수는 MainActivity.updateWidgetUI에서 호출됨
    internal fun updateContributionGrid(views: RemoteViews, contributionsData: Map<String, Int>, context: Context, appWidgetId: Int) {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val cellIds = cellIds_1x1
        val maxDays = MAX_DAYS_1x1
        val numRows = 7
        val numCols = (maxDays + numRows - 1) / numRows // 올림

        if (cellIds.size < maxDays) {
            Log.w(TAG, "Cell IDs size mismatch for 1x1 widget $appWidgetId")
            return
        }

        // 그리드 셀 초기화
        for (id in cellIds) {
            try { views.setInt(id, "setBackgroundColor", Color.parseColor("#EEEEEE")) } catch (e: Exception) { /* Log */ }
        }

        val startOfWeek = DayOfWeek.MONDAY

        for (dayIndex in 0 until maxDays) {
            val currentDate = today.minusDays(dayIndex.toLong())
            val dateStr = currentDate.format(formatter)
            val contributions = contributionsData[dateStr] ?: 0

            val dayOfWeekValue = currentDate.dayOfWeek.value
            val row = (dayOfWeekValue - startOfWeek.value + 7) % 7

            val weeksAgo = java.time.temporal.ChronoUnit.WEEKS.between(
                currentDate.with(startOfWeek),
                today.with(startOfWeek)
            ).toInt()
            val col = (numCols - 1) - weeksAgo

            val cellIndex = col * numRows + row

            if (col >= 0 && cellIndex >= 0 && cellIndex < cellIds.size) {
                val cellId = cellIds[cellIndex]
                try {
                    val color = getContributionColor(contributions) // 공통 함수 사용 고려
                    views.setInt(cellId, "setBackgroundColor", color)
                 } catch (e: Exception) { Log.e(TAG, "SetColor err 1x1 $cellIndex $cellId $dateStr W:$appWidgetId",e) }
            } else { /* Log invalid index */ }
        }
    }

    // 기여도 색상 계산 (GitHubWidgetProvider4x3 와 동일하게 가져오거나 공통 유틸로 분리 필요)
    private fun getContributionColor(contributions: Int): Int {
        return Color.parseColor(when {
            contributions == 0 -> "#EEEEEE"
            contributions < 3 -> "#9BE9A8"
            contributions < 5 -> "#40C463"
            contributions < 10 -> "#30A14E"
            else -> "#216E39"
        })
    }
} 