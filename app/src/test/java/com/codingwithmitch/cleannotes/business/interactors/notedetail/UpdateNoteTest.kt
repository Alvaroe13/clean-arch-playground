package com.codingwithmitch.cleannotes.business.interactors.notedetail


import com.codingwithmitch.cleannotes.business.data.cache.CacheErrors
import com.codingwithmitch.cleannotes.business.data.cache.FORCE_UPDATE_NOTE_EXCEPTION
import com.codingwithmitch.cleannotes.business.domain.model.Note
import com.codingwithmitch.cleannotes.business.domain.state.DataState
import com.codingwithmitch.cleannotes.business.interactors.BaseUseCaseToolsTest
import com.codingwithmitch.cleannotes.business.interactors.notedetail.UpdateNote.Companion.UPDATE_NOTE_SUCCESS
import com.codingwithmitch.cleannotes.framework.presentation.notedetail.state.NoteDetailStateEvent
import com.codingwithmitch.cleannotes.framework.presentation.notedetail.state.NoteDetailViewState
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.*

/*
Test cases:
1. updateNote_success_confirmNetworkAndCacheUpdated()
    a) select a random note from the cache
    b) update that note
    c) confirm UPDATE_NOTE_SUCCESS msg is emitted from flow
    d) confirm note is updated in network
    e) confirm note is updated in cache
2. updateNote_fail_confirmNetworkAndCacheUnchanged()
    a) attempt to update a note, fail since does not exist
    b) check for failure message from flow emission
    c) confirm nothing was updated in the cache
3. throwException_checkGenericError_confirmNetworkAndCacheUnchanged()
    a) attempt to update a note, force an exception to throw
    b) check for failure message from flow emission
    c) confirm nothing was updated in the cache
 */
@InternalCoroutinesApi
class UpdateNoteTest : BaseUseCaseToolsTest() {

    //system in test
    private lateinit var updateNote: UpdateNote

    init {
        initSystemInTest()
    }

    override fun initSystemInTest() {
        updateNote = UpdateNote(
            noteCacheDataSource = noteCacheDataSource,
            noteRemoteDataSource = noteRemoteDataSource
        )
    }

    @Test
    fun updateNote_success_confirmNetworkAndCacheUpdated() = runBlocking {

        val randomNote = noteCacheDataSource.searchNotes("", "", 1)
            .get(0)
        val updatedNote = noteFactory.createSingleNote(
            id = randomNote.id,
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString()
        )
        updateNote.updateNote(
            note = updatedNote,
            stateEvent = NoteDetailStateEvent.UpdateNoteEvent()
        ).collect(object : FlowCollector<DataState<NoteDetailViewState>?> {
            override suspend fun emit(value: DataState<NoteDetailViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    UPDATE_NOTE_SUCCESS
                )
            }
        })

        // confirm cache was updated
        val cacheNote = noteCacheDataSource.searchNoteById(updatedNote.id)
        assertTrue { cacheNote == updatedNote }

        // confirm that network was updated
        val networkNote = noteRemoteDataSource.searchNote(updatedNote)
        assertTrue { networkNote == updatedNote }
    }

    @Test
    fun updateNote_fail_confirmNetworkAndCacheUnchanged() = runBlocking {

        // create a note that doesnt exist in cache
        val noteToUpdate = Note(
            id = UUID.randomUUID().toString(),
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString(),
            updated_at = UUID.randomUUID().toString(),
            created_at = UUID.randomUUID().toString()
        )
        updateNote.updateNote(
            note = noteToUpdate,
            stateEvent = NoteDetailStateEvent.UpdateNoteEvent()
        ).collect(object : FlowCollector<DataState<NoteDetailViewState>?> {
            override suspend fun emit(value: DataState<NoteDetailViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    UpdateNote.UPDATE_NOTE_FAILED
                )
            }
        })

        // confirm nothing updated in cache
        val cacheNote = noteCacheDataSource.searchNoteById(noteToUpdate.id)
        assertTrue { cacheNote == null }

        // confirm nothing updated in network
        val networkNote = noteRemoteDataSource.searchNote(noteToUpdate)
        assertTrue { networkNote == null }


    }


    @Test
    fun throwException_checkGenericError_confirmNetworkAndCacheUnchanged() = runBlocking {

        // create a note that doesnt exist in cache
        val noteToUpdate = Note(
            id = FORCE_UPDATE_NOTE_EXCEPTION,
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString(),
            updated_at = UUID.randomUUID().toString(),
            created_at = UUID.randomUUID().toString()
        )
        updateNote.updateNote(
            note = noteToUpdate,
            stateEvent = NoteDetailStateEvent.UpdateNoteEvent()
        ).collect(object : FlowCollector<DataState<NoteDetailViewState>?> {
            override suspend fun emit(value: DataState<NoteDetailViewState>?) {
                assert(
                    value?.stateMessage?.response?.message
                        ?.contains(CacheErrors.CACHE_ERROR_UNKNOWN) ?: false
                )
            }
        })

        // confirm nothing updated in cache
        val cacheNote = noteCacheDataSource.searchNoteById(noteToUpdate.id)
        assertTrue { cacheNote == null }

        // confirm nothing updated in network
        val networkNote = noteRemoteDataSource.searchNote(noteToUpdate)
        assertTrue { networkNote == null }
    }

}