package com.galvarez.ttw.model;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.galvarez.ttw.model.components.AIControlled;
import com.galvarez.ttw.model.components.Discoveries;
import com.galvarez.ttw.model.components.Research;
import com.galvarez.ttw.model.data.Culture;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.rendering.components.Name;

@Wire
public final class AIDiscoverySystem extends EntityProcessingSystem {

  private static final Logger log = LoggerFactory.getLogger(AIDiscoverySystem.class);

  private ComponentMapper<Discoveries> discoveries;

  private ComponentMapper<Empire> empires;

  private DiscoverySystem discoverySystem;

  @SuppressWarnings("unchecked")
  public AIDiscoverySystem() {
    super(Aspect.getAspectForAll(AIControlled.class, Discoveries.class, Empire.class));
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  @Override
  protected void process(Entity e) {
    Discoveries d = discoveries.get(e);
    if (d.next == null) {
      Culture culture = empires.get(e).culture;
      Entry<Faction, Research> prefered = null;
      float max = Float.NEGATIVE_INFINITY;
      for (Entry<Faction, Research> possible : discoverySystem.possibleDiscoveries(e, d).entrySet()) {
        Faction faction = possible.getKey();
        float score = culture.ai.get(faction) * possible.getValue().target.factions.get(faction, 1);
        if (score > max) {
          max = score;
          prefered = possible;
        }
      }
      if (prefered != null) {
        log.info("{} follows {} advice to investigate {}", e.getComponent(Name.class), prefered.getKey(),
            prefered.getValue().target);
        d.next = prefered.getValue();
      }
    }
  }
}
