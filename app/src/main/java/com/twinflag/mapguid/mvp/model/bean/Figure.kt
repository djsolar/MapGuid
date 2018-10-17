package com.twinflag.mapguid.mvp.model.bean

data class Figure(val width: Int, val height: Int, val maxId: Int, val nodes: ArrayList<Node>, val edges: ArrayList<Edge>) {
    data class Node(val id: String, val floor: String, val locationX: Float, val locationY: Float, val locationId: Int, val categoryId: Int, val points: Int = 0)
    data class Edge(val nodeId1: String, val nodeId2: String, val weight: Int)
}