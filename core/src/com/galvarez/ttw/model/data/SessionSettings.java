package com.galvarez.ttw.model.data;

import static java.lang.Math.abs;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Json;
import com.galvarez.ttw.model.map.MapGenerator;
import com.galvarez.ttw.utils.BooleanValue;
import com.galvarez.ttw.utils.EnumValue;

public final class SessionSettings implements Cloneable {

  /** List different colors that are easy to recognize on-screen. */
  public static final List<Color> COLORS = Arrays.asList(Color.RED, Color.GREEN, Color.BLUE, Color.PURPLE,
      Color.YELLOW, Color.TEAL, Color.WHITE, Color.BLACK, Color.GRAY, Color.PINK, Color.LIGHT_GRAY, Color.ORANGE,
      Color.MAGENTA, Color.DARK_GRAY, Color.CYAN, Color.OLIVE, Color.MAROON, Color.NAVY);

  private final Map<String, Culture> cultures;

  private final Map<String, Discovery> discoveries;

  public final List<Empire> empires = new ArrayList<>();

  public final Properties map;

  public final EnumValue<MapGenerator.Generator> mapType;

  public final BooleanValue startWithDiplomacy = new BooleanValue(false);

  public SessionSettings() {
    // init map parameters
    mapType = new EnumValue<>(MapGenerator.Generator.ISLAND);
    map = mapType.get().algo.getDefaultValues();

    // load cultures from data file
    Json json = new Json();
    this.cultures = cultures(json).stream().collect(toMap(Culture::getName, c -> c));
    this.discoveries = discoveries(json).stream().collect(toMap(Discovery::getName, c -> c));

    // generate some default empires
    boolean ai = false;
    for (Culture culture : cultures.values()) {
      empires.add(new Empire(guessColor(), culture, ai));
      ai = true;
    }
  }

  public SessionSettings(SessionSettings settings) {
    // load cultures and discoveries from data file
    Json json = new Json();
    this.cultures = cultures(json).stream().collect(toMap(Culture::getName, c -> c));
    this.discoveries = discoveries(json).stream().collect(toMap(Discovery::getName, c -> c));

    // copy map creation options
    this.map = new Properties();
    this.map.putAll(settings.map);
    this.mapType = new EnumValue<>(MapGenerator.Generator.ISLAND);
    this.mapType.set(settings.mapType.get());
    this.startWithDiplomacy.set(settings.startWithDiplomacy.get());

    // copy empires using new cultures (as they have a city iterator)
    for (Empire empire : settings.empires)
      this.empires.add(new Empire(empire.color, cultures.get(empire.culture.name), empire.isComputerControlled()));
  }

  private static List<Culture> cultures(Json json) {
    json.setSerializer(Culture.class, Culture.SER);
    List<Culture> cultures = new ArrayList<>();
    for (FileHandle f : files("data/cultures/", "antiquity.json"))
      cultures.addAll(Arrays.asList(json.fromJson(Culture[].class, f)));
    return cultures;
  }

  private static List<Discovery> discoveries(Json json) {
    // TODO add ideas from http://doodlegod.wikia.com/wiki/Doodle_Devil
    // TODO add ideas from http://doodlegod.wikia.com/wiki/Doodle_God_2
    // TODO The mythic discoveries could be used as 'totem' animals
    json.setSerializer(Discovery.class, Discovery.SER);
    List<Discovery> discoveries = new ArrayList<>();
    for (FileHandle f : files("data/discoveries/", "nature.json", "prehistory.json", "antiquity.json", "classic.json"))
      discoveries.addAll(Arrays.asList(json.fromJson(Discovery[].class, f)));
    return discoveries;
  }

  private static Iterable<FileHandle> files(String dir, String ... files) {
    return Arrays.stream(files).map(f -> Gdx.files.internal(dir + f)).collect(toList());
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

  private final Random rand = new Random();

  public Culture guessCulture() {
    Map<Culture, Integer> usedCultures = cultures.values().stream().distinct()
        .collect(toMap(c -> c, c -> (int) empires.stream().filter(e -> e.culture == c).count()));
    List<Culture> list = cultures
        .values()
        .stream()
        .sorted(
            (c1, c2) -> -Integer.compare(c1.cities.size - usedCultures.get(c1), c2.cities.size - usedCultures.get(c2)))
        .limit(3).collect(toList());
    if (list.isEmpty())
      throw new IllegalStateException("No remaining culture!");
    return list.get(abs(rand.nextInt() % list.size()));
  }
}
