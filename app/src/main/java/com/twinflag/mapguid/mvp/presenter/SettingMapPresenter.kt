package com.twinflag.mapguid.mvp.presenter

import com.twinflag.mapguid.MyApplication
import com.twinflag.mapguid.mvp.contract.SettingMapContract
import com.twinflag.mapguid.mvp.model.MapModel
import com.twinflag.mapguid.utils.Preference

class SettingMapPresenter: SettingMapContract.Presenter {

    val mapModel: MapModel by lazy {
        MapModel.mapModel
    }

    var startNodeId: String by Preference("startNodeId", "")

    private var rootView: SettingMapContract.View? = null

    override fun start() {
        mapModel.load(MyApplication.MAP_PATH)
        rootView?.showSettingStartNode(mapModel.figures?.toList()!!)
    }

    override fun displaySelectFloor(floorName: String) {
        val figure = mapModel[floorName]
        rootView?.showIndicateFloor(figure!!)
        rootView?.showStartNode(mapModel.getNode(startNodeId))
    }

    override fun selectStartNode(mapName: String, x: Float, y: Float): Boolean {
        val startNode = mapModel.getStartNode(mapName, x, y)
        if (startNode != null) {
            rootView?.showConfirmDialog(startNode)
            rootView?.showStartNode(startNode)
            return true
        }
        return false
    }

    override fun attachView(rootView: SettingMapContract.View) {
        this.rootView = rootView
    }

    override fun detachView() {
    }

    override fun showStartNode() {
        rootView?.showStartNode(mapModel.getNode(startNodeId))
    }
}