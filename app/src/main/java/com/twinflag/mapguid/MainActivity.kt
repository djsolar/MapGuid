package com.twinflag.mapguid

import android.content.res.XmlResourceParser
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Xml
import com.twinflag.mapguid.domain.Edge
import com.twinflag.mapguid.domain.Node
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val data = parseMapXml()
        val nodeMap:Map<Char, Node> = data.first.associateBy({it.id.toChar()}, {it})
        val graph = initGraph(data)
        mengView.initDrawPointAndLine(data.first, data.second)
        mengView.onCalLine = object: MengView.OnCalLine {
            override fun onCal(startNode: Node, endNode: Node) {
                val paths = graph.getShortestPath(startNode.id.toChar(), endNode.id.toChar())
                paths.add(startNode.id.toChar())
                val nodes = ArrayList<Node>()
                paths.reversed().forEach {
                    nodes.add(nodeMap[it]!!)
                }
                mengView.drawLine(nodes)
            }
        }
        mengView.onClickEvent = object : MengView.OnClickEvent {
            override fun onClick(x: Float, y: Float) {
                val node = findClickNode(x, y, data.first)
                mengView.selectedNode = node
            }
        }
    }

    fun findClickNode(x: Float, y: Float, nodes: ArrayList<Node>): Node? {
        nodes.forEach {
            val distance = Math.sqrt(Math.pow(it.x.toDouble() - x.toDouble(), 2.0) + Math.pow(it.y.toDouble() - y.toDouble(), 2.0))
            if (distance < px2dip(20)) {
                return it
            }
        }
        return null
    }

    private fun initGraph(data: Pair<ArrayList<Node>, ArrayList<Edge>>): Graph {
        val graph = Graph()
        data.second.forEach {
            graph.addVertex(it.firstNodeId.toChar(), Vertex(it.secondNodeId.toChar(), it.weight))
            graph.addVertex(it.secondNodeId.toChar(), Vertex(it.firstNodeId.toChar(), it.weight))
        }
        return graph
    }

    fun parseMapXml(): Pair<ArrayList<Node>, ArrayList<Edge>> {
        val inputStream = assets.open("3f.xml")
        val parser = Xml.newPullParser()
        parser.setInput(inputStream, "UTF-8")
        var eventType = parser.eventType
        val nodes = ArrayList<Node>()
        val edges = ArrayList<Edge>()
        while (eventType != XmlResourceParser.END_DOCUMENT) {
            when(eventType) {
                XmlResourceParser.START_TAG -> {
                    if (parser.name == "Node") {
                        val id = parser.getAttributeValue(null, "ID")
                        val coordinate = parser.getAttributeValue(null, "Coordinates")
                        val coordinateArray = coordinate.split(",")
                        val node = Node(id.toInt(), px2dip(coordinateArray[0].toInt()), px2dip(coordinateArray[1].toInt()))
                        nodes.add(node)
                    } else if (parser.name == "Edge") {
                        val nodeId1 = parser.getAttributeValue(null, "NodeID1")
                        val nodeId2 = parser.getAttributeValue(null, "NodeID2")
                        val weight = parser.getAttributeValue(null, "Weight")
                        val edge = Edge(nodeId1.toInt(), nodeId2.toInt(), px2dip(weight.toInt()))
                        edges.add(edge)
                    }
                }
                else -> {
                    println("")
                }
            }
            eventType = parser.next()
        }
        return Pair(nodes, edges)
    }

    fun px2dip(dp: Int): Int {
        return (resources.displayMetrics.density * dp + 0.5).toInt()
    }
}
