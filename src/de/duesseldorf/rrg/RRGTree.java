package de.duesseldorf.rrg;

import de.tuebingen.tree.Node;

public class RRGTree {

    // representation of the syntactic tree
    private Node root;

    public RRGTree(Node root) {
        this.root = root;
    }

    @Override
    public String toString() {
        return RRGTreeTools.recursivelyPrintNode(root);
    }
}
