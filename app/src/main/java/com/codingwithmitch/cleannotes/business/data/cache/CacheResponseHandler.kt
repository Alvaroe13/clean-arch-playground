package com.codingwithmitch.cleannotes.business.data.cache

import com.codingwithmitch.cleannotes.business.domain.state.*

/**
 *  -param "ViewState" = Indicates the view in the app where this class is called
 *  -param "Data" = Indicates the type of result returned from this operation, which is always the
 *  type of the "response" param in the builder
 *
 *  -NOTE : See this class implementation in use-cases for visual explanation.
 */
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

    /**
     * NOTE : This can be an abstract fun as well and return error value here but we would have to
     * remove "Data" param of this class since result will be returned by "handleSuccess"  fun
     * or this fun "processErrorResult"
     */
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

    //added by me
    abstract fun processSuccessfulResponse(resultObj: Data, stateEvent : StateEvent ?) : DataState<ViewState> ?

}