# Presencial
Presencial nasceu de um problema real: acompanhar uma meta mensal de presença sem precisar fazer contas manualmente.

Aplicativo Android para controle de comparecimento presencial no trabalho. Calcula automaticamente dias úteis, feriados nacionais brasileiros e a meta mensal de presença com base no percentual configurado.

## Funcionalidades

- **Dashboard** — progresso em tempo real, barras circular/linear, mensagens inteligentes geradas por IA e check-in diário
- **Check-in Automático** — cadastro de locais de trabalho (Geofencing) para registro automático de presença ao chegar no escritório
- **Calendário mensal** — visualização colorida por status com edição de dias passados
- **Histórico** — resumo de todos os meses registrados com compartilhamento
- **Estatísticas** — gráficos de evolução, média anual, sequências e exportação PDF
- **Configurações** — percentual de presença, sábados como dias úteis, backup/restauração JSON
- **Ausências** — registro de férias, day off, licenças e ausências com desconto automático na meta mensal
- **Notificações** — lembrete às 18h em dias úteis (se ainda não confirmou presença) e aviso de check-in automático
- **Widget** — exibe "Faltam X dias presenciais" diretamente na tela inicial
- **Sobre** — informações da versão 1.0.3, política de privacidade (dados locais) e link para o desenvolvedor
- **Tema claro/escuro** — Material Design 3 com suporte a Dynamic Color (Material You)

## Tecnologias

| Camada | Stack |
|--------|-------|
| UI | Kotlin, Jetpack Compose, Material 3, Navigation Compose, Lottie |
| Arquitetura | MVVM, Clean Architecture, Repository Pattern |
| DI | Hilt |
| Dados | Room, DataStore Preferences |
| Async | Kotlin Coroutines + Flow |
| Background | WorkManager |
| Localização | Google Play Services Location (Geofencing) |
| Permissões | Accompanist Permissions |
| Widget | Glance AppWidget |
| Datas | `java.time` (API 26+) |

## Arquitetura

```
app/src/main/java/com/presencial/app/
├── data/           # Implementações de repositório, Room, DataStore, backup, PDF
├── domain/         # Modelos, interfaces de repositório, use cases, utilitários
├── presentation/   # ViewModels e telas Compose por feature
├── ui/             # Tema Material 3 e componentes reutilizáveis
├── di/             # Módulos Hilt e injeção de Dispatchers
├── notification/   # Canal e agendamento de lembretes
├── widget/         # Widget Glance
└── worker/         # Worker de notificação diária e processos em background
```

### Fluxo de dados

```
UI (Compose) → ViewModel → UseCase → Repository → Room / DataStore / Remote
                              ↑
                         Domain Models
```

### Regras de negócio principais

1. **Meta mensal:** `ceil(dias_úteis_líquidos × percentual / 100)`
2. **Dias úteis líquidos:** exclui domingos, feriados nacionais, sábados (configurável) e períodos de ausência registrados (férias, licenças, etc.)
3. **Feriados móveis:** calculados a partir da Páscoa (algoritmo de Meeus/Jones/Butcher)
4. **Geofencing:** check-in automático via dwell de 30 s dentro do raio configurado; geofences restauradas no boot e na inicialização do app. A localização é definida por geocoding do endereço ou GPS.

## Como executar

### Pré-requisitos

- Android Studio Meerkat (2024.3+) ou mais recente
- JDK 17
- Android SDK 37 (Target)
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

Testes unitários abrangentes na camada de domínio e dados:

- `WorkdayCalculatorTest` — lógica de cálculo de dias úteis
- `GoalCalculatorTest` — cálculos de meta e percentuais
- `HolidayCalculatorTest` — feriados nacionais e móveis
- `SettingsDataStoreTest` — persistência de configurações
- `BackupManagerTest` — integridade de exportação/importação

```bash
./gradlew test
```

## Decisões de design

| Decisão | Motivo |
|---------|--------|
| DataStore para settings, Room para check-ins | Settings são pequenas e tipadas; check-ins precisam de queries relacionais |
| Feriados calculados localmente | Offline-first, sem dependência de APIs externas |
| WorkManager para notificações | Respeita Doze mode e garante execução persistente |
| Dispatchers injetados | Facilita testes unitários substituindo o IO/Default |
| Glance para widget | API moderna e declarativa alinhada com Compose |
| `java.time` nativo | minSdk 26 elimina a necessidade de bibliotecas legadas |

## Estrutura de entidades Room

- **CheckIn** — `dateEpochDay`, `status`, `updatedAt`, `source` (`MANUAL` ou `auto_geofence`), `workAddressId`
- **Absence** — `id`, `type`, `startDate`, `endDate`, `notes` (períodos de afastamento)
- **WorkAddress** — `id`, `name`, `addressText`, `latitude`, `longitude`, `radius` (padrão 50 m), `isActive`
- **MonthlySummary** — agregado mensal cacheado para histórico rápido

## Licença

Projeto desenvolvido por Márcia Cristina.
Uso livre para fins de estudo e demonstração.

---
GitHub: [https://github.com/marciacrisrs/presencial-app](https://github.com/marciacrisrs/presencial-app)
