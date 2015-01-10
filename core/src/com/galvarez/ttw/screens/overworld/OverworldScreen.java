package com.galvarez.ttw.screens.overworld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.galvarez.ttw.model.AIDiplomaticSystem;
import com.galvarez.ttw.model.AIDiscoverySystem;
import com.galvarez.ttw.model.AIInfluenceSystem;
import com.galvarez.ttw.model.BuildingsSystem;
import com.galvarez.ttw.model.DiplomaticSystem;
import com.galvarez.ttw.model.DiscoverySystem;
import com.galvarez.ttw.model.EffectsSystem;
import com.galvarez.ttw.model.InfluenceSystem;
import com.galvarez.ttw.model.PoliciesSystem;
import com.galvarez.ttw.model.components.AIControlled;
import com.galvarez.ttw.model.components.Capital;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.model.data.SessionSettings;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.model.map.MapTools;
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
import com.galvarez.ttw.screens.PoliciesMenuScreen;
import com.galvarez.ttw.screens.overworld.controls.InputManager;

public final class OverworldScreen extends AbstractScreen {

  private static final Logger log = LoggerFactory.getLogger(OverworldScreen.class);

  public final GameMap gameMap;

  private final SpriteRenderSystem spriteRenderSystem;

  private final InfluenceRenderSystem influenceRenderSystem;

  private final FadingMessageRenderSystem fadingMessageRenderSystem;

  private final EffectsSystem effectsSystem;

  private final DiscoverySystem discoverySystem;

  private final BuildingsSystem buildingsSystem;

  private final PoliciesSystem policiesSystem;

  private final DiplomaticSystem diplomaticSystem;

  private final InfluenceSystem influenceSystem;

  private final AIInfluenceSystem iaInfluence;

  private final AIDiscoverySystem iaDiscovery;

  private final AIDiplomaticSystem iaDiplomacy;

  final MapRenderer mapRenderer;

  private final MapHighlighter mapHighlighter;

  private final Map<MapPosition, String> highlightedTiles = new HashMap<>();

  private final boolean renderMap;

  private boolean renderHighlighter;

  private final InputManager inputManager;

  /** Stage to display the HUD. */
  private final Stage stage;

  private final CameraMovementSystem cameraMovementSystem;

  private boolean firstShow = true;

  private int turnNumber = 0;

  public MapPosition selectedTile;

  private final PauseMenuScreen pauseScreen;

  private final DiplomacyMenuScreen diplomacyScreen;

  private final DiscoveryMenuScreen discoveryScreen;

  private final PoliciesMenuScreen policiesScreen;

  private final NotificationsSystem notificationsSystem;

  public Entity player;

  private final List<Entity> empires;

  public OverworldScreen(ThingsThatWereGame game, SpriteBatch batch, World world, SessionSettings settings) {
    super(game, world, batch);

    cameraMovementSystem = world.setSystem(new CameraMovementSystem(camera));

    stage = new Stage(new ScreenViewport(), new SpriteBatch());

    gameMap = new GameMap(settings.mapType.get().algo.getMapData(settings.map), settings.empires);

    notificationsSystem = world.setSystem(new NotificationsSystem(), true);
    effectsSystem = world.setSystem(new EffectsSystem(), true);
    discoverySystem = world.setSystem(new DiscoverySystem(settings, gameMap, this), true);
    buildingsSystem = world.setSystem(new BuildingsSystem(this, settings), true);
    policiesSystem = world.setSystem(new PoliciesSystem(), true);
    diplomaticSystem = world.setSystem(new DiplomaticSystem(this, settings.startWithDiplomacy.get()), true);
    influenceSystem = world.setSystem(new InfluenceSystem(gameMap, settings, this), true);
    iaInfluence = world.setSystem(new AIInfluenceSystem(gameMap, this), true);
    iaDiscovery = world.setSystem(new AIDiscoverySystem(), true);
    iaDiplomacy = world.setSystem(new AIDiplomaticSystem(gameMap), true);
    influenceRenderSystem = world.setSystem(new InfluenceRenderSystem(camera, batch, gameMap), true);
    spriteRenderSystem = world.setSystem(new SpriteRenderSystem(camera, batch), true);
    fadingMessageRenderSystem = world.setSystem(new FadingMessageRenderSystem(camera, batch), true);

    world.initialize();
    empires = fillWorldWithEntities();
    discoverySystem.process();
    buildingsSystem.process();
    policiesSystem.process();
    diplomaticSystem.process();
    influenceSystem.process();
    influenceRenderSystem.preprocess();
    notificationsSystem.process();
    log.info("The world is initialized");

    inputManager = new InputManager(camera, world, this, stage, gameMap);
    inputManager.menuBuilder.buildTurnMenu();
    inputManager.menuBuilder.buildNotificationMenu();
    inputManager.menuBuilder.buildEmpireMenu();

    world.setManager(new PlayerManager());

    mapRenderer = new MapRenderer(camera, batch, gameMap);
    mapHighlighter = new MapHighlighter(camera, batch);

    renderMap = true;
    renderHighlighter = false;

    pauseScreen = new PauseMenuScreen(game, world, batch, this, settings);
    diplomacyScreen = new DiplomacyMenuScreen(game, world, batch, this, empires, player, diplomaticSystem);
    discoveryScreen = new DiscoveryMenuScreen(game, world, batch, this, player, discoverySystem);
    policiesScreen = new PoliciesMenuScreen(game, world, batch, this, player, policiesSystem, effectsSystem);
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

    // render AFTER map was drawn
    fadingMessageRenderSystem.process();

    stage.act(delta);
    stage.draw();
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
    } while (!gameMap.getTerrainAt(x, y).canStart()
    // avoid having two cities on the same tile
        || gameMap.getEntityAt(x, y) != null
        // or on neighbor tiles
        || hasNeighbourCity(x, y));
    Entity city = EntityFactory.createCity(world, x, y, name, empire);
    Entity entity;

    entity = EntityFactory.createEmpire(world, city, empire);
    if (empire.isComputerControlled()) {
      city.edit().add(new AIControlled());
      entity.edit().add(new AIControlled());
    } else {
      player = entity;
    }
    gameMap.addEntity(city, x, y);
    log.info("Created city {} for empire {}", name, empire);
    return entity;
  }

  private boolean hasNeighbourCity(int x, int y) {
    for (MapPosition pos : MapTools.getNeighbors(x, y, 2))
      if (gameMap.getEntityAt(pos.x, pos.y) != null)
        return true;
    return false;
  }

  public void processTurn() {
    log.info("Process new turn");

    // If they don't have AI, give control to the human player
    inputManager.setEnabled(true);
    stage.setKeyboardFocus(null);
    stage.setScrollFocus(null);

    iaInfluence.process();
    iaDiscovery.process();
    iaDiplomacy.process();
    discoverySystem.process();
    buildingsSystem.process();
    policiesSystem.process();
    diplomaticSystem.process();
    influenceSystem.process();

    world.setDelta(0);
    world.process();

    influenceRenderSystem.preprocess();
    notificationsSystem.process();
    turnNumber++;

  }

  public void flag(Entity source, int x, int y) {
    InfluenceSource inf = source.getComponent(InfluenceSource.class);
    inf.target = new MapPosition(x, y);
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
    inputManager.reloadMenus();
  }

  public void stopHighlighing() {
    renderHighlighter = false;
    highlightedTiles.clear();
    inputManager.reloadMenus();
  }

  public Map<MapPosition, String> getHighlightedTiles() {
    return highlightedTiles;
  }

  public int getTurnNumber() {
    return turnNumber;
  }

  public int getCurrentYear() {
    return (turnNumber * 10) - 4000;
  }

  public boolean canFinishTurn() {
    return notificationsSystem.canFinishTurn()
    // make sure no player action on map is waiting for input
        && !renderHighlighter;
  }

  public Entity addComponent(Component component, Entity entity) {
    if (entity == null)
      return null;
    entity.edit().add(component);
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

  public void policiesMenu() {
    game.setScreen(policiesScreen);
  }

  public void select(Entity e) {
    inputManager.select(e.getComponent(MapPosition.class), e, false);
    cameraMovementSystem.move(selectedTile.x, selectedTile.y);
  }

}
