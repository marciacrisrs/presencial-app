package com.presencial.app.domain.util

import java.time.LocalDate
import java.time.YearMonth

/**
 * Gera mensagens inteligentes para o dashboard com base no progresso atual.
 */
object SmartMessageGenerator {

    fun generate(
        completedDays: Int,
        requiredDays: Int,
        remainingDays: Int,
        achievedPercentage: Float,
        today: LocalDate,
        yearMonth: YearMonth,
        countSaturdaysAsWorkdays: Boolean
    ): String {
        if (requiredDays <= 0) {
            return "Configure seu percentual de presença nas configurações."
        }

        if (completedDays >= requiredDays) {
            return "Meta concluída 🎉"
        }

        if (remainingDays == 0) {
            return "Meta concluída 🎉"
        }

        val remainingWorkdays = WorkdayCalculator.countRemainingWorkdays(
            today, yearMonth, countSaturdaysAsWorkdays
        )

        if (achievedPercentage >= 80f) {
            return "Você já cumpriu ${achievedPercentage.toInt()}% da meta."
        }

        if (remainingDays <= remainingWorkdays && remainingWorkdays > 0) {
            val weeks = (remainingWorkdays / 5.0).coerceAtLeast(1.0)
            if (remainingDays <= 3) {
                return "Faltam apenas $remainingDays ${dayLabel(remainingDays)}."
            }
            return "Você precisará ir $remainingDays vezes nas próximas ${weeks.toInt()} semanas."
        }

        if (remainingDays > remainingWorkdays) {
            return "Atenção: faltam $remainingDays dias e restam apenas $remainingWorkdays dias úteis."
        }

        if (completedDays > requiredDays / 2) {
            return "Você está adiantado."
        }

        if (remainingWorkdays > remainingDays * 2) {
            return "Mesmo não indo esta semana, ainda atingirá a meta."
        }

        return "Faltam $remainingDays ${dayLabel(remainingDays)} presenciais para a meta."
    }

    private fun dayLabel(count: Int): String = if (count == 1) "dia" else "dias"
}
