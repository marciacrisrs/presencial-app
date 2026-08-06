# Plano de Implementação de Testes Unitários

Este plano descreve a estratégia para implementar testes unitários em todo o projeto Presencial, visando 80% de cobertura de linhas e branches, utilizando JUnit 5, MockK e Turbine.

## User Review Required

> [!IMPORTANT]
> A migração para **JUnit 5** requer configuração no Gradle para habilitar a execução dos testes (`useJUnitPlatform()`).
> A cobertura será medida via relatório do Gradle (Kover ou similar, se disponível, ou via IDE).

## Proposed Changes

### Dependências e Configuração

#### [MODIFY] [libs.versions.toml](file:///C:/Users/marci/Projects/Presencial/gradle/libs.versions.toml)
Adicionar dependências de MockK, Turbine e JUnit 5.

#### [MODIFY] [build.gradle.kts (app)](file:///C:/Users/marci/Projects/Presencial/app/build.gradle.kts)
Configurar `useJUnitPlatform()` e adicionar as novas dependências de teste.

### Camada de Domínio (domain)

#### [NEW] Testes de Use Cases
- `ToggleTodayCheckInUseCaseTest.kt`
- `UpdateDayStatusUseCaseTest.kt`
- `GetDashboardDataUseCaseTest.kt`
- `GetHistoryUseCaseTest.kt`
- `GetMonthCalendarUseCaseTest.kt`
- `GetStatisticsUseCaseTest.kt`
- `GetAiSmartMessageUseCaseTest.kt`

#### [NEW] Testes de Util
- `SmartMessageGeneratorTest.kt`
- Adicionar casos de borda nos testes existentes se necessário.

### Camada de Dados (data)

#### [NEW] Testes de Mappers
- `EntityMappersTest.kt`

#### [NEW] Testes de Repositories
- `AbsenceRepositoryImplTest.kt`
- `CheckInRepositoryImplTest.kt`
- `WorkAddressRepositoryImplTest.kt`
- `MonthlySummaryRepositoryImplTest.kt`

#### [NEW] Testes de Backup/Export
- `BackupManagerTest.kt`
- `PdfExporterTest.kt` (lógica de formatação)

### Camada de Apresentação (presentation)

#### [NEW] Testes de ViewModels
- `DashboardViewModelTest.kt`
- `AbsenceViewModelTest.kt`
- `CalendarViewModelTest.kt`
- `HistoryViewModelTest.kt`
- `WorkAddressViewModelTest.kt`
- `SettingsViewModelTest.kt`
- `StatisticsViewModelTest.kt`

### Infraestrutura de Teste

#### [NEW] [TestDataFactory.kt](file:///C:/Users/marci/Projects/Presencial/app/src/test/java/com/presencial/app/util/TestDataFactory.kt)
Fábrica centralizada para criação de objetos de teste (Modelos e Entidades).

## Verification Plan

### Automated Tests
- Executar `./gradlew test` para garantir que todos os testes passem.
- Verificar o relatório de cobertura gerado pelo Android Studio ou ferramenta de linha de comando.

### Manual Verification
- O projeto deve continuar compilando e funcionando normalmente (`./gradlew assembleDebug`).
