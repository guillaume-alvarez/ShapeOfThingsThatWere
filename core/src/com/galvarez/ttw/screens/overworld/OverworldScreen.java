package com.galvarez.ttw.screens.overworld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.galvarez.ttw.screens.*;
import com.galvarez.ttw.utils.Assets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.galvarez.ttw.model.AIArmyCommandSystem;
import com.galvarez.ttw.model.AIArmyMovementSystem;
import com.galvarez.ttw.model.AIDiplomaticSystem;
import com.galvarez.ttw.model.AIDiscoverySystem;
import com.galvarez.ttw.model.AISourceDestinationSystem;
import com.galvarez.ttw.model.ArmiesSystem;
import com.galvarez.ttw.model.DestinationSystem;
import com.galvarez.ttw.model.DiplomaticSystem;
import com.galvarez.ttw.model.DisasterSystem;
import com.galvarez.ttw.model.DiscoverySystem;
import com.galvarez.ttw.model.EffectsSystem;
import com.galvarez.ttw.model.EventsSystem;
import com.galvarez.ttw.model.InfluenceSystem;
import com.galvarez.ttw.model.PoliciesSystem;
import com.galvarez.ttw.model.RevoltSystem;
import com.galvarez.ttw.model.ScoreSystem;
import com.galvarez.ttw.model.SpecialDiscoveriesSystem;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.model.data.SessionSettings;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.model.map.Influence;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.rendering.CameraMovementSystem;
import com.galvarez.ttw.rendering.CounterRenderSystem;
import com.galvarez.ttw.rendering.DestinationRenderSystem;
import com.galvarez.ttw.rendering.DiplomacyRenderSystem;
import com.galvarez.ttw.rendering.FadingMessageRenderSystem;
import com.galvarez.ttw.rendering.InfluenceRenderSystem;
import com.galvarez.ttw.rendering.NotificationsSystem;
import com.galvarez.ttw.rendering.SpriteRenderSystem;
import com.galvarez.ttw.rendering.TextBoxRenderSystem;
import com.galvarez.ttw.rendering.components.Description;
import com.galvarez.ttw.rendering.map.MapHighlighter;
import com.galvarez.ttw.rendering.map.MapRenderer;
import com.galvarez.ttw.screens.overworld.controls.InputManager;

public final class OverworldScreen extends AbstractScreen {

  private static final Logger log = LoggerFactory.getLogger(OverworldScreen.class);

  public final GameMap map;

  private final SpriteRenderSystem spriteRenderSystem;

  private final CounterRenderSystem counterRenderSystem;

  private final DiplomacyRenderSystem diplomacyRenderSystem;

  private final InfluenceRenderSystem influenceRenderSystem;

  private final TextBoxRenderSystem textBoxRenderSystem;

  private final DestinationRenderSystem destinationRenderSystem;

  private final FadingMessageRenderSystem fadingMessageRenderSystem;

  private final EffectsSystem effectsSystem;

  private final EventsSystem eventsSystem;

  private final DiscoverySystem discoverySystem;

  private final PoliciesSystem policiesSystem;

  private final DiplomaticSystem diplomaticSystem;

  private final InfluenceSystem influenceSystem;

  final RevoltSystem revoltSystem;

  private final DisasterSystem disasterSystem;

  private final ArmiesSystem armiesSystem;

  private final DestinationSystem destinationSystem;

  private final ScoreSystem scoreSystem;

  private final AISourceDestinationSystem aiDestination;

  private final AIDiscoverySystem aiDiscovery;

  private final AIDiplomaticSystem aiDiplomacy;

  private final AIArmyCommandSystem aiCommand;

  private final AIArmyMovementSystem aiArmies;

  final MapRenderer mapRenderer;

  private final MapHighlighter mapHighlighter;

  private final Map<MapPosition, String> highlightedTiles = new HashMap<>();

  private boolean renderHighlighter;

  private final List<String> indications = new ArrayList<>();

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

  private final AskDiscoveryScreen askDiscoveryScreen;

  private final AskEventScreen askEventScreen;

  private final PoliciesMenuScreen policiesScreen;

  private final ScoresMenuScreen scoresScreen;

  private final ArmiesMenuScreen armiesScreen;

  private final TutorialMenuScreen tutorialScreen;

  private final NotificationsSystem notificationsSystem;

  public Entity player;

  private final List<Entity> empires;

  public OverworldScreen(ThingsThatWereGame game, SpriteBatch batch, World world, SessionSettings settings) {
    super(game, world, batch);

    cameraMovementSystem = world.setSystem(new CameraMovementSystem(camera));

    stage = new Stage(new ScreenViewport(), new SpriteBatch());

    map = new GameMap(settings.mapType.get().algo.getMapData(settings.map), settings.empires);

    notificationsSystem = world.setSystem(new NotificationsSystem(game.assets), true);
    eventsSystem = world.setSystem(new EventsSystem(), true);
    effectsSystem = world.setSystem(new EffectsSystem(), true);
    world.setSystem(new SpecialDiscoveriesSystem(this), true);
    discoverySystem = world.setSystem(new DiscoverySystem(settings, map, this), true);
    policiesSystem = world.setSystem(new PoliciesSystem(), true);
    diplomaticSystem = world.setSystem(new DiplomaticSystem(this, settings.startWithDiplomacy.get()), true);
    influenceSystem = world.setSystem(new InfluenceSystem(map), true);
    revoltSystem = world.setSystem(new RevoltSystem(map, settings, this), true);
    disasterSystem = world.setSystem(new DisasterSystem(this), true);
    armiesSystem = world.setSystem(new ArmiesSystem(map, this), true);
    destinationSystem = world.setSystem(new DestinationSystem(map, this), true);
    scoreSystem = world.setSystem(new ScoreSystem(settings, this), true);
    aiDestination = world.setSystem(new AISourceDestinationSystem(map, this), true);
    aiDiscovery = world.setSystem(new AIDiscoverySystem(), true);
    aiDiplomacy = world.setSystem(new AIDiplomaticSystem(map), true);
    aiCommand = world.setSystem(new AIArmyCommandSystem(map, this), true);
    aiArmies = world.setSystem(new AIArmyMovementSystem(map, this), true);
    influenceRenderSystem = world.setSystem(new InfluenceRenderSystem(camera, batch, map), true);
    diplomacyRenderSystem = world.setSystem(new DiplomacyRenderSystem(camera, batch), true);
    textBoxRenderSystem = world.setSystem(new TextBoxRenderSystem(game.assets, camera, batch), true);
    destinationRenderSystem = world.setSystem(new DestinationRenderSystem(camera, batch), true);
    spriteRenderSystem = world.setSystem(new SpriteRenderSystem(camera, batch), true);
    counterRenderSystem = world.setSystem(new CounterRenderSystem(game.assets, camera, batch), true);
    fadingMessageRenderSystem = world.setSystem(new FadingMessageRenderSystem(game.assets, camera, batch), true);

    world.initialize();
    empires = fillWorldWithEntities();
    discoverySystem.process();
    policiesSystem.process();
    diplomaticSystem.process();
    influenceSystem.process();
    revoltSystem.process();
    disasterSystem.process();
    armiesSystem.process();
    destinationSystem.process();
    scoreSystem.process();
    diplomacyRenderSystem.preprocess();
    influenceRenderSystem.preprocess();
    textBoxRenderSystem.preprocess();
    notificationsSystem.process();
    log.info("The world is initialized");

    inputManager = new InputManager(game.assets, camera, world, this, stage, map);
    inputManager.menuBuilder.buildTurnMenu();
    inputManager.menuBuilder.buildNotificationMenu();
    inputManager.menuBuilder.buildEmpireMenu();

    world.setManager(new PlayerManager());

    mapRenderer = new MapRenderer(camera, batch, map);
    mapHighlighter = new MapHighlighter(game.assets, camera, batch);

    renderHighlighter = false;

    tutorialScreen = new TutorialMenuScreen(game, world, batch, this);
    pauseScreen = new PauseMenuScreen(game, world, batch, this);
    diplomacyScreen = new DiplomacyMenuScreen(game, world, batch, this, empires, player, diplomaticSystem);
    discoveryScreen = new DiscoveryMenuScreen(game, world, batch, this, player, discoverySystem);
    askDiscoveryScreen = new AskDiscoveryScreen(game, world, batch, this, player, discoverySystem);
    askEventScreen = new AskEventScreen(game, world, batch, this);
    policiesScreen = new PoliciesMenuScreen(game, world, batch, this, player, policiesSystem, effectsSystem,
        revoltSystem);
    scoresScreen = new ScoresMenuScreen(game, world, batch, this, scoreSystem);
    armiesScreen = new ArmiesMenuScreen(game, world, batch, this, player, armiesSystem);
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
        cameraMovementSystem.move(selectedTile);
      else
        cameraMovementSystem.move(new MapPosition(map.width / 2, map.height / 2));
      firstShow = false;
    }

    mapRenderer.render();
    diplomacyRenderSystem.process();
    influenceRenderSystem.process();
    destinationRenderSystem.process();
    counterRenderSystem.process();
    spriteRenderSystem.process();
    textBoxRenderSystem.process();

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
    for (Empire empire : map.empires)
      list.add(createEmpire(empire.newCityName(), empire));

    selectedTile = player.getComponent(MapPosition.class);

    // You have to process the world once to get all the entities loaded up with
    // the "Stats" component - I'm not sure why, but if you don't, the bag of
    // entities that turnManagementSystem gets is empty?
    world.process();

    return list;
  }

  private Entity createEmpire(String name, Empire empire) {
    int x, y;
    do {
      x = MathUtils.random(map.width - 4) + 2;
      y = MathUtils.random(map.height - 4) + 2;
    } while (!map.getTerrainAt(x, y).canStart()
    // avoid having two cities on the same tile
        || map.getEntityAt(x, y) != null
        // or on neighbor tiles
        || hasNeighbourCity(x, y));
    Entity entity = EntityFactory.createEmpire(world, x, y, name, empire);
    if (!empire.isComputerControlled())
      player = entity;
    map.setEntity(entity, x, y);
    InfluenceSource source = entity.getComponent(InfluenceSource.class);
    source.influencedTiles.add(map.getPositionAt(x, y));
    for (MapPosition p : map.getNeighbors(x, y, 1)) {
      if (map.isOnMap(p)) {
        Influence inf = map.getInfluenceAt(p);
        inf.setInfluence(entity, inf.getMaxInfluence());
        source.influencedTiles.add(p);
      }
    }
    log.info("Created {} for empire {}", name, empire);
    return entity;
  }

  private boolean hasNeighbourCity(int x, int y) {
    for (MapPosition pos : map.getNeighbors(x, y, 2))
      if (map.getEntityAt(pos.x, pos.y) != null)
        return true;
    return false;
  }

  private void processTurn() {
    log.info("Process new turn");
    long start = System.currentTimeMillis();

    // If they don't have AI, give control to the human player
    inputManager.setEnabled(true);
    stage.setKeyboardFocus(null);
    stage.setScrollFocus(null);

    // compute AI before 'really' processing turn
    aiDestination.process();
    aiDiscovery.process();
    aiDiplomacy.process();
    aiCommand.process();
    // make sure armies created by command are assigned a destination
    aiArmies.process();

    discoverySystem.process();
    policiesSystem.process();
    diplomaticSystem.process();
    armiesSystem.process();
    destinationSystem.process();
    influenceSystem.process();
    revoltSystem.process();
    disasterSystem.process();
    scoreSystem.process();
    eventsSystem.process();

    world.setDelta(0);
    world.process();
    
    updateRendering();
    notificationsSystem.process();
    turnNumber++;

    log.debug("Processed turn #{} in {}ms", turnNumber, System.currentTimeMillis() - start);
  }
  
  public void updateRendering() {
    diplomacyRenderSystem.preprocess();
    influenceRenderSystem.preprocess();
    textBoxRenderSystem.preprocess();    
  }

  public void flag(Entity source, int x, int y) {
    destinationSystem.computePath(source, new MapPosition(x, y));
  }

  public void highlightFlagRange(Entity source) {
    highlightedTiles.clear();
    for (MapPosition tile : destinationSystem.getTargetTiles(source))
      highlightedTiles.put(tile, null);
    renderHighlighter = true;
    mapHighlighter.setColor(0f, 0f, 0.2f, 0.3f);
    indications.add("Select destination in highlighted tiles for " + source.getComponent(Description.class));

    inputManager.reloadMenus();
  }

  public void stopHighlighing() {
    renderHighlighter = false;
    highlightedTiles.clear();
    indications.clear();

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

  public void endTurn() {
    if (canFinishTurn())
      processTurn();

    // update the menu data
    inputManager.reloadMenus();
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

  public void askDiscovery() {
    game.setScreen(askDiscoveryScreen);
  }

  public void askEvent(String label, Map<String, Runnable> choices) {
    askEventScreen.setEvent(label, choices);
    game.setScreen(askEventScreen);
  }

  public void armiesMenu() {
    game.setScreen(armiesScreen);
  }

  public void policiesMenu() {
    game.setScreen(policiesScreen);
  }

  public void scoresMenu() {
    game.setScreen(scoresScreen);
  }

  public void tutorialMenu() {
    game.setScreen(tutorialScreen);
  }

  public void select(Entity e, boolean flagIfMoveable) {
    inputManager.select(e.getComponent(MapPosition.class), e, flagIfMoveable);
    cameraMovementSystem.move(selectedTile);
  }

  public List<String> getIndications() {
    return indications;
  }

  public void displayColoredInfluence(boolean b) {
    influenceRenderSystem.displayColoredInfluence(b);
  }

  public boolean displayColoredInfluence() {
    return influenceRenderSystem.displayColoredInfluence();
  }

}
