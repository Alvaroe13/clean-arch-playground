package com.codingwithmitch.cleannotes.business.data.cache.util

import com.codingwithmitch.cleannotes.business.domain.state.*


abstract class CacheResponseHandler<ViewState, Data>(
    private val response: CacheResult<Data?>,
    private val stateEvent: StateEvent?
) {
    suspend fun getResult(): DataState<ViewState>? {

        return when (response) {

            is CacheResult.GenericError -> processErrorResult(response.errorMessage)

            is CacheResult.Success -> {
                if (response.value == null) {
                    processErrorResult(CacheErrors.CACHE_DATA_NULL)
                } else {
                    handleSuccess(resultObj = response.value)
                }
            }

        }
    }

    private fun processErrorResult(error: String?): DataState<ViewState> {
        return DataState.error(
            response = Response(
                message = "${stateEvent?.errorInfo()}\n\nReason: $error",
                uiComponentType = UIComponentType.Dialog(),
                messageType = MessageType.Error()
            ),
            stateEvent = stateEvent
        )
    }

    abstract suspend fun handleSuccess(resultObj: Data): DataState<ViewState>?

}