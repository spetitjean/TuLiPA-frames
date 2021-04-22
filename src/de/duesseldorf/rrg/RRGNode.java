package de.duesseldorf.rrg;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import de.duesseldorf.frames.Fs;
import de.duesseldorf.util.GornAddress;
import de.tuebingen.tree.Node;

import de.tuebingen.tag.Environment;
import de.duesseldorf.frames.UnifyException;
import de.tuebingen.anchoring.NameFactory;

/*
 *  File RRGNode.java
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
public class RRGNode implements Node, Comparable<RRGNode> {

    private List<Node> children; // all children of the Node, in order
    private RRGNodeType type; // the type of this node
    private String name; // the name of the node
    private String category; // the cat of a node, or its terminal label
    private GornAddress gornaddress; // the gorn address
    private Fs nodeFs;

    private RRGNode(RRGNodeType type, String name, String category,
            GornAddress ga, List<Node> children, Fs nodeFs) {
        this.children = children;
        this.type = type;
        this.name = name;
        this.setCategory(category);
        this.gornaddress = ga;
        this.nodeFs = new Fs(nodeFs);
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public void addRightmostChild(Node node) {
        this.children.add(node);
    }

    /**
     * Add the node {@code node} as the {@code position}'s child of
     * {@code this}.
     * 
     * @param node
     * @param position
     */
    public void addXchild(Node node, int position) {
        this.children.add(position, node);
    }

    /**
     * 
     * @return the syntactic category of a node or its terminal label
     */
    public String getCategory() {
        return category;
    }

    private void setCategory(String category) {
        this.category = category;
    }

    public RRGNodeType getType() {
        return this.type;
    }

    // not yet implemented
    public String getName() {
        return this.name;
    }

    // not yet implemented
    public void setName(String name) {
        System.out
                .println("RRGNode.setName was called but is not implemented. ");
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            System.out.println(ste);
        }
    }

    public GornAddress getGornaddress() {
        return gornaddress;
    }

    public void setGornAddress(GornAddress gornaddress) {
        this.gornaddress = gornaddress;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gornaddress, children, type, name, category);
    }

    @Override
    public boolean equals(Object obj) {
        if (this != null && obj != null && obj instanceof RRGNode) {
            return this.hashCode() == obj.hashCode();
        }
        return false;
    }

    /**
     * returns true iff the node category and node type is the same
     * and weakEquals is true for all children of this
     * 
     * @param other
     * @return
     */
    public boolean weakEquals(RRGNode other) {
        boolean baseCase = this.getCategory().equals(other.getCategory())
                && this.getType().equals(other.getType());
        if (!baseCase) {
            // System.out.println("no basecase: " + this + " VS " + other);
            return false;
        }
        boolean fsCase = this.getNodeFs().equals(other.getNodeFs(), false);
        if (!fsCase) {
            return false;
        }
        // look at the children
        if (this.getChildren().size() != other.getChildren().size()) {
            return false;
        }
        for (int i = 0; i < this.getChildren().size(); i++) {
            RRGNode thisChild = (RRGNode) getChildren().get(i);
            RRGNode otherChild = (RRGNode) other.getChildren().get(i);
            if (!thisChild.weakEquals(otherChild)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return a String representation of this node, without children and
     *         without feature structures
     */
    public String toStringWithoutFs() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.gornaddress.toString());
        sb.append(" ");
        sb.append(this.category);
        sb.append(" ");
        sb.append(this.name);
        sb.append(" (");
        sb.append(this.type.name());
        sb.append(")");
        return sb.toString();
    }

    /**
     * @return a String representation of this node, without children
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.gornaddress.toString());
        sb.append(" ");
        sb.append(this.category);
        sb.append(" ");
        sb.append(this.name);
        sb.append(" (");
        sb.append(this.type.name());
        sb.append(")");
        if (nodeFs != null) {
            sb.append(" " + nodeFs.toStringWithOutTypeOneLiner());
        }
        return sb.toString();
    }

    public void setType(RRGNodeType type) {
        this.type = type;
    }

    public Fs getNodeFs() {
        return nodeFs;
    }

    public void setNodeFs(Fs nodeFs) {
        this.nodeFs = nodeFs;
    }

    public enum RRGNodeType {
        STD, // not another type
        ANCHOR, // anchor node
        LEX, // lexical node
        STAR, // root node of a tree used for sister-adjunction
        SUBST, // substitution leaf node
        DDAUGHTER // d-daughter for wrapping substitution, marks the d-edge
    }

    public static class Builder {
        private List<Node> children = new LinkedList<Node>();
        private RRGNodeType type;
        private String name;
        private String category;
        private GornAddress gornaddress = new GornAddress();
        private Fs nodeFs = new Fs();

        public Builder() {
        }

        public Builder(RRGNode other) {
            // deep processing of children
            this.children = new LinkedList<Node>();
            for (Node child : other.getChildren()) {
                children.add(new RRGNode.Builder((RRGNode) child).build());
            }
            type = other.getType() != null ? other.getType() : RRGNodeType.STD;
            name = other.getName() != null ? other.getName() : "";
            category = other.getCategory() != null ? other.getCategory() : "";
            gornaddress = other.getGornaddress() != null
                    ? other.getGornaddress() : new GornAddress();
            nodeFs = other.getNodeFs() != null ? other.getNodeFs() : new Fs();
        }

        public Builder children(List<Node> children) {
            this.children = children;
            return this;
        }

        public Builder type(RRGNodeType type) {
            this.type = type;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder cat(String cat) {
            this.category = cat;
            return this;
        }

        public Builder gornaddress(GornAddress ga) {
            this.gornaddress = ga;
            return this;
        }

        public Builder fs(Fs fs) {
            this.nodeFs = fs;
            return this;
        }

        public RRGNode build() {
            return new RRGNode(type, name, category, gornaddress, children,
                    nodeFs);
        }
    }

    @Override
    public int compareTo(RRGNode o) {
        int compareCats = this.category.compareTo(o.category);
        if (compareCats != 0) {
            return compareCats;
        }
        int compareTypes = this.type.toString().compareTo(o.type.toString());
        if (compareTypes != 0) {
            return compareTypes;
        }

        int childrenSizeDiff = this.children.size() - o.children.size();
        if (childrenSizeDiff != 0) {
            return childrenSizeDiff;
        }
        for (int i = 0; i < this.children.size(); i++) {
            RRGNode thisChild = (RRGNode) children.get(i);
            RRGNode otherChild = (RRGNode) o.children.get(i);
            int childDiff = thisChild.compareTo(otherChild);
            if (childDiff != 0) {
                return childDiff;
            }
        }
        return 0;
    }


    public void updateFS(Environment env, boolean finalUpdate)
	throws UnifyException {
	this.setNodeFs(Fs.updateFS(this.getNodeFs(), env, finalUpdate));
        
        // if the node has children, we update them
        if (this.getChildren() != null) {
            for (int j = 0; j < this.getChildren().size(); j++) {
                ((RRGNode) this.getChildren().get(j)).updateFS(env, finalUpdate);
            }
        }
    }

    
    public RRGNode copyNode(NameFactory nf){
    	RRGNode newNode = new RRGNode(this.getType(), this.getName(), this.getCategory(), this.getGornaddress(), new LinkedList<Node>(), new Fs(this.getNodeFs(), nf));
    	if (this.getChildren() != null){
            for (int j = 0; j < this.getChildren().size(); j++) {
                newNode.addRightmostChild(((RRGNode)this.getChildren().get(j)).copyNode(nf));
    	    }
    	}
    	return newNode;
    }
    
}
