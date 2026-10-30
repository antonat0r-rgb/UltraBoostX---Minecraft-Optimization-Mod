# UltraBoost X 0.4.1-dev

Client-side NeoForge project targeting Minecraft 26.3 and Java 25.

> **Honest status:** this is an early development/diagnostic build, not yet a proven FPS booster. It measures frame pacing and heap use, and contains a standalone chunk-priority planner that is not connected to Minecraft's chunk renderer. Do not expect an FPS increase from this version.

## Build a JAR on GitHub (no BAT, Java, or Gradle installation on your PC)

1. Sign in at https://github.com and create a new repository. A private repository is fine.
2. Extract this ZIP on your PC.
3. Open the new repository page, choose **Add file → Upload files**, and upload the *contents* of this folder (including the hidden `.github` folder).
4. Commit the files to the `main` branch.
5. Open the repository's **Actions** tab and select **Build UltraBoost X**.
6. If the workflow has not started automatically, choose **Run workflow**.
7. Wait for the run to finish with a green check. Open the run and download **UltraBoostX-0.4.1-jar** under **Artifacts**.
8. Extract the downloaded artifact ZIP. The `.jar` inside goes into your Minecraft `mods` folder for a separate NeoForge 26.3 profile.

GitHub Actions builds the JAR on its own cloud runner using Java 25 and Gradle 9.8.2. The source ZIP itself is not a mod and must not be placed in `mods`.

## In-game diagnostics

- `/ubx` — status and optional Sodium detection
- `/ubx report` — latest frame-time/heap report
- `/ubx reset` — reset profiler samples
- `/ubx on` and `/ubx off` — toggle profiling

Reports are also written to `logs/latest.log` approximately every five seconds.

## Verification status

The dependency-free classes have standalone self-tests in `tools/`. A successful GitHub workflow confirms that Gradle produced a JAR, but it does not replace launching Minecraft and testing the mod in-game. Sodium compatibility and FPS gains are not yet verified.
