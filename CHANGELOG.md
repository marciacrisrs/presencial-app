# Changelog

All notable changes to Presencial are documented in this file.

## [Unreleased]

### Added
- Feriados estaduais e municipais com base no local de trabalho (issue #7)
- Geocoding reverso para persistir UF e cidade ao salvar endereço
- Catálogo offline `regional_holidays.json` para feriados regionais

### Changed
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
- Geofence receiver com `goAsync()` e handlers extraídos para testes
- Cobertura Kover/SonarCloud alinhada (~97%)
