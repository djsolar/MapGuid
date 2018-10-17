package com.twinflag.mapguid.mvp.model

import com.twinflag.mapguid.mvp.model.bean.Figure

class MapModel private constructor(){

    private val mutableMap: MutableMap<String, Figure> = mutableMapOf()

    companion object {

        const val DEFAULT_DIFFER = 10

        const val TAG = "MapModel"
        val mapModel: MapModel by lazy {
            MapModel()
        }
    }

    /**
     * 解析楼层图
     */
    fun parseMap(): List<Figure>? {
        return null
    }

    /**
     * 楼层之间建立联系
     * 检查每个楼层的电梯节点，如果坐标相同，则认为电梯是相通，添加电梯之间边
     * 上下两层电梯允许的误差为距离10
     */
    fun linkLiftNode(figures: List<Figure>) {
        if (figures.size > 1) {
            for(i in 0 until (figures.size - 1)) {
                val figureFirst = figures[i]
                val figureSecond = figures[i + 1]
                // 筛选出电梯
                val firstNodes = figureFirst.nodes.filter { it.categoryId == 5 }
                val secondNodes = figureFirst.nodes.filter { it.categoryId == 5 }
                if (firstNodes.isEmpty() || secondNodes.isEmpty()) {
                    return
                }
                for(firstNode in firstNodes) {
                    for(secondNode in secondNodes) {
                        if (distanceBetweenNodes(firstNode, secondNode) < DEFAULT_DIFFER) {
                            figureFirst.edges.add(Figure.Edge(firstNode.id, secondNode.id, 1))
                            figureSecond.edges.add(Figure.Edge(secondNode.id, firstNode.id, 1))
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取所有的点的Map {"id": Node}
     */
    fun getAllFigureNodes(figures: List<Figure>): Map<String, Figure.Node> {
        return figures.flatMap { it.nodes }.associateBy({it.id}, {it})
    }

    fun getAllFigureEdges(figures: List<Figure>): List<Figure.Edge> {
        return figures.flatMap { it.edges }
    }

    private fun distanceBetweenNodes(firstNode: Figure.Node, secondNode: Figure.Node): Double {
        return Math.sqrt(Math.pow(((firstNode.locationX - secondNode.locationX).toDouble()), 2.0) + Math.pow((firstNode.locationX - secondNode.locationX).toDouble(), 2.0))
    }
}