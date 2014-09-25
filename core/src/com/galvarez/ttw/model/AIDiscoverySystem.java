package com.galvarez.ttw.model;

import java.util.List;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.galvarez.ttw.model.components.AIControlled;
import com.galvarez.ttw.model.components.Discoveries;
import com.galvarez.ttw.model.components.Research;

@Wire
public final class AIDiscoverySystem extends EntityProcessingSystem {

  private ComponentMapper<Discoveries> discoveries;

  private DiscoverySystem discoverySystem;

  @SuppressWarnings("unchecked")
  public AIDiscoverySystem() {
    super(Aspect.getAspectForAll(AIControlled.class, Discoveries.class));
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  @Override
  protected void process(Entity e) {
    Discoveries d = discoveries.get(e);
    List<Research> possible = discoverySystem.possibleDiscoveries(d, 1);
    if (!possible.isEmpty())
      d.nextDiscovery = possible.get(0);
  }

}
