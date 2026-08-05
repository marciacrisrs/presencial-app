package com.presencial.app.domain.util

import java.time.LocalDate
import java.time.Month

/**
 * Calcula feriados nacionais brasileiros, incluindo móveis baseados na Páscoa.
 */
object HolidayCalculator {

    data class Holiday(val date: LocalDate, val name: String)

    fun getHolidaysForYear(year: Int): List<Holiday> {
        val easter = EasterCalculator.calculateEaster(year)
        val fixed = listOf(
            Holiday(LocalDate.of(year, Month.JANUARY, 1), "Confraternização Universal"),
            Holiday(LocalDate.of(year, Month.APRIL, 21), "Tiradentes"),
            Holiday(LocalDate.of(year, Month.MAY, 1), "Dia do Trabalho"),
            Holiday(LocalDate.of(year, Month.SEPTEMBER, 7), "Independência"),
            Holiday(LocalDate.of(year, Month.OCTOBER, 12), "Nossa Senhora Aparecida"),
            Holiday(LocalDate.of(year, Month.NOVEMBER, 2), "Finados"),
            Holiday(LocalDate.of(year, Month.NOVEMBER, 15), "Proclamação da República"),
            Holiday(LocalDate.of(year, Month.NOVEMBER, 20), "Consciência Negra"),
            Holiday(LocalDate.of(year, Month.DECEMBER, 25), "Natal")
        )
        val mobile = listOf(
            Holiday(easter.minusDays(48), "Carnaval"),
            Holiday(easter.minusDays(47), "Carnaval"),
            Holiday(easter.minusDays(2), "Sexta-feira Santa"),
            Holiday(easter.plusDays(60), "Corpus Christi")
        )
        return (fixed + mobile).sortedBy { it.date }
    }

    fun getHoliday(date: LocalDate): Holiday? =
        getHolidaysForYear(date.year).find { it.date == date }

    fun isHoliday(date: LocalDate): Boolean = getHoliday(date) != null

    fun getHolidaysInMonth(year: Int, month: Int): List<Holiday> =
        getHolidaysForYear(year).filter { it.date.monthValue == month }
}
