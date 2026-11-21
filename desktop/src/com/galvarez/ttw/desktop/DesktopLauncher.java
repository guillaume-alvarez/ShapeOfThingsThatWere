package com.galvarez.ttw.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
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

    Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
    cfg.setWindowedMode(WIDTH, HEIGHT);
    cfg.setTitle("The Shape of Things that Were");
    cfg.useVsync(!smokeTest);
    cfg.setResizable(true);
    // no audio implemented yet
    // for some unknown reason OpenAL init is very long (a few minutes) on some machines
    cfg.disableAudio(true);

    if (smokeTest) {
      new Lwjgl3Application(new ThingsThatWereGameTest(WIDTH, HEIGHT), cfg);
    } else {
      new Lwjgl3Application(new ThingsThatWereGame(WIDTH, HEIGHT), cfg);
    }
  }
}
