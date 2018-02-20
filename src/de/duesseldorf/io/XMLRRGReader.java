package de.duesseldorf.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.w3c.dom.Document;

import de.duesseldorf.rrg.RRGTree;
import de.tuebingen.tree.Node;
import de.tuebingen.util.XMLUtilities;

public class XMLRRGReader extends FileReader {

    // A representation of our grammar
    private Document rrgGramDoc;

    public XMLRRGReader(File rrgGrammar) throws FileNotFoundException {
        super(rrgGrammar);
        rrgGramDoc = XMLUtilities.parseXMLFile(rrgGrammar, false);
    }

    public RRGTree retrieveTree() {
        Node treeroot = retrieveNode();

        return new RRGTree(treeroot);
    }

    private Node retrieveNode() {
        return null;
    }
}
