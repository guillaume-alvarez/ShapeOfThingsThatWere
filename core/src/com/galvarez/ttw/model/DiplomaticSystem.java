package com.galvarez.ttw.model;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.galvarez.ttw.model.components.Capital;
import com.galvarez.ttw.model.components.Diplomacy;

/**
 * For every empire, apply diplomatic modifiers.
 * 
 * @author Guillaume Alvarez
 */
@Wire
public final class DiplomaticSystem extends EntitySystem {

  public enum State {
    NONE, WAR, TREATY, TRIBUTE;
  }

  private ComponentMapper<Diplomacy> relations;

  @SuppressWarnings("unchecked")
  public DiplomaticSystem() {
    super(Aspect.getAspectForAll(Diplomacy.class, Capital.class));
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> entities) {
    for (Entity entity : entities) {
      // TODO
    }
  }
}
