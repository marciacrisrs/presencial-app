# Presencial

Aplicativo Android para controle de comparecimento presencial no trabalho. Calcula automaticamente dias úteis, feriados nacionais brasileiros e a meta mensal de presença com base no percentual configurado.

## Funcionalidades

- **Dashboard** — progresso em tempo real, barras circular/linear, mensagens inteligentes e check-in diário
- **Calendário mensal** — visualização colorida por status com edição de dias passados
- **Histórico** — resumo de todos os meses registrados com compartilhamento
- **Estatísticas** — gráficos de evolução, média anual, sequências e exportação PDF
- **Configurações** — percentual de presença, sábados como dias úteis, backup/restauração JSON
- **Notificações** — lembrete às 18h em dias úteis (se ainda não confirmou presença)
- **Widget** — dias restantes na tela inicial
- **Tema claro/escuro** — Material Design 3 com Material You

## Tecnologias

| Camada | Stack |
|--------|-------|
| UI | Kotlin, Jetpack Compose, Material 3, Navigation Compose, Lottie |
| Arquitetura | MVVM, Clean Architecture, Repository Pattern |
| DI | Hilt |
| Dados | Room, DataStore Preferences |
| Async | Kotlin Coroutines + Flow |
| Background | WorkManager |
| Widget | Glance AppWidget |
| Datas | `java.time` (API 26+) |

## Arquitetura

```
app/src/main/java/com/presencial/app/
├── data/           # Implementações de repositório, Room, DataStore, backup, PDF
├── domain/         # Modelos, interfaces de repositório, use cases, utilitários
├── presentation/   # ViewModels e telas Compose por feature
├── ui/             # Tema Material 3 e componentes reutilizáveis
├── di/             # Módulos Hilt
├── notification/   # Canal e agendamento de lembretes
├── widget/         # Widget Glance
└── worker/         # Worker de notificação diária
```

### Fluxo de dados

```
UI (Compose) → ViewModel → UseCase → Repository → Room / DataStore
                              ↑
                         Domain Models
```

### Regras de negócio principais

1. **Meta mensal:** `ceil(dias_úteis × percentual / 100)`
2. **Dias úteis:** exclui domingos, feriados nacionais e sábados (configurável)
3. **Feriados móveis:** calculados a partir da Páscoa (algoritmo de Meeus/Jones/Butcher)
4. **Check-in:** apenas dias úteis passados ou hoje são editáveis

## Como executar

### Pré-requisitos

- Android Studio Ladybug (2024.2+) ou mais recente
- JDK 17
- Android SDK 35
- Dispositivo/emulador API 26+

### Passos

1. Abra a pasta `Presencial` no Android Studio
2. Aguarde o sync do Gradle
3. Execute no emulador ou dispositivo: **Run ▶ app**

```bash
# Via linha de comando (com Gradle wrapper)
./gradlew assembleDebug
./gradlew test
```

## Testes

Testes unitários na camada de domínio:

- `WorkdayCalculatorTest` — cálculo de dias úteis
- `GoalCalculatorTest` — meta e percentuais
- `HolidayCalculatorTest` — feriados nacionais
- `EasterCalculatorTest` — Páscoa e feriados móveis

```bash
./gradlew test
```

## Decisões de design

| Decisão | Motivo |
|---------|--------|
| DataStore para settings, Room para check-ins | Settings são pequenas e tipadas; check-ins precisam de queries relacionais |
| Feriados calculados localmente | Offline-first, sem dependência de APIs externas |
| WorkManager para notificações | Respeita Doze mode e reinício do dispositivo |
| Use cases por feature | ViewModels finos, lógica testável isolada |
| Glance para widget | API moderna alinhada com Compose |
| `java.time` nativo | minSdk 26 dispensa ThreeTenABP |

## Estrutura de entidades Room

- **CheckIn** — `dateEpochDay`, `status`, `updatedAt`
- **MonthlySummary** — agregado mensal cacheado para histórico rápido
- **Settings** — entidade reservada; configurações ativas via DataStore

## Licença

Projeto de demonstração — uso livre.
