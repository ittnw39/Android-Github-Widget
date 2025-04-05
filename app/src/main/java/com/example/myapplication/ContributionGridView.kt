package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * GitHub 컨트리뷰션 그래프를 표시하기 위한 커스텀 뷰
 * 바둑판 모양의 그리드로 기여도를 시각화
 */
class ContributionGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 컨트리뷰션 데이터 맵 (날짜 문자열 -> 기여 횟수)
    private var contributionData: Map<String, Int> = emptyMap()
    
    // 색상 배열 (기여도에 따라 다른 색상)
    private val colorLevels = arrayOf(
        ContextCompat.getColor(context, android.R.color.darker_gray), // 기여 없음 (회색)
        ContextCompat.getColor(context, android.R.color.holo_green_light), // 레벨 1 (연한 녹색)
        ContextCompat.getColor(context, android.R.color.holo_green_dark), // 레벨 2 (중간 녹색)
        ContextCompat.getColor(context, R.color.github_green_medium), // 레벨 3 (진한 녹색)
        ContextCompat.getColor(context, R.color.github_green_dark) // 레벨 4 (아주 진한 녹색)
    )
    
    // 셀 크기 및 간격
    private var cellSize = 10f
    private var cellSpacing = 2f
    
    // 그리드 차원
    private val columns = 12 // 12개월
    private val rows = 7 // 요일 (월~일)
    
    // 페인트 객체
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    // 날짜 포맷
    @RequiresApi(Build.VERSION_CODES.O)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    // 컨트리뷰션 데이터 설정
    fun setContributionData(data: Map<String, Int>) {
        contributionData = data
        invalidate() // 뷰 다시 그리기
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 가로 크기에 맞게 셀 크기 조정
        val width = MeasureSpec.getSize(widthMeasureSpec)
        cellSize = (width - (columns - 1) * cellSpacing) / columns
        
        val height = (cellSize * rows) + ((rows - 1) * cellSpacing)
        setMeasuredDimension(width, height.toInt())
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // 현재 날짜
        val today = LocalDate.now()
        // 1년 전
        val oneYearAgo = today.minusYears(1)
        
        // 각 셀 그리기
        for (col in 0 until columns) {
            for (row in 0 until rows) {
                // 날짜 계산 (현재 위치에 해당하는 날짜)
                val dayOffset = (col * rows + row)
                val date = oneYearAgo.plusDays(dayOffset.toLong())
                
                // 현재 날짜를 넘어가면 그리지 않음
                if (date.isAfter(today)) continue
                
                // 날짜 문자열
                val dateStr = date.format(dateFormatter)
                
                // 해당 날짜의 기여도 가져오기
                val contributions = contributionData[dateStr] ?: 0
                
                // 기여도에 따른 색상 레벨
                val colorIndex = when {
                    contributions == 0 -> 0
                    contributions < 3 -> 1
                    contributions < 5 -> 2
                    contributions < 10 -> 3
                    else -> 4
                }
                
                // 셀 위치 계산
                val left = col * (cellSize + cellSpacing)
                val top = row * (cellSize + cellSpacing)
                
                // 셀 그리기
                paint.color = colorLevels[colorIndex]
                canvas.drawRect(
                    left,
                    top,
                    left + cellSize,
                    top + cellSize,
                    paint
                )
            }
        }
    }
} 