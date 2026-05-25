# PulseClient

Meteor ve Wurst hilelerinin birleşiminden oluşan özgün ve güçlü bir Minecraft hile client'ı.

## Özellikler
- 320+ Port edilmiş modül.
- Meteor ve Wurst'ün en iyi özelliklerini birleştiren `PulseKillAura` ve `PulseFlight`.
- Fabric tabanlı Mixin mimarisi.
- Hafif ve hızlı modül yönetim sistemi.

## Build Alma (Nasıl Build Alınır?)

PulseClient'ı build etmek için Gradle kullanılır. Terminalde projenin kök dizininde aşağıdaki komutu çalıştırın:

**Windows için:**
```cmd
gradlew.bat build
```

**Linux/Mac için:**
```bash
./gradlew build
```

Build bittikten sonra oluşan `.jar` dosyası şurada bulunacaktır:
`build/libs/pulse-client-1.0.0.jar`

## Kurulum
1. [Fabric Loader](https://fabricmc.net/use/installer/)'ı kurun.
2. Build aldığınız `pulse-client-1.0.0.jar` dosyasını `.minecraft/mods` klasörüne kopyalayın.
3. Minecraft'ı Fabric profili ile başlatın.

## Geliştiriciler İçin
Modüller `com.pulseclient.modules` paketi altındadır. Yeni bir modül eklemek için `Module` sınıfını inherit etmeniz ve `ModuleManager`'a kaydetmeniz yeterlidir.
