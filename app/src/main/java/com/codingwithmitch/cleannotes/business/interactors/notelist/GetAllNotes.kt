package com.codingwithmitch.cleannotes.business.interactors.notelist

import com.codingwithmitch.cleannotes.business.data.cache.CacheResponseHandler
import com.codingwithmitch.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.codingwithmitch.cleannotes.business.data.util.safeCacheCall
import com.codingwithmitch.cleannotes.business.domain.model.Note
import com.codingwithmitch.cleannotes.business.domain.state.*
import com.codingwithmitch.cleannotes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetAllNotes(
    private val noteCacheDataSource: NoteCacheDataSource
) {

    fun getAllNotes(
        stateEvent: StateEvent
    ) : Flow<DataState<NoteListViewState>?> = flow{

        val cacheResult = safeCacheCall(Dispatchers.IO) {
            noteCacheDataSource.getAllNotes()
        }

        val response = object : CacheResponseHandler<NoteListViewState, List<Note>>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {

            override suspend fun handleSuccess(resultObj: List<Note>): DataState<NoteListViewState>? {
                return processSuccessfulResponse(resultObj, stateEvent)
            }

            override fun processSuccessfulResponse(
                resultObj: List<Note>,
                stateEvent: StateEvent?
            ): DataState<NoteListViewState>? {

                var message: String? = SearchNotes.SEARCH_NOTES_SUCCESS

                var uiComponentType: UIComponentType? = UIComponentType.None()

                if (resultObj.isEmpty()) {
                    message = SearchNotes.SEARCH_NOTES_NO_MATCHING_RESULTS
                    uiComponentType = UIComponentType.Toast()
                }

                return DataState.data(
                    response = Response(
                        message = message,
                        uiComponentType = uiComponentType as UIComponentType,
                        messageType = MessageType.Success()
                    ),
                    data = NoteListViewState(
                        noteList = ArrayList(resultObj)
                    ),
                    stateEvent = stateEvent
                )

            }

        }.execute()

        //send response to collector (in vm)
        emit(response)

    }
}