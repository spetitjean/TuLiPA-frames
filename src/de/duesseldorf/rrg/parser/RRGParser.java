package de.duesseldorf.rrg.parser;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import de.duesseldorf.frames.Situation;
import de.duesseldorf.rrg.RRG;
import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGParseTree;
import de.duesseldorf.rrg.RRGTree;
import de.duesseldorf.rrg.extractor.NewParseForestExtractor;
import de.duesseldorf.rrg.parser.SimpleRRGParseItem.NodePos;

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
        int i = 0;
        while (!agenda.isEmpty()) {
            // TODO: optimize this based on the node position?
            // System.out.println("step: " + i);
            i++;
            SimpleRRGParseItem currentItem = agenda.pollFirst();
            // System.out.println("current item: " + currentItem);
            noleftsister(currentItem);
            moveup(currentItem);
            combinesisters(currentItem);
            substitute(currentItem);
            sisteradjoin(currentItem);
            predictwrapping(currentItem);
            completewrapping(currentItem);
            // System.out.println("Agenda size: " + agenda.size());
        }
        System.out.println("Done parsing. \n" + chart.toString());
        // old version:
        // ParseForestExtractor extractor = new ParseForestExtractor(chart,
        // toksentence);
        // new version:
        NewParseForestExtractor extractor = new NewParseForestExtractor(chart,
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
     *            always give the antecedent items in left-to-right order
     */
    private void addToChartAndAgenda(SimpleRRGParseItem consequent,
            Operation operation, SimpleRRGParseItem... antecedents) {
        if (chart.addItem(consequent, operation, antecedents)) {
            agenda.add(consequent);
        }
        // Debug
        // System.out.println("next to agenda: " + consequent + "\n\t " +
        // operation
        // + "\n\t antecedents: " + Arrays.asList(antecedents));
    }

    private void completewrapping(SimpleRRGParseItem currentItem) {
        // System.out.println("complW with " + currentItem);
        boolean rootItem = requirementFinder
                .isCompleteWrappingRootItem(currentItem);
        boolean fillerItem = requirementFinder
                .isCompleteWrappingFillerItem(currentItem);
        if (rootItem) {
            for (Gap gap : currentItem.getGaps()) {
                Set<SimpleRRGParseItem> completeWrappingFillerAntecedents = requirementFinder
                        .findCompleteWrappingFillers(currentItem, gap, chart);
                for (SimpleRRGParseItem fillerddaughterItem : completeWrappingFillerAntecedents) {
                    SimpleRRGParseItem consequent = deducer
                            .applyCompleteWrapping(currentItem,
                                    fillerddaughterItem, gap);
                    addToChartAndAgenda(consequent, Operation.COMPLETEWRAPPING,
                            currentItem, fillerddaughterItem);
                }
            }

        }
        if (fillerItem) {
            System.out.println("TODO in Parser CW 2 " + currentItem);
            Set<SimpleRRGParseItem> completeWrappingRootAntecedents = requirementFinder
                    .findCompleteWrappingRoots(currentItem, chart);
            System.out.println("untested completeWrapping territory! D");
            // System.out.println("root: " + completeWrappingRootAntecedents);
            // System.out.println("ddaughter: " + currentItem);
        }
    }

    private void predictwrapping(SimpleRRGParseItem currentItem) {
        if (requirementFinder.predWrappingReqs(currentItem)) {
            // look at the whole grammar and find fitting substitution nodes
            String cat = currentItem.getNode().getCategory();
            // System.out.println("got to predict: " + currentItem);
            for (RRGTree tree : ((RRG) situation.getGrammar()).getTrees()) {
                Set<RRGNode> substNodes = tree.getSubstNodes().get(cat);
                if (substNodes != null) {
                    HashSet<Gap> gaps = new HashSet<Gap>();
                    gaps.add(new Gap(currentItem.startPos(),
                            currentItem.getEnd(), cat));
                    for (RRGNode substNode : substNodes) {
                        // System.out.println("got to for: " + substNode);
                        SimpleRRGParseItem consequent = new SimpleRRGParseItem(
                                currentItem, tree, substNode,
                                SimpleRRGParseItem.NodePos.BOT, -1, -1, gaps,
                                false, false);

                        // System.out.println("cons: " + consequent);
                        addToChartAndAgenda(consequent,
                                Operation.PREDICTWRAPPING, currentItem);
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
    private void sisteradjoin(SimpleRRGParseItem currentItem) {
        boolean sisadjroot = requirementFinder.isSisadjRoot(currentItem);
        boolean sisAdjTarget = requirementFinder.isSisadjTarget(currentItem);
        // System.out.print(root);
        // System.out.println(" " + currentItem.toString());
        if (sisadjroot) {
            // left-adjoin
            Set<SimpleRRGParseItem> leftAdjoinTargets = requirementFinder
                    .findLeftAdjoinTargets(currentItem, chart);
            for (SimpleRRGParseItem target : leftAdjoinTargets) {
                // System.out.println("THERE: " + simpleRRGParseItem);
                SimpleRRGParseItem consequent = deducer.applyLeftAdjoin(target,
                        currentItem);
                addToChartAndAgenda(consequent, Operation.LEFTADJOIN,
                        currentItem, target);
            }

            // right-adjoin
            Set<SimpleRRGParseItem> rightAdjoinAntecedents = requirementFinder
                    .findRightAdjoinTargets(currentItem, chart);
            for (SimpleRRGParseItem target : rightAdjoinAntecedents) {
                SimpleRRGParseItem consequent = deducer.applyRightAdjoin(target,
                        currentItem);
                addToChartAndAgenda(consequent, Operation.RIGHTADJOIN, target,
                        currentItem);
                System.out.println(
                        "you triggered some special case for sister adjunction which I haven't tested yet. D");
            }
            // System.out.println("rightadjoin: " + currentItem);
            // System.out.println(rightAdjoinAntecedents);
        } else if (sisAdjTarget) {
            Map<String, Set<SimpleRRGParseItem>> sisadjroots = requirementFinder
                    .findSisAdjRoots(currentItem, chart);
            // System.out.println("sisadj with " + currentItem);
            // System.out.println("sisl" + sisadjroots.get("l"));
            // System.out.println("sisr" + sisadjroots.get("r"));

            // left-adjoin
            for (SimpleRRGParseItem auxRootItem : sisadjroots.get("l")) {
                SimpleRRGParseItem consequent = deducer
                        .applyLeftAdjoin(currentItem, auxRootItem);
                addToChartAndAgenda(consequent, Operation.LEFTADJOIN,
                        auxRootItem, currentItem);
            }
            // right-adjoin
            for (SimpleRRGParseItem auxRootItem : sisadjroots.get("r")) {
                SimpleRRGParseItem consequent = deducer
                        .applyRightAdjoin(currentItem, auxRootItem);
                addToChartAndAgenda(consequent, Operation.RIGHTADJOIN,
                        currentItem, auxRootItem);
                // System.out.println(auxRootItem + " and " + currentItem
                // + "\n\t lead to " + consequent);

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
                                false, true);
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

        // case 1: currentItem is the left node of the combination
        Set<SimpleRRGParseItem> rightSisterCandidates = requirementFinder
                .findCombineSisRightSisters(currentItem, chart);
        // System.out.println("currentItem: " + leftSisterAntecedentItem);
        for (SimpleRRGParseItem rightSisterAntecedentItem : rightSisterCandidates) {
            // System.out.println(
            // "mate with: " + rightSisterAntecedentItem + "results in");
            SimpleRRGParseItem rightSisTopItem = deducer.applyCombineSisters(
                    currentItem, rightSisterAntecedentItem);
            // System.out.println(rightSisTopItem);
            addToChartAndAgenda(rightSisTopItem, Operation.COMBINESIS,
                    currentItem, rightSisterAntecedentItem);
        }
        // case 2: currentItem is the right node of the combination
        Set<SimpleRRGParseItem> leftSisterCandidates = requirementFinder
                .findCombineSisLeftSisters(currentItem, chart);
        for (SimpleRRGParseItem leftSisterAntecedentItem : leftSisterCandidates) {
            SimpleRRGParseItem rightSisTopItem = deducer
                    .applyCombineSisters(leftSisterAntecedentItem, currentItem);
            addToChartAndAgenda(rightSisTopItem, Operation.COMBINESIS,
                    leftSisterAntecedentItem, currentItem);

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
                                new HashSet<Gap>(), false);
                        addToChartAndAgenda(scannedItem, Operation.SCAN);
                    }
                }
            }
        }
    }
}
