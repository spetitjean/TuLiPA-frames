package de.duesseldorf.rrg;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.extractor.ExtractionStep;
import de.duesseldorf.rrg.parser.RRGParseItem;
import de.duesseldorf.util.GornAddress;
import de.tuebingen.tree.Node;

/**
 * File RRGParseTree.java
 * 
 * Authors:
 * David Arps <david.arps@hhu.de>
 * 
 * Copyright
 * David Arps, 2018
 * 
 * 
 * This file is part of the TuLiPA-frames system
 * https://github.com/spetitjean/TuLiPA-frames
 * 
 * 
 * TuLiPA is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * TuLiPA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
public class RRGParseTree extends RRGTree {

    private List<ExtractionStep> extractionsteps;
    private Map<GornAddress, String> idMap;
    /**
     * A stack of the subtrees of wrapping trees from a ddaughter downwards. A
     * subtree is to be pushed on here during completeWrapping, and taken again
     * and substituted when doing predictWrapping
     */
    private Map<RRGParseItem, RRGNode> wrappingSubTrees;

    public RRGParseTree(Node root, String id) {
        super(root, id);
        this.idMap = new HashMap<GornAddress, String>();
        idMap.put(((RRGNode) root).getGornaddress(), id);
        this.wrappingSubTrees = new HashMap<RRGParseItem, RRGNode>();
        this.extractionsteps = new LinkedList<ExtractionStep>();
    }

    public RRGParseTree(RRGParseTree tree) {
        super(tree);
        this.idMap = new HashMap<GornAddress, String>(tree.getIdMap());
        initDeepCopyOfWrappingSubtrees(tree);
        this.extractionsteps = tree.getExtractionsteps();
    }

    public RRGParseTree(RRGTree tree) {
        super(tree);
        if (tree instanceof RRGParseTree) {
            this.idMap = new HashMap<GornAddress, String>(
                    ((RRGParseTree) tree).getIdMap());
            this.extractionsteps = ((RRGParseTree) tree).getExtractionsteps();
        } else {
            this.idMap = new HashMap<GornAddress, String>();
            this.idMap.put(new GornAddress(), tree.getId());
            this.wrappingSubTrees = new HashMap<RRGParseItem, RRGNode>();
            this.extractionsteps = new LinkedList<ExtractionStep>();
        }
    }

    private void initDeepCopyOfWrappingSubtrees(RRGTree tree) {
        this.wrappingSubTrees = new HashMap<RRGParseItem, RRGNode>();
        if (tree instanceof RRGParseTree) {
            for (Entry<RRGParseItem, RRGNode> entry : ((RRGParseTree) tree)
                    .getWrappingSubTrees().entrySet()) {
                this.wrappingSubTrees.put(entry.getKey(),
                        new RRGNode(entry.getValue()));
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

    public Map<RRGParseItem, RRGNode> getWrappingSubTrees() {
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
     * @return
     */
    public RRGParseTree insertWrappedTree(RRGTree wrappedTree,
            GornAddress ddaughterAddress, RRGParseItem ddaughterItem) {
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
            resultingTree.wrappingSubTrees.put(ddaughterItem,
                    wrappingSubTreeRoot);
            resultingTree.findNode(dmother).getChildren().remove(position);

            List<Node> rootChildren = new LinkedList<Node>(
                    wrappedTree.getRoot().getChildren());
            for (int i = rootChildren.size() - 1; i >= 0; i--) {
                targetNode.addXchild(rootChildren.get(i), position);
            }
            // update GornAddress shifts
            resultingTree.idMap.put(ddaughterAddress, wrappedTree.getId());
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
     * Corresponding to the predict wrapping step. At the absolute address of
     * the ddaughter in that parseTree, add the subtree of the wrapping tree
     * below the ddaughter.
     * 
     * TODO change the wrappingsubtrees stack from Nodes to trees, so that the
     * idMap can properly work.
     * 
     * @param ddaughterAbsAddress
     * @param ddaughterItem
     * @return
     */
    public RRGParseTree addWrappingSubTree(GornAddress ddaughterAbsAddress,
            RRGParseItem ddaughterItem) {
        RRGParseTree resultingTree = new RRGParseTree(this);
        RRGNode subTreeRoot = resultingTree.wrappingSubTrees.get(ddaughterItem);

        // System.out.println("target GA: " + ddaughterAbsAddress);
        // System.out.println("in tree:\n" + resultingTree);
        // System.out.println("parseTree target node: "
        // + resultingTree.findNode(ddaughterAbsAddress));
        // System.out.println(
        // "subtree: " + RRGTreeTools.recursivelyPrintNode(subTreeRoot));
        if (resultingTree.findNode(ddaughterAbsAddress)
                .nodeUnificationPossible(subTreeRoot)) {
            // try improvement: setting the node type to STD as wrapping is
            // finished. No parse tree should have ddaughters in them now
            subTreeRoot.setType(RRGNodeType.STD);
            // old version, one liner, works but results in several ddaughters
            resultingTree.setNode(ddaughterAbsAddress, subTreeRoot);

            // when wrapping several times, the subtree we just added is
            // "forgotten" in this step.
            // resultingTree.wrappingSubTrees.remove(0);
        } else {
            System.out.println(
                    "adding a subtree at extracting predict wrapping was not possible");
        }
        // TODO see comment
        resultingTree.idMap.put(ddaughterAbsAddress, subTreeRoot.getCategory());
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
            result.idMap.put(targetAddress.ithDaughter(position),
                    adjoiningTree.getId());
        } else {
            System.out.println(
                    "node unification not possible during sister adjunction");
        }
        // for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
        // System.out.println(e.toString());
        // }
        // System.out.println(
        // "Sister adjunction at GA" + targetAddress + "pos: " + position);
        // System.out.println("in tree: " + this.toString());
        // System.out.println("resultingTree: " + result);
        // System.out.println("adjoining tree: " + adjoiningTree);

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
        } else {
            System.out.println(
                    "NU not possible on tree with id: " + this.getId());
            System.out.println("at GA " + address);
            System.out.println("target tree: " + this);
            System.out.println("subst tree: " + substitutionTree);
            System.exit(0);
        }
        result.idMap.put(address, substitutionTree.getId());
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

    @Override
    public int hashCode() {
        return super.hashCode() * Objects.hash(idMap);
    }

    @Override
    public boolean equals(Object obj) {
        if (this != null && obj != null && obj instanceof RRGParseTree) {
            return this.hashCode() == obj.hashCode();
        }
        return false;
    }

    public String idMap2string() {
        String idMap2str = "";

        for (GornAddress key : idMap.keySet()) {
            idMap2str += key + " -> " + idMap.get(key) + "\n";
        }
        return idMap2str;
    }

    @Override
    public String toString() {

        return id + idMap2string() + super.toString();
    }

    public List<ExtractionStep> getExtractionsteps() {
        return extractionsteps;
    }

    public boolean addExtractionStep(ExtractionStep e) {
        this.extractionsteps.add(0, e);
        return true;
    }

    public String extractionstepsPrinted() {
        StringBuffer sb = new StringBuffer();
        for (ExtractionStep e : extractionsteps) {
            sb.append(e);
            sb.append("\n");
        }
        return sb.toString();
    }
}
