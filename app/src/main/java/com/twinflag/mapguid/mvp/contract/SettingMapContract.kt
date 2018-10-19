package com.twinflag.mapguid.mvp.contract

import com.twinflag.mapguid.base.IBasePresenter
import com.twinflag.mapguid.base.IBaseView
import com.twinflag.mapguid.mvp.model.bean.Figure

interface SettingMapContract {

    interface View : IBaseView {
        /**
         * 确认是否确定当前点为起始点
         *
         */
        fun showConfirmDialog(node: Figure.Node)

        /**
         * 显示设置界面
         *
         */
        fun showSettingStartNode(figures: List<Figure>)


        fun showIndicateFloor(figure: Figure)

        fun showStartNode(node: Figure.Node?)
    }

    interface Presenter : IBasePresenter<View> {

        fun start()

        fun displaySelectFloor(floorName: String)

        fun selectStartNode(mapName: String, x: Float, y: Float): Boolean

        fun showStartNode()
    }
}