package de.duesseldorf.rrg.parser;

import de.duesseldorf.util.GornAddress;

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
}
