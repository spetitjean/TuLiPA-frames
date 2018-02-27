package de.duesseldorf.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.duesseldorf.rrg.RRG;
import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.RRGTree;
import de.tuebingen.tag.Fs;
import de.tuebingen.tree.Node;
import de.tuebingen.util.XMLUtilities;

/**
 * 
 * @author david
 *         Reads a XMG2-style XML file of a RRG.
 * 
 *         Structure of the XML file, with method that extracts the component:
 *         <grammar>
 *         -<entry name="Family_ModelIndex">
 *         --<family>Family</>
 *         --<trace>
 *         --<class>...</>
 *         --<frame>if there is any, sem.representation</>
 *         --<tree id="Family_ModelIndex">
 *         ---<node type="NodeType" name="XMGVariables"> NodeType corresponds to
 *         {@code RRGNode.RRGNodeType}
 *         ----<narg>
 *         -----<fs coref="Coref-variable">
 *         ------<f name="x"> [X=Y] FS
 *         -------<sym value="Y"/>
 *         --<interface> link syntax and semantics
 */
public class XMLRRGReader extends FileReader {

    // Our grammar
    private Document rrgGramDoc;

    public XMLRRGReader(File rrgGrammar) throws FileNotFoundException {
        super(rrgGrammar);
        rrgGramDoc = XMLUtilities.parseXMLFile(rrgGrammar, false);
    }

    public RRG retrieveRRG() {
        Element rrgGramDocRoot = rrgGramDoc.getDocumentElement();
        NodeList grammarEntries = rrgGramDocRoot
                .getElementsByTagName(XMLRRGTag.ENTRY.StringVal());
        // iterate over all grammar entries
        for (int i = 0; i < 3; i++) {
            Element ithEntrie = (Element) grammarEntries.item(i);
            Element tree = (Element) ithEntrie
                    .getElementsByTagName(XMLRRGTag.TREE.StringVal()).item(0);

            // process semantics, family etc. here, maybe via

            retrieveTree(tree);
        }
        return new RRG();
    }

    /**
     * 
     * @param root
     *            Not the Element representing the root of the syntax tree, but
     *            the Element one level above
     * @return
     */
    private RRGTree retrieveTree(Element root) {
        Element syntacticTreeMother = (Element) root
                .getElementsByTagName(XMLRRGTag.NODE.StringVal()).item(0);
        Node treeRoot = recursivelyRetrieveTree(syntacticTreeMother);
        // debug:
        // System.out.println(treeRoot.getChildren().size());
        // System.out.println(RRGTreeTools.recursivelyPrintNode(treeRoot));

        // TODO: give the tree an id etc.
        return new RRGTree(treeRoot);
    }

    /**
     * 
     * @param root
     *            the root node of the (sub)tree
     * @return a (RRG) Node representation of the subtree
     */
    private Node recursivelyRetrieveTree(Element root) {
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

    private Node retrieveNode(Element root) {
        RRGNodeType type = findRRGNodeType(root);

        // do we need this name?
        String name = root.getAttribute(XMLRRGTag.NAME.StringVal());
        Fs narg = retrievenarg(root);

        Node treeRoot = new RRGNode(type, name);
        return treeRoot;
    }

    private Fs retrievenarg(Element root) {
        return null;
    }

    /**
     * 
     * 
     * @return The {@code RRGNodeType} of the {@code node} parameter, which must
     *         be an Element representing a node in a syntactic tree
     */
    private RRGNodeType findRRGNodeType(Element node) {
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
            System.out.print("Unknown node type when reading the grammar: "
                    + xmlNodeType + "tag: " + node.getTagName());

            return null;
        }

    }

}
