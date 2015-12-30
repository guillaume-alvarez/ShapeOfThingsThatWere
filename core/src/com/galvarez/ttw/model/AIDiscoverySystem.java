package com.galvarez.ttw.model;

import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.galvarez.ttw.model.components.AIControlled;
import com.galvarez.ttw.model.components.Discoveries;
import com.galvarez.ttw.model.components.Policies;
import com.galvarez.ttw.model.components.Research;
import com.galvarez.ttw.model.data.Culture;
import com.galvarez.ttw.model.data.Discovery;
import com.galvarez.ttw.model.data.Empire;
import com.galvarez.ttw.model.data.Policy;
import com.galvarez.ttw.rendering.components.Name;

@Wire
public final class AIDiscoverySystem extends EntityProcessingSystem {

  private static final Logger log = LoggerFactory.getLogger(AIDiscoverySystem.class);

  private ComponentMapper<Policies> policies;

  private ComponentMapper<Empire> empires;

  private DiscoverySystem system;

  @SuppressWarnings("unchecked")
  public AIDiscoverySystem() {
    super(Aspect.getAspectForAll(AIControlled.class, Discoveries.class, Empire.class));
  }

  @Override
  protected boolean checkProcessing() {
    return false;
  }

  @Override
  protected void process(Entity e) {
    // should not be called, selectNewDiscovery will be called when an event
    // triggers a discovery
  }

  public void selectNewDiscovery(Entity empire, Discoveries lab) {
    Culture culture = empires.get(empire).culture;
    Entry<Faction, Research> prefered = null;
    float max = Float.NEGATIVE_INFINITY;
    for (Entry<Faction, Research> possible : lab.nextPossible.entrySet()) {
      Faction faction = possible.getKey();
      float score = culture.ai.get(faction) * possible.getValue().target.factions.get(faction, 1);
      if (score > max) {
        max = score;
        prefered = possible;
      }
    }

    if (prefered != null) {
      log.debug("{} follows {} advice to investigate {} from {}", empire.getComponent(Name.class), prefered.getKey(),
          prefered.getValue().target, prefered.getValue().previous);
      system.discoverNew(empire, lab, prefered.getValue());
    }

    Set<Policy> newPolicies;
    if (lab.last != null && (newPolicies = PoliciesSystem.getPolicies(lab.last.target)) != null) {
      Policies empirePolicies = policies.get(empire);
      for (Policy p : newPolicies) {
        Discovery newD = lab.last.target;
        Discovery oldD = empirePolicies.policies.get(p);
        float oldScore = policyScore(oldD, culture);
        float newScore = policyScore(newD, culture);
        if (newScore > oldScore) {
          log.info("{} replaced policy {}={}(score={}) by {}(score={})", empire.getComponent(Name.class), p, oldD,
              oldScore, newD, newScore);
          empirePolicies.policies.put(p, newD);
        } else
          log.debug("{} did not replace policy {}={}(score={}) by {}(score={})", empire.getComponent(Name.class), p,
              oldD, oldScore, newD, newScore);
      }
    }
  }

  private static float policyScore(Discovery d, Culture culture) {
    if (d == null)
      return 0f;
    float score = 0f;
    for (Faction f : Faction.values())
      score += d.factions.get(f, 0f) * culture.ai.get(f).floatValue();
    return score;
  }

}
