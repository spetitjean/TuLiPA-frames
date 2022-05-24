/*
 *  File XMLRCGReader.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@uni-tuebingen.de>
 *
 *  Copyright:
 *     Wolfgang Maier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 10:48:51 CEST 2007
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
/**
 * Class for loading an RCG in XML format
 *
 * @author wmaier
 */
package de.tuebingen.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.tuebingen.rcg.ArgContent;
import de.tuebingen.rcg.Argument;
import de.tuebingen.rcg.Clause;
import de.tuebingen.rcg.PredStringLabel;
import de.tuebingen.rcg.Predicate;
import de.tuebingen.rcg.RCG;
import de.tuebingen.util.XMLUtilities;

public class XMLRCGReader extends FileReader implements RCGReader {

    private File grammarFile;
    static Document gramDoc;

    /**
     * Generate a parser for an XML file containing an XMG RCG
     *
     * @param grammar
     *            the XML file
     */
    public XMLRCGReader(File grammar) throws FileNotFoundException {
        super(grammar);
        this.grammarFile = grammar;
        gramDoc = XMLUtilities.parseXMLFile(grammarFile, false);
    }


    private static ArgContent nodeToArgContent(Node ac) {
        ArgContent ret = null;
        String content = "";
        Element e = (Element) ac;
        if (e.getTagName().equals("range")) {
            String type = e.getAttribute("type");
            if (type.equals("const") || type.equals("var")) {
                content = e.getAttribute("content");
                int itype = type.equals("const") ? ArgContent.TERM : ArgContent.VAR;
                ret = new ArgContent(itype, content);
            } else if (type.equals("eps")) {
                ret = new ArgContent(ArgContent.EPSILON, "");
            } else if (type.equals("seq")) {
                List<ArgContent> argconts = new LinkedList<ArgContent>();
                NodeList argcontl = e.getChildNodes();
                for (int i = 0; i < argcontl.getLength(); ++i) {
                    Node n = argcontl.item(i);
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        if (((Element) n).getTagName().equals("range")) {
                            ArgContent a = nodeToArgContent(n);
                            argconts.add(a);
                        }
                    }
                }
                ret = new ArgContent(argconts);
            }
        }
        return ret;
    }

    private static Argument nodeToArgument(Node an) {
        Argument ret = null;
        Element e = (Element) an;
        if (e.getTagName().equals("range")) {
            ret = new Argument();
            // get the range type
            String type = e.getAttribute("type");
            if (type.equals("const") || type.equals("var")) {
                String content = e.getAttribute("content");
                int itype = type.equals("const") ? ArgContent.TERM : ArgContent.VAR;
                ret.addArg(new ArgContent(itype, content));
            } else if (type.equals("eps")) {
                ret.addArg(new ArgContent(ArgContent.EPSILON, ""));
            } else if (type.equals("seq")) {
                NodeList argcontl = e.getChildNodes();
                for (int i = 0; i < argcontl.getLength(); ++i) {
                    Node n = argcontl.item(i);
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        if (((Element) n).getTagName().equals("range")) {
                            ArgContent a = nodeToArgContent(n);
                            ret.addArg(a);
                        }
                    }
                }
            }
        }
        return ret;
    }

    private static Predicate nodeToPredicate(Node cn) {
        Predicate ret = new Predicate();
        Element e = (Element) cn;
        String predname = e.getAttribute("name");
        ret.setLabel(new PredStringLabel(predname));
        // top-level daughters of type "range" are the arguments
        NodeList argsl = e.getChildNodes();
        for (int i = 0; i < argsl.getLength(); ++i) {
            Node n = argsl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Argument a = nodeToArgument(n);
                if (a == null) {
                    ret = null;
                    break;
                }
                ret.addArg(a);
            }
        }
        return ret;
    }

    private static boolean isStartPredicate(Node cn) {
        Element e = (Element) cn;
        return e.hasAttribute("isstart") && e.getAttribute("isstart").equals("y");
    }

    public RCG getRCG() throws Exception {
        RCG ret = new RCG();
        Element root = gramDoc.getDocumentElement();
        NodeList clauselist = root.getElementsByTagName("clause");
        for (int i = 0; i < clauselist.getLength(); i++) {
            Node n = clauselist.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) n;
                // top level element may only be clause
                Clause c = new Clause();
                // get predicates. |pred| > 0 ensured by DTD.
                NodeList predl = e.getElementsByTagName("pred");
                // get the lhs predicate
                Node cn = predl.item(0);
                Predicate p = nodeToPredicate(cn);
                // check if we have the start predicate
                if (isStartPredicate(cn)) {
                    if (ret.startPredicateDefined()
                            && !ret.getStartPredicateLabel().equals(p.getLabel())) {
                        throw new Exception("Cannot read RCG: Grammar seems to contain > 1 start predicates");
                    } else {
                        ret.setStartPredicate(p.getLabel());
                    }
                }
                c.setLhs(p);
                // get the rhs predicates
                for (int j = 1; j < predl.getLength(); ++j) {
                    cn = predl.item(j);
                    p = nodeToPredicate(cn);
                    c.addToRhs(p);
                }
                if (!ret.addClause(c, null)) {
                    throw new Exception("Cannot add clause to RCG, maybe same clause with different arities.");
                }
            }
        }
        // is there a start predicate?
        if (!ret.startPredicateDefined()) {
            throw new Exception("Cannot read RCG: No start predicate found!");
        }
        return ret;
    }


    /*
     * just for testing
     */
    public static void main(String[] args) throws Exception {
        XMLRCGReader r = new XMLRCGReader(new File(""));
        RCG g = r.getRCG();
        System.out.println(g.toString());
        r.close();
    }
}