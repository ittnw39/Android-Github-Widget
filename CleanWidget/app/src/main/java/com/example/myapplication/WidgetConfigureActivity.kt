package com.example.myapplication

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class WidgetConfigureActivity : Activity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var etUsername: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_configure)

        // 위젯 ID 가져오기
        intent.extras?.let {
            appWidgetId = it.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // 위젯 ID가 유효하지 않으면 액티비티 종료
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // 결과 기본값을 CANCELED로 설정 (사용자가 뒤로 가기 누를 경우)
        setResult(RESULT_CANCELED)

        etUsername = findViewById(R.id.et_config_username)
        val btnSave: Button = findViewById(R.id.btn_config_save)

        // 저장 버튼 클릭 리스너
        btnSave.setOnClickListener {
            val context = this
            val username = etUsername.text.toString().trim()

            if (username.isNotEmpty()) {
                // 사용자 이름 저장
                saveUsername(context, username)

                // 위젯 업데이트 트리거
                val appWidgetManager = AppWidgetManager.getInstance(context)
                // 모든 Provider의 위젯을 업데이트 하도록 MainActivity의 함수 호출 유도?
                // 여기서는 그냥 MainActivity의 함수를 직접 호출 (context가 Activity이므로 가능)
                MainActivity.updateAllWidgets(context)
                // 또는 해당 위젯만 바로 업데이트 (선택 사항)
                // updateWidgetNow(context, appWidgetManager, appWidgetId)

                // 결과 설정 및 액티비티 종료
                val resultValue = Intent().apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                setResult(RESULT_OK, resultValue)
                finish()
            } else {
                Toast.makeText(context, "사용자명을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 사용자 이름 저장 함수 (MainActivity의 것과 동일하게 유지)
    private fun saveUsername(context: Context, username: String) {
        // 공통 변수 업데이트
        GitHubWidgetProvider4x3.GITHUB_USERNAME = username
        // SharedPreferences에 저장
        val prefs = context.getSharedPreferences("GitHubWidgetPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("username", username).apply()
    }

    // 필요 시 특정 위젯만 즉시 업데이트하는 함수 (선택 사항)
    /*
    private fun updateWidgetNow(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        // 위젯 타입에 맞는 레이아웃 ID 결정 필요 (어려움)
        // 여기서는 모든 타입에 대해 업데이트를 시도하거나, 기본 레이아웃(4x3)으로 가정
        // WidgetUpdateUtils.updateWidget(context, appWidgetManager, appWidgetId, R.layout.widget_layout) // 예시
    }
    */
} 