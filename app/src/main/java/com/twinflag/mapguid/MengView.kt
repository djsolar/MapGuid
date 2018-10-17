package com.twinflag.mapguid

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import com.twinflag.mapguid.domain.Edge
import com.twinflag.mapguid.domain.Node

class MengView(context: Context, attributes: AttributeSet) : ImageView(context, attributes) {

    var nodes: ArrayList<Node>? = null

    var edges: ArrayList<Edge>? = null

    var lineNodes: ArrayList<Node>? = null

    var linePaint: Paint? = null

    var routeLinePaint: Paint? = null

    var textPaint: Paint? = null

    var onCalLine: OnCalLine? = null

    var onClickEvent: OnClickEvent? = null

    var selectedNode: Node? = null
        set(value) {
            this.lineNodes = null
            // 如果选中的点是null，则认为是取消寻路
            if (value == null) {
                startNode = null
                endNode = null
            } else {
                // 如果开始节点是null,则将新值赋给开始节点
                if(startNode == null) {
                    startNode = value
                } else if (endNode == null) {
                    // 如果结束节点为null，则将新值赋给结束节点
                    endNode = value
                } else {
                    // 如果value是开始节点，则取消开始节点
                    // 如果value是结束节点，则取消结束节点
                    // 如果value即不是开始节点也不是结束节点，则重置结束节点
                    when (value) {
                        startNode -> startNode = null
                        endNode -> endNode = null
                        else -> endNode = value
                    }
                }
            }
            if (startNode != null && endNode != null) {
                this.onCalLine?.onCal(startNode!!, endNode!!)
            }
            this.postInvalidate()
        }

    private var startNode: Node? = null

    private var endNode: Node? = null

    init {
        linePaint = Paint()
        linePaint?.strokeWidth = context.dp2px(2)
        linePaint?.style = Paint.Style.FILL_AND_STROKE
        linePaint?.color = Color.parseColor("#008577")

        textPaint = Paint()
        textPaint?.style = Paint.Style.STROKE
        textPaint?.color = Color.parseColor("#D81B60")
        textPaint?.textSize = 12.0f

        routeLinePaint = Paint()
        routeLinePaint?.strokeWidth = context.dp2px(2)
        routeLinePaint?.style = Paint.Style.FILL_AND_STROKE
        routeLinePaint?.color = Color.parseColor("#D81B60")
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawInitView(canvas)
        guidLineDraw(canvas)

    }

    private fun guidLineDraw(canvas: Canvas?) {
        this.lineNodes?.forEach {
            canvas?.drawCircle(it.x.toFloat(), it.y.toFloat(), context.dp2px(5), routeLinePaint)
        }
        if (this.lineNodes != null && this.lineNodes!!.size > 0) {
            val size: Int = this.lineNodes!!.size
            var i = 0
            while (i < size -1) {
                val firstNode = this.lineNodes!![i]
                val secondNode = this.lineNodes!![i + 1]
                canvas?.drawLine(firstNode.x.toFloat(), firstNode.y.toFloat(), secondNode.x.toFloat(), secondNode.y.toFloat(), routeLinePaint)
                i++
            }
        }
        if (this.startNode != null) {
            canvas?.drawText("起点", this.startNode!!.x.toFloat() + context.dp2px(7), this.startNode!!.y.toFloat(), textPaint)
            canvas?.drawCircle(this.startNode!!.x.toFloat(), this.startNode!!.y.toFloat(), context.dp2px(5), routeLinePaint)
        }

        if (this.endNode != null) {
            canvas?.drawText("终点", this.endNode!!.x.toFloat() + context.dp2px(7), this.endNode!!.y.toFloat(), textPaint)
            canvas?.drawCircle(this.endNode!!.x.toFloat(), this.endNode!!.y.toFloat(), context.dp2px(5), routeLinePaint)
        }
    }

    fun drawLine(nodes: ArrayList<Node>) {
        this.lineNodes = nodes
        postInvalidate()
    }

    fun initDrawPointAndLine(nodes: ArrayList<Node>, edges: ArrayList<Edge>) {
        this.nodes = nodes
        this.edges = edges
        postInvalidate()
    }
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        onClickEvent?.onClick(event!!.x, event.y)
        return super.onTouchEvent(event)
    }

    private fun drawInitView(canvas: Canvas?) {
        this.nodes?.forEach {
            canvas?.drawCircle(it.x.toFloat(), it.y.toFloat(), context.dp2px(5), linePaint)
        }
        val nodeMap = this.nodes?.associateBy({ it.id }, {it})
        this.edges?.forEach {
            val firstNode = nodeMap!![it.firstNodeId]
            val secondNode = nodeMap[it.secondNodeId]
            canvas?.drawLine(firstNode!!.x.toFloat(), firstNode.y.toFloat(), secondNode!!.x.toFloat(), secondNode.y.toFloat(), linePaint)
        }
    }

    interface OnCalLine {
        fun onCal(startNode: Node, endNode: Node)
    }

    interface OnClickEvent {
        fun onClick(x: Float, y: Float)
    }
}