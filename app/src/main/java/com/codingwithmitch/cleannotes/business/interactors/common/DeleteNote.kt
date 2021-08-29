package com.codingwithmitch.cleannotes.business.interactors.common

import com.codingwithmitch.cleannotes.business.data.cache.CacheResponseHandler
import com.codingwithmitch.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.codingwithmitch.cleannotes.business.data.remote.abstraction.NoteRemoteDataSource
import com.codingwithmitch.cleannotes.business.data.uitl.safeApiCall
import com.codingwithmitch.cleannotes.business.data.uitl.safeCacheCall
import com.codingwithmitch.cleannotes.business.domain.model.Note
import com.codingwithmitch.cleannotes.business.domain.state.*
import com.codingwithmitch.cleannotes.framework.presentation.notelist.state.NoteListStateEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Since this class will be used in 2 views (noteList / noteDetails ) we need to specify the
 * viewState when this use-case is executed.
 */
class DeleteNote<ViewState>(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteRemoteDataSource: NoteRemoteDataSource
){

    fun deleteNote(
        note : Note,
        stateEvent: NoteListStateEvent
    ): Flow<DataState<ViewState>?> = flow{

        /** Int returned is the column deleted or "-1" if deletion failed */
        val cacheResult = safeCacheCall(Dispatchers.IO){
            noteCacheDataSource.deleteNote( note.id)
        }

        val response = object : CacheResponseHandler<ViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ){
            override suspend fun handleSuccess(resultObj: Int): DataState<ViewState>? {
                return processSuccessfulResponse(resultObj, stateEvent)
            }

            override fun processSuccessfulResponse(
                resultObj: Int,
                stateEvent: StateEvent
            ): DataState<ViewState>? {

                return if ( resultObj > 0){
                    DataState.data(
                        response = Response(
                            message = DELETE_NOTE_SUCCESS,
                            uiComponentType = UIComponentType.None(),
                            messageType = MessageType.Success()

                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }else{
                    DataState.data(
                        response = Response(
                            message = DELETE_NOTE_FAILED,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Success()

                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }

            }

        }.execute()

        updateNetwork(
            message = response?.stateMessage?.response?.message ,
            note = note
        )

        emit(response)
    }

    private suspend fun updateNetwork(message : String?, note:Note){
        if( message.equals(DELETE_NOTE_SUCCESS)){

            //remove from notes node in firestore
            safeApiCall(Dispatchers.IO){
                noteRemoteDataSource.deleteDeletedNote(note)
            }

            //add it to deleted-notes node in firestore
            safeApiCall(Dispatchers.IO){
                noteRemoteDataSource.insertDeletedNote(note)
            }
        }
    }

    companion object{
        val DELETE_NOTE_SUCCESS = "Successfully deleted note."
        val DELETE_NOTE_PENDING = "Delete pending..."
        val DELETE_NOTE_FAILED = "Failed to delete note."
        val DELETE_ARE_YOU_SURE = "Are you sure you want to delete this?"
    }
}