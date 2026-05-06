package com.turkcell.data.util

// tüm api isteklerim tek kalıpta ilerlesin

// .success
// .complete
// .onSuccess
interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error<T>(val error: Throwable) : ApiResult<Nothing>
}