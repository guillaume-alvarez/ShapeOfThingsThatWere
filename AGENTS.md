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
