package com.yesvoters.android.ui.base


interface BaseInterface {
    fun initView()
    fun setClickListeners()
    fun progressBarHideShow(isVisible: Boolean)
    fun setObservers()
}
