package com.presencial.app.domain.location

class GeofenceRegistrationException(
    message: String,
    val retryable: Boolean,
    cause: Throwable? = null
) : IllegalStateException(message, cause)
