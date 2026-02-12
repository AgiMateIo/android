# Agimate Android

Android agent with trigger monitoring and action execution via Centrifugo WebSocket.

## Architecture

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Koin** for dependency injection
- **Retrofit + OkHttp** for HTTP
- **centrifuge-java** for Centrifugo WebSocket
- **Room + DataStore** for local storage
- **Foreground Services** for trigger monitoring and action execution

## Naming Convention

Trigger and action names follow a unified naming scheme across all Agimate platforms:

```
{platform}.trigger.{plugin}.{event}
{platform}.action.{plugin}.{verb}
```

- **platform** — `desktop`, `android`, `ios`, etc.
- **trigger/action** — fixed literal
- **plugin** — plugin or module name (e.g. `wifi`, `tts`, `notification`)
- **event/verb** — specific event or action (e.g. `connected`, `speak`, `show`)

### Android Triggers

| Name | Params |
|------|--------|
| `android.trigger.call.incoming` | phoneNumber, timestamp |
| `android.trigger.battery.low` | batteryLevel, threshold, timestamp |
| `android.trigger.wifi.connected` | ssid, bssid, connected, signalStrength, timestamp |
| `android.trigger.wifi.disconnected` | ssid, bssid, connected, signalStrength, timestamp |
| `android.trigger.shake.detected` | acceleration, x, y, z, timestamp |

### Android Actions

| Name | Params |
|------|--------|
| `android.action.notification.show` | title, message |
| `android.action.tts.speak` | text |

## Server Communication

### Endpoints

| Purpose | Method | Path |
|---------|--------|------|
| Link device | POST | `/device/registration/link` |
| Send trigger | POST | `/device/trigger/new` |
| Centrifugo token | POST | `/device/centrifugo/token` |
| WebSocket | WS | `/connection/websocket` |

### Auth Header

```
X-Device-Auth-Key: {deviceKey}
```

### Connection Flow

1. **Link device** — POST `/device/registration/link` with `{deviceId, deviceName, deviceOs, triggers, actions}`
2. **Fetch Centrifugo tokens** — POST `/device/centrifugo/token` with `{deviceId}`, returns `{connectionToken, subscriptionToken, channel, wsUrl?}`
3. **Connect WebSocket** — using connection token, subscribe to server-provided channel with subscription token
4. **Receive actions** — actions arrive as JSON `{type, parameters}` via Centrifugo publications

### WebSocket URL Derivation

If server doesn't provide `wsUrl` in token response:
- Multi-level domain: replace first subdomain with `centrifugo`, use `wss://` (e.g. `api.agimate.io` -> `wss://centrifugo.agimate.io/connection/websocket`)
- Single-level host: keep host, match scheme (e.g. `http://localhost:8080` -> `ws://localhost:8080/connection/websocket`)

## Configuration

Default server: `https://api.agimate.io`

Settings are stored in DataStore:
- Device Key
- Server URL
- Device ID (auto-generated)
- Debug Logging toggle

## Build

```bash
./gradlew assembleDebug
```
