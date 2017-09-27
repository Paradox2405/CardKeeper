package com.awscherb.cardkeeper.ui.base

import android.app.Application

import com.awscherb.cardkeeper.di.component.DaggerViewComponent
import com.awscherb.cardkeeper.di.component.ViewComponent
import com.awscherb.cardkeeper.di.module.AppModule
import com.awscherb.cardkeeper.di.module.DaoModule
import com.awscherb.cardkeeper.di.module.SchedulerModule
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric



class BaseApplication : Application() {

    lateinit var viewComponent: ViewComponent
        private set

    override fun onCreate() {
        super.onCreate()

        Fabric.with(this, Crashlytics())

        viewComponent = DaggerViewComponent.builder()
                .appModule(AppModule(this))
                .daoModule(DaoModule())
                .schedulerModule(SchedulerModule())
                .build()

    }
}
