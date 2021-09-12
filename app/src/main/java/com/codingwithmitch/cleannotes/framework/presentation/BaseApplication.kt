package com.codingwithmitch.cleannotes.framework.presentation

import android.app.Application
import com.codingwithmitch.cleannotes.business.di.AppComponent
import com.codingwithmitch.cleannotes.business.di.DaggerAppComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
open class BaseApplication : Application(){

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        initAppCOmponent()
    }


    open fun initAppCOmponent(){
        appComponent = DaggerAppComponent.factory().create( this )
    }
}