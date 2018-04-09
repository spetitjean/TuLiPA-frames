package de.duesseldorf.rrg.parser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.RRGParseTree;
import de.tuebingen.util.CollectionUtilities;

public class ParseForestExtractor {

    private enum TreeOperation {
        GOON, SUBSTITUTE, SISADJ, WRAP;
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
        Set<ParseItem> goals = chart.retrieveGoalItems();
        if (goals.isEmpty()) {
            System.out.println("no goal items!");
        } else {
            System.out.println("Goal items: " + chart.retrieveGoalItems());
            System.out.println("With backpointers: ");
            for (ParseItem goal : goals) {
                printBackpointersRec(chart, (SimpleRRGParseItem) goal, "");
                System.out.println();
                RRGParseTree res = new RRGParseTree(
                        ((SimpleRRGParseItem) goal).getTree().getRoot());
                System.out.println(res.toString());
                parseTrees.add(res);
            }
            extract(goals, parseTrees);
        }
        return parseTrees;
    }

    private void extract(Set<ParseItem> consequent,
            Set<RRGParseTree> parseTrees) {
        for (ParseItem goal : consequent) {
            Set<Set<ParseItem>> allAntecedents = chart
                    .getBackPointers((SimpleRRGParseItem) goal);
            if (CollectionUtilities.emtySetofSets(allAntecedents)) {
                return;
            }
            for (Set<ParseItem> backps : allAntecedents) {
                TreeOperation whatsNext = whatsNext(backps);
                // System.out.println("extract " + backps);
                if (whatsNext.equals(TreeOperation.GOON)) {
                    extract(backps, parseTrees);
                    System.out.println("go on!");
                } else if (whatsNext.equals(TreeOperation.SUBSTITUTE)) {
                    System.out.println("subst!");
                    extract(backps, parseTrees);
                } else {
                    System.out.println("whaaat " + backps);
                }
            }
        }

    }

    private TreeOperation whatsNext(Set<ParseItem> backps) {
        if (backps.size() == 1) {
            SimpleRRGParseItem single = (SimpleRRGParseItem) backps.iterator()
                    .next();
            if (single.getNode().getType().equals(RRGNodeType.SUBST)) {
                return TreeOperation.SUBSTITUTE;
            } else {
                return TreeOperation.GOON;
            }
        } else {
            System.out.println("different: " + backps);
        }
        return TreeOperation.SISADJ;
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
