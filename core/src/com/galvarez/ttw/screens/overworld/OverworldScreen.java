package com.galvarez.ttw.screens.overworld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.World;
import com.artemis.managers.PlayerManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.galvarez.ttw.EntityFactory;
import com.galvarez.ttw.ThingsThatWereGame;
import com.galvarez.ttw.model.AIDiscoverySystem;
import com.galvarez.ttw.model.AIInfluenceSystem;
import com.galvarez.ttw.model.DiscoverySystem;
import com.galvarez.ttw.model.InfluenceSystem;
import com.galvarez.ttw.model.components.AIControlled;
import com.galvarez.ttw.model.components.Capital;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.model.data.SessionSettings;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.HexMapGenerator;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.MapTools;
import com.galvarez.ttw.model.map.Terrain;
import com.galvarez.ttw.rendering.CameraMovementSystem;
import com.galvarez.ttw.rendering.FadingMessageRenderSystem;
import com.galvarez.ttw.rendering.InfluenceRenderSystem;
import com.galvarez.ttw.rendering.NotificationsSystem;
import com.galvarez.ttw.rendering.SpriteRenderSystem;
import com.galvarez.ttw.rendering.map.MapHighlighter;
import com.galvarez.ttw.rendering.map.MapRenderer;
import com.galvarez.ttw.screens.AbstractScreen;
import com.galvarez.ttw.screens.DiplomacyMenuScreen;
import com.galvarez.ttw.screens.DiscoveryMenuScreen;
import com.galvarez.ttw.screens.PauseMenuScreen;
import com.galvarez.ttw.screens.overworld.controls.InputManager;

public final class OverworldScreen extends AbstractScreen {

  public int turnNumber = 0;

  public final GameMap gameMap;

  private final SpriteRenderSystem spriteRenderSystem;

  private final InfluenceRenderSystem influenceRenderSystem;

  private final FadingMessageRenderSystem fadingMessageRenderSystem;

  private final DiscoverySystem discoverySystem;

  final InfluenceSystem influenceSystem;

  private final AIInfluenceSystem iaInfluence;

  private final AIDiscoverySystem iaDiscovery;

  final MapRenderer mapRenderer;

  private final MapHighlighter mapHighlighter;

  private final Map<MapPosition, String> highlightedTiles = new HashMap<>();

  private final boolean renderMap;

  private boolean renderHighlighter;

  private final InputManager inputManager;

  /** Stage to display the HUD. */
  private final Stage stage;

  public final CameraMovementSystem cameraMovementSystem;

  private boolean firstShow = true;

  public MapPosition selectedTile;

  private final PauseMenuScreen pauseScreen;

  private final DiplomacyMenuScreen diplomacyScreen;

  private final DiscoveryMenuScreen discoveryScreen;

  private Entity player;

  private final List<Entity> empires;

  public OverworldScreen(ThingsThatWereGame game, SpriteBatch batch, World world, SessionSettings settings) {
    super(game, world, batch);

    cameraMovementSystem = new CameraMovementSystem(camera);

    gameMap = new GameMap(new HexMapGenerator().getDiamondSquare(settings.mapNoise.get(), settings.mapWidth.get(),
        settings.mapHeight.get()), settings.empires);

    stage = new Stage(new ScreenViewport(), new SpriteBatch());

    inputManager = new InputManager(camera, world, this, stage, gameMap);
    inputManager.menuBuilder.buildTurnMenu();
    inputManager.menuBuilder.buildEmpireMenu();

    world.setManager(new PlayerManager());

    mapRenderer = new MapRenderer(camera, batch, gameMap);
    mapHighlighter = new MapHighlighter(camera, batch, gameMap);

    world.setSystem(new NotificationsSystem(stage));
    discoverySystem = world.setSystem(new DiscoverySystem(settings.getDiscoveries(), gameMap), true);
    influenceSystem = world.setSystem(new InfluenceSystem(gameMap), true);
    iaInfluence = world.setSystem(new AIInfluenceSystem(gameMap, this), true);
    iaDiscovery = world.setSystem(new AIDiscoverySystem(), true);
    influenceRenderSystem = world.setSystem(new InfluenceRenderSystem(camera, batch, gameMap), true);
    spriteRenderSystem = world.setSystem(new SpriteRenderSystem(camera, batch), true);
    fadingMessageRenderSystem = world.setSystem(new FadingMessageRenderSystem(camera, batch), true);

    world.initialize();
    influenceRenderSystem.preprocess();
    System.out.println("The world is initialized");

    empires = fillWorldWithEntities();

    renderMap = true;
    renderHighlighter = false;

    pauseScreen = new PauseMenuScreen(game, world, batch, this);
    diplomacyScreen = new DiplomacyMenuScreen(game, world, batch, this, empires);
    discoveryScreen = new DiscoveryMenuScreen(game, world, batch, this, player, discoverySystem);
  }

  @Override
  public void show() {
    super.show();

    inputManager.setEnabled(true);
  }

  @Override
  public void render(float delta) {
    super.render(delta);

    if (firstShow) {
      if (selectedTile != null)
        cameraMovementSystem.move(selectedTile.x, selectedTile.y);
      else
        cameraMovementSystem.move(gameMap.width / 2, gameMap.height / 2);
      firstShow = false;
    }

    if (renderMap) {
      mapRenderer.render();
      spriteRenderSystem.process();
      influenceRenderSystem.process();
    }

    if (renderHighlighter) {
      mapHighlighter.render(highlightedTiles);
    }

    fadingMessageRenderSystem.process();

    stage.act(delta);
    stage.draw();

    cameraMovementSystem.process(delta);
  }

  @Override
  public void resize(int width, int height) {
    super.resize(width, height);

    // update map camera and keep same center
    Vector3 pos = camera.position.cpy();
    camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    camera.position.set(pos);

    // update GUI view
    stage.getViewport().update(width, height, true);
    /*
     * The GUI use a ScreenViewport, meaning it won't scale when screen size
     * change. This is fine because we don't want the GUI size to change,
     * becoming zoomed in and ugly or zoomed and unreadable. However it has a
     * small side effect: the existing menu were placed according to the
     * vertical stage size. The stage size changed with the screen (game window)
     * one. So we must recompute the GUI elements coordinates. The simlest way
     * to do it is to recreate the menu.
     */
    inputManager.reloadMenus();
  }

  @Override
  public void dispose() {
    world.deleteSystem(spriteRenderSystem);
    world.deleteSystem(influenceRenderSystem);
    stage.dispose();
  }

  private List<Entity> fillWorldWithEntities() {
    // add the entity for players cities
    List<Entity> list = new ArrayList<>();
    for (Empire empire : gameMap.empires)
      list.add(createEmpire(empire.newCityName(), empire));

    selectedTile = player.getComponent(Capital.class).capital.getComponent(MapPosition.class);

    // You have to process the world once to get all the entities loaded up with
    // the "Stats" component - I'm not sure why, but if you don't, the bag of
    // entities that turnManagementSystem gets is empty?
    world.process();

    return list;
  }

  private Entity createEmpire(String name, Empire empire) {
    int x, y;
    do {
      x = MathUtils.random(MapTools.width() - 4) + 2;
      y = MathUtils.random(MapTools.height() - 4) + 2;
    } while ((gameMap.map[x][y] != Terrain.PLAIN && gameMap.map[x][y] != Terrain.GRASSLAND)
    // avoid having two cities on the same tile
        || gameMap.getEntityAt(x, y) != null
        // or on neighbor tiles
        || hasNeighbourCity(x, y));
    Entity city = EntityFactory.createCity(world, x, y, name, empire);
    Entity entity;

    if (empire.isComputerControlled()) {
      city.addComponent(new AIControlled());
      entity = EntityFactory.createEmpire(world, city);
      entity.addComponent(new AIControlled());
    } else {
      entity = player = EntityFactory.createEmpire(world, city);
    }
    city.addToWorld();
    entity.addToWorld();
    gameMap.addEntity(city, x, y);
    System.out.println("Created city " + name + " for empire " + empire);
    return entity;
  }

  private boolean hasNeighbourCity(int x, int y) {
    for (MapPosition pos : MapTools.getNeighbors(x, y, 2))
      if (gameMap.getEntityAt(pos.x, pos.y) != null)
        return true;
    return false;
  }

  public void processTurn() {
    System.out.println("Process new turn");

    // If they don't have AI, give control to the human player
    inputManager.setEnabled(true);
    stage.setKeyboardFocus(null);
    stage.setScrollFocus(null);

    iaInfluence.process();
    iaDiscovery.process();
    discoverySystem.process();
    influenceSystem.process();
    influenceRenderSystem.preprocess();
    turnNumber++;

  }

  public void flag(Entity source, int x, int y) {
    InfluenceSource inf = source.getComponent(InfluenceSource.class);
    inf.target = new MapPosition(x, y);
    source.changedInWorld();
  }

  public void setHighlightColor(float r, float g, float b, float a) {
    mapHighlighter.setColor(r, g, b, a);
  }

  public void highlightFlagRange(Entity source) {
    highlightedTiles.clear();
    for (MapPosition tile : influenceSystem.getFlaggableTiles(source))
      highlightedTiles.put(tile, String.valueOf(gameMap.getInfluenceAt(tile).requiredInfluence(source)));
    renderHighlighter = true;
    setHighlightColor(0f, 0f, 0.2f, 0.3f);
  }

  public void stopHighlighing() {
    renderHighlighter = false;
    highlightedTiles.clear();
  }

  public Map<MapPosition, String> getHighlightedTiles() {
    return highlightedTiles;
  }

  public Entity addComponent(Component component, Entity entity) {
    if (entity == null)
      return null;
    entity.addComponent(component);
    entity.changedInWorld();
    return entity;
  }

  public void pauseMenu() {
    game.setScreen(pauseScreen);
  }

  public void diplomacyMenu() {
    game.setScreen(diplomacyScreen);
  }

  public void discoveryMenu() {
    game.setScreen(discoveryScreen);
  }

}
