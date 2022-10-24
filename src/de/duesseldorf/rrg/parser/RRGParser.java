package de.duesseldorf.rrg.parser;

import de.duesseldorf.factorizer.*;
import de.duesseldorf.frames.Situation;
import de.duesseldorf.frames.UnifyException;
import de.duesseldorf.rrg.*;
import de.duesseldorf.rrg.extractor.ParseForestExtractor;
import de.duesseldorf.ui.ParsingInterface;
import de.tuebingen.tag.Environment;

import java.util.*;

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
@SuppressWarnings("ClassHasNoToStringMethod")
public class RRGParser {

    private RRGParseChart chart;
    private Agenda agenda;
    private RequirementFinder requirementFinder;
    private Deducer deducer;

    private FactorizingInterface factorizer;

    private boolean verbosePrintsToStdOut;

    private boolean noExtractionForBigCharts;
    private String axiom;

    public RRGParser(String axiom, FactorizingInterface factorizer) {
        this.axiom = (null == axiom) ? "" : axiom;
        this.requirementFinder = new RequirementFinder();
        this.deducer = new Deducer();
        this.factorizer = factorizer;
    }

    public RRGParseResult parseSentence(List<String> toksentence) {
        System.out.println("\nstart parsing sentence " + toksentence);
        System.out.println("number of trees in the grammar: "
                + ((RRG) Situation.getGrammar()).getTrees().size());
        this.agenda = new Agenda();
        this.chart = new RRGParseChart(toksentence.size(), axiom);

        // System.out.println("Environments before parsing:");

        // for (RRGTree rrgtree : ((RRG) Situation.getGrammar()).getTrees()){
        //     System.out.println(rrgtree.getEnv());
        // }


        // Axioms through scanning:
        if (!scan(toksentence)) {
            return new RRGParseResult.Builder().successfulParses(new HashSet<>())
                    .treesWithEdgeFeatureMismatches(new HashSet<>()).build();
        }

        this.requirementFinder = new RequirementFinder();

        System.out.println("Found fitting lexical items in the following "
                + agenda.size() + " eqClasses: ");
        if (!ParsingInterface.omitPrinting) {
            for (RRGParseItem item : agenda.getAllItems()) {
                System.out.println(item.getEqClass().getId()+", ");
                System.out.println(item.getEqClass().toString());
                System.out.println("--------------------------------");
            }
        }
        System.out.println("--------------------------------");

        // The real recognition
        int i = 0;
        while (!agenda.isEmpty()) {
            RRGParseItem currentItem = agenda.getNext();
            if (verbosePrintsToStdOut) {
                System.out.println("step: " + i + "\t" + currentItem);
            }
            i++;
            if (currentItem.getEqClass().isBottomClass()) {
                noleftsister(currentItem); //done
            } else {
                moveup(currentItem); //done
                substitute(currentItem); //done
                sisteradjoin(currentItem);//done
            }
            predictwrapping(currentItem);//done
            combinesisters(currentItem);//done
            completeWrapping(currentItem);
            generalizedCompleteWrapping(currentItem);
            jumpBackAfterGenWrapping(currentItem);
        }
        if (verbosePrintsToStdOut) {
            System.out.println("Done parsing. \n" + chart.toString());
        }

        System.out.println("Done parsing. Chart size: " + chart.computeSize());
        //System.out.println(chart);
        if (noExtractionForBigCharts && 3000 < chart.computeSize()) {
            System.out.println(
                    "ERROR: abort parse tree extraction because chart is too large: "
                            + chart.computeSize());
            return new RRGParseResult.Builder().build();
        } else {

            Set<RRGParseItem> goals = chart.retrieveGoalItems();
            // System.out.println("Environments after parsing:");

            // for (RRGTree rrgtree : ((RRG) Situation.getGrammar()).getTrees()){
            // 	System.out.println(rrgtree.getEnv());
            // }


            // extract parse results from chart
            //ParseForestExtractor extractor = new ParseForestExtractor(chart, TODO: Just for testing, needs to work
            //        toksentence);

            // System.out.println("Environments after extraction:");

            // for (RRGTree rrgtree : ((RRG) Situation.getGrammar()).getTrees()){
            // 	System.out.println(rrgtree.getEnv());
            // }

            //return extractor.extractParseTrees(); TODO: Just for testing, needs to work
            return new RRGParseResult.Builder().build();
        }

    }

    /**
     * @param antecedents always give the antecedent items in left-to-right order
     */
    private void addToChartAndAgenda(RRGParseItem consequent,
                                     Operation operation, RRGParseItem... antecedents) {
        if (chart.addItem(consequent, operation, antecedents)) {
            agenda.add(consequent);
        }
        // Debug
        if (verbosePrintsToStdOut) {
            System.out.println("next to agenda: " + consequent + "\n\t "
                    + operation + "\n\t antecedents: "
                    + Arrays.asList(antecedents));
        }
    }


    private void jumpBackAfterGenWrapping(RRGParseItem currentItem) {
        boolean isRootItemOfGenWrappingTree = requirementFinder.isJumpBackAntecedent(currentItem);
        if (isRootItemOfGenWrappingTree) {
            RRGParseItem consequent = deducer.applyJumpBackAfterGenWrapping(currentItem);

            addToChartAndAgenda(consequent, Operation.GENCWJUMPBACK, currentItem);
        }
    }

    private void generalizedCompleteWrapping(RRGParseItem currentItem) {
        boolean rootItem = requirementFinder.isGeneralizedCompleteWrappingTargetItem(currentItem);
        boolean fillerItem = requirementFinder.isInternalCompleteWrappingFillerItem(currentItem);
        // System.out.println("in generalizedCompleteWrapping (" + currentItem + ")");
        if (rootItem) {
            for (Gap gap : currentItem.getGaps()) {
                //System.out.println(gap);
                Set<RRGParseItem> completeWrappingFilterAntecedents =
                        requirementFinder.findCompleteWrappingFillers(currentItem, gap, chart);
                //System.out.println(completeWrappingFilterAntecedents);
                // keep only those where the node of the filler is a daughter of the root
                /*completeWrappingFilterAntecedents = completeWrappingFilterAntecedents.stream().filter( //GenWrapping warum mother,mother?
                        item -> item.getNode().getGornaddress().mother().mother() == null
                ).collect(Collectors.toSet());*/
                //System.out.println(completeWrappingFilterAntecedents);
                // find out if you moved one up or if you try to wrap around a single node
                Set<Set<RRGParseItem>> combineSisAntecedents = chart.getBackPointers(currentItem).getAntecedents(Operation.COMBINESIS);
                boolean wrappedAroundSameNode = false;
                for (Set<RRGParseItem> combsisants : combineSisAntecedents) {
                    RRGParseItem botCSAntecedent = combsisants.stream().filter(item -> item.getEqClass().isBottomClass()).findFirst().orElse(null);
                    if (null != botCSAntecedent) {
                        Set<Set<RRGParseItem>> pwantecedents = chart.getBackPointers(botCSAntecedent).getAntecedents(Operation.PREDICTWRAPPING);
                        for (Set<RRGParseItem> pwantecedent : pwantecedents) {
                            RRGParseItem pwantecedentItem = pwantecedent.stream().findFirst().orElse(null);
                            if (completeWrappingFilterAntecedents.stream().anyMatch((item) -> item.equals(pwantecedentItem))) {
                                wrappedAroundSameNode = true;
                            }
                        }
                    }
                }
                if (!wrappedAroundSameNode) {
                    for (RRGParseItem fillerddaughterItem : completeWrappingFilterAntecedents) {
                        RRGParseItem consequent = deducer.applyGeneralizedCompleteWrapping(currentItem, fillerddaughterItem, gap);
                        //System.out.println("Adding by generalizedCompleteWrapping: " + consequent);
                        //System.out.println("Previous item: " + currentItem);
                        addToChartAndAgenda(consequent, Operation.GENCW, currentItem, fillerddaughterItem);
                    }
                }
            }
        }
        if (fillerItem) {
            System.out.println("Got to internal wrapping not implemented second case!!!" + currentItem);
        }
    }

    private void completeWrapping(RRGParseItem currentItem) {
        // System.out.println("complW with " + currentItem);
        boolean rootItem = requirementFinder
                .isCompleteWrappingRootItem(currentItem);
        boolean fillerItem = requirementFinder
                .isCompleteWrappingFillerItem(currentItem);
        if (rootItem) {
            for (Gap gap : currentItem.getGaps()) {
                Set<RRGParseItem> completeWrappingFillerAntecedents = requirementFinder
                        .findCompleteWrappingFillers(currentItem, gap, chart);
                for (RRGParseItem fillerddaughterItem : completeWrappingFillerAntecedents) {
                    RRGParseItem consequent = deducer.applyCompleteWrapping(
                            currentItem, fillerddaughterItem, gap);
                    // System.out.println("did a Compl Wrapping with: "
                    // + consequent + currentItem);
                    addToChartAndAgenda(consequent, Operation.COMPLETEWRAPPING,
                            currentItem, fillerddaughterItem);
                }
            }
        }
        if (fillerItem) {
            // System.out.println("TODO in Parser CW 2 " + currentItem);
            Set<RRGParseItem> completeWrappingRootAntecedents =
                    requirementFinder
                            .findCompleteWrappingRoots(currentItem, chart);
            for (RRGParseItem rootAntecedent :
                    completeWrappingRootAntecedents) {
                for (Gap gap : rootAntecedent.getGaps()) {
                    if (gap.start != currentItem.startPos() || gap.end != currentItem.getEnd() || !gap.nonterminal.equals(currentItem.getEqClass().cat)) {
                        continue;
                    }
                    RRGParseItem consequent = deducer.applyCompleteWrapping(
                            rootAntecedent, currentItem, gap);
                    //System.out.println("I actually did something!!!");
                    //System.out.println("cons: " + consequent);
                    addToChartAndAgenda(consequent, Operation.COMPLETEWRAPPING,
                            currentItem, rootAntecedent);
                }
            }
            //System.out.println("untested completeWrapping territory! D");
            //System.out.println("root: " + completeWrappingRootAntecedents);
            //System.out.println("ddaughter: " + currentItem);
        }
    }

    private void predictwrapping(RRGParseItem currentItem) {
        if (requirementFinder.predWrappingReqs(currentItem)) {
            // look at the whole grammar and find fitting substitution nodes
            String cat = currentItem.getEqClass().cat;
            // System.out.println("got to predict: " + currentItem);

            Set<EqClassBot> substClasses = factorizer.getSubstClasses(cat);

            Set<Gap> gaps = new HashSet<>();
            gaps.add(new Gap(currentItem.startPos(),
                    currentItem.getEnd(), cat));

            for (EqClassBot substClass : substClasses) {
                boolean nodeUnificationPossible = true;
                try {
                    // RRGTreeTools.unifyNodes(substClass,
                    //         currentItem.getNode(),
                    //         currentItem.getTree().getEnv());
                    FactorizingInterface.unifyClasses(substClass.copyClass(),
                            currentItem.getEqClass().copyClass(),
                            new Environment(5));
                } catch (UnifyException e) {
                    nodeUnificationPossible = false;
                }

                if (nodeUnificationPossible) {
                    // System.out.println("got to for: " + substClass);
                    RRGParseItem cons = new RRGParseItem.Builder()
                            .eqClass(substClass)
                            .start(currentItem.startPos())
                            .end(currentItem.getEnd()).gaps(gaps)
                            .ws(false).build();
                    addToChartAndAgenda(cons, Operation.PREDICTWRAPPING,
                            currentItem);
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
        //System.out.println("sisteradjoin");
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
                RRGParseItem consequent = deducer.applyLeftAdjoin(target,
                        currentItem);
                addToChartAndAgenda(consequent, Operation.LEFTADJOIN,
                        currentItem, target);
            }

            // right-adjoin
            Set<RRGParseItem> rightAdjoinAntecedents = requirementFinder
                    .findRightAdjoinTargets(currentItem, chart);
            for (RRGParseItem target : rightAdjoinAntecedents) {
                RRGParseItem consequent = deducer.applyRightAdjoin(target,
                        currentItem);
                addToChartAndAgenda(consequent, Operation.RIGHTADJOIN, target,
                        currentItem);
            }
        } else if (sisAdjTarget) {
            Map<String, Set<RRGParseItem>> sisadjroots = requirementFinder
                    .findSisAdjRoots(currentItem, chart);
            // if (!sisadjroots.get("l").isEmpty()) {
            // System.out.println("sisadj with " + currentItem);
            // System.out.println("sisl" + sisadjroots.get("l"));
            // System.out.println("sisr" + sisadjroots.get("r"));
            // }
            // left-adjoin
            for (RRGParseItem auxRootItem : sisadjroots.get("l")) {
                RRGParseItem consequent = deducer.applyLeftAdjoin(currentItem,
                        auxRootItem);
                addToChartAndAgenda(consequent, Operation.LEFTADJOIN,
                        auxRootItem, currentItem);
            }
            // right-adjoin
            for (RRGParseItem auxRootItem : sisadjroots.get("r")) {
                RRGParseItem consequent = deducer.applyRightAdjoin(currentItem,
                        auxRootItem);
                addToChartAndAgenda(consequent, Operation.RIGHTADJOIN,
                        currentItem, auxRootItem);
                // System.out.println(auxRootItem + " and " + currentItem
                // + "\n\t lead to " + consequent);

                // System.out.println("RA " + currentItem + auxRootItem);
                // System.out.println(sisadjroots.get("r"));
            }
        }
    }

    private void substitute(RRGParseItem currentItem) {
        //System.out.println("substitute");
        //System.out.println(currentItem.getNode());

        if (requirementFinder.substituteReq(currentItem)) {
            Set<EqClassBot> substClasses = factorizer.getSubstClasses(currentItem.getEqClass().cat);
            for (EqClassBot substClass : substClasses) {
                boolean checkIfUnificationWorks = true;
                try {
                    // RRGTreeTools.unifyNodes(substClass,
                    //         currentItem.getNode(),
                    //         currentItem.getTree().getEnv());
                    FactorizingInterface.unifyClasses(substClass.copyClass(),
                            currentItem.getEqClass().copyClass(),
                            new Environment(5));
                } catch (UnifyException e) {
                    //System.out.println("Failed unification");
                    checkIfUnificationWorks = false;
                }
                if (checkIfUnificationWorks) {
                    substClass.setDaughters(currentItem.getEqClass().getDaughterEQClasses());
                    RRGParseItem cons = new RRGParseItem.Builder()
                            .eqClass(substClass)
                            .start(currentItem.startPos())
                            .end(currentItem.getEnd())
                            .gaps(currentItem.getGaps()).ws(false)
                            .genwrappingjumpback(currentItem.getGenwrappingjumpback())
                            .build();

                    addToChartAndAgenda(cons, Operation.SUBSTITUTE,
                            currentItem);
                }
            }
        }
    }

    private void moveup(RRGParseItem currentItem) {
        //System.out.println("moveup");
        // System.out.println("currentnode: " + currentItem.getNode());
        boolean moveupreq = requirementFinder.moveupReq(currentItem);
        if (moveupreq) {
            Set<RRGParseItem> newItems = deducer.applyMoveUp(currentItem);
            for(RRGParseItem item: newItems) {
                addToChartAndAgenda(item, Operation.MOVEUP, currentItem);
            }
        }
    }

    private void combinesisters(RRGParseItem currentItem) {
         // case 1: currentItem is the left node of the combination
        Set<RRGParseItem> rightSisterCandidates = requirementFinder
                .findCombineSisRightSisters(currentItem, chart);
        for (RRGParseItem rightSisterAntecedentItem : rightSisterCandidates) {

            Set<RRGParseItem> rightSisTopItems = deducer.applyCombineSisters(
                    currentItem, rightSisterAntecedentItem);

            for (RRGParseItem ti : rightSisTopItems) {
                addToChartAndAgenda(ti, Operation.COMBINESIS,
                        currentItem, rightSisterAntecedentItem);
            }
        }
        // case 2: currentItem is the right node of the combination
        Set<RRGParseItem> leftSisterCandidates = requirementFinder
                .findCombineSisLeftSisters(currentItem, chart);
        for (RRGParseItem leftSisterAntecedentItem : leftSisterCandidates) {

            Set<RRGParseItem> rightSisTopItems = deducer
                    .applyCombineSisters(leftSisterAntecedentItem, currentItem);

            for (RRGParseItem ti : rightSisTopItems) {
                addToChartAndAgenda(ti, Operation.COMBINESIS,
                        leftSisterAntecedentItem, currentItem);
            }
        }
    }

    /**
     * note that NLS is the only deduction rule that can be done for items in
     * BOT position on a leftmost daughter node
     *
     */
    private void noleftsister(RRGParseItem currentItem) {
        //System.out.println("noleftsister");
        boolean nlsrequirements = requirementFinder.nlsReq(currentItem);
        if (nlsrequirements) {

            Set<RRGParseItem> newItems = deducer.applyNoLeftSister(currentItem);
            for(RRGParseItem item: newItems){
                addToChartAndAgenda(item, Operation.NLS, currentItem);
            }
        }
    }

    /**
     * apply the scanning deduction rule
     */
    private boolean scan(List<String> sentence) {
        //System.out.println("scan");
        Collection<String> scannedWords = new HashSet<>();
        // Look at all trees
        //System.out.println("RRGParser scan: "+tree);
        // Look at all words
        for (int start = 0; start < sentence.size(); start++) {
            String word = sentence.get(start);

            // See if the word is a lex Node of the tree
            Set<EqClassBot> candidates = factorizer.getLexClasses(word);
            if (null != candidates) {
                scannedWords.add(word + "_" + start);
                for (EqClassBot lexLeaf : candidates) {
                    // If so, create a new item and add it to the chart and
                    // agenda
                    RRGParseItem scannedItem = new RRGParseItem.Builder()
                            .eqClass(lexLeaf)
                            .start(start).end(start + 1)
                            .gaps(new HashSet<>()).ws(false).build();
                    addToChartAndAgenda(scannedItem, Operation.SCAN);
                }
            }
        }

        if (scannedWords.size() != sentence.size()) {
            System.out.println("the number of scanned words and the number of words in the sentence differ.");
            if (scannedWords.size() < sentence.size()) {
                System.out.println("Is there a word in the sentence that is not in your lexicon");
            }
            System.out.println("Sentence: " + sentence);
            System.out.println("Scanned words: " + scannedWords);
            for (String word : sentence) {
                if (!scannedWords.contains(word)) {
                    System.out.println("Word from sentence not in the set of scanned words: " + word);
                }
            }
        }
        return scannedWords.size() == sentence.size();
    }
}
