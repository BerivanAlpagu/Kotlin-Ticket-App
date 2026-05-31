package com.turkcell.data.util

import com.turkcell.core.util.ApiException
import com.turkcell.core.util.NetworkException
import retrofit2.HttpException
import java.io.IOException

// Todo: İlerde tekrar konuşalım.
suspend inline fun <T> runCatchingApi(crossinline block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch(e: HttpException)
{
    Result.failure(ApiException(code = e.code(), errorMessage = e.message(), cause=e))
} catch(e: IOException)
{
    Result.failure(NetworkException(e))
} catch(e: Exception)
{
    Result.failure(e)
}

//1- bir ekranda koin ile (araştır) authrepository