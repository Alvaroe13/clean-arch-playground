package com.codingwithmitch.cleannotes.business.data.util

import com.codingwithmitch.cleannotes.business.data.cache.CacheConstants.CACHE_TIMEOUT
import com.codingwithmitch.cleannotes.business.data.cache.CacheErrors.CACHE_ERROR_TIMEOUT
import com.codingwithmitch.cleannotes.business.data.cache.CacheErrors.CACHE_ERROR_UNKNOWN
import com.codingwithmitch.cleannotes.business.data.cache.CacheResult
import com.codingwithmitch.cleannotes.business.data.remote.ApiResult
import com.codingwithmitch.cleannotes.business.data.remote.NetworkConstants.NETWORK_TIMEOUT
import com.codingwithmitch.cleannotes.business.data.remote.NetworkErrors.NETWORK_ERROR_TIMEOUT
import com.codingwithmitch.cleannotes.business.data.remote.NetworkErrors.NETWORK_ERROR_UNKNOWN
import com.codingwithmitch.cleannotes.util.crashLog
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.io.IOException

/**
 * Reference: https://medium.com/@douglas.iacovelli/how-to-handle-errors-with-retrofit-and-coroutines-33e7492a912
 */

const val ERROR_UNKNOWN = "Unknown error"
const val INVALID_STATE_EVENT = "Invalid state event"

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> T?
): ApiResult<T?> {
    return withContext(dispatcher) {
        try {
            // throws TimeoutCancellationException
            withTimeout(NETWORK_TIMEOUT){
                ApiResult.Success(apiCall.invoke())
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            crashLog( throwable.message )
            when (throwable) {
                is TimeoutCancellationException -> {
                    val code = 408 // timeout error code
                    ApiResult.GenericError(code, NETWORK_ERROR_TIMEOUT)
                }
                is IOException -> {
                    ApiResult.NetworkError
                }
                is HttpException -> {
                    val code = throwable.code()
                    val errorResponse = convertErrorBody(throwable)
                    ApiResult.GenericError(
                        code,
                        errorResponse
                    )
                }
                else -> {
                    ApiResult.GenericError(
                        null,
                        NETWORK_ERROR_UNKNOWN
                    )
                }
            }
        }
    }
}

/**
 * NOTE : "cacheCall" is a fun passed as param and "invoke()" method is the one used to call the
 * func pass as param and do what is has to do.
 * (E.g = In case of calling this "safeCacheCall" method in InsertNewNote.kt is to insert a note.)
 */
suspend fun <T> safeCacheCall(
    dispatcher: CoroutineDispatcher,
    cacheCall: suspend () -> T?
): CacheResult<T?> {
    return withContext(dispatcher) {
        try {
            // throws TimeoutCancellationException
            withTimeout(CACHE_TIMEOUT){
                CacheResult.Success(cacheCall.invoke())
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            crashLog( throwable.message )
            when (throwable) {

                is TimeoutCancellationException -> {
                    CacheResult.GenericError(CACHE_ERROR_TIMEOUT)
                }
                else -> {
                    CacheResult.GenericError(CACHE_ERROR_UNKNOWN)
                }
            }
        }
    }
}

private fun convertErrorBody(throwable: HttpException): String? {
    return try {
        throwable.response()?.errorBody()?.string()
    } catch (exception: Exception) {
        ERROR_UNKNOWN
    }
}
