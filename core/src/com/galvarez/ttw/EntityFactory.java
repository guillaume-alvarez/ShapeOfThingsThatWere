package com.galvarez.ttw;

import com.artemis.Entity;
import com.artemis.EntityEdit;
import com.artemis.World;
import com.badlogic.gdx.graphics.Color;
import com.galvarez.ttw.ExpiringSystem.Expires;
import com.galvarez.ttw.model.components.Army;
import com.galvarez.ttw.model.components.Buildings;
import com.galvarez.ttw.model.components.Capital;
import com.galvarez.ttw.model.components.Diplomacy;
import com.galvarez.ttw.model.components.Discoveries;
import com.galvarez.ttw.model.components.InfluenceSource;
import com.galvarez.ttw.model.components.Policies;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.model.map.MapPosition;
import com.galvarez.ttw.rendering.components.ColorAnimation;
import com.galvarez.ttw.rendering.components.Description;
import com.galvarez.ttw.rendering.components.FadingMessage;
import com.galvarez.ttw.rendering.components.MutableMapPosition;
import com.galvarez.ttw.rendering.components.Name;
import com.galvarez.ttw.rendering.components.ScaleAnimation;
import com.galvarez.ttw.rendering.components.Sprite;

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

  public static Entity createCity(World world, int x, int y, String name, Empire empire) {
    Entity e = world.createEntity();

    EntityEdit edit = e.edit();

    Sprite sprite = new Sprite();
    sprite.name = "cylinderwide";
    sprite.rotation = 0f;
    sprite.scaleX = 1f;
    sprite.scaleY = 1f;
    sprite.color = empire.color;
    edit.add(sprite);

    edit.add(new MapPosition(x, y)).add(new Description("City of " + name)).add(new InfluenceSource())
        .add(new Buildings()).add(new Name(name));

    return e;
  }

  public static Entity createEmpire(World world, Entity capital, Empire empire) {
    Entity e = world.createEntity();

    e.edit().add(empire).add(new Discoveries()).add(new Policies()).add(new Diplomacy()).add(new Army())
        .add(new Capital(capital)).add(new Name(capital.getComponent(Name.class).name));

    // link the capital to its empire
    capital.getComponent(InfluenceSource.class).empire = e;

    return e;
  }

  public static Entity createFadingTileLabel(World world, String label, Color color, float x, float y) {
    Entity e = world.createEntity();
    EntityEdit edit = e.edit();

    MutableMapPosition position = edit.create(MutableMapPosition.class);
    position.x = x;
    position.y = y;

    FadingMessage fading = edit.create(FadingMessage.class);
    fading.label = label;
    fading.color = color;
    fading.duration = 1.2f;
    fading.vx = 0f;
    fading.vy = 1.3f;

    return e;
  }

}
