package com.galvarez.ttw.utils;

import com.badlogic.gdx.utils.Json;
import com.galvarez.ttw.model.data.Discovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public final class DiscoveriesToPlantuml {

    private static final Logger log = LoggerFactory.getLogger(DiscoveriesToPlantuml.class);


    private static List<Discovery> discoveries() throws IOException {
        Json json = new Json();
        json.setSerializer(Discovery.class, Discovery.SER);
        List<Discovery> discoveries = new ArrayList<>();
        for (String f : Arrays.asList("nature.json", "prehistory.json", "antiquity.json", "classic.json")) {
            FileInputStream is = new FileInputStream("core/assets/data/discoveries/" + f);
            discoveries.addAll(Arrays.asList(json.fromJson(Discovery[].class, is)));
        }
        return discoveries;
    }

    private static String id(String name) {
        return name.replace(' ', '_').replace('-', '_');
    }

    public static void main(String... args) {
        try {
            Collection<Discovery> discoveries = discoveries();
            FileWriter w = new FileWriter(new File("discoveries.plantuml"));
            w.append("@startuml\n");

            Map<String, Set<String>> groups = new HashMap<>();
            Set<String> notInGroup = new HashSet<>();
            for (Discovery d : discoveries) {
                if (d.groups != null && !d.groups.isEmpty()) {
                    for (String g : d.groups) {
                        Set<String> group = groups.get(g);
                        if (group == null)
                            groups.put(g, group = new HashSet<>());
                        group.add(d.name);
                    }
                } else
                    notInGroup.add(d.name);
            }

            // first print groups
            for (String group : groups.keySet()) {
                w.append(String.format("frame %s {\n", group));
                for (String d : groups.get(group)) {
                    w.append(String.format("  rectangle %s\n", id(d)));
                }
                w.append(String.format("}\n", group));
            }

            // then discoveries not in a group
            for (String d : notInGroup) {
                w.append(String.format("rectangle %s\n", id(d)));
            }

            // then links between them
            for (Discovery d : discoveries) {
                Set<String> previous = d.previous.stream().flatMap(s -> s.stream()).collect(Collectors.toSet());
                for (String p : previous) {
                    w.append(String.format("%s --> %s \n", id(p), id(d.name)));
                }
            }

            w.append("@enduml\n");
            w.flush();
            w.close();
        } catch (Exception e) {
            log.error("Cannot write discoveries: " + e, e);
        }
    }

}
