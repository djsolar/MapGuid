package com.twinflag.mapguid.mvp.contract

import android.graphics.Point
import com.twinflag.mapguid.base.IBasePresenter
import com.twinflag.mapguid.base.IBaseView
import com.twinflag.mapguid.mvp.model.bean.Figure

interface MapContract {

    interface View : IBaseView {

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
        fun showNavigationLine(points: List<Point>)

        /**
         * 清除
         *
         */
        fun clearView()

        fun showError(message: String)
    }

    interface Presenter : IBasePresenter<View> {

        fun start()

        fun receiveEndPoint(endNodeId: String)

        fun playMaterial()

    }
}