package de.duesseldorf.rrg;

import de.duesseldorf.util.GornAddress;
import de.tuebingen.tree.Node;

public class RRGParseTree extends RRGTree {

    public RRGParseTree(Node root) {
        super(root);
    }

    public RRGParseTree(RRGTree tree) {
        super(tree.getRoot());
    }

    public void addSubTree(GornAddress address, RRGNode subTreeRoot) {
        // not a good idea for things like left-sister-adjunction!
        findNode(address).addRightmostChild(subTreeRoot);
    }
}
