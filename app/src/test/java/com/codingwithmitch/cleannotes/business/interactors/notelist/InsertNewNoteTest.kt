package com.codingwithmitch.cleannotes.business.interactors.notelist

import com.codingwithmitch.cleannotes.business.data.cache.CacheErrors
import com.codingwithmitch.cleannotes.business.data.cache.FORCE_GENERAL_FAILURE
import com.codingwithmitch.cleannotes.business.data.cache.FORCE_NEW_NOTE_EXCEPTION
import com.codingwithmitch.cleannotes.business.interactors.BaseUseCaseToolsTest
import com.codingwithmitch.cleannotes.business.interactors.notelist.InsertNewNote.Companion.INSERT_NOTE_SUCCESS
import com.codingwithmitch.cleannotes.framework.presentation.notelist.state.NoteListStateEvent
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.*

/*
Test cases:
1. insertNote_success_confirmNetworkAndCacheUpdated()
    a) insert a new note
    b) listen for INSERT_NOTE_SUCCESS emission from flow
    c) confirm cache was updated with new note
    d) confirm network was updated with new note
2. insertNote_fail_confirmNetworkAndCacheUnchanged()
    a) insert a new note
    b) force a failure (return -1 from db operation)
    c) listen for INSERT_NOTE_FAILED emission from flow
    e) confirm cache was not updated
    e) confirm network was not updated
3. throwException_checkGenericError_confirmNetworkAndCacheUnchanged()
    a) insert a new note
    b) force an exception
    c) listen for CACHE_ERROR_UNKNOWN emission from flow
    e) confirm cache was not updated
    e) confirm network was not updated
 */

@InternalCoroutinesApi
class InsertNewNoteTest : BaseUseCaseToolsTest() {

    // system in test ( the one to be tested )
    private lateinit var insertNewNote: InsertNewNote


    init {
        initSystemInTest()

    }

    override fun initSystemInTest(){
        insertNewNote = InsertNewNote(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteRemoteDataSource,
            noteFactory = noteFactory
        )
    }

    @Test
    fun insertNote_success_confirmNetworkAndCacheUpdated() = runBlocking {

        val newNote = noteFactory.createSingleNote(
            id = UUID.randomUUID().toString(),
            title = UUID.randomUUID().toString()
        )

        insertNewNote.insertNewNote(
            id = newNote.id,
            title = newNote.title,
            stateEvent = NoteListStateEvent.InsertNewNoteEvent(newNote.title, newNote.body)
        ).collect {
            assertEquals(
                it?.stateMessage?.response?.message,
                INSERT_NOTE_SUCCESS
            )
        }

        //confirm network was updated
        val noteInsertedInNetwork = noteRemoteDataSource.searchNote(newNote)
        assertTrue { noteInsertedInNetwork == newNote }

        //confirm cache ws updated
        val cacheNoteThatWasInserted = noteCacheDataSource.searchNoteById(newNote.id)
        assertTrue { cacheNoteThatWasInserted == newNote }

    }

    @InternalCoroutinesApi
    @Test
    fun insertNote_fail_confirmNetworkAndCacheUnchanged() = runBlocking {

        val newNote = noteFactory.createSingleNote(
            id = FORCE_GENERAL_FAILURE,
            title = UUID.randomUUID().toString()
        )

        insertNewNote.insertNewNote(
            id = newNote.id,
            title = newNote.title,
            stateEvent = NoteListStateEvent.InsertNewNoteEvent(newNote.title, newNote.body)
        ).collect {
            assertEquals(
                it?.stateMessage?.response?.message,
                InsertNewNote.INSERT_NOTE_FAILED
            )
        }

        // confirm network was not changed
        val networkNoteThatWasInserted = noteRemoteDataSource.searchNote(newNote)
        assertTrue { networkNoteThatWasInserted == null }

        // confirm cache was not changed
        val cacheNoteThatWasInserted = noteCacheDataSource.searchNoteById(newNote.id)
        assertTrue { cacheNoteThatWasInserted == null }

    }


    @Test
    fun throwException_checkGenericError_confirmNetworkAndCacheUnchanged() = runBlocking {

        val newNote = noteFactory.createSingleNote(
            id = FORCE_NEW_NOTE_EXCEPTION,
            title = UUID.randomUUID().toString()
        )

        insertNewNote.insertNewNote(
            id = newNote.id,
            title = newNote.title,
            stateEvent = NoteListStateEvent.InsertNewNoteEvent(newNote.title, newNote.body)
        ).collect {
            assert(
                it?.stateMessage?.response?.message
                    ?.contains(CacheErrors.CACHE_ERROR_UNKNOWN) ?: false
            )
        }

        // confirm network was not changed
        val networkNoteThatWasInserted = noteRemoteDataSource.searchNote(newNote)
        assertTrue { networkNoteThatWasInserted == null }

        // confirm cache was not changed
        val cacheNoteThatWasInserted = noteCacheDataSource.searchNoteById(newNote.id)
        assertTrue { cacheNoteThatWasInserted == null }
    }


}