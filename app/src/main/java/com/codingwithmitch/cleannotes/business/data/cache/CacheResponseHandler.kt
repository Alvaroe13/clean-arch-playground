package com.codingwithmitch.cleannotes.business.data.cache

import com.codingwithmitch.cleannotes.business.domain.state.*


abstract class CacheResponseHandler<ViewState, Data>(
    private val response: CacheResult<Data?>,
    private val stateEvent: StateEvent?
) {

    /**
     * the one triggering
     */
    suspend fun execute(): DataState<ViewState>? {

        return when (response) {

            is CacheResult.GenericError -> processErrorResult(response.errorMessage)

            is CacheResult.Success -> {
                response.value?.let{
                    handleSuccess(resultObj = it)
                } ?: processErrorResult(CacheErrors.CACHE_DATA_NULL)
            }

        }
    }

    private fun processErrorResult(error: String?): DataState<ViewState> =
        DataState.error(
            response = Response(
                message = "${stateEvent?.errorInfo()}, Reason: $error",
                uiComponentType = UIComponentType.Dialog(),
                messageType = MessageType.Error()
            ),
            stateEvent = stateEvent
        )

    abstract suspend fun handleSuccess(resultObj: Data): DataState<ViewState>?

}