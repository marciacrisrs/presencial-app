# Changelog

All notable changes to Presencial are documented in this file.

## [Unreleased]

### Added
- Sincronização na nuvem (Google Drive / OneDrive / Dropbox) via pasta SAF em Configurações
- Documento de pesquisa comparativa em `docs/cloud-sync-research.md`
- Feriados estaduais e municipais com base no local de trabalho (issue #7)
- Geocoding reverso para persistir UF e cidade ao salvar endereço
- Catálogo offline `regional_holidays.json` para feriados regionais
- Snackbar e animação no check-in da home (issue #16)
- Acessibilidade no calendário: descrição por dia e ícone de presencial

### Changed
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
