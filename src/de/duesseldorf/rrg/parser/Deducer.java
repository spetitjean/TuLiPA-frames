package de.duesseldorf.rrg.parser;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.util.GornAddress;

public class Deducer {

    public SimpleRRGParseItem applyCombineSisters(SimpleRRGParseItem leftItem,
            SimpleRRGParseItem rightItem) {
        List<Gap> gaps = new LinkedList<Gap>(leftItem.getGaps());
        gaps.addAll(rightItem.getGaps());
        SimpleRRGParseItem result = new SimpleRRGParseItem(rightItem, null,
                null, SimpleRRGParseItem.NodePos.TOP, leftItem.startPos(), -1,
                gaps, false);
        return result;
    }

    public SimpleRRGParseItem applyMoveUp(SimpleRRGParseItem currentItem) {
        GornAddress motheraddress = currentItem.getNode().getGornaddress()
                .mother();
        RRGNode mothernode = currentItem.getTree().findNode(motheraddress);
        boolean newwsflag = mothernode.getType().equals(RRGNodeType.DDAUGHTER);
        Set<ParseItem> backpointers = new HashSet<ParseItem>();
        backpointers.add(currentItem);

        SimpleRRGParseItem newItem = new SimpleRRGParseItem(currentItem, null,
                mothernode, SimpleRRGParseItem.NodePos.BOT, -1, -1, null,
                newwsflag);

        // Debug
        // System.out.println(motheraddress + " is the mother of "
        // + currentItem.getNode().getGornaddress());
        return newItem;
    }

    public SimpleRRGParseItem applyNoLeftSister(
            SimpleRRGParseItem currentItem) {

        return new SimpleRRGParseItem(currentItem, null, null,
                SimpleRRGParseItem.NodePos.TOP, -1, -1, null, null);

    }

    public SimpleRRGParseItem applyLeftAdjoin(SimpleRRGParseItem targetSister,
            SimpleRRGParseItem auxTreeRoot) {

        LinkedList<Gap> gaps = new LinkedList<Gap>(auxTreeRoot.getGaps());
        gaps.addAll(targetSister.getGaps());

        SimpleRRGParseItem result = new SimpleRRGParseItem(targetSister, null,
                null, null, auxTreeRoot.startPos(), -1, gaps, false);
        return result;
    }

}
