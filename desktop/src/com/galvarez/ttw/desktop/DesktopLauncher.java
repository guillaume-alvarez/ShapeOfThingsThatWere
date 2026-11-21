package com.galvarez.ttw.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.galvarez.ttw.ThingsThatWereGame;
import com.galvarez.ttw.ThingsThatWereGameTest;

public class DesktopLauncher {

  private static final int WIDTH = 1024;

  private static final int HEIGHT = 800;

  private static boolean isSmokeTest(String[] args) {
    for (String arg : args) {
      if ("--smoke-test".equals(arg)) return true;
    }
    return false;
  }

  public static void main(String[] args) {
//    ImagePacker.run();

    boolean smokeTest = isSmokeTest(args);

    LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
    cfg.width = WIDTH;
    cfg.height = HEIGHT;
    cfg.title = "The Shape of Things that Were";
    cfg.vSyncEnabled = !smokeTest;
    cfg.resizable = true;
    // no audio implemented yet
    // for some unknown reason OpenAL init is very long (a few minutes) on some machines
    LwjglApplicationConfiguration.disableAudio = true;

    if (smokeTest) {
      new LwjglApplication(new ThingsThatWereGameTest(WIDTH, HEIGHT), cfg);
    } else {
      new LwjglApplication(new ThingsThatWereGame(WIDTH, HEIGHT), cfg);
    }
  }
}
