package de.duesseldorf.rrg.extractor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.duesseldorf.rrg.RRGParseTree;
import de.duesseldorf.rrg.RRGTree;
import de.duesseldorf.rrg.parser.Backpointer;
import de.duesseldorf.rrg.parser.Operation;
import de.duesseldorf.rrg.parser.ParseItem;
import de.duesseldorf.rrg.parser.SimpleRRGParseChart;
import de.duesseldorf.rrg.parser.SimpleRRGParseItem;
import de.duesseldorf.util.GornAddress;
import de.tuebingen.util.TextUtilities;

public class NewParseForestExtractor {

    private SimpleRRGParseChart parseChart;
    Set<RRGParseTree> resultingParses;

    // private Map<ParseItem, Set<Pair>> extractionAgenda;

    private Set<Operation> sameElemTree = new HashSet<Operation>(Arrays
            .asList(Operation.COMBINESIS, Operation.MOVEUP, Operation.NLS));
    private List<String> toksentence;

    public NewParseForestExtractor(SimpleRRGParseChart parseChart,
            List<String> toksentence) {
        this.parseChart = parseChart;
        this.toksentence = toksentence;
        resultingParses = new HashSet<RRGParseTree>();
    }

    public Set<RRGParseTree> extractParseTrees() {
        // find goal items in the chart. Extract them all and add the set of
        // parse trees derived from them to the resulting parses
        Set<ParseItem> goals = parseChart.retrieveGoalItems();
        for (ParseItem goal : goals) {
            ExtractionStep initExtrStep = initialExtractionStep(
                    (SimpleRRGParseItem) goal);
            Set<RRGParseTree> resultingTrees = extract(initExtrStep);
            addToResultingParses(resultingTrees);
        }
        return resultingParses;
    }

    /**
     * this method takes a
     * 
     * @param goal
     *            item and createsthe initial parse step from that item
     */
    private ExtractionStep initialExtractionStep(SimpleRRGParseItem goal) {
        ExtractionStep result = new ExtractionStep(goal,
                new GornAddress(), new RRGParseTree(goal.getTree()));
        return result;
    }

    private void addToResultingParses(Set<RRGParseTree> resultingTrees) {
        for (RRGParseTree resultingParseTree : resultingTrees) {
            String newId = TextUtilities.appendList(toksentence, "_")
                    + resultingParses.size();
            resultingParseTree.setId(newId);
            resultingParses.add(resultingParseTree);
        }
    }

    private Set<RRGParseTree> extract(ExtractionStep extractionstep) {
        Backpointer backPointers = parseChart
                .getBackPointers(extractionstep.getCurrentItem());

        System.out.println(extractionstep);
        // distinguish different operations here
        Set<RRGParseTree> parsesInThisStep = new HashSet<RRGParseTree>();
        // NLS
        parsesInThisStep.addAll(extractNLS(
                backPointers.getAntecedents(Operation.NLS), extractionstep));

        // Move-Up
        Set<List<ParseItem>> moveupAntecedents = backPointers
                .getAntecedents(Operation.MOVEUP);
        parsesInThisStep
                .addAll(extractMoveUp(moveupAntecedents, extractionstep));

        // Combine-Sisters
        Set<List<ParseItem>> combsisAntecedents = backPointers
                .getAntecedents(Operation.COMBINESIS);
        parsesInThisStep
                .addAll(extractCombSis(combsisAntecedents, extractionstep));

        // Substitution
        Set<List<ParseItem>> substAntecedents = backPointers
                .getAntecedents(Operation.SUBSTITUTE);
        parsesInThisStep.addAll(extractSubst(substAntecedents, extractionstep));

        // Left-Adjoin TODO
        Set<List<ParseItem>> leftAdjAntecedents = backPointers
                .getAntecedents(Operation.LEFTADJOIN);
        parsesInThisStep
                .addAll(extractLeftAdjoin(leftAdjAntecedents, extractionstep));

        // Right-Adjoin TODO
        Set<List<ParseItem>> rightAdjAntecedents = backPointers
                .getAntecedents(Operation.RIGHTADJOIN);
        parsesInThisStep.addAll(
                extractRightAdjoin(rightAdjAntecedents, extractionstep));

        // Complete-Wrapping TODO
        Set<List<ParseItem>> coWrAntecedents = backPointers
                .getAntecedents(Operation.COMPLETEWRAPPING);
        parsesInThisStep.addAll(
                extractCompleteWrapping(coWrAntecedents, extractionstep));

        // Predict-Wrapping TODO
        Set<List<ParseItem>> prWrAntecedents = backPointers
                .getAntecedents(Operation.PREDICTWRAPPING);
        parsesInThisStep.addAll(
                extractPredictWrapping(prWrAntecedents, extractionstep));

        // if no other rule applied (i.e. if we dealt with a scanned item):
        if (parsesInThisStep.isEmpty()) {
            parsesInThisStep.add(extractionstep.getCurrentParseTree());
        }
        // for (RRGParseTree parseInThisStep : parsesInThisStep) {
        // System.out.println(parseInThisStep);
        // }
        return parsesInThisStep;

        // Idee für Ambiguität:
        // Gehe depth-first durch den gesamten Baum.
        // Wenn alles unambig ist, gehe wieder nach oben zurück und füge dann
        // den Baum hinzu.
        // Wenn etwas ambig ist, füge den aktuellen Parsebaum hinzu, bevor die
        // zweite, dritte... Möglichkeit durchextrahiert wird.
    }

    private Set<RRGParseTree> extractPredictWrapping(
            Set<List<ParseItem>> predictWrappingAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisPWStep = new HashSet<RRGParseTree>();
        for (List<ParseItem> predictWrappingantecedentItems : predictWrappingAntecedents) {
        }
        return parsesInThisPWStep;
    }

    private Set<RRGParseTree> extractCompleteWrapping(
            Set<List<ParseItem>> completeWrappingAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisCWStep = new HashSet<RRGParseTree>();
        for (List<ParseItem> CWantecedentItems : completeWrappingAntecedents) {
        }
        return parsesInThisCWStep;
    }

    private Set<RRGParseTree> extractRightAdjoin(
            Set<List<ParseItem>> rightAdjAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisRightAdjStep = new HashSet<RRGParseTree>();
        for (List<ParseItem> rightAdjantecedentItems : rightAdjAntecedents) {
        }
        return parsesInThisRightAdjStep;
    }

    private Set<RRGParseTree> extractLeftAdjoin(
            Set<List<ParseItem>> leftAdjAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisLeftAdjStep = new HashSet<RRGParseTree>();
        for (List<ParseItem> leftAdjantecedentItems : leftAdjAntecedents) {
        }
        return parsesInThisLeftAdjStep;
    }

    private Set<RRGParseTree> extractSubst(
            Set<List<ParseItem>> substAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisSUBSTStep = new HashSet<RRGParseTree>();
        for (List<ParseItem> substantecedentItemSingletonList : substAntecedents) {
            SimpleRRGParseItem substAntecedentItem = (SimpleRRGParseItem) substantecedentItemSingletonList
                    .get(0);
            GornAddress GAtoReplaceAt = extractionstep.getGAInParseTree();
            RRGTree substTree = substAntecedentItem.getTree();
            RRGParseTree nextStepParseTree = extractionstep
                    .getCurrentParseTree().substitute(substTree, GAtoReplaceAt);
            ExtractionStep nextStep = new ExtractionStep(
                    substAntecedentItem, extractionstep.getGAInParseTree(),
                    nextStepParseTree);
            parsesInThisSUBSTStep.addAll(extract(nextStep));
        }
        return parsesInThisSUBSTStep;
    }

    private Set<RRGParseTree> extractCombSis(
            Set<List<ParseItem>> combsisAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisCombSisStep = new HashSet<RRGParseTree>();
        // how many different antecedents are there?
        for (List<ParseItem> combsisantecedentItems : combsisAntecedents) {
            Set<RRGParseTree> tmpResult = new HashSet<RRGParseTree>();
            tmpResult.add(extractionstep.getCurrentParseTree());
            for (int i = 0; i < combsisantecedentItems.size(); i++) {
                SimpleRRGParseItem combsisantecedentItem = (SimpleRRGParseItem) combsisantecedentItems
                        .get(i);
                // idea of this part: The inner for loop only takes one round
                // when extracting the first item from the list. When extracting
                // the first item, add all extracted parse trees to tmpResult.
                // Then, you can further elaborate those possibilities with a
                // second take on the inner for loop.
                for (RRGParseTree oneTree : tmpResult) {
                    ExtractionStep nextStep;
                    GornAddress GAtoExtractFrom;
                    boolean currentAntecedentIsRightSister = ((SimpleRRGParseItem) combsisantecedentItem)
                            .getNode().getGornaddress()
                            .equals(extractionstep.getCurrentItem().getNode()
                                    .getGornaddress());
                    // First, find the GA from which to further extract
                    // processing the right sister
                    if (currentAntecedentIsRightSister) {
                        GAtoExtractFrom = extractionstep.getGAInParseTree();
                    } else { // processing the left sister
                        GAtoExtractFrom = extractionstep.getGAInParseTree()
                                .leftSister();
                    }
                    // Then, extract and add to the right set (see above)
                    nextStep = new ExtractionStep(
                            (SimpleRRGParseItem) combsisantecedentItem,
                            GAtoExtractFrom, oneTree);
                    if (i == 0) {
                        tmpResult = extract(nextStep);
                    } else {
                        parsesInThisCombSisStep.addAll(extract(nextStep));
                    }
                }
            }
        }
        return parsesInThisCombSisStep;
    }

    private Set<RRGParseTree> extractMoveUp(
            Set<List<ParseItem>> moveupAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisMoveUpStep = new HashSet<RRGParseTree>();

        for (List<ParseItem> moveupAntecedentItemSingletonList : moveupAntecedents) {
            SimpleRRGParseItem moveupAntecedentItem = (SimpleRRGParseItem) moveupAntecedentItemSingletonList
                    .get(0);
            GornAddress newMoveUpGA = extractionstep.getGAInParseTree()
                    .ithDaughter(moveupAntecedentItem.getNode().getGornaddress()
                            .isIthDaughter());
            ExtractionStep nextStep = new ExtractionStep(
                    moveupAntecedentItem, newMoveUpGA,
                    extractionstep.getCurrentParseTree());
            parsesInThisMoveUpStep.addAll(extract(nextStep));
        }
        return parsesInThisMoveUpStep;
    }

    private Set<RRGParseTree> extractNLS(Set<List<ParseItem>> nlsAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisNLSStep = new HashSet<RRGParseTree>();
        for (List<ParseItem> antecedentItemSingletonList : nlsAntecedents) {
            ExtractionStep nextStep = new ExtractionStep(
                    (SimpleRRGParseItem) antecedentItemSingletonList.get(0),
                    extractionstep.getGAInParseTree(),
                    extractionstep.getCurrentParseTree());
            parsesInThisNLSStep.addAll(extract(nextStep));
        }

        return parsesInThisNLSStep;

    }
}
