package com.galvarez.ttw.model;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.galvarez.ttw.model.components.Card;
import com.galvarez.ttw.model.components.Card.DiscoveryCard;
import com.galvarez.ttw.model.components.Cards;
import com.galvarez.ttw.model.components.Discoveries;
import com.galvarez.ttw.model.data.Discovery;
import com.galvarez.ttw.model.data.SessionSettings;
import com.galvarez.ttw.model.map.GameMap;
import com.galvarez.ttw.screens.overworld.OverworldScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;

/**
 * Manipulates the cards the player can use.
 */
@Wire
public final class CardsSystem extends EntitySystem {

  private static final Logger log = LoggerFactory.getLogger(CardsSystem.class);

  private static final int HAND_SIZE = 5;

  private DiscoverySystem discoveries;

  private ComponentMapper<Cards> decks;

  public CardsSystem(SessionSettings s, GameMap map, OverworldScreen screen) {
    super(Aspect.getAspectForAll(Cards.class));
  }

  @Override
  protected void inserted(Entity e) {
    super.inserted(e);
    Cards cards = decks.get(e);

    // init deck
    discoveries.getPossibleDiscoveries(e).stream()
            .map(r -> new DiscoveryCard(r.target))
            .forEach(c -> cards.deck.add(c));
    Collections.shuffle(cards.deck);

    // then draw some cards
    drawHand(cards);
  }

  /**
   * Draw cards from deck to replenish hand.
   */
  private void drawHand(Cards cards) {
    int nb = HAND_SIZE - cards.hand.size();
    for (int i = 0; i < nb; i++) {
      Card card = cards.deck.remove(0);
      cards.hand.add(card);
    }
  }

  @Override
  protected void processEntities(ImmutableBag<Entity> entities) {

  }
}
