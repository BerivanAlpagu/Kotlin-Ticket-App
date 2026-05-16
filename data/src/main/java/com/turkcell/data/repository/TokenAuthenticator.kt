package com.turkcell.data.repository

import com.turkcell.data.local.TokenStore
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

// Sadece HTTP 401'lerde çalış, Refresh akışı sürdür.
class TokenAuthenticator(
    private val tokenStore: TokenStore
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        // İstek 401'e düştüğü anda, eğer sistemde jwt-refresh pairi tanımlıysa git refresh ile yeni jwt alıp isteği tekrar dene.
        val refreshToken = tokenStore.refreshTokenBlocking() ?: return null;

        //... bu token ile jwt yenilemeye çalış..
        return null
    }
}