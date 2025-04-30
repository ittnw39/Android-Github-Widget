package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    private var cellSize = 20f
    private var cellSpacing = 4f
    
    // 색상 배열 (기여도에 따라 다른 색상)
    private val colorLevels = arrayOf(
        Color.parseColor("#EEEEEE"),
        Color.parseColor("#9BE9A8"),
        Color.parseColor("#40C463"),
        Color.parseColor("#30A14E"),
        Color.parseColor("#216E39")
    )
    
    // 그리드 차원
    private val columns = 12 // 12개월
    private val rows = 7 // 요일 (월~일)
    
    // 페인트 객체
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    // 날짜 포맷
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    // 컨트리뷰션 데이터 설정
    fun setContributionData(data: Map<String, Int>) {
        contributionData = data
        requestLayout()
        invalidate() // 뷰 다시 그리기
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val days = contributionData.size
        // 주 단위로 컬럼 개수 계산
        val weeks = (contributionData.size / 7.0).toInt().coerceAtLeast(1)
        val width = ((cellSize + cellSpacing) * weeks - cellSpacing).toInt()
        val height = ((cellSize + cellSpacing) * 7 - cellSpacing).toInt()
        setMeasuredDimension(width, height)
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val sortedDates = contributionData.keys.sorted()
        var col = 0
        var row = 0

        sortedDates.forEach { date ->
            val count = contributionData[date] ?: 0
            val colorIndex = when {
                count == 0 -> 0
                count < 3 -> 1
                count < 5 -> 2
                count < 10 -> 3
                else -> 4
            }

            paint.color = colorLevels[colorIndex]

            val left = col * (cellSize + cellSpacing)
            val top = row * (cellSize + cellSpacing)

            canvas.drawRect(left, top, left + cellSize, top + cellSize, paint)

            row++
            if (row == 7) {
                row = 0
                col++
            }
        }
    }
} 