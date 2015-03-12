package com.galvarez.ttw.model;

import static java.lang.Math.min;
import static java.util.stream.Collectors.toList;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.galvarez.ttw.model.components.Discoveries;
import com.galvarez.ttw.model.components.Policies;
import com.galvarez.ttw.model.data.Discovery;
import com.galvarez.ttw.model.data.Policy;

/**
 * For every empire, handle policies changes.
 * <p>
 * When changing a policy, the stability decreases. It increases slowly over
 * time.
 * </p>
 * 
 * @author Guillaume Alvarez
 */
@Wire
public final class PoliciesSystem extends EntitySystem {

  private static final int STABILITY_LOSS_WHEN_SWITCHING = 20;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(PoliciesSystem.class);

  private ComponentMapper<Policies> policies;

  private ComponentMapper<Discoveries> discoveries;

  private EffectsSystem effects;

  @SuppressWarnings("unchecked")
  public PoliciesSystem() {
    super(Aspect.getAspectForAll(Policies.class));
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> entities) {
    for (Entity entity : entities) {
      Policies empire = policies.get(entity);
      if (empire.stability < empire.stabilityMax)
        empire.stability = min(empire.stabilityMax, empire.stability + empire.stabilityGrowth);
      else if (empire.stability > empire.stabilityMax)
        // decrease only one at a time
        empire.stability--;
    }
  }

  public void applyPolicy(Entity entity, Policy policy, Discovery selected) {
    Policies empire = policies.get(entity);
    Discovery old = empire.policies.put(policy, selected);
    if (old != null) {
      effects.apply(old.effects, entity, true);
      empire.stability -= STABILITY_LOSS_WHEN_SWITCHING;
    }
    if (selected != null) {
      effects.apply(selected.effects, entity, false);
    }
  }

  public List<Discovery> getAvailablePolicies(Entity empire, Policy choice) {
    return discoveries.get(empire).done.stream().filter(d -> d.groups.contains(choice.name())).collect(toList());
  }

  public static Set<Policy> getPolicies(Discovery d) {
    Set<Policy> set = null;
    for (String group : d.groups) {
      Policy p = Policy.get(group);
      if (p != null) {
        if (set == null)
          set = EnumSet.of(p);
        else
          set.add(p);
      }
    }
    return set;
  }
}
