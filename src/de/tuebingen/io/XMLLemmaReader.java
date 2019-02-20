/*
 *  File XMLLemmaReader.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *     David Arps <david.arps@hhu.de>
 *     Simon Petitjean <petitjean@phil.hhu.de>
 *          
 *  Copyright:
 *     Yannick Parmentier, 2007
 *     David Arps, 2017
 *     Simon Petitjean, 2017
 *
 *  Last modified:
 *     2017
 *
 *  This file is part of the TuLiPA-frames system
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
package de.tuebingen.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.duesseldorf.frames.Fs;
import de.duesseldorf.frames.Value;
import de.duesseldorf.io.XMLGrammarReadingTools;
import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.lexicon.Anchor;
import de.tuebingen.lexicon.CoAnchor;
import de.tuebingen.lexicon.Equation;
import de.tuebingen.lexicon.Lemma;
import de.tuebingen.lexicon.LexSem;
import de.tuebingen.util.XMLUtilities;

/**
 * 
 * @author parmenti
 *
 */
public class XMLLemmaReader extends FileReader {

    private File lemmaFile;
    static Document lemmaDoc;

    public XMLLemmaReader(File lemma) throws FileNotFoundException {
        /**
         * Generate a parser for an XML file containing an XMG MC-TAG grammar
         * 
         * @param lemma
         *            the XML file
         */
        super(lemma);
        this.lemmaFile = lemma;
        lemmaDoc = XMLUtilities.parseXMLFile(lemmaFile, false);
    }

    public Map<String, List<Lemma>> getLemmas() {

        Map<String, List<Lemma>> lemmas = new HashMap<String, List<Lemma>>();
        Element root = lemmaDoc.getDocumentElement();
        NodeList l = root.getElementsByTagName("lemmas");
        Element e = (Element) l.item(0);

        NodeList ll = e.getChildNodes();

        for (int i = 0; i < ll.getLength(); i++) {
            Node n = ll.item(i);
            // according to the DTD, the XML chidlren
            // of tag is lemma
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                if (el.getTagName().equals("lemma")) {
                    String name = el.getAttribute("name");
                    String cat = el.getAttribute("cat");
                    Lemma lemma = new Lemma(name, cat);
                    lemma.setAnchors(getAnchors(el));

                    List<Lemma> llemma = lemmas.get(lemma.getName());
                    if (llemma == null) {
                        llemma = new LinkedList<Lemma>();
                    }
                    llemma.add(lemma);
                    lemmas.put(lemma.getName(), llemma);
                }
            }
        }
        return lemmas;
    }

    public static List<Anchor> getAnchors(Element e) {
        List<Anchor> ancs = new LinkedList<Anchor>();

        NodeList l = e.getChildNodes();

        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                if (el.getTagName().equals("anchor")) {
                    String tid = el.getAttribute("tree_id");

                    try {
                        Pattern p = Pattern.compile("name=([\\w-_]+)");
                        // System.out.println(tid);
                        Matcher m = p.matcher(tid);
                        boolean a = m.find();
                        if (a) {
                            // System.out.println("--"+m.group(1));
                            tid = m.group(1);
                        }
                    } catch (PatternSyntaxException pse) {
                        System.out.println(pse.getDescription());
                    } catch (IllegalStateException ise) {
                        System.out.println(ise.toString());
                    }

                    Anchor anc = new Anchor(tid);
                    anc.setCoanchors(getCoAnchors(el));
                    anc.setEquations(getEquations(el));
                    anc.setFilter(getFilter(el));
                    anc.setSemantics(getSem(el));
                    ancs.add(anc);
                }
            }
        }
        return ancs;
    }

    public static List<LexSem> getSem(Element e) {
        List<LexSem> ls = null;
        NodeList l = e.getChildNodes();

        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                if (el.getTagName().equals("sem")) {
                    ls = XMLLemmaReader.getLexSem(el);
                }
            }
        }
        return ls;
    }

    public static List<LexSem> getLexSem(Element e) {
        List<LexSem> ls = new LinkedList<LexSem>();
        NodeList l = e.getChildNodes();

        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                if (el.getTagName().equals("semclass")) {
                    ls.add(XMLLemmaReader.getALexSem(el));
                }
            }
        }
        return ls;
    }

    public static LexSem getALexSem(Element e) {
        LexSem lexsem = new LexSem(e.getAttribute("name"));
        Fs fs = null;
        NodeList l = e.getChildNodes();

        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                if (el.getTagName().equals("args")) {
                    fs = XMLGrammarReadingTools.getFeats(el,
                            XMLTTMCTAGReader.FROM_OTHER,
                            new Hashtable<String, Value>(), new NameFactory());
                }
            }
        }
        lexsem.setArgs(fs);
        return lexsem;
    }

    public static Fs getFilter(Element e) {
        Fs fs = null;
        NodeList l = e.getChildNodes();

        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                if (el.getTagName().equals("filter")) {
                    fs = XMLGrammarReadingTools.getNarg(el,
                            XMLTTMCTAGReader.FROM_OTHER, new NameFactory());
                }
            }
        }
        return fs;
    }

    public static List<CoAnchor> getCoAnchors(Element e) {
        List<CoAnchor> coancs = new LinkedList<CoAnchor>();
        NodeList l = e.getChildNodes();

        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                if (el.getTagName().equals("coanchor")) {
                    String nid = el.getAttribute("node_id");
                    CoAnchor coanc = new CoAnchor(nid);
                    coanc.setLex(getLex(el));
                    String cat = el.getAttribute("cat");
                    coanc.setCat(cat);
                    coancs.add(coanc);
                }
            }
        }
        return coancs;
    }

    public static List<Equation> getEquations(Element e) {
        List<Equation> equas = new LinkedList<Equation>();
        NodeList l = e.getChildNodes();

        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                if (el.getTagName().equals("equation")) {
                    String t = el.getAttribute("type");
                    String nid = el.getAttribute("node_id");
                    Equation eq = new Equation(t, nid);
                    eq.setFeatures(XMLGrammarReadingTools.getNarg(el,
                            XMLTTMCTAGReader.FROM_OTHER, new NameFactory()));
                    equas.add(eq);
                }
            }
        }
        return equas;
    }

    public static List<String> getLex(Element e) {
        List<String> lexs = new LinkedList<String>();
        NodeList l = e.getElementsByTagName("lex");

        for (int i = 0; i < l.getLength(); i++) {
            Element el = (Element) l.item(i);

            if (el.getTagName().equals("lex")) {
                lexs.add(el.getTextContent());
            }
        }
        return lexs;
    }
}
