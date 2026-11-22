package com.galvarez.ttw.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.galvarez.ttw.ThingsThatWereGame;
import com.galvarez.ttw.screens.TutorialMenuScreen;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HeadlessPlayTest extends ThingsThatWereGame {

  private static final int TARGET_TURN = 12;
  private static final int WIDTH = 1024;
  private static final int HEIGHT = 800;

  private static final String OUT_DIR = "out/test";
  private static final String STATE_FILE = OUT_DIR + "/headless-playtest-state.txt";
  private static final String SCREENSHOT_FILE = OUT_DIR + "/headless-playtest-turn" + TARGET_TURN + ".png";

  private int frames = 0;
  private boolean overworldReady = false;
  private boolean finished = false;

  public HeadlessPlayTest() {
    super(WIDTH, HEIGHT);
  }

  @Override
  public void create() {
    super.create();
    startGame(true);
  }

  @Override
  public void render() {
    frames++;
    super.render();

    if (!overworldReady && getScreen() instanceof TutorialMenuScreen) {
      OverworldScreen screen = getOverworldScreen((TutorialMenuScreen) getScreen());
      if (screen != null) {
        setScreen(screen);
        overworldReady = true;
        System.out.println("[HeadlessPlayTest] Tutorial dismissed. Turn=" + screen.getTurnNumber()
            + " Year=" + screen.getCurrentYear() + " canFinishTurn=" + screen.canFinishTurn());
      }
      return;
    }

    if (overworldReady && !finished) {
      OverworldScreen screen = (OverworldScreen) getScreen();
      int turn = screen.getTurnNumber();
      if (turn < TARGET_TURN) {
        try {
          java.lang.reflect.Method m =
              OverworldScreen.class.getDeclaredMethod("processTurn");
          m.setAccessible(true);
          m.invoke(screen);
        } catch (Exception e) {
          e.printStackTrace();
        }
        int after = screen.getTurnNumber();
        System.out.println("[HeadlessPlayTest] processTurn() turn=" + turn + "->" + after
            + " year=" + screen.getCurrentYear());
        if (after == turn) {
          System.out.println("[HeadlessPlayTest] WARNING: turn did not advance, stopping");
          finished = true;
        }
      } else {
        captureAndExit(screen);
        finished = true;
      }
    }
  }

  private OverworldScreen getOverworldScreen(TutorialMenuScreen tutorial) {
    try {
      Field f = TutorialMenuScreen.class.getSuperclass().getDeclaredField("gameScreen");
      f.setAccessible(true);
      return (OverworldScreen) f.get(tutorial);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private void captureAndExit(OverworldScreen screen) {
    try {
      Files.createDirectories(Paths.get(OUT_DIR));

      StringBuilder state = new StringBuilder();
      state.append("Turn: ").append(screen.getTurnNumber()).append("\n");
      state.append("Year: ").append(screen.getCurrentYear()).append("\n");
      state.append("canFinishTurn: ").append(screen.canFinishTurn()).append("\n");

      try {
        Field empiresF = OverworldScreen.class.getDeclaredField("empires");
        empiresF.setAccessible(true);
        state.append("Empires: ").append(((java.util.List<?>) empiresF.get(screen)).size()).append("\n");
      } catch (Exception e) {
        state.append("Empires: <reflection failed>\n");
      }

      try {
        Field notifF = OverworldScreen.class.getDeclaredField("notificationsSystem");
        notifF.setAccessible(true);
        Object ns = notifF.get(screen);
        java.lang.reflect.Method m = ns.getClass().getMethod("getNotifications");
        state.append("Notifications: ").append(((java.util.List<?>) m.invoke(ns)).size()).append("\n");
      } catch (Exception e) {
        state.append("Notifications: <reflection failed>\n");
      }

      try {
        Field hadActionsF = OverworldScreen.class.getDeclaredField("hadActionsToDo");
        hadActionsF.setAccessible(true);
        state.append("HadActionsToDo: ").append(hadActionsF.getBoolean(screen)).append("\n");
      } catch (Exception e) {
        state.append("HadActionsToDo: <reflection failed>\n");
      }

      Files.writeString(Paths.get(STATE_FILE), state.toString());
      System.out.println("[HeadlessPlayTest] State written to " + STATE_FILE);
      System.out.println(state);

      int width = Gdx.graphics.getWidth();
      int height = Gdx.graphics.getHeight();
      Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
      Gdx.gl.glReadPixels(0, 0, width, height, Gdx.gl.GL_RGBA, Gdx.gl.GL_UNSIGNED_BYTE, pixmap.getPixels());
      PixmapIO.writePNG(Gdx.files.local(SCREENSHOT_FILE), pixmap);
      pixmap.dispose();
      System.out.println("[HeadlessPlayTest] Screenshot written to " + SCREENSHOT_FILE);

    } catch (Throwable t) {
      t.printStackTrace();
    }
    Gdx.app.exit();
  }

  public static void main(String[] args) {
    Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
    cfg.setWindowedMode(WIDTH, HEIGHT);
    cfg.setTitle("ShapeOfThingsThatWere - Headless Play Test");
    cfg.useVsync(false);
    cfg.disableAudio(true);
    new Lwjgl3Application(new HeadlessPlayTest(), cfg);
  }
}
