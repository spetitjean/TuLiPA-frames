package de.duesseldorf.rrg.parser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.duesseldorf.rrg.RRGParseTree;
import de.duesseldorf.util.GornAddress;
import de.tuebingen.util.TextUtilities;

public class NewParseForestExtractor {

    private class ExtractionStepInfo {
        private GornAddress GAInParseTree;
        private SimpleRRGParseItem currentItem;
        private RRGParseTree currentParseTree;

        public ExtractionStepInfo(SimpleRRGParseItem currentItem,
                GornAddress GAInParseTree, RRGParseTree currentParseTree) {
            this.GAInParseTree = GAInParseTree;
            this.currentItem = currentItem;
            this.currentParseTree = currentParseTree;
        }

        public GornAddress getGAInParseTree() {
            return GAInParseTree;
        }

        public SimpleRRGParseItem getCurrentItem() {
            return currentItem;
        }

        public RRGParseTree getCurrentParseTree() {
            return currentParseTree;
        }

    }

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

        Set<ParseItem> goals = parseChart.retrieveGoalItems();
        for (ParseItem goal : goals) {
            ExtractionStepInfo initExtrStep = initialExtractionStep(
                    (SimpleRRGParseItem) goal);
            RRGParseTree resultingTree = extract(initExtrStep);
            addToResultingParses(resultingTree);
        }
        return resultingParses;
    }

    /**
     * this method takes a
     * 
     * @param goal
     *            item and createsthe initial parse step from that item
     */
    private ExtractionStepInfo initialExtractionStep(SimpleRRGParseItem goal) {
        ExtractionStepInfo result = new ExtractionStepInfo(goal,
                new GornAddress(), new RRGParseTree(goal.getTree()));
        return result;
    }

    private void addToResultingParses(RRGParseTree tree) {
        String newId = TextUtilities.appendList(toksentence, "_")
                + resultingParses.size();
        tree.setId(newId);
        resultingParses.add(tree);
    }

    private RRGParseTree extract(ExtractionStepInfo extractionstep) {
        Backpointer backPointers = parseChart
                .getBackPointers(extractionstep.getCurrentItem());
        // if (backPointers.size() > 1) {
        // System.out.println("Extraction does not deal with ambiguity");
        // } else if (backPointers.size() == 1) {
        // distinguish different operatons here

        // NLS
        Set<List<ParseItem>> nlsAntecedents = backPointers
                .getAntecedents(Operation.NLS);
        for (List<ParseItem> antecedentItemSingletonList : nlsAntecedents) {
            ExtractionStepInfo nextStep = new ExtractionStepInfo(
                    (SimpleRRGParseItem) antecedentItemSingletonList.get(0),
                    extractionstep.getGAInParseTree(),
                    extractionstep.getCurrentParseTree());
            return extract(nextStep);
        }

        // Move-Up
        Set<List<ParseItem>> moveupAntecedents = backPointers
                .getAntecedents(Operation.MOVEUP);
        for (List<ParseItem> moveupAntecedentItemSingletonList : moveupAntecedents) {
            SimpleRRGParseItem moveupAntecedentItem = (SimpleRRGParseItem) moveupAntecedentItemSingletonList
                    .get(0);
            GornAddress newMoveUpGA = extractionstep.getGAInParseTree()
                    .ithDaughter(moveupAntecedentItem.getNode().getGornaddress()
                            .isIthDaughter());
            ExtractionStepInfo nextStep = new ExtractionStepInfo(
                    moveupAntecedentItem, newMoveUpGA,
                    extractionstep.getCurrentParseTree());
            return extract(nextStep);
        }

        // Combine-Sisters
        Set<List<ParseItem>> combsisAntecedents = backPointers
                .getAntecedents(Operation.COMBINESIS);
        for (List<ParseItem> combsisantecedentItems : combsisAntecedents) {
            RRGParseTree tmpResult = extractionstep.getCurrentParseTree();
            for (ParseItem combsisantecedentItem : combsisantecedentItems) {
                // processing the right sister
                if (((SimpleRRGParseItem) combsisantecedentItem).getNode()
                        .getGornaddress().equals(extractionstep.getCurrentItem()
                                .getNode().getGornaddress())) {
                    ExtractionStepInfo nextStep = new ExtractionStepInfo(
                            (SimpleRRGParseItem) combsisantecedentItem,
                            extractionstep.getGAInParseTree(), tmpResult);
                    tmpResult = extract(nextStep);
                } else { // processing the left sister
                    ExtractionStepInfo nextStep = new ExtractionStepInfo(
                            (SimpleRRGParseItem) combsisantecedentItem,
                            extractionstep.getGAInParseTree().leftSister(),
                            tmpResult);
                    tmpResult = extract(nextStep);
                }
            }
            return tmpResult;
        }

        // Substitution
        Set<List<ParseItem>> substAntecedents = backPointers
                .getAntecedents(Operation.SUBSTITUTE);

        for (List<ParseItem> substantecedentItemSingletonList : substAntecedents) {
            SimpleRRGParseItem substAntecedentItem = (SimpleRRGParseItem) substantecedentItemSingletonList
                    .get(0);
            extractionstep.currentParseTree.replaceNodeWithSubTree(
                    extractionstep.getGAInParseTree(),
                    substAntecedentItem.getTree());
            ExtractionStepInfo nextStep = new ExtractionStepInfo(
                    substAntecedentItem, extractionstep.getGAInParseTree(),
                    extractionstep.getCurrentParseTree());
            return extract(nextStep);
        }
        // }

        // if no other rule applied (i.e. if we dealt with a scanned item):
        return extractionstep.getCurrentParseTree();

        // Idee für Ambiguität:
        // Gehe depth-first durch den gesamten Baum.
        // Wenn alles unambig ist, gehe wieder nach oben zurück und füge dann
        // den Baum hinzu.
        // Wenn etwas ambig ist, füge den aktuellen Parsebaum hinzu, bevor die
        // zweite, dritte... Möglichkeit durchextrahiert wird.
    }
}
