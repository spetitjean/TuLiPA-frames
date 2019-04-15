package de.duesseldorf.rrg.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.parser.RRGParseItem.NodePos;
import de.duesseldorf.util.GornAddress;

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
    public boolean moveupReq(RRGParseItem currentItem) {
        boolean res = currentItem.getNodePos().equals(RRGParseItem.NodePos.TOP); // 1
        GornAddress currentAddress = currentItem.getNode().getGornaddress();

        res = res && (currentAddress.mother() != null); // 2
        res = res && (currentItem.getTree()
                .findNode(currentAddress.rightSister()) == null); // 3
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
        return currentItem.getNodePos().equals(RRGParseItem.NodePos.BOT) // 1
                && !currentItem.getNode().getGornaddress().hasLeftSister(); // 2
    }

    /**
     * needed:
     * - same tree
     * - neighbouring nodes, one in TOP and one in BOT pos
     * - ws no both times
     * - end of left item is start of right item
     *
     * @param leftSister
     * @param chart
     *            look up here
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
                    .tree(leftSister.getTree()).node(rightSis)
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
                    .tree(rightSister.getTree()).node(leftSis)
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
     *
     * @param currentItem
     * @return
     */
    public boolean substituteReq(RRGParseItem currentItem) {
        boolean res = currentItem.getNodePos().equals(RRGParseItem.NodePos.TOP) // 1.
                // the current node has no mother, i.e. it is a root
                && currentItem.getNode().getGornaddress().mother() == null;
        return res;
    }

    /**
     * needed:
     * 1. in root node with star mark
     * 2. in TOP position
     * 3. no ws
     *
     * @param item
     * @return {@code true} iff {@code item} is in the root of a sister
     *         adjunction tree
     */
    public boolean isSisadjRoot(RRGParseItem item) {
        boolean result = item.getNode().getGornaddress().mother() == null && // 1a
                item.getNode().getType().equals(RRGNodeType.STAR) && // 1b
                item.getNodePos().equals(RRGParseItem.NodePos.TOP) && // 2.
                !item.getwsflag(); // 3
        return result;
    }

    /**
     * @param sisAdjRoot
     *            is the root of a sister adjunction tree
     * @param chart
     * @return All items in the chart that the {@code sisAdjRoot} node might
     *         left-sister-adjoin to.
     */
    public Set<RRGParseItem> findLeftAdjoinTargets(RRGParseItem sisAdjRoot,
            RRGParseChart chart) {
        // create a template for the items that might perform leftAdjoin with
        // currentItem
        // Before Refactor
        // RRGParseItem model = new RRGParseItem(null, null,
        // RRGParseItem.NodePos.TOP, sisAdjRoot.getEnd(), -2, null,
        // false);
        RRGParseItem model = new RRGParseItem.Builder().nodepos(NodePos.TOP)
                .start(sisAdjRoot.getEnd()).ws(false).build();
        // find all items matching the template in the chart
        Set<RRGParseItem> candidates = chart.findUnderspecifiedItem(model,
                false);
        // System.out.println("sisadj currentItem: " + currentItem);
        // System.out.println("model: " + model);
        // filter all that have matching labels
        Set<RRGParseItem> suitableMother = filterByMother(sisAdjRoot,
                candidates);
        Set<RRGParseItem> result = new HashSet<RRGParseItem>();
        for (RRGParseItem item : suitableMother) {
            if (!item.getNode().getGornaddress().hasLeftSister()) {
                result.add(item);
            }
        }
        return result;

    }

    /**
     * @param sisadjroot
     *            is the root of a sister adjunction tree
     * @param chart
     * @return All items in the chart that the {@code sisAdjRoot} node might
     *         right-sister-adjoin to.
     */
    public Set<RRGParseItem> findRightAdjoinTargets(RRGParseItem sisadjroot,
            RRGParseChart chart) {
        // RRGParseItem model = new RRGParseItem(null, null, RRGParseItem
        // .NodePos.TOP,
        // -2, sisadjroot.startPos(), null,
        // false);
        RRGParseItem model = new RRGParseItem.Builder().nodepos(NodePos.TOP)
                .end(sisadjroot.startPos()).ws(false).build();
        Set<RRGParseItem> candidates = chart.findUnderspecifiedItem(model,
                false);
        return filterByMother(sisadjroot, candidates);
    }

    /**
     * @param sisadjroot
     * @param targetCandidates
     * @return A set of items X such that every item in X is in targetCandidates
     *         and the mother of the node has the same label as the mother of
     *         the (sister adjunction) root item sisadjroot.
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
     * @return true iff the node in root has the same label as the mother of the
     *         node in target
     */
    private boolean suitableMother(RRGParseItem root, RRGParseItem target) {
        RRGNode targetMother = target.getTree()
                .findNode(target.getNode().getGornaddress().mother());
        if (targetMother != null) {
            if (root.getNode().nodeUnificationPossible(targetMother)) {
                // String candidateMotherLabel = targetMother.getCategory();
                // if
                // (root.getNode().getCategory().equals(candidateMotherLabel)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param currentItem
     * @param chart
     * @return A map with two entries. The entrie "l" maps to a set with all
     *         items suited for left-adjunction, the "r" entrie contains right
     *         adjoin root items.
     */
    public Map<String, Set<RRGParseItem>> findSisAdjRoots(
            RRGParseItem currentItem, RRGParseChart chart) {
        Map<String, Set<RRGParseItem>> result = new HashMap<String, Set<RRGParseItem>>();
        result.put("l", new HashSet<RRGParseItem>());
        result.put("r", new HashSet<RRGParseItem>());

        // left adjunction
        if (!currentItem.getNode().getGornaddress().hasLeftSister()) {
            /*
             * RRGParseItem leftAdjModel = new RRGParseItem(null, null,
             * RRGParseItem.NodePos.TOP, -2,
             * currentItem.startPos(), null,
             * false);
             */
            RRGParseItem leftAdjModel = new RRGParseItem.Builder()
                    .nodepos(NodePos.TOP).end(currentItem.startPos()).ws(false)
                    .build();
            Set<RRGParseItem> leftAdj = chart
                    .findUnderspecifiedItem(leftAdjModel, false);

            for (RRGParseItem item : leftAdj) {
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
                .nodepos(NodePos.TOP).start(currentItem.getEnd()).ws(false)
                .build();
        Set<RRGParseItem> rightAdj = chart.findUnderspecifiedItem(rightAdjModel,
                false);
        for (RRGParseItem item : rightAdj) {
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
                && currentItem.getNodePos().equals(RRGParseItem.NodePos.TOP) // 2
                && currentItem.getNode().getGornaddress().mother() != null; // 3
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
                && currentItem.getNode().getGornaddress().mother() == null // 2
                && currentItem.getGaps().size() > 0; // 3
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
     * Given the {@code targetRootItem}, which has a Gap {@code gap}, find all
     * items in the chart such that one might do the CompleteWrapping operation
     * with those two items.
     *
     * @param targetRootItem
     * @param gap
     * @param chart
     * @return
     */
    public Set<RRGParseItem> findCompleteWrappingFillers(
            RRGParseItem targetRootItem, Gap gap, RRGParseChart chart) {
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
            boolean targetRootSuitsDMother = targetRootItem.getNode()
                    .nodeUnificationPossible(item.getTree().findNode(
                            item.getNode().getGornaddress().mother()));
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
}