package de.duesseldorf.rrg.io;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.RRGTree;
import de.duesseldorf.rrg.RRGTreeTools;
import de.tuebingen.tree.Node;

/*
 *  File XMLRRGTreeRetriever.java
 *
 *  Authors:
 *     David Arps <david.arps@hhu.de
 *     
 *  Copyright:
 *     David Arps, 2018
 *
 *  This file is part of the TuLiPA system
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
public class XMLRRGTreeRetriever {

    /**
     * 
     * @param root
     *            Not the Element representing the root of the syntax tree, but
     *            the Element one level above
     * @return
     */
    public static RRGTree retrieveTree(Element root) {
        Element syntacticTreeMother = (Element) root
                .getElementsByTagName(XMLRRGTag.NODE.StringVal()).item(0);
        Node treeRoot = recursivelyRetrieveTree(syntacticTreeMother);
        // debug:
        // System.out.println(treeRoot.getChildren().size());
        // System.out.println(RRGTreeTools.recursivelyPrintNode(treeRoot));

        // TODO: give the tree an id etc.
        String id = root.getAttribute(XMLRRGTag.ID.StringVal());
        // System.out.println("ID: " + id);
        // give the tree its gorn address:
        RRGTreeTools.initGornAddresses((RRGNode) treeRoot);
        return new RRGTree(treeRoot, id);
    }

    /**
     * 
     * @param root
     *            the root node of the (sub)tree
     * @return a (RRG) Node representation of the subtree
     */
    private static Node recursivelyRetrieveTree(Element root) {
        // base case: process the root node of the subtree
        Node treeRoot = retrieveNode(root);
        // process all daughters of the XML-Element root, i.e. process the tree
        // from left to right, depth-first
        NodeList daughters = root.getChildNodes();
        for (int i = 0; i < daughters.getLength(); i++) {
            org.w3c.dom.Node ithchild = daughters.item(i);
            // first check
            // 1) whether the daughter is an element - all nodes are elements
            // 2) whether the daughter is a node - and not e.g. an narg
            if (ithchild.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                    && ithchild.getNodeName()
                            .equals(XMLRRGTag.NODE.StringVal())) {
                ((RRGNode) treeRoot).addRightmostChild(
                        recursivelyRetrieveTree((Element) daughters.item(i)));
            }
        }
        return treeRoot;
    }

    private static Node retrieveNode(Element root) {
        RRGNodeType type = findRRGNodeType(root);

        String category = retrieveCat(root);

        // do we need this name?
        String name = root.getAttribute(XMLRRGTag.NAME.StringVal());

        Node treeRoot = new RRGNode(type, name, category);
        return treeRoot;
    }

    /**
     * This method processes (in parts) the narg of a node. Might be of use
     * later!
     * 
     * @param root
     * @return the syntactic category or the terminal label of a node
     */
    private static String retrieveCat(Element root) {
        // the narg element
        Element narg = (Element) root
                .getElementsByTagName(XMLRRGTag.NARG.StringVal()).item(0);
        // the fs element
        Element fs = (Element) narg
                .getElementsByTagName(XMLRRGTag.FEATURESTRUCTURE.StringVal())
                .item(0);

        // walk through all the features of the fs and find the category
        NodeList features = fs
                .getElementsByTagName(XMLRRGTag.FEATURE.StringVal());
        for (int i = 0; i < features.getLength(); i++) {
            Element f = ((Element) features.item(i));
            if (f.getAttribute(XMLRRGTag.NAME.StringVal())
                    .equals(XMLRRGTag.CAT.StringVal())) {
                String cat = ((Element) f
                        .getElementsByTagName(XMLRRGTag.SYM.StringVal())
                        .item(0)).getAttribute(XMLRRGTag.VALUE.StringVal());
                return cat;
            }
        }

        // if no feature 'cat' was found:
        System.err.println("Error while retrieving the category of node "
                + root.getAttribute(XMLRRGTag.NAME.StringVal()));
        return null;

    }

    /**
     * @param node
     *            an Element representing a node in a syntactic tree
     * 
     * @return The {@code RRGNodeType} of the {@code node} parameter. This is
     *         the "internal" - non XML - node type that the parser works with
     */
    private static RRGNodeType findRRGNodeType(Element node) {
        String xmlNodeType = node.getAttribute(XMLRRGTag.TYPE.StringVal());

        if (xmlNodeType.equals(XMLRRGTag.XMLLEXNode.StringVal())) {
            return RRGNodeType.LEX;
        } else if (xmlNodeType.equals(XMLRRGTag.XMLSTDNode.StringVal())) {
            return RRGNodeType.STD;
        } else if (xmlNodeType.equals(XMLRRGTag.XMLANCHORNode.StringVal())) {
            return RRGNodeType.ANCHOR;
        } else if (xmlNodeType.equals(XMLRRGTag.XMLSUBSTNode.StringVal())) {
            return RRGNodeType.SUBST;
        } else if (xmlNodeType.equals(XMLRRGTag.XMLDDAUGHTERNode.StringVal())) {
            return RRGNodeType.DDAUGHTER;
        } else if (xmlNodeType
                .equals(XMLRRGTag.XMLSISADJFOOTNode.StringVal())) {
            return RRGNodeType.STAR;
        } else {
            System.err.println("Unknown node type when reading the grammar: "
                    + xmlNodeType + "tag: " + node.getTagName());
            return null;
        }
    }
}