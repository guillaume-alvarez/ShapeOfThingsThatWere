package com.galvarez.ttw.model.data;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.artemis.Component;
import com.badlogic.gdx.graphics.Color;

/**
 * Represent an empire (and a player) in the game.
 * <p>
 * It can be used as a key in a {@link Map}.
 * 
 * @author Guillaume Alvarez
 */
public final class Empire extends Component {

  private static final AtomicInteger COUNTER = new AtomicInteger(0);

  public final int id;

  public final Color color;

  public final Culture culture;

  private final boolean computer;

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
    return culture.name + "(ia=" + computer + ")";
  }

}
