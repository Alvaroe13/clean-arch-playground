package com.codingwithmitch.cleannotes.business.interactors.notelist

import com.codingwithmitch.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.codingwithmitch.cleannotes.business.data.remote.abstraction.NoteRemoteDataSource
import com.codingwithmitch.cleannotes.business.di.DependencyContainer
import com.codingwithmitch.cleannotes.business.domain.model.NoteFactory

class InsertNewNoteTest {

    // system in test ( the one to be tested )
    private lateinit var insertNewNote: InsertNewNote

    //dependencies
    private val dependencyContainer: DependencyContainer = DependencyContainer()
    private val noteCacheDataSource: NoteCacheDataSource
    private val noteRemoteDataSource: NoteRemoteDataSource
    private val noteFactory: NoteFactory

    init{
        dependencyContainer.build()
        noteCacheDataSource = dependencyContainer.noteCacheDataSource
        noteRemoteDataSource = dependencyContainer.noteRemoteDataSource
        noteFactory = dependencyContainer.noteFactory


        insertNewNote = InsertNewNote(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteRemoteDataSource,
            noteFactory = noteFactory
        )
    }

}