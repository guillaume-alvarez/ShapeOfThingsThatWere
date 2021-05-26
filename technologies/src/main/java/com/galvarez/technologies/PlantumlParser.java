package com.galvarez.technologies;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class PlantumlParser {


    private final Map<String, Technology> technologies = new HashMap<>();

    private Technology current = null;

    PlantumlParser() {
    }

    Map<String, Technology> parse(BufferedReader reader) throws IOException {
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            line = line.trim();
            if (current == null) {
                if (!parseNodeStart(line)) {
                    parseLink(line);
                }
            } else {
                parseNodeContent(line);
            }
        }
        return technologies;
    }

    private static final Pattern NODE_START = Pattern.compile("node\\s+(\\w+)\\s+\\[");

    private boolean parseNodeStart(String line) {
        Matcher matcher = NODE_START.matcher(line);
        if (matcher.matches()) {
            current = new Technology(matcher.group(1));
            return true;
        }
        return false;
    }


    private static final Pattern NODE_END = Pattern.compile("\\]");

    private void parseNodeContent(String line) {
        Matcher matcher = NODE_END.matcher(line);
        if (matcher.matches()) {
            technologies.put(current.getName(), current);
            current = null;
        } else {
            current.appendText(line.trim());
        }
    }

    private static final Pattern LINK = Pattern.compile("(\\w+)\\s+([\\.-]+>)\\s+(\\w+)");

    private boolean parseLink(String line) {
        Matcher matcher = LINK.matcher(line);
        if (matcher.matches()) {
            Technology source = technologies.get(matcher.group(1));
            Technology target = technologies.get(matcher.group(3));
            if (matcher.group(2).contains("-->"))
                target.addPrevious(source);
            else
                target.addHelp(source);
            return true;
        }
        return false;
    }

}
