/*
 *  File DerivedTreeViewer.java
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
package de.tuebingen.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.duesseldorf.frames.FSPrintingTools;
import de.tuebingen.derive.DerivedTree;
import de.tuebingen.derive.ElementaryTree;
import de.tuebingen.derive.TreeDeriver;
import de.tuebingen.expander.ParseTreeHandler;
import de.tuebingen.tag.SemLit;
import de.tuebingen.tag.TagTree;

public class DerivedTreeViewer {

    public static ArrayList<ParseTreeCollection> getViewTreesFromDOM(Document d,
            Map<String, TagTree> treeDict, boolean elementaryTreeOutput,
            boolean derivationStepOutput, boolean debugMode,
            boolean needsAnchoring, List<String> semlabels, boolean noUtool) {

        try {
            // XMLTreeViewer.displayTree(d.getDocumentElement());
            // System.out.println("GETTING VIEW TREES");
            Document derivationTrees = ParseTreeHandler
                    .extractDerivationTrees(d);

            // XMLTreeViewer.displayTree(derivationTrees.getDocumentElement());
            NodeList startNodes = derivationTrees.getElementsByTagName("start");
            // Simon: added this for debugging
            // This should be done earlier, but I don't know where
            HashSet<Integer> toRemove = new HashSet<Integer>();
            for (int i = 0; i < startNodes.getLength(); i++) {
                for (int j = i + 1; j < startNodes.getLength(); j++) {
                    if (startNodes.item(i).isEqualNode(startNodes.item(j))) {
                        // System.out.println("Found duplicate: "+i+"-"+j);
                        toRemove.add(j);
                    }
                }
            }

            ArrayList<ParseTreeCollection> viewTrees = new ArrayList<ParseTreeCollection>();
            for (ParseTreeCollection parseTreeCollection : viewTrees) {
                System.out.println(
                        "DTV.97: frameSem" + parseTreeCollection.getFrameSem());
                // System.out.println(
                // "DTV.97: frames" + parseTreeCollection.getFrames());
            }
            for (int i = 0; i < startNodes.getLength(); i++) {
                if (toRemove.contains(i)) {
                    continue;
                }
                Node startNode = startNodes.item(i);

                XMLViewTree viewTree = ViewTreeBuilder
                        .makeViewableDerivationTree(startNode, treeDict);
                ArrayList<ElementaryTree> eTrees = null;
                if (elementaryTreeOutput)
                    eTrees = new ArrayList<ElementaryTree>();
                ArrayList<ElementaryTree> steps = null;
                if (derivationStepOutput)
                    steps = new ArrayList<ElementaryTree>();
                DerivedTree dTree = TreeDeriver.deriveTree(startNode, treeDict,
                        eTrees, steps, debugMode, semlabels, needsAnchoring);
                if (dTree != null) {
                    if (!dTree.success) {
                        viewTree.description = "*" + viewTree.description;
                    }
                    XMLViewTree derivedTree = ViewTreeBuilder
                            .makeViewableDerivedTree(dTree);
                    String semanticsString = "Semantic representation:<br>";
                    for (SemLit sl : dTree.semantics) {
                        semanticsString += sl.toString() + "<br>";
                    }
                    // if (dTree.frames != null) {
                    // All of this should be done in TreeDeriver
                    // Environment env= new Environment(0);
                    // List<Fs> mergedFrames = Fs.mergeFS(dTree.frames,
                    // situation,env);
                    // if(mergedFrames==null){
                    // continue;
                    // }
                    // // clean up the list here
                    // List<Fs> cleanFrames = FsTools.cleanup(mergedFrames);
                    // dTree.updateFeatures(dTree.root, env,
                    // false);
                    // // This is only because it's not done in TreeDeriver:
                    // derivedTree = ViewTreeBuilder
                    // .makeViewableDerivedTree(dTree);
                    // That is the only thing which should be here:
                    // for (Fs fs : cleanFrames) {
                    // for (Fs fs : dTree.frames) {
                    // semanticsString += FsTools.printFS(fs);
                    // }
                    // }
                    if (dTree.getFrameSem() != null) {
                        de.duesseldorf.frames.Frame frameSem = dTree
                                .getFrameSem();
                        semanticsString += FSPrintingTools.printFrame(frameSem,
                                debugMode);
                    }
                    // if (dTree.frames == null && dTree.getFrameSem() != null)
                    // {
                    // semanticsString += "frame null";
                    // }
                    // ParseTreeCollection trees = new ParseTreeCollection(
                    // viewTree, derivedTree, semanticsString,
                    // dTree.semantics, dTree.frames, dTree.getFrameSem(),
                    // noUtool);
                    ParseTreeCollection trees = new ParseTreeCollection(
                            viewTree, derivedTree, semanticsString,
                            dTree.semantics, dTree.getFrameSem(), noUtool);
                    if (eTrees != null) {
                        ArrayList<XMLViewTree> viewElemTrees = new ArrayList<XMLViewTree>();
                        for (ElementaryTree eTree : eTrees) {
                            // old method which might have worked for semlits
                            // but doesnt care about frames:
                            // eTree.updateTBFeatures(eTree.root, dTree.env,
                            // false);
                            // List<Fs> mergedFrames = Fs.mergeFS(eTree.frames,
                            // situation);
                            // clean up the list here7
                            // List<Fs> cleanFrames = FsTools
                            // .cleanup(mergedFrames);
                            // eTree.frames = cleanFrames;
                            XMLViewTree elemTree = ViewTreeBuilder
                                    .makeViewableElementaryTree(eTree);
                            viewElemTrees.add(elemTree);
                        }
                        trees.elementaryTrees = viewElemTrees;
                    }
                    if (steps != null) {
                        ArrayList<XMLViewTree> viewStepTrees = new ArrayList<XMLViewTree>();
                        for (ElementaryTree eTree : steps) {
                            XMLViewTree elemTree = ViewTreeBuilder
                                    .makeViewableElementaryTree(eTree);
                            viewStepTrees.add(elemTree);
                        }
                        trees.derivationSteps = viewStepTrees;
                    }
                    viewTrees.add(trees);
                }
            }
            return viewTrees;
        } catch (

        Exception e) {
            System.err.println("Error while reading XML File:");
            System.err.println(e.toString());
            StackTraceElement[] stack = e.getStackTrace();
            for (int i = 0; i < stack.length; i++) {
                System.err.println(stack[i]);
            }
            // System.exit(0);
        }
        return new ArrayList<ParseTreeCollection>();
    }

    public static void displayTreesfromDOM(final String s, Document d,
            Map<String, TagTree> treeDict, boolean elementaryTreeOutput,
            boolean derivationStepOutput, boolean debugMode,
            boolean needsAnchoring, List<String> semlabels, boolean noUtool) {

        ArrayList<ParseTreeCollection> viewTrees = getViewTreesFromDOM(d,
                treeDict, elementaryTreeOutput, derivationStepOutput, debugMode,
                needsAnchoring, semlabels, noUtool);
        // final ArrayList<ParseTreeCollection> viewTrees =
        // getViewTreesFromDOM(d,
        // situation, treeDict, elementaryTreeOutput, derivationStepOutput,
        // debugMode, needsAnchoring, semlabels, noUtool);
        HashSet<ParseTreeCollection> viewSet = new HashSet<ParseTreeCollection>(
                viewTrees);
        viewTrees = new ArrayList<ParseTreeCollection>(viewSet);
        if (viewTrees.size() > 0) {
            XMLTreeDisplay display = new XMLTreeDisplay(s, viewTrees);
            // display.setSize(1000, 750);
            // --------------------------------
            // full screen (to see the semantics on bottom)
            display.setState(Frame.NORMAL);
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension dimension = toolkit.getScreenSize();
            display.setSize(dimension);
            // --------------------------------

            display.setLocation(0, 0);
            display.setTitle("TuLiPa parse results: \"" + s + "\"");
            display.setVisible(true);
        } else {
            System.err.println(
                    "\nDerivedTreeViewer: No derivation trees found in forest!");
        }
    }

    public static void dumpParsesfromDOM(Document d,
            Map<String, TagTree> treeDict, File dumpFile,
            boolean needsAnchoring, List<String> semlabels) throws IOException {
        ArrayList<ParseTreeCollection> viewTrees = getViewTreesFromDOM(d,
                treeDict, false, false, false, needsAnchoring, semlabels,
                false);
        if (viewTrees.size() > 0) {
            FileWriter out = new FileWriter(dumpFile);
            out.write("<?xml version=\"1.0\" encoding=\"utf-8\">\n<parses>\n");
            for (ParseTreeCollection ptc : viewTrees) {
                out.write(ptc.toXML());
            }
            out.write("</parses>\n");
            out.close();
        } else {
            System.err.println(
                    "\nDerivedTreeViewer: No derivation trees found in forest!");
        }
    }

}
