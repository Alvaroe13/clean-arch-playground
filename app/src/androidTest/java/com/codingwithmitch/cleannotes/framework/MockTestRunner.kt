package com.codingwithmitch.cleannotes.framework

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.codingwithmitch.cleannotes.framework.presentation.TestBaseApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

/**  This will set as BaseApplication the one we created for testing (TestBaseApplication)  for
 * the JUnit runner  when we run tests, without this step, the junit runner would use the one in
 * the Manifest (AKA BAseApplication)
 * */
@FlowPreview
@ExperimentalCoroutinesApi
class MockTestRunner : AndroidJUnitRunner(){

    /** Here we by-pass the BaseApplication for the one we created for testing**/
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ) : Application = super.newApplication(cl, TestBaseApplication::class.java.name, context)

}