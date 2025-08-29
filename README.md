# LibreXrayVPN

## English
LibreXrayVPN is an open-source Android VPN client based on **Xray core** ([AndroidLibXrayLite](https://github.com/2dust/AndroidLibXrayLite)).  
The project is built with a modern technology stack and inspired by [v2rayNG](https://github.com/2dust/v2rayNG).

### Tech stack
- **Kotlin**, **Coroutines / Flow**
- **Jetpack Compose** (UI)
- **MVI architecture**
- **Hilt** (dependency injection)
- **MMKV** (local storage)
- **ML Kit** (QR code scanning)
- **Testing**: JUnit, MockK, Espresso

### Features
- Import configs via **QR code** and **clipboard**
- Connection control via **persistent foreground notification**
- Latency testing for servers
- Real-time traffic display
- VPN tunnel management through **VpnService**
- Light and dark themes, RU/EN localization **(UI in progress)**

### Native dependencies
The project requires additional native libraries that are **not stored in Git**:  

- `libs/libv2ray.aar` and `libs/libv2ray-sources.jar`  
  → download from [AndroidLibXrayLite](https://github.com/2dust/AndroidLibXrayLite)

- `framework/libs/<ABI>/libtun2socks.so`  
  (where `<ABI>` is the target architecture: `arm64-v8a`, `armeabi-v7a`, `x86_64`, …)  
  → can be obtained from LibreXrayVPN [Releases](../../releases) or reused from other VPN clients that bundle tun2socks

### License
This project is licensed under **GPL-3.0-or-later**.  
See [LICENSE](LICENSE) and [NOTICE](NOTICE) for details.

---

## Русский
LibreXrayVPN — Android VPN-клиент с открытым исходным кодом на основе **Xray core** ([AndroidLibXrayLite](https://github.com/2dust/AndroidLibXrayLite)).  
Проект создан с упором на современный стек технологий и вдохновлён [v2rayNG](https://github.com/2dust/v2rayNG).

### Технологии
- **Kotlin**, **Coroutines / Flow**
- **Jetpack Compose** (UI)
- **MVI архитектура**
- **Hilt** (внедрение зависимостей)
- **MMKV** (локальное хранилище)
- **ML Kit** (сканирование QR-кодов)
- **Тестирование**: JUnit, MockK, Espresso

### Возможности
- Импорт конфигураций через **QR-код** и **буфер обмена**
- Управление подключением через **постоянное foreground-уведомление**
- Тестирование задержки серверов
- Отображение трафика в реальном времени
- Управление VPN-туннелем через **VpnService**
- Светлая и тёмная темы, локализация RU/EN **(UI в разработке)**

### Нативные зависимости
Проект требует дополнительные нативные библиотеки, которые **не хранятся в Git**:  

- `libs/libv2ray.aar` и `libs/libv2ray-sources.jar`  
  → скачать из [AndroidLibXrayLite](https://github.com/2dust/AndroidLibXrayLite)

- `framework/libs/<ABI>/libtun2socks.so`  
  (где `<ABI>` — целевая архитектура: `arm64-v8a`, `armeabi-v7a`, `x86_64`, …)  
  → можно найти в [Releases](../../releases) LibreXrayVPN или использовать из других VPN-клиентов, включающих tun2socks

### Лицензия
Проект распространяется по лицензии **GPL-3.0-or-later**.  
Подробнее см. [LICENSE](LICENSE) и [NOTICE](NOTICE).
