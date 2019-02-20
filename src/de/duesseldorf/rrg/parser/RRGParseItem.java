
package de.duesseldorf.rrg.parser;

import java.util.Objects;
import java.util.Set;

import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGTree;

/*
 *  File RRGParseItem.java
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
public class RRGParseItem implements Comparable<RRGParseItem> {

    private final RRGTree tree;
    private final RRGNode node;
    private final NodePos nodepos;
    private final int start;
    private final int end;
    private final Set<Gap> gaps;
    private final boolean ws;

    private RRGParseItem(RRGTree tree, RRGNode node, NodePos nodepos, int start,
            int end, Set<Gap> gaps, boolean ws) {
        this.tree = tree;
        this.node = node;
        this.nodepos = nodepos;
        this.start = start;
        this.end = end;
        this.gaps = gaps;
        this.ws = ws;
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
        if (o instanceof RRGParseItem) {
            return o.hashCode() == this.hashCode();
        }
        return false;
    }

    /**
     * @return
     */
    public String toString() {
        String gapstr = "[";
        for (Gap gap : gaps) {
            gapstr += gap.toString();
        }
        gapstr += "]";
        String itemstr = "[" + this.tree.getId() + ", " + this.node + ", "
                + this.nodepos + ", " + this.start + ", " + this.end + ", "
                + gapstr + ", " + ws + "]";

        return itemstr;
    }

    public int compareTo(RRGParseItem o) {
        if (this.equals(o)) {
            return 0;
        }
        if (o.startPos() < this.startPos()) {
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * Do we look at everything below this node ({@code BOT}), or do we also
     * look at the left sister ({@code TOP})?
     *
     * @author david
     */
    public enum NodePos {
        TOP, BOT;
    }

    public static class Builder {
        // all of the properties are initialized with their default
        // (underspecified) values).
        private RRGTree tree = null;
        private RRGNode node = null;
        private NodePos nodepos = null;
        private int start = -2;
        private int end = -2;
        private Set<Gap> gaps = null;
        private Boolean ws;

        public Builder() {
        }

        public Builder tree(RRGTree tree) {
            this.tree = tree;
            return this;
        }

        public Builder node(RRGNode node) {
            this.node = node;
            return this;
        }

        public Builder nodepos(NodePos nodepos) {
            this.nodepos = nodepos;
            return this;
        }

        public Builder start(int start) {
            this.start = start;
            return this;
        }

        public Builder end(int end) {
            this.end = end;
            return this;
        }

        public Builder gaps(Set<Gap> gaps) {
            this.gaps = gaps;
            return this;
        }

        public Builder ws(boolean ws) {
            this.ws = ws;
            return this;
        }

        public RRGParseItem build() {
            /*
             * System.out.println();
             * System.out.println();
             * // System.out.println(tree.toString());
             * System.out.println();
             * System.out.println();
             */
            return new RRGParseItem(tree, node, nodepos, start, end, gaps, ws);
        }
    }
}
