package com.twinflag.mapguid

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import com.twinflag.mapguid.mvp.contract.MapContract
import com.twinflag.mapguid.mvp.model.bean.Figure
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

    override fun showNavigationLine() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clearView() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showLoading() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dismissLoading() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showSettingStartNode(figures: List<Figure>) {
        figures.asReversed().forEachIndexed{
            index, figure ->
            val radioButton = createRadioButton(figure.mapName)
            radioButton.setOnClickListener { view ->
                (view as RadioButton).isChecked = true
                mPresenter.displaySelectFloor(view.text.toString())
            }
            if (index == 0) {
                radioButton.isChecked = true
            }
            floorRadioGroup.addView(radioButton)
            if (index != figures.size -1) {
                floorRadioGroup.addView(createDividerView())
            }
        }
        floorRadioGroup.visibility = View.VISIBLE
    }

    override fun showIndicateFloor(figure: Figure) {

    }

    private fun createRadioButton(radioButtonText: String): RadioButton {
        return RadioButton(this).apply {
            text = radioButtonText
            setTextColor(R.drawable.rb_color_floor)
            setBackgroundResource(R.drawable.selector_radiobutton)
            textSize = 22.0f
            layoutParams = RadioGroup.LayoutParams(dp2px(64).toInt(), dp2px(64).toInt(), 1.0f)
            gravity = Gravity.CENTER
            buttonDrawable = null
        }
    }

    private fun createDividerView(): View {
        return View(this).apply {
            layoutParams = RadioGroup.LayoutParams(dp2px(1).toInt(), dp2px(1).toInt(), 1.0f)
            setBackgroundColor(ContextCompat.getColor(this@MapActivity, android.R.color.white))
        }
    }



    override fun showError(message: String) {
        toast(message)
    }
}
