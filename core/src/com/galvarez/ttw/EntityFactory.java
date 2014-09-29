package com.galvarez.ttw;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.graphics.Color;
import com.galvarez.ttw.ExpiringSystem.Expires;
import com.galvarez.ttw.model.components.Capital;
import com.galvarez.ttw.model.components.Diplomacy;
import com.galvarez.ttw.model.components.Discoveries;
import com.galvarez.ttw.model.components.InfluenceSource;
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

    MutableMapPosition pos = e.createComponent(MutableMapPosition.class);
    pos.x = x;
    pos.y = y;

    Sprite sprite = new Sprite();
    sprite.name = "click";
    sprite.color = new Color(1f, 1f, 1f, 0.5f);
    sprite.rotation = 0f;
    sprite.scaleX = startScale;
    sprite.scaleY = startScale;
    e.addComponent(sprite);

    Expires expires = e.createComponent(Expires.class);
    expires.delay = 1f;

    ScaleAnimation scaleAnimation = e.createComponent(ScaleAnimation.class);
    scaleAnimation.speed = speed;

    ColorAnimation colorAnimation = e.createComponent(ColorAnimation.class);
    colorAnimation.alphaAnimate = true;
    colorAnimation.alphaSpeed = -1f;

    return e;
  }

  public static Entity createCity(World world, int x, int y, String name, Empire empire) {
    Entity e = world.createEntity();

    e.addComponent(new MapPosition(x, y));

    Sprite sprite = new Sprite();
    sprite.name = "cylinderwide";
    sprite.rotation = 0f;
    sprite.scaleX = 1f;
    sprite.scaleY = 1f;
    sprite.color = empire.color;
    e.addComponent(sprite);

    e.addComponent(new Description("City of " + name));

    e.addComponent(new InfluenceSource(empire));

    e.addComponent(new Name(name));

    return e;
  }

  public static Entity createEmpire(World world, Entity capital) {
    Entity e = world.createEntity();

    e.addComponent(new Discoveries());
    e.addComponent(new Diplomacy());

    e.addComponent(new Capital(capital));
    e.addComponent(new Name(capital.getComponent(Name.class).name));

    return e;
  }

  public static Entity createInfluenceLabel(World world, String label, Color color, float x, float y) {
    Entity e = world.createEntity();

    MutableMapPosition position = e.createComponent(MutableMapPosition.class);
    position.x = x;
    position.y = y;

    FadingMessage fading = e.createComponent(FadingMessage.class);
    fading.label = label;
    fading.color = color;
    fading.duration = 1.2f;
    fading.vx = 0f;
    fading.vy = 1.3f;

    return e;
  }

}
