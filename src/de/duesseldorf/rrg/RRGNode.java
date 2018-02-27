package de.duesseldorf.rrg;

import java.util.LinkedList;
import java.util.List;

import de.duesseldorf.util.GornAddress;
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

    private List<Node> children; // all children of the Node, in order
    private RRGNodeType type; // the type of this node
    private String name; // the name of the node
    private String category; // the cat of a node, or its terminal label
    private GornAddress gornaddress; // the gorn address

    public RRGNode(RRGNodeType type, String name, String category) {
        children = new LinkedList<Node>();
        this.type = type;
        this.name = name;
        this.setCategory(category);
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

}
