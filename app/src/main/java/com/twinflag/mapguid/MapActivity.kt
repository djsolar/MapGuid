package com.twinflag.mapguid

import android.graphics.PointF
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.twinflag.mapguid.mvp.contract.MapContract
import com.twinflag.mapguid.mvp.model.bean.MapLine
import com.twinflag.mapguid.mvp.model.bean.MapPiece
import com.twinflag.mapguid.mvp.presenter.MapPresenter
import kotlinx.android.synthetic.main.activity_map.*


class MapActivity : AppCompatActivity(), MapContract.View {

    private val mPresenter: MapPresenter by lazy {
        MapPresenter()
    }

    init {
        mPresenter.attachView(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
    }

    override fun onResume() {
        super.onResume()
        mPresenter.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.detachView()
    }

    override fun showVideo() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showImage() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showWeb() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showNavigationLine(mapLine: MapLine) {
        createImageView(mapLine)
        routeLineView.drawNavigationLine(mapLine.points)
    }

    private fun createImageView(mapLine: MapLine): Array<ImageView?> {

        val images = Array<ImageView?>(2) {null}
        val mapPiece = mapLine.mapPieces
        // 地图的总高度
        val height = mapPiece.sumBy { it.height }
        val width = mapPiece[0].width
        val dm = resources.displayMetrics
        val dw = dm.widthPixels
        val dh = dm.heightPixels
        var top = (dh - height) / 2
        val routLineTop = top
        val left = (dw - width) / 2
        if (mapPiece.size > 0) {
            val mp = mapPiece[0]
            val upperFloorImageView = ImageView(this).apply {
                val params = FrameLayout.LayoutParams(mp.width, mp.height)
                params.leftMargin = left
                params.topMargin = top
                layoutParams = params
                scaleType = ImageView.ScaleType.FIT_XY
            }
            top += mp.height
            frameLayout.addView(upperFloorImageView, 0)
            images[0] = upperFloorImageView
            Glide.with(this).load(mp.mapPath).into(upperFloorImageView)
        }

        if (mapPiece.size > 1) {
            val mp = mapPiece[1]
            val lowerFloorImageView = ImageView(this).apply {
                val params = FrameLayout.LayoutParams(mp.width, mp.height)
                params.leftMargin = left
                params.topMargin = top
                layoutParams = params
                scaleType = ImageView.ScaleType.FIT_XY
            }
            frameLayout.addView(lowerFloorImageView, 0)
            images[1] = lowerFloorImageView
            Glide.with(this).load(mp.mapPath).into(lowerFloorImageView)
        }
        val params = FrameLayout.LayoutParams(width, height)
        params.leftMargin = left
        params.topMargin = routLineTop
        routeLineView.layoutParams = params
        return images
    }


    override fun clearView() {

    }

    override fun showLoading() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dismissLoading() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showError(message: String) {
        toast(message)
    }
}


