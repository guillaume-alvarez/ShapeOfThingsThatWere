package com.galvarez.ttw.model.components;

import com.artemis.Component;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.galvarez.ttw.model.EventsSystem.EventHandler;

import java.util.HashMap;
import java.util.Map;

public class EventsCount extends Component {

  public final ObjectIntMap<EventHandler> scores = new ObjectIntMap<>();

  public final ObjectIntMap<EventHandler> increment = new ObjectIntMap<>();

  public final ObjectIntMap<EventHandler> display = new ObjectIntMap<>();

  public final Map<EventHandler, String> reasons = new HashMap<>();

  public EventsCount() {
  }

}
