/*
 *  File TreeDeriver.java
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

//import de.tuebingen.gui.XMLTreeViewer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.duesseldorf.frames.ConstraintChecker;
import de.duesseldorf.frames.Frame;
import de.duesseldorf.frames.UnifyException;
import de.tuebingen.tag.Environment;
import de.tuebingen.tag.TagTree;
import de.tuebingen.anchoring.NameFactory;


public class TreeDeriver {
    public static DerivedTree deriveTree(Node derivationTree,
            Map<String, TagTree> treeDict, ArrayList<ElementaryTree> eTrees,
            ArrayList<ElementaryTree> steps, boolean returnIncompleteTrees,
            List<String> semlabels, boolean needsAnchoring) {
        //System.out.println("\n\nDeriving new tree");
        DerivedTree derivedTree = null;
        boolean failed = false;
        try {
            Document D = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().getDOMImplementation()
                    .createDocument(null, "tree", null);
            Node initialTreeNode = derivationTree.getFirstChild()
                    .getFirstChild();
            String initialTreeID = initialTreeNode.getAttributes()
                    .getNamedItem("id").getNodeValue();
            ElementaryTree iniTree = getTreeInstance(initialTreeID, treeDict, D,
                    eTrees, needsAnchoring);
	    NameFactory nf = new NameFactory();
            if (iniTree != null) {
                derivedTree = new DerivedTree(iniTree);
                if (steps != null) {
                    derivedTree.updateTopDownFeatures(derivedTree.root, false,
                            false);
                    // ElementaryTree newStep = (new ElementaryTree(
                    // derivedTree.root, "", "", derivedTree.topFeatures,
                    // derivedTree.bottomFeatures, derivedTree.semantics,
                    // derivedTree.frames, derivedTree.getFrameSem()))
                    // .createDumpingInstance(D);
                    ElementaryTree newStep = (new ElementaryTree(
                            derivedTree.root, "", "", derivedTree.topFeatures,
                            derivedTree.bottomFeatures, derivedTree.semantics,
                            derivedTree.getFrameSem()))
                                    .createDumpingInstance(D);
                    System.out.println(
                            "frameSem in newStep: " + newStep.getFrameSem());
                    newStep.setID("Step " + steps.size());
                    steps.add(newStep);
                }
                // XMLTreeViewer.displayTree(derivedTree.root);
                // System.err.println(" === NEW DERIVATION TREE === ");
                ArrayList<Object[]> operations = prepareOperations(
                        initialTreeNode, treeDict, D, eTrees, needsAnchoring);
                iniTree.applyOperations(operations, derivedTree, steps);
                for (Object[] operation : operations) {
                    recursivelyDeriveTree((Node) operation[4],
                            (ElementaryTree) operation[2], treeDict, D,
                            derivedTree, eTrees, steps, needsAnchoring);
                }
                // XMLTreeViewer.displayTree(derivedTree.root);
            } else {
                System.err.println(
                        "ERROR: Tree not found in grammar: " + initialTreeID);
                System.exit(1);
            }
            // System.err.println("Pre-final environment: " + derivedTree.env);
            // System.err.println("Pre-final sem: " +
            // derivedTree.semantics.toString());
            // System.out.println("\n\nUpdating top-down features");
            derivedTree.updateTopDownFeatures(derivedTree.root, true, false);
            // System.out.println("*****Updated top-down features******");
            // System.err.println("Environment after TOP-BOT: " +
            // derivedTree.env);
            ElementaryTree.updateSem(derivedTree.semantics, derivedTree.env,
                    false);
            // System.out.println("Environment: "+derivedTree.env);
            // System.err.println("Sem after TOP-BOT: " +
            // derivedTree.semantics.toString());
            derivedTree.updateFeatures(derivedTree.root, derivedTree.env, nf,
                    false);
            // System.err.println("Environment after TOP-BOT: " +
            // derivedTree.env);

            // for variables and semantic labels renaming:
            // System.err.println("Sem labels:\n" + semlabels);
            derivedTree.env.setSemlabels(semlabels);

            // Environment.rename(derivedTree.env);
            // System.out.println("Ended rename ");
            derivedTree.updateFeatures(derivedTree.root, derivedTree.env, nf, true);
            ElementaryTree.updateSem(derivedTree.semantics, derivedTree.env,
                    true);
            // Environment.rename(derivedTree.env);

            // derivedTree.frames =
            // ElementaryTree.updateFrames(derivedTree.frames,
            // derivedTree.env, true);
            // System.out.println("Updated frames: " + derivedTree.frames);
            // System.out.println("Another round");
            // derivedTree.frames =
            // ElementaryTree.updateFrames(derivedTree.frames,
            // derivedTree.env, true);
            // Environment.rename(derivedTree.env);
            // System.out.println("Environment: "+derivedTree.env);

            // derivedTree.frames =
            // ElementaryTree.updateFrames(derivedTree.frames,
            // derivedTree.env, true);

            // List<Fs> mergedFrames = Fs.mergeFS(derivedTree.frames, situation,
            // derivedTree.env);
            // List<Fs> mergedFrames = derivedTree.frames;
            derivedTree.updateFeatures(derivedTree.root, derivedTree.env, nf, true);
	    Boolean p1 = derivedTree.postUpdateFeatures(derivedTree.root, derivedTree.env, nf,
					   true);
	    Boolean p2 = derivedTree.postPostUpdateFeatures(derivedTree.root, derivedTree.env, nf,
					   true);
	    // we need a second round (see comment in Fs:collect_corefs about artificial variables)
	    p1 = derivedTree.postUpdateFeatures(derivedTree.root, derivedTree.env, nf,
	    				   true);
	    p2 = derivedTree.postPostUpdateFeatures(derivedTree.root, derivedTree.env, nf,
	    				   true);
	    if(!p1||!p2)
	     	failed=true;
            // if (mergedFrames == null) {
            // System.err
            // .println("Frame unification failed, tree discarded!\n");
            // failed = true;
            // } else {
            // List<Fs> cleanFrames = FsTools.cleanup(mergedFrames);
            // derivedTree.frames = cleanFrames;
            // }
            // System.out.println("Derived tree env before: "+derivedTree.env);
            // DA addRelations

	    //System.out.println("\n\nOld sem: "+derivedTree.getFrameSem());
            Frame newFrameSem = ElementaryTree.updateFrameSemWithMerge(
                    derivedTree.getFrameSem(), derivedTree.env, true);

            if (newFrameSem == null) {
                failed = true;
	    }
	    if(!failed){
		newFrameSem = new ConstraintChecker(newFrameSem,
						    derivedTree.env, returnIncompleteTrees)
		    .checkConstraints();
                // newFrameSem = ElementaryTree.updateFrameSemWithMerge(
                // newFrameSem, derivedTree.env, false);
                derivedTree.setFrameSem(newFrameSem);
		//System.out.println("\n\nNew sem: "+newFrameSem);
                // System.out.println("Derived tree env after:
                // "+derivedTree.env);
                Environment.rename(derivedTree.env);
                derivedTree.updateFeatures(derivedTree.root, derivedTree.env, nf,
                        true);
                derivedTree.setFrameSem(ElementaryTree
                        .updateFrameSem(newFrameSem, derivedTree.env, true));
                // System.out.println("env: " + derivedTree.env);
            }
            // End DA addRelations
        } catch (UnifyException e) {
            System.err.println("Unify Exception (derived tree building): "
                    + e.getMessage());
            failed = true;
        } catch (Exception e) {
            System.err.println("Error while deriving tree:");
            System.err.println(e.getMessage());
            e.printStackTrace();
            // System.exit(1);
	    failed = true;
        }
        // TODO: find threshold here by taking input size into consideration
        if (needsAnchoring && derivedTree.numTerminals < Integer.MIN_VALUE) {
            System.err.println("Incomplete tree discarded!\n");
            failed = true;
        }
        if (failed) {
            if (!returnIncompleteTrees)
                return null;
            derivedTree.success = false;
        }
        // System.out.println("\n\n\nFrames at the end: ");
        // for(Fs a_frame:derivedTree.frames)
        // System.out.println(a_frame);
        return derivedTree;
    }

    public static void recursivelyDeriveTree(Node treeNode,
            ElementaryTree elementaryTree, Map<String, TagTree> treeDict,
            Document D, DerivedTree t, ArrayList<ElementaryTree> eTrees,
            ArrayList<ElementaryTree> steps, boolean needsAnchoring)
            throws UnifyException {
        // System.out.println(
        // "Test output: recursivelyDeriveTree in TreeDeriver was used");
        ArrayList<Object[]> operations = prepareOperations(treeNode, treeDict,
                D, eTrees, needsAnchoring);
        elementaryTree.applyOperations(operations, t, steps);
        for (Object[] operation : operations) {
            recursivelyDeriveTree((Node) operation[4],
                    (ElementaryTree) operation[2], treeDict, D, t, eTrees,
                    steps, needsAnchoring);
        }
    }

    /**
     * Instantiation of elementary trees during the derived tree building
     * 
     * @param id
     * @param treeDict
     * @param D
     * @param eTrees
     * @param needsAnchoring
     *            : if we are handling a formal grammar (i.e. no anchoring), we
     *            need to create tree-based name space, otherwise, we can the
     *            tuple name space.
     * @return
     */
    public static ElementaryTree getTreeInstance(String id,
            Map<String, TagTree> treeDict, Document D,
            ArrayList<ElementaryTree> eTrees, boolean needsAnchoring) {
        // System.err.println(treeDict.keySet());
        // tree dict entries are accessed without disambiguation IDs
        // if (id.indexOf("__") >= 0) id = id.substring(0,id.indexOf("__"));
        ElementaryTree tree = new ElementaryTree(treeDict.get(id));

        // ----------------------------
        // added tree renaming (c.f. lexical ambiguity)
        tree.setID(treeDict.get(id).getOriginalId());
        // ----------------------------
        if (eTrees != null)
            eTrees.add(tree);
        if (needsAnchoring) {
            ElementaryTree dump = tree.createDumpingInstance(D);
            return dump;
        } else {
            return tree.instantiate(D);
        }
    }

    public static ArrayList<Object[]> prepareOperations(Node treeNode,
            Map<String, TagTree> treeDict, Document D,
            ArrayList<ElementaryTree> eTrees, boolean needsAnchoring) {
        // System.out.println(treeNode);
        ArrayList<Object[]> operations = new ArrayList<Object[]>();
        for (int i = 0; i < treeNode.getChildNodes().getLength(); i++) {
            Node op = treeNode.getChildNodes().item(i);
            if (op.getNodeName().equals("adj")) {
                Object[] operation = new Object[5];
                operation[0] = getOpId(op);
                operation[1] = "adj";

                for (int j = 0; j < op.getChildNodes().getLength(); j++) {

                    Node child = op.getChildNodes().item(j);
                    // System.err.println(DOMWriter.elementToString(child, 0));
                    if (child.getNodeName().equals("tree")) {

                        String adjoinedTreeId = child.getAttributes()
                                .getNamedItem("id").getNodeValue();
                        // System.err.println(adjoinedTreeId);
                        String address = op.getAttributes().getNamedItem("node")
                                .getNodeValue();
                        ElementaryTree adjoinedTree = getTreeInstance(
                                adjoinedTreeId, treeDict, D, eTrees,
                                needsAnchoring);
                        if (adjoinedTree != null) {
                            operation[2] = adjoinedTree;
                            operation[3] = address;
                            operation[4] = child;
                        } else {
                            System.err.println(
                                    "ERROR: Tree not found in grammar: "
                                            + adjoinedTreeId);
                            System.exit(1);
                        }
                    }

                }
                operations.add(operation);
            } else if (op.getNodeName().equals("subst")) {
                Object[] operation = new Object[5];
                operation[0] = getOpId(op);
                operation[1] = "subst";
                for (int j = 0; j < op.getChildNodes().getLength(); j++) {
                    Node child = op.getChildNodes().item(j);
                    if (child.getNodeName().equals("tree")) {
                        String substTreeId = child.getAttributes()
                                .getNamedItem("id").getNodeValue();
                        // System.err.println(substTreeId);
                        String address = op.getAttributes().getNamedItem("node")
                                .getNodeValue();
                        ElementaryTree substTree = getTreeInstance(substTreeId,
                                treeDict, D, eTrees, needsAnchoring);
                        if (substTree != null) {
                            operation[2] = substTree;
                            operation[3] = address;
                            operation[4] = child;
                        } else {
                            System.err.println(
                                    "ERROR: Tree not found in grammar: "
                                            + substTreeId);
                            System.exit(1);
                        }
                    }
                }
                operations.add(operation);
            }
        }
        return operations;
    }

    public static int getOpId(Node op) {
        String idString = op.getAttributes().getNamedItem("id").getNodeValue();
        idString = idString.replaceAll("_", "");
        int id = Integer.parseInt(idString);
        return id;
    }
}
