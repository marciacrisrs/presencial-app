package com.presencial.app.util

import com.presencial.app.domain.util.TimeProvider
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class FakeTimeProvider(
    private var today: LocalDate = LocalDate.now(),
    private var now: LocalDateTime = LocalDateTime.now(),
    private var currentMonth: YearMonth = YearMonth.now()
) : TimeProvider {

    override fun today(): LocalDate = today
    override fun now(): LocalDateTime = now
    override fun currentMonth(): YearMonth = currentMonth

    fun setToday(date: LocalDate) {
        today = date
        currentMonth = YearMonth.from(date)
    }

    fun setNow(dateTime: LocalDateTime) {
        now = dateTime
        today = dateTime.toLocalDate()
        currentMonth = YearMonth.from(dateTime)
    }
}
