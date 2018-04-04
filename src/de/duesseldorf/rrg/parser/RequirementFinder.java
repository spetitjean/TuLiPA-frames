package de.duesseldorf.rrg.parser;

import java.util.HashSet;
import java.util.Set;

import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.util.GornAddress;

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
     * @param currentItem
     * @param chart
     *            look up here
     * @return
     */
    public Set<SimpleRRGParseItem> combinesisReq(SimpleRRGParseItem currentItem,
            SimpleRRGParseChart chart) {
        Set<SimpleRRGParseItem> candidates = new HashSet<SimpleRRGParseItem>();

        // case 1: currentItem is the left node of the combination
        // find the right sister, which already ensures we are in the same tree
        RRGNode rightSis = currentItem.getTree()
                .findNode(currentItem.getNode().getGornaddress().rightSister());

        boolean leftReq = rightSis != null // there is a right sister
                && !currentItem.getwsflag() // no WS
                && currentItem.getNodePos() // the left item is in TOP position
                        .equals(SimpleRRGParseItem.NodePos.TOP);
        if (leftReq) {
            // System.out.println("starter: " + currentItem);
            SimpleRRGParseItem model = new SimpleRRGParseItem(currentItem,
                    currentItem.getTree(), rightSis,
                    SimpleRRGParseItem.NodePos.BOT, currentItem.getEnd(), -2,
                    null, false);
            // System.out.println("model: " + model);
            candidates = chart.findUnderspecifiedItem(model);
        } else {
            // case 2: current item is the right node of the combination
            RRGNode leftSis = currentItem.getTree().findNode(
                    currentItem.getNode().getGornaddress().leftSister());

            boolean rightReq = leftSis != null // there is a left sister
                    && currentItem.getNode().getGornaddress().hasLeftSister()
                    && !currentItem.getwsflag() // no WS
                    && currentItem.getNodePos() // the right item is in BOT
                                                // position
                            .equals(SimpleRRGParseItem.NodePos.BOT);
            if (rightReq) {
                SimpleRRGParseItem model = new SimpleRRGParseItem(currentItem,
                        null, leftSis, SimpleRRGParseItem.NodePos.TOP, -2,
                        currentItem.startPos(), null, false);
                // System.out.println("right req met for: " + currentItem);
                // System.out.println("model: " + model);
                candidates = chart.findUnderspecifiedItem(model);
            }
        }
        // System.out.println("currI" + currentItem + "\nmate with: ");
        // for (SimpleRRGParseItem simpleRRGParseItem : candidates) {
        // System.out.println(simpleRRGParseItem);
        // }
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
     * @param currentItem
     * @return
     */
    public boolean sisadjRoot(SimpleRRGParseItem currentItem) {
        boolean result = currentItem.getNode().getGornaddress().mother() == null
                && // 1a
                currentItem.getNode().getType().equals(RRGNodeType.STAR) && // 1b
                currentItem.getNodePos().equals(SimpleRRGParseItem.NodePos.TOP)
                && // 2.
                !currentItem.getwsflag(); // 3
        return result;
    }

    /**
     * 
     * @param currentItem
     *            is the root of a sister adjunction tree
     * @param chart
     * @return
     */
    public Set<SimpleRRGParseItem> leftAdjoinAntecedents(
            SimpleRRGParseItem currentItem, SimpleRRGParseChart chart) {
        // create a template for the items that might perform leftAdjoin with
        // currentItem
        SimpleRRGParseItem model = new SimpleRRGParseItem(null, null,
                SimpleRRGParseItem.NodePos.TOP, currentItem.getEnd(), -2, null,
                false);
        // find all items matching the template in the chart
        Set<SimpleRRGParseItem> candidates = chart
                .findUnderspecifiedItem(model);
        // System.out.println("sisadj currentItem: " + currentItem);
        // System.out.println("model: " + model);

        return filterByMotherLabel(currentItem, candidates);
    }

    public Set<SimpleRRGParseItem> rightAdjoinAntecedents(
            SimpleRRGParseItem sisadjroot, SimpleRRGParseChart chart) {
        SimpleRRGParseItem model = new SimpleRRGParseItem(null, null,
                SimpleRRGParseItem.NodePos.TOP, -2, sisadjroot.startPos(), null,
                false);
        Set<SimpleRRGParseItem> candidates = chart
                .findUnderspecifiedItem(model);
        // System.out.println("BLA" + candidates.size());

        // filter all that have matching labels

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
        String rootItemLabel = sisadjroot.getNode().getCategory();
        for (SimpleRRGParseItem candidate : targetCandidates) {
            if (!candidate.getNode().getGornaddress().hasLeftSister()) {
                RRGNode candidateMother = candidate.getTree().findNode(
                        candidate.getNode().getGornaddress().mother());
                if (candidateMother != null) {
                    String candidateMotherLabel = candidateMother.getCategory();
                    if (rootItemLabel.equals(candidateMotherLabel)) {
                        filteredCandidates.add(candidate);
                    }
                }
            }
        }
        return filteredCandidates;
    }
}
