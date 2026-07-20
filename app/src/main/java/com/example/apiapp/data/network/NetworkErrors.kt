package com.example.apiapp.data.network

import retrofit2.HttpException
import java.io.IOException

fun Throwable.toUserMessage(): String = when (this) {
    is HttpException -> when (code()) {
        in 500..599 -> "El servidor no respondió correctamente (código ${code()})."
        404 -> "El recurso solicitado no existe (404)."
        else -> "Error de red inesperado (código ${code()})."
    }
    is IOException -> "No se pudo conectar a Internet. Verifica tu conexión."
    else -> localizedMessage ?: "Ocurrió un error inesperado."
}
