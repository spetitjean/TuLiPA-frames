package de.duesseldorf.rrg.io;

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

import de.duesseldorf.frames.Fs;
import de.tuebingen.anchoring.NameFactory;
import de.duesseldorf.io.XMLGrammarReadingTools;

/**
 * File RRGParseTree.java
 * <p>
 * Authors:
 * David Arps <david.arps@hhu.de>
 * <p>
 * Copyright
 * David Arps, 2018
 *
 * @author david
 * Reads a XMG2-style XML file of a RRG.
 * <p>
 * Structure of the XML file, with method that extracts the component:
 * <grammar>
 * -<entry name="Family_ModelIndex">
 * --<family>Family</>
 * --<trace>
 * --<class>...</>
 * --<frame>if there is any, sem.representation</>
 * --<tree id="Family_ModelIndex">
 * ---<node type="NodeType" name="XMGVariables"> NodeType corresponds to
 * {@code RRGNode.RRGNodeType}
 * ----<narg>
 * -----<fs coref="Coref-variable">
 * ------<f name="x"> [X=Y] FS
 * -------<sym value="Y"/>
 * --<interface> link syntax and semantics
 * <p>
 * <p>
 * This file is part of the TuLiPA-frames system
 * https://github.com/spetitjean/TuLiPA-frames
 * <p>
 * <p>
 * TuLiPA is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * TuLiPA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
            NameFactory nf = new NameFactory();
            Element ithEntrie = (Element) grammarEntries.item(i);
            Element tree = (Element) ithEntrie
                    .getElementsByTagName(XMLRRGTag.TREE.StringVal()).item(0);

            // process semantics, family etc. here
            NodeList frameRoot = ithEntrie.getElementsByTagName(XMLRRGTag.FRAME.StringVal());


            RRGTree syntaxTree = XMLRRGTreeRetriever.retrieveTree(tree, frameRoot, nf);

            // process interface
            NodeList l = ithEntrie.getElementsByTagName("interface");
            Fs iface = XMLGrammarReadingTools.getNarg((Element) l.item(0),
                    1, nf);
            syntaxTree.setIface(iface);


            String fam = ithEntrie
                    .getElementsByTagName(XMLRRGTag.FAMILY.StringVal()).item(0)
                    .getTextContent();
            syntaxTree.setFamily(fam);
            // debug
            // System.out.println(syntaxTree.toString() + "\n\n\n");
            trees.add(syntaxTree);
        }
        return new RRG(trees);
    }
}
