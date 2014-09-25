package com.galvarez.ttw.model;

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.galvarez.ttw.model.components.AIControlled;
import com.galvarez.ttw.model.components.Discoveries;
import com.galvarez.ttw.model.components.Research;
import com.galvarez.ttw.model.data.Discovery;
import com.galvarez.ttw.rendering.NotificationsSystem;
import com.galvarez.ttw.rendering.components.Name;

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
public final class DiscoverySystem extends EntitySystem {

  private NotificationsSystem notifications;

  private final Map<String, Discovery> discoveries;

  private ComponentMapper<Discoveries> empires;

  private ComponentMapper<AIControlled> ia;

  @SuppressWarnings("unchecked")
  public DiscoverySystem(Map<String, Discovery> discoveries) {
    super(Aspect.getAspectForAll(Discoveries.class));
    this.discoveries = discoveries;
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> entities) {
    for (Entity entity : entities) {
      Discoveries e = empires.get(entity);
      if (e.nextDiscovery != null) {
        System.out.printf("%s discoved %s from %s\n", entity.getComponent(Name.class), e.nextDiscovery.target,
            e.nextDiscovery.previous);
        if (!ia.has(entity))
          notifications.addNotification("Discovery!", "You discovered " + e.nextDiscovery.target + " from "
              + previousString(e, e.nextDiscovery));
        e.discovered.add(e.nextDiscovery.target);
        e.nextDiscovery = null;
      }
    }
  }

  public List<Research> possibleDiscoveries(Discoveries empire, int nb) {
    Set<String> done = empire.discovered.stream().map(Discovery::getName).collect(toSet());
    empire.discovered.forEach(d -> done.addAll(d.groups));

    return discoveries.values().stream() //
        .filter(d -> !done.contains(d.name) && done.containsAll(d.previous)) //
        .limit(nb) //
        .collect(ArrayList<Research>::new, (l, d) -> l.add(new Research(d.previous, d)), ArrayList::addAll);
  }

  public String previousString(Discoveries empire, Research next) {
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

  private Discovery getDiscoveryForGroup(Discoveries empire, String previous) {
    for (Discovery d : empire.discovered)
      if (d.groups.contains(previous)) {
        return d;
      }
    return null;
  }
}
