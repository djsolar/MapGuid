package com.twinflag.mapguid.mvp.model

import android.content.res.XmlResourceParser
import android.graphics.Point
import android.graphics.PointF
import android.util.Xml
import com.twinflag.mapguid.mvp.model.bean.*
import java.io.File
import java.io.FileInputStream

class MapModel private constructor() : MapModeOperate {

    override var figures: MutableList<Figure>? = null

    override var startNode: Figure.Node? = null

    override var figureMap: MutableMap<String, Figure>? = null

    override var nodeMap: MutableMap<String, Figure.Node>? = null
    override var edges: MutableList<Figure.Edge>? = null
    override var mapFolder: String? = null
    private val graph: Graph = Graph()

    companion object {

        const val DEFAULT_DIFFER = 10

        const val DEFAULT_SELECT_DIFFER = 80

        const val DEFAULT_LIFT_CATEGORY = 5

        const val LIFT_CATEGORY = 5

        const val NORMAL_CATEGORY = 0

        const val TAG = "MapModel"
        val mapModel: MapModel by lazy {
            MapModel()
        }
    }

    /**
     *
     * 重置数据
     */
    override fun reset() {
        figureMap?.clear()
        nodeMap?.clear()
        edges?.clear()
        graph.clear()
    }

    /**
     * 装载数据
     *
     */
    override fun loadData() {
        this.edges?.forEach {
            graph.addVertex(it.nodeId1, Vertex(it.nodeId2, it.weight))
            graph.addVertex(it.nodeId2, Vertex(it.nodeId1, it.weight))
        }
        this.edges?.forEach {
            println(it)
        }
    }

    /**
     * 获取连个点之间的最近距离
     */
    override fun getShortestDistance(nodeId: String): List<Figure.Node> {
        val pathNodes: List<String> = graph.getShortestPath(startNode?.id, nodeId).asReversed()
        val nodes: ArrayList<Figure.Node> = arrayListOf()
        pathNodes.forEach { nodeId ->
            nodes.add(this.nodeMap!![nodeId]!!)
        }
        return nodes.toList()
    }

    override fun getShortestLineMap(nodeId: String): MapLine {
        val nodes = getShortestDistance(nodeId)
        val map = nodes.groupBy { it.floor }
        if (map.size == 1) {
            val floor = map.keys.toList()[0]
            val figure = figureMap!![floor]
            val mapPiece = MapPiece(figure!!.mapPath, figure.mapName, figure.width, figure.height)
            val points = arrayListOf<MapPointF>()
            nodes.forEach {
                points.add(MapPointF(it.locationX, it.locationY, it.categoryId))
            }
            return MapLine(arrayListOf(mapPiece), points)
        } else {
            val keys = map.keys
            val sortKeys = keys.sorted()
            // 去除中间的电梯节点
            val firstFloorName = sortKeys.first()
            val lastFloorName = sortKeys.last()
            val firstFigure = figureMap!![firstFloorName]
            val lastFigure = figureMap!![lastFloorName]
            val newNodes = nodes.filter { it.floor == firstFloorName || it.floor == lastFloorName }
            val lastFigureHeight = lastFigure?.height
            // 对新节点的坐标进行处理，如果是第一层的坐标，则要进行向下的位移
            val finalPointF = arrayListOf<MapPointF>()
            newNodes.forEach {
                if (it.floor == firstFloorName) {
                    finalPointF.add(MapPointF(it.locationX, it.locationY + lastFigureHeight!!, it.categoryId))
                } else {
                    finalPointF.add(MapPointF(it.locationX, it.locationY, it.categoryId))
                }
            }
            val mapPieces = arrayListOf(MapPiece(lastFigure!!.mapPath, lastFigure.mapName, lastFigure.width, lastFigure.height), MapPiece(firstFigure!!.mapPath, firstFigure.mapName, firstFigure.width, firstFigure.height))
            return MapLine(mapPieces, finalPointF)
        }
    }

    /**
     * 加载地图包
     */
    override fun load(mapFolder: String) {
        if (this.mapFolder == mapFolder) {
            return
        }
        reset()
        this.mapFolder = mapFolder
        if (!parseMap())
            return
        linkFigures()
        loadData()
    }

    /**
     * 楼层之间建立联系
     * 检查每个楼层的电梯节点，如果坐标相同，则认为电梯是相通，添加电梯之间边
     * 上下两层电梯允许的误差为距离10
     */
    override fun linkFigures() {
        val figures: List<Figure>? = figureMap?.map { it.value }
        if (figures!!.size > 1) {
            for (i in 0 until (figures.size - 1)) {
                val figureFirst = figures[i]
                val figureSecond = figures[i + 1]
                // 筛选出电梯
                val firstNodes = figureFirst.nodes.filter { it.categoryId == DEFAULT_LIFT_CATEGORY }
                val secondNodes = figureSecond.nodes.filter { it.categoryId == DEFAULT_LIFT_CATEGORY }
                if (firstNodes.isEmpty() || secondNodes.isEmpty()) {
                    return
                }
                for (firstNode in firstNodes) {
                    for (secondNode in secondNodes) {
                        if (distanceBetweenNodes(firstNode, secondNode) < DEFAULT_DIFFER) {
                            figureFirst.edges.add(Figure.Edge(firstNode.id, secondNode.id, 1))
                            figureSecond.edges.add(Figure.Edge(secondNode.id, firstNode.id, 1))
                        }
                    }
                }
            }
        }
        this.edges = figures.flatMap { it.edges }.toMutableList()
        this.nodeMap = getAllFigureNodes(figures)
    }

    override operator fun get(figureName: String): Figure? {
        return this.figureMap!![figureName]
    }

    override fun getNode(nodeId: String): Figure.Node? {
        return this.nodeMap!![nodeId]
    }

    /**
     *
     * 解析数据
     */
    override fun parseMap(): Boolean {
        val mapFile = File(this.mapFolder)
        if (!mapFile.exists())
            return false
        val files = mapFile.listFiles()
        if (files == null || files.isEmpty()) {
            return false
        }
        val figures = arrayListOf<Figure>()
        files.filter { file -> file.name.endsWith(".xml") }.forEach {
            val figure = parseXmlFile(it)
            if (figure != null) {
                figures.add(figure)
            } else {
                return false
            }
        }
        this.figures = figures
        this.figureMap = figures.associateBy({ it.mapName.substring(0, it.mapName.lastIndexOf('.')) }, { it }).toMutableMap()
        return true
    }

    /**
     * 解析点位信息
     *
     */
    private fun parseXmlFile(file: File): Figure? {
        val fileName: String = file.name
        val mapPath: String = file.path.replace(".xml", ".png")
        val figureName = fileName.substring(0, fileName.lastIndexOf('.'))
        val inputStream = FileInputStream(file)
        val parser = Xml.newPullParser()
        parser.setInput(inputStream, "UTF-8")
        var eventType = parser.eventType
        var figure: Figure? = null
        val nodes: ArrayList<Figure.Node> = arrayListOf()
        val edges: ArrayList<Figure.Edge> = arrayListOf()
        while (eventType != XmlResourceParser.END_DOCUMENT) {
            when (eventType) {
                XmlResourceParser.START_TAG -> {
                    when {
                        parser.name == "Node" -> {
                            val id = parser.getAttributeValue(null, "ID")
                            val coordinate = parser.getAttributeValue(null, "Coordinates")
                            val locationId = parser.getAttributeValue(null, "LocationId").toInt()
                            val categoryId = parser.getAttributeValue(null, "CategoryId").toInt()
                            val coordinateArray = coordinate.split(",")
                            val node = Figure.Node(id + figureName, figureName, coordinateArray[0].toFloat(), coordinateArray[1].toFloat(), locationId, categoryId)
                            figure?.nodes?.add(node)
                        }
                        parser.name == "Edge" -> {
                            val nodeId1 = parser.getAttributeValue(null, "NodeID1")
                            val nodeId2 = parser.getAttributeValue(null, "NodeID2")
                            val weight = parser.getAttributeValue(null, "Weight")
                            val edge = Figure.Edge(nodeId1 + figureName, nodeId2 + figureName, weight.toInt())
                            figure?.edges?.add(edge)
                        }
                        parser.name == "Figure" -> {
                            val width = parser.getAttributeValue(null, "Width").toInt()
                            val height = parser.getAttributeValue(null, "Height").toInt()
                            val maxId = parser.getAttributeValue(null, "MaxID").toInt()
                            figure = Figure(fileName, mapPath, width, height, maxId, nodes, edges)
                        }
                    }
                }
                else -> {
                    println("")
                }
            }
            eventType = parser.next()
        }
        return figure
    }


    override fun getStartNode(figureName: String, x: Float, y: Float): Figure.Node? {
        val figure = figureMap!![figureName]
        val nodes = figure?.nodes
        var distance = Double.MAX_VALUE
        var selectNode: Figure.Node? = null
        nodes?.forEach {
            val tempDistance = distanceBetweenNodeWidthPoint(it, x, y)
            if (tempDistance <= distance) {
                distance = tempDistance
                if (distance < DEFAULT_SELECT_DIFFER) {
                    selectNode = it
                }
            }
        }
        return selectNode
    }

    /**
     * 获取所有的点的Map {"id": Node}
     */
    private fun getAllFigureNodes(figures: List<Figure>): MutableMap<String, Figure.Node> {
        return figures.flatMap { it.nodes }.associateBy({ it.id }, { it }).toMutableMap()
    }

    private fun distanceBetweenNodeWidthPoint(firstNode: Figure.Node, x: Float, y: Float): Double {
        return Math.sqrt(Math.pow(((firstNode.locationX - x).toDouble()), 2.0) + Math.pow((firstNode.locationY - y).toDouble(), 2.0))
    }

    /**
     *
     * 获取两个点的平面距离
     */
    private fun distanceBetweenNodes(firstNode: Figure.Node, secondNode: Figure.Node): Double {
        return Math.sqrt(Math.pow(((firstNode.locationX - secondNode.locationX).toDouble()), 2.0) + Math.pow((firstNode.locationY - secondNode.locationY).toDouble(), 2.0))
    }
}