package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationUtils {
    private const val CHANNEL_ID = "github_contributions_channel"
    private const val CHANNEL_NAME = "GitHub Contributions"
    private const val CHANNEL_DESCRIPTION = "GitHub 컨트리뷰션 알림"
    private const val NOTIFICATION_ID = 1001
    
    // 알림 채널 생성
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    // 컨트리뷰션 리마인더 알림 표시
    fun showContributionReminder(context: Context) {
        // 메인 액티비티 실행 인텐트
        val mainIntent = Intent(context, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(
            context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE
        )
        
        // 새로고침 액션 인텐트 (수정: 모든 위젯 업데이트 트리거)
        val refreshIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val refreshPendingIntent = PendingIntent.getActivity(
            context, 1, refreshIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // 알림 빌더
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("GitHub 컨트리뷰션 알림")
            .setContentText("오늘 GitHub 컨트리뷰션이 없습니다. 커밋을 남겨보세요!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("오늘 GitHub 컨트리뷰션이 없습니다.\n" +
                        "커밋을 남겨서 당신의 GitHub 활동을 기록하세요!\n" +
                        "앱을 열어 자세한 정보를 확인하거나 새로고침할 수 있습니다."))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(mainPendingIntent)
            .addAction(
                android.R.drawable.ic_popup_sync,
                "앱 열기",
                refreshPendingIntent
            )
            .setAutoCancel(true)
            .build()
        
        // 알림 표시
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, notification)
            }
        } catch (e: SecurityException) {
            // 알림 권한이 없는 경우 처리
            e.printStackTrace()
        }
    }
}
