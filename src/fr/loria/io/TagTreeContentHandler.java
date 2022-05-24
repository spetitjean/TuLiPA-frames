/*
 *  File TagTreeContentHandler.java
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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.LocatorImpl;

import de.duesseldorf.frames.Fs;
import de.duesseldorf.frames.Value;
import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.tag.SemDom;
import de.tuebingen.tag.SemLit;
import de.tuebingen.tag.SemPred;
import de.tuebingen.tag.TagNode;
import de.tuebingen.tag.TagTree;

public class TagTreeContentHandler implements ContentHandler {

    public static final int NONE = 0;
    public static final int FAMILY = 1;
    public static final int CLASS = 2;
    public static final int LABEL = 3;
    public static final int PRED = 4;
    public static final int ARGS = 5;
    public static final int DOM = 6;

    private Locator locator;
    private boolean verbose = false;
    private Map<String, TagTree> schema;
    private TagTree currentTree;
    private List<TagNode> currentNode; // stack of nodes (cf recursivity)
    private List<Fs> currentAVM; // stack of avms (cf recursivity)
    private List<String> currentFeat; // stack of features (cf recursivity)
    private LinkedList<Value> currentADisj = null;
    private List<SemLit> currentLit;
    private int dataType = NONE;
    private int semType = NONE;
    private int argNum = 0;
    private boolean iface = false;
    private boolean semantics = false;

    private NameFactory nf;

    public TagTreeContentHandler(boolean v) {
        super();
        locator = new LocatorImpl();
        verbose = v;
        schema = new HashMap<String, TagTree>();
    }

    /**
     * @param locator to use
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
     * @param chosen namespace prefix
     * @param URI    of the name-space
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
     * java.lang.String)
     */
    public void startPrefixMapping(String prefix, String URI)
            throws SAXException {
    }

    /**
     * @param chose namespace prefix
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping(String prefix) throws SAXException {
    }

    /**
     * @param nameSpaceURI.
     * @param localName.
     * @param rawName       for version 1.0 <code>nameSpaceURI + ":" + localName</code>
     * @throws SAXException (error such as not DTD compliant)
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
     * java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String nameSpaceURI, String localName,
                             String rawName, Attributes attributs) throws SAXException {

        if (!"".equals(nameSpaceURI) && verbose) {
            System.err.println(" Namespace detected : " + nameSpaceURI);
        }

        if (localName.equals("grammar")) {
            // nothing to do
        } else if (localName.equals("entry")) {
            // System.err.println("Processing " + attributs.getValue("name"));
            String id = attributs.getValue("name");
            currentTree = new TagTree(id);
            currentTree.setOriginalId(id);
            currentTree.setTupleId(id);
            schema.put(id, currentTree);
            nf = new NameFactory(); // one variable name space per entry
            currentNode = new LinkedList<TagNode>();
            currentAVM = new LinkedList<Fs>();
            currentFeat = new LinkedList<String>();
        } else if (localName.equals("family")) {
            dataType = FAMILY;
        } else if (localName.equals("trace")) {
            // nothing to do
        } else if (localName.equals("class")) {
            dataType = CLASS;
        } else if (localName.equals("tree")) {
            // nothing to do
        } else if (localName.equals("node")) {
            TagNode tn = new TagNode();
            String name = attributs.getValue("name");
            String type = attributs.getValue("type");
            if (type.equals("std"))
                tn.setType(TagNode.STD);
            else if (type.equals("anchor"))
                tn.setType(TagNode.ANCHOR);
            else if (type.equals("lex"))
                tn.setType(TagNode.LEX);
            else if (type.equals("subst"))
                tn.setType(TagNode.SUBST);
            else if (type.equals("coanchor"))
                tn.setType(TagNode.COANCHOR);
            else if (type.equals("foot"))
                tn.setType(TagNode.FOOT);
            else if (type.equals("nadj"))
                tn.setType(TagNode.NADJ);
            if (name != null)
                tn.setName(name);
            if (currentNode.size() > 0) {
                TagNode current = currentNode.get(currentNode.size() - 1);
                current.add2children(tn);
            } else
                currentTree.setRoot(tn);
            currentNode.add(tn);
        } else if (localName.equals("narg")) {
            // nothing to do
        } else if (localName.equals("fs")) {
            Fs fs = new Fs();
            currentAVM.add(fs);
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
            if (!semantics) { // we are not processing semantic info
                if (currentADisj == null) { // we are not processing an atomic
                    // disjunction
                    // we set the current feat of the current avm (stack of avms
                    // and of features)
                    Fs feats = currentAVM.get(currentAVM.size() - 1);
                    feats.setFeatWithoutReplace(
                            currentFeat.get(currentFeat.size() - 1), val);
                } else // otherwise we update the atomic disjunction
                    currentADisj.add(val);
            } else {
                switch (semType) {
                    case LABEL:
                        ((SemPred) currentLit.get(currentLit.size() - 1))
                                .setLabel(val);
                        break;
                    case PRED:
                        ((SemPred) currentLit.get(currentLit.size() - 1))
                                .setPred(val);
                        break;
                    case ARGS:
                        ((SemPred) currentLit.get(currentLit.size() - 1))
                                .addArg(val);
                        break;
                    case DOM:
                        switch (argNum) {
                            case 1:
                                ((SemDom) currentLit.get(currentLit.size() - 1))
                                        .setArg1(val);
                                break;
                            case 2:
                                ((SemDom) currentLit.get(currentLit.size() - 1))
                                        .setArg2(val);
                                break;
                        }
                        break;
                }
            }
        } else if (localName.equals("vAlt")) {
            currentADisj = new LinkedList<Value>();
            String coref = attributs.getValue("coref");
            currentADisj.add(new Value(Value.Kind.VAR, coref));
        } else if (localName.equals("semantics")) {
            semantics = true;
            currentLit = new LinkedList<SemLit>();
        } else if (localName.equals("literal")) {
            SemPred pred = new SemPred();
            String negated = attributs.getValue("negated");
            if (negated.equals("yes"))
                pred.setNegated(true);
            currentLit.add(pred);
        } else if (localName.equals("label")) {
            semType = LABEL;
        } else if (localName.equals("predicate")) {
            semType = PRED;
        } else if (localName.equals("arg")) {
            semType = ARGS;
            argNum++;
        } else if (localName.equals("semdominance")) {
            semType = DOM;
            SemDom sdom = new SemDom();
            currentLit.add(sdom);
        } else if (localName.equals("interface")) {
            iface = true;
        }
    }

    /**
     * Evenement recu a chaque fermeture de balise.
     *
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    public void endElement(String nameSpaceURI, String localName,
                           String rawName) throws SAXException {
        if (!"".equals(nameSpaceURI)) { // name space non null
            System.err.print(" Namespace detected : " + localName);
        }

        if (localName.equals("node")) {
            // add empty top and bot if missing
            Fs res = currentNode.get(currentNode.size() - 1).getLabel();
            Value top = res.getFeat("top");
            if (top == null) {
                top = new Value(new Fs());
                res.setFeatWithoutReplace("top", top);
            }
            Value bot = res.getFeat("bot");
            if (bot == null) {
                bot = new Value(new Fs());
                res.setFeatWithoutReplace("bot", bot);

            }
            currentNode.get(currentNode.size() - 1).findCategory(); // also
            // propagates
            // the cat
            // over the
            // feature
            // structure
            currentNode.remove(currentNode.size() - 1);
        } else if (localName.equals("vAlt")) {
            Value val = new Value(currentADisj);
            Fs feats = currentAVM.get(currentAVM.size() - 1);
            feats.setFeatWithoutReplace(currentFeat.get(currentFeat.size() - 1),
                    val);
            currentADisj = null;
        } else if (localName.equals("fs")) {
            if (!iface) {
                if (currentAVM.size() == 1) { // extern fs -> attaches to a node
                    TagNode current = currentNode.get(currentNode.size() - 1);
                    current.setLabel(currentAVM.get(currentAVM.size() - 1));
                    currentAVM.remove(currentAVM.size() - 1);
                } else { // intern fs -> is a feature value
                    Fs feats = currentAVM.get(currentAVM.size() - 1);
                    String f = currentFeat.get(currentFeat.size() - 1);
                    currentAVM.remove(currentAVM.size() - 1);
                    currentAVM.get(currentAVM.size() - 1)
                            .setFeatWithoutReplace(f, new Value(feats));
                }
            } else {
                currentTree.setIface(currentAVM.get(currentAVM.size() - 1));
                currentAVM.remove(currentAVM.size() - 1);
            }
        } else if (localName.equals("f")) {
            currentFeat.remove(currentFeat.size() - 1);
        } else if (localName.equals("semantics")) {
            currentTree.add2sem(currentLit);
            semantics = false;
        } else if (localName.equals("arg")) {
            argNum--;
        } else if (localName.equals("interface")) {
            iface = false;
        } else if (localName.equals("entry")) {
            currentTree.findMarks(currentTree.getRoot(), "0");
            currentTree = null;
        }
    }

    /**
     * for DATA
     *
     * @param ch    characters
     * @param start 1st character to process
     * @param end   last character to process
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int end) throws SAXException {
        String s = new String(ch, start, end);
        // if (verbose)
        // System.err.println("#PCDATA : " + s);

        switch (dataType) {
            case NONE:
                break;
            case FAMILY:
                if (currentTree != null) // cf the SAX filter
                    currentTree.setFamily(s);
                break;
            case CLASS:
                if (currentTree != null) // cf the SAX filter
                    currentTree.add2Trace(s);
                break;
        }
    }

    /**
     * @param ch    characters
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
     * java.lang.String)
     */
    public void processingInstruction(String target, String data)
            throws SAXException {
    }

    /**
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    public void skippedEntity(String arg0) throws SAXException {
    }

    public Locator getLocator() {
        return locator;
    }

    public Map<String, TagTree> getSchema() {
        return schema;
    }

}
