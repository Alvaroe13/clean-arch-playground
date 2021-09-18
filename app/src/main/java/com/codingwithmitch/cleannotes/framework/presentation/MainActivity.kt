package com.codingwithmitch.cleannotes.framework.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.codingwithmitch.cleannotes.R
import com.codingwithmitch.cleannotes.business.domain.state.DialogInputCaptureCallback
import com.codingwithmitch.cleannotes.business.domain.state.Response
import com.codingwithmitch.cleannotes.business.domain.state.StateMessageCallback
import com.codingwithmitch.cleannotes.framework.presentation.common.NoteFragmentFactory
import com.codingwithmitch.cleannotes.util.printLogD
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class MainActivity : AppCompatActivity(), UIController {

    @Inject
    lateinit var fragmentFactory: NoteFragmentFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        setFragmentFactory()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun inject(){
        (application as BaseApplication).appComponent
            .inject(this)
    }

    private fun setFragmentFactory(){
        supportFragmentManager.fragmentFactory = fragmentFactory
    }

    override fun displayProgressBar(isDisplayed: Boolean) {
        // TODO("Not yet implemented")
    }

    override fun hideSoftKeyboard() {
        // TODO("Not yet implemented")
    }

    override fun displayInputCaptureDialog(title: String, callback: DialogInputCaptureCallback) {
        // TODO("Not yet implemented")
    }

    override fun onResponseReceived(
        response: Response,
        stateMessageCallback: StateMessageCallback
    ) {
        // TODO("Not yet implemented")
        printLogD("MainActivity", "response: ${response}")
    }

}

























