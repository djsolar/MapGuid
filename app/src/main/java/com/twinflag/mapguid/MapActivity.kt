package com.twinflag.mapguid

import android.graphics.Point
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.twinflag.mapguid.mvp.contract.MapContract
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
        mPresenter.receiveEndPoint("95")
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

    override fun showNavigationLine(points: List<Point>) {
        routeLineView.drawNavigationLine(points.toMutableList())
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

