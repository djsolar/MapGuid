package com.twinflag.mapguid.mvp.model

import com.twinflag.mapguid.mvp.model.bean.Figure

interface MapModeOperate {

    var figureMap: MutableMap<String, Figure>?

    var figures: MutableList<Figure>?

    var nodeMap: MutableMap<String, Figure.Node>?

    var edges: MutableList<Figure.Edge>?

    var mapFolder: String?

    var startNode: Figure.Node?

    fun reset()

    fun load(mapFolder: String)

    fun parseMap(): Boolean

    fun linkFigures()

    fun loadData()

    fun get(figureName: String): Figure?

    fun getNode(nodeId: String): Figure.Node?

    fun getShortestDistance(nodeId: String): List<Figure.Node>
}