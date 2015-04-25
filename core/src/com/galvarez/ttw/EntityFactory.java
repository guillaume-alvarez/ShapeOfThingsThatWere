package com.galvarez.ttw;

import com.artemis.Entity;
import com.artemis.EntityEdit;
import com.artemis.World;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.galvarez.ttw.ExpiringSystem.Expires;
import com.galvarez.ttw.model.components.AIControlled;
import com.galvarez.ttw.model.components.Army;
import com.galvarez.ttw.model.components.ArmyCommand;
import com.galvarez.ttw.model.components.Buildings;
import com.galvarez.ttw.model.components.Destination;
import com.galvarez.ttw.model.components.Diplomacy;
import com.galvarez.ttw.model.components.Discoveries;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.components.Policies;
import com.galvarez.ttw.model.components.Score;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.rendering.components.ColorAnimation;
import com.galvarez.ttw.rendering.components.Counter;
import com.galvarez.ttw.rendering.components.Description;
import com.galvarez.ttw.rendering.components.FadingMessage;
import com.galvarez.ttw.rendering.components.MutableMapPosition;
import com.galvarez.ttw.rendering.components.Name;
import com.galvarez.ttw.rendering.components.ScaleAnimation;
import com.galvarez.ttw.rendering.components.Sprite;
import com.galvarez.ttw.rendering.components.TextBox;
import com.galvarez.ttw.utils.Colors;

public class EntityFactory {

  public static Entity createClick(World world, int x, int y, float startScale, float speed) {
    Entity e = world.createEntity();
    EntityEdit edit = e.edit();

    MutableMapPosition pos = edit.create(MutableMapPosition.class);
    pos.x = x;
    pos.y = y;

    Sprite sprite = new Sprite();
    sprite.name = "click";
    sprite.color = new Color(1f, 1f, 1f, 0.5f);
    sprite.rotation = 0f;
    sprite.scaleX = startScale;
    sprite.scaleY = startScale;
    edit.add(sprite);

    Expires expires = edit.create(Expires.class);
    expires.delay = 1f;

    ScaleAnimation scaleAnimation = edit.create(ScaleAnimation.class);
    scaleAnimation.speed = speed;

    ColorAnimation colorAnimation = edit.create(ColorAnimation.class);
    colorAnimation.alphaAnimate = true;
    colorAnimation.alphaSpeed = -1f;

    return e;
  }

  public static Entity createEmpire(World world, int x, int y, String name, Empire empire) {
    Entity e = world.createEntity();

    EntityEdit edit = e.edit();

    ArmyCommand command = new ArmyCommand();
    edit.add(empire).add(new Discoveries()).add(new Policies()).add(new Diplomacy()).add(command).add(new Score());

    Sprite sprite = new Sprite();
    sprite.name = "cylinderwide";
    sprite.rotation = 0f;
    sprite.scaleX = 1f;
    sprite.scaleY = 1f;
    sprite.color = empire.color;
    edit.add(sprite);

    InfluenceSource source = new InfluenceSource();
    edit.add(new MapPosition(x, y)).add(source).add(new Buildings()).add(new Destination(command.forbiddenTiles));

    edit.add(new Name(name)).add(new Description("Tribe of " + name))
        .add(new TextBox(() -> name + ": " + source.power()));

    if (empire.isComputerControlled())
      edit.add(new AIControlled());

    return e;
  }

  public static Entity createArmy(World world, MapPosition pos, String name, Empire empire, Entity source,
      int militaryPower) {
    Entity e = world.createEntity();
    EntityEdit edit = e.edit();

    edit.add(new Counter(Colors.contrast(empire.color), empire.color, militaryPower));

    edit.add(pos).add(new Name(name)).add(new Description(name))
        .add(new Destination(source.getComponent(ArmyCommand.class).forbiddenTiles))
        .add(new Army(source, militaryPower)).add(empire);

    if (empire.isComputerControlled())
      edit.add(new AIControlled());

    return e;
  }

  public static Entity createFadingTileLabel(World world, String label, Color color, float x, float y, float duration) {
    Entity e = world.createEntity();
    EntityEdit edit = e.edit();

    MutableMapPosition position = edit.create(MutableMapPosition.class);
    position.x = x;
    position.y = y;

    FadingMessage fading = edit.create(FadingMessage.class);
    fading.label = label;
    fading.color = color;
    fading.duration = duration;
    fading.vx = 0f;
    fading.vy = 1.3f;

    return e;
  }

  public static Entity createFadingTileIcon(World world, TextureRegion icon, Color color, float x, float y, float duration) {
    Entity e = world.createEntity();
    EntityEdit edit = e.edit();

    MutableMapPosition position = edit.create(MutableMapPosition.class);
    position.x = x;
    position.y = y;

    FadingMessage fading = edit.create(FadingMessage.class);
    fading.icon = icon;
    fading.color = color;
    fading.duration = duration;
    fading.vx = 0f;
    fading.vy = 1.3f;

    return e;
  }

}
