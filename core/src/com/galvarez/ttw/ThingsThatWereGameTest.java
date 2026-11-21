package com.galvarez.ttw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;

public class ThingsThatWereGameTest extends ThingsThatWereGame {

  private int framesRendered = 0;
  private final int expectedWidth;
  private final int expectedHeight;
  private Throwable startupError;
  private Throwable renderError;
  private int glErrorCount = 0;

  private static final int SMOKE_TEST_FRAMES = 60;
  private static final String SMOKE_TEST_SCREENSHOT = "smoke-test-screenshot.png";

  public ThingsThatWereGameTest(int width, int height) {
    super(width, height);
    this.expectedWidth = width;
    this.expectedHeight = height;
  }

  @Override
  public void create() {
    try {
      super.create();
    } catch (Throwable t) {
      startupError = t;
      t.printStackTrace();
    }
  }

  @Override
  public void render() {
    framesRendered++;
    try {
      super.render();
    } catch (Throwable t) {
      renderError = t;
      t.printStackTrace();
    }
    if (Gdx.gl.glGetError() != GL20.GL_NO_ERROR) {
      glErrorCount++;
    }
    if (framesRendered >= SMOKE_TEST_FRAMES) {
      finishSmokeTest();
      return;
    }
  }

  private void finishSmokeTest() {
    StringBuilder errors = new StringBuilder();

    if (startupError != null) {
      errors.append("Startup error: ").append(startupError.getMessage()).append("\n");
    }

    if (renderError != null) {
      errors.append("Render error: ").append(renderError.getMessage()).append("\n");
    }

    if (glErrorCount > 0) {
      errors.append("OpenGL errors detected: ").append(glErrorCount).append("\n");
    }

    if (Gdx.graphics.getWidth() != expectedWidth || Gdx.graphics.getHeight() != expectedHeight) {
      errors.append("Window size mismatch: expected ").append(expectedWidth).append("x").append(expectedHeight)
          .append(" got ").append(Gdx.graphics.getWidth()).append("x").append(Gdx.graphics.getHeight()).append("\n");
    }

    if (assets == null) {
      errors.append("Assets failed to load\n");
    } else {
      if (assets.getSkin() == null) {
        errors.append("UI skin failed to load\n");
      }
      if (assets.getFont(16) == null) {
        errors.append("Font failed to load\n");
      }
    }

    if (getScreen() == null) {
      errors.append("No screen initialized\n");
    }

    try {
      int width = Gdx.graphics.getWidth();
      int height = Gdx.graphics.getHeight();

      Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
      Gdx.gl.glReadPixels(0, 0, width, height, Gdx.gl.GL_RGBA, Gdx.gl.GL_UNSIGNED_BYTE, pixmap.getPixels());
      java.nio.ByteBuffer pixels = pixmap.getPixels();
      pixels.rewind();
      int nonBlackPixels = 0;
      int pixelCount = pixels.remaining() / 4;
      for (int i = 0; i < pixelCount; i++) {
        byte r = pixels.get();
        byte g = pixels.get();
        byte b = pixels.get();
        pixels.get();
        if (r != 0 || g != 0 || b != 0) {
          nonBlackPixels++;
        }
      }

      PixmapIO.writePNG(Gdx.files.local(SMOKE_TEST_SCREENSHOT), pixmap);
      pixmap.dispose();

      if (nonBlackPixels == 0) {
        errors.append("Screenshot is entirely black\n");
      } else {
        System.out.println("Smoke test screenshot saved: " + SMOKE_TEST_SCREENSHOT + " (" + nonBlackPixels + " non-black pixels)");
      }
    } catch (Throwable t) {
      errors.append("Screenshot capture failed: ").append(t.getMessage()).append("\n");
    }

    boolean passed = errors.length() == 0;
    if (!passed) {
      System.err.println("SMOKE TEST FAILED:");
      System.err.println(errors.toString());
      Gdx.app.error("SmokeTest", errors.toString());
    } else {
      System.out.println("SMOKE TEST PASSED: All checks passed, screenshot saved.");
    }

    try {
      java.nio.file.Files.writeString(
        java.nio.file.Paths.get("smoke-test-result.txt"),
        passed ? "PASS" : "FAIL\n" + errors.toString()
      );
    } catch (Exception ignored) {
    }

    Gdx.app.exit();
  }
}
