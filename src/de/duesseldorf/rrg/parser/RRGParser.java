package de.duesseldorf.rrg.parser;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

import de.duesseldorf.frames.Situation;
import de.duesseldorf.rrg.*;
import de.duesseldorf.rrg.extractor.ParseForestExtractor;
import de.duesseldorf.rrg.parser.RRGParseItem.NodePos;

/**
 * File RRGParser.java
 * <p>
 * Authors:
 * David Arps <david.arps@hhu.de>
 * <p>
 * Copyright
 * David Arps, 2018
 * <p>
 * <p>
 * This file is part of the TuLiPA-frames system
 * https://github.com/spetitjean/TuLiPA-frames
 * <p>
 * <p>
 * TuLiPA is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * TuLiPA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
public class RRGParser {

    private Situation situation;
    private RRGParseChart chart;
    private ConcurrentSkipListSet<RRGParseItem> agenda;
    private RequirementFinder requirementFinder;
    private Deducer deducer;

    private boolean verbosePrintsToStdOut = true;

    public RRGParser(Situation sit) {
        this.situation = sit;
        this.requirementFinder = new RequirementFinder();
        this.deducer = new Deducer();
    }

    public Set<RRGParseTree> parseSentence(List<String> toksentence) {
        this.agenda = new ConcurrentSkipListSet<RRGParseItem>();
        this.chart = new RRGParseChart(toksentence.size());
        //Axioms through scanning:
        scan(toksentence);
        // Debug:
        this.requirementFinder = new RequirementFinder();

        if (verbosePrintsToStdOut) {
            System.out.println("Done scanning. ");
        }
        // The real recognition
        int i = 0;
        while (!agenda.isEmpty()) {
            // System.out.println("step: " + i);
            i++;
            RRGParseItem currentItem = agenda.pollFirst();
            // System.out.println("current item: " + currentItem);
            if (currentItem.getNodePos().equals(RRGParseItem.NodePos.BOT)) {
                noleftsister(currentItem);
            }
            else {
                moveup(currentItem);
                substitute(currentItem);
                sisteradjoin(currentItem);
            }
            predictwrapping(currentItem);
            combinesisters(currentItem);
            completewrapping(currentItem);
            // System.out.println("Agenda size: " + agenda.size());
        }
        if (verbosePrintsToStdOut) {
            System.out.println("Done parsing. \n" + chart.toString());
        }
        // extract parse results from chart
        ParseForestExtractor extractor = new ParseForestExtractor(chart, toksentence);
        Set<RRGParseTree> result = extractor.extractParseTrees();
        return result;
    }

    /**
     * @param consequent
     * @param antecedents always give the antecedent items in left-to-right order
     */
    private void addToChartAndAgenda(RRGParseItem consequent, Operation operation,
                                     RRGParseItem... antecedents) {
        if (chart.addItem(consequent, operation, antecedents)) {
            agenda.add(consequent);
        }
        // Debug
        System.out.println("next to agenda: " + consequent + "\n\t " +
        operation + "\n\t antecedents: " + Arrays.asList(antecedents));
    }

    private void completewrapping(RRGParseItem currentItem) {
        // System.out.println("complW with " + currentItem);
        boolean rootItem = requirementFinder.isCompleteWrappingRootItem(currentItem);
        boolean fillerItem = requirementFinder.isCompleteWrappingFillerItem(currentItem);
        if (rootItem) {
            for (Gap gap : currentItem.getGaps()) {
                Set<RRGParseItem> completeWrappingFillerAntecedents = requirementFinder
                        .findCompleteWrappingFillers(currentItem, gap, chart);
                for (RRGParseItem fillerddaughterItem : completeWrappingFillerAntecedents) {
                    RRGParseItem consequent = deducer
                            .applyCompleteWrapping(currentItem, fillerddaughterItem, gap);
                    // System.out.println("did a Compl Wrapping with: "
                    // + consequent + currentItem);
                    addToChartAndAgenda(consequent, Operation.COMPLETEWRAPPING, currentItem,
                                        fillerddaughterItem);
                }
            }

        }
        if (fillerItem) {
            // System.out.println("TODO in Parser CW 2 " + currentItem);
            // Set<RRGParseItem> completeWrappingRootAntecedents =
            // requirementFinder
            // .findCompleteWrappingRoots(currentItem, chart);
            // System.out.println("untested completeWrapping territory! D");
            // System.out.println("root: " + completeWrappingRootAntecedents);
            // System.out.println("ddaughter: " + currentItem);
        }
    }

    private void predictwrapping(RRGParseItem currentItem) {
        if (requirementFinder.predWrappingReqs(currentItem)) {
            // look at the whole grammar and find fitting substitution nodes
            String cat = currentItem.getNode().getCategory();
            // System.out.println("got to predict: " + currentItem);
            for (RRGTree tree : ((RRG) situation.getGrammar()).getTrees()) {
                Set<RRGNode> substNodes = tree.getSubstNodes().get(cat);
                if (substNodes != null) {
                    HashSet<Gap> gaps = new HashSet<Gap>();
                    gaps.add(new Gap(currentItem.startPos(), currentItem.getEnd(), cat));
                    for (RRGNode substNode : substNodes) {
                        // System.out.println("got to for: " + substNode);
                        RRGParseItem cons = new RRGParseItem.Builder().tree(tree)
                                                                      .node(substNode)
                                                                      .nodepos(NodePos.BOT)
                                                                      .start(currentItem
                                                                                     .startPos())
                                                                      .end(currentItem.getEnd())
                                                                      .gaps(gaps).ws(false)
                                                                      .build();
                        // System.out.println("cons: " + consequent);
                        addToChartAndAgenda(cons, Operation.PREDICTWRAPPING, currentItem);
                    }
                }
            }
        }
    }

    /**
     * the currentItem is either
     * - a root item, for which we need to find a target tree
     * - or a node such that we want to find a root item (this might be
     * expensive. But we probably cant survive without it)
     */
    private void sisteradjoin(RRGParseItem currentItem) {
        boolean sisadjroot = requirementFinder.isSisadjRoot(currentItem);
        boolean sisAdjTarget = requirementFinder.isSisadjTarget(currentItem);
        // System.out.print(root);
        // System.out.println(" " + currentItem.toString());
        if (sisadjroot) {
            // left-adjoin
            Set<RRGParseItem> leftAdjoinTargets = requirementFinder
                    .findLeftAdjoinTargets(currentItem, chart);
            for (RRGParseItem target : leftAdjoinTargets) {
                // System.out.println("THERE: " + simpleRRGParseItem);
                RRGParseItem consequent = deducer.applyLeftAdjoin(target, currentItem);
                addToChartAndAgenda(consequent, Operation.LEFTADJOIN, currentItem, target);
            }

            // right-adjoin
            Set<RRGParseItem> rightAdjoinAntecedents = requirementFinder
                    .findRightAdjoinTargets(currentItem, chart);
            for (RRGParseItem target : rightAdjoinAntecedents) {
                RRGParseItem consequent = deducer.applyRightAdjoin(target, currentItem);
                addToChartAndAgenda(consequent, Operation.RIGHTADJOIN, target, currentItem);
                // System.out.println(
                // "you triggered some special case for sister adjunction which
                // I haven't tested yet. D");
            }
            // System.out.println("rightadjoin: " + currentItem);
            // System.out.println(rightAdjoinAntecedents);
        }
        else if (sisAdjTarget) {
            Map<String, Set<RRGParseItem>> sisadjroots = requirementFinder
                    .findSisAdjRoots(currentItem, chart);
            // if (!sisadjroots.get("l").isEmpty()) {
            // System.out.println("sisadj with " + currentItem);
            // System.out.println("sisl" + sisadjroots.get("l"));
            // System.out.println("sisr" + sisadjroots.get("r"));
            // }
            // left-adjoin
            for (RRGParseItem auxRootItem : sisadjroots.get("l")) {
                RRGParseItem consequent = deducer.applyLeftAdjoin(currentItem, auxRootItem);
                addToChartAndAgenda(consequent, Operation.LEFTADJOIN, auxRootItem, currentItem);
            }
            // right-adjoin
            for (RRGParseItem auxRootItem : sisadjroots.get("r")) {
                RRGParseItem consequent = deducer.applyRightAdjoin(currentItem, auxRootItem);
                addToChartAndAgenda(consequent, Operation.RIGHTADJOIN, currentItem,
                                    auxRootItem);
                // System.out.println(auxRootItem + " and " + currentItem
                // + "\n\t lead to " + consequent);

                // System.out.println("RA " + currentItem + auxRootItem);
                // System.out.println(sisadjroots.get("r"));
            }
        }
    }

    private void substitute(RRGParseItem currentItem) {
        if (requirementFinder.substituteReq(currentItem)) {
            for (RRGTree tree : ((RRG) situation.getGrammar()).getTrees()) {
                Set<RRGNode> substNodes = tree.getSubstNodes()
                                              .get(currentItem.getNode().getCategory());
                if (substNodes != null) {
                    for (RRGNode substNode : substNodes) {
                        // System.out.println("got to for: " + substNode);
                        RRGParseItem cons = new RRGParseItem.Builder().tree(tree)
                                                                      .node(substNode)
                                                                      .nodepos(NodePos.BOT)
                                                                      .start(currentItem
                                                                                     .startPos())
                                                                      .end(currentItem.getEnd())
                                                                      .gaps(currentItem
                                                                                    .getGaps())
                                                                      .ws(false).build();
                        // System.out.println("cons: " + consequent);
                        addToChartAndAgenda(cons, Operation.SUBSTITUTE, currentItem);
                    }
                }
            }
        }
    }

    private void moveup(RRGParseItem currentItem) {
        // System.out.println("currentnode: " + currentItem.getNode());
        boolean moveupreq = requirementFinder.moveupReq(currentItem);
        if (moveupreq) {
            RRGParseItem newItem = deducer.applyMoveUp(currentItem);
            addToChartAndAgenda(newItem, Operation.MOVEUP, currentItem);
        }
    }

    private void combinesisters(RRGParseItem currentItem) {

        // case 1: currentItem is the left node of the combination
        Set<RRGParseItem> rightSisterCandidates = requirementFinder
                .findCombineSisRightSisters(currentItem, chart);
        // System.out.println("currentItem: " + leftSisterAntecedentItem);
        for (RRGParseItem rightSisterAntecedentItem : rightSisterCandidates) {
            // System.out.println(
            // "mate with: " + rightSisterAntecedentItem + "results in");
            RRGParseItem rightSisTopItem = deducer
                    .applyCombineSisters(currentItem, rightSisterAntecedentItem);
            // System.out.println(rightSisTopItem);
            addToChartAndAgenda(rightSisTopItem, Operation.COMBINESIS, currentItem,
                                rightSisterAntecedentItem);
        }
        // case 2: currentItem is the right node of the combination
        Set<RRGParseItem> leftSisterCandidates = requirementFinder
                .findCombineSisLeftSisters(currentItem, chart);
        for (RRGParseItem leftSisterAntecedentItem : leftSisterCandidates) {
            RRGParseItem rightSisTopItem = deducer
                    .applyCombineSisters(leftSisterAntecedentItem, currentItem);
            addToChartAndAgenda(rightSisTopItem, Operation.COMBINESIS, leftSisterAntecedentItem,
                                currentItem);

        }
    }

    /**
     * note that NLS is the only deduction rule that can be done for items in
     * BOT position on a leftmost daughter node
     *
     * @param currentItem
     */
    private void noleftsister(RRGParseItem currentItem) {

        boolean nlsrequirements = requirementFinder.nlsReq(currentItem);
        if (nlsrequirements) {

            RRGParseItem newItem = deducer.applyNoLeftSister(currentItem);

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
                        RRGParseItem scannedItem = new RRGParseItem.Builder().tree(tree)
                                                                             .node(lexLeaf)
                                                                             .nodepos(
                                                                                     NodePos.BOT)
                                                                             .start(start)
                                                                             .end(start + 1)
                                                                             .gaps(new HashSet<Gap>())
                                                                             .ws(false).build();
                        addToChartAndAgenda(scannedItem, Operation.SCAN);
                    }
                }
            }
        }
    }
}
