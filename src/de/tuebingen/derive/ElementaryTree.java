/*
 *  File ElementaryTree.java
 *
 *  Authors:
 *     Johannes Dellert  <johannes.dellert@sfs.uni-tuebingen.de>
 *     David Arps <david.arps@hhu.de>
 *     Simon Petitjean <petitjean@phil.hhu.de>
 *
 *  Copyright:
 *     Johannes Dellert, 2007
 *     David Arps, 2017
 *     Simon Petitjean, 2017
 *
 *  Last modified:
 *     2017
 *
 *  This file is part of the TuLiPA-frames system
 *     https://github.com/spetitjean/TuLiPA-frames
 *
 *  TuLiPA is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TuLiPA is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.tuebingen.derive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.duesseldorf.frames.Frame;
import de.duesseldorf.frames.Fs;
import de.duesseldorf.frames.FsTools;
import de.duesseldorf.frames.Relation;
import de.duesseldorf.frames.UnifyException;
import de.duesseldorf.frames.Value;
import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.tag.Environment;
import de.tuebingen.tag.SemDom;
import de.tuebingen.tag.SemLit;
import de.tuebingen.tag.SemPred;
import de.tuebingen.tag.TagNode;
import de.tuebingen.tag.TagTree;

public class ElementaryTree {
    // id of the tree
    public String id;

    // pointer to the root node
    public Node root;

    // gorn address of the foot node (empty string if it is no auxiliary tree)
    public String foot;

    // gorn address of the anchor node (empty string if there is none)
    public String anchor;

    // feature strctures
    public HashMap<Node, Fs> topFeatures;
    public HashMap<Node, Fs> bottomFeatures;

    // list of semantic literals
    public List<SemLit> semantics;

    boolean verbose = false;

    // public List<Fs> frames;

    private Frame frameSem = new Frame();

    public Frame getFrameSem() {
        return frameSem;
    }

    public ElementaryTree(Node root, String foot, String anchor,
                          HashMap<Node, Fs> topFeatures, HashMap<Node, Fs> bottomFeatures,
                          List<SemLit> semantics) {
        this.id = "";
        this.root = root;
        this.foot = foot;
        this.anchor = anchor;
        this.topFeatures = topFeatures;
        this.bottomFeatures = bottomFeatures;
        this.semantics = semantics;
        this.frameSem = new Frame();
    }

    // public ElementaryTree(Node root, String foot, String anchor,
    // HashMap<Node, Fs> topFeatures, HashMap<Node, Fs> bottomFeatures,
    // List<SemLit> semantics, List<Fs> frames) {
    // this.id = "";
    // this.root = root;
    // this.foot = foot;
    // this.anchor = anchor;
    // this.topFeatures = topFeatures;
    // this.bottomFeatures = bottomFeatures;
    // this.semantics = semantics;
    // this.frames = frames;
    // }

    // public ElementaryTree(Node root, String foot, String anchor,
    // HashMap<Node, Fs> topFeatures, HashMap<Node, Fs> bottomFeatures,
    // List<SemLit> semantics, List<Fs> oldframes, Frame frameSem) {
    public ElementaryTree(Node root, String foot, String anchor,
                          HashMap<Node, Fs> topFeatures, HashMap<Node, Fs> bottomFeatures,
                          List<SemLit> semantics, Frame frameSem) {
        this.id = "";
        this.root = root;
        this.foot = foot;
        this.anchor = anchor;
        this.topFeatures = topFeatures;
        this.bottomFeatures = bottomFeatures;
        this.semantics = semantics;
        // this.frames = oldframes;
        this.frameSem = frameSem;
    }

    public ElementaryTree(TagTree tree) {
        Document D = null;
        try {
            // build the output document with "tree" as document element tag
            D = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .getDOMImplementation().createDocument(null, "tree", null);
            foot = "";
            anchor = "";
            topFeatures = new HashMap<Node, Fs>();
            bottomFeatures = new HashMap<Node, Fs>();
            semantics = tree.getSem();
            // frames = tree.getFrames();
            frameSem = tree.getFrameSem();

            root = convertTAGNodeToXML("0", tree.getRoot(), D);
            // id = tree.getId();
            id = tree.getOriginalId();
            if (verbose)
                ((Element) root).setAttribute("id", id);
            // System.err.println("Created tree: " + id);
        } catch (Exception e) {
            System.err.println("Error while building parse tree XML");
            System.err.println(e.getMessage());
            StackTraceElement[] stack = e.getStackTrace();
            for (int i = 0; i < stack.length; i++) {
                System.err.println(stack[i]);
            }
            System.exit(0);
        }
    }

    public Node convertTAGNodeToXML(String address, de.tuebingen.tree.Node node,
                                    Document D) {
        TagNode tagNode = (TagNode) node;
        String nodeName = tagNode.getCategory();
        // can't have nodes without names because of XML
        // if (nodeName == null) nodeName = "";
        if (nodeName.length() == 0)
            nodeName = "_";
        Node root = D.createElement(nodeName);
        // System.err.println(tagNode.getLabel());
        if (tagNode.getLabel() != null) {
            if (tagNode.getLabel().getFeat("top") != null) {
                topFeatures.put(root,
                        tagNode.getLabel().getFeat("top").getAvmVal());
            }
            if (tagNode.getLabel().getFeat("bot") != null) {
                bottomFeatures.put(root,
                        tagNode.getLabel().getFeat("bot").getAvmVal());
            }
        }
        if (tagNode.getChildren() != null) {
            for (int i = 0; i < tagNode.getChildren().size(); i++) {
                de.tuebingen.tree.Node child = tagNode.getChildren().get(i);

                String childAddress = address + "." + (i + 1);
                if (childAddress.startsWith("0."))
                    childAddress = childAddress.substring(2);
                Node childNode = convertTAGNodeToXML(childAddress, child, D);
                root.appendChild(childNode);
            }
        }
        if (tagNode.getType() == TagNode.ANCHOR) {
            anchor = address;
        }
        if (tagNode.getType() == TagNode.FOOT) {
            foot = address;
        }
        return root;
    }

    public Node getNodeByAddress(String address) {
        Node n = root;
        if (address.equals(""))
            return null;
        if (address.equals("0"))
            return n;
        String childId = "";
        for (int i = 0; i < address.length(); i++) {
            char c = address.charAt(i);
            if (c == '.') {
                int id = Integer.parseInt(childId);
                n = n.getChildNodes().item(id - 1);
                childId = "";
            } else {
                childId += c;
            }
        }
        int id = Integer.parseInt(childId);
        n = n.getChildNodes().item(id - 1);
        if (n == null) {
            System.err.println(
                    "WARNING: Something went wrong in node addressing!");
        }
        return n;
    }

    public ElementaryTree instantiate(Document D) {
        NameFactory nf = new NameFactory();
        HashMap<Node, Fs> newTopFeatures = new HashMap<Node, Fs>();
        HashMap<Node, Fs> newBottomFeatures = new HashMap<Node, Fs>();
        List<SemLit> newSemantics = new LinkedList<SemLit>();
        for (SemLit sl : semantics) {
            if (sl instanceof SemPred)
                newSemantics.add(new SemPred((SemPred) sl, nf));
            else if (sl instanceof SemDom)
                newSemantics.add(new SemDom((SemDom) sl, nf));
            else if (sl instanceof Value)
                newSemantics.add(new Value((Value) sl, nf));
        }
        // List<Fs> newFrames = frames;
        // DA addRelations

        Frame newFrameSem = getFrameSem();
        // END DA addRelations
        // called different constructor for new frame:
        // return new ElementaryTree(
        // copyNodeStructure(nf, root, D, newTopFeatures,
        // newBottomFeatures),
        // foot, anchor, newTopFeatures, newBottomFeatures, newSemantics,
        // newFrames, newFrameSem);
        return new ElementaryTree(
                copyNodeStructure(nf, root, D, newTopFeatures,
                        newBottomFeatures),
                foot, anchor, newTopFeatures, newBottomFeatures, newSemantics,
                newFrameSem);
    }

    public ElementaryTree createDumpingInstance(Document D) {
        HashMap<Node, Fs> newTopFeatures = new HashMap<Node, Fs>();
        HashMap<Node, Fs> newBottomFeatures = new HashMap<Node, Fs>();
        List<SemLit> newSemantics = new LinkedList<SemLit>();
        for (SemLit sl : semantics) {
            if (sl instanceof SemPred)
                newSemantics.add(new SemPred((SemPred) sl));
            else if (sl instanceof SemDom)
                newSemantics.add(new SemDom((SemDom) sl));
            else if (sl instanceof Value)
                newSemantics.add(new Value((Value) sl));
        }
        // List<Fs> newFrames = frames;
        // return new ElementaryTree(
        // copyNodeStructureWithoutNameFactory(root, D, newTopFeatures,
        // newBottomFeatures),
        // foot, anchor, newTopFeatures, newBottomFeatures, newSemantics,
        // newFrames, frameSem);
        return new ElementaryTree(
                copyNodeStructureWithoutNameFactory(root, D, newTopFeatures,
                        newBottomFeatures),
                foot, anchor, newTopFeatures, newBottomFeatures, newSemantics,
                frameSem);
    }

    public Node copyNodeStructureWithoutNameFactory(Node n, Document D,
                                                    HashMap<Node, Fs> newTopFeatures,
                                                    HashMap<Node, Fs> newBottomFeatures) {

        Node newN = D.importNode(n, false).cloneNode(false);
        if (topFeatures.get(n) != null) {
            newTopFeatures.put(newN, new Fs(topFeatures.get(n)));
        }
        if (bottomFeatures.get(n) != null) {
            newBottomFeatures.put(newN, new Fs(bottomFeatures.get(n)));
        }
        for (int i = 0; i < n.getChildNodes().getLength(); i++) {
            Node newChild = copyNodeStructureWithoutNameFactory(
                    n.getChildNodes().item(i), D, newTopFeatures,
                    newBottomFeatures);
            newN.appendChild(newChild);
        }
        return newN;
    }

    // this copy method keeps the feature structure references
    public Node copyNodeStructure(NameFactory nf, Node n, Document D,
                                  HashMap<Node, Fs> newTopFeatures,
                                  HashMap<Node, Fs> newBottomFeatures) {

        Node newN = D.importNode(n, false).cloneNode(false);
        if (topFeatures.get(n) != null) {
            newTopFeatures.put(newN, new Fs(topFeatures.get(n), nf));
        }
        if (bottomFeatures.get(n) != null) {
            newBottomFeatures.put(newN, new Fs(bottomFeatures.get(n), nf));
        }
        for (int i = 0; i < n.getChildNodes().getLength(); i++) {
            Node newChild = copyNodeStructure(nf, n.getChildNodes().item(i), D,
                    newTopFeatures, newBottomFeatures);
            newN.appendChild(newChild);
        }
        return newN;
    }

    public void lexicalize(String lex) {
        Node anchorNode = getNodeByAddress(anchor);
        if (anchorNode != null) {
            Node lexNode = root.getOwnerDocument().createElement(lex);
            if (anchorNode == root) {
                root = lexNode;
            } else {
                anchorNode.getParentNode().replaceChild(lexNode, anchorNode);
            }
        }
    }

    public void applyOperations(ArrayList<Object[]> operations,
                                DerivedTree dTree, ArrayList<ElementaryTree> steps)
            throws UnifyException {
        // first store the nodes to operate on (addresses will be misled by
        // adjunctions)
        for (Object[] op : operations) {
            // System.err.print("Looking for node at address: " + op[3] + " -->
            // ");
            op[3] = getNodeByAddress((String) op[3]);
            // System.err.println(op[3]);
        }
        for (Object[] op : operations) {
            if (((ElementaryTree) op[2]).anchor != null) {
                dTree.numTerminals++;
            }
            if (((String) op[1]).equals("adj")) {
                if (adjoin((ElementaryTree) op[2], (Node) op[3], dTree)) {
                    dTree.topFeatures.put(root,
                            FsTools.unify(dTree.topFeatures.get(root),
                                    dTree.topFeatures.get(dTree.root),
                                    dTree.env));
                    dTree.root = root;
                }
            } else if (((String) op[1]).equals("subst")) {
                if (substitute((ElementaryTree) op[2], (Node) op[3], dTree)) {
                    dTree.topFeatures.put(root,
                            FsTools.unify(dTree.topFeatures.get(root),
                                    dTree.topFeatures.get(dTree.root),
                                    dTree.env));
                    dTree.root = root;
                }
            }
            dTree.semantics.addAll(((ElementaryTree) op[2]).semantics);
            updateSem(dTree.semantics, dTree.env, false);
            // dTree.frames.addAll(((ElementaryTree) op[2]).frames);
            // dTree.frames = updateFrames(dTree.frames, dTree.env, false);

            Frame newFrameSem = dTree.getFrameSem();
            newFrameSem.addOtherFrame(((ElementaryTree) op[2]).getFrameSem());
            dTree.setFrameSem(updateFrameSem(newFrameSem, dTree.env, false));

            // for verbose output of the steps
            // OLD:
            // if (steps != null) {
            // dTree.updateTopDownFeatures(dTree.root, false, false);
            // ElementaryTree newStep = (new ElementaryTree(dTree.root, "", "",
            // dTree.topFeatures, dTree.bottomFeatures,
            // dTree.semantics, dTree.frames))
            // .createDumpingInstance(root.getOwnerDocument());
            // newStep.setID("Step " + steps.size());
            // steps.add(newStep);
            // }
            // END OLD
            // NEW
            if (steps != null) {
                dTree.updateTopDownFeatures(dTree.root, false, false);
                // ElementaryTree newStep = (new ElementaryTree(dTree.root, "",
                // "",
                // dTree.topFeatures, dTree.bottomFeatures,
                // dTree.semantics, dTree.frames, dTree.getFrameSem()))
                // .createDumpingInstance(root.getOwnerDocument());
                ElementaryTree newStep = (new ElementaryTree(dTree.root, "", "",
                        dTree.topFeatures, dTree.bottomFeatures,
                        dTree.semantics, dTree.getFrameSem()))
                        .createDumpingInstance(root.getOwnerDocument());
                newStep.setID("Step " + steps.size());
                steps.add(newStep);
            }
            // END NEW
        }
    }

    /**
     * returns true if derived tree root has been changed
     *
     * @param adjoinedTree
     * @param adjoinNode
     * @param dTree
     * @return
     * @throws UnifyException
     */
    public boolean adjoin(ElementaryTree adjoinedTree, Node adjoinNode,
                          DerivedTree dTree) throws UnifyException {
        Node rootNode = adjoinedTree.root;
        if (adjoinedTree.foot.length() == 0) {
            System.err.println("ERROR: Adjoined tree has no foot node!");
            System.exit(1);
        }

        Fs addedTopFs = dTree.topFeatures.get(adjoinNode);
        if (addedTopFs != null) {
            // System.err.println("original top: " + addedTopFs);
            // System.err.println("adjunction top: "
            // + adjoinedTree.topFeatures.get(rootNode));
            // System.err.println("environment: " + dTree.env);

            // Why is this feature structure null?
            if (adjoinedTree.topFeatures.get(rootNode) == null) {
                adjoinedTree.topFeatures.put(rootNode, new Fs());
            }

            // System.err.println("adjunction top: ");
            // System.err.println(" rootNode: " + rootNode.toString());
            /*
             * Iterator<Node> it = adjoinedTree.topFeatures.keySet().iterator();
             * while (it.hasNext()) {
             * Node n = it.next();
             * Fs fs = adjoinedTree.topFeatures.get(n);
             * System.err.println(n + "--" + fs.toString());
             * }
             */
            addedTopFs = FsTools.unify(addedTopFs,
                    adjoinedTree.topFeatures.get(rootNode), dTree.env);
        }
        dTree.topFeatures.put(rootNode, addedTopFs);
        if (adjoinedTree.bottomFeatures.get(rootNode) != null) {
            dTree.bottomFeatures.put(rootNode,
                    adjoinedTree.bottomFeatures.get(rootNode));
        }
        Node footNode = adjoinedTree.getNodeByAddress(adjoinedTree.foot);
        int childrenToRepend = adjoinNode.getChildNodes().getLength();
        for (int i = 0; i < childrenToRepend; i++) {
            footNode.appendChild(adjoinNode.getChildNodes().item(0));
        }

        Fs addedBotFs = dTree.bottomFeatures.get(adjoinNode);
        if (addedBotFs != null
                && adjoinedTree.bottomFeatures.get(footNode) != null) {
            // System.err.println("original bottom: " + addedBotFs);
            // System.err.println("adjunction bottom: " +
            // adjoinedTree.bottomFeatures.get(footNode));
            // System.err.println("environment: " + dTree.env);
            addedBotFs = FsTools.unify(addedBotFs,
                    adjoinedTree.bottomFeatures.get(footNode), dTree.env);
        }
        dTree.bottomFeatures.put(footNode, addedBotFs);

        // take top features from adjoined node to simulate foot node
        // replacement
        if (adjoinedTree.topFeatures.get(footNode) != null) {
            dTree.topFeatures.put(footNode,
                    adjoinedTree.topFeatures.get(footNode));
        }
        dTree.addMissingTopFeatures(adjoinedTree.topFeatures);
        dTree.addMissingBottomFeatures(adjoinedTree.bottomFeatures);

        // this part handles integration into derived tree
        if (adjoinNode.getParentNode() != null) {
            if (verbose)
                System.err.println("Replacing child " + adjoinNode.hashCode()
                        + " with " + rootNode.hashCode() + " at "
                        + adjoinNode.getParentNode().hashCode());
            adjoinNode.getParentNode().replaceChild(rootNode, adjoinNode);
        }
        // if adjunction takes place at topmost node in derived tree, the root
        // of the derived tree must be updated
        else {
            if (verbose)
                System.err.println(adjoinNode.hashCode() + " without father!");
            root = rootNode;
            return true;
        }

        return false;
    }

    // returns true if derived tree root has been changed
    public boolean substitute(ElementaryTree substTree, Node substNode,
                              DerivedTree dTree) throws UnifyException {
        if (verbose) {
            System.err.println(
                    "Substitution: " + substTree.hashCode() + ", " + substNode);
        }
        Node rootNode = substTree.root;
        Fs addedTopFs = dTree.topFeatures.get(substNode);
        if (addedTopFs != null) {
            addedTopFs = FsTools.unify(addedTopFs,
                    substTree.topFeatures.get(rootNode), dTree.env);
        }
        dTree.topFeatures.put(rootNode, addedTopFs);
        dTree.addMissingTopFeatures(substTree.topFeatures);
        dTree.addMissingBottomFeatures(substTree.bottomFeatures);

        if (substNode.getParentNode() != null) {
            if (verbose)
                System.err.println("Replacing child " + substNode.hashCode()
                        + " with " + rootNode.hashCode() + " at "
                        + substNode.getParentNode().hashCode());
            substNode.getParentNode().replaceChild(rootNode, substNode);
        } else {
            if (verbose)
                System.err.println(substNode.hashCode() + " without father!");
            root = rootNode;
            return true;
        }
        return false;
    }

    public void setID(String id) {
        this.id = id;
    }

    public static void updateSem(List<SemLit> semlist, Environment env,
                                 boolean finalUpdate) {
        for (SemLit sl : semlist) {
            sl.update(env, finalUpdate);
        }
    }

    /**
     * updating the variables in the frames with respect to a variable
     *
     * @param frames
     * @param env
     * @param finalUpdate
     * @throws UnifyException
     */
    public static List<Fs> updateFrames(List<Fs> frames, Environment env,
                                        boolean finalUpdate) throws UnifyException {
        List<Fs> newFrames = new ArrayList<Fs>();
        for (Fs fs : frames) {
            // System.out.println("Before: " + fs);
            if (fs != null) {
                newFrames.add(Fs.updateFS(fs, env, finalUpdate));
                // System.out.println("After: " + fs);
            }
        }
        return newFrames;
    }

    // public void concatFrames(Fs frame) {
    // if (this.frames == null) {
    // frames = new LinkedList<Fs>();
    // }
    // frames.add(frame);
    // }

    /**
     * similar to the old updateFs method. changes the variables in the frameSem
     *
     * @param frameSem
     * @param env
     * @param finalUpdate
     * @return
     * @throws UnifyException
     */
    public static Frame updateFrameSem(Frame frameSem, Environment env,
                                       boolean finalUpdate) throws UnifyException {

        List<Fs> newFs = new LinkedList<Fs>();
        if (frameSem != null && frameSem.getFeatureStructures() != null)
            for (Fs fs : frameSem.getFeatureStructures()) {
                if (fs != null)
                    newFs.add(Fs.updateFS(fs, env, finalUpdate));
            }

        Set<Relation> newRelations = new HashSet<Relation>();
        if (frameSem != null && frameSem.getRelations() != null)
            for (Relation oldRel : frameSem.getRelations()) {
                List<Value> newArgs = new LinkedList<Value>();
                for (Value oldVal : oldRel.getArguments()) {
                    Value oldCopy = new Value(oldVal);
                    oldCopy.update(env, finalUpdate);
                    // Value newVal = env.deref(oldVal);
                    newArgs.add(oldCopy);
                }
                newRelations.add(new Relation(oldRel.getName(), newArgs));
            }

        return new Frame(newFs, newRelations);
    }

    /**
     * Like updateFrameSem, but merging the frames in the end
     *
     * @param frameSem
     * @param env
     * @param finalUpdate
     * @return
     * @throws UnifyException
     */
    public static Frame updateFrameSemWithMerge(Frame frameSem, Environment env,
                                                boolean finalUpdate) throws UnifyException {
        NameFactory nf = new NameFactory();
        List<Fs> newFs = new LinkedList<Fs>();
        // System.out.println("Environment before update: "+env);

        for (Fs fs : frameSem.getFeatureStructures()) {
            if (fs != null)
                newFs.add(Fs.updateFS(fs, env, finalUpdate));
        }
        // do not know why 2 merges are now necessary...
        List<Fs> mergedFrames = Fs.mergeFS(newFs, env, nf);
        if (mergedFrames != null)
            mergedFrames = Fs.mergeFS(newFs, env, nf);
        List<Fs> cleanedFrames = new LinkedList<Fs>();
        if (mergedFrames == null) {
            System.err.println("Frame unification failed, tree discarded!\n");
            return null;
        } else {
            cleanedFrames = FsTools.cleanup(mergedFrames);
        }

        Set<Relation> newRelations = new HashSet<Relation>();
        // System.out.println("Old relations: "+frameSem.getRelations());

        for (Relation oldRel : frameSem.getRelations()) {
            List<Value> newArgs = new LinkedList<Value>();
            for (Value oldVal : oldRel.getArguments()) {
                Value oldCopy = new Value(oldVal);
                oldCopy.update(env, finalUpdate);
                // Value newVal = env.deref(oldVal);
                newArgs.add(oldCopy);
            }
            newRelations.add(new Relation(oldRel.getName(), newArgs));
        }
        // System.out.println("Environment: "+env);
        // System.out.println("New relations: "+newRelations);
        return new Frame(cleanedFrames, newRelations);
    }

    public void updateTBFeatures(Node n, Environment env, boolean finalUpdate)
            throws UnifyException {
        // update vars by environment
        Fs bfs = bottomFeatures.get(n);
        if (bfs != null) {
            bfs = Fs.updateFS(bfs, env, finalUpdate);
            bottomFeatures.put(n, bfs);
        }
        Fs tfs = topFeatures.get(n);
        if (tfs != null) {
            tfs = Fs.updateFS(tfs, env, finalUpdate);
            topFeatures.put(n, tfs);
        }
        // update child node features
        for (int i = 0; i < n.getChildNodes().getLength(); i++) {
            updateTBFeatures(n.getChildNodes().item(i), env, finalUpdate);
        }
    }
}
