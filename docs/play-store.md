# Google Play — sådan kommer appen derop

Appen er bygget, så den kan publiceres uden omskrivning. AAB-filen bygges ved hver release
(`app-release.aab` i artifact og på udgivelsen).

## 1. Signeringsnøgle

Play kræver en fast nøgle. Lav den én gang og gem den sikkert:

```bash
keytool -genkeypair -v -keystore hyggespil.jks -alias hyggespil \
  -keyalg RSA -keysize 4096 -validity 10000
```

Læg den i repoets secrets som:

| Secret | Indhold |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | `base64 -w0 hyggespil.jks` |
| `ANDROID_KEYSTORE_PASSWORD` | nøglens adgangskode |
| `ANDROID_KEY_ALIAS` | `hyggespil` |
| `ANDROID_KEY_PASSWORD` | nøgleadgangskoden |

Uden secrets signeres release-APK'en med debug-nøglen, så den stadig kan installeres lokalt — men
den kan **ikke** uploades til Play.

> Play bruger «Play App Signing»: den nøgle, du uploader med, er kun upload-nøglen. Gem den alligevel
> — uden den kan du ikke opdatere appen.

## 2. App-identitet

- **Package:** `dk.allanpersson.hyggespil`
- **versionName:** spillets version (fx `0.2.43`)
- **versionCode:** `major*10000 + minor*100 + patch` (fx `20043`), så hver udgivelse kan opdateres

## 3. Butiksmetadata

`fastlane/metadata/android/da-DK/` indeholder titel, kort og lang beskrivelse samt changelogs.
Tilføj selv:

- **Ikon:** 512×512 PNG (32-bit)
- **Feature-grafik:** 1024×500 PNG
- **Skærmbilleder:** mindst 2 i telefonformat (tag dem fra appen)
- **Privatlivspolitik:** appen indsamler **ingen** data og har ingen analyse eller reklamer.
  Læg en kort erklæring op (fx på GitHub Pages) og indsæt linket i Play Console.
- **Indholdsdeklaration:** «Spil», ikke rettet mod børn under 13 (der er voksenindhold bag 🔞).
- **Data safety:** ingen indsamling, ingen deling, ingen sporing.

## 4. Upload

Play Console → **Test → Intern test** først:

1. Opret app med package `dk.allanpersson.hyggespil`.
2. Upload `hyggespil-x.y.z.aab` fra den seneste release.
3. Udfyld butiksprofilen og data safety.
4. Send til gennemsyn.

Ved nye spilversioner: hent den nye AAB fra releasen og upload igen — versionCode stiger automatisk.
