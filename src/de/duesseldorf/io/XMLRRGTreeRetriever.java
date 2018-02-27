package de.duesseldorf.io;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.RRGTree;
import de.duesseldorf.rrg.RRGTreeTools;
import de.tuebingen.tree.Node;

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

        // give the tree its gorn address:
        RRGTreeTools.initGornAddresses((RRGNode) treeRoot);
        return new RRGTree(treeRoot);
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
        Element narg = (Element) root
                .getElementsByTagName(XMLRRGTag.NARG.StringVal()).item(0);
        Element fs = (Element) narg
                .getElementsByTagName(XMLRRGTag.FEATURESTRUCTURE.StringVal())
                .item(0);
        Element f = (Element) fs
                .getElementsByTagName(XMLRRGTag.FEATURE.StringVal()).item(0);
        if (f.getAttribute(XMLRRGTag.NAME.StringVal())
                .equals(XMLRRGTag.CAT.StringVal())) {
            String cat = ((Element) f
                    .getElementsByTagName(XMLRRGTag.SYM.StringVal()).item(0))
                            .getAttribute(XMLRRGTag.VALUE.StringVal());
            return cat;
        } else {
            System.err.println("Error while retrieving the category of node "
                    + root.getAttribute(XMLRRGTag.NAME.StringVal()));
            return null;
        }
    }

    /**
     * 
     * 
     * @return The {@code RRGNodeType} of the {@code node} parameter, which must
     *         be an Element representing a node in a syntactic tree
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
