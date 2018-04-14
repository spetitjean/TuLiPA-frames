package de.duesseldorf.rrg.parser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.duesseldorf.rrg.RRGParseTree;

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
            for (ParseItem goal : goals) {
                parseTrees.add(new RRGParseTree(
                        ((SimpleRRGParseItem) goal).getTree()));
                extract((SimpleRRGParseItem) goal, parseTrees);
            }
        }

        return parseTrees;

    }

    private Set<RRGParseTree> extract(SimpleRRGParseItem consequent,
            Set<RRGParseTree> parseTrees) {
        Set<Set<ParseItem>> nlsset = chart.getBackPointers(consequent)
                .getBackpointers(Operation.NLS);
        // we assume that there is exactly one way to create an item using the
        // NLS rule
        if (nlsset != null) {
            // recursive call with the item that created consequent
            SimpleRRGParseItem antecedent = (SimpleRRGParseItem) nlsset
                    .iterator().next().iterator().next();
            // System.out.println("NLS cons: " + consequent);
            // System.out.println("NLS antecedent: " + antecedent);
            extract(antecedent, parseTrees);
        }
        // we assume that there is exactly one way to create an item using the
        // MOVEUP rule
        Set<Set<ParseItem>> moveUpSet = chart.getBackPointers(consequent)
                .getBackpointers(Operation.MOVEUP);
        if (moveUpSet != null) {
            // recursive call with the item that created consequent
            SimpleRRGParseItem antecedent = (SimpleRRGParseItem) moveUpSet
                    .iterator().next().iterator().next();
            // System.out.println("MOVEUP cons: " + consequent);
            // System.out.println("MOVEUP antecedent: " + antecedent);
            extract(antecedent, parseTrees);
        }
        // combine sisters
        Set<Set<ParseItem>> combineSisSet = chart.getBackPointers(consequent)
                .getBackpointers(Operation.COMBINESIS);
        if (combineSisSet != null) {
            if (combineSisSet.size() > 1) {
                System.out
                        .println("something wrong with combineSis extraction!");
            }
            for (ParseItem antecedent : combineSisSet.iterator().next()) {

                // System.out.println("COMBINESIS cons: " + consequent);
                // System.out.println("COMBINESIS antecedent: " + antecedent);
                extract((SimpleRRGParseItem) antecedent, parseTrees);
            }
        }

        // substitute
        Set<Set<ParseItem>> substituteSet = chart.getBackPointers(consequent)
                .getBackpointers(Operation.SUBSTITUTE);
        if (substituteSet != null) {
            if (substituteSet.size() > 1) {
                System.out.println("sth wrong in substitution extraction!");
            } else {
                SimpleRRGParseItem substTreeRootItem = (SimpleRRGParseItem) substituteSet
                        .iterator().next().iterator().next();
                for (RRGParseTree rrgParseTree : parseTrees) {
                    if (rrgParseTree.idequals(consequent.getTree())) {
                        System.out.println("SUBST! " + substTreeRootItem);
                        rrgParseTree
                                .findNode(consequent.getNode().getGornaddress())
                                .nodeUnification(substTreeRootItem.getNode());
                        extract(substTreeRootItem, parseTrees);
                    }
                }
            }
        }

        // left-sister adjunction
        Set<Set<ParseItem>> leftsisadjSet = chart.getBackPointers(consequent)
                .getBackpointers(Operation.LEFTADJOIN);
        if (leftsisadjSet != null) {
            if (leftsisadjSet.size() > 1) {
                System.out.println(
                        "sth wrong in left sister adjunction extraction!");
            } else {
                leftsisadjSet.iterator().
            }

        }
        return parseTrees;
    }

}
