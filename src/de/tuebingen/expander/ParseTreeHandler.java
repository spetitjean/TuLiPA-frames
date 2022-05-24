/*
 *  File ParseTreeHandler.java
 *
 *  Authors:
 *     Johannes Dellert  <johannes.dellert@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Johannes Dellert, 2008
 *
 *  Last modified:
 *     Wed Jan 30 11:49:58 CET 2008
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
package de.tuebingen.expander;
/**
 * A class to extract the parse trees from parse forests;
 * Typically, only the static method extractDerivationTrees() will be used
 *
 * @author Johannes Dellert
 */

import org.w3c.dom.*;

import javax.xml.parsers.*;

import java.util.*;

import java.io.*;

//import de.tuebingen.gui.*;

public class ParseTreeHandler {
    HashMap<String, Rule> rules;
    ArrayList<Node> derivationTrees;

    boolean verbose = false;

    public ParseTreeHandler(Document forest) {
        rules = new HashMap<String, Rule>();
        derivationTrees = new ArrayList<Node>();
    }

    public void expandForest(Document forest, Document D) {
        //retrieve start rule IDs
        ArrayList<String> startRules = new ArrayList<String>();
        NodeList startRuleNodes = forest.getElementsByTagName("start_rule");
        for (int i = 0; i < startRuleNodes.getLength(); i++) {
            startRules.add(startRuleNodes.item(i).getAttributes().getNamedItem("id").getNodeValue());
        }
        //read in rules
        NodeList ruleNodes = forest.getElementsByTagName("rule");
        for (int i = 0; i < ruleNodes.getLength(); i++) {
            Node ruleNode = ruleNodes.item(i);
            Rule newRule = new Rule(ruleNode);
            rules.put(newRule.id, newRule);
        }

        for (String s : startRules) {
            Rule startRule = rules.get(s);
            Element startElement = D.createElement("tree");
            startElement.setAttribute("id", startRule.treeName);
            ArrayList<Element> trees = startRule.apply(startElement, D, rules, 0, verbose);
            derivationTrees.addAll(trees);
        }
    }

    public static Document extractDerivationTrees(Document doc) {
        //make a PTH out of the document by extracting all possible histories
        ParseTreeHandler pth = new ParseTreeHandler(doc);
        Document D = null;
        try {
            //build the output document with "forest" as document element tag
            D = DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation().createDocument(null, "forest", null);
            //convert all the histories to derivation trees
            pth.expandForest(doc, D);
            //append the extracted derivation trees as children to the output document
            System.err.println("Number of derivation trees " + pth.derivationTrees.size());
            for (Node root : pth.derivationTrees) {
                Node startNode = D.createElement("start");
                Node fakeSubstNode = D.createElement("subst");
                fakeSubstNode.appendChild(root);
                startNode.appendChild(fakeSubstNode);
                D.getDocumentElement().appendChild(startNode);
            }
        } catch (Exception e) {
            System.out.println("Error while building parse tree XML");
            System.out.println(e.getMessage());
            StackTraceElement[] stack = e.getStackTrace();
            for (int i = 0; i < stack.length; i++) {
                System.out.println(stack[i]);
            }
            System.exit(0);
        }
        return D;
    }

    //for testing purposes
    public static void main(String[] args) {
        try {
            File testFile = new File("/tmp/a.xml");
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(testFile);
            //make a PTH out of the document by extracting all possible histories
            Document D = ParseTreeHandler.extractDerivationTrees(doc);
            System.out.println(DOMWriter.documentToString(D));
        } catch (Exception e) {
            System.err.println("Error while reading economy.xml: ");
            System.err.println(e.getMessage());
            for (StackTraceElement s : e.getStackTrace()) {
                System.err.println(s.toString());
            }
        }
    }

}
