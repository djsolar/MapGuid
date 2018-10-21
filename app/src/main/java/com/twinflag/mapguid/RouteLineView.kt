package com.twinflag.mapguid

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View

/**
 * TODO: document your custom view class.
 */
class RouteLineView : View {

    companion object {
        const val DEFAULT_LINE_COLOR = Color.GREEN
        const val DEFAULT_LINE_WIDTH = 1.0f
    }

    private var _startNodeDrawable: Drawable? = null
    private var _endNodeDrawable: Drawable? = null
    private var _edgeLineColor: Int? = null
    private var _edgeLineWidth: Float? = null
    private var linePaint: Paint? = null
    private var linePoints: MutableList<Point>? = null
    private val path = Path()

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
                attrs, R.styleable.RouteLineView, defStyle, 0)
        _startNodeDrawable = a.getDrawable(R.styleable.RouteLineView_startNodeDrawable)
        _endNodeDrawable = a.getDrawable(R.styleable.RouteLineView_endNodeDrawable)
        _edgeLineColor = a.getColor(
                R.styleable.RouteLineView_edgeLineColor,
                DEFAULT_LINE_COLOR)
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        _edgeLineWidth = a.getDimension(
                R.styleable.RouteLineView_edgeLineWidth,
                DEFAULT_LINE_WIDTH)
        a.recycle()

        // Set up a default TextPaint object
        linePaint = Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            style = Paint.Style.STROKE
            color = _edgeLineColor!!
            strokeWidth = _edgeLineWidth!!
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 绘制开始界点的图像
        path.reset()

        if (linePoints != null && linePoints?.size!! > 0) {
            linePoints?.first()?.let {
                val drawableWidth = _startNodeDrawable?.intrinsicWidth
                val halfDrawableWidth = drawableWidth!! / 2
                val drawableHeight = _startNodeDrawable?.intrinsicHeight
                val halfDrawableHeight = drawableHeight!! / 2
                _startNodeDrawable?.setBounds(it.x - halfDrawableWidth, it.y -
                        halfDrawableHeight, it.x + halfDrawableWidth, it.y + halfDrawableHeight)
                _startNodeDrawable?.draw(canvas)
                path.moveTo(it.x.toFloat(), it.y.toFloat())
            }
        }

        if (linePoints != null && linePoints?.size!! > 1) {
            linePoints?.forEachIndexed { index, point ->
                if (index > 0) {
                    path.lineTo(point.x.toFloat(), point.y.toFloat())
                }
            }
            canvas.drawPath(path, linePaint)
            // 绘制结束点的图标
            val endPoint = linePoints?.last()
            val drawableWidth = _endNodeDrawable?.intrinsicWidth
            val halfDrawableWidth = drawableWidth!! / 2
            val drawableHeight = _endNodeDrawable?.intrinsicHeight
            val halfDrawableHeight = drawableHeight!! / 2
            _endNodeDrawable?.setBounds(endPoint?.x!! - halfDrawableWidth, endPoint.y -
                    halfDrawableHeight, endPoint.x + halfDrawableWidth, endPoint.y + halfDrawableHeight)
            _endNodeDrawable?.draw(canvas)
        }

    }

    fun drawNavigationLine(points: MutableList<Point>) {
        this.linePoints = points
        this.invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        path.close()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(widthSize, heightSize)
    }
}
