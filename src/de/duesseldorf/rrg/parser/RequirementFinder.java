package de.duesseldorf.rrg.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.parser.SimpleRRGParseItem.NodePos;
import de.duesseldorf.util.GornAddress;

/**
 * File RequirementFinder.java
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
public class RequirementFinder {

    /**
     * needed:
     * 1. in TOP position
     * 2. and not in root position
     * 3. and no right sister exists
     * 4. and ws?=no
     * 
     * 
     * @param currentItem
     */
    public boolean moveupReq(SimpleRRGParseItem currentItem) {
        boolean res = currentItem.getNodePos()
                .equals(SimpleRRGParseItem.NodePos.TOP); // 1
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
    public boolean nlsReq(SimpleRRGParseItem currentItem) {
        return currentItem.getNodePos().equals(SimpleRRGParseItem.NodePos.BOT) // 1
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
    public Set<SimpleRRGParseItem> findCombineSisRightSisters(
            SimpleRRGParseItem leftSister, SimpleRRGParseChart chart) {
        Set<SimpleRRGParseItem> candidates = new HashSet<SimpleRRGParseItem>();
        // find the right sister, which already ensures we are in the same tree
        RRGNode rightSis = leftSister.getTree()
                .findNode(leftSister.getNode().getGornaddress().rightSister());

        boolean leftReq = rightSis != null // there is a right sister
                && !leftSister.getwsflag() // no WS
                && leftSister.getNodePos() // the left item is in TOP position
                        .equals(SimpleRRGParseItem.NodePos.TOP);

        if (leftReq) {
            // System.out.println("starter: " + currentItem);
            SimpleRRGParseItem model = new SimpleRRGParseItem(leftSister,
                    leftSister.getTree(), rightSis,
                    SimpleRRGParseItem.NodePos.BOT, leftSister.getEnd(), -2,
                    null, false, false);
            // System.out.println("model: " + model);
            candidates = chart.findUnderspecifiedItem(model, false);
        }

        // System.out.println("currI" + currentItem + "\nmate with: ");
        // for (SimpleRRGParseItem simpleRRGParseItem : candidates) {
        // System.out.println(simpleRRGParseItem);
        // }
        return candidates;

    }

    public Set<SimpleRRGParseItem> findCombineSisLeftSisters(
            SimpleRRGParseItem rightSister, SimpleRRGParseChart chart) {
        Set<SimpleRRGParseItem> candidates = new HashSet<SimpleRRGParseItem>();
        // case 2: current item is the right node of the combination
        RRGNode leftSis = rightSister.getTree()
                .findNode(rightSister.getNode().getGornaddress().leftSister());

        boolean rightReq = leftSis != null // there is a left sister
                && rightSister.getNode().getGornaddress().hasLeftSister()
                && !rightSister.getwsflag() // no WS
                && rightSister.getNodePos() // the right item is in BOT
                                            // position
                        .equals(SimpleRRGParseItem.NodePos.BOT);
        if (rightReq) {
            // hier liegt der Hund begraben: Die Gaps werden falsch modelliert
            SimpleRRGParseItem model = new SimpleRRGParseItem(rightSister, null,
                    leftSis, SimpleRRGParseItem.NodePos.TOP, -2,
                    rightSister.startPos(), null, false, false);
            // System.out.println("right req met for: " + currentItem);
            // System.out.println("model: " + model);
            candidates = chart.findUnderspecifiedItem(model, false);
        }
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
    public boolean substituteReq(SimpleRRGParseItem currentItem) {
        boolean res = currentItem.getNodePos()
                .equals(SimpleRRGParseItem.NodePos.TOP) // 1.
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
    public boolean isSisadjRoot(SimpleRRGParseItem item) {
        boolean result = item.getNode().getGornaddress().mother() == null && // 1a
                item.getNode().getType().equals(RRGNodeType.STAR) && // 1b
                item.getNodePos().equals(SimpleRRGParseItem.NodePos.TOP) && // 2.
                !item.getwsflag(); // 3
        return result;
    }

    /**
     * 
     * @param sisAdjRoot
     *            is the root of a sister adjunction tree
     * @param chart
     * @return All items in the chart that the {@code sisAdjRoot} node might
     *         left-sister-adjoin to.
     */
    public Set<SimpleRRGParseItem> findLeftAdjoinTargets(
            SimpleRRGParseItem sisAdjRoot, SimpleRRGParseChart chart) {
        // create a template for the items that might perform leftAdjoin with
        // currentItem
        SimpleRRGParseItem model = new SimpleRRGParseItem(null, null,
                SimpleRRGParseItem.NodePos.TOP, sisAdjRoot.getEnd(), -2, null,
                false);
        // find all items matching the template in the chart
        Set<SimpleRRGParseItem> candidates = chart.findUnderspecifiedItem(model,
                false);
        // System.out.println("sisadj currentItem: " + currentItem);
        // System.out.println("model: " + model);
        // filter all that have matching labels
        Set<SimpleRRGParseItem> sameMotherLabel = filterByMotherLabel(
                sisAdjRoot, candidates);
        Set<SimpleRRGParseItem> result = new HashSet<SimpleRRGParseItem>();
        for (SimpleRRGParseItem item : sameMotherLabel) {
            if (!item.getNode().getGornaddress().hasLeftSister()) {
                result.add(item);
            }
        }
        return result;

    }

    /**
     * 
     * @param sisAdjRoot
     *            is the root of a sister adjunction tree
     * @param chart
     * @return All items in the chart that the {@code sisAdjRoot} node might
     *         right-sister-adjoin to.
     */
    public Set<SimpleRRGParseItem> findRightAdjoinTargets(
            SimpleRRGParseItem sisadjroot, SimpleRRGParseChart chart) {
        SimpleRRGParseItem model = new SimpleRRGParseItem(null, null,
                SimpleRRGParseItem.NodePos.TOP, -2, sisadjroot.startPos(), null,
                false);
        Set<SimpleRRGParseItem> candidates = chart.findUnderspecifiedItem(model,
                false);

        return filterByMotherLabel(sisadjroot, candidates);
    }

    /**
     * 
     * @param sisadjroot
     * @param targetCandidates
     * @return A set of items X such that every item in X is in targetCandidates
     *         and the mother of the node has the same label as the mother of
     *         the (sister adjunction) root item sisadjroot.
     */
    private Set<SimpleRRGParseItem> filterByMotherLabel(
            SimpleRRGParseItem sisadjroot,
            Set<SimpleRRGParseItem> targetCandidates) {
        Set<SimpleRRGParseItem> filteredCandidates = new HashSet<SimpleRRGParseItem>();
        for (SimpleRRGParseItem candidate : targetCandidates) {
            if (sameMotherLabel(sisadjroot, candidate))
                filteredCandidates.add(candidate);
        }
        // System.out.println("fbML called with parameters \n\troot: " +
        // sisadjroot
        // + "\n\tcandidates: " + targetCandidates + "\n\tresult: "
        // + filteredCandidates);
        return filteredCandidates;
    }

    /**
     * 
     * @param root
     * @param target
     * @return true iff the node in root has the same label as the mother of the
     *         node in target
     */
    private boolean sameMotherLabel(SimpleRRGParseItem root,
            SimpleRRGParseItem target) {
        RRGNode targetMother = target.getTree()
                .findNode(target.getNode().getGornaddress().mother());
        if (targetMother != null) {
            String candidateMotherLabel = targetMother.getCategory();
            if (root.getNode().getCategory().equals(candidateMotherLabel)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 
     * @param currentItem
     * @param chart
     * @return A map with two entries. The entrie "l" maps to a set with all
     *         items suited for left-adjunction, the "r" entrie contains right
     *         adjoin root items.
     */
    public Map<String, Set<SimpleRRGParseItem>> findSisAdjRoots(
            SimpleRRGParseItem currentItem, SimpleRRGParseChart chart) {
        Map<String, Set<SimpleRRGParseItem>> result = new HashMap<String, Set<SimpleRRGParseItem>>();
        result.put("l", new HashSet<SimpleRRGParseItem>());
        result.put("r", new HashSet<SimpleRRGParseItem>());

        // left adjunction
        if (!currentItem.getNode().getGornaddress().hasLeftSister()) {
            SimpleRRGParseItem leftAdjModel = new SimpleRRGParseItem(null, null,
                    SimpleRRGParseItem.NodePos.TOP, -2, currentItem.startPos(),
                    null, false);
            Set<SimpleRRGParseItem> leftAdj = chart
                    .findUnderspecifiedItem(leftAdjModel, false);

            for (SimpleRRGParseItem item : leftAdj) {
                if (isSisadjRoot(item) && sameMotherLabel(item, currentItem)) {
                    result.get("l").add(item);
                }
            }
        }
        // right adjunction
        SimpleRRGParseItem rightAdjModel = new SimpleRRGParseItem(null, null,
                SimpleRRGParseItem.NodePos.TOP, currentItem.getEnd(), -2, null,
                false);
        Set<SimpleRRGParseItem> rightAdj = chart
                .findUnderspecifiedItem(rightAdjModel, false);
        for (SimpleRRGParseItem item : rightAdj) {
            // if the item is really a sisadjrot (specification for the
            // chart method can't be this detailled
            if (isSisadjRoot(item) && sameMotherLabel(item, currentItem)) {
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
    public boolean isSisadjTarget(SimpleRRGParseItem currentItem) {
        boolean result = !currentItem.getwsflag() // 1
                && currentItem.getNodePos()
                        .equals(SimpleRRGParseItem.NodePos.TOP) // 2
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
    public boolean predWrappingReqs(SimpleRRGParseItem currentItem) {
        return (currentItem.getNodePos().equals(SimpleRRGParseItem.NodePos.BOT)) // 1
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
    public boolean isCompleteWrappingRootItem(SimpleRRGParseItem currentItem) {
        return (currentItem.getNodePos().equals(SimpleRRGParseItem.NodePos.TOP)) // 1
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
    public boolean isCompleteWrappingFillerItem(
            SimpleRRGParseItem currentItem) {
        return (currentItem.getNodePos().equals(SimpleRRGParseItem.NodePos.BOT)) // 1
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
    public Set<SimpleRRGParseItem> findCompleteWrappingFillers(
            SimpleRRGParseItem targetRootItem, Gap gap,
            SimpleRRGParseChart chart) {
        SimpleRRGParseItem model = new SimpleRRGParseItem(null, null,
                NodePos.BOT, gap.start, gap.end, null, true);
        Set<SimpleRRGParseItem> candidates = chart.findUnderspecifiedItem(model,
                false);
        Set<SimpleRRGParseItem> candidatesWithFittingCats = new HashSet<SimpleRRGParseItem>();
        for (SimpleRRGParseItem item : candidates) {
            if (item.getNode().getCategory().equals(gap.nonterminal)
                    && targetRootItem.getNode().getCategory()
                            .equals(item
                                    .getTree().findNode(item.getNode()
                                            .getGornaddress().mother())
                                    .getCategory())) {
                candidatesWithFittingCats.add(item);
            }
        }
        return candidatesWithFittingCats;
    }

    /**
     * Given the {@code fillerItem}, find all items in the chart that are in TOP
     * position and have a gap such that both items might perform
     * copleteWrapping.
     * 
     * Not tested yet! (April 25, 2018 D.)
     * 
     * @param fillerItem
     * @param chart
     * @return
     */
    public Set<SimpleRRGParseItem> findCompleteWrappingRoots(
            SimpleRRGParseItem fillerItem, SimpleRRGParseChart chart) {
        Gap modelgap = new Gap(fillerItem.startPos(), fillerItem.getEnd(),
                fillerItem.getNode().getCategory());
        Set<Gap> modelgaps = new HashSet<Gap>();
        modelgaps.add(modelgap);
        SimpleRRGParseItem model = new SimpleRRGParseItem(null, null,
                NodePos.TOP, -2, -2, modelgaps, false);
        return chart.findUnderspecifiedItem(model, true);
    }
}