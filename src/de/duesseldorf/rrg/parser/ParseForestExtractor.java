package de.duesseldorf.rrg.parser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.duesseldorf.rrg.RRGParseTree;

public class ParseForestExtractor {

    private enum TreeOperation {
        GOON, SUBSTITUTE;
    }

    private SimpleRRGParseChart chart;
    private List<String> tokSentence;

    public ParseForestExtractor(SimpleRRGParseChart chart,
            List<String> toksentence) {
        this.chart = chart;
        this.tokSentence = toksentence;
    }

    public Set<RRGParseTree> extractParseTrees() {
        Set<RRGParseTree> parseTrees = new HashSet<RRGParseTree>();
        Set<SimpleRRGParseItem> goals = chart.retrieveGoalItems();
        if (goals.isEmpty()) {
            System.out.println("no goal items!");
        } else {
            System.out.println("Goal items: " + chart.retrieveGoalItems());
            System.out.println("With backpointers: ");
            for (SimpleRRGParseItem goal : goals) {
                printBackpointersRec(chart, goal, "");
                System.out.println();
                RRGParseTree res = new RRGParseTree(goal.getTree().getRoot());
                System.out.println(res.toString());
                parseTrees.add(res);

            }
            extract(goals);
        }
        return parseTrees;
    }

    private void extract(Set<SimpleRRGParseItem> goals) {
        for (SimpleRRGParseItem goal : goals) {
            Set<Set<ParseItem>> backpss = chart.getBackPointers(goal);
            for (Set<ParseItem> backps : backpss) {
                // simple case:
                // if(backps)
            }
        }

    }

    /**
     * Print all backpointers of an item in a chart recursively. Initialize
     * recDepth with "".
     * 
     * @param chart
     * @param goal
     * @param recDepth
     */
    private void printBackpointersRec(SimpleRRGParseChart chart,
            SimpleRRGParseItem goal, String recDepth) {
        System.out.println(recDepth + goal);
        Set<Set<ParseItem>> backpointers = chart.getBackPointers(goal);
        for (Set<ParseItem> set : backpointers) {
            for (ParseItem parseItem : set) {
                printBackpointersRec(chart, (SimpleRRGParseItem) parseItem,
                        " " + recDepth);
            }
        }
    }

}
