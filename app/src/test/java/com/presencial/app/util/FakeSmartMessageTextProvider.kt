package com.presencial.app.util

import com.presencial.app.domain.util.SmartMessageTextProvider

class FakeSmartMessageTextProvider : SmartMessageTextProvider {
    override fun configureRequiredPercentage(): String =
        "Configure seu percentual de presença nas configurações."

    override fun goalMetCelebration(): String =
        "🎉 Meta batida! Aproveite o home office sem culpa."

    override fun mustAttendAllRemaining(): String =
        "⚠️ Alerta: você precisa ir todos os dias restantes para atingir a meta."

    override fun weeklyRequired(days: Int): String =
        "⚠️ Você precisará comparecer $days vezes nesta semana."

    override fun homeOfficeUntilFriday(): String =
        "📅 Você pode fazer home office até sexta sem comprometer sua meta."

    override fun closeToGoal(remaining: Int): String =
        "🎯 Quase lá! Apenas mais $remaining presenciais e a meta é sua."

    override fun monthStartSuggestion(): String =
        "🚀 Início de mês! Que tal planejar 2 presenciais para esta semana?"

    override fun projectedMonthEnd(percentage: Int): String =
        "🎯 Se mantiver o ritmo atual, terminará o mês com $percentage%."

    override fun remainingDays(count: Int): String =
        "📅 Faltam $count ${if (count == 1) "dia" else "dias"} para a meta."
}
