package de.duesseldorf.rrg;

import de.duesseldorf.util.GornAddress;
import de.tuebingen.tree.Node;

public class RRGParseTree extends RRGTree {

    public RRGParseTree(Node root) {
        super(root);
    }

    public RRGParseTree(RRGParseTree tree) {
        super(tree.getRoot());
    }

    public void addSubTree(GornAddress address, RRGNode subTreeRoot) {
        findNode(address).addRightmostChild(subTreeRoot);
    }
}
