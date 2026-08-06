package com.presencial.app.domain.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

interface TimeProvider {
    fun today(): LocalDate
    fun now(): LocalDateTime
    fun currentMonth(): YearMonth
}

@Singleton
class DefaultTimeProvider @Inject constructor() : TimeProvider {
    override fun today(): LocalDate = LocalDate.now()
    override fun now(): LocalDateTime = LocalDateTime.now()
    override fun currentMonth(): YearMonth = YearMonth.now()
}
