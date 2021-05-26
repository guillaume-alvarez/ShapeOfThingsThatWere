package com.galvarez.technologies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Technology {

    private final String name;
    private final StringBuilder text = new StringBuilder();

    private final List<Technology> previous = new ArrayList<>();
    private final List<Technology> help = new ArrayList<>();

    private int rank = -1;

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

    /**
     * Return the card rank: 0 for the first ones without any previous, then 1 for the next, etc.
     */
    public int getRank() {
        if (rank < 0) {
            if (previous.isEmpty())
                rank = 0;
            else {
                for (Technology t : previous)
                    rank = Math.max(t.getRank() + 1, rank);
            }
        }
        return rank;
    }

    public boolean isRoot() {
        return previous.isEmpty() && help.isEmpty();
    }

    @Override
    public String toString() {
        return name;
    }
}
