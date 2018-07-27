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

/**
 * File NewParseForestExtractor.java
 * 
 * Authors:
 * David Arps <david.arps@hhu.de>
 * 
 * Copyright
 * David Arps, 2018
 * 
 * 
 * This file is part of the TuLiPA-frames system
 * https://github.com/spetitjean/TuLiPA-frames
 * 
 * 
 * TuLiPA is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * TuLiPA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
public class ParseForestExtractor {

    private SimpleRRGParseChart parseChart;
    Set<RRGParseTree> resultingParses;

    public boolean verbosePrintsToStdOut = false;

    private Set<Operation> sameElemTree = new HashSet<Operation>(Arrays
            .asList(Operation.COMBINESIS, Operation.MOVEUP, Operation.NLS));
    private List<String> toksentence;

    public ParseForestExtractor(SimpleRRGParseChart parseChart,
            List<String> toksentence) {
        this.parseChart = parseChart;
        this.toksentence = toksentence;
        resultingParses = new HashSet<RRGParseTree>();
    }

    public Set<RRGParseTree> extractParseTrees() {
        // find goal items in the chart. Extract them all and add the set of
        // parse trees derived from them to the resulting parses
        Set<ParseItem> goals = parseChart.retrieveGoalItems();
        if (verbosePrintsToStdOut) {
            System.out.println("goal items: " + goals);
        }
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
     *            item and creates the initial extraction step from that item
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
        Set<RRGParseTree> parsesInThisStep = new HashSet<RRGParseTree>();

        if (verbosePrintsToStdOut) {
            System.out.println(extractionstep);
        }
        // distinguish different operations here
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

        // Left-Adjoin
        Set<List<ParseItem>> leftAdjAntecedents = backPointers
                .getAntecedents(Operation.LEFTADJOIN);
        parsesInThisStep
                .addAll(extractLeftAdjoin(leftAdjAntecedents, extractionstep));

        // Right-Adjoin
        Set<List<ParseItem>> rightAdjAntecedents = backPointers
                .getAntecedents(Operation.RIGHTADJOIN);
        parsesInThisStep.addAll(
                extractRightAdjoin(rightAdjAntecedents, extractionstep));

        // Complete-Wrapping
        Set<List<ParseItem>> coWrAntecedents = backPointers
                .getAntecedents(Operation.COMPLETEWRAPPING);
        parsesInThisStep.addAll(
                extractCompleteWrapping(coWrAntecedents, extractionstep));

        // Predict-Wrapping
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
    }

    private Set<RRGParseTree> extractPredictWrapping(
            Set<List<ParseItem>> predictWrappingAntecedents,
            ExtractionStep extractionstep) {

        Set<RRGParseTree> parsesInThisPWStep = new HashSet<RRGParseTree>();
        for (List<ParseItem> predictWrappingantecedentItemsingletonList : predictWrappingAntecedents) {
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.PREDICTWRAPPING);
            }
            // do the substitution and extract wrapping tree below d-daughter
            // System.out.println("got here! antecedens: "
            // + predictWrappingantecedentItemsingletonList);
            // System.out.println("step: " + extractionstep);

            SimpleRRGParseItem predictWrappingAntecedentItem = (SimpleRRGParseItem) predictWrappingantecedentItemsingletonList
                    .get(0);
            GornAddress ddaughterAbsAddress = new GornAddress(
                    extractionstep.getGAInParseTree());
            // insert the subtree stored in the RRGParseTree at the correct GA
            // (seems to work)
            // continue extraction from there with same GA
            RRGParseTree nextStepParseTree = extractionstep
                    .getCurrentParseTree().addWrappingSubTree(
                            ddaughterAbsAddress, predictWrappingAntecedentItem);
            ExtractionStep nextStep = new ExtractionStep(
                    predictWrappingAntecedentItem, ddaughterAbsAddress,
                    nextStepParseTree);
            parsesInThisPWStep.addAll(extract(nextStep));
        }
        return parsesInThisPWStep;
    }

    private Set<RRGParseTree> extractCompleteWrapping(
            Set<List<ParseItem>> completeWrappingAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisCWStep = new HashSet<RRGParseTree>();
        for (List<ParseItem> CWantecedentItems : completeWrappingAntecedents) {
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.COMPLETEWRAPPING);
            }
            // System.out.println(
            // "do complete Wrapping with\n" + CWantecedentItems.toString()
            // + "\n" + extractionstep.getCurrentItem());

            SimpleRRGParseItem dDaughter = (SimpleRRGParseItem) (((SimpleRRGParseItem) CWantecedentItems
                    .get(0)).getwsflag() ? CWantecedentItems.get(0)
                            : CWantecedentItems.get(1));
            SimpleRRGParseItem gapItem = (SimpleRRGParseItem) (((SimpleRRGParseItem) CWantecedentItems
                    .get(0)).getwsflag() ? CWantecedentItems.get(1)
                            : CWantecedentItems.get(0));

            // extract the d-daughter in the predict-wrapping step

            // insert the wrapped tree into the parse tree
            // System.out.println(
            // "before wrapping: " + extractionstep.getCurrentParseTree());
            // System.out.println("insert that tree: " + gapItem.getTree());
            // System.out.println("at: " + extractionstep.getGAInParseTree());
            RRGParseTree nextStepParseTree = extractionstep
                    .getCurrentParseTree().insertWrappedTree(gapItem.getTree(),
                            extractionstep.getGAInParseTree(), dDaughter);
            // System.out.println("after wrapping: " + nextStepParseTree);

            // adjust Gorn Addresses here.
            GornAddress shiftedGAInParseTree = extractionstep.getGAInParseTree()
                    .mother();
            // System.out.println("extract from address (root of wrapped tree):
            // "
            // + shiftedGAInParseTree);
            ExtractionStep nextStep = new ExtractionStep(gapItem,
                    shiftedGAInParseTree, nextStepParseTree,
                    extractionstep.getGAInParseTree().isIthDaughter());
            parsesInThisCWStep.addAll(extract(nextStep));
        }
        return parsesInThisCWStep;
    }

    private Set<RRGParseTree> extractRightAdjoin(
            Set<List<ParseItem>> rightAdjAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisRightAdjStep = new HashSet<RRGParseTree>();
        for (List<ParseItem> rightAdjAntecedentItems : rightAdjAntecedents) {
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.RIGHTADJOIN);
            }
            Set<RRGParseTree> tmpResult = new HashSet<RRGParseTree>();

            // First: find both items
            // Second: extract the right one (might cause problem when the
            // consequent item of this extraction step was the left sister in a
            // ComBSis step and was extracted there first
            SimpleRRGParseItem auxRootItem, targetItem;
            for (List<ParseItem> rightAdjAntecedentList : rightAdjAntecedents) {
                if ((((SimpleRRGParseItem) rightAdjAntecedentItems.get(0))
                        .getNode().getType().equals(RRGNodeType.STAR))) {
                    auxRootItem = ((SimpleRRGParseItem) rightAdjAntecedentItems
                            .get(0));
                    targetItem = ((SimpleRRGParseItem) rightAdjAntecedentItems
                            .get(1));
                } else {
                    auxRootItem = ((SimpleRRGParseItem) rightAdjAntecedentItems
                            .get(1));
                    targetItem = ((SimpleRRGParseItem) rightAdjAntecedentItems
                            .get(0));
                }

                RRGParseTree nextStepParseTree = extractionstep
                        .getCurrentParseTree()
                        .sisterAdjoin(auxRootItem.getTree(),
                                extractionstep.getGAInParseTree().mother(),
                                extractionstep.getGAInParseTree()
                                        .isIthDaughter() + 1);
                // extract aux tree:
                ExtractionStep nextStep = new ExtractionStep(auxRootItem,
                        extractionstep.getGAInParseTree().mother(),
                        nextStepParseTree,
                        extractionstep.goToRightWhenGoingDown + 1);

                tmpResult = extract(nextStep);
                // extract target:
                for (RRGParseTree rrgParseTree : tmpResult) {
                    nextStep = new ExtractionStep(targetItem,
                            extractionstep.getGAInParseTree(), rrgParseTree);
                    parsesInThisRightAdjStep.addAll(extract(nextStep));
                }
            }
            // first, do the actual sister adjunction
            // RRGTree adjoiningTree;
            // if ((((SimpleRRGParseItem)
            // rightAdjAntecedentItems.get(0)).getNode()
            // .getType().equals(RRGNodeType.STAR))) {
            // adjoiningTree = ((SimpleRRGParseItem) rightAdjAntecedentItems
            // .get(0)).getTree();
            // } else {
            // adjoiningTree = ((SimpleRRGParseItem) rightAdjAntecedentItems
            // .get(1)).getTree();
            // }
            // System.out.println(
            // "before sisadj: " + extractionstep.getCurrentParseTree());
            // System.out.println(
            // "sisadj at " + extractionstep.getGAInParseTree().mother());
            // System.out.println("as daughter: "
            // + (extractionstep.getGAInParseTree().isIthDaughter() + 1));
            // RRGParseTree nextStepParseTree = extractionstep
            // .getCurrentParseTree().sisterAdjoin(adjoiningTree,
            // extractionstep.getGAInParseTree().mother(),
            // extractionstep.getGAInParseTree().isIthDaughter()
            // + 1);
            // System.out.println("after: " + nextStepParseTree);
            // tmpResult.add(nextStepParseTree);
            // for (RRGParseTree parseTree : tmpResult) {

            // for (int i = 0; i < rightAdjAntecedentItems.size(); i++) {
            // SimpleRRGParseItem rightAdjAntecedentItem =
            // (SimpleRRGParseItem) rightAdjAntecedentItems
            // .get(i);
            // ExtractionStep nextStep;
            // GornAddress GAtoExtractFrom;
            // boolean antItemIsAdjoiningTree = rightAdjAntecedentItem
            // .getNode().getType().equals(RRGNodeType.STAR);
            // int goToRightWhenGoingDown = 0;
            // if (antItemIsAdjoiningTree) { // extraction of the adjoining
            // // tree
            // GAtoExtractFrom = extractionstep.getGAInParseTree()
            // .mother();
            // goToRightWhenGoingDown = 1;
            // } else { // continue extraction of the target tree
            // GAtoExtractFrom = extractionstep.getGAInParseTree();
            // }
            // System.out.println(
            // "the GAs have changed. Consider GA shifting again!");
            // nextStep = new ExtractionStep(rightAdjAntecedentItem,
            // GAtoExtractFrom, nextStepParseTree,
            // goToRightWhenGoingDown);
            // // System.out.println("next round: " + nextStep);
            // if (i == 0) {
            // tmpResult = extract(nextStep);
            // } else {
            // parsesInThisRightAdjStep.addAll(extract(nextStep));
            // }
            //
            // }
            // }
        }

        return parsesInThisRightAdjStep;

    }

    private Set<RRGParseTree> extractLeftAdjoin(
            Set<List<ParseItem>> leftAdjAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisLeftAdjStep = new HashSet<RRGParseTree>();
        for (List<ParseItem> leftAdjAntecedentItems : leftAdjAntecedents) {
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.LEFTADJOIN);
            }
            // System.out.println("leftsisadj: " + leftAdjAntecedentItems);
            Set<RRGParseTree> tmpResult = new HashSet<RRGParseTree>();
            // tmpResult.add(extractionstep.getCurrentParseTree());

            SimpleRRGParseItem auxRootItem, rightSisItem;
            if (((SimpleRRGParseItem) leftAdjAntecedentItems.get(0))
                    .startPos() <= ((SimpleRRGParseItem) leftAdjAntecedentItems
                            .get(1)).startPos()) {
                auxRootItem = (SimpleRRGParseItem) leftAdjAntecedentItems
                        .get(0);
                rightSisItem = (SimpleRRGParseItem) leftAdjAntecedentItems
                        .get(1);
            } else {
                auxRootItem = (SimpleRRGParseItem) leftAdjAntecedentItems
                        .get(1);
                rightSisItem = (SimpleRRGParseItem) leftAdjAntecedentItems
                        .get(0);
            }

            ExtractionStep nextStep = new ExtractionStep(rightSisItem,
                    extractionstep.getGAInParseTree(),
                    extractionstep.getCurrentParseTree());
            tmpResult = extract(nextStep);
            for (RRGParseTree rrgParseTree : tmpResult) {
                RRGParseTree nextStepParseTree = rrgParseTree.sisterAdjoin(
                        auxRootItem.getTree(),
                        extractionstep.getGAInParseTree().mother(), 0);
                nextStep = new ExtractionStep(auxRootItem,
                        extractionstep.getGAInParseTree().mother(),
                        nextStepParseTree);
                parsesInThisLeftAdjStep.addAll(extract(nextStep));
            }
            // first, do the actual sister adjunction
            // RRGTree adjoiningTree;
            // if ((((SimpleRRGParseItem)
            // leftAdjAntecedentItems.get(0)).getNode()
            // .getType().equals(RRGNodeType.STAR))) {
            // adjoiningTree = ((SimpleRRGParseItem) leftAdjAntecedentItems
            // .get(0)).getTree();
            // } else {
            // adjoiningTree = ((SimpleRRGParseItem) leftAdjAntecedentItems
            // .get(1)).getTree();
            // }
            // System.out.println(
            // "before sisadj: " + extractionstep.getCurrentParseTree());
            // System.out.println("adjoin this tree at: "
            // + extractionstep.getGAInParseTree().mother() + ":\n"
            // + adjoiningTree);
            // RRGParseTree nextStepParseTree = extractionstep
            // .getCurrentParseTree().sisterAdjoin(adjoiningTree,
            // extractionstep.getGAInParseTree().mother(), 0);
            // System.out.println("after: " + nextStepParseTree);
            // tmpResult.add(nextStepParseTree);
            //
            // for (RRGParseTree parseTree : tmpResult) {
            // for (int i = 0; i < leftAdjAntecedentItems.size(); i++) {
            // SimpleRRGParseItem leftAdjAntecedentItem = (SimpleRRGParseItem)
            // leftAdjAntecedentItems
            // .get(i);
            // ExtractionStep nextStep;
            // GornAddress GAtoExtractFrom;
            // int goToRightWhenGoingDown = 0;
            // boolean antItemIsAdjoiningTree = leftAdjAntecedentItem
            // .getNode().getType().equals(RRGNodeType.STAR);
            // if (antItemIsAdjoiningTree) { // extraction of the adjoining
            // // tree
            // GAtoExtractFrom = extractionstep.getGAInParseTree()
            // .mother();
            // } else { // continue extraction of the target tree
            // // goToRightWhenGoingDown = 1;
            // GAtoExtractFrom = extractionstep.getGAInParseTree()
            // .rightSister();
            // // nextStepParseTree = parseTree;
            // }
            // nextStep = new ExtractionStep(leftAdjAntecedentItem,
            // GAtoExtractFrom, nextStepParseTree,
            // goToRightWhenGoingDown);
            // System.out.println("next round: " + nextStep);
            // // TODO problem: When extracting sisadj or wrapping as well,
            // // probably, the GA to extract from has changing conditions
            // // because material can be added in places affecting the GA.
            // if (i == 0) {
            // tmpResult = extract(nextStep);
            // } else {
            // parsesInThisLeftAdjStep.addAll(extract(nextStep));
            // }
            //
            // }
            // }
        }
        return parsesInThisLeftAdjStep;
    }

    private Set<RRGParseTree> extractCombSis(
            Set<List<ParseItem>> combsisAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisCombSisStep = new HashSet<RRGParseTree>();
        // how many different antecedents are there?
        for (List<ParseItem> combsisantecedentItems : combsisAntecedents) {
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.COMBINESIS);
            }

            // find out which item is which
            SimpleRRGParseItem leftItem, rightItem;
            if (((SimpleRRGParseItem) combsisantecedentItems.get(0))
                    .getNodePos().equals(SimpleRRGParseItem.NodePos.TOP)) {
                leftItem = (SimpleRRGParseItem) combsisantecedentItems.get(0);
                rightItem = (SimpleRRGParseItem) combsisantecedentItems.get(1);
            } else {
                leftItem = (SimpleRRGParseItem) combsisantecedentItems.get(1);
                rightItem = (SimpleRRGParseItem) combsisantecedentItems.get(0);
            }

            ExtractionStep nextStep = new ExtractionStep(rightItem,
                    extractionstep.getGAInParseTree(),
                    extractionstep.getCurrentParseTree());
            Set<RRGParseTree> tmpResult = extract(nextStep);
            for (RRGParseTree rrgParseTree : tmpResult) {
                nextStep = new ExtractionStep(leftItem,
                        extractionstep.getGAInParseTree().leftSister(),
                        rrgParseTree);
                parsesInThisCombSisStep.addAll(extract(nextStep));
            }
            // old part:
            // Set<RRGParseTree> tmpResult = new HashSet<RRGParseTree>();
            // tmpResult.add(extractionstep.getCurrentParseTree());
            // for (int i = 0; i < combsisantecedentItems.size(); i++) {
            // SimpleRRGParseItem combsisantecedentItem = (SimpleRRGParseItem)
            // combsisantecedentItems
            // .get(i);
            // idea of this part: The inner for loop only takes one round
            // when extracting the first item from the list. When extracting
            // the first item, add all extracted parse trees to tmpResult.
            // Then, you can further elaborate those possibilities with a
            // second take on the inner for loop.
            // for (RRGParseTree oneTree : tmpResult) {
            // ExtractionStep nextStep;
            // GornAddress GAtoExtractFrom;
            // boolean currentAntecedentIsRightSister = ((SimpleRRGParseItem)
            // combsisantecedentItem)
            // .getNode().getGornaddress()
            // .equals(extractionstep.getCurrentItem().getNode()
            // .getGornaddress());
            // First, find the GA from which to further extract
            // processing the right sister
            // if (currentAntecedentIsRightSister) {
            // GAtoExtractFrom = extractionstep.getGAInParseTree();
            // } else { // processing the left sister
            // GAtoExtractFrom = extractionstep.getGAInParseTree()
            // .leftSister();
            // }
            // Then, extract and add to the right set (see above)
            // nextStep = new ExtractionStep(
            // (SimpleRRGParseItem) combsisantecedentItem,
            // GAtoExtractFrom, oneTree);
            //
            // if (i == 0) {
            // tmpResult = extract(nextStep);
            // } else {
            // parsesInThisCombSisStep.addAll(extract(nextStep));
            // }
            // }
            // }

        }
        return parsesInThisCombSisStep;

    }

    private Set<RRGParseTree> extractSubst(
            Set<List<ParseItem>> substAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisSUBSTStep = new HashSet<RRGParseTree>();
        for (List<ParseItem> substantecedentItemSingletonList : substAntecedents) {
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.SUBSTITUTE);
            }
            // System.out.println("Subst: " + substantecedentItemSingletonList
            // + "\n" + extractionstep);

            SimpleRRGParseItem substAntecedentItem = (SimpleRRGParseItem) substantecedentItemSingletonList
                    .get(0);
            GornAddress GAtoReplaceAt = extractionstep.getGAInParseTree();
            RRGTree substTree = substAntecedentItem.getTree();
            // System.out.println("try to subst this tree: " + substTree);
            // System.out.println(
            // "into that tree: " + extractionstep.getCurrentParseTree());
            // System.out.println("at GA " + GAtoReplaceAt);
            RRGParseTree nextStepParseTree = extractionstep
                    .getCurrentParseTree().substitute(substTree, GAtoReplaceAt);
            // System.out.println("result: " + nextStepParseTree);
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
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.MOVEUP);
            }
            SimpleRRGParseItem moveupAntecedentItem = (SimpleRRGParseItem) moveupAntecedentItemSingletonList
                    .get(0);
            GornAddress newMoveUpGA = extractionstep.getGAInParseTree()
                    .ithDaughter(moveupAntecedentItem.getNode().getGornaddress()
                            .isIthDaughter()
                            + extractionstep.getGoToRightWhenGoingDown());
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
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.NLS);
            }
            ExtractionStep nextStep = new ExtractionStep(
                    (SimpleRRGParseItem) antecedentItemSingletonList.get(0),
                    extractionstep.getGAInParseTree(),
                    extractionstep.getCurrentParseTree(),
                    extractionstep.getGoToRightWhenGoingDown());
            parsesInThisNLSStep.addAll(extract(nextStep));
        }

        return parsesInThisNLSStep;

    }
}
