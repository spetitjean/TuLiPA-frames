package de.duesseldorf.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.duesseldorf.rrg.RRG;
import de.duesseldorf.rrg.RRGTree;
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
        Set<RRGTree> trees = new HashSet<RRGTree>();

        Element rrgGramDocRoot = rrgGramDoc.getDocumentElement();
        NodeList grammarEntries = rrgGramDocRoot
                .getElementsByTagName(XMLRRGTag.ENTRY.StringVal());
        // iterate over all grammar entries
        for (int i = 0; i < grammarEntries.getLength(); i++) {
            Element ithEntrie = (Element) grammarEntries.item(i);
            Element tree = (Element) ithEntrie
                    .getElementsByTagName(XMLRRGTag.TREE.StringVal()).item(0);

            // process semantics, family etc. here

            RRGTree syntaxTree = XMLRRGTreeRetriever.retrieveTree(tree);

            // debug
            System.out.println(syntaxTree.toString() + "\n\n\n");
            trees.add(syntaxTree);
        }
        return new RRG(trees);
    }

}
