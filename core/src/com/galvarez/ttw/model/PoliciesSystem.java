package com.galvarez.ttw.model;

import static java.util.stream.Collectors.toList;

import java.util.List;

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

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(PoliciesSystem.class);

  private ComponentMapper<Policies> policies;

  private ComponentMapper<Discoveries> discoveries;

  private DiscoverySystem discoveriesSystem;

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
      if (empire.stability < 100)
        empire.stability += 1;
    }
  }

  public void applyPolicy(Entity entity, Policy policy, Discovery selected) {
    Policies empire = policies.get(entity);
    Discovery old = empire.policies.put(policy, selected);
    if (old != null) {
      discoveriesSystem.applyDiscoveryEffects(old, entity, true);
      empire.stability -= 20;
    }
    discoveriesSystem.applyDiscoveryEffects(selected, entity, false);
  }

  public List<Discovery> getAvailablePolicies(Entity empire, Policy choice) {
    return discoveries.get(empire).done.stream().filter(d -> d.groups.contains(choice.name())).collect(toList());
  }
}
