package de.duesseldorf.rrg;

import java.util.HashMap;
import java.util.Map;

import de.duesseldorf.util.GornAddress;
import de.tuebingen.tree.Node;

public class RRGParseTree extends RRGTree {

    private Map<GornAddress, String> idMap;

    public RRGParseTree(Node root, String id) {
        super(root, id);
        this.idMap = new HashMap<GornAddress, String>();
        idMap.put(((RRGNode) root).getGornaddress(), id);
    }

    public RRGParseTree(RRGTree tree) {
        super(tree.getRoot(), tree.getId());
        this.idMap = new HashMap<GornAddress, String>();
        idMap.put(((RRGNode) tree.getRoot()).getGornaddress(), tree.getId());
    }

    /**
     * replace the id of this parse tree with
     * 
     * @param newId
     */
    public void setId(String newId) {
        this.id = newId;
    }

    /**
     * 
     * @param id
     * @return true iff this parse tree contains an elementary tree with the
     *         non-unique identifier {@code id}.
     */
    public boolean containsElementaryTree(String id) {
        return idMap.containsValue(id);
    }

    /**
     * 
     * @param tree
     * @return {@code true} iff {@code this} and {@code tree} have the same
     *         non-unique id of the elementary tree providing the root for
     *         {@code this}.
     */
    public boolean idequals(RRGTree tree) {
        return this.getId().equals(tree.getId());
    }

    public RRGParseTree replaceNodeWithSubTree(GornAddress address,
            RRGTree subtree) {
        RRGParseTree resultingTree = new RRGParseTree(this);
        resultingTree.findNode(address)
                .nodeUnification((RRGNode) subtree.getRoot());

        return resultingTree;
    }

    /**
     * Did I mess up this method, because it adds the root of the subtree as a
     * daughter?
     * 
     * @param address
     * @param subTree
     * @param position
     * @return
     */
    public RRGParseTree addSubTree(GornAddress address, RRGTree subTree,
            int position) {
        RRGParseTree resultingTree = new RRGParseTree(this);
        RRGNode motherOfSubtree = resultingTree.findNode(address);
        motherOfSubtree.addXchild(subTree.getRoot(), position);
        resultingTree.idMap.put(address, subTree.getId());
        return resultingTree;
    }
}
