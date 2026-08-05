package com.presencial.app.domain.model

/**
 * Status possível de um dia no calendário de presença.
 */
enum class DayStatus {
    PRESENCIAL,
    HOME_OFFICE,
    FERIADO,
    FIM_DE_SEMANA,
    FUTURO,
    FALTOU,
    ABSENCE
}
