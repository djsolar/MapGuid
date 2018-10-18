package com.twinflag.mapguid.mvp.presenter

import android.text.TextUtils
import com.twinflag.mapguid.MyApplication
import com.twinflag.mapguid.mvp.contract.MapContract
import com.twinflag.mapguid.mvp.model.MapModel
import com.twinflag.mapguid.utils.Preference

class MapPresenter: MapContract.Presenter {

    val mapModel: MapModel by lazy {
        MapModel.mapModel
    }

    var startNodeId: String by Preference("startNodeId", "")

    private var rootView: MapContract.View? = null

    override fun start() {
        if (TextUtils.isEmpty(startNodeId)) {
            mapModel.load(MyApplication.MAP_PATH)
            rootView?.showSettingStartNode(mapModel.figures?.toList()!!)
        }
    }

    override fun receiveEndPoint() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    override fun displaySelectFloor(floorName: String) {
        val figure = mapModel[floorName]
        rootView?.showIndicateFloor(figure!!)
    }
}