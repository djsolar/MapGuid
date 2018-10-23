package com.twinflag.mapguid

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.twinflag.mapguid.mvp.model.bean.MapPointF

/**
 * TODO: document your custom view class.
 */
class RouteLineView : View {

    companion object {
        const val DEFAULT_LINE_COLOR = Color.GREEN
        const val DEFAULT_LINE_WIDTH = 1.0f
        const val DEFAULT_CIRCLE_RADIUS = 3.0f
        const val DEFAULT_FOOT_PRINT_DISTANCE = 15.0
    }

    private var _startNodeDrawable: Drawable? = null
    private var _endNodeDrawable: Drawable? = null
    private var _edgeLineColor: Int? = null
    private var _edgeLineWidth: Float? = null
    private var linePaint: Paint? = null
    private var ballPaint: Paint? = null
    private var linePoints: MutableList<MapPointF>? = null
    private val path = Path()
    private val scaleMatrix = Matrix()
    private var mCurrentValue = 1.0f
    private val _mapPointDistanceList = mutableListOf<PointDistanceFromStartPoint>()
    private var _allNodeDistance = 0.0
    private var mCurrentPointF: RatioPointF? = null
    private var mLastPointF: RatioPointF? = null
    private var _rightFootPrintDrawable: Drawable? = null
    private var _leftFootPrintDrawable: Drawable? = null
    private var isRight: Boolean = true
    private var _animationFootPrint = false

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
        _rightFootPrintDrawable = a.getDrawable(R.styleable.RouteLineView_foot_print_right)
        _leftFootPrintDrawable = a.getDrawable(R.styleable.RouteLineView_foot_print_left)
        a.recycle()

        // Set up a default TextPaint object
        linePaint = Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            style = Paint.Style.STROKE
            color = _edgeLineColor!!
            strokeWidth = _edgeLineWidth!!
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
        }
        ballPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isDither = true
            style = Paint.Style.FILL_AND_STROKE
            color = _edgeLineColor!!
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 绘制开始界点的图像
        path.reset()
        if (linePoints != null && linePoints?.size!! > 0) {
            linePoints?.first()?.let {
                path.moveTo(it.x, it.y)
                val drawableWidth = _startNodeDrawable?.intrinsicWidth
                val halfDrawableWidth = drawableWidth!! / 2
                val drawableHeight = _startNodeDrawable?.intrinsicHeight
                canvas.save()
                scaleMatrix.reset()
                // val scaleValue = scaleValues[(startCount % scaleValues.size).toInt()]
                scaleMatrix.postScale(mCurrentValue, mCurrentValue)
                canvas.translate(it.x - halfDrawableWidth * mCurrentValue, it.y -
                        drawableHeight!! * mCurrentValue)
                val startBitmap = (_startNodeDrawable as BitmapDrawable).bitmap
                canvas.drawBitmap(startBitmap, scaleMatrix, null)
                canvas.restore()
            }
        }

        if (linePoints != null && linePoints?.size!! > 1) {
            linePoints?.let {
                it.forEachIndexed { index, point ->
                    if (index > 0) {
                        // val previousPoint = linePoints?.get(index - 1)
                        // path.quadTo((point.x + previousPoint!!.x) / 2, (point.y + previousPoint.y) / 2, point.x, point.y)
                        if (index > 0) {
                            path.lineTo(point.x, point.y)
                        }
                    }
                }
            }
            // 绘制结束点的图标
            val endPoint = linePoints?.last()
            val drawableWidth = _endNodeDrawable?.intrinsicWidth
            val halfDrawableWidth = drawableWidth!! / 2
            val drawableHeight = _endNodeDrawable?.intrinsicHeight
            val endBitmap = (_endNodeDrawable as BitmapDrawable).bitmap
            canvas.save()
            scaleMatrix.reset()
            // val scaleValue = scaleValues[(endCount % scaleValues.size).toInt()]
            scaleMatrix.postScale(mCurrentValue, mCurrentValue)
            canvas.translate(endPoint?.x!! - halfDrawableWidth * mCurrentValue, endPoint.y - drawableHeight!! * mCurrentValue)
            canvas.drawBitmap(endBitmap, scaleMatrix, null)
            canvas.restore()
            canvas.drawPath(path, linePaint)
        }

        if (mCurrentPointF != null) {
            /*if (_leftFootPrintDrawable != null && _rightFootPrintDrawable != null) {
                canvas.save()
                canvas.rotate(mCurrentPointF?.k?.toFloat()!!, mCurrentPointF?.x!!, mCurrentPointF?.y!!)
                if (isRight) {
                    val rightWidth = _rightFootPrintDrawable!!.intrinsicWidth / 2
                    val rightHeight = _rightFootPrintDrawable!!.intrinsicHeight / 2
                    _rightFootPrintDrawable!!.setBounds((mCurrentPointF!!.x - rightWidth / 2 + rightWidth / 4).toInt(), (mCurrentPointF!!.y - rightHeight / 2).toInt(),
                            (mCurrentPointF!!.x + rightWidth / 2 + rightWidth / 4).toInt(), (mCurrentPointF!!.y + rightHeight / 2).toInt())
                    _rightFootPrintDrawable!!.draw(canvas)

                    if (mLastPointF != null) {
                        val leftWidth = _leftFootPrintDrawable!!.intrinsicWidth / 2
                        val leftHeight = _leftFootPrintDrawable!!.intrinsicHeight / 2
                        _leftFootPrintDrawable!!.setBounds((mLastPointF!!.x - leftWidth / 2 - leftWidth / 4).toInt(), (mLastPointF!!.y - leftHeight / 2).toInt(),
                                (mLastPointF!!.x + leftWidth / 2 - leftWidth / 4).toInt(), (mLastPointF!!.y + leftHeight / 2).toInt())
                        _leftFootPrintDrawable!!.draw(canvas)
                    }

                } else {
                    val leftWidth = _leftFootPrintDrawable!!.intrinsicWidth / 2
                    val leftHeight = _leftFootPrintDrawable!!.intrinsicHeight / 2
                    _leftFootPrintDrawable!!.setBounds((mCurrentPointF!!.x - leftWidth / 2 - leftWidth / 4).toInt(), (mCurrentPointF!!.y - leftHeight / 2).toInt(),
                            (mCurrentPointF!!.x + leftWidth / 2 - leftWidth / 4).toInt(), (mCurrentPointF!!.y + leftHeight / 2).toInt())
                    _leftFootPrintDrawable!!.draw(canvas)
                    if (mLastPointF != null) {
                        val rightWidth = _rightFootPrintDrawable!!.intrinsicWidth / 2
                        val rightHeight = _rightFootPrintDrawable!!.intrinsicHeight / 2
                        _rightFootPrintDrawable!!.setBounds((mLastPointF!!.x - rightWidth / 2 + rightWidth / 4).toInt(), (mLastPointF!!.y - rightHeight / 2).toInt(),
                                (mLastPointF!!.x + rightWidth / 2 + rightWidth / 4).toInt(), (mLastPointF!!.y + rightHeight / 2).toInt())
                        _rightFootPrintDrawable!!.draw(canvas)
                    }
                }
                if (_animationFootPrint) {
                    _animationFootPrint = false
                    isRight = !isRight
                }
                canvas.restore()*/
            //}
            canvas.drawCircle(mCurrentPointF!!.x, mCurrentPointF!!.y, dp2px(DEFAULT_CIRCLE_RADIUS), ballPaint)
        }
    }

    fun drawNavigationLine(points: MutableList<MapPointF>) {
        this.linePoints = points
        loadPointDistance()
        startAnimation()
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

    private fun startAnimation() {
        val valueAnimator = ValueAnimator.ofFloat(1.0f, 1.4f)
        valueAnimator.duration = 1000
        valueAnimator.addUpdateListener {
            mCurrentValue = it.animatedValue as Float
            invalidate()
        }
        valueAnimator.repeatMode = ValueAnimator.REVERSE
        valueAnimator.repeatCount = ValueAnimator.INFINITE
        valueAnimator.start()

        val lineValueAnimator = ValueAnimator.ofFloat(0.0f, _allNodeDistance.toFloat())
        lineValueAnimator.duration = 10000
        lineValueAnimator.repeatCount = ValueAnimator.INFINITE
        lineValueAnimator.repeatMode = ValueAnimator.RESTART

        lineValueAnimator.addUpdateListener { va ->
            val distance = va.animatedValue as Float
            val pdsp = _mapPointDistanceList.firstOrNull { distance <= it.superior && distance >= it.prescribed }
            mCurrentPointF = pdsp?.findPoint(distance.toDouble())
            invalidate()
           /* val distance = va.animatedValue as Float
            val pdsp = _mapPointDistanceList.firstOrNull { distance <= it.superior && distance >= it.prescribed }
            if (pdsp != null) {
                val tempPoint = pdsp.findPoint(distance.toDouble())
                if (mCurrentPointF == null) {
                    mCurrentPointF = tempPoint
                    _animationFootPrint = true
                    invalidate()
                } else {
                    if (distanceBetweenPoints(mCurrentPointF!!, tempPoint) >= DEFAULT_FOOT_PRINT_DISTANCE) {
                        mLastPointF = mCurrentPointF
                        mCurrentPointF = tempPoint
                        _animationFootPrint = true
                        invalidate()
                    }
                }
            }*/
        }
        lineValueAnimator.start()
    }

    private fun loadPointDistance() {
        _mapPointDistanceList.clear()
        var distance = 0.0
        for (index in 0 until this.linePoints?.size!! - 1) {
            val start = this.linePoints!![index]
            val end = this.linePoints!![index + 1]
            val tempDistance = distanceBetweenNodes(start, end)
            _mapPointDistanceList.add(PointDistanceFromStartPoint(start, end, distance, distance + tempDistance))
            distance += tempDistance
        }
        _allNodeDistance = distance
    }

    /**
     *
     * 获取两个点的平面距离
     */
    private fun distanceBetweenNodes(start: MapPointF, end: MapPointF): Double {
        return Math.sqrt(Math.pow(((start.x - end.x).toDouble()), 2.0) + Math.pow((start.y - end.y).toDouble(), 2.0))
    }

    private fun distanceBetweenPoints(start: RatioPointF, end: RatioPointF): Double {
        return Math.sqrt(Math.pow(((start.x - end.x).toDouble()), 2.0) + Math.pow((start.y - end.y).toDouble(), 2.0))
    }

    private class PointDistanceFromStartPoint(val startPoint: MapPointF, val endPoint: MapPointF, val prescribed: Double, val superior: Double) {

        fun findPoint(distance: Double): RatioPointF {
            val differ = distance - prescribed
            if (startPoint.x == endPoint.x) {
                return if (startPoint.y < endPoint.y) {
                    RatioPointF(startPoint.x, (startPoint.y + differ).toFloat(), 90)
                } else {
                    RatioPointF(startPoint.x, (startPoint.y - differ).toFloat(), -90)
                }
            }
            if (startPoint.y == endPoint.y) {
                return if (startPoint.x < endPoint.x) {
                    RatioPointF((startPoint.x + differ).toFloat(), startPoint.y, 0)
                } else {
                    RatioPointF((startPoint.x - differ).toFloat(), startPoint.y, 0)
                }
            }

            // 求直线斜率
            val k = (endPoint.y - startPoint.y) / (endPoint.x - startPoint.x)

            val y = when {
                endPoint.y > startPoint.y -> differ / Math.sqrt(1.0f + 1.0f / Math.pow(k.toDouble(), 2.0)) + startPoint.y
                else -> startPoint.y - differ / Math.sqrt(1.0f + 1.0f / Math.pow(k.toDouble(), 2.0))
            }
            val x = (y - startPoint.y) / k + startPoint.x
            val radio = Math.atan(k.toDouble()) / Math.PI * 180
            if (k > 0) {
                if (startPoint.y > endPoint.y) {
                    return RatioPointF(x.toFloat(), y.toFloat(), ((90 + radio + 180).toInt()))
                } else {
                    return RatioPointF(x.toFloat(), y.toFloat(), ((radio).toInt()))
                }
            } else {
                if (startPoint.y > endPoint.y) {
                    return RatioPointF(x.toFloat(), y.toFloat(), ((90 + radio).toInt()))
                } else {
                    return RatioPointF(x.toFloat(), y.toFloat(), ((radio + 90 + 180).toInt()))
                }
            }
            // return RatioPointF(x.toFloat(), y.toFloat(), radio.toInt())
        }
    }

    private data class RatioPointF(val x: Float, val y: Float, val k: Int)
}
