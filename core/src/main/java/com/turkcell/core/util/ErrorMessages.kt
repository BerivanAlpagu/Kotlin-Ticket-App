package com.turkcell.core.util

// Bağlantı kopuk, timeout, dns çözümleme
class NetworkException(cause: Throwable) : RuntimeException("Network Error", cause)

// Sunucu 4xx, 5xx
class ApiException(
    val code: Int,
    val errorMessage: String?,
    cause: Throwable? = null
) : RuntimeException("HTTP $code: $errorMessage", cause)

fun Throwable.toUserMessage(
    extraCodes: Map<Int, String> = emptyMap()
): String = when (this) {
    is ApiException -> extraCodes[code] ?: when (code) {
        401 -> "Email veya şifre hatalı."
        403 -> when (errorMessage) {
            "not_purchase_owner" -> "Bu satın alım size ait değil."
            else -> "Bu işlemi yapmaya yetkiniz yok."
        }
        404 -> "İstenen kaynak bulunamadı."
        409 -> when (errorMessage) {
            "capacity_exceeded" -> "Stok yetersiz, lütfen etkinliği yenileyin."
            "already_paid"      -> "Bu satın alım zaten ödenmiş."
            else -> "Bir çakışma hatası oluştu."
        }
        in 500..599 -> "Sunucu şu anda cevap veremiyor."
        else -> "Beklenmeyen bir hata oluştu."
    }
    is NetworkException -> "İnternet bağlantısı yok."
    else -> message ?: "Bilinmeyen bir hata oluştu."
}
