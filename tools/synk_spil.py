"""Kopierer spillets filer ind i Android-appens assets.

Bruges både lokalt og i CI, så APK'en altid indeholder den nyeste version af spillet.

Kør:
    python tools/synk_spil.py [sti-til-hyggespil-repo]
"""
import os
import shutil
import sys
import json

ROD = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SPIL = sys.argv[1] if len(sys.argv) > 1 else os.path.join(ROD, "..", "hyggespil-hacs")
KILDE = os.path.join(SPIL, "custom_components", "hyggespil", "www")
MÅL = os.path.join(ROD, "app", "src", "main", "assets", "www")

# Filer der hører til udvikleren eller brugeren og aldrig må med i en APK
UNDGÅ = {"extra.json", "ledger.json", "verify_state.json", "extra_pending.json",
         "hs-user-pools.js", "backup.js.local", "github.json", "secrets.json"}

if not os.path.isdir(KILDE):
    raise SystemExit(f"finder ikke spillets filer i {KILDE}")

shutil.rmtree(MÅL, ignore_errors=True)
os.makedirs(MÅL, exist_ok=True)

antal = 0
for base, mapper, filer in os.walk(KILDE):
    mapper[:] = [m for m in mapper if m not in {"__pycache__"}]
    for f in filer:
        if f in UNDGÅ or f.endswith(".pyc") or f.endswith(".bak"):
            continue
        src = os.path.join(base, f)
        rel = os.path.relpath(src, KILDE)
        dst = os.path.join(MÅL, rel)
        os.makedirs(os.path.dirname(dst), exist_ok=True)
        shutil.copy2(src, dst)
        antal += 1

# Spillet henter extra.json ved opstart; uden filen får man en 404 i loggen
with open(os.path.join(MÅL, "extra.json"), "w", encoding="utf-8") as h:
    json.dump({}, h)

# Appens egne flag: Android-appen kører lokalt og er Play-udgaven, så spillet
# skjuler Home Assistant- og Music Assistant-valg og links til køb uden for butikken.
with open(os.path.join(MÅL, "hs-app.js"), "w", encoding="utf-8") as h:
    h.write("window.HSA = { android: true, play: true };\n")

version = json.load(open(os.path.join(SPIL, "custom_components", "hyggespil", "manifest.json"),
                        encoding="utf-8"))["version"]
with open(os.path.join(ROD, "version.txt"), "w", encoding="utf-8") as h:
    h.write(version + "\n")

print(f"kopierede {antal} filer fra {KILDE} til assets/www (spilversion {version})")
