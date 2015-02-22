package com.galvarez.ttw.model.components;

import java.util.Collection;

import com.artemis.Component;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.galvarez.ttw.model.Faction;
import com.galvarez.ttw.model.data.Discovery;

public class Research extends Component {

  public final Collection<Discovery> previous;

  public final ObjectFloatMap<Faction> factions;

  public final Discovery target;

  public int progress = 0;

  public Research(Discovery target, Collection<Discovery> previous) {
    this.target = target;
    this.previous = previous;

    this.factions = new ObjectFloatMap<>();
    for (Faction f : Faction.values()) {
      factions.put(f, target.factions.get(f, 0f));
      for (Discovery p : previous)
        factions.getAndIncrement(f, 0f, p.factions.get(f, 0f));
    }
  }

  @Override
  public String toString() {
    return target.name + "[" + progress + "% " + factions + "]";
  }

}
