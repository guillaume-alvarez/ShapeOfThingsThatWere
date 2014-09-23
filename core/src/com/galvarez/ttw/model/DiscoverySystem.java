package com.galvarez.ttw.model;

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.artemis.annotations.Wire;
import com.artemis.systems.VoidEntitySystem;
import com.galvarez.ttw.model.components.Research;
import com.galvarez.ttw.model.data.Discovery;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.rendering.NotificationsSystem;

/**
 * For every empire, compute the new discovery.
 * <p>
 * Every turn there is a chance a discovery will be made. This chance depends on
 * empire tiles and capital power.
 * </p>
 * 
 * @author Guillaume Alvarez
 */
@Wire
public final class DiscoverySystem extends VoidEntitySystem {

  private NotificationsSystem notifications;

  private final Map<String, Discovery> discoveries;

  private final Collection<Empire> empires;

  public DiscoverySystem(Map<String, Discovery> discoveries, Collection<Empire> empires) {
    this.discoveries = discoveries;
    this.empires = empires;
  }

  @Override
  protected void processSystem() {
    // no entities selected, just check discoveries

    for (Empire e : empires) {
      if (e.nextDiscovery != null) {
        System.out.printf("%s discoved %s from %s\n", e, e.nextDiscovery.target, e.nextDiscovery.previous);
        if (!e.isComputerControlled())
          notifications.addNotification("Discovery!", "You discovered " + e.nextDiscovery.target + " from "
              + previousString(e, e.nextDiscovery));
        e.discoveries.add(e.nextDiscovery.target);
        e.nextDiscovery = null;
      }
    }
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  public List<Research> possibleDiscoveries(Empire empire, int nb) {
    Set<String> done = empire.discoveries.stream().map(Discovery::getName).collect(toSet());
    empire.discoveries.forEach(d -> done.addAll(d.groups));

    return discoveries.values().stream() //
        .filter(d -> !done.contains(d.name) && done.containsAll(d.previous)) //
        .limit(nb) //
        .collect(ArrayList<Research>::new, (l, d) -> l.add(new Research(d.previous, d)), ArrayList::addAll);
  }

  public String previousString(Empire empire, Research next) {
    if (next.previous.isEmpty())
      return "NOTHING";

    StringBuilder sb = new StringBuilder();
    for (String previous : next.previous) {
      if (previous.matches("[A-Z]+"))
        sb.append(getDiscoveryForGroup(empire, previous).name);
      else
        sb.append(previous);
      sb.append(", ");
    }
    sb.setLength(sb.length() - 2);
    return sb.toString();
  }

  private Discovery getDiscoveryForGroup(Empire empire, String previous) {
    for (Discovery d : empire.discoveries)
      if (d.groups.contains(previous)) {
        return d;
      }
    return null;
  }
}
