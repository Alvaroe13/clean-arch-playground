package com.codingwithmitch.cleannotes.business.di

import com.codingwithmitch.cleannotes.business.data.NoteDataFactory
import com.codingwithmitch.cleannotes.business.data.cache.FakeNoteCacheDataSourceImpl
import com.codingwithmitch.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.codingwithmitch.cleannotes.business.data.remote.FakeNoteNetworkDataSourceImpl
import com.codingwithmitch.cleannotes.business.data.remote.abstraction.NoteRemoteDataSource
import com.codingwithmitch.cleannotes.business.domain.model.Note
import com.codingwithmitch.cleannotes.business.domain.model.NoteFactory
import com.codingwithmitch.cleannotes.business.domain.util.DateUtil
import com.codingwithmitch.cleannotes.util.isUnitTest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

/**
 * here we do manual dependency injection since is for testing only
 */
class DependencyContainer {

    companion object {
        private const val DATE_FORMAT: String = "yyyy-MM-dd hh:mm:ss a"
    }

    private val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH)
    val dateUitl = DateUtil(dateFormat)
    lateinit var noteRemoteDataSource: NoteRemoteDataSource
    lateinit var noteCacheDataSource: NoteCacheDataSource
    lateinit var noteFactory: NoteFactory
    lateinit var noteDataFactory: NoteDataFactory

    init {
        isUnitTest = true // for logger.kt
    }

    // data sets
    lateinit var notesData: HashMap<String, Note>

    fun build() {
        fakeNoteListInstance()
        noteFactoryInstance()
        remoteDatyaSourceInstance()
        cacheDataSourceInstance()
    }

    private fun noteFactoryInstance() {
        noteFactory = NoteFactory(dateUitl)
    }

    private fun remoteDatyaSourceInstance() {
        noteRemoteDataSource = FakeNoteNetworkDataSourceImpl(
            notesData = notesData,
            deletedNotesData = HashMap()
        )
    }

    private fun cacheDataSourceInstance() {
        noteCacheDataSource = FakeNoteCacheDataSourceImpl(
            notesData = notesData,
            dateUtil = dateUitl
        )
    }

    private fun fakeNoteListInstance() {
        // data sets
        this.javaClass.classLoader?.let {
            noteDataFactory = NoteDataFactory(it)

            // fake data set
            notesData = noteDataFactory.produceHashMapOfNotes(
                noteDataFactory.produceListOfNotes()
            )
        }
    }
}