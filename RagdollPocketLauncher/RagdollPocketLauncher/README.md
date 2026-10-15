# Ragdoll Pocket Launcher (Android Studio project)

Pick your own game archive -> extract -> find the .exe -> hand off to Winlator/GameHub. Use only a copy you legitimately own.

## Build
1. Android Studio Koala (2024.1) or newer, JDK 17. File > Open this folder; let Gradle sync (it downloads Gradle 8.7; no wrapper jar is included).
2. Build > Build APK(s), or run on your phone with USB debugging.
   CLI alternative: install Gradle 8.7, run `gradle wrapper` once, then `./gradlew assembleDebug`.

## Not built or tested
This project was written without an Android SDK, so it has not been compiled or run. Expect to fix small compile errors.

## Known limits
- RAR5 is not supported (junrar reads RAR4 only). The app detects RAR5 and tells you; re-pack as ZIP/RAR4 or extract with ZArchiver.
- No documented Winlator/GameHub intent exists to run an .exe directly. The app copies the exe path and opens the runtime; you add/run it there.
- Android can't inject touches into another app. The control layout here is a preview plus an EXPERIMENTAL Winlator profile export; the JSON schema is from memory and may not import. Otherwise recreate the layout in Winlator's Input Controls editor (stick -> WASD, trackpad -> mouse move, buttons -> left click / Space / R).
- Huawei P30 (Kirin 980, Mali-G76, no Google services): Winlator targets Adreno GPUs best, so expect poor or no performance on Mali. Try 640x480, Box64 presets set to "performance", and VirGL/software rendering.
- Extraction needs free space for the archive copy plus the extracted files.
