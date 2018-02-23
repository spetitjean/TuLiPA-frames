package de.duesseldorf.rrg;

import java.util.LinkedList;
import java.util.List;

import de.tuebingen.tree.Node;

public class RRGNode implements Node {

    public enum RRGNodeType {
        STD, // not another type
        ANCHOR, // anchor node
        LEX, // lexical node, or already anchored
        STAR, // root node of a tree used for sister-adjunction
        SUBST, // substitution leaf node
        DDAUGHTER // d-daughter for wrapping substitution, marks the d-edge
    }

    // all children of the Node, in order
    private List<Node> children;
    private RRGNodeType type;

    public RRGNode(RRGNodeType type) {
        children = new LinkedList<Node>();
        this.type = type;
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

    // not yet implemented
    public String getName() {
        return null;
    }

    // not yet implemented
    public void setName(String name) {
    }

    /**
     * @return a String representation of this node, without children
     */
    @Override
    public String toString() {
        return this.type.name();
    }
}
