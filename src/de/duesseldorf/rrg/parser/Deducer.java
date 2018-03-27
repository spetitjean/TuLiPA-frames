package de.duesseldorf.rrg.parser;

import java.util.HashSet;
import java.util.Set;

import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.util.GornAddress;

public class Deducer {

    public SimpleRRGParseItem applyMoveUp(SimpleRRGParseItem currentItem) {
        GornAddress motheraddress = currentItem.getNode().getGornaddress()
                .mother();
        RRGNode mothernode = currentItem.getTree().findNode(motheraddress);
        boolean newwsflag = mothernode.getType().equals(RRGNodeType.DDAUGHTER);
        Set<ParseItem> backpointers = new HashSet<ParseItem>();
        backpointers.add(currentItem);

        SimpleRRGParseItem newItem = new SimpleRRGParseItem(currentItem,
                mothernode, SimpleRRGParseItem.NodePos.BOT, null, null, null,
                newwsflag, backpointers);

        // Debug
        System.out.println(motheraddress + " is the mother of "
                + currentItem.getNode().getGornaddress());
        return newItem;
    }

}
