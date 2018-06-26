package de.duesseldorf.rrg;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.duesseldorf.rrg.extractor.GAShiftHandler;
import de.duesseldorf.util.GornAddress;
import de.tuebingen.tree.Node;

public class RRGParseTree extends RRGTree {

    private Map<GornAddress, String> idMap;
    /**
     * A stack of the subtrees of wrapping trees from a ddaughter downwards. A
     * subtree is to be pushed on here during completeWrapping, and taken again
     * and substituted when doing predictWrapping
     */
    private List<RRGNode> wrappingSubTrees;

    private GAShiftHandler addressShiftHandler;

    public RRGParseTree(Node root, String id) {
        super(root, id);
        this.idMap = new HashMap<GornAddress, String>();
        idMap.put(((RRGNode) root).getGornaddress(), id);
        this.wrappingSubTrees = new LinkedList<RRGNode>();
    }

    public RRGParseTree(RRGParseTree tree) {
        super(tree);
        this.idMap = new HashMap<GornAddress, String>(tree.getIdMap());
        initDeepCopyOfWrappingSubtrees(tree);
    }

    public RRGParseTree(RRGTree tree) {
        super(tree);
        if (tree instanceof RRGParseTree) {
            this.idMap = new HashMap<GornAddress, String>(
                    ((RRGParseTree) tree).getIdMap());
        } else {
            this.idMap = new HashMap<GornAddress, String>();
            this.idMap.put(new GornAddress(), tree.getId());
            this.wrappingSubTrees = new LinkedList<RRGNode>();
        }
    }

    private void initDeepCopyOfWrappingSubtrees(RRGTree tree) {
        this.wrappingSubTrees = new LinkedList<RRGNode>();
        if (tree instanceof RRGParseTree) {
            for (int i = 0; i < ((RRGParseTree) tree).getWrappingSubTrees()
                    .size(); i++) {
                this.wrappingSubTrees.set(i, new RRGNode(
                        ((RRGParseTree) tree).getWrappingSubTrees().get(i)));
            }
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

    private List<RRGNode> getWrappingSubTrees() {
        return wrappingSubTrees;
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
     * TODO deal with substitution of the tree below ddaughter. Keep that
     * subtree somewhere?
     * 
     * Returns a new tree that is a (hopefully deep) copy of the one the method
     * is called on.
     * The method inserts all children of the root of wrappedTree at the
     * position where the node ddaughter used to be. The ddaughter is The
     * GornAddressees of all
     * dmother's children further right might be affected, and are updated.
     * 
     * @param wrappedTree
     * @param dmother
     * @param position
     * @return
     */
    public RRGParseTree insertWrappedTree(RRGTree wrappedTree,
            GornAddress ddaughterAddress) {
        RRGParseTree resultingTree = new RRGParseTree(this);
        GornAddress dmother = ddaughterAddress.mother();
        int position = ddaughterAddress.isIthDaughter();

        // insert the children
        RRGNode targetNode = resultingTree.findNode(dmother);
        boolean wrappingPossible = targetNode
                .nodeUnificationPossible((RRGNode) wrappedTree.getRoot());
        if (wrappingPossible) {
            // put the subtree from the ddaughter downwards somewhere safe, in
            // order to take it again when doing predictWrapping
            RRGNode wrappingSubTreeRoot = resultingTree
                    .findNode(ddaughterAddress);
            this.wrappingSubTrees.add(0, wrappingSubTreeRoot);
            resultingTree.findNode(dmother).getChildren().remove(position);

            List<Node> rootChildren = new LinkedList<Node>(
                    wrappedTree.getRoot().getChildren());
            for (int i = rootChildren.size() - 1; i >= 0; i--) {
                targetNode.addXchild(rootChildren.get(i), position);
            }
            // update GornAddress shifts
        } else {
            System.out.println(
                    "could not complete a wrapping of target tree into wrapping tree at node "
                            + dmother.toString() + "\nwrapped tree:\n"
                            + wrappedTree.toString() + "\ntarget tree:\n"
                            + this.toString());
            return resultingTree;
        }
        return resultingTree;
    }

    /**
     * TODO do I need to create a deep copy of the adjoining tree?
     * 
     * Returns a new tree that is a (hopefully deep) copy of the one the method
     * is called on. Method sister-adjoins the adjoining tree at the GA
     * targetAddress (root of adjoiningtree and targetAddress node are unified.
     * 
     * @param adjoiningTree
     *            root of that tree must have only one daughter.
     * @param targetAddress
     * @param position
     *            indicates at which position sisteradjunction happens. position
     *            = 0 means that the adjoining tree is added as leftmost sister,
     *            position = 1 that the adjoining tree has one left sister etc.
     * @return
     */
    public RRGParseTree sisterAdjoin(RRGTree adjoiningTree,
            GornAddress targetAddress, int position) {
        RRGParseTree result = new RRGParseTree(this);
        RRGNode targetNode = result.findNode(targetAddress);
        if (((RRGNode) adjoiningTree.getRoot())
                .nodeUnificationPossible(targetNode)) {
            targetNode.addXchild(adjoiningTree.getRoot().getChildren().get(0),
                    position);
        }
        return result;
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
