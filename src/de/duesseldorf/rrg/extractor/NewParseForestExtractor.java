package de.duesseldorf.rrg.extractor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.duesseldorf.rrg.RRGNode.RRGNodeType;
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
        ExtractionStep result = new ExtractionStep(goal, new GornAddress(),
                new RRGParseTree(goal.getTree()));
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

        // System.out.println(extractionstep);
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
            // do the substitution and extract wrapping tree below d-daughter
        }
        return parsesInThisPWStep;
    }

    private Set<RRGParseTree> extractCompleteWrapping(
            Set<List<ParseItem>> completeWrappingAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisCWStep = new HashSet<RRGParseTree>();
        for (List<ParseItem> CWantecedentItems : completeWrappingAntecedents) {
            System.out.println(
                    "do complete Wrapping with\n" + CWantecedentItems.toString()
                            + "\n" + extractionstep.getCurrentItem());

            for (ParseItem antecedentItem : CWantecedentItems) {

                boolean isDdaughter = ((SimpleRRGParseItem) antecedentItem)
                        .getwsflag();
                // extract the d-daughter in the predict-wrapping step
                if (!isDdaughter) {
                    // insert the wrapped tree into the parse tree
                    System.out.println("before wrapping: "
                            + extractionstep.getCurrentParseTree());

                    RRGParseTree nextStepParseTree = extractionstep
                            .getCurrentParseTree().insertWrappedTree(
                                    ((SimpleRRGParseItem) antecedentItem)
                                            .getTree(),
                                    extractionstep.getGAInParseTree());
                    // adjust Gorn Addresses here. Actually, do that in
                    // RRGParseTree?
                    System.out.println("after wrapping: " + nextStepParseTree);
                    // parsesInThisCWStep.addAll(extract(nextStep));
                }
            }

        }
        return parsesInThisCWStep;
    }

    private Set<RRGParseTree> extractRightAdjoin(
            Set<List<ParseItem>> rightAdjAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisRightAdjStep = new HashSet<RRGParseTree>();
        for (List<ParseItem> rightAdjAntecedentItems : rightAdjAntecedents) {
            Set<RRGParseTree> tmpResult = new HashSet<RRGParseTree>();
            // tmpResult.add(extractionstep.getCurrentParseTree());

            // first, do the actual sister adjunction
            RRGTree adjoiningTree;
            if ((((SimpleRRGParseItem) rightAdjAntecedentItems.get(0)).getNode()
                    .getType().equals(RRGNodeType.STAR))) {
                adjoiningTree = ((SimpleRRGParseItem) rightAdjAntecedentItems
                        .get(0)).getTree();
            } else {
                adjoiningTree = ((SimpleRRGParseItem) rightAdjAntecedentItems
                        .get(1)).getTree();
            }
            System.out.println(
                    "before sisadj: " + extractionstep.getCurrentParseTree());
            System.out.println(
                    "sisadj at " + extractionstep.getGAInParseTree().mother());
            System.out.println("as daughter: "
                    + (extractionstep.getGAInParseTree().isIthDaughter() + 1));
            RRGParseTree nextStepParseTree = extractionstep
                    .getCurrentParseTree().sisterAdjoin(adjoiningTree,
                            extractionstep.getGAInParseTree().mother(),
                            extractionstep.getGAInParseTree().isIthDaughter()
                                    + 1);
            System.out.println("after: " + nextStepParseTree);
            tmpResult.add(nextStepParseTree);
            for (RRGParseTree parseTree : tmpResult) {
                for (int i = 0; i < rightAdjAntecedentItems.size(); i++) {
                    SimpleRRGParseItem rightAdjAntecedentItem = (SimpleRRGParseItem) rightAdjAntecedentItems
                            .get(i);
                    ExtractionStep nextStep;
                    GornAddress GAtoExtractFrom;
                    boolean antItemIsAdjoiningTree = rightAdjAntecedentItem
                            .getNode().getType().equals(RRGNodeType.STAR);
                    if (antItemIsAdjoiningTree) { // extraction of the adjoining
                                                  // tree
                        GAtoExtractFrom = extractionstep.getGAInParseTree()
                                .mother();
                        // how do we find out here to which daughter we have to
                        // move?
                    } else { // continue extraction of the target tree
                        GAtoExtractFrom = extractionstep.getGAInParseTree();
                    }
                    System.out.println(
                            "the GAs have changed. Consider GA shifting!");
                    nextStep = new ExtractionStep(rightAdjAntecedentItem,
                            GAtoExtractFrom, nextStepParseTree);
                    System.out.println("next round: " + nextStep);
                    if (i == 0) {
                        tmpResult = extract(nextStep);
                    } else {
                        parsesInThisRightAdjStep.addAll(extract(nextStep));
                    }

                }
            }
        }

        return parsesInThisRightAdjStep;
    }

    private Set<RRGParseTree> extractLeftAdjoin(
            Set<List<ParseItem>> leftAdjAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisLeftAdjStep = new HashSet<RRGParseTree>();
        for (List<ParseItem> leftAdjAntecedentItems : leftAdjAntecedents) {
            System.out.println("leftsisadj: " + leftAdjAntecedentItems + "\n"
                    + extractionstep);
            Set<RRGParseTree> tmpResult = new HashSet<RRGParseTree>();
            // tmpResult.add(extractionstep.getCurrentParseTree());

            // first, do the actual sister adjunction
            RRGTree adjoiningTree;
            if ((((SimpleRRGParseItem) leftAdjAntecedentItems.get(0)).getNode()
                    .getType().equals(RRGNodeType.STAR))) {
                adjoiningTree = ((SimpleRRGParseItem) leftAdjAntecedentItems
                        .get(0)).getTree();
            } else {
                adjoiningTree = ((SimpleRRGParseItem) leftAdjAntecedentItems
                        .get(1)).getTree();
            }
            System.out.println(
                    "before sisadj: " + extractionstep.getCurrentParseTree());
            RRGParseTree nextStepParseTree = extractionstep
                    .getCurrentParseTree().sisterAdjoin(adjoiningTree,
                            extractionstep.getGAInParseTree().mother(), 0);
            System.out.println("after: " + nextStepParseTree);
            tmpResult.add(nextStepParseTree);

            for (RRGParseTree parseTree : tmpResult) {
                for (int i = 0; i < leftAdjAntecedentItems.size(); i++) {
                    SimpleRRGParseItem leftAdjAntecedentItem = (SimpleRRGParseItem) leftAdjAntecedentItems
                            .get(i);
                    ExtractionStep nextStep;
                    GornAddress GAtoExtractFrom;
                    boolean antItemIsAdjoiningTree = leftAdjAntecedentItem
                            .getNode().getType().equals(RRGNodeType.STAR);
                    if (antItemIsAdjoiningTree) { // extraction of the adjoining
                                                  // tree
                        GAtoExtractFrom = extractionstep.getGAInParseTree()
                                .mother();
                        // move creation of the new tree before the if/else?
                        // nextStepParseTree =
                        // extractionstep.getCurrentParseTree()
                        // .sisterAdjoin(leftAdjAntecedentItem.getTree(),
                        // GAtoExtractFrom, 0);
                    } else { // continue extraction of the target tree
                        GAtoExtractFrom = extractionstep.getGAInParseTree()
                                .rightSister();
                        // nextStepParseTree = parseTree;
                    }
                    nextStep = new ExtractionStep(leftAdjAntecedentItem,
                            GAtoExtractFrom, nextStepParseTree);
                    System.out.println("next round: " + nextStep);
                    // TODO problem: When extracting sisadj or wrapping as well,
                    // probably, the GA to extract from has changing conditions
                    // because material can be added in places affecting the GA.
                    if (i == 0) {
                        tmpResult = extract(nextStep);
                    } else {
                        parsesInThisLeftAdjStep.addAll(extract(nextStep));
                    }

                }
            }
        }
        return parsesInThisLeftAdjStep;
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

    private Set<RRGParseTree> extractSubst(
            Set<List<ParseItem>> substAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisSUBSTStep = new HashSet<RRGParseTree>();
        for (List<ParseItem> substantecedentItemSingletonList : substAntecedents) {
            System.out.println("Subst: " + substantecedentItemSingletonList
                    + "\n" + extractionstep);

            SimpleRRGParseItem substAntecedentItem = (SimpleRRGParseItem) substantecedentItemSingletonList
                    .get(0);
            GornAddress GAtoReplaceAt = extractionstep.getGAInParseTree();
            RRGTree substTree = substAntecedentItem.getTree();
            System.out.println("try to subst this tree: " + substTree);
            System.out.println(
                    "into that tree: " + extractionstep.getCurrentParseTree());
            System.out.println("at GA " + GAtoReplaceAt);
            RRGParseTree nextStepParseTree = extractionstep
                    .getCurrentParseTree().substitute(substTree, GAtoReplaceAt);
            System.out.println("result: " + nextStepParseTree);
            ExtractionStep nextStep = new ExtractionStep(substAntecedentItem,
                    extractionstep.getGAInParseTree(), nextStepParseTree);
            parsesInThisSUBSTStep.addAll(extract(nextStep));
        }
        return parsesInThisSUBSTStep;
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
            ExtractionStep nextStep = new ExtractionStep(moveupAntecedentItem,
                    newMoveUpGA, extractionstep.getCurrentParseTree());
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
