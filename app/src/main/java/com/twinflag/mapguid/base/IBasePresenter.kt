package com.twinflag.mapguid.base

interface IBasePresenter<in V : IBaseView> {

    fun attachView(rootView: V)

    fun detachView()
}