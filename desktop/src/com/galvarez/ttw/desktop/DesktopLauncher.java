package com.galvarez.ttw.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.galvarez.ttw.ThingsThatWereGame;

public class DesktopLauncher {

  private static final int WIDTH = 1024;

  private static final int HEIGHT = 800;

  public static void main(String[] args) {
//    ImagePacker.run();
    
    LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
    cfg.width = WIDTH;
    cfg.height = HEIGHT;
    cfg.title = "The Shape of Things that Were";
    cfg.vSyncEnabled = true;
    cfg.resizable = true;
    new LwjglApplication(new ThingsThatWereGame(WIDTH, HEIGHT), cfg);
  }
}
