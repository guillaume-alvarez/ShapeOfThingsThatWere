package com.galvarez.ttw.model.data;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.artemis.Entity;
import com.badlogic.gdx.graphics.Color;
import com.galvarez.ttw.model.components.Research;
import com.galvarez.ttw.rendering.components.Name;

/**
 * Represent an empire (and a player) in the game.
 * <p>
 * It can be used as a key in a {@link Map}.
 * 
 * @author Guillaume Alvarez
 */
public final class Empire {

  private static final AtomicInteger COUNTER = new AtomicInteger(0);

  public final int id;

  public final Color color;

  public final Culture culture;

  private final boolean computer;

  public final List<Discovery> discoveries = new ArrayList<>();

  public final Map<Choice, Discovery> choices = new EnumMap<>(Choice.class);

  private String name;

  private Entity capital;

  public Research nextDiscovery;

  public Empire(Color color, Culture culture, boolean computer) {
    this.id = COUNTER.getAndIncrement();
    this.color = color;
    this.culture = culture;
    this.computer = computer;
  }

  public boolean isComputerControlled() {
    return computer;
  }

  public Color getColor() {
    return color;
  }

  public Culture getCulture() {
    return culture;
  }

  /** Get the empire name, depending on its government form and history. */
  public String name() {
    return name;
  }

  public void setCapital(Entity capital) {
    this.capital = capital;
    this.name = capital.getComponent(Name.class).name;
  }

  public void setNextDiscovery(Research research) {
    System.out.println(this + " researches " + research.target);
    this.nextDiscovery = research;
  }

  public String newCityName() {
    return culture.newCityName();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;
    else if (obj instanceof Empire)
      return id == ((Empire) obj).id;
    else
      return false;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public String toString() {
    return culture.name + "(" + name() + ")";
  }

}
