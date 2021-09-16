package com.codingwithmitch.cleannotes.di

import com.codingwithmitch.cleannotes.framework.presentation.TempTest
import com.codingwithmitch.cleannotes.framework.presentation.TestBaseApplication
import com.google.android.datatransport.runtime.dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@FlowPreview
@ExperimentalCoroutinesApi
@Singleton
@Component(
    modules = [
        TestModule::class,
        AppModule::class
    ]
)
interface TestAppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: TestBaseApplication): TestAppComponent
    }

    fun inject(test: TempTest)

}