package com.twinflag.mapguid

import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.widget.FrameLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.twinflag.mapguid.mvp.contract.SettingMapContract
import com.twinflag.mapguid.mvp.model.bean.Figure
import com.twinflag.mapguid.mvp.presenter.SettingMapPresenter
import kotlinx.android.synthetic.main.activity_setting_location.*

class SettingLocationActivity : AppCompatActivity(), SettingMapContract.View {

    private var selectMapName: String? = null


    private val mPresenter: SettingMapPresenter by lazy {
        SettingMapPresenter()
    }

    init {
        mPresenter.attachView(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_location)
    }

    override fun onResume() {
        super.onResume()
        mPresenter.start()
    }

    override fun showConfirmDialog(node: Figure.Node) {
        val builder = AlertDialog.Builder(this).apply {
            setTitle(R.string.start_node_title)
            setMessage(R.string.start_node_info)
            setNegativeButton(R.string.start_node_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton(R.string.start_node_confirm) { dialog, _ ->
                mPresenter.startNodeId = node.id
                dialog.dismiss()
            }
        }
        val dialog = builder.create()
        val params = WindowManager.LayoutParams()
        params.y = 400//设置y坐标
        dialog.window.attributes = params
        dialog.show()
    }

    override fun showIndicateFloor(figure: Figure) {
        Glide.with(this).load(figure.mapPath).listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                return false
            }

            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                if (dataSource == DataSource.MEMORY_CACHE) {
                    val alphaAnimation = AlphaAnimation(0.1f, 1.0f)
                    alphaAnimation.duration = 2000
                    displayFloorImageView!!.startAnimation(alphaAnimation)
                }
                return false
            }

        }).transition(DrawableTransitionOptions().crossFade(2000)).into(displayFloorImageView!!)
        displayFloorImageView.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val x = event.x
                val y = event.y
                mPresenter.selectStartNode(selectMapName!!, x, y)
            } else {
                false
            }
        }
    }

    override fun showSettingStartNode(figures: List<Figure>) {
        figures.asReversed().forEachIndexed { index, figure ->
            val mapDisplayName = figure.mapName.substring(0, figure.mapName.lastIndexOf('.'))
            val radioButton = createRadioButton(mapDisplayName)
            radioButton.setOnCheckedChangeListener { view, checked ->
                (view as RadioButton).isChecked = checked
                if (view.isChecked) {
                    Log.e("zz", "view.text: ${view.text}")
                    selectMapName = view.text.toString()
                    mPresenter.displaySelectFloor(view.text.toString())
                }
            }
            floorRadioGroup.addView(radioButton)
            if (index != figures.size - 1) {
                floorRadioGroup.addView(createDividerView())
            }
            if (index == figures.size - 1) {
                radioButton.isChecked = true
                selectMapName = mapDisplayName
            }
        }
        floorRadioGroup.visibility = View.VISIBLE
        mPresenter.displaySelectFloor(selectMapName!!)
    }

    override fun showLoading() {
    }

    override fun dismissLoading() {

    }

    override fun showStartNode(node: Figure.Node?) {
        Log.e("zz", "floorName: ${node?.floor}, selectMapName: $selectMapName")
        if (node != null && node.floor == selectMapName) {
            val position = IntArray(2)
            displayFloorImageView.getLocationInWindow(position)
            val layoutParams = locationIconView.layoutParams as FrameLayout.LayoutParams
            layoutParams.leftMargin = position[0] + node.locationX.toInt()
            layoutParams.topMargin = position[1] + node.locationY.toInt()
            Log.e("zz", "leftMargin: ${layoutParams.leftMargin}, topMargin: ${layoutParams.leftMargin}")
            locationIconView.layoutParams = layoutParams
            locationIconView.visibility = View.VISIBLE
        } else {
            locationIconView.visibility = View.GONE
        }
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
            setBackgroundColor(ContextCompat.getColor(this@SettingLocationActivity, android.R.color.white))
        }
    }
}
