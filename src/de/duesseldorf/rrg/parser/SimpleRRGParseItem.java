package de.duesseldorf.rrg.parser;

import java.util.List;
import java.util.Objects;
import java.util.Set;

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
    private Set<ParseItem> backpointers;
    private boolean ws;

    public SimpleRRGParseItem(RRGTree tree, RRGNode node, NodePos nodepos,
            int start, int end, List<Gap> gaps, boolean ws,
            Set<ParseItem> backpointers) {
        this.tree = tree;
        this.node = node;
        this.nodepos = nodepos;
        this.start = start;
        this.end = end;
        this.gaps = gaps;
        this.ws = ws;
        this.backpointers = backpointers;
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
     * @param backpointers
     *            Backpointers must be given, as they always change
     */
    public SimpleRRGParseItem(SimpleRRGParseItem item, RRGNode node,
            NodePos nodepos, Integer start, Integer end, List<Gap> gaps,
            Boolean ws, Set<ParseItem> backpointers) {
        // the ones that are always given
        this.tree = item.getTree();
        this.backpointers = backpointers;

        // the optional ones
        this.node = !(node == null) ? node : item.getNode();
        this.nodepos = !(nodepos == null) ? nodepos : item.getNodePos();
        this.start = !(start == null) ? start : item.startPos();
        this.end = !(end == null) ? end : item.getEnd();
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

    public Set<ParseItem> getBackpointers() {
        return backpointers;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tree, start, end, node, gaps, backpointers);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SimpleRRGParseItem) {
            return o.hashCode() == this.hashCode();
        }
        return false;
    }

    /**
     * Verbose toString: Select if you want to include the backpointers
     * associated to the item in the string
     * 
     * @param includeBackPointers
     * @return
     */
    public String toString(boolean includeBackPointers) {
        String gapstr = "[";
        for (Gap gap : gaps) {
            gapstr += gap.toString();
        }
        gapstr += "]";
        String itemstr = "[" + this.tree.getRoot() + ", " + this.node + ", "
                + this.nodepos + ", " + this.start + ", " + this.end + ", "
                + gapstr + ", " + ws + "]";
        if (includeBackPointers) {
            itemstr += " : Here come the BPs!";
        }
        return itemstr;
    }

    /**
     * Default toString: return a representation of the item without
     * backpointers (shorter)
     */
    @Override
    public String toString() {
        return toString(false);
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
