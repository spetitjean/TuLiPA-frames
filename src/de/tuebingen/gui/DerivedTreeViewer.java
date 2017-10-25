/*
 *  File DerivedTreeViewer.java
 *
 *  Authors:
 *     Johannes Dellert  <johannes.dellert@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Johannes Dellert, 2007
 *
 *  Last modified:
 *     Di 16. Okt 10:50:21 CEST 2007
 *
 *  This file is part of the TuLiPA system
 *     http://www.sfb441.uni-tuebingen.de/emmy-noether-kallmeyer/tulipa
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
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.duesseldorf.frames.FSPrinter;
import de.duesseldorf.frames.Situation;
import de.tuebingen.derive.DerivedTree;
import de.tuebingen.derive.ElementaryTree;
import de.tuebingen.derive.TreeDeriver;
import de.tuebingen.expander.ParseTreeHandler;
import de.tuebingen.tag.Fs;
import de.tuebingen.tag.SemLit;
import de.tuebingen.tag.TagTree;

public class DerivedTreeViewer {

    public static ArrayList<ParseTreeCollection> getViewTreesFromDOM(Document d,
            Map<String, TagTree> treeDict, boolean elementaryTreeOutput,
            boolean derivationStepOutput, boolean debugMode,
            boolean needsAnchoring, List<String> semlabels, boolean noUtool) {
        return getViewTreesFromDOM(d, null, treeDict, elementaryTreeOutput,
                derivationStepOutput, debugMode, needsAnchoring, semlabels,
                noUtool);
    }

    public static ArrayList<ParseTreeCollection> getViewTreesFromDOM(Document d,
            Situation situation, Map<String, TagTree> treeDict,
            boolean elementaryTreeOutput, boolean derivationStepOutput,
            boolean debugMode, boolean needsAnchoring, List<String> semlabels,
            boolean noUtool) {
        try {
            // XMLTreeViewer.displayTree(d.getDocumentElement());
            Document derivationTrees = ParseTreeHandler
                    .extractDerivationTrees(d);
            // XMLTreeViewer.displayTree(derivationTrees.getDocumentElement());
            NodeList startNodes = derivationTrees.getElementsByTagName("start");
            ArrayList<ParseTreeCollection> viewTrees = new ArrayList<ParseTreeCollection>();
            for (int i = 0; i < startNodes.getLength(); i++) {
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
                    // if (dTree.success) derivedTree.collapseAllAttributes();
                    String semanticsString = "Semantic representation:<br>";
                    for (SemLit sl : dTree.semantics) {
                        semanticsString += sl.toString() + "<br>";
                    }
                    if (dTree.frames != null) {
                        List<Fs> mergedFrames=Fs.mergeFS(dTree.frames);
                        // clean up the list here
                        for (Fs fs : mergedFrames) {
                            semanticsString += FSPrinter.printFS(fs);
                        }
                    } else {
                        semanticsString += "frame null";
                    }
                    ParseTreeCollection trees = new ParseTreeCollection(
                            viewTree, derivedTree, semanticsString,
                            dTree.semantics, noUtool);
                    if (eTrees != null) {
                        ArrayList<XMLViewTree> viewElemTrees = new ArrayList<XMLViewTree>();
                        for (ElementaryTree eTree : eTrees) {
                            // doesn't work at the moment; elementary tree
                            // display not very helpful feature-wise
                            // eTree.updateTBFeatures(eTree.root, dTree.env,
                            // false);
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
        } catch (Exception e) {
            System.err.println("Error while reading XML File:");
            System.err.println(e.toString());
            StackTraceElement[] stack = e.getStackTrace();
            for (int i = 0; i < stack.length; i++) {
                System.err.println(stack[i]);
            }
            System.exit(0);
        }
        return new ArrayList<ParseTreeCollection>();
    }

    public static void displayTreesfromDOM(final String s, Document d,
            Map<String, TagTree> treeDict, boolean elementaryTreeOutput,
            boolean derivationStepOutput, boolean debugMode,
            boolean needsAnchoring, List<String> semlabels, boolean noUtool) {
        displayTreesfromDOM(s, d, null, treeDict, elementaryTreeOutput,
                derivationStepOutput, debugMode, needsAnchoring, semlabels,
                noUtool);
    }

    public static void displayTreesfromDOM(final String s, Document d,
            Situation situation, Map<String, TagTree> treeDict,
            boolean elementaryTreeOutput, boolean derivationStepOutput,
            boolean debugMode, boolean needsAnchoring, List<String> semlabels,
            boolean noUtool) {
        final ArrayList<ParseTreeCollection> viewTrees = getViewTreesFromDOM(d,
                situation, treeDict, elementaryTreeOutput, derivationStepOutput,
                debugMode, needsAnchoring, semlabels, noUtool);
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
