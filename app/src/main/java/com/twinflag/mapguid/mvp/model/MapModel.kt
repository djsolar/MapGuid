package com.twinflag.mapguid.mvp.model

import android.content.res.XmlResourceParser
import android.util.Xml
import com.twinflag.mapguid.mvp.model.bean.Figure
import com.twinflag.mapguid.mvp.model.bean.Graph
import com.twinflag.mapguid.mvp.model.bean.Vertex
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

        const val DEFAULT_LIFT_CATEGORY = 5

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
    }

    /**
     * 获取连个点之间的最近距离
     */
    override fun getShortestDistance(nodeId: String): List<Figure.Node> {
        val pathNodes: List<String> = graph.getShortestPath(startNode?.id, nodeId)
        return pathNodes.asReversed().map { this.nodeMap!![nodeId]!! }
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
        if(!parseMap())
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
                val secondNodes = figureFirst.nodes.filter { it.categoryId == DEFAULT_LIFT_CATEGORY }
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
        this.figureMap = figures.associateBy({it.mapName.substring(0, it.mapName.lastIndexOf('.'))}, {it}).toMutableMap()
        return true
    }

    /**
     * 解析点位信息
     *
     */
    private fun parseXmlFile(file: File): Figure? {
        val fileName: String = file.name
        val mapPath: String = file.path.replace(".xml", ".png")
        val inputStream = FileInputStream(file)
        val parser = Xml.newPullParser()
        parser.setInput(inputStream, "UTF-8")
        var eventType = parser.eventType
        var figure: Figure? = null
        val nodes: ArrayList<Figure.Node> = arrayListOf()
        val edges: ArrayList<Figure.Edge> = arrayListOf()
        while (eventType != XmlResourceParser.END_DOCUMENT) {
            when(eventType) {
                XmlResourceParser.START_TAG -> {
                    when {
                        parser.name == "Node" -> {
                            val id = parser.getAttributeValue(null, "ID")
                            val coordinate = parser.getAttributeValue(null, "Coordinates")
                            val locationId = parser.getAttributeValue(null, "LocationId").toInt()
                            val categoryId = parser.getAttributeValue(null, "CategoryId").toInt()
                            val coordinateArray = coordinate.split(",")
                            val node = Figure.Node(id, fileName, coordinateArray[0].toFloat(), coordinateArray[1].toFloat(), locationId, categoryId)
                            figure?.nodes?.add(node)
                        }
                        parser.name == "Edge" -> {
                            val nodeId1 = parser.getAttributeValue(null, "NodeID1")
                            val nodeId2 = parser.getAttributeValue(null, "NodeID2")
                            val weight = parser.getAttributeValue(null, "Weight")
                            val edge = Figure.Edge(nodeId1, nodeId2, weight.toInt())
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

    /**
     * 获取所有的点的Map {"id": Node}
     */
    private fun getAllFigureNodes(figures: List<Figure>): MutableMap<String, Figure.Node> {
        return figures.flatMap { it.nodes }.associateBy({ it.id }, { it }).toMutableMap()
    }

    /**
     *
     * 获取两个点的平面距离
     */
    private fun distanceBetweenNodes(firstNode: Figure.Node, secondNode: Figure.Node): Double {
        return Math.sqrt(Math.pow(((firstNode.locationX - secondNode.locationX).toDouble()), 2.0) + Math.pow((firstNode.locationX - secondNode.locationX).toDouble(), 2.0))
    }
}