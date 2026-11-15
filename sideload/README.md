# Signeringsnøgle til selvinstallation

`hyggespil.p12` er appens **faste sideload-nøgle**. Den ligger i repoet med vilje:

- alle CI-bygninger får dermed **samme signatur**, så Android kan opdatere appen oven på den forrige,
- uden den ville hver bygning få en ny debug-nøgle, og telefonen ville afvise opdateringen med
  «signaturkonflikt» (det var årsagen til Obtainiums `failureConflict`).

Nøglen er **kun** til APK'er, der hentes her fra repoet.

## Til Google Play

Play kræver en **egen upload-nøgle**, som aldrig må ligge i repoet. Læg den i secrets:

| Secret | Indhold |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | base64 af din `.jks`/`.p12` |
| `ANDROID_KEYSTORE_PASSWORD` | nøglens adgangskode |
| `ANDROID_KEY_ALIAS` | alias |
| `ANDROID_KEY_PASSWORD` | nøgleadgangskode |

Er de sat, bruger Gradle dem i stedet for sideload-nøglen. Se [../docs/play-store.md](../docs/play-store.md).

> Skifter du sideload-nøglen, skal appen **afinstalleres** på telefonerne én gang, før den nye kan installeres.
