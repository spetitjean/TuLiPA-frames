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

    public void addSubTree(GornAddress address, RRGTree subTree, int position) {
        RRGNode motherOfSubtree = findNode(address);

        motherOfSubtree.addXchild(subTree.getRoot(), position);
        idMap.put(address, subTree.getId());
    }
}
