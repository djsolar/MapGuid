package com.twinflag.mapguid.mvp.contract

import com.twinflag.mapguid.base.IBasePresenter
import com.twinflag.mapguid.base.IBaseView
import com.twinflag.mapguid.mvp.model.bean.Figure

interface MapContract {

    interface View : IBaseView {

        fun showSettingStartNode(figures: List<Figure>)

        fun showIndicateFloor(figure: Figure)

        /**
         * 播放视频
         */
        fun showVideo()

        /**
         * 播放图片
         */
        fun showImage()

        /**
         * 播放网页
         */
        fun showWeb()

        /**
         * 显示行进路线
         */
        fun showNavigationLine()

        /**
         * 清除
         *
         */
        fun clearView()

        fun showError(message: String)

    }

    interface Presenter : IBasePresenter<View> {

        fun start()

        fun receiveEndPoint()

        fun playMaterial()

        fun displaySelectFloor(floorName: String)
    }
}