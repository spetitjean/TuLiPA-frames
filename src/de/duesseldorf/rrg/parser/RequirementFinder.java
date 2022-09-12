package de.duesseldorf.rrg.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.duesseldorf.factorizer.EqClassBot;
import de.duesseldorf.factorizer.EqClassTop;
import de.duesseldorf.factorizer.FactorizingInterface;
import de.duesseldorf.frames.UnifyException;
import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.RRGTreeTools;
import de.duesseldorf.rrg.parser.RRGParseItem.NodePos;
import de.duesseldorf.util.GornAddress;
import de.tuebingen.tag.Environment;

/**
 * File RequirementFinder.java
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
public class RequirementFinder {

    /**
     * needed:
     * 1. in TOP position
     * 2. and not in root position
     * 3. and no right sister exists
     * 4. and ws?=no
     *
     * @param currentItem
     */
    //!!Deducer needs to compute subset of possible Mothers where 3. is fulfilled, req only checks if such a mother exists
    public boolean moveupReq(RRGParseItem currentItem) {
        boolean res = currentItem.getEqClass().isTopClass(); //1
        if(res == false) {
            return res;
        }
        res = res && !(((EqClassTop)currentItem.getEqClass()).isRoot()); // 2

        res = res && (((EqClassTop)currentItem.getEqClass()).getPossibleMothers().containsValue(true)); // 3
        res = res && !currentItem.getwsflag(); // 4
        return res;

    }

    /**
     * needed:
     * 1. BOT position
     * 2. and leftmost daughter
     *
     * @param currentItem
     * @return
     */
    public boolean nlsReq(RRGParseItem currentItem) {
        boolean res = currentItem.getEqClass().isBottomClass();// 1
        res = res && currentItem.getEqClass().noLeftSisters();// 2

        return res;
    }

    /**
     * needed:
     * - same tree
     * - neighbouring nodes, one in TOP and one in BOT pos
     * - ws no both times
     * - end of left item is start of right item
     *
     * @param leftSister
     * @param chart      look up here
     * @return
     */
    public Set<RRGParseItem> findCombineSisRightSisters(RRGParseItem leftSister,
                                                        RRGParseChart chart) {
        Set<RRGParseItem> candidates = new HashSet<RRGParseItem>();
        // find the right sister, which already ensures we are in the same tree
        RRGNode rightSis = leftSister.getTree()
                .findNode(leftSister.getNode().getGornaddress().rightSister());

        boolean leftReq = rightSis != null // there is a right sister
                && !leftSister.getwsflag() // no WS
                && leftSister.getNodePos() // the left item is in TOP position
                .equals(RRGParseItem.NodePos.TOP);

        if (leftReq) {
            // System.out.println("starter: " + leftSister);

            RRGParseItem model = new RRGParseItem.Builder()
                    .tree(leftSister.getTree().getInstance()).node(rightSis.copyNode())
                    .nodepos(NodePos.BOT).start(leftSister.getEnd()).ws(false)
                    .build();
            // System.out.println("model: " + model);
            candidates = chart.findUnderspecifiedItem(model, false);
        }
        // System.out.println("currI" + currentItem + "\nmate with: ");
        // for (RRGParseItem simpleRRGParseItem : candidates) {
        // System.out.println(simpleRRGParseItem);
        // }
        return candidates;

    }

    public Set<RRGParseItem> findCombineSisLeftSisters(RRGParseItem rightSister,
                                                       RRGParseChart chart) {
        Set<RRGParseItem> candidates = new HashSet<RRGParseItem>();
        // case 2: current item is the right node of the combination
        RRGNode leftSis = rightSister.getTree()
                .findNode(rightSister.getNode().getGornaddress().leftSister());

        boolean rightReq = leftSis != null // there is a left sister
                && rightSister.getNode().getGornaddress().hasLeftSister()
                && !rightSister.getwsflag() // no WS
                && rightSister.getNodePos() // the right item is in BOT
                // position
                .equals(RRGParseItem.NodePos.BOT);
        if (rightReq) {
            // hier liegt der Hund begraben: Die Gaps werden falsch modelliert
            RRGParseItem model = new RRGParseItem.Builder()
                    .tree(rightSister.getTree().getInstance()).node(leftSis.copyNode())
                    .nodepos(NodePos.TOP).end(rightSister.startPos()).ws(false)
                    .build();
            // System.out.println("right req met for: " + currentItem);
            // System.out.println("model: " + model);
            candidates = chart.findUnderspecifiedItem(model, false);
        }
        // System.out.println(candidates);
        return candidates;
    }

    /**
     * needed:
     * 1. TOP position in a
     * 2. root node
     * 3. root is no star node
     *
     * @param currentItem
     * @return
     */
    public boolean substituteReq(RRGParseItem currentItem) {

        boolean res = currentItem.getEqClass().isTopClass();//1
        if(res == false){return res;}

        res = res && !(((EqClassTop)currentItem.getEqClass()).isRoot()) // 2.
                && (((EqClassTop)currentItem.getEqClass()).type != RRGNodeType.STAR); //3.
        return res;
    }

    /**
     * needed:
     * 1. in TOP position
     * 2. in root node with star mark
     * 3. no ws
     *
     * @param item
     * @return {@code true} iff {@code item} is in the root of a sister
     * adjunction tree
     */
    public boolean isSisadjRoot(RRGParseItem item) {
        boolean res = item.getEqClass().isTopClass(); //1.
        if(res == false){return res;}

        res = res && ((EqClassTop) item.getEqClass()).isRoot()//2a
                && item.getEqClass().type == RRGNodeType.STAR //2b
                && !item.getwsflag();//3
        return res;
    }

    /**
     * @param sisAdjRoot is the root of a sister adjunction tree
     * @param chart
     * @return All items in the chart that the {@code sisAdjRoot} node might
     * left-sister-adjoin to.
     */
    public Set<RRGParseItem> findLeftAdjoinTargets(RRGParseItem sisAdjRoot,
                                                   RRGParseChart chart) {
        // create a template for the items that might perform leftAdjoin with
        // currentItem
        // Before Refactor
        // RRGParseItem model = new RRGParseItem(null, null,
        // RRGParseItem.NodePos.TOP, sisAdjRoot.getEnd(), -2, null,
        // false);
        RRGParseItem model = new RRGParseItem.Builder()
                .start(sisAdjRoot.getEnd()).ws(false).build();
        // find all items matching the template in the chart
        Set<RRGParseItem> candidates = chart.findUnderspecifiedItem(model,
                false);
        //Make sure items are in TOP position
        Set<RRGParseItem> candidatesTop = candidates.stream().filter(item -> item.getEqClass().isTopClass() == true).collect(Collectors.toSet());
        // System.out.println("sisadj currentItem: " + currentItem);
        // System.out.println("model: " + model);
        // filter all that have matching labels
        Set<RRGParseItem> suitableSisters = filterByMother(sisAdjRoot,
                candidatesTop);
        Set<RRGParseItem> result = new HashSet<RRGParseItem>();
        for (RRGParseItem sister : suitableSisters) {
            if (sister.getEqClass().noLeftSisters()) {
                result.add(sister);
            }
        }
        return result;

    }

    /**
     * @param sisadjroot is the root of a sister adjunction tree
     * @param chart
     * @return All items in the chart that the {@code sisAdjRoot} node might
     * right-sister-adjoin to.
     */
    public Set<RRGParseItem> findRightAdjoinTargets(RRGParseItem sisadjroot,
                                                    RRGParseChart chart) {
        // RRGParseItem model = new RRGParseItem(null, null, RRGParseItem
        // -2, sisadjroot.startPos(), null,
        // false);
        RRGParseItem model = new RRGParseItem.Builder()
                .end(sisadjroot.startPos()).ws(false).build();
        Set<RRGParseItem> candidates = chart.findUnderspecifiedItem(model,
                false);
        //Filter for TOP position
        Set<RRGParseItem> candidatesTops = candidates.stream().filter(item -> item.getEqClass().isTopClass() == true).collect(Collectors.toSet());
        return filterByMother(sisadjroot, candidatesTops);
    }

    /**
     * @param sisadjroot root node
     * @param targetCandidates poss sisters for root node by sisadj
     * @return A set of items X such that every item in X is in targetCandidates
     * and the mother of the node has the same label as the mother of
     * the (sister adjunction) root item sisadjroot.
     */
    private Set<RRGParseItem> filterByMother(RRGParseItem sisadjroot,
                                             Set<RRGParseItem> targetCandidates) {
        Set<RRGParseItem> filteredCandidates = new HashSet<RRGParseItem>();
        for (RRGParseItem candidate : targetCandidates) {
            if (suitableMother(sisadjroot, candidate))
                filteredCandidates.add(candidate);
        }
        // System.out.println("fbML called with parameters \n\troot: " +
        // sisadjroot
        // + "\n\tcandidates: " + targetCandidates + "\n\tresult: "
        // + filteredCandidates);
        return filteredCandidates;
    }

    /**
     * @param root
     * @param target
     * @return true iff there is a possible mother for the target eqClass that has the same label as the eqClass of the root item
     */
    private boolean suitableMother(RRGParseItem root, RRGParseItem target) {
        Map<EqClassBot, Boolean> possMothers = ((EqClassTop)target.getEqClass()).getPossibleMothers();
        for(EqClassBot mother: possMothers.keySet()){
            boolean nodeUnificationPossible = true;
            try {
                FactorizingInterface.unifyClasses(root.getEqClass().copyClass(), mother.copyClass(),
                        new Environment(5));
            } catch (UnifyException e) {
                nodeUnificationPossible = false;
            }
            return nodeUnificationPossible;
        }
        return false;
    }

    /**
     * @param currentItem
     * @param chart
     * @return A map with two entries. The entrie "l" maps to a set with all
     * items suited for left-adjunction, the "r" entrie contains right
     * adjoin root items.
     */
    public Map<String, Set<RRGParseItem>> findSisAdjRoots(
            RRGParseItem currentItem, RRGParseChart chart) {
        Map<String, Set<RRGParseItem>> result = new HashMap<String, Set<RRGParseItem>>();
        result.put("l", new HashSet<RRGParseItem>());
        result.put("r", new HashSet<RRGParseItem>());

        // left adjunction
        if (currentItem.getEqClass().noLeftSisters()) {
            /*
             * RRGParseItem leftAdjModel = new RRGParseItem(null, null,
             * RRGParseItem.NodePos.TOP, -2,
             * currentItem.startPos(), null,
             * false);
             */
            RRGParseItem leftAdjModel = new RRGParseItem.Builder()
                    .end(currentItem.startPos()).ws(false)
                    .build();
            Set<RRGParseItem> leftAdj = chart
                    .findUnderspecifiedItem(leftAdjModel, false);

            Set<RRGParseItem> leftAdjTops = leftAdj.stream().filter(item -> item.getEqClass().isTopClass() == true).collect(Collectors.toSet());

            for (RRGParseItem item : leftAdjTops) {
                if (isSisadjRoot(item) && suitableMother(item, currentItem)) {
                    result.get("l").add(item);
                }
            }
        }
        // right adjunction
        /*
         * RRGParseItem rightAdjModel = new RRGParseItem(null, null,
         * RRGParseItem.NodePos.TOP,
         * currentItem.getEnd(), -2, null,
         * false);
         */
        RRGParseItem rightAdjModel = new RRGParseItem.Builder()
                .start(currentItem.getEnd()).ws(false)
                .build();
        Set<RRGParseItem> rightAdj = chart.findUnderspecifiedItem(rightAdjModel,
                false);
        Set<RRGParseItem> rightAdjTops = rightAdj.stream().filter(item -> item.getEqClass().isTopClass() == true).collect(Collectors.toSet());

        for (RRGParseItem item : rightAdjTops) {
            // if the item is really a sisadjrot (specification for the
            // chart method can't be this detailled
            if (isSisadjRoot(item) && suitableMother(item, currentItem)) {
                result.get("r").add(item);
                // System.out.println("item added: " + item);
            }
        }
        return result;
    }

    /**
     * needed:<br>
     * 1. ws flag = no<br>
     * 2. TOP position<br>
     * 3. not a root node
     *
     * @param currentItem
     * @return
     */
    public boolean isSisadjTarget(RRGParseItem currentItem) {
        boolean result = !currentItem.getwsflag() // 1
                && currentItem.getEqClass().isTopClass(); // 2
        if(result == false){return result;}
        result = result
                && !(((EqClassTop) currentItem.getEqClass()).isRoot()); // 3
        return result;
    }

    /**
     * needed:<br>
     * 1. Bot position
     * 2. ws-flag true
     *
     * @param currentItem
     */
    public boolean predWrappingReqs(RRGParseItem currentItem) {
        return (currentItem.getNodePos().equals(RRGParseItem.NodePos.BOT)) // 1
                && (currentItem.getwsflag() == true); // 2
    }

    /**
     * needed:<br>
     * 1. in TOP position<br>
     * 2. of a root node <br>
     * 3. at least one gap <br>
     *
     * @param currentItem
     * @return
     */
    public boolean isCompleteWrappingRootItem(RRGParseItem currentItem) {
        return (currentItem.getNodePos().equals(RRGParseItem.NodePos.TOP)) // 1
                && currentItem.getGaps().size() > 0 // 3
                && currentItem.getNode().getGornaddress().mother() == null; // 2
    }

    /**
     * needed:<br>
     * 1. in TOP position<br>
     * 2. of a non-root node
     * 2. at least one gap <br>
     *
     * @param currentItem
     * @return
     */
    public boolean isGeneralizedCompleteWrappingTargetItem(RRGParseItem currentItem) {
        return (currentItem.getNodePos().equals(RRGParseItem.NodePos.TOP)) // 1
                && currentItem.getGaps().size() > 0 // 2
                && currentItem.getNode().getGornaddress().mother() != null;
    }

    /**
     * needed: <br>
     * 1. BOT position<br>
     * 2. ws=yes<br>
     * 3. not in a root node<br>
     *
     * @param currentItem
     * @return
     */
    public boolean isCompleteWrappingFillerItem(RRGParseItem currentItem) {
        return (currentItem.getNodePos().equals(RRGParseItem.NodePos.BOT)) // 1
                && currentItem.getwsflag() == true // 2
                && currentItem.getNode().getGornaddress().mother() != null; // 3
    }

    /**
     * needed: <br>
     * 1. BOT position<br>
     * 2. ws=yes<br>
     * 3. is a daughter of the root node<br>
     *
     * @param item
     * @return
     */
    public boolean isInternalCompleteWrappingFillerItem(RRGParseItem item) {
        return (item.getNodePos().equals(RRGParseItem.NodePos.BOT)) // 1
                && item.getwsflag() //2
                && item.getNode().getGornaddress().getAddress().size() == 1;
    }

    /**
     * Given the {@code targetRootItem}, which has a Gap {@code gap}, find all
     * items in the chart such that one might do the CompleteWrapping operation
     * with those two items.
     *
     * @param targetRootItem
     * @param gap
     * @param chart
     * @return
     */
    public Set<RRGParseItem> findCompleteWrappingFillers(RRGParseItem targetRootItem, Gap gap, RRGParseChart chart) {
        /*
         * RRGParseItem model = new RRGParseItem(null, null, NodePos.BOT,
         * gap.start,
         * gap.end, null, true);
         */
        RRGParseItem model = new RRGParseItem.Builder().nodepos(NodePos.BOT)
                .start(gap.start).end(gap.end).ws(true).build();
        Set<RRGParseItem> candidates = chart.findUnderspecifiedItem(model,
                false);
        Set<RRGParseItem> candidatesWithFittingCats = new HashSet<RRGParseItem>();
        for (RRGParseItem item : candidates) {
            boolean gapHasRightLabel = item.getNode().getCategory()
                    .equals(gap.nonterminal);
            boolean targetRootSuitsDMother = true;
            try {
                RRGTreeTools.unifyNodes(targetRootItem.getNode().copyNode(),
                        item.getTree().findNode(
                                item.getNode().getGornaddress().mother()).copyNode(),
                        new Environment(5));
            } catch (UnifyException e) {
                targetRootSuitsDMother = false;
            }
            if (gapHasRightLabel && targetRootSuitsDMother) {
                // && targetRootItem.getNode().getCategory()
                // .equals(item.getTree()
                // .findNode(item.getNode().getGornaddress().mother())
                // .getCategory())) {
                candidatesWithFittingCats.add(item);
            }
        }
        return candidatesWithFittingCats;
    }

    /**
     * Given the {@code fillerItem}, find all items in the chart that are in TOP
     * position and have a gap such that both items might perform
     * copleteWrapping.
     * <p>
     * Not tested yet! (April 25, 2018 D.)
     *
     * @param fillerItem
     * @param chart
     * @return
     */
    public Set<RRGParseItem> findCompleteWrappingRoots(RRGParseItem fillerItem,
                                                       RRGParseChart chart) {
        Gap modelgap = new Gap(fillerItem.startPos(), fillerItem.getEnd(),
                fillerItem.getNode().getCategory());
        Set<Gap> modelgaps = new HashSet<Gap>();
        modelgaps.add(modelgap);
        /*
         * RRGParseItem model = new RRGParseItem(null, null, NodePos.TOP, -2,
         * -2,
         * modelgaps, false);
         */
        RRGParseItem model = new RRGParseItem.Builder().nodepos(NodePos.TOP)
                .gaps(modelgaps).ws(false).build();
        return chart.findUnderspecifiedItem(model, true);
    }

    /**
     * in root pos (1) and TOP position (1) and has a jumpback item (3)
     *
     * @param currentItem
     * @return
     */
    public boolean isJumpBackAntecedent(RRGParseItem currentItem) {
        return currentItem.getNode().getGornaddress().mother() == null && // 1
                currentItem.getNodePos() == NodePos.TOP && // 2
                currentItem.getGenwrappingjumpback() != null; // 3
    }
}
