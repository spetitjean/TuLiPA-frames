package de.duesseldorf.rrg;

import de.duesseldorf.util.GornAddress;
import de.tuebingen.tree.Node;

public class RRGParseTree extends RRGTree {

    public RRGParseTree(Node root, String id) {
        super(root, id);
    }

    public RRGParseTree(RRGTree tree) {
        super(tree.getRoot(), tree.getId());
    }

    /**
     * 
     * @param tree
     * @return {@code true} iff {@code this} and {@code tree} have the same
     *         non-unique id
     */
    public boolean idequals(RRGTree tree) {
        return this.getId().equals(tree.getId());
    }

    public void addSubTree(GornAddress address, RRGNode subTreeRoot) {
        // not a good idea for things like left-sister-adjunction!
        findNode(address).addRightmostChild(subTreeRoot);
    }
}
