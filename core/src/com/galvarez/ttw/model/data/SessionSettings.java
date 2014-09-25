package com.galvarez.ttw.model.data;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Json;
import com.galvarez.ttw.utils.IntValue;

public final class SessionSettings {

  /** List different colors that are easy to recognize on-screen. */
  public static final List<Color> COLORS = Arrays.asList(Color.RED, Color.GREEN, Color.BLUE, Color.PURPLE,
      Color.YELLOW, Color.TEAL, Color.WHITE, Color.BLACK, Color.GRAY, Color.PINK, Color.LIGHT_GRAY, Color.ORANGE,
      Color.MAGENTA, Color.DARK_GRAY, Color.CYAN, Color.OLIVE, Color.MAROON, Color.NAVY);

  private final Map<String, Culture> cultures;

  private final Map<String, Discovery> discoveries;

  public final List<Empire> empires = new ArrayList<>();

  public final IntValue mapNoise, mapWidth, mapHeight;

  public SessionSettings() {
    // init map parameters
    mapNoise = new IntValue(4);
    mapWidth = new IntValue(2);
    mapHeight = new IntValue(2);

    // load cultures from data file
    Json json = new Json();
    this.cultures = cultures(json).stream().collect(Collectors.toMap(Culture::getName, c -> c));
    this.discoveries = discoveries(json).stream().collect(Collectors.toMap(Discovery::getName, c -> c));

    // generate some default empires
    empires.add(new Empire(Color.BLACK, cultures.get("Babylonian"), false));
    empires.add(new Empire(Color.BLUE, cultures.get("Greek"), true));
    empires.add(new Empire(Color.GREEN, cultures.get("Minoan"), true));
    empires.add(new Empire(Color.RED, cultures.get("Roman"), true));
    empires.add(new Empire(Color.TEAL, cultures.get("Macedonian"), true));
    empires.add(new Empire(Color.YELLOW, cultures.get("Egyptian"), true));
    empires.add(new Empire(Color.PURPLE, cultures.get("Hebraic"), true));
  }

  private static List<Culture> cultures(Json json) {
    return Arrays.asList(json.fromJson(Culture[].class, Gdx.files.internal("data/cultures.json")));
  }

  private static List<Discovery> discoveries(Json json) {
    // TODO add ideas from http://doodlegod.wikia.com/wiki/Doodle_Devil
    // TODO add ideas from http://doodlegod.wikia.com/wiki/Doodle_God_2
    // TODO The mythic discoveries could be used as 'totem' animals
    return Arrays.asList(json.fromJson(Discovery[].class, Gdx.files.internal("data/discoveries.json")));
  }

  public Map<String, Discovery> getDiscoveries() {
    return discoveries;
  }

  public Collection<Culture> getCultures() {
    return Collections.unmodifiableCollection(cultures.values());
  }

  public Culture getCulture(String name) {
    return cultures.get(name);
  }

  public Color guessColor() {
    List<Color> usedColors = empires.stream().map(Empire::getColor).collect(toList());
    Optional<Color> color = COLORS.stream().filter(c -> !usedColors.contains(c)).findFirst();
    if (color.isPresent())
      return color.get();
    else {
      // must use an existing color
      return COLORS.get(new Random().nextInt(COLORS.size()));
    }
  }

  public Culture guessCulture() {
    Map<Culture, Integer> usedCultures = empires.stream().map(Empire::getCulture).distinct().collect(toMap(//
        c -> c,//
        c -> (int) empires.stream().filter(e -> e.culture == c).count()));
    Optional<Culture> culture = cultures.values().stream()
        .filter(c -> !usedCultures.containsKey(c) || usedCultures.get(c) < c.cities.size).findFirst();
    if (culture.isPresent())
      return culture.get();
    else
      throw new IllegalStateException("No remaining culture!");
  }
}
