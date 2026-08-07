package com.presencial.app.presentation.history

import app.cash.turbine.test
import com.presencial.app.domain.usecase.GetHistoryUseCase
import com.presencial.app.util.MainDispatcherExtension
import com.presencial.app.util.TestDataFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class HistoryViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val getHistoryUseCase = mockk<GetHistoryUseCase>()

    @Test
    fun `summaries should reflect use case flow`() = runTest {
        val summaries = listOf(TestDataFactory.createMonthlySummary())
        every { getHistoryUseCase() } returns flowOf(summaries)

        val viewModel = HistoryViewModel(getHistoryUseCase)

        viewModel.summaries.test {
            assertEquals(summaries, awaitItem())
        }
    }
}
