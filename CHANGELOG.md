# Changelog

All notable changes to Presencial are documented in this file.

## [Unreleased]

### Added
- Políticas de presença configuráveis (issue #5): percentual livre, dias fixos, semanas alternadas
- Check-in automático por geofence com sync após boot e restore de backup
- Mapa OSM (Leaflet) para seleção de endereço de trabalho
- Mensagens inteligentes no dashboard (motor local + OpenAI opcional)
- Widget informativo com atualização automática após check-in, boot e configurações
- Banner de status de monitoramento em Dashboard, Configurações e Locais
- Chave OpenAI armazenada com EncryptedSharedPreferences
- Versionamento automático no workflow de release
- CI: build release + upload APK debug + quality gate Sonar

### Changed
- Backup v3 inclui política de presença, endereços de trabalho e origem do check-in
- Política de privacidade atualizada (geocoding, OSM, OpenAI)
- Textos de IA, monitoramento e política externalizados em strings.xml

### Fixed
- Geofence receiver com `goAsync()` e handlers extraídos para testes
- Cobertura Kover/SonarCloud alinhada (~97%)
