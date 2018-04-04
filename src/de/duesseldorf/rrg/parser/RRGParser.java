package de.duesseldorf.rrg.parser;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import de.duesseldorf.frames.Situation;
import de.duesseldorf.rrg.RRG;
import de.duesseldorf.rrg.RRGNode;
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

    public boolean parseSentence(List<String> toksentence) {
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
        System.out.println("Agenda size: " + agenda.size());
        ParseForestExtractor extractor = new ParseForestExtractor(chart,
                toksentence);
        return false;
    }

    /**
     * 
     * @param consequent
     * @param antecedents
     */
    private void addToChartAndAgenda(SimpleRRGParseItem consequent,
            SimpleRRGParseItem... antecedents) {
        if (chart.addItem(consequent, antecedents)) {
            agenda.add(consequent);
        }
        // Debug
        // System.out.println("cons: " + consequent + "\n\n");
    }

    private void sisteradjoin(SimpleRRGParseItem currentItem) {
        // the currentItem is either
        // - a root item, for which we need to find a target tree
        // - or a node such that we want to find a root item (this might be
        // expensive. Can we leave it out?
        boolean sisadjroot = requirementFinder.sisadjRoot(currentItem);
        // System.out.print(root);
        // System.out.println(" " + currentItem.toString());
        if (sisadjroot) {
            // left-adjoin
            Set<SimpleRRGParseItem> leftAdjoinAntecedents = requirementFinder
                    .leftAdjoinAntecedents(currentItem, chart);
            for (SimpleRRGParseItem simpleRRGParseItem : leftAdjoinAntecedents) {
                // System.out.println("THERE: " + simpleRRGParseItem);
                SimpleRRGParseItem consequent = deducer
                        .applyLeftAdjoin(simpleRRGParseItem, currentItem);
                addToChartAndAgenda(consequent, currentItem,
                        simpleRRGParseItem);
            }

            // right-adjoin
            Set<SimpleRRGParseItem> rightAdjoinAntecedents = requirementFinder
                    .rightAdjoinAntecedents(currentItem, chart);
        }

        // Note April 3:
        // next do the adjunctions with other antecedents,
        // refactor parseItems, ws, think about recognizer -> parser

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
                        addToChartAndAgenda(consequent, currentItem);
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
            addToChartAndAgenda(newItem, currentItem);
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
                addToChartAndAgenda(rightSisTopItem, currentItem,
                        simpleRRGParseItem);
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

            addToChartAndAgenda(newItem, currentItem);
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
                        addToChartAndAgenda(scannedItem);
                    }
                }
            }
        }
    }
}
