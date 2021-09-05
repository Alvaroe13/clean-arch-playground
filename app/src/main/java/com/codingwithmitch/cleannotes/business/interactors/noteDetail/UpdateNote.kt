package com.codingwithmitch.cleannotes.business.interactors.notedetail

import com.codingwithmitch.cleannotes.business.data.cache.CacheResponseHandler
import com.codingwithmitch.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.codingwithmitch.cleannotes.business.data.remote.abstraction.NoteRemoteDataSource
import com.codingwithmitch.cleannotes.business.data.uitl.safeApiCall
import com.codingwithmitch.cleannotes.business.data.uitl.safeCacheCall
import com.codingwithmitch.cleannotes.business.domain.model.Note
import com.codingwithmitch.cleannotes.business.domain.state.*
import com.codingwithmitch.cleannotes.framework.presentation.notedetail.state.NoteDetailViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UpdateNote(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteRemoteDataSource: NoteRemoteDataSource
) {

    fun updateNote(
        note: Note,
        stateEvent: StateEvent
    ) : Flow<DataState<NoteDetailViewState>?> = flow{

        val cacheResult = safeCacheCall(Dispatchers.IO){
            noteCacheDataSource.updateNote(
                primaryKey =  note.id,
                newTitle = note.title,
                newBody = note.body
            )
        }

        val result = object : CacheResponseHandler<NoteDetailViewState , Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ){
            override suspend fun handleSuccess(resultObj: Int): DataState<NoteDetailViewState>? {
                return processSuccessfulResponse( resultObj , stateEvent )
            }

            override fun processSuccessfulResponse(
                resultObj: Int,
                stateEvent: StateEvent ?
            ): DataState<NoteDetailViewState>? {
                return if(resultObj > 0){
                    DataState.data(
                        response = Response(
                            message = UPDATE_NOTE_SUCCESS,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Success()
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }
                else{
                    DataState.data(
                        response = Response(
                            message = UPDATE_NOTE_FAILED,
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

        updateNetwork(  response =  result?.stateMessage?.response?.message , note = note)

    }

    private suspend fun updateNetwork(response : String? , note: Note){
        if(response.equals(UPDATE_NOTE_SUCCESS)){

            safeApiCall(Dispatchers.IO){
                noteRemoteDataSource.insertOrUpdateNote(note)
            }
        }
    }

    companion object{
        val UPDATE_NOTE_SUCCESS = "Successfully updated note."
        val UPDATE_NOTE_FAILED = "Failed to update note."
        val UPDATE_NOTE_FAILED_PK = "Update failed. Note is missing primary key."
    }

}