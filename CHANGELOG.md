# Changelog

## [0.1.1] - 2025-02-13

### Added
- Centrifugo token-based authentication (connection + subscription tokens)
- Device capability advertising при линковке (triggers & actions)
- Новые API endpoints: `getCentrifugoToken()`, `linkDevice()`
- Smart WebSocket URL derivation (HTTP→WS, domain-aware routing)
- Полная локализация UI — все строки вынесены в `strings.xml` (75+ ресурсов)
- DTO: `CentrifugoTokenRequest`, `CentrifugoTokenResponse`
- Документация проекта (`CLAUDE.md`, `README.md`, `docs/`)

### Changed
- Стандартизация именования событий: `device.*` → `android.trigger.*` / `android.action.*`
- API пути упрощены: убран префикс `mobile-api/`
- Server URL по умолчанию: `http://api.agimate.lc/` → `https://api.agimate.io`
- Модели данных используют `@StringRes` вместо захардкоженных строк
- Порог ShakeTrigger увеличен: 800 → 2500
- ActionType теперь использует `actionName` + `fromActionName()` для серверных имён

### Fixed
- Корректная конвертация HTTP/HTTPS URL в WS/WSS для Centrifugo
