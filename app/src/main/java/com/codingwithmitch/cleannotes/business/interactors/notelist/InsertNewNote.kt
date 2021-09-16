package com.codingwithmitch.cleannotes.business.interactors.notelist

import com.codingwithmitch.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.codingwithmitch.cleannotes.business.data.cache.CacheResponseHandler
import com.codingwithmitch.cleannotes.business.data.remote.abstraction.NoteRemoteDataSource
import com.codingwithmitch.cleannotes.business.data.util.safeCacheCall
import com.codingwithmitch.cleannotes.business.domain.model.Note
import com.codingwithmitch.cleannotes.business.domain.model.NoteFactory
import com.codingwithmitch.cleannotes.business.domain.state.*
import com.codingwithmitch.cleannotes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * use-case for inserting a new note
 */
class InsertNewNote(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteRemoteDataSource,
    private val noteFactory: NoteFactory
) {

    fun insertNewNote(
        id: String? = null,
        title: String,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {

        val newNote = noteFactory.createSingleNote(
            id = id,
            title = title
        )

        val cacheResult = safeCacheCall(Dispatchers.IO) {
            noteCacheDataSource.insertNote(newNote)
        }

        val cacheResponse = object : CacheResponseHandler<NoteListViewState, Long>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {

            override suspend fun handleSuccess(resultObj: Long): DataState<NoteListViewState>? {
                return processSuccessfulResponse(resultObj, stateEvent)
            }

            override fun processSuccessfulResponse(
                resultObj: Long,
                stateEvent: StateEvent ?
            ): DataState<NoteListViewState>? {
                return if (resultObj > 0) {

                    val viewState = NoteListViewState(newNote = newNote)

                    val response = Response(
                        message = INSERT_NOTE_SUCCESS,
                        uiComponentType = UIComponentType.Toast(),
                        messageType = MessageType.Success()
                    )

                    DataState.data(response = response, data = viewState, stateEvent = stateEvent)
                } else {

                    val response = Response(
                        message = INSERT_NOTE_FAILED,
                        uiComponentType = UIComponentType.Toast(),
                        messageType = MessageType.Error()
                    )

                    DataState.data(response = response, data = null, stateEvent = stateEvent)
                }
            }


        }.execute()

        emit(cacheResponse)

        updateNetwork(cacheResponse?.stateMessage?.response?.message, newNote)
    }


    private suspend fun updateNetwork(cacheResponse: String?, newNote: Note) {
        if (cacheResponse.equals(INSERT_NOTE_SUCCESS)) {

            noteNetworkDataSource.insertOrUpdateNote(newNote)
        }
    }

    companion object {
        const val INSERT_NOTE_SUCCESS = "Successfully inserted new note."
        const val INSERT_NOTE_FAILED = "Failed to insert new note."
    }
}