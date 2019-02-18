/*
 *  File LemmaContentHandler.java
 *
 *  Authors:
 *     Yannick Parmentier <parmenti@loria.fr>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Fri May 16 16:03:14 CEST 2008
 *
 *  This file is part of the Polarity Filter
 *
 *  The Polarity Filter is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  The Polarity Filter is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package fr.loria.io;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.LocatorImpl;

import de.duesseldorf.frames.Fs;
import de.duesseldorf.frames.Value;
import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.lexicon.Anchor;
import de.tuebingen.lexicon.CoAnchor;
import de.tuebingen.lexicon.Equation;
import de.tuebingen.lexicon.Lemma;
import de.tuebingen.lexicon.LexSem;

public class LemmaContentHandler implements ContentHandler {

    public static final int FILTER = 1;
    public static final int EQUATION = 2;

    private Locator locator;
    private boolean verbose = false;
    private Map<String, List<Lemma>> lemma;
    private String currentName;
    private Lemma currentLemma;
    private Anchor currentAnchor;
    private Equation currentEq;
    private LexSem currentSem;
    private CoAnchor coanchor;
    private List<Fs> currentFeats;
    private List<String> currentFeat;
    private LinkedList<Value> currentADisj = null;
    private int fsType = 0;
    private Map<String, List<String>> coanchors;

    private NameFactory nf;

    public LemmaContentHandler(boolean v) {
        super();
        locator = new LocatorImpl();
        verbose = v;
        lemma = new HashMap<String, List<Lemma>>();
        coanchors = new HashMap<String, List<String>>();
    }

    public void add2lemma(String key, Lemma value) {
        List<Lemma> ll = lemma.get(key);
        if (ll == null) {
            ll = new LinkedList<Lemma>();
            lemma.put(key, ll);
        }
        ll.add(value);
    }

    /**
     * @param locator
     *            to use
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator(Locator value) {
        locator = value;
    }

    /**
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
    }

    /**
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
    }

    /**
     * @param chosen
     *            namespace prefix
     * @param URI
     *            of the name-space
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
     *      java.lang.String)
     */
    public void startPrefixMapping(String prefix, String URI)
            throws SAXException {
    }

    /**
     * @param chose
     *            namespace prefix
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping(String prefix) throws SAXException {
    }

    /**
     * @param nameSpaceURI.
     * @param localName.
     * @param rawName
     *            for version 1.0 <code>nameSpaceURI + ":" + localName</code>
     * @throws SAXException
     *             (error such as not DTD compliant)
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String nameSpaceURI, String localName,
            String rawName, Attributes attributs) throws SAXException {

        if (!"".equals(nameSpaceURI) && verbose) {
            System.err.println(" Namespace detected : " + nameSpaceURI);
        }

        if (localName.equals("mcgrammar")) {
            // nothing to do
        } else if (localName.equals("lemmas")) {
            // nothing to do
        } else if (localName.equals("lemma")) {
            currentName = attributs.getValue("name");
            String cat = attributs.getValue("cat");
            currentLemma = new Lemma(currentName, cat);
            nf = new NameFactory(); // usually not needed (no variables in the
                                    // lemma entries)
        } else if (localName.equals("anchor")) {
            String tid = attributs.getValue("tree_id");

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
                System.err.println(pse.getDescription());
            } catch (IllegalStateException ise) {
                System.err.println(ise.toString());
            }
            currentAnchor = new Anchor(tid);
        } else if (localName.equals("filter")) {
            currentFeats = new LinkedList<Fs>();
            currentFeat = new LinkedList<String>();
            fsType = FILTER;
        } else if (localName.equals("equation")) {
            currentFeats = new LinkedList<Fs>();
            currentFeat = new LinkedList<String>();
            String type = attributs.getValue("type");
            String nid = attributs.getValue("node_id");
            currentEq = new Equation(type, nid);
            currentAnchor.addEquation(currentEq);
            fsType = EQUATION;
        } else if (localName.equals("sem")) {
            // nothing to do
        } else if (localName.equals("semclass")) {
            String name = attributs.getValue("name");
            currentSem = new LexSem(name);
            currentAnchor.addSem(currentSem);
        } else if (localName.equals("args")) {
            Fs fs = new Fs(10);
            currentFeats.add(fs);
        } else if (localName.equals("coanchor")) {
            String nid = attributs.getValue("node_id");
            coanchor = new CoAnchor(nid);
        } else if (localName.equals("fs")) {
            Fs fs = new Fs(10);
            currentFeats.add(fs);
        } else if (localName.equals("f")) {
            String name = attributs.getValue("name");
            currentFeat.add(name);
        } else if (localName.equals("sym")) {
            Value val = null;
            if (attributs.getIndex("varname") != -1) {
                String varname = attributs.getValue("varname");
                String realVar = nf.getName(varname);
                val = new Value(Value.Kind.VAR, realVar);
            } else if (attributs.getIndex("value") != -1) {
                String cste = attributs.getValue("value");
                val = new Value(Value.Kind.VAL, cste);
            }
            if (currentADisj == null) {
                Fs feats = currentFeats.get(currentFeats.size() - 1);
                feats.setFeat(currentFeat.get(currentFeat.size() - 1), val);
            } else
                currentADisj.add(val);
        } else if (localName.equals("vAlt")) {
            currentADisj = new LinkedList<Value>();
        }
    }

    /**
     * Evenement recu a chaque fermeture de balise.
     * 
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void endElement(String nameSpaceURI, String localName,
            String rawName) throws SAXException {
        if (!"".equals(nameSpaceURI)) { // name space non null
            System.err.print(" Namespace detected : " + localName);
        }

        if (localName.equals("vAlt")) {
            Value val = new Value(currentADisj);
            Fs feats = currentFeats.get(currentFeats.size() - 1);
            feats.setFeat(currentFeat.get(currentFeat.size() - 1), val);
            currentADisj = null;
        } else if (localName.equals("args")) {
            currentSem.setArgs(currentFeats.get(currentFeats.size() - 1));
            currentFeats.remove(currentFeats.size() - 1);
        } else if (localName.equals("fs")) {
            if (currentFeats.size() == 1) { // extern fs -> attaches to
                                            // something
                switch (fsType) {
                case FILTER:
                    currentAnchor.setFilter(
                            currentFeats.get(currentFeats.size() - 1));
                    break;
                case EQUATION:
                    currentEq.setFeatures(
                            currentFeats.get(currentFeats.size() - 1));
                    break;
                }
                currentFeats.remove(currentFeats.size() - 1);
            } else { // intern fs -> is a feature value
                Fs feats = currentFeats.get(currentFeats.size() - 1);
                String f = currentFeat.get(currentFeat.size() - 1);
                currentFeats.remove(currentFeats.size() - 1);
                currentFeats.get(currentFeats.size() - 1).setFeat(f,
                        new Value(feats));
            }
        } else if (localName.equals("f")) {
            currentFeat.remove(currentFeat.size() - 1);
        } else if (localName.equals("anchor")) {
            currentLemma.addAnchor(new Anchor(currentAnchor));
            currentAnchor = null;
        } else if (localName.equals("lemma")) {
            this.add2lemma(currentName, new Lemma(currentLemma));
            currentLemma = null;
        } else if (localName.equals("coanchor")) {
            currentAnchor.addCoAnchor(new CoAnchor(coanchor));
            // we update the map of coanchors
            update(coanchors, coanchor.getLex().get(0), coanchor.getCat());
            coanchor = null;
        }
    }

    /**
     * for DATA
     * 
     * @param ch
     *            characters
     * @param start
     *            1st character to process
     * @param end
     *            last character to process
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int end) throws SAXException {
        // if (verbose)
        // System.err.println("#PCDATA : " + new String(ch, start, end));
        coanchor.addLex(new String(ch, start, end));
    }

    /**
     * @param ch
     *            characters
     * @param start
     * @param end
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, int end)
            throws SAXException {
        // if (verbose)
        // System.err.println(" useless white spaces : ..." + new String(ch,
        // start, end) + "...");
    }

    /**
     * @param target
     * @param data
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String,
     *      java.lang.String)
     */
    public void processingInstruction(String target, String data)
            throws SAXException {
    }

    /**
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    public void skippedEntity(String arg0) throws SAXException {
    }

    public static void update(Map<String, List<String>> table, String lex,
            String cat) {
        List<String> values = table.get(lex);
        if (!table.containsKey(lex)) {
            values = new LinkedList<String>();
            table.put(lex, values);
        }
        values.add(cat);
    }

    public Locator getLocator() {
        return locator;
    }

    public Map<String, List<Lemma>> getLemma() {
        return lemma;
    }

    public Map<String, List<String>> getCoanchors() {
        return coanchors;
    }

}
