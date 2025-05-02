package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

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
    private var cellSize = 25f // 셀 크기 조정
    private var cellSpacing = 5f // 셀 간격 조정
    private val dayLabelWidth = 40f // 요일 라벨 너비
    private val monthLabelHeight = 30f // 월 라벨 높이
    
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
    private val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textSize = 20f // 라벨 텍스트 크기
        textAlign = Paint.Align.CENTER
    }
    private val monthLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textSize = 20f
        textAlign = Paint.Align.LEFT
    }
    
    // 날짜 포맷
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val dayLabels = listOf("Mon", "", "Wed", "", "Fri", "", "") // 월/수/금 만 표시
    private val textBounds = Rect() // 텍스트 경계 계산용
    
    // 컨트리뷰션 데이터 설정
    fun setContributionData(data: Map<String, Int>) {
        contributionData = data.toSortedMap() // 정렬된 데이터 사용
        requestLayout()
        invalidate() // 뷰 다시 그리기
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (contributionData.isEmpty()) {
            setMeasuredDimension(0, 0)
            return
        }

        // 가장 오래된 날짜와 최신 날짜 찾기 (데이터가 정렬되어 있다고 가정)
        val firstDate = LocalDate.parse(contributionData.keys.first(), dateFormatter)
        val lastDate = LocalDate.parse(contributionData.keys.last(), dateFormatter)

        // 첫 주의 시작 요일(월요일 기준) 계산
        val firstDayOfWeekOffset = firstDate.dayOfWeek.value - DayOfWeek.MONDAY.value
        val startOffset = if (firstDayOfWeekOffset < 0) firstDayOfWeekOffset + 7 else firstDayOfWeekOffset

        // 총 필요한 셀 개수 (시작 오프셋 포함)
        val totalDays = java.time.temporal.ChronoUnit.DAYS.between(firstDate, lastDate) + 1
        val totalCells = startOffset + totalDays

        // 필요한 주(열)의 수 계산
        val weeks = (totalCells / 7.0).toInt().coerceAtLeast(1)

        // 라벨 공간 포함한 전체 너비 및 높이 계산
        val totalWidth = dayLabelWidth + (cellSize + cellSpacing) * weeks - cellSpacing + paddingLeft + paddingRight
        val totalHeight = monthLabelHeight + (cellSize + cellSpacing) * 7 - cellSpacing + paddingTop + paddingBottom

        setMeasuredDimension(totalWidth.toInt(), totalHeight.toInt())
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (contributionData.isEmpty()) return

        val firstDate = LocalDate.parse(contributionData.keys.first(), dateFormatter)
        val firstDayOfWeekOffset = firstDate.dayOfWeek.value - DayOfWeek.MONDAY.value
        val startOffset = if (firstDayOfWeekOffset < 0) firstDayOfWeekOffset + 7 else firstDayOfWeekOffset

        var currentX = paddingLeft + dayLabelWidth // X 시작 위치 (요일 라벨 너비 + 왼쪽 패딩)
        var currentY = paddingTop + monthLabelHeight // Y 시작 위치 (월 라벨 높이 + 위쪽 패딩)
        var weekOfYear = -1 // 월 라벨 추적용
        var currentMonth = -1 // 월 변경 감지용

        // 1. 월 라벨 그리기
        var dateIterator = firstDate
        var colIdx = 0
        val lastDate = LocalDate.parse(contributionData.keys.last(), dateFormatter)
        val firstColX = paddingLeft + dayLabelWidth // 첫 열의 시작 X

        while (!dateIterator.isAfter(lastDate)) {
            if (dateIterator.monthValue != currentMonth) {
                 currentMonth = dateIterator.monthValue
                val monthText = dateIterator.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                val textX = firstColX + colIdx * (cellSize + cellSpacing)
                canvas.drawText(monthText, textX, paddingTop + monthLabelHeight - labelPaint.descent(), monthLabelPaint)
            }
             // 다음 주 첫날로 이동 (주의: 날짜 계산 방식에 따라 월 라벨 위치 조정 필요)
             // 여기서는 단순하게 각 열 시작점에 월 표시 시도
            dateIterator = dateIterator.plusWeeks(1).with(DayOfWeek.MONDAY)
            colIdx++
        }

        // 2. 요일 라벨 그리기
        for (i in dayLabels.indices) {
            if (dayLabels[i].isNotEmpty()) {
                val textY = currentY + i * (cellSize + cellSpacing) + cellSize / 2 - (labelPaint.descent() + labelPaint.ascent()) / 2
                 canvas.drawText(dayLabels[i], paddingLeft + dayLabelWidth / 2, textY, labelPaint)
            }
        }

        // 3. 컨트리뷰션 셀 그리기
        var cellIndex = 0
        contributionData.forEach { (dateStr, count) ->
            val date = LocalDate.parse(dateStr, dateFormatter)
            val dayOfWeek = date.dayOfWeek.value - DayOfWeek.MONDAY.value // 0 = Monday, 6 = Sunday
            val row = if (dayOfWeek < 0) dayOfWeek + 7 else dayOfWeek

            // 해당 날짜의 열 인덱스 계산 (주의: 시작 오프셋 필요)
            val daysFromStart = java.time.temporal.ChronoUnit.DAYS.between(firstDate, date)
            val currentCellIndex = startOffset + daysFromStart
            val col = (currentCellIndex / 7).toInt()

            val left = paddingLeft + dayLabelWidth + col * (cellSize + cellSpacing)
            val top = paddingTop + monthLabelHeight + row * (cellSize + cellSpacing)

            val colorIndex = when {
                count == 0 -> 0
                count < 3 -> 1
                count < 5 -> 2
                count < 10 -> 3
                else -> 4
            }
            cellPaint.color = colorLevels[colorIndex]
            canvas.drawRect(left, top, left + cellSize, top + cellSize, cellPaint)
        }
    }
} 