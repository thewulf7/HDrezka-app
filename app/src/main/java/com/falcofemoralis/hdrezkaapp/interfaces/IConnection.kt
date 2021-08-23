package com.falcofemoralis.hdrezkaapp.interfaces

interface IConnection {
    enum class ErrorType {
        NO_INTERNET,
        PARSING_ERROR,
        EMPTY,
        TIMEOUT,
        ERROR,
        BLOCKED_SITE,
        MALFORMED_URL,
        MODERATE_BY_ADMIN,
        PROVIDER_TIMEOUT
    }

    fun showConnectionError(type: ErrorType, errorText: String)
}