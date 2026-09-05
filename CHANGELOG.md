# Changelog

All notable changes to Presencial are documented in this file.

## [Unreleased]

## [1.0.21] - 2026-09-05

### Added
- Navegação primária com 4 seções; Estatísticas abre a partir do Histórico (issue #66)
- Descrições de acessibilidade para voltar, adicionar, compartilhar e navegar o calendário (issue #78)
- Documentação de investigação de crash no fluxo de release (issue #77)
- Secret `GOOGLE_SERVICES_JSON` no workflow de release para ligar o Crashlytics no AAB

### Changed
- Saudação de onboarding neutra (“Boas-vindas ao Presencial”)
- Onboarding e Início ocupam melhor a tela conforme a altura
- Concluir disponível em qualquer passo do onboarding

### Fixed
- Crash ao tocar na notificação de presença (issue #132)
- Check-in automático que não disparava no dia seguinte (issue #133)
- Toque no lembrete fora da Home que podia não abrir o check-in (issue #132)
- Card “Esqueceu de ontem?” no dia 1 mesmo com check-in no mês anterior
- Check-in automático em dia de férias, day off ou home office já registrado
- Histórico vazio que mostrava skeleton em vez de estado vazio (issue #78)

### Added
- Onboarding inicial de meta, lembrete das 18h e local opcional (issue #60)
- Exportação de relatório mensal em CSV e Excel (.xlsx) na tela de estatísticas (issue #11)
- Dashboard analítico: gráfico semanal, linha de evolução, resumo anual e heatmap anual (issue #12)
- Integração Firebase Crashlytics (opt-in via `app/google-services.json`) (issue #13)
- Job de testes instrumentados no GitHub Actions (issue #13)
- Onboarding de localização na tela de endereços de trabalho (issue #15)
- Configuração e documentação do CodeRabbit (issue #21)
- Sincronização na nuvem (Google Drive / OneDrive / Dropbox) via pasta SAF em Configurações
- Documento de pesquisa comparativa em `docs/cloud-sync-research.md`
- Feriados estaduais e municipais com base no local de trabalho (issue #7)
- Geocoding reverso para persistir UF e cidade ao salvar endereço
- Catálogo offline `regional_holidays.json` para feriados regionais
- Snackbar e animação no check-in da home (issue #16)
- Acessibilidade no calendário: descrição por dia e ícone de presencial

### Changed
- Backup em pasta via SAF, com confirmação antes de restaurar; permissão revogada não apaga o último sync (issue #62)
- Backup JSON v4 inclui ausências; importação valida versões suportadas (3 e 4)
- Home com scroll vertical para evitar clipping em telas pequenas
- Botão de check-in com labels e contentDescription para TalkBack
- CI executa `koverVerify` após testes unitários
- Backfill de geocoding só roda quando há endereços pendentes
- Regras ProGuard para Hilt, Room, workers e receivers
- Políticas de presença configuráveis (issue #5): percentual livre, dias fixos, semanas alternadas
- Check-in automático por geofence com sync após boot e restore de backup
- Mapa OSM (Leaflet) para seleção de endereço de trabalho
- Mensagens contextuais no dashboard via `SmartMessageEngine` (templates locais, offline)
- Widget informativo com atualização automática após check-in, boot e configurações
- Banner de status de monitoramento em Dashboard, Configurações e Locais
- Versionamento automático no workflow de release
- CI: build release + upload APK debug + quality gate Sonar

### Changed
- Backup v3 inclui política de presença, endereços de trabalho e origem do check-in
- Política de privacidade atualizada (geocoding, OSM, mensagens locais)
- Textos de monitoramento e política externalizados em strings.xml
- Removida integração OpenAI; chaves legadas são apagadas na inicialização

### Removed
- Integração OpenAI e armazenamento de chave de API

### Fixed
- Permissão de notificações solicitada no Android 13+; lembretes das 18h respeitam permissão concedida
- Acessibilidade: gráfico mensal, exportação PDF e logotipo na tela Sobre
- WebView do mapa restringe URLs a assets locais e esquema `presencial://location`
- Teste instrumentado e README alinhados com política de privacidade atual
- Cobertura Kover/SonarCloud alinhada (~97%)
