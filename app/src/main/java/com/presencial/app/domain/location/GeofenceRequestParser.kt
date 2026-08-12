package com.presencial.app.domain.location

object GeofenceRequestParser {
    fun parseWorkAddressId(requestId: String?): Long? = requestId?.toLongOrNull()
}
