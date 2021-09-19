package com.codingwithmitch.cleannotes.business.interactors.common

import com.codingwithmitch.cleannotes.business.data.cache.CacheErrors.CACHE_ERROR_UNKNOWN
import com.codingwithmitch.cleannotes.business.data.cache.FORCE_DELETE_NOTE_EXCEPTION
import com.codingwithmitch.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.codingwithmitch.cleannotes.business.data.remote.abstraction.NoteRemoteDataSource
import com.codingwithmitch.cleannotes.business.di.DependencyContainer
import com.codingwithmitch.cleannotes.business.domain.model.NoteFactory
import com.codingwithmitch.cleannotes.business.domain.state.DataState
import com.codingwithmitch.cleannotes.business.interactors.BaseUseCaseToolsTest
import com.codingwithmitch.cleannotes.business.interactors.common.DeleteNote.Companion.DELETE_NOTE_FAILED
import com.codingwithmitch.cleannotes.business.interactors.common.DeleteNote.Companion.DELETE_NOTE_SUCCESS
import com.codingwithmitch.cleannotes.framework.presentation.notelist.state.NoteListStateEvent
import com.codingwithmitch.cleannotes.framework.presentation.notelist.state.NoteListViewState
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/*
Test cases:
1. deleteNote_success_confirmNetworkUpdated()
    a) delete a note
    b) check for success message from flow emission
    c) confirm note was deleted from "notes" node in network
    d) confirm note was added to "deletes" node in network
2. deleteNote_fail_confirmNetworkUnchanged()
    a) attempt to delete a note, fail since does not exist
    b) check for failure message from flow emission
    c) confirm network was not changed
3. throwException_checkGenericError_confirmNetworkUnchanged()
    a) attempt to delete a note, force an exception to throw
    b) check for failure message from flow emission
    c) confirm network was not changed
 */
@InternalCoroutinesApi
class DeleteNoteTest :BaseUseCaseToolsTest(){

    // system in test
    private lateinit var deleteNotes: DeleteNote<NoteListViewState>


    init {
        initSystemInTest()
    }

    override fun initSystemInTest() {
        deleteNotes = DeleteNote(
            noteCacheDataSource = noteCacheDataSource,
            noteRemoteDataSource = noteRemoteDataSource
        )
    }

    @Test
    fun deleteNote_success_confirmNetworkUpdated() = runBlocking {

        // choose a note at random to delete
        // select a random note to update
        val noteToDelete = noteCacheDataSource
            .searchNotes("", "", 1).get(0)

        deleteNotes.deleteNote(
            noteToDelete,
            NoteListStateEvent.DeleteNoteEvent(noteToDelete)
        ).collect(object: FlowCollector<DataState<NoteListViewState>?> {
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    DELETE_NOTE_SUCCESS
                )
            }
        })

        // confirm was deleted from "notes" node
        val wasNoteDeleted = !noteRemoteDataSource.getAllNotes()
            .contains(noteToDelete)
        assertTrue { wasNoteDeleted }

        // confirm was inserted into "deletes" node
        val wasDeletedNoteInserted = noteRemoteDataSource.getDeletedNotes()
            .contains(noteToDelete)
        assertTrue { wasDeletedNoteInserted }
    }

    @Test
    fun deleteNote_fail_confirmNetworkUnchanged() =  runBlocking{

        // create a note to delete that doesn't exist in data set
        val noteToDelete = noteFactory.createSingleNote("","","")


        deleteNotes.deleteNote(
            noteToDelete,
            NoteListStateEvent.DeleteNoteEvent(noteToDelete)
        ).collect(object: FlowCollector<DataState<NoteListViewState>?>{
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    DELETE_NOTE_FAILED
                )
            }
        })

        // confirm nothing was deleted from "notes" node
        val notes = noteRemoteDataSource.getAllNotes()
        val numNotesInCache = noteCacheDataSource.getNumNotes()
        assertTrue { numNotesInCache == notes.size}

        // confirm was NOT inserted into "deletes" node
        val wasDeletedNoteInserted = !noteRemoteDataSource.getDeletedNotes()
            .contains(noteToDelete)
        assertTrue { wasDeletedNoteInserted }
    }

    @Test
    fun throwException_checkGenericError_confirmNetworkUnchanged() = runBlocking{

        // create a note to delete that will throw exception
        val noteToDelete = noteFactory.createSingleNote(
            id = FORCE_DELETE_NOTE_EXCEPTION,
            title ="",
            body = ""
        )

        deleteNotes.deleteNote(
            noteToDelete,
            NoteListStateEvent.DeleteNoteEvent(noteToDelete)
        ).collect(object: FlowCollector<DataState<NoteListViewState>?>{
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assert(
                    value?.stateMessage?.response?.message
                        ?.contains(CACHE_ERROR_UNKNOWN) ?: false
                )
            }
        })

        // confirm nothing was deleted from "notes" node
        val notes = noteRemoteDataSource.getAllNotes()
        val numNotesInCache = noteCacheDataSource.getNumNotes()
        assertTrue { numNotesInCache == notes.size}

        // confirm was NOT inserted into "deletes" node
        val wasDeletedNoteInserted = !noteRemoteDataSource.getDeletedNotes()
            .contains(noteToDelete)
        assertTrue { wasDeletedNoteInserted }
    }

}