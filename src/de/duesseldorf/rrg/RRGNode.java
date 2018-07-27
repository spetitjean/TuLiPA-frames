package de.duesseldorf.rrg;

import java.util.LinkedList;
import java.util.List;

import de.duesseldorf.util.GornAddress;
import de.tuebingen.tree.Node;

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
public class RRGNode implements Node {

    public enum RRGNodeType {
        STD, // not another type
        ANCHOR, // anchor node
        LEX, // lexical node
        STAR, // root node of a tree used for sister-adjunction
        SUBST, // substitution leaf node
        DDAUGHTER // d-daughter for wrapping substitution, marks the d-edge
    }

    private List<Node> children; // all children of the Node, in order
    private RRGNodeType type; // the type of this node
    private String name; // the name of the node
    private String category; // the cat of a node, or its terminal label
    private GornAddress gornaddress; // the gorn address

    public RRGNode(RRGNode other) {
        this.type = other.getType();
        this.name = other.name;
        this.category = other.category;

        // deep processing of children
        this.children = new LinkedList<Node>();
        for (Node child : other.getChildren()) {
            children.add(new RRGNode((RRGNode) child));
        }
        this.gornaddress = new GornAddress(other.getGornaddress());
    }

    public RRGNode(RRGNodeType type, String name, String category) {
        children = new LinkedList<Node>();
        this.type = type;
        this.name = name;
        this.setCategory(category);
        this.gornaddress = new GornAddress();
    }

    /**
     * unifies this node and the other node by replacing (!) this nodes children
     * with {@code other}s chilren, if the categories of both nodes
     * match.
     * 
     * @param other
     * @return {@code true} iff the unification succeeded
     */
    public boolean nodeUnification(RRGNode other) {
        boolean result = false;
        if (other.getCategory().equals(this.getCategory())) {
            this.setChildren(new LinkedList<Node>(other.getChildren()));
            result = true;
        }
        return result;
    }

    /**
     * returns true iff unification of this node and the other node is possible,
     * i.e. iff the categories of both nodes
     * match.
     * 
     * @param other
     * @return {@code true} iff the unification succeeded
     */
    public boolean nodeUnificationPossible(RRGNode other) {
        boolean result = false;
        if (other.getCategory().equals(this.getCategory())) {
            // this.setChildren(new LinkedList<Node>(other.getChildren()));
            result = true;
            // System.out.println(
            // children.get(1).toString() + children.get(1).getChildren());
        }
        return result;
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

    public void setCategory(String category) {
        this.category = category;
    }

    public RRGNodeType getType() {
        return this.type;
    }

    // not yet implemented
    public String getName() {
        return null;
    }

    // not yet implemented
    public void setName(String name) {
    }

    public GornAddress getGornaddress() {
        return gornaddress;
    }

    public void setGornAddress(GornAddress gornaddress) {
        this.gornaddress = gornaddress;
    }

    /**
     * @return a String representation of this node, without children
     */
    @Override
    public String toString() {
        return this.gornaddress.toString() + " " + this.category + " "
                + this.name + " (" + this.type.name() + ")";
    }

    public void setType(RRGNodeType type) {
        this.type = type;
    }

}
