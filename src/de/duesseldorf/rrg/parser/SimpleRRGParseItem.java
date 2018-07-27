package de.duesseldorf.rrg.parser;

import java.util.Objects;
import java.util.Set;

import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGTree;

/*
 *  File SimpleRRGParseItem.java
 *
 *  Authors:
 *     David Arps <david.arps@hhu.de
 *     
 *  Copyright:
 *     David Arps, 2018
 *
 *  This file is part of the TuLiPA system
 *     https://github.com/spetitjean/TuLiPA-frames
 *
 *  TuLiPA is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TuLiPA is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
public class SimpleRRGParseItem implements ParseItem, Comparable<ParseItem> {

    /**
     * Do we look at everything below this node ({@code BOT}), or do we also
     * look at the left sister ({@code TOP})?
     * 
     * @author david
     *
     */
    public enum NodePos {
        TOP, BOT;
    }

    private RRGTree tree;
    private RRGNode node;
    private NodePos nodepos;
    private int start;
    private int end;
    private Set<Gap> gaps;
    private boolean ws;

    public SimpleRRGParseItem(RRGTree tree, RRGNode node, NodePos nodepos,
            int start, int end, Set<Gap> gaps, boolean ws) {
        this.tree = tree;
        this.node = node;
        this.nodepos = nodepos;
        this.start = start;
        this.end = end;
        this.gaps = gaps;
        this.ws = ws;
    }

    /**
     * 
     * 
     * @param item
     *            The item to take values from when the value given as parameter
     *            is
     * @param node
     *            {@code null}
     * @param nodepos
     *            {@code null}
     * @param start
     *            {@code -1}
     * @param end
     *            {@code -1}
     * @param gaps
     *            {@code null}
     * @param ws
     *            {@code null}
     * @param takeGapsFromItem
     *            if this is {@code true} and {@code gaps} is null, the items of
     *            the model will be the ones from {@code item}.
     *            If this is {@code false} and {@code gaps} is null, the gaps
     *            will be null as well.
     *            If this is {@code false} and {@code gaps} is not null, the
     *            gaps will be the one in the {@code gaps parameter}
     */
    public SimpleRRGParseItem(SimpleRRGParseItem item, RRGTree tree,
            RRGNode node, NodePos nodepos, int start, int end, Set<Gap> gaps,
            Boolean ws, boolean takeGapsFromItem) {

        // the optional ones
        this.tree = !(tree == null) ? tree : item.getTree();
        this.node = !(node == null) ? node : item.getNode();
        this.nodepos = !(nodepos == null) ? nodepos : item.getNodePos();
        this.start = !(start == -1) ? start : item.startPos();
        this.end = !(end == -1) ? end : item.getEnd();
        this.ws = !(ws == null) ? ws : item.getwsflag();
        if (takeGapsFromItem) {
            this.gaps = !(gaps == null) ? gaps : item.getGaps();
        } else {
            this.gaps = !(gaps == null) ? gaps : null;
        }
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

    public Set<Gap> getGaps() {
        return gaps;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tree, start, end, node, gaps, nodepos, ws);
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
        String itemstr = "[" + this.tree.getRoot() + " " + this.tree.getId()
                + ", " + this.node + ", " + this.nodepos + ", " + this.start
                + ", " + this.end + ", " + gapstr + ", " + ws + "]";

        return itemstr;
    }

    public int compareTo(ParseItem o) {
        if (this.equals(o)) {
            return 0;
        }
        if (((SimpleRRGParseItem) o).startPos() < this.startPos()) {
            return 1;
        } else {
            return -1;
        }
    }
}
