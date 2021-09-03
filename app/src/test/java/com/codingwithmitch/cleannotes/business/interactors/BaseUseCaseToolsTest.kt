package com.codingwithmitch.cleannotes.business.interactors

import com.codingwithmitch.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.codingwithmitch.cleannotes.business.data.remote.abstraction.NoteRemoteDataSource
import com.codingwithmitch.cleannotes.business.di.DependencyContainer
import com.codingwithmitch.cleannotes.business.domain.model.NoteFactory

abstract class BaseUseCaseToolsTest {

    //dependencies
    private val dependencyContainer: DependencyContainer
    protected val noteCacheDataSource: NoteCacheDataSource
    protected val noteRemoteDataSource: NoteRemoteDataSource
    protected val noteFactory: NoteFactory

    init {
        dependencyContainer = DependencyContainer().build()
        noteCacheDataSource = dependencyContainer.noteCacheDataSource
        noteRemoteDataSource = dependencyContainer.noteRemoteDataSource
        noteFactory = dependencyContainer.noteFactory
    }

    abstract fun initSystemInTest()

}