package de.duesseldorf.rrg.parser;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import de.duesseldorf.frames.Situation;
import de.duesseldorf.rrg.RRG;
import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGParseTree;
import de.duesseldorf.rrg.RRGTree;
import de.duesseldorf.rrg.parser.SimpleRRGParseItem.NodePos;

/**
 * TODO
 * addToChartAndAgenda method also checks for goal items, or check for goal
 * items in the chart class
 * 
 * @author david
 *
 */
/**
 * @author david
 *
 */
public class RRGParser {

    private Situation situation;
    private SimpleRRGParseChart chart;
    private ConcurrentSkipListSet<SimpleRRGParseItem> agenda;
    private RequirementFinder requirementFinder;
    private Deducer deducer;

    public RRGParser(Situation sit) {
        this.situation = sit;
        this.requirementFinder = new RequirementFinder();
        this.deducer = new Deducer();
    }

    public Set<RRGParseTree> parseSentence(List<String> toksentence) {
        this.agenda = new ConcurrentSkipListSet<SimpleRRGParseItem>();
        this.chart = new SimpleRRGParseChart(toksentence.size());
        scan(toksentence);
        // Debug:
        this.requirementFinder = new RequirementFinder();

        System.out.println("Done scanning. ");
        // System.out.println(chart.toString());
        while (!agenda.isEmpty()) {
            // TODO: optimize this based on the ws flag?
            SimpleRRGParseItem currentItem = agenda.pollFirst();
            // System.out.println("cI: " + currentItem);
            noleftsister(currentItem);
            moveup(currentItem);
            combinesisters(currentItem);
            substitute(currentItem);
            sisteradjoin(currentItem);
            // System.out.println("Agenda size: " + agenda.size());
        }
        System.out.println("Done parsing. \n" + chart.toString());
        ParseForestExtractor extractor = new ParseForestExtractor(chart,
                toksentence);
        Set<RRGParseTree> result = extractor.extractParseTrees();
        System.out.println("result: ");
        for (RRGParseTree rrgParseTree : result) {
            System.out.println(rrgParseTree);
        }
        return result;
    }

    /**
     * 
     * @param consequent
     * @param antecedents
     */
    private void addToChartAndAgenda(SimpleRRGParseItem consequent,
            Operation operation, SimpleRRGParseItem... antecedents) {
        if (chart.addItem(consequent, operation, antecedents)) {
            agenda.add(consequent);
        }
        // Debug
        // System.out.println("cons: " + consequent);
    }

    /**
     * the currentItem is either
     * - a root item, for which we need to find a target tree
     * - or a node such that we want to find a root item (this might be
     * expensive. But we probably cant survive without it)
     */
    private void sisteradjoin(SimpleRRGParseItem currentItem) {
        boolean sisadjroot = requirementFinder.isSisadjRoot(currentItem);
        // System.out.print(root);
        // System.out.println(" " + currentItem.toString());
        if (sisadjroot) {
            // left-adjoin
            Set<SimpleRRGParseItem> leftAdjoinAntecedents = requirementFinder
                    .findLeftAdjoinTargets(currentItem, chart);
            for (SimpleRRGParseItem simpleRRGParseItem : leftAdjoinAntecedents) {
                // System.out.println("THERE: " + simpleRRGParseItem);
                SimpleRRGParseItem consequent = deducer
                        .applyLeftAdjoin(simpleRRGParseItem, currentItem);
                addToChartAndAgenda(consequent, Operation.LEFTADJOIN,
                        currentItem, simpleRRGParseItem);
            }

            // right-adjoin
            Set<SimpleRRGParseItem> rightAdjoinAntecedents = requirementFinder
                    .findRightAdjoinTargets(currentItem, chart);
            for (SimpleRRGParseItem target : rightAdjoinAntecedents) {
                SimpleRRGParseItem consequent = deducer.applyRightAdjoin(target,
                        currentItem);
                addToChartAndAgenda(consequent, Operation.RIGHTADJOIN,
                        currentItem, target);
                System.out.println("blabalablkdkdm");
            }
            // System.out.println("rightadjoin: " + currentItem);
            // System.out.println(rightAdjoinAntecedents);
        } else {

            // Note 09.04.2018: The findSisAdjRoots method is overgenerating

            Map<String, Set<SimpleRRGParseItem>> sisadjroots = requirementFinder
                    .findSisAdjRoots(currentItem, chart);
            // System.out.println("sisadj with " + currentItem);
            // System.out.println("sisl" + sisadjroots.get("l"));
            // System.out.println("sisr" + sisadjroots.get("r"));

            for (SimpleRRGParseItem auxRootItem : sisadjroots.get("l")) {
                SimpleRRGParseItem consequent = deducer
                        .applyLeftAdjoin(currentItem, auxRootItem);
                addToChartAndAgenda(consequent, Operation.LEFTADJOIN,
                        auxRootItem, currentItem);
            }
            if (!sisadjroots.get("r").isEmpty()) {
                // System.out.println("target: " + currentItem);
                // System.out.println("roots: " + sisadjroots.get("r"));
                // System.out.println("cons: " + deducer.applyRightAdjoin(
                // currentItem, sisadjroots.get("r").iterator().next()));
                // System.out.println();
            }
            for (SimpleRRGParseItem auxRootItem : sisadjroots.get("r")) {
                SimpleRRGParseItem consequent = deducer
                        .applyRightAdjoin(currentItem, auxRootItem);
                if (!auxRootItem.equals(consequent)) {
                    addToChartAndAgenda(consequent, Operation.RIGHTADJOIN,
                            auxRootItem, currentItem);
                    // System.out.println(auxRootItem + " and " + currentItem
                    // + "\n\t lead to " + consequent);
                } else {
                    System.out
                            .println("yay" + auxRootItem + " vs " + consequent);
                }
                // System.out.println("RA " + currentItem + auxRootItem);
                // System.out.println(sisadjroots.get("r"));
            }
        }

    }

    private void substitute(SimpleRRGParseItem currentItem) {
        if (requirementFinder.substituteReq(currentItem)) {
            for (RRGTree tree : ((RRG) situation.getGrammar()).getTrees()) {
                Set<RRGNode> substNodes = tree.getSubstNodes()
                        .get(currentItem.getNode().getCategory());
                if (substNodes != null) {
                    for (RRGNode substNode : substNodes) {
                        // System.out.println("got to for: " + substNode);
                        SimpleRRGParseItem consequent = new SimpleRRGParseItem(
                                currentItem, tree, substNode,
                                SimpleRRGParseItem.NodePos.BOT, -1, -1, null,
                                false);
                        // System.out.println("cons: " + consequent);
                        addToChartAndAgenda(consequent, Operation.SUBSTITUTE,
                                currentItem);
                    }
                }
            }
        }
    }

    private void moveup(SimpleRRGParseItem currentItem) {
        // System.out.println("currentnode: " + currentItem.getNode());
        boolean moveupreq = requirementFinder.moveupReq(currentItem);
        if (moveupreq) {
            SimpleRRGParseItem newItem = deducer.applyMoveUp(currentItem);
            addToChartAndAgenda(newItem, Operation.MOVEUP, currentItem);
        }
    }

    private void combinesisters(SimpleRRGParseItem currentItem) {

        Set<SimpleRRGParseItem> combinesisCandidates = requirementFinder
                .combinesisReq(currentItem, chart);
        if (!combinesisCandidates.isEmpty()) {
            // System.out.println("currentItem: " + currentItem);
            for (SimpleRRGParseItem simpleRRGParseItem : combinesisCandidates) {
                // System.out.println(
                // "mate with: " + simpleRRGParseItem + "results in");
                SimpleRRGParseItem rightSisTopItem = deducer
                        .applyCombineSisters(currentItem, simpleRRGParseItem);
                // System.out.println(rightSisTopItem);
                addToChartAndAgenda(rightSisTopItem, Operation.COMBINESIS,
                        currentItem, simpleRRGParseItem);
            }
        }
    }

    /**
     * note that NLS is the only deduction rule that can be done for items in
     * BOT position on a leftmost daughter node
     * 
     * @param currentItem
     */
    private void noleftsister(SimpleRRGParseItem currentItem) {

        boolean nlsrequirements = requirementFinder.nlsReq(currentItem);
        if (nlsrequirements) {

            SimpleRRGParseItem newItem = deducer.applyNoLeftSister(currentItem);

            addToChartAndAgenda(newItem, Operation.NLS, currentItem);
        }
    }

    /**
     * apply the scanning deduction rule
     */
    private void scan(List<String> sentence) {
        // Look at all trees
        for (RRGTree tree : ((RRG) situation.getGrammar()).getTrees()) {
            // Look at all words
            for (int start = 0; start < sentence.size(); start++) {
                String word = sentence.get(start);

                // See if the word is a lex Node of the tree
                Set<RRGNode> candidates = tree.getLexNodes().get(word);
                if (candidates != null) {
                    for (RRGNode lexLeaf : candidates) {
                        // If so, create a new item and add it to the chart and
                        // agenda
                        SimpleRRGParseItem scannedItem = new SimpleRRGParseItem(
                                tree, lexLeaf, NodePos.BOT, start, start + 1,
                                new LinkedList<Gap>(), false);
                        addToChartAndAgenda(scannedItem, Operation.SCAN);
                    }
                }
            }
        }
    }
}
