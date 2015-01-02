package com.galvarez.ttw.model;

import static java.util.EnumSet.allOf;
import static java.util.EnumSet.complementOf;
import static java.util.EnumSet.of;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.galvarez.ttw.model.components.AIControlled;
import com.galvarez.ttw.model.components.Capital;
import com.galvarez.ttw.model.components.Diplomacy;
import com.galvarez.ttw.rendering.NotificationsSystem;
import com.galvarez.ttw.rendering.NotificationsSystem.Type;
import com.galvarez.ttw.rendering.components.Name;
import com.galvarez.ttw.screens.overworld.OverworldScreen;

/**
 * For every empire, apply diplomatic modifiers.
 * <p>
 * Each empire can make different proposals to other empires. When the are
 * compatible then the proposal is enacted. Some require both empires to agree,
 * like signing a treaty. Obviously one empire can declare war to another
 * whatever the peer wants to do.
 * 
 * @author Guillaume Alvarez
 */
@Wire
public final class DiplomaticSystem extends EntitySystem {

  private static final Logger log = LoggerFactory.getLogger(DiplomaticSystem.class);

  public enum State {
    NONE, WAR, TREATY, TRIBUTE;
  }

  public enum Action {

    NO_CHANGE("no change", null, null, allOf(State.class), a -> false),

    DECLARE_WAR("declare war", State.WAR, State.WAR, complementOf(of(State.WAR)), a -> true),

    MAKE_PEACE("make peace", State.NONE, State.NONE, of(State.WAR), a -> false),

    SIGN_TREATY("sign a treaty", State.TREATY, State.TREATY, of(State.WAR, State.NONE, State.TRIBUTE),
        a -> a == MAKE_PEACE),

    SURRENDER("surrender", State.TRIBUTE, State.TREATY, of(State.WAR), a -> a == MAKE_PEACE || a == SIGN_TREATY);

    public final String str;

    public final Set<State> before;

    private final State afterMe;

    private final State afterYou;

    private final Predicate<Action> compatibleWith;

    private Action(String str, State afterMe, State afterYou, Set<State> before, Predicate<Action> compatibleWith) {
      this.str = str;
      this.afterMe = afterMe;
      this.afterYou = afterYou;
      this.before = before;
      this.compatibleWith = compatibleWith;
    }

    public boolean compatibleWith(Action targetProposal) {
      return targetProposal == this || compatibleWith.test(targetProposal);
    }

  }

  private NotificationsSystem notifications;

  private ComponentMapper<Diplomacy> relations;

  private ComponentMapper<AIControlled> ai;

  private final OverworldScreen screen;

  private final boolean startWithDiplomacy;

  @SuppressWarnings("unchecked")
  public DiplomaticSystem(OverworldScreen screen, boolean startWithDiplomacy) {
    super(Aspect.getAspectForAll(Diplomacy.class, Capital.class));
    this.screen = screen;
    this.startWithDiplomacy = startWithDiplomacy;
  }

  @Override
  protected void inserted(Entity e) {
    super.inserted(e);
    if (startWithDiplomacy)
      relations.get(e).knownStates.addAll(Arrays.asList(State.values()));
  }

  @Override
  protected boolean checkProcessing() {
    return true;
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> entities) {
    for (Entity entity : entities) {
      Diplomacy diplo = relations.get(entity);
      for (Iterator<Entry<Entity, Action>> it = diplo.proposals.entrySet().iterator(); it.hasNext();) {
        Entry<Entity, Action> proposal = it.next();
        Entity target = proposal.getKey();
        Action action = proposal.getValue();
        Diplomacy targetDiplo = relations.get(target);
        if (action.compatibleWith(targetDiplo.proposals.get(entity))) {
          diplo.relations.put(target, action.afterMe);
          targetDiplo.relations.put(entity, action.afterYou);
          // remove the accepted proposal
          it.remove();
          targetDiplo.proposals.remove(entity);
          // prevent changing state for a few turns
          Integer turn = Integer.valueOf(screen.getTurnNumber());
          diplo.lastChange.put(target, turn);
          targetDiplo.lastChange.put(entity, turn);
          log.info("Relation between {} and {} is now {}/{}", entity.getComponent(Name.class),
              target.getComponent(Name.class), diplo.getRelationWith(target), targetDiplo.getRelationWith(entity));
          if (!ai.has(entity))
            notifications.addNotification(() -> screen.diplomacyMenu(), null, Type.DIPLOMACY,
                "Your relation with %s is now %s!", target.getComponent(Name.class), action.afterMe);
          else if (!ai.has(target))
            notifications.addNotification(() -> screen.diplomacyMenu(), null, Type.DIPLOMACY,
                "Your relation with %s is now %s!", entity.getComponent(Name.class), action.afterYou);
        }
      }
    }
  }

  public Collection<Action> getPossibleActions(Diplomacy diplo, Entity target) {
    Set<Action> actions = EnumSet.of(Action.NO_CHANGE);
    for (Action action : Action.values()) {
      if (diplo.knownStates.contains(action.afterMe) && action.before.contains(diplo.getRelationWith(target)))
        actions.add(action);
    }
    return actions;
  }
}
