package com.galvarez.technologies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Technology {

    private final String name;
    private final StringBuilder text = new StringBuilder();

    private final List<Technology> previous = new ArrayList<>();
    private final List<Technology> help = new ArrayList<>();

    Technology(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text.toString();
    }

    void appendText(String text) {
        if (this.text.length() > 0)
            this.text.append('\n');
        this.text.append(text);
    }

    public Collection<Technology> getPrevious() {
        return previous;
    }

    void addPrevious(Technology t) {
        previous.add(t);
    }

    public List<Technology> getHelp() {
        return help;
    }

    void addHelp(Technology t) {
        help.add(t);
    }

    @Override
    public String toString() {
        return name;
    }
}
