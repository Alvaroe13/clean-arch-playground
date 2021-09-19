package com.codingwithmitch.cleannotes.framework.presentation.common

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.codingwithmitch.cleannotes.business.domain.util.DateUtil
import com.codingwithmitch.cleannotes.framework.presentation.notedetail.NoteDetailFragment
import com.codingwithmitch.cleannotes.framework.presentation.notelist.NoteListFragment
import com.codingwithmitch.cleannotes.framework.presentation.splash.SplashFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class NoteFragmentFactory
@Inject
constructor(
    private val viewModelFactory: NoteViewModelFactory,
    private val dateUtil: DateUtil
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {

            NoteListFragment::class.java.name -> {
                val fragment = NoteListFragment(viewModelFactory, dateUtil)
                fragment
            }

            NoteDetailFragment::class.java.name -> {
                val fragment = NoteDetailFragment(viewModelFactory)
                fragment
            }

            SplashFragment::class.java.name -> {
                val fragment = SplashFragment(viewModelFactory)
                fragment
            }

            else -> {
                super.instantiate(classLoader, className)
            }
        }

    }
}