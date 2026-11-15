# 🎲 Hyggespil til Android

Den officielle Android-app til [Hyggespil](https://github.com/allanpersson/hyggespil) — de danske
selskabsspil til Home Assistant.

App'en er en let WebView-container omkring spillet og indeholder **spillets filer direkte i APK'en**,
så den kan spilles uden forbindelse til Home Assistant. Den bygges automatisk ved hver ny udgivelse af
spillet, så Android-versionen altid matcher.

## Hent og installer

1. Åbn [Releases](https://github.com/allanpersson/hyggespil-android/releases/latest).
2. Hent `hyggespil-x.y.z.apk`.
3. Åbn filen på telefonen og tillad «installér fra denne kilde», hvis Android spørger.

> App'en ligger kun her til **selvinstallation**. Den er bygget så den også kan lægges på Google Play
> senere (AAB bygges ved hver release) — se [docs/play-store.md](docs/play-store.md).

## Hvad virker

- Alle spil, puljer og opdateringer fra hovedprojektet
- Dansk oplæsning via telefonens egen **tekst-til-tale** (en bro til Androids TTS, da WebView ikke har stemmer)
- Musik, bip og spil-logik som i browseren
- Kaffe-link og eksterne links åbner i telefonens browser

Home Assistant-funktioner (lys-automation, fælles rum, sensor) kræver, at spillet kører i Home
Assistants panel. App'en her er den **lokale** udgave til telefon og tablet.

## Byg selv

```bash
python tools/synk_spil.py ../hyggespil-hacs   # kopiér spillets filer ind i assets
gradle assembleRelease -PversionName=0.2.43   # APK
gradle bundleRelease -PversionName=0.2.43     # AAB til Play
```

CI gør det samme automatisk (`.github/workflows/apk.yml`) og lægger begge filer på en release.

## Links

- **Spillet:** https://github.com/allanpersson/hyggespil
- **Hovedprojektets README:** https://github.com/allanpersson/hyggespil#readme
- **Fejl og ønsker:** https://github.com/allanpersson/hyggespil/issues

Licens: MIT · Udvikler: Allan Persson
