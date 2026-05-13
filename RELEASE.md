# Oskolki - Release Build Guide

## Сборка APK

### Release APK (подписан)
```bash
./gradlew assembleRelease
```
Результат: `app/build/outputs/apk/release/app-release.apk`

### Debug APK (не подписан)
```bash
./gradlew assembleDebug
```
Результат: `app/build/outputs/apk/debug/app-debug.apk`

## Установка на устройство

### Debug APK (Android 11+)
1. Включить установку из неизвестных источников: Настройки → Безопасность
2. Скопировать APK на устройство
3. Открыть файл и установить

### Release APK (все версии)
Обычно устанавливается без проблем (подписан).

## keystore

Файл `keystore.properties` содержит настройки подписи и **не хранится в Git** (в `.gitignore`).

Если нужно восстановить подписывание после чистой клонировки:
1. Скопировать `keystore.properties` с другого устройства
2. Или создать новый keystore:
```bash
keytool -genkey -v \
    -keystore keystore/oskolki.keystore \
    -alias oskolki \
    -keypass oskolki123 \
    -storepass oskolki123 \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -dname "CN=Oskolki, OU=Development, O=Oskolki, L=Moscow, ST=Moscow, C=RU"
```

2. Создать `keystore.properties` с теми же паролями.

## Versioning

В `build.gradle.kts`:
```kotlin
versionCode = 1  // Integer - увеличивать при каждом релизе
versionName = "1.0"  // String - версия для пользователей
```

При релизе увеличивайте versionCode:

```kotlin
defaultConfig {
    versionCode = 2  // Было 1
    versionName = "1.1"
}
```
