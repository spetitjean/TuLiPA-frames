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

    public RRGParseTree(RRGParseTree tree) {
        super(tree);
        this.idMap = new HashMap<GornAddress, String>(tree.getIdMap());
    }

    public RRGParseTree(RRGTree tree) {
        super(tree);
        if (tree instanceof RRGParseTree) {
            this.idMap = new HashMap<GornAddress, String>(
                    ((RRGParseTree) tree).getIdMap());
        } else {
            this.idMap = new HashMap<GornAddress, String>();
            this.idMap.put(new GornAddress(), tree.getId());
        }
    }

    /**
     * replace the id of this parse tree with
     * 
     * @param newId
     */
    public void setId(String newId) {
        this.id = newId;
    }

    public Map<GornAddress, String> getIdMap() {
        return idMap;
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

    public void setNode(GornAddress address, RRGNode newNode) {
        this.findNode(address.mother()).getChildren()
                .set(address.isIthDaughter(), newNode);
    }

    /**
     * replaces this trees node with gornaddress address with the root node of
     * the subtree subtree. Returns a new tree (to leave the original one
     * unchanged)
     * 
     * @param address
     * @param subtree
     * @return
     */
    public RRGParseTree replaceNodeWithSubTree(GornAddress address,
            RRGTree subtree) {
        RRGParseTree resultingTree = new RRGParseTree(this);
        System.out.println(resultingTree);
        RRGNode resTreeNode = resultingTree.findNode(address);
        RRGNode subTreeRoot = new RRGNode((RRGNode) subtree.getRoot());

        // System.out.println("whos null?");
        // System.out.println(resTreeNode);
        // System.out.println(subTreeRoot);

        boolean substPossible = resTreeNode
                .nodeUnificationPossible(subTreeRoot);
        if (substPossible) {
            // setNode(address, subTreeRoot);
            resTreeNode.setChildren(subtree.getRoot().getChildren());
        } else {
            System.out.println(
                    "substitution did not work when replacing at GA in\n" + this
                            + "\nwith subtree\n" + subtree);
        }

        System.out.println("a GA that should not exist: " + address
                .ithDaughter(1).ithDaughter(1).ithDaughter(1).ithDaughter(0));
        System.out.println("a node: " + "" + resultingTree.findNode(address
                .ithDaughter(1).ithDaughter(1).ithDaughter(1).ithDaughter(0)));
        // // System.out.println(
        // "resulting tree in replacement after: " + resultingTree);

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

    public RRGParseTree substitute(RRGTree substitutionTree,
            GornAddress address) {
        RRGParseTree result = new RRGParseTree(this);
        // can we substitute?
        RRGNode targetNode = result.findNode(address);
        if (((RRGNode) substitutionTree.getRoot())
                .nodeUnificationPossible(targetNode)) {
            result.setNode(address, (RRGNode) substitutionTree.getRoot());
        }
        // TODO add things to idmap
        return result;

    }
}
