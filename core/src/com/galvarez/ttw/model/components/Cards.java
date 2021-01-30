package com.galvarez.ttw.model.components;

import com.artemis.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Cards available to the player.
 */
public final class Cards extends Component {

  public final List<Card> hand = new LinkedList<>();

  public final List<Card> deck = new LinkedList<>();

}
