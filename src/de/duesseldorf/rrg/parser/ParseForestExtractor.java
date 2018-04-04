package de.duesseldorf.rrg.parser;

import java.util.List;
import java.util.Set;

public class ParseForestExtractor {

    public ParseForestExtractor(SimpleRRGParseChart chart,
            List<String> toksentence) {

        Set<SimpleRRGParseItem> goals = chart.retrieveGoalItems();
        if (goals.isEmpty()) {
            System.out.println("no goal items!");
        } else {
            System.out.println("Goal items: " + chart.retrieveGoalItems());
            for (SimpleRRGParseItem goal : goals) {
                printRec(chart, goal, "");
            }
        }

    }

    private void printRec(SimpleRRGParseChart chart, SimpleRRGParseItem goal,
            String recDepth) {
        System.out.println(recDepth + goal);
        Set<Set<ParseItem>> backpointers = chart.getBackPointers(goal);
        for (Set<ParseItem> set : backpointers) {
            for (ParseItem parseItem : set) {
                printRec(chart, (SimpleRRGParseItem) parseItem, " " + recDepth);
            }
        }
    }

}
