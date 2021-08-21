package com.codingwithmitch.cleannotes.business.data.remote.util

import com.codingwithmitch.cleannotes.business.domain.state.*


abstract class ApiResponseHandler <ViewState, Data>(
    private val response: ApiResult<Data?>,
    private val stateEvent: StateEvent?
){

    suspend fun getResult(): DataState<ViewState>? {

        return when(response){

            is ApiResult.GenericError -> processErrorResponse( response.errorMessage.toString() )

            is ApiResult.NetworkError -> processErrorResponse( NetworkErrors.NETWORK_ERROR )

            is ApiResult.Success -> {

                response.value?.let{
                    handleSuccess(resultObj = it)
                } ?: processErrorResponse( NetworkErrors.NETWORK_DATA_NULL )

            }

        }
    }

    private fun processErrorResponse(error : String) : DataState<ViewState> =
        DataState.error(
            response = Response(
                message = "${stateEvent?.errorInfo()}\n\nReason: $error.",
                uiComponentType = UIComponentType.Dialog(),
                messageType = MessageType.Error()
            ),
            stateEvent = stateEvent
        )

    abstract suspend fun handleSuccess(resultObj: Data): DataState<ViewState>?

}