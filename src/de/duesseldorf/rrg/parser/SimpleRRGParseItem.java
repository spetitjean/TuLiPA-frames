package de.duesseldorf.rrg.parser;

import java.util.List;
import java.util.Objects;

import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGTree;

/**
 * 
 * 
 * @author david
 *
 */
public class SimpleRRGParseItem implements ParseItem, Comparable<ParseItem> {

    /**
     * Do we look at everything below this node, or do we also look at the left
     * sister?
     * 
     * @author david
     *
     */
    enum NodePos {
        TOP, BOT;
    }

    private RRGTree tree;
    private RRGNode node;
    private NodePos nodepos;
    private int start;
    private int end;
    private List<Gap> gaps;
    private boolean ws;

    public SimpleRRGParseItem(RRGTree tree, RRGNode node, NodePos nodepos,
            int start, int end, List<Gap> gaps, boolean ws) {
        this.tree = tree;
        this.node = node;
        this.nodepos = nodepos;
        this.start = start;
        this.end = end;
        this.gaps = gaps;
        this.ws = ws;
    }

    /**
     * playing around with some Optional values (you need not give them). Maybe
     * its sweeter
     * 
     * @param item
     *            The item to take values from when no Optional is given, and to
     *            take the tree
     * @param node
     * @param nodepos
     * @param start
     * @param end
     * @param gaps
     * @param ws
     */
    public SimpleRRGParseItem(SimpleRRGParseItem item, RRGNode node,
            NodePos nodepos, int start, int end, List<Gap> gaps, Boolean ws) {
        // the ones that are always given
        this.tree = item.getTree();

        // the optional ones
        this.node = !(node == null) ? node : item.getNode();
        this.nodepos = !(nodepos == null) ? nodepos : item.getNodePos();
        this.start = !(start == -1) ? start : item.startPos();
        this.end = !(end == -1) ? end : item.getEnd();
        this.gaps = !(gaps == null) ? gaps : item.getGaps();
        this.ws = !(ws == null) ? ws : item.getwsflag();
    }

    public RRGTree getTree() {
        return tree;
    }

    public NodePos getNodePos() {
        return this.nodepos;
    }

    public RRGNode getNode() {
        return node;
    }

    public int startPos() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public boolean getwsflag() {
        return ws;
    }

    public List<Gap> getGaps() {
        return gaps;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tree, start, end, node, gaps, nodepos);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SimpleRRGParseItem) {
            return o.hashCode() == this.hashCode();
        }
        return false;
    }

    /**
     * 
     * @return
     */
    public String toString() {
        String gapstr = "[";
        for (Gap gap : gaps) {
            gapstr += gap.toString();
        }
        gapstr += "]";
        String itemstr = "[" + this.tree.getRoot() + ", " + this.node + ", "
                + this.nodepos + ", " + this.start + ", " + this.end + ", "
                + gapstr + ", " + ws + "]";

        return itemstr;
    }

    public int compareTo(ParseItem o) {
        int res;
        if (this.equals(o)) {
            return 0;
        }

        if (((SimpleRRGParseItem) o).startPos() < this.startPos()) {
            return -1;
        } else {
            return 1;
        }
    }
}
