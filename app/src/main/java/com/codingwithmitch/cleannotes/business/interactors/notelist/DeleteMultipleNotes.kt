package com.codingwithmitch.cleannotes.business.interactors.notelist

import com.codingwithmitch.cleannotes.business.data.cache.CacheResponseHandler
import com.codingwithmitch.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.codingwithmitch.cleannotes.business.data.remote.abstraction.NoteRemoteDataSource
import com.codingwithmitch.cleannotes.business.data.util.safeApiCall
import com.codingwithmitch.cleannotes.business.data.util.safeCacheCall
import com.codingwithmitch.cleannotes.business.domain.model.Note
import com.codingwithmitch.cleannotes.business.domain.state.*
import com.codingwithmitch.cleannotes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteMultipleNotes(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteRemoteDataSource: NoteRemoteDataSource
) {


    // set true if an error occurs when deleting any of the notes from cache
    private var onDeleteError: Boolean = false

    // notes that were successfully deleted
    val successfulDeletes: ArrayList<Note> = ArrayList()

    /**
     * Logic:
     * 1. execute all the deletes and save result into an ArrayList<DataState<NoteListViewState>>
     * 2a. If one of the results is a failure, emit an "error" response
     * 2b. If all success, emit success response
     * 3. Update network with notes that were successfully deleted
     */
    fun deleteNotes(
        notes: List<Note>,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {

        notes.forEach { note ->
            deleteNote(note, stateEvent)
        }

        if(onDeleteError){
            emit(
                DataState.data<NoteListViewState>(
                    response = Response(
                        message = DELETE_NOTES_ERRORS,
                        uiComponentType = UIComponentType.Dialog(),
                        messageType = MessageType.Success()
                    ),
                    data = null,
                    stateEvent = stateEvent
                )
            )
        }
        else{
            emit(
                DataState.data<NoteListViewState>(
                    response = Response(
                        message = DELETE_NOTES_SUCCESS,
                        uiComponentType = UIComponentType.Toast(),
                        messageType = MessageType.Success()
                    ),
                    data = null,
                    stateEvent = stateEvent
                )
            )
        }

        updateNetwork(successfulDeletes)
    }

    private suspend fun deleteNote(note : Note, stateEvent : StateEvent){
        val cacheResult = safeCacheCall(Dispatchers.IO){
            noteCacheDataSource.deleteNote(note.id)
        }

        val response = object: CacheResponseHandler<NoteListViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ){
            override suspend fun handleSuccess(resultObj: Int): DataState<NoteListViewState>? {
                return processSuccessfulResponse(resultObj, stateEvent)
            }

            override fun processSuccessfulResponse(
                resultObj: Int,
                stateEvent: StateEvent ?
            ): DataState<NoteListViewState>? {
                // if -1 is error
                if(resultObj < 0){
                    onDeleteError = true
                } else {
                    successfulDeletes.add(note)
                }
                return null
            }
        }.execute()

        // check for random errors
        if(response?.stateMessage?.response?.message
                ?.contains(stateEvent.errorInfo()) == true){
            onDeleteError = true
        }
    }

    private suspend fun updateNetwork(successfulDeletes: ArrayList<Note>){
        for (note in successfulDeletes){

            // delete from "notes" node
            safeApiCall(Dispatchers.IO){
                noteRemoteDataSource.deleteNote(note.id)
            }

            // insert into "deletes" node
            safeApiCall(Dispatchers.IO){
                noteRemoteDataSource.insertDeletedNote(note)
            }
        }
    }

    companion object{
        val DELETE_NOTES_SUCCESS = "Successfully deleted notes."
        val DELETE_NOTES_ERRORS = "Not all the notes you selected were deleted. There was some errors."
        val DELETE_NOTES_YOU_MUST_SELECT = "You haven't selected any notes to delete."
        val DELETE_NOTES_ARE_YOU_SURE = "Are you sure you want to delete these?"
    }
}