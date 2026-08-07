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
            Holiday(LocalDate.of(year, Month.JANUARY, DAY_01), "Confraternização Universal"),
            Holiday(LocalDate.of(year, Month.APRIL, DAY_21), "Tiradentes"),
            Holiday(LocalDate.of(year, Month.MAY, DAY_01), "Dia do Trabalho"),
            Holiday(LocalDate.of(year, Month.SEPTEMBER, DAY_07), "Independência"),
            Holiday(LocalDate.of(year, Month.OCTOBER, DAY_12), "Nossa Senhora Aparecida"),
            Holiday(LocalDate.of(year, Month.NOVEMBER, DAY_02), "Finados"),
            Holiday(LocalDate.of(year, Month.NOVEMBER, DAY_15), "Proclamação da República"),
            Holiday(LocalDate.of(year, Month.NOVEMBER, DAY_20), "Consciência Negra"),
            Holiday(LocalDate.of(year, Month.DECEMBER, DAY_25), "Natal")
        )
        val mobile = listOf(
            Holiday(easter.minusDays(CARNIVAL_MONDAY_OFFSET), "Carnaval"),
            Holiday(easter.minusDays(CARNIVAL_TUESDAY_OFFSET), "Carnaval"),
            Holiday(easter.minusDays(GOOD_FRIDAY_OFFSET), "Sexta-feira Santa"),
            Holiday(easter.plusDays(CORPUS_CHRISTI_OFFSET), "Corpus Christi")
        )
        return (fixed + mobile).sortedBy { it.date }
    }

    private const val DAY_01 = 1
    private const val DAY_02 = 2
    private const val DAY_07 = 7
    private const val DAY_12 = 12
    private const val DAY_15 = 15
    private const val DAY_20 = 20
    private const val DAY_21 = 21
    private const val DAY_25 = 25
    private const val CARNIVAL_MONDAY_OFFSET = 48L
    private const val CARNIVAL_TUESDAY_OFFSET = 47L
    private const val GOOD_FRIDAY_OFFSET = 2L
    private const val CORPUS_CHRISTI_OFFSET = 60L

    fun getHoliday(date: LocalDate): Holiday? =
        getHolidaysForYear(date.year).find { it.date == date }

    fun isHoliday(date: LocalDate): Boolean = getHoliday(date) != null
}
