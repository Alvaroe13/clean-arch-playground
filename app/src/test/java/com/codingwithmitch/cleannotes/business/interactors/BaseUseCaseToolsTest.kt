package com.codingwithmitch.cleannotes.business.interactors

import com.codingwithmitch.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.codingwithmitch.cleannotes.business.data.remote.abstraction.NoteRemoteDataSource
import com.codingwithmitch.cleannotes.business.di.DependencyContainer
import com.codingwithmitch.cleannotes.business.domain.model.NoteFactory
import com.codingwithmitch.cleannotes.business.domain.util.DateUtil
import java.text.SimpleDateFormat
import java.util.*

abstract class BaseUseCaseToolsTest {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)

    //dependencies
    private val dependencyContainer: DependencyContainer
    protected val noteCacheDataSource: NoteCacheDataSource
    protected val noteRemoteDataSource: NoteRemoteDataSource
    protected val noteFactory: NoteFactory
    val dateUtil = DateUtil(dateFormat)

    init {
        dependencyContainer = DependencyContainer().build()
        noteCacheDataSource = dependencyContainer.noteCacheDataSource
        noteRemoteDataSource = dependencyContainer.noteRemoteDataSource
        noteFactory = dependencyContainer.noteFactory
    }

    abstract fun initSystemInTest()

}