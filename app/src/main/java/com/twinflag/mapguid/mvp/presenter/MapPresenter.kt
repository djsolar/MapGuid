package com.twinflag.mapguid.mvp.presenter

import android.text.TextUtils
import android.util.Log
import com.twinflag.mapguid.MyApplication
import com.twinflag.mapguid.mvp.contract.MapContract
import com.twinflag.mapguid.mvp.model.MapModel

class MapPresenter: MapContract.Presenter {
    val mapModel: MapModel by lazy {
        MapModel.mapModel
    }
    var startNodeId: String = "1261F" //by Preference("startNodeId", "")

    private var rootView: MapContract.View? = null

    override fun start() {
        if (TextUtils.isEmpty(startNodeId)) {
            rootView?.showError("未设置当前设备位置")
        } else {
            mapModel.load(MyApplication.MAP_PATH)
            if (mapModel.getNode(startNodeId) == null) {
                rootView?.showError("当前设备位置错误")
            } else {
                mapModel.startNode = mapModel.getNode(startNodeId)
            }
        }
        receiveEndPoint("43F")

    }

    override fun receiveEndPoint(endNodeId: String) {
        val mapLine = mapModel.getShortestLineMap(endNodeId)
        Log.e("zz", mapLine.toString())
        rootView?.showNavigationLine(mapLine)
    }

    override fun playMaterial() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun attachView(rootView: MapContract.View) {
        this.rootView = rootView
    }

    override fun detachView() {
        rootView?.clearView()
    }
}