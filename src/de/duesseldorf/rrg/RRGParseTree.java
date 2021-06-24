package de.duesseldorf.rrg;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import de.duesseldorf.frames.UnifyException;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.extractor.ExtractionStep;
import de.duesseldorf.rrg.parser.RRGParseItem;
import de.duesseldorf.util.GornAddress;
import de.tuebingen.tag.Environment;
import de.tuebingen.tree.Node;

/**
 * File RRGParseTree.java
 * <p>
 * Authors:
 * David Arps <david.arps@hhu.de>
 * <p>
 * Copyright
 * David Arps, 2018
 * <p>
 * <p>
 * This file is part of the TuLiPA-frames system
 * https://github.com/spetitjean/TuLiPA-frames
 * <p>
 * <p>
 * TuLiPA is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * TuLiPA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
public class RRGParseTree extends RRGTree {

    private List<ExtractionStep> extractionsteps;
    private List<String> ids;
    /**
     * A stack of the subtrees of wrapping trees from a ddaughter downwards. A
     * subtree is to be pushed on here during completeWrapping, and taken again
     * and substituted when doing predictWrapping
     */
    private Map<RRGParseItem, RRGNode> wrappingSubTrees;

    public RRGParseTree(Node root, String id) {
        super(root, id);
        this.ids = new LinkedList<String>();
        ids.add("ROOT::" + id);
        this.wrappingSubTrees = new HashMap<RRGParseItem, RRGNode>();
        this.extractionsteps = new LinkedList<ExtractionStep>();
    }

    public RRGParseTree(RRGParseTree tree) {
        super(tree);
        this.ids = new LinkedList<String>(tree.getIds());
        initDeepCopyOfWrappingSubtrees(tree);
        this.extractionsteps = tree.getExtractionsteps();
    }

    public RRGParseTree(RRGTree tree) {
        super(tree);
        if (tree instanceof RRGParseTree) {
            this.ids = new LinkedList<String>(((RRGParseTree) tree).getIds());
            this.extractionsteps = ((RRGParseTree) tree).getExtractionsteps();
        } else {
            this.ids = new LinkedList<String>();
            this.ids.add("ROOT::" + tree.getId());
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
                        new RRGNode.Builder(entry.getValue()).build());
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

    public List<String> getIds() {
        return ids;
    }

    /**
     * @param tree
     * @return {@code true} iff {@code this} and {@code tree} have the same
     * non-unique id of the elementary tree providing the root for
     * {@code this}.
     */
    public boolean idequals(RRGTree tree) {
        return this.getId().equals(tree.getId());
    }

    public void setNode(GornAddress address, RRGNode newNode) {
        if (address.mother() == null) {
            root = newNode;
        } else {
            this.findNode(address.mother()).getChildren()
                    .set(address.isIthDaughter(), newNode);
        }
    }

    public RRGParseTree insertWrappingTree(RRGTree wrappingTree, GornAddress targetAddress, RRGParseItem jumpBackitem) {
        RRGParseTree resultingTree = new RRGParseTree(this);

        RRGNode targetNode = resultingTree.findNode(targetAddress);
        boolean wrappingPossible = true;
        RRGNode newTargetNode = null;
        try {
            newTargetNode = RRGTreeTools.unifyNodes((RRGNode) wrappingTree.getRoot(), targetNode, getEnv());
        } catch (UnifyException e) {
            wrappingPossible = false;
        }
        if (wrappingPossible) {
            resultingTree.setNode(targetAddress, newTargetNode);
            resultingTree.wrappingSubTrees.put(jumpBackitem, targetNode);
        }
        resultingTree.getFrameSem().addOtherFrame(wrappingTree.getFrameSem());
        return resultingTree;
    }

    public RRGParseTree insertWrappedTreeForGeneralizedWrapping(RRGParseItem wrapRootItem, GornAddress ddaughterAddress, RRGParseItem ddaughterItem) {

        RRGNode wrappedNode = wrappingSubTrees.get(wrapRootItem);

        // what to do when wrappedNode is null?
        if (wrappedNode == null) {
            return this;
        }
        RRGTree artificialWrappedTree = new RRGParseTree(wrappedNode, ddaughterItem.getTree().getId());
        return insertWrappedTree(artificialWrappedTree, ddaughterAddress, ddaughterItem, true);
    }

    /**
     * TODO deal with substitution of the tree below ddaughter. Keep that
     * subtree somewhere?
     * <p>
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
                                          GornAddress ddaughterAddress, RRGParseItem ddaughterItem, boolean internalWrapping) {
        RRGParseTree resultingTree = new RRGParseTree(this);
        GornAddress dmother = ddaughterAddress.mother();
        int position = ddaughterAddress.isIthDaughter();

        // insert the children
        RRGNode targetNode = resultingTree.findNode(dmother);
        boolean wrappingPossible = true;
        RRGNode newTargetNode = null;
        try {
            newTargetNode = RRGTreeTools.unifyNodes(targetNode,
                    (RRGNode) wrappedTree.getRoot(), getEnv());
        } catch (UnifyException | NullPointerException e) {
            wrappingPossible = false;
        }
        if (wrappingPossible) {

            resultingTree.setNode(dmother, newTargetNode);
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
                newTargetNode.addXchild(rootChildren.get(i), position);
            }
            if (internalWrapping) {
                resultingTree.ids.add("INTERNAL_WRAPPING::" + wrappedTree.getId());
            } else {
                resultingTree.ids.add("WRAPPING::" + wrappedTree.getId());
            }
            try {
                resultingTree.setEnv(Environment.merge(resultingTree.getEnv(),
                        wrappedTree.getEnv()));
            } catch (UnifyException e) {
                System.out.println(
                        "ERROR: Not able to merge environments during wrapping");
            }
        } else {
            System.err.println(
                    "could not complete a wrapping of target tree into wrapping tree at node "
                            + dmother.toString() + "\nwrapped tree:\n"
                            + wrappedTree.toString() + "\ntarget tree:\n"
                            + this.toString());
            return resultingTree;
        }
        //TODO why is it this.getFrameSem() and not resultingTree.getFrameSem()?
        this.getFrameSem().addOtherFrame(wrappedTree.getFrameSem());
        return resultingTree;
    }

    /**
     * Corresponding to the predict wrapping step. At the absolute address of
     * the ddaughter in that parseTree, add the subtree of the wrapping tree
     * below the ddaughter.
     *
     * @param ddaughterAbsAddress
     * @param ddaughterItem
     * @return null if no fitting subtree available
     */
    public RRGParseTree addWrappingSubTree(GornAddress ddaughterAbsAddress,
                                           RRGParseItem ddaughterItem) {
        RRGParseTree resultingTree = new RRGParseTree(this);
        RRGNode subTreeRoot = resultingTree.wrappingSubTrees.get(ddaughterItem);

        // System.out.println("target GA: " + ddaughterAbsAddress);
        // System.out.println("in tree:\n" + resultingTree);
        // System.out.println("parseTree target node: " + resultingTree.findNode(ddaughterAbsAddress));
        // System.out.println(
        // "subtree: " + RRGTreeTools.recursivelyPrintNode(subTreeRoot));
        RRGNode ddaughter = resultingTree.findNode(ddaughterAbsAddress);
        if (subTreeRoot != null) {
            try {
                RRGNode newSubTreeRoot = RRGTreeTools.unifyNodes(subTreeRoot,
                        ddaughter, getEnv());

                // try improvement: setting the node type to STD as wrapping is
                // finished. No parse tree should have ddaughters in them now
                subTreeRoot.setType(RRGNodeType.STD);

                resultingTree.setNode(ddaughterAbsAddress, newSubTreeRoot);

                // when wrapping several times, the subtree we just added is
                // "forgotten" in this step.
                // resultingTree.wrappingSubTrees.remove(0);
                // resultingTree.ids.add("PREDWRAPPING::" +
                // subTreeRoot.getCategory());
            } catch (UnifyException | NullPointerException e) {
                System.out.println("adding a subtree at extracting predict wrapping was not possible");
                return null;
            }
        } else {
            return null;
        }
        return resultingTree;
    }

    /**
     * TODO do I need to create a deep copy of the adjoining tree?
     * <p>
     * Returns a new tree that is a (hopefully deep) copy of the one the method
     * is called on. Method sister-adjoins the adjoining tree at the GA
     * targetAddress (root of adjoiningtree and targetAddress node are unified.
     *
     * @param adjoiningTree root of that tree must have only one daughter.
     * @param targetAddress
     * @param position      indicates at which position sisteradjunction happens. position
     *                      = 0 means that the adjoining tree is added as leftmost sister,
     *                      position = 1 that the adjoining tree has one left sister etc.
     * @return
     */
    public RRGParseTree sisterAdjoin(RRGTree adjoiningTree,
                                     GornAddress targetAddress, int position) {
        // System.out.println(
        // "Sister adjunction at GA" + targetAddress + "pos: " + position);
        // System.out.println("in tree: " + this.toString());
        // System.out.println("adjoining tree: " + adjoiningTree);
        RRGParseTree result = new RRGParseTree(this);
        RRGNode targetNode = result.findNode(targetAddress);
        try {
            // unify root of aux tree and target node
            RRGNode newTargetNode = RRGTreeTools.unifyNodes(targetNode,
                    (RRGNode) adjoiningTree.getRoot(), getEnv());
            // // put new target node in resulting tree
            // System.out.println(newTargetNode + " newtarget");
            // System.out.println("result tree: " + result.toString());
            // System.out.println("targetAddress: " + targetAddress);,env
            // Da kann irgendwas nicht stimmen. Nochmal überdenken, was wo hin
            // muss, bevor sich allees verknotet.
            result.setNode(targetAddress, newTargetNode);
            newTargetNode.addXchild(
                    adjoiningTree.getRoot().getChildren().get(0), position);
            result.ids.add("SISTERADJOIN::" + adjoiningTree.getId());
            try {
                result.setEnv(
                        Environment.merge(getEnv(), adjoiningTree.getEnv()));
            } catch (UnifyException e) {
                System.out.println(
                        "ERROR: Not able to merge environments during sister adjunction");
            }
        } catch (UnifyException e) {
            System.out.println(
                    "node unification not possible during sister adjunction");
        }
        // for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
        // System.out.println(e.toString());
        // }

        // System.out.println("resultingTree: " + result);
        this.getFrameSem().addOtherFrame(adjoiningTree.getFrameSem());
        return result;
    }

    public RRGParseTree substitute(RRGTree substitutionTree,
                                   GornAddress address) {
        RRGParseTree result = new RRGParseTree(this);
        // can we substitute?
        // System.out.println("address: " + address);
        // System.out.println("this tr: " + this.toString());
        RRGNode targetNode = result.findNode(address);
        // System.out.println("targetNode: " + targetNode);
        try {
            RRGNode substNodeWithFs = RRGTreeTools.unifyNodes(
                    (RRGNode) substitutionTree.getRoot(), targetNode, getEnv());
            result.setNode(address, substNodeWithFs);
            result.ids.add("SUBST::" + substitutionTree.getId());
            try {
                result.setEnv(
                        Environment.merge(getEnv(), substitutionTree.getEnv()));
            } catch (UnifyException | NullPointerException e) {
                System.out.println(
                        "ERROR: Not able to merge environments during substitution");
            }
        } catch (UnifyException | NullPointerException e) {
            System.err.println(
                    "node unification not possible on tree with id: " + this.getId());
            System.err.println("at GA " + address);
            System.err.println("target tree: " + this);
            System.err.println("subst tree: " + substitutionTree);
            //System.exit(0);
        }
        this.getFrameSem().addOtherFrame(substitutionTree.getFrameSem());
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
        resultingTree.ids.add(subTree.getId());
        return resultingTree;
    }

    @Override
    public int hashCode() {
        return super.hashCode() * Objects.hash(ids);
    }

    @Override
    public boolean equals(Object obj) {
        if (this != null && obj != null && obj instanceof RRGParseTree) {
            return this.hashCode() == obj.hashCode();
        }
        return false;
    }

    @Override
    public String toString() {

        return id + super.toString();
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
