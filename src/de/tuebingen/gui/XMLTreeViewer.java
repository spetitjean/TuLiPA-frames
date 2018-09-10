/*
 *  File XMLTreeViewer.java
 *
 *  Authors:
 *     Johannes Dellert  <johannes.dellert@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Johannes Dellert, 2007
 *
 *  Last modified:
 *     Di 16. Okt 10:52:29 CEST 2007
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

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XMLTreeViewer {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("\nPlease specify the filename");
            System.exit(0);
        }
        String filename = args[0];
        try {
            Document D = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(new File(filename));
            XMLViewTree viewTree = ViewTreeBuilder.createViewTree(D);
            ArrayList<ParseTreeCollection> trees = new ArrayList<ParseTreeCollection>();
            ParseTreeCollection viewTreeContainer = new ParseTreeCollection(
                    viewTree, viewTree, "", null, null, false);
            trees.add(viewTreeContainer);
            XMLTreeDisplay display = new XMLTreeDisplay(trees);
            display.setSize(1000, 750);
            display.setLocation(0, 0);
            display.setVisible(true);
        } catch (Exception e) {
            System.out.println("Error while reading XML File:");
            System.out.println(e.getMessage());
            StackTraceElement[] stack = e.getStackTrace();
            for (int i = 0; i < stack.length; i++) {
                System.out.println(stack[i]);
            }
            System.exit(0);
        }
    }

    public static void displayTree(Node toDisplay) {
        XMLViewTree viewTree = ViewTreeBuilder.createViewTree(toDisplay);
        viewTree.createNodeLayers();
        viewTree.setTreeLevelHeight(50);
        viewTree.setTreeNodesDistance(100);
        viewTree.calculateCoordinates();
        ArrayList<ParseTreeCollection> trees = new ArrayList<ParseTreeCollection>();
        ParseTreeCollection viewTreeContainer = new ParseTreeCollection(
                viewTree, viewTree, "", null, null, false);
        trees.add(viewTreeContainer);
        XMLTreeDisplay display = new XMLTreeDisplay(trees);
        display.setSize(1000, 750);
        display.setLocation(0, 0);
        display.setVisible(true);
    }

}
