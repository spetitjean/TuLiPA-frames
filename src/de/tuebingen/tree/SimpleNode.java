/*
 *  File SimpleNode.java
 *
 *  Authors:
 *     Wolfgang Maier <wo.maier@uni-tuebingen.de>
 *
 *  Copyright:
 *     Wolfgang Maier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:35:24 CEST 2007
 *
 *  This file is part of the TuLiPA system
 *     http://www.sfb441.uni-tuebingen.de/emmy-noether-kallmeyer/tulipa
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
package de.tuebingen.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a simple node in a tree.
 *
 * @author wmaier
 */
public class SimpleNode implements Node {

    private String name;
    private Node parent;
    private List<Node> children;
    private int left;
    private int right;
    private List<Node> terminals;
    private Label label;

    private boolean complement;
    private boolean head;

    public SimpleNode(String name) {
        this(name, null);
    }

    public SimpleNode(String name, Node parent) {
        this(name, parent, new ArrayList<Node>());
    }

    public SimpleNode(String name, Node parent, List<Node> children) {
        this.name = name;
        this.children = children;
        this.parent = null;
        this.terminals = new ArrayList<Node>();
        this.complement = false;
        this.head = false;
    }

    public List<Node> getTerminals() {
        return terminals;
    }

    public List<Node> getTerminals(int left, int right) {
        return terminals.subList(left, right);
    }

    public void setTerminals(List<Node> terminals) {
        this.terminals = terminals;
    }

    public void addAllTerminals(List<Node> terminals) {
        this.terminals.addAll(terminals);
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public void addChild(Node node) {
        children.add(node);
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public boolean isRoot() {
        return (getParent() == null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public boolean isComplement() {
        return complement;
    }

    public void setComplement(boolean complement) {
        this.complement = complement;
    }

    public boolean isHead() {
        return head;
    }

    public void setHead(boolean head) {
        this.head = head;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public Label getLabel() {
        return label;
    }

    public String toString() {
        String ret = name;
        if (complement) {
            ret += "-C";
        }
        //ret += "[" + getLeft() + "-" + getRight() + "]";
		/*ret += "[";
		for (int i = 0; i < this.getTerminals().size(); ++i) {
			ret += getTerminals().get(i).getName() + " ";
		}
		ret += "]";*/
        if (hasChildren()) {
            ret += " ";
            Iterator<Node> it = children.iterator();
            while (it.hasNext()) {
                Node n = it.next();
                ret += n.toString();
            }
            ret = "(" + ret + ")";
        }
        return ret;
    }

}
