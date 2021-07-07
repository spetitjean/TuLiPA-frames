package de.duesseldorf.rrg.extractor;

import de.duesseldorf.frames.UnifyException;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.RRGParseResult;
import de.duesseldorf.rrg.RRGParseTree;
import de.duesseldorf.rrg.RRGTree;
import de.duesseldorf.rrg.parser.Backpointer;
import de.duesseldorf.rrg.parser.Operation;
import de.duesseldorf.rrg.parser.RRGParseChart;
import de.duesseldorf.rrg.parser.RRGParseItem;
import de.duesseldorf.util.GornAddress;
import de.tuebingen.util.TextUtilities;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * File NewParseForestExtractor.java
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
public class ParseForestExtractor {

    private RRGParseChart parseChart;
    private Set<RRGParseTree> resultingParses;

    public boolean verbosePrintsToStdOut = false;

    private Set<Operation> sameElemTree = new HashSet<Operation>(Arrays
            .asList(Operation.COMBINESIS, Operation.MOVEUP, Operation.NLS));
    private List<String> toksentence;

    public ParseForestExtractor(RRGParseChart parseChart,
                                List<String> toksentence) {
        this.parseChart = parseChart;
        this.toksentence = toksentence;
        this.resultingParses = new ConcurrentSkipListSet<RRGParseTree>();
    }

    public RRGParseResult extractParseTrees() {
        // find goal items in the chart. Extract them all and add the set of
        // parse trees derived from them to the resulting parses

        Set<RRGParseItem> goals = parseChart.retrieveGoalItems();
        //if (verbosePrintsToStdOut) {
        System.out.println("goal items: " + goals);
        //}
        goals.stream().forEach((goal) -> {
            ExtractionStep initExtrStep = initialExtractionStep(
                    (RRGParseItem) goal);
            Set<RRGParseTree> resultingTrees = extract(initExtrStep);
            addToResultingParses(resultingTrees);
        });
        return ParseForestPostProcessor
                .postProcessParseTreeSet(resultingParses);
    }

    /**
     * this method takes a
     *
     * @param goal item and creates the initial extraction step from that item
     */
    private ExtractionStep initialExtractionStep(RRGParseItem goal) {
        ExtractionStep result = new ExtractionStep(goal, new GornAddress(),
                new RRGParseTree(goal.getTreeInstance()), 0);
        return result;
    }

    private synchronized void addToResultingParses(
            Set<RRGParseTree> resultingTrees) {
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
        // Set<RRGParseTree> parsesInThisStep = new ConcurrentSkipListSet<RRGParseTree>();
        Set<RRGParseTree> parsesInThisStep = new HashSet<RRGParseTree>();
        if (verbosePrintsToStdOut) {
            System.out.println(extractionstep);
        }

        // distinguish different operations here
        // NLS
        parsesInThisStep.addAll(extractNLS(
                backPointers.getAntecedents(Operation.NLS), extractionstep));

        // Move-Up
        Set<Set<RRGParseItem>> moveupAntecedents = backPointers
                .getAntecedents(Operation.MOVEUP);
        parsesInThisStep
                .addAll(extractMoveUp(moveupAntecedents, extractionstep));

        // Combine-Sisters
        Set<Set<RRGParseItem>> combsisAntecedents = backPointers
                .getAntecedents(Operation.COMBINESIS);
        parsesInThisStep
                .addAll(extractCombSis(combsisAntecedents, extractionstep));

        // Substitution
        Set<Set<RRGParseItem>> substAntecedents = backPointers
                .getAntecedents(Operation.SUBSTITUTE);
        parsesInThisStep.addAll(extractSubst(substAntecedents, extractionstep));

        // Left-Adjoin
        Set<Set<RRGParseItem>> leftAdjAntecedents = backPointers
                .getAntecedents(Operation.LEFTADJOIN);
        parsesInThisStep
                .addAll(extractLeftAdjoin(leftAdjAntecedents, extractionstep));

        // Right-Adjoin
        Set<Set<RRGParseItem>> rightAdjAntecedents = backPointers
                .getAntecedents(Operation.RIGHTADJOIN);
        parsesInThisStep.addAll(
                extractRightAdjoin(rightAdjAntecedents, extractionstep));

        // Complete-Wrapping
        Set<Set<RRGParseItem>> coWrAntecedents = backPointers
                .getAntecedents(Operation.COMPLETEWRAPPING);
        parsesInThisStep.addAll(
                extractCompleteWrapping(coWrAntecedents, extractionstep));

        // Predict-Wrapping

        Set<Set<RRGParseItem>> prWrAntecedents = backPointers
                .getAntecedents(Operation.PREDICTWRAPPING);
        boolean wrappingException = false;
        try {
            parsesInThisStep.addAll(
                    extractPredictWrapping(prWrAntecedents, extractionstep));
        } catch (WrappingException e) {
            wrappingException = true;
        }

        // Generalized Wrapping
        Set<Set<RRGParseItem>> jumpbackAntecedents = backPointers.getAntecedents(Operation.GENCWJUMPBACK);
        parsesInThisStep.addAll(extractJumpBack(jumpbackAntecedents, extractionstep));

        Set<Set<RRGParseItem>> gencwAntecedents = backPointers.getAntecedents(Operation.GENCW);
        parsesInThisStep.addAll(extractGenCompleteWrapping(gencwAntecedents, extractionstep));
        // if no other rule applied (i.e. if we dealt with a scanned item):
        boolean noBackPointersBecauseLexNode = extractionstep.getCurrentItem()
                .getNode().getType().equals(RRGNodeType.LEX);
        if (parsesInThisStep.isEmpty() && !wrappingException
                && noBackPointersBecauseLexNode) {
            parsesInThisStep.add(extractionstep.getCurrentParseTree());
        }
        // System.out.println("---------------------------------------------");
        // System.out.println(parsesInThisStep.size());
        // System.out.println("---------------------------------------------");
        // System.out.println(extractionstep);
        // for (RRGParseTree parseInThisStep : parsesInThisStep) {
        // System.out.println(parseInThisStep);
        // }
        // System.out.println("---------------------------------------------");

        // Set<RRGParseTree> parsesInThisStepWithCurrentExtractionStep =
        // addExtractionStepToAllTrees(
        // parsesInThisStep, extractionstep);

        // System.out.println("\nParses in this step:");
        // System.out.println(parsesInThisStep);

        return parsesInThisStep;
    }

    private Set<RRGParseTree> extractJumpBack(Set<Set<RRGParseItem>> jumpbackAntecedents, ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisStep = new HashSet<>();
        for (Set<RRGParseItem> itemset : jumpbackAntecedents) {
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.GENCWJUMPBACK);
            }
            RRGParseTree currentParseTree = extractionstep.getCurrentParseTree();
            RRGParseItem jumpbackAntecedent = itemset.iterator().next();
            RRGTree wrappingTree = jumpbackAntecedent.getTree();
            /**int ddaughterIndex = -1;
             for (int i = 0; i < wrappingTree.getRoot().getChildren().size(); i++) {
             RRGNode ithDaughter = (RRGNode) wrappingTree.getRoot().getChildren().get(i);
             if (ithDaughter.getType().equals(RRGNodeType.DDAUGHTER)) {
             break;
             }
             }*/
            GornAddress currentGA = extractionstep.getGAInParseTree();
            RRGParseTree nextStepParseTree = currentParseTree.insertWrappingTree(wrappingTree, currentGA, jumpbackAntecedent.getGenwrappingjumpback());
            ExtractionStep nextStep = new ExtractionStep(jumpbackAntecedent, currentGA, nextStepParseTree, extractionstep.getGoToRightWhenGoingDown());
            parsesInThisStep.addAll(extract(nextStep));
            // first extract all arms to the right of the ddaguther
            // then jumpback
            // then extract left armas
            // before: debug a very simple general wrapping example (on the master branch!) to see how it should work. Bonus: Nested wrapping extraction
        }
        return parsesInThisStep;
    }

    private Set<RRGParseTree> extractPredictWrapping(
            Set<Set<RRGParseItem>> predictWrappingAntecedents,
            ExtractionStep extractionstep) throws WrappingException {

        Set<RRGParseTree> parsesInThisPWStep = new HashSet<RRGParseTree>();
        for (Set<RRGParseItem> predictWrappingantecedentItemsingletonList : predictWrappingAntecedents) {
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.PREDICTWRAPPING);
            }
            // do the substitution and extract wrapping tree below d-daughter
            // System.out.println("got here! antecedens: "
            // + predictWrappingantecedentItemsingletonList);
            // System.out.println("step: " + extractionstep);


            RRGParseItem predictWrappingAntecedentItem = (RRGParseItem) predictWrappingantecedentItemsingletonList
                    .iterator().next();
            GornAddress ddaughterAbsAddress = new GornAddress(
                    extractionstep.getGAInParseTree());

            // insert the subtree stored in the RRGParseTree at the correct GA
            // (seems to work)
            // continue extraction from there with same GA	    
            RRGParseTree nextStepParseTree = extractionstep
                    .getCurrentParseTree().addWrappingSubTree(
                            ddaughterAbsAddress, predictWrappingAntecedentItem);
            if (nextStepParseTree != null) {
                ExtractionStep nextStep = new ExtractionStep(
                        predictWrappingAntecedentItem, ddaughterAbsAddress,
                        nextStepParseTree,
                        extractionstep.getGoToRightWhenGoingDown());
                parsesInThisPWStep.addAll(extract(nextStep));
            }
        }
        return parsesInThisPWStep;
    }

    private Set<RRGParseTree> extractGenCompleteWrapping(Set<Set<RRGParseItem>> gencwAntecedents, ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisStep = new HashSet<>();
        for (Set<RRGParseItem> backpointerset : gencwAntecedents) {
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.GENCW);
            }

            RRGParseItem ddaughterItem = backpointerset.stream().filter(item -> item.getwsflag()).findFirst().orElseGet(null);
            RRGParseItem wraprootItem = backpointerset.stream().filter(item -> !item.getwsflag()).findFirst().orElseGet(null);


            RRGParseTree currentParseTree = extractionstep.getCurrentParseTree();
            GornAddress currentGA = extractionstep.getGAInParseTree();
            RRGParseTree nextStepParseTree = currentParseTree.insertWrappedTreeForGeneralizedWrapping(wraprootItem, currentGA, ddaughterItem);

            GornAddress newGA = extractionstep.getGAInParseTree().mother();
            ExtractionStep nextStep = new ExtractionStep(wraprootItem,
                    newGA, nextStepParseTree,
                    extractionstep.getGAInParseTree().isIthDaughter());
            parsesInThisStep.addAll(extract(nextStep));
        }
        return parsesInThisStep;
    }

    private Set<RRGParseTree> extractCompleteWrapping(
            Set<Set<RRGParseItem>> completeWrappingAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisCWStep = new HashSet<RRGParseTree>();
        for (Set<RRGParseItem> CWantecedentItems : completeWrappingAntecedents) {
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.COMPLETEWRAPPING);
            }
            // System.out.println(
            // "do complete Wrapping with\n" + CWantecedentItems.toString()
            // + "\n" + extractionstep.getCurrentItem());

            RRGParseItem dDaughter, gapItem;
            Iterator<RRGParseItem> it = CWantecedentItems.iterator();
            RRGParseItem candidate1 = it.next();
            if (candidate1.getwsflag()) {
                dDaughter = candidate1;
                gapItem = it.next();
            } else {
                dDaughter = it.next();
                gapItem = candidate1;
            }

            // extract the d-daughter in the predict-wrapping step

            // insert the wrapped tree into the parse tree
            // System.out.println(
            // "before wrapping: " + extractionstep.getCurrentParseTree());
            // System.out.println("insert that tree: " + gapItem.getTree());
            // System.out.println("at: " + extractionstep.getGAInParseTree());
            RRGParseTree nextStepParseTree = extractionstep
                    .getCurrentParseTree().insertWrappedTree(gapItem.getTreeInstance(),
                            extractionstep.getGAInParseTree(), dDaughter, false);
            // System.out.println("after wrapping: " + nextStepParseTree);

            // adjust Gorn Addresses here.
            GornAddress shiftedGAInParseTree = extractionstep.getGAInParseTree()
                    .mother();
            // System.out.println("extract from address (root of wrapped tree):
            // "
            // + shiftedGAInParseTree);
            ExtractionStep nextStep = new ExtractionStep(gapItem,
                    shiftedGAInParseTree, nextStepParseTree,
                    extractionstep.getGAInParseTree().isIthDaughter()
                    /* + extractionstep.getGoToRightWhenGoingDown() */);
            parsesInThisCWStep.addAll(extract(nextStep));
        }
        return parsesInThisCWStep;
    }

    private Set<RRGParseTree> extractRightAdjoin(
            Set<Set<RRGParseItem>> rightAdjAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisRightAdjStep = new HashSet<RRGParseTree>();
        for (Set<RRGParseItem> rightAdjAntecedentItems : rightAdjAntecedents) {
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.RIGHTADJOIN);
            }
            Set<RRGParseTree> tmpResult = new HashSet<RRGParseTree>();
            // First: find both items
            // Second: extract the right one (might cause problem when the
            // consequent item of this extraction step was the left sister in a
            // ComBSis step and was extracted there first
            RRGParseItem auxRootItem, targetItem;
            Iterator<RRGParseItem> it = rightAdjAntecedentItems.iterator();
            RRGParseItem candidate1 = it.next();
            if (candidate1.getNode().getType().equals(RRGNodeType.STAR)) {
                auxRootItem = candidate1;
                targetItem = it.next();
            } else {
                auxRootItem = it.next();
                targetItem = candidate1;
            }
            RRGParseTree nextStepParseTree = null;
            try {
                nextStepParseTree = extractionstep
                        .getCurrentParseTree().sisterAdjoin(auxRootItem.getTreeInstance(),
                                extractionstep.getGAInParseTree().mother(),
                                extractionstep.getGAInParseTree().isIthDaughter()
                                        + extractionstep.getGoToRightWhenGoingDown()
                                        + 1);
            } catch (UnifyException e) {
                continue;
            }
            // extract aux tree:
            ExtractionStep nextStep = new ExtractionStep(auxRootItem,
                    extractionstep.getGAInParseTree().mother(),
                    nextStepParseTree,
                    extractionstep.goToRightWhenGoingDown
                            + extractionstep.getGAInParseTree().isIthDaughter()
                            + 1);

            tmpResult = extract(nextStep);
            // System.out.println("tmpresult size: " + tmpResult.size());
            // extract target:
            for (RRGParseTree rrgParseTree : tmpResult) {
                nextStep = new ExtractionStep(targetItem,
                        extractionstep.getGAInParseTree(), rrgParseTree,
                        extractionstep.getGoToRightWhenGoingDown());
                parsesInThisRightAdjStep.addAll(extract(nextStep));
            }
            // System.out.println("RIGHTADJOIN");
            // System.out.println("extractionstep: " + extractionstep);
            // System.out.println("auxroot: " + auxRootItem);
            // System.out.println("targetitem: " + targetItem);
            // System.out.println("nextStep: " + nextStep);
            // System.out.println("tmpResult: " + tmpResult);
        }
        // System.out.println("\nparses in this right adj step:");
        // System.out.println(parsesInThisRightAdjStep);
        return parsesInThisRightAdjStep;
    }

    private Set<RRGParseTree> extractLeftAdjoin(
            Set<Set<RRGParseItem>> leftAdjAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisLeftAdjStep = new HashSet<RRGParseTree>();
        // for all possible antecedents
        for (Set<RRGParseItem> leftAdjAntecedentItems : leftAdjAntecedents) {
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.LEFTADJOIN);
            }
            // System.out.println("leftsisadj: " + leftAdjAntecedentItems);
            Set<RRGParseTree> tmpResult = new HashSet<RRGParseTree>();
            // tmpResult.add(extractionstep.getCurrentParseTree());

            RRGParseItem auxRootItem, rightSisItem;
            Iterator<RRGParseItem> it = leftAdjAntecedentItems.iterator();
            RRGParseItem candidate1 = it.next();
            if (candidate1.getNode().getType().equals(RRGNodeType.STAR)) {
                auxRootItem = candidate1;
                rightSisItem = it.next();
            } else {
                auxRootItem = it.next();
                rightSisItem = candidate1;
            }
            // first extract the right sister
            ExtractionStep nextStep = new ExtractionStep(rightSisItem,
                    extractionstep.getGAInParseTree(),
                    extractionstep.getCurrentParseTree(),
                    extractionstep.getGoToRightWhenGoingDown());
            tmpResult = extract(nextStep);
            // System.out.println("aux root: " + auxRootItem);
            // System.out.println("right sis: " + rightSisItem);
            // then extract the left sister
            for (RRGParseTree rrgParseTree : tmpResult) {
                int position = Math
                        .max(extractionstep.getGAInParseTree().isIthDaughter()
                                /* extractionstep.getGoToRightWhenGoingDown() */, 0);

                RRGParseTree nextStepParseTree = null;
                try {
                    nextStepParseTree = rrgParseTree.sisterAdjoin(
                            auxRootItem.getTreeInstance(),
                            extractionstep.getGAInParseTree().mother(), position);
                } catch (UnifyException e) {
                    continue;
                }
                nextStep = new ExtractionStep(auxRootItem,
                        extractionstep.getGAInParseTree().mother(),
                        nextStepParseTree, position);
                parsesInThisLeftAdjStep.addAll(extract(nextStep));
            }
            // System.out.println("LEFTADJOIN");
            // System.out.println("extractionstep: " + extractionstep);
            // System.out.println("auxroot: " + auxRootItem);
            // System.out.println("targetitem: " + rightSisItem);
            // System.out.println("nextStep: " + nextStep);
            // System.out.println("tmpResult: " + tmpResult);
        }
        return parsesInThisLeftAdjStep;
    }

    private Set<RRGParseTree> extractCombSis(
            Set<Set<RRGParseItem>> combsisAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisCombSisStep = new HashSet<RRGParseTree>();
        // how many different antecedents are there?
        for (Set<RRGParseItem> combsisantecedentItems : combsisAntecedents) {
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.COMBINESIS);
            }

            // find out which item is which
            RRGParseItem leftItem, rightItem;
            Iterator<RRGParseItem> it = combsisantecedentItems.iterator();
            RRGParseItem candidate1 = it.next();
            if (candidate1.getNodePos().equals(RRGParseItem.NodePos.TOP)) {
                leftItem = candidate1;
                rightItem = it.next();
            } else {
                leftItem = it.next();
                rightItem = candidate1;
            }

            ExtractionStep nextStep = new ExtractionStep(rightItem,
                    extractionstep.getGAInParseTree(),
                    extractionstep.getCurrentParseTree(),
                    extractionstep.getGoToRightWhenGoingDown());
            Set<RRGParseTree> tmpResult = extract(nextStep);
            for (RRGParseTree rrgParseTree : tmpResult) {
                nextStep = new ExtractionStep(leftItem,
                        extractionstep.getGAInParseTree().leftSister(),
                        rrgParseTree,
                        extractionstep.getGoToRightWhenGoingDown());
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
            Set<Set<RRGParseItem>> substAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisSUBSTStep = new HashSet<RRGParseTree>();
        for (Set<RRGParseItem> substantecedentItemSingletonList : substAntecedents) {
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.SUBSTITUTE);
            }
            // System.out.println("Subst: " + substantecedentItemSingletonList
            // + "\n" + extractionstep);

            RRGParseItem substAntecedentItem = (RRGParseItem) substantecedentItemSingletonList
                    .iterator().next();
            GornAddress GAtoReplaceAt = extractionstep.getGAInParseTree();
            //RRGTree substTree = substAntecedentItem.getTree();
            RRGTree substTree = substAntecedentItem.getTreeInstance();
            // System.out.println("try to subst this tree: " + substTree);
            // System.out.println(
            // "into that tree: " + extractionstep.getCurrentParseTree());
            // System.out.println("at GA " + GAtoReplaceAt);
            RRGParseTree nextStepParseTree = extractionstep
                    .getCurrentParseTree().substitute(substTree, GAtoReplaceAt);
            // System.out.println("result: " + nextStepParseTree);
            ExtractionStep nextStep = new ExtractionStep(substAntecedentItem,
                    extractionstep.getGAInParseTree(), nextStepParseTree, 0);
            parsesInThisSUBSTStep.addAll(extract(nextStep));
        }
        return parsesInThisSUBSTStep;
    }

    private Set<RRGParseTree> extractMoveUp(
            Set<Set<RRGParseItem>> moveupAntecedents,
            ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisMoveUpStep = new HashSet<RRGParseTree>();

        for (Set<RRGParseItem> moveupAntecedentItemSingletonList : moveupAntecedents) {
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.MOVEUP);
            }
            RRGParseItem moveupAntecedentItem = (RRGParseItem) moveupAntecedentItemSingletonList
                    .iterator().next();
            GornAddress newMoveUpGA = extractionstep.getGAInParseTree()
                    .ithDaughter(moveupAntecedentItem.getNode().getGornaddress()
                            .isIthDaughter()
                            + extractionstep.getGoToRightWhenGoingDown());
            ExtractionStep nextStep = new ExtractionStep(moveupAntecedentItem,
                    newMoveUpGA, extractionstep.getCurrentParseTree(), 0);
            parsesInThisMoveUpStep.addAll(extract(nextStep));
        }
        return parsesInThisMoveUpStep;
    }

    private Set<RRGParseTree> extractNLS(Set<Set<RRGParseItem>> nlsAntecedents,
                                         ExtractionStep extractionstep) {
        Set<RRGParseTree> parsesInThisNLSStep = new HashSet<RRGParseTree>();
        for (Set<RRGParseItem> antecedentItemSingletonList : nlsAntecedents) {
            if (verbosePrintsToStdOut) {
                System.out.println(Operation.NLS);
            }
            ExtractionStep nextStep = new ExtractionStep(
                    (RRGParseItem) antecedentItemSingletonList.iterator()
                            .next(),
                    extractionstep.getGAInParseTree(),
                    extractionstep.getCurrentParseTree(),
                    extractionstep.getGoToRightWhenGoingDown());
            parsesInThisNLSStep.addAll(extract(nextStep));
        }

        return parsesInThisNLSStep;

    }
}
