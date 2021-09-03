package com.codingwithmitch.cleannotes.business.interactors.notelist

import com.codingwithmitch.cleannotes.business.data.cache.CacheResponseHandler
import com.codingwithmitch.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.codingwithmitch.cleannotes.business.data.remote.abstraction.NoteRemoteDataSource
import com.codingwithmitch.cleannotes.business.data.uitl.safeApiCall
import com.codingwithmitch.cleannotes.business.data.uitl.safeCacheCall
import com.codingwithmitch.cleannotes.business.domain.model.Note
import com.codingwithmitch.cleannotes.business.domain.state.*
import com.codingwithmitch.cleannotes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RestoreDeletedNote(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteRemoteDataSource: NoteRemoteDataSource
) {

    fun restoreDeletedNote(
        note: Note,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {


        val cacheResult = safeCacheCall(Dispatchers.IO) {
            noteCacheDataSource.insertNote(note)
        }

        val result = object : CacheResponseHandler<NoteListViewState, Long>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Long): DataState<NoteListViewState>? {
                return processSuccessfulResponse(resultObj, stateEvent)
            }

            override fun processSuccessfulResponse(
                resultObj: Long,
                stateEvent: StateEvent
            ): DataState<NoteListViewState>? {
                return if (resultObj > 0) {
                    val viewState =
                        NoteListViewState(
                            notePendingDelete = NoteListViewState.NotePendingDelete(
                                note = note
                            )
                        )
                    DataState.data(
                        response = Response(
                            message = RESTORE_NOTE_SUCCESS,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Success()
                        ),
                        data = viewState,
                        stateEvent = stateEvent
                    )
                } else {
                    DataState.data(
                        response = Response(
                            message = RESTORE_NOTE_FAILED,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Error()
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }
            }

        }.execute()


        emit(result)

        updateNetwork(result?.stateMessage?.response?.message, note)
    }

    private suspend fun updateNetwork(message: String?, note: Note) {
        if (message.equals(RESTORE_NOTE_SUCCESS)) {

            // add again to "notes" node
            safeApiCall(Dispatchers.IO) {
                noteRemoteDataSource.insertOrUpdateNote(note)
            }

            // remove from "deleted" node
            safeApiCall(Dispatchers.IO) {
                noteRemoteDataSource.deleteDeletedNote(note)
            }
        }
    }


    companion object {
        val RESTORE_NOTE_SUCCESS = "Successfully restored the deleted note."
        val RESTORE_NOTE_FAILED = "Failed to restore the deleted note."
    }

}