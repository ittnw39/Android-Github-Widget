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

// 4x1 위젯용 Provider 클래스
class GitHubWidgetProvider4x1 : AppWidgetProvider() {

    companion object {
        private const val TAG = "GitHubWidgetProvider4x1"
        const val ACTION_UPDATE_WIDGET_4x1 = "com.example.myapplication.ACTION_UPDATE_WIDGET_4x1"
        // GITHUB_USERNAME은 공통으로 사용하므로 여기서는 선언하지 않음 (GitHubWidgetProvider4x3의 companion object 참조)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { updateAppWidget(context, appWidgetManager, it) } // 내부 함수 호출로 변경
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle?) {
        updateAppWidget(context, appWidgetManager, appWidgetId) // 내부 함수 호출로 변경
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET_4x1) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, GitHubWidgetProvider4x1::class.java)
            onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(componentName))
        }
    }

    // 내부 업데이트 로직 복원
    @SuppressLint("RemoteViewLayout")
    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.github_widget_4x1)
        val requestCode = appWidgetId // 각 위젯 인스턴스별 고유 requestCode

        // 리프레시 버튼
        val refreshIntent = Intent(context, GitHubWidgetProvider4x1::class.java).apply {
            action = ACTION_UPDATE_WIDGET_4x1
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context, requestCode, refreshIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.refresh_button, refreshPendingIntent)

        // 메인 액티비티 클릭 이동
        val mainActivityIntent = Intent(context, MainActivity::class.java)
        val mainActivityPendingIntent = PendingIntent.getActivity(
            context, requestCode, mainActivityIntent, PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, mainActivityPendingIntent)

        // 데이터 로딩 및 UI 업데이트
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = GitHubRepository()
                val year = LocalDate.now().year
                val username = GitHubWidgetProvider4x3.GITHUB_USERNAME // 4x3의 공통 변수 참조
                if (username.isEmpty()) {
                    Log.w(TAG, "GitHub username is empty. Cannot update widget.")
                    CoroutineScope(Dispatchers.Main).launch {
                        views.setTextViewText(R.id.widget_title, "사용자 설정 필요")
                        views.setTextViewText(R.id.total_contributions, "-")
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                    return@launch
                }
                val (totalCount, _) = repository.getContributionYearData(username, year)

                CoroutineScope(Dispatchers.Main).launch {
                    views.setTextViewText(R.id.widget_title, username)
                    views.setTextViewText(R.id.total_contributions, totalCount.toString())
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                Log.e(TAG, "4x1 위젯 업데이트 실패", e)
                 CoroutineScope(Dispatchers.Main).launch {
                    views.setTextViewText(R.id.widget_title, "오류 발생 (4x1)")
                    views.setTextViewText(R.id.total_contributions, "?")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
} 