package de.duesseldorf.rrg.extractor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.RRGParseTree;
import de.duesseldorf.rrg.RRGTools;
import de.duesseldorf.rrg.RRGTree;
import de.duesseldorf.rrg.parser.Backpointer;
import de.duesseldorf.rrg.parser.Operation;
import de.duesseldorf.rrg.parser.RRGParseChart;
import de.duesseldorf.rrg.parser.RRGParseItem;
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

    private RRGParseChart parseChart;
    private Set<RRGTree> resultingParses;

    public boolean verbosePrintsToStdOut = true;

    private Set<Operation> sameElemTree = new HashSet<Operation>(Arrays
            .asList(Operation.COMBINESIS, Operation.MOVEUP, Operation.NLS));
    private List<String> toksentence;

    public ParseForestExtractor(RRGParseChart parseChart,
            List<String> toksentence) {
        this.parseChart = parseChart;
        this.toksentence = toksentence;
        resultingParses = new HashSet<RRGTree>();
    }

    public Set<RRGParseTree> extractParseTrees() {
        // find goal items in the chart. Extract them all and add the set of
        // parse trees derived from them to the resulting parses
        Set<RRGParseItem> goals = parseChart.retrieveGoalItems();
        if (verbosePrintsToStdOut) {
            System.out.println("goal items: " + goals);
        }
        for (RRGParseItem goal : goals) {
            ExtractionStep initExtrStep = initialExtractionStep(
                    (RRGParseItem) goal);
            Set<RRGParseTree> resultingTrees = extract(initExtrStep);
            addToResultingParses(resultingTrees);
        }
        Set<RRGParseTree> resultingParsesFiltered = RRGTools.convertTreeSet(
                RRGTools.removeDoubleTreesByWeakEquals(resultingParses));
        // .filterDoublesByIdMap(resultingParses);
        return resultingParsesFiltered;
        // return RRGTools.convertTreeSet(resultingParses);
    }

    /**
     * this method takes a
     * 
     * @param goal
     *            item and creates the initial extraction step from that item
     */
    private ExtractionStep initialExtractionStep(RRGParseItem goal) {
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
        Set<List<RRGParseItem>> moveupAntecedents = backPointers
                .getAntecedents(Operation.MOVEUP);
        parsesInThisStep
                .addAll(extractMoveUp(moveupAntecedents, extractionstep));

        // Combine-Sisters
        Set<List<RRGParseItem>> combsisAntecedents = backPointers
                .getAntecedents(Operation.COMBINESIS);
        parsesInThisStep
                .addAll(extractCombSis(combsisAntecedents, extractionstep));

        // Substitution
        Set<List<RRGParseItem>> substAntecedents = backPointers
                .getAntecedents(Operation.SUBSTITUTE);
        parsesInThisStep.addAll(extractSubst(substAntecedents, extractionstep));

        // Left-Adjoin
        Set<List<RRGParseItem>> leftAdjAntecedents = backPointers
                .getAntecedents(Operation.LEFTADJOIN);
        parsesInThisStep
                .addAll(extractLeftAdjoin(leftAdjAntecedents, extractionstep));

        // Right-Adjoin
        Set<List<RRGParseItem>> rightAdjAntecedents = backPointers
                .getAntecedents(Operation.RIGHTADJOIN);
        parsesInThisStep.addAll(
                extractRightAdjoin(rightAdjAntecedents, extractionstep));

        // Complete-Wrapping
        Set<List<RRGParseItem>> coWrAntecedents = backPointers
                .getAntecedents(Operation.COMPLETEWRAPPING);
        parsesInThisStep.addAll(
                extractCompleteWrapping(coWrAntecedents, extractionstep));

        // Predict-Wrapping
        Set<List<RRGParseItem>> prWrAntecedents = backPointers
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

        Set<RRGParseTree> parsesInThisStepWithCurrentExtractionStep = addExtractionStepToAllTrees(
                parsesInThisStep, extractionstep);
        return parsesInThisStep;
    }

    private Set<RRGParseTree> addExtractionStepToAllTrees(
            Set<RRGParseTree> parsesInThisStep, ExtractionStep e) {
        Set<RRGParseTree> result = new HashSet<RRGParseTree>();
        for (RRGParseTree tree : parsesInThisStep) {
            tree.addExtractionStep(e);
            result.add(tree);
        }
        return result;
    }

    private Set<RRGParseTree> extractPredictWrapping(
            Set<List<RRGParseItem>> predictWrappingAntecedents,
            ExtractionStep extractionstep) {

        Set<RRGParseTree> parsesInThisPWStep = new HashSet<RRGParseTree>();
        for (List<RRGParseItem> predictWrappingantecedentItemsingletonList : predictWrappingAntecedents) {
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.PREDICTWRAPPING);
            }
            // do the substitution and extract wrapping tree below d-daughter
            // System.out.println("got here! antecedens: "
            // + predictWrappingantecedentItemsingletonList);
            // System.out.println("step: " + extractionstep);

            RRGParseItem predictWrappingAntecedentItem = (RRGParseItem) predictWrappingantecedentItemsingletonList
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
            Set<List<RRGParseItem>> completeWrappingAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisCWStep = new HashSet<RRGParseTree>();
        for (List<RRGParseItem> CWantecedentItems : completeWrappingAntecedents) {
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.COMPLETEWRAPPING);
            }
            // System.out.println(
            // "do complete Wrapping with\n" + CWantecedentItems.toString()
            // + "\n" + extractionstep.getCurrentItem());

            RRGParseItem dDaughter = (RRGParseItem) (((RRGParseItem) CWantecedentItems
                    .get(0)).getwsflag() ? CWantecedentItems.get(0)
                            : CWantecedentItems.get(1));
            RRGParseItem gapItem = (RRGParseItem) (((RRGParseItem) CWantecedentItems
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
            Set<List<RRGParseItem>> rightAdjAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisRightAdjStep = new HashSet<RRGParseTree>();
        for (List<RRGParseItem> rightAdjAntecedentItems : rightAdjAntecedents) {
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.RIGHTADJOIN);
            }
            Set<RRGParseTree> tmpResult = new HashSet<RRGParseTree>();
            // First: find both items
            // Second: extract the right one (might cause problem when the
            // consequent item of this extraction step was the left sister in a
            // ComBSis step and was extracted there first
            RRGParseItem auxRootItem, targetItem;
            if ((((RRGParseItem) rightAdjAntecedentItems.get(0)).getNode()
                    .getType().equals(RRGNodeType.STAR))) {
                auxRootItem = ((RRGParseItem) rightAdjAntecedentItems.get(0));
                targetItem = ((RRGParseItem) rightAdjAntecedentItems.get(1));
            } else {
                auxRootItem = ((RRGParseItem) rightAdjAntecedentItems.get(1));
                targetItem = ((RRGParseItem) rightAdjAntecedentItems.get(0));
            }
            RRGParseTree nextStepParseTree = extractionstep
                    .getCurrentParseTree().sisterAdjoin(auxRootItem.getTree(),
                            extractionstep.getGAInParseTree().mother(),
                            extractionstep.getGAInParseTree().isIthDaughter()
                                    + 1);
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
        return parsesInThisRightAdjStep;
    }

    private Set<RRGParseTree> extractLeftAdjoin(
            Set<List<RRGParseItem>> leftAdjAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisLeftAdjStep = new HashSet<RRGParseTree>();
        // for all possible antecedents
        for (List<RRGParseItem> leftAdjAntecedentItems : leftAdjAntecedents) {
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.LEFTADJOIN);
            }
            // System.out.println("leftsisadj: " + leftAdjAntecedentItems);
            Set<RRGParseTree> tmpResult = new HashSet<RRGParseTree>();
            // tmpResult.add(extractionstep.getCurrentParseTree());

            RRGParseItem auxRootItem, rightSisItem;
            if ((((RRGParseItem) leftAdjAntecedentItems.get(0)).getNode()
                    .getType().equals(RRGNodeType.STAR))) {
                auxRootItem = (RRGParseItem) leftAdjAntecedentItems.get(0);
                rightSisItem = (RRGParseItem) leftAdjAntecedentItems.get(1);
            } else {
                auxRootItem = (RRGParseItem) leftAdjAntecedentItems.get(1);
                rightSisItem = (RRGParseItem) leftAdjAntecedentItems.get(0);
            }
            // first extract the right sister
            ExtractionStep nextStep = new ExtractionStep(rightSisItem,
                    extractionstep.getGAInParseTree(),
                    extractionstep.getCurrentParseTree());
            tmpResult = extract(nextStep);
            System.out.println("aux root:  " + auxRootItem);
            System.out.println("right sis: " + rightSisItem);
            // then extract the left sister
            for (RRGParseTree rrgParseTree : tmpResult) {
                int position = Math.max(
                        extractionstep.getGAInParseTree().isIthDaughter(), 0);
                RRGParseTree nextStepParseTree = rrgParseTree.sisterAdjoin(
                        auxRootItem.getTree(),
                        extractionstep.getGAInParseTree().mother(), position);
                nextStep = new ExtractionStep(auxRootItem,
                        extractionstep.getGAInParseTree().mother(),
                        nextStepParseTree);
                parsesInThisLeftAdjStep.addAll(extract(nextStep));
            }
        }
        return parsesInThisLeftAdjStep;
    }

    private Set<RRGParseTree> extractCombSis(
            Set<List<RRGParseItem>> combsisAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisCombSisStep = new HashSet<RRGParseTree>();
        // how many different antecedents are there?
        for (List<RRGParseItem> combsisantecedentItems : combsisAntecedents) {
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.COMBINESIS);
            }

            // find out which item is which
            RRGParseItem leftItem, rightItem;
            if (((RRGParseItem) combsisantecedentItems.get(0)).getNodePos()
                    .equals(RRGParseItem.NodePos.TOP)) {
                leftItem = (RRGParseItem) combsisantecedentItems.get(0);
                rightItem = (RRGParseItem) combsisantecedentItems.get(1);
            } else {
                leftItem = (RRGParseItem) combsisantecedentItems.get(1);
                rightItem = (RRGParseItem) combsisantecedentItems.get(0);
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
            // RRGParseItem combsisantecedentItem = (RRGParseItem)
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
            // boolean currentAntecedentIsRightSister = ((RRGParseItem)
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
            // (RRGParseItem) combsisantecedentItem,
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
            Set<List<RRGParseItem>> substAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisSUBSTStep = new HashSet<RRGParseTree>();
        for (List<RRGParseItem> substantecedentItemSingletonList : substAntecedents) {
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.SUBSTITUTE);
            }
            // System.out.println("Subst: " + substantecedentItemSingletonList
            // + "\n" + extractionstep);

            RRGParseItem substAntecedentItem = (RRGParseItem) substantecedentItemSingletonList
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
            Set<List<RRGParseItem>> moveupAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisMoveUpStep = new HashSet<RRGParseTree>();

        for (List<RRGParseItem> moveupAntecedentItemSingletonList : moveupAntecedents) {
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.MOVEUP);
            }
            RRGParseItem moveupAntecedentItem = (RRGParseItem) moveupAntecedentItemSingletonList
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

    private Set<RRGParseTree> extractNLS(Set<List<RRGParseItem>> nlsAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisNLSStep = new HashSet<RRGParseTree>();
        for (List<RRGParseItem> antecedentItemSingletonList : nlsAntecedents) {
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.NLS);
            }
            ExtractionStep nextStep = new ExtractionStep(
                    (RRGParseItem) antecedentItemSingletonList.get(0),
                    extractionstep.getGAInParseTree(),
                    extractionstep.getCurrentParseTree(),
                    extractionstep.getGoToRightWhenGoingDown());
            parsesInThisNLSStep.addAll(extract(nextStep));
        }

        return parsesInThisNLSStep;

    }
}
