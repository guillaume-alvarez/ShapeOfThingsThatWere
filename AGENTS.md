# Agent Guidelines

## Design

`doc/DESIGN.md` is the reference for all design decisions. Changes in code must always be consistent with its content.

If you want to change the design, ask the user and suggest changes — never do it yourself.

## Test

Always run tests locally before pushing any change on GitHub, or asking to test any change.

### Running tests

```bash
gradlew test    # Linux/macOS
gradlew.bat test   # Windows
```

### Testing the UI result

The game includes a smoke test that verifies the UI renders correctly without crashing.

**Locally (Windows):**

```bash
gradlew.bat :desktop:run --args='--smoke-test'
```

**Locally (Linux/macOS):**

```bash
./gradlew :desktop:run --args='--smoke-test'
```

**Headless CI (Linux):** Requires a virtual display (Xvfb) and Mesa software rendering:

```bash
xvfb-run --auto-servernum --server-args="-screen 0 1024x768x24" ./gradlew :desktop:run --args='--smoke-test'
```

The smoke test renders 60 frames, checks for startup/render/OpenGL errors, verifies assets (skin, font) load, confirms a screen is initialized, captures a screenshot, and ensures it is not entirely black. It writes `smoke-test-result.txt` with `PASS` or `FAIL` and saves `smoke-test-screenshot.png`.

The CI runs this under Xvfb on Ubuntu. See `.github/workflows/ci.yml` (smoke-test job) for the exact environment setup (Xvfb, libgl1, mesa-dri).

### Headless play test (advance N turns programmatically)

The desktop module includes `HeadlessPlayTest` (`desktop/src/com/galvarez/ttw/desktop/HeadlessPlayTest.java`), a headless harness that launches the game, dismisses the tutorial, advances turns by invoking `OverworldScreen.processTurn()` directly (bypassing the `canFinishTurn()` guard on `endTurn()`), and exports a screenshot + game state at a target turn.

**Run it:**

```bash
# Windows
./gradlew.bat :desktop:run -PmainClass=com.galvarez.ttw.desktop.HeadlessPlayTest

# Linux/macOS
./gradlew :desktop:run -PmainClass=com.galvarez.ttw.desktop.HeadlessPlayTest
```

Or set `mainClassName` temporarily in `desktop/build.gradle`:

```groovy
project.ext.mainClassName = "com.galvarez.ttw.desktop.HeadlessPlayTest"
```

then run `./gradlew.bat :desktop:run` and revert `mainClassName` afterward.

**Outputs:**
- `desktop/headless-playtest-state.txt` — turn number, year, empire count, notification count, etc.
- `desktop/headless-playtest-turn12.png` — rendered screenshot at the target turn.

Adjust `TARGET_TURN` in the source to play more or fewer turns.
