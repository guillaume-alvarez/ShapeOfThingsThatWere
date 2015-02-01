package com.galvarez.ttw.model;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.VoidEntitySystem;
import com.galvarez.ttw.model.components.Destination;
import com.galvarez.ttw.model.components.Discoveries;
import com.galvarez.ttw.model.data.Discovery;
import com.galvarez.ttw.rendering.NotificationsSystem;
import com.galvarez.ttw.rendering.NotificationsSystem.Type;
import com.galvarez.ttw.rendering.components.Description;
import com.galvarez.ttw.rendering.components.Name;

/**
 * Contains 'special discoveries', having special effects not covered by
 * {@link EffectsSystem}.
 * 
 * @author Guillaume Alvarez
 */
@Wire
public final class SpecialDiscoveriesSystem extends VoidEntitySystem {

  private static final Logger log = LoggerFactory.getLogger(SpecialDiscoveriesSystem.class);

  private static final String VILLAGE = "Village";

  private static final String CITY = "City";

  private ComponentMapper<Name> names;

  private ComponentMapper<Description> descriptions;

  private NotificationsSystem notifications;

  private interface Special {
    void apply(Entity empire, Discovery d, Discoveries discoveries);
  }

  private final Map<String, Special> effects = new HashMap<>();

  public SpecialDiscoveriesSystem() {
    effects.put(VILLAGE, new Special() {
      @Override
      public void apply(Entity empire, Discovery d, Discoveries discoveries) {
        if (!discoveries.done.contains(CITY)) {
          String name = names.get(empire).name;
          String desc = "Village of " + name;
          setDescription(empire, name, desc);
          cannotMove(empire);
        }
      }
    });
    effects.put(CITY, new Special() {
      @Override
      public void apply(Entity empire, Discovery d, Discoveries discoveries) {
        String name = names.get(empire).name;
        String desc = "City of " + name;
        setDescription(empire, name, desc);
        cannotMove(empire);
      }
    });
  }

  @Override
  protected void processSystem() {
    // nothing to do, called by other systems to activate special effects
  }

  private void setDescription(Entity empire, String name, String desc) {
    descriptions.get(empire).desc = desc;
    log.info("{} is now known as '{}'", name, desc);
    // notify all empires to player
    notifications.addNotification(null, null, Type.BUILDINGS, "%s is now known as '%s'", name, desc);
  }

  private void cannotMove(Entity capital) {
    capital.edit().remove(Destination.class);
  }

  public void checkDiscoveries(Map<String, Discovery> discoveries) {
    for (String d : effects.keySet())
      if (!discoveries.containsKey(d))
        log.warn("Cannot find discovery '{}' that has a special effect.", d);
  }

  public void apply(Entity empire, Discovery d, Discoveries discoveries) {
    Special effect = effects.get(d.name);
    if (effect != null)
      effect.apply(empire, d, discoveries);
  }

}
