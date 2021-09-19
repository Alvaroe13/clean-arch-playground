package com.codingwithmitch.cleannotes.framework.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Singleton

class SplashViewModel
constructor(
    private val noteNetworkSyncManager: NoteNetworkSyncManager
): ViewModel(){

    init {
        syncCacheWithNetwork()
    }

    fun hasSyncBeenExecuted() = noteNetworkSyncManager.hasSyncBeenExecuted

    private fun syncCacheWithNetwork() {
        noteNetworkSyncManager.executeDataSync(viewModelScope)
    }

}