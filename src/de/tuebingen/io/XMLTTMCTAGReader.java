/*
 *  File XMLTTMCTAGReader.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *     David Arps <david.arps@hhu.de>
 *     Simon Petitjean <petitjean@phil.hhu.de>
 *     
 *  Copyright:
 *     David Arps, 2017
 *     Simon Petitjean, 2017
 *     Yannick Parmentier, 2007
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
/**
 * Class for loading an MCTAG grammar in XML format
 * (following the DTD
 * 
 * @author parmenti
 * 
 */
package de.tuebingen.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.duesseldorf.frames.Frame;
import de.duesseldorf.frames.Relation;
import de.duesseldorf.frames.Type;
import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.tag.Fs;
import de.tuebingen.tag.SemDom;
import de.tuebingen.tag.SemLit;
import de.tuebingen.tag.SemPred;
import de.tuebingen.tag.TagNode;
import de.tuebingen.tag.TagTree;
import de.tuebingen.tag.Tuple;
import de.tuebingen.tag.Value;
import de.tuebingen.util.XMLUtilities;

public class XMLTTMCTAGReader extends FileReader {

    // integers to check if we're processing a top fs, a bot fs or no fs.
    public static final int NOFS = -1;
    public static final int TOP = 0;
    public static final int BOT = 1;
    // integers to distinguish node features from other features (interface,
    // equations, etc)
    public static final int FROM_NODE = 0;
    public static final int FROM_OTHER = 1;

    private File grammarFile;
    private List<Boolean> needsAnchoring;
    static Document gramDoc;

    /**
     * Generate a parser for an XML file containing an XMG MC-TAG grammar
     * 
     * @param grammar
     *            the XML file
     */
    public XMLTTMCTAGReader(File grammar) throws FileNotFoundException {
        super(grammar);
        this.grammarFile = grammar;
        gramDoc = XMLUtilities.parseXMLFile(grammarFile, false);
        needsAnchoring = new LinkedList<Boolean>();
    }

    public Map<String, List<Tuple>> getFrames() {
        Map<String, List<Tuple>> sets = new HashMap<String, List<Tuple>>();

        Element root = gramDoc.getDocumentElement();
        NodeList l = root.getChildNodes();

        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            // according to the DTD, the XML chidlren
            // of mcgrammar are either entry (single tree) or mcset
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String id = null;

                Tuple t = null;
                Element e = (Element) n;
                NameFactory nf = new NameFactory();
                if (e.getTagName().equals("entry")) {
                    id = e.getAttribute("name");
                    t = new Tuple("tuple-" + id);
                    TagTree ttree = getEntry(e, needsAnchoring, nf);
                    ttree.setTupleId(id);
                    ttree.setIsHead(true);
                    t.setHead(ttree);
                    updateHash(sets, t.getHead().getFamily(), t);
                    // System.out.println("TTREE: "+ttree.getFrameSem());
                    // System.out.println("IFACE: "+ttree.getIface());
                } else if (e.getTagName().equals("mcset")) {
                    id = e.getAttribute("id");
                    t = new Tuple(id);
                    LinkedList<TagTree> trees = (LinkedList<TagTree>) getTrees(
                            e, id, needsAnchoring, nf);
                    // here head is selected from trees
                    // (it is in 1st position)
                    t.setHead(trees.poll());
                    t.setArguments(trees);
                    updateHash(sets, t.getHead().getFamily(), t);
                }
                // we update the tuple's family
                if (t != null) {
                    t.setFamily(t.getHead().getFamily());
                }
            }
        }
        return sets;
    }

    /**
     * Retrieve the tuples Object from the DOM document
     * corresponding to the input XML file
     * 
     * @author parmenti
     * 
     * @return a list of Tuple Objects
     * 
     */
    public Map<String, List<Tuple>> getTuples() {
        Map<String, List<Tuple>> sets = new HashMap<String, List<Tuple>>();
        Element root = gramDoc.getDocumentElement();
        NodeList l = root.getChildNodes();

        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            // according to the DTD, the XML chidlren
            // of mcgrammar are either entry (single tree) or mcset
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String id = null;

                Tuple t = null;
                Element e = (Element) n;
                NameFactory nf = new NameFactory();

                if (e.getTagName().equals("entry")) {
                    id = e.getAttribute("name");
                    t = new Tuple("tuple-" + id);

                    // System.err.println("Using a new NameFactory for " + id);

                    TagTree ttree = getEntry(e, needsAnchoring, nf);
                    ttree.setTupleId(id);
                    ttree.setIsHead(true);
                    t.setHead(ttree);
                    updateHash(sets, t.getHead().getFamily(), t);
                } else if (e.getTagName().equals("mcset")) {
                    id = e.getAttribute("id");
                    t = new Tuple(id);
                    LinkedList<TagTree> trees = (LinkedList<TagTree>) getTrees(
                            e, id, needsAnchoring, nf);
                    // here head is selected from trees
                    // (it is in 1st position)
                    t.setHead(trees.poll());
                    t.setArguments(trees);
                    updateHash(sets, t.getHead().getFamily(), t);
                }
                // we update the tuple's family
                if (t != null) {
                    t.setFamily(t.getHead().getFamily());
                }
            }
        }
        return sets;
    }

    public static void updateHash(Map<String, List<Tuple>> tuples, String key,
            Tuple t) {
        List<Tuple> ltu = tuples.get(key);
        if (ltu == null) {
            ltu = new LinkedList<Tuple>();
        }
        ltu.add(t);
        tuples.put(key, ltu);
    }

    public static List<TagTree> getTrees(Element e, String tupleId,
            List<Boolean> na, NameFactory nf) {
        /**
         * Extract trees inside a given mcset (XML Element)
         *
         * @param e
         *            Element from which trees has to be extracted
         * 
         * @return a list of TagTrees
         * 
         */
        List<TagTree> res = new LinkedList<TagTree>();

        NodeList l = e.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            // each element in l is an Entry XML tag
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                TagTree t = getEntry(el, na, nf);
                t.setTupleId(tupleId);
                // head is the first element of the list
                if (el.getAttribute("type").equals("anc")) {
                    res.add(0, t);
                } else {
                    // only the head of the set contains semantic formulas
                    // otherwise, we duplicate the semantics
                    t.setSem(new LinkedList<SemLit>());
                    res.add(t);
                }
            }
        }
        return res;
    }

    /**
     * Process an entry XML tag and extract a TagTree
     * 
     * @param e
     *            the DOM Element corresponding to the entry
     */
    public static TagTree getEntry(Element e, List<Boolean> na,
            NameFactory nf) {
        TagTree res = new TagTree();
        // 1. Processing of the tree
        // System.err.println("Tree part ");

        NodeList l = e.getElementsByTagName("tree");
        Element el1 = (Element) l.item(0);
        res = getATree(el1, na, nf);
        // 2. Processing of the trace
        l = e.getElementsByTagName("class");
        List<String> trace = new LinkedList<String>();
        for (int i = 0; i < l.getLength(); i++) {
            Element el = (Element) l.item(i);
            trace.add(el.getTextContent());
        }
        // Debug
        // for (String string : trace) {
        // System.out.print(string);
        // }
        // System.out.println("...trace");
        res.setTrace((List<String>) trace);
        // 3. Processing of the interface
        l = e.getElementsByTagName("interface");
        Fs iface = getNarg((Element) l.item(0), FROM_OTHER, nf);
        res.setIface(iface);
        // 4. Processing of the family name
        l = e.getElementsByTagName("family");
        Element el2 = (Element) l.item(0);
        String family = el2.getTextContent();
        res.setFamily(family);
        // 5. Processing of the frames

        // System.err.println("Frame part ");

        l = e.getElementsByTagName("frame");

        if (l.getLength() > 0) {
            List<Fs> framefss = new ArrayList<Fs>();
            Set<Relation> framerels = new HashSet<Relation>();

            Element frameEl = (Element) l.item(0);
            NodeList frameEls = frameEl.getChildNodes();
            for (int i = 0; i < frameEls.getLength(); i++) {
                if (frameEls.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element ithFrameEl = (Element) frameEls.item(i);
                    Hashtable<String, Value> toAdd = new Hashtable<String, Value>();
                    if (ithFrameEl.getTagName().equals("fs")) {
                        Fs framefs = getFeats(ithFrameEl, NOFS, toAdd, nf);
                        framefss.add(framefs);
                        // res.concatFrames(framefs);
                    } else if (ithFrameEl.getTagName().equals("relation")) {
                        Relation rel = getRelation(ithFrameEl, toAdd, nf);
                        framerels.add(rel);
                    }
                }
            }
            Frame frameSem = new Frame(framefss, framerels);
            // System.out.println(
            // "frameSem in XMLTTMCTAGREADER:\n" + frameSem.toString());
            res.setFrameSem(frameSem);
	    //System.out.println("Frame from XMLTTMCTAGReader: " + frameSem);
        }
        // 6. Processing of the semantics
        // to ensure that the semantics of a TagTree is not null, it might
        // get replaced with the contents of the frames
        l = e.getElementsByTagName("semantics");
        List<SemLit> semrepr = new LinkedList<SemLit>();
        Element el3 = (Element) l.item(0);
        if (el3 != null) {
            semrepr = getSemantics(el3, nf);
        }
        // } else {
        // semrepr = getSemantics(el4, nf);
        // }
        res.setSem(semrepr);

        return res;
    }

    /**
     * A relation element has an attribute with the relation name and daughters
     * for each of the arguments
     * 
     * @param n
     * @return
     */
    private static Relation getRelation(Element n,
            Hashtable<String, Value> toAdd, NameFactory nf) {
        String name = n.getAttribute("name");
        List<Value> arguments = new LinkedList<Value>();

        NodeList syms = n.getElementsByTagName("sym");
        for (int i = 0; i < syms.getLength(); i++) {
            Element sym = (Element) syms.item(i);
            String varname = sym.getAttribute("varname");
            Value val = getSingleValue(sym, nf);
            arguments.add(val);
        }
        return new Relation(name, arguments);
    }

    /**
     * Function used to read the XML semantic representation associated with a
     * Tree
     * 
     * @param e
     * @return the list of semantic literals labelling the tree
     */
    public static List<SemLit> getSemantics(Element e, NameFactory nf) {
        List<SemLit> semrepr = new LinkedList<SemLit>();
        NodeList l = e.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                semrepr.add(getSem(el, nf));
            }
        }
        return semrepr;
    }

    public static SemLit getSem(Element e, NameFactory nf) {
        SemLit sem = null;
        if (e.getTagName().equals("literal")) {
            sem = getLit(e, nf);
        } else if (e.getTagName().equals("sym")) {
            sem = getSingleValue(e, nf);
        } else if (e.getTagName().equals("semdominance")) {
            sem = getDom(e, nf);
        }
        // added this part to try and get fs in frames
        else if (e.getTagName().equals("fs")) {
            Fs feats = getNarg(e, FROM_OTHER, nf);
            sem = new Value(feats);
        }
        // originally no else -> only 3 types of tags allowed
        return sem;
    }

    public static SemLit getLit(Element e, NameFactory nf) {
        SemLit sem = new SemPred();
        List<Value> semargs = new LinkedList<Value>();

        NodeList l = e.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                if (el.getTagName().equals("label")) {
                    Value val = getVal(el, NOFS, new Hashtable<String, Value>(),
                            "", nf);
                    ((SemPred) sem).setLabel(val);
                } else if (el.getTagName().equals("predicate")) {
                    ((SemPred) sem).setPred(getVal(el, NOFS,
                            new Hashtable<String, Value>(), "", nf));
                } else if (el.getTagName().equals("arg")) {
                    Value semval = getSemArg(el, nf);
                    semargs.add(semval);
                }
            }
        }
        ((SemPred) sem).setArgs(semargs);

        return sem;
    }

    public static Value getSemArg(Element e, NameFactory nf) {
        Value semval = null;

        NodeList l = e.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                if (el.getTagName().equals("fs")) {
                    Fs feats = getNarg(el, FROM_OTHER, nf);
                    semval = new Value(feats);
                } else if (el.getTagName().equals("sym")) {
                    semval = getSingleValue(el, nf);
                }
            }
        }

        return semval;
    }

    public static SemLit getDom(Element e, NameFactory nf) {
        SemLit sem = new SemDom();

        int argnum = 1;
        NodeList l = e.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                if (el.getTagName().equals("sym")) {
                    Value semval = getSingleValue(el, nf);
                    switch (argnum) {
                    case 1:
                        ((SemDom) sem).setArg1(semval);
                        argnum++;
                        break;
                    case 2:
                        ((SemDom) sem).setArg2(semval);
                        argnum++;
                        break;
                    default:// skip
                    }
                }
            }
        }

        return sem;
    }

    /**
     * Process a tree XML tag and extract a TagTree
     * 
     * @param e
     *            the DOM Element corresponding to a tree
     * 
     * @return the TagTree object
     */
    public static TagTree getATree(Element e, List<Boolean> na,
            NameFactory nf) {
        TagTree res = new TagTree(e.getAttribute("id"));
        NodeList l = e.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                // el is the root of the tree
                res.setRoot(getNode(el, na, nf));
            }
        }
        // after building the tree, we can find both its anchor and its foot (if
        // any)
        if (res.getRoot() != null) {
            res.findMarks(res.getRoot(), "0");
        }
        return res;
    }

    public static de.tuebingen.tree.Node getNode(Element e, List<Boolean> na,
            NameFactory nf) {
        /**
         * Process (recursively) a node XML tag and extract a TagNode
         * 
         * @param e
         *            the DOM Element corresponding to a node
         * 
         */
        TagNode res = new TagNode();
        LinkedList<de.tuebingen.tree.Node> children = new LinkedList<de.tuebingen.tree.Node>();

        String type = e.getAttribute("type");
        if (type.equals("")) {
            res.setType(TagNode.STD);
        } else if (type.equals("std")) {
            res.setType(TagNode.STD);
        } else if (type.equals("subst")) {
            res.setType(TagNode.SUBST);
        } else if (type.equals("lex")) {
            res.setType(TagNode.LEX);
        } else if (type.equals("nadj")) {
            res.setType(TagNode.NOADJ);
            res.setNoadj(true);
        } else if (type.equals("anchor")) {
            res.setType(TagNode.ANCHOR);
            na.add(true);
        } else if (type.equals("coanchor")) {
            res.setType(TagNode.COANCHOR);
        } else if (type.equals("foot")) {
            res.setType(TagNode.FOOT);
        } else if (type.equals("nadjanc")) {
            res.setType(TagNode.ANCHOR);
            res.setNoadj(true);
            na.add(true);
        } else if (type.equals("nadjcoanc")) {
            res.setType(TagNode.COANCHOR);
            res.setNoadj(true);
        }

        String name = e.getAttribute("name");
        if (!(name.equals(""))) {
            res.setName(name);
        }

        NodeList l = e.getChildNodes();

        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                if (el.getTagName().equals("node")) {
                    children.addLast(getNode(el, na, nf));
                } else if (el.getTagName().equals("narg")) {
                    Fs fs = getNarg(el, FROM_NODE, nf);
                    res.setLabel(fs);
                }
            }
        }
        if (children.size() > 0) {
            res.setChildren(children);
        }
        // after building the node, we can find her category
        res.findCategory();
        return res;
    }

    /**
     * Process a narg XML tag to extract a TagNode label
     * 
     * @param e
     *            the DOM Element corresponding to the feature structure
     * 
     */
    public static Fs getNarg(Element e, int from, NameFactory nf) {
        Fs res = null;
        try {
            NodeList l = e.getChildNodes();

            // we declare the hash that will be used for features
            // that have to be added to both top and bot
            Hashtable<String, Value> toAdd = new Hashtable<String, Value>();
            for (int i = 0; i < l.getLength(); i++) {
                Node n = l.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) n;
                    if (el.getTagName().equals("fs")) {
                        res = getFeats(el, NOFS, toAdd, nf);
                    }
                }
            }
            if (from == FROM_NODE && toAdd.size() > 0) {
                // we post-process the features to add
                Value top = res.getFeat("top");
                if (top == null) {
                    top = new Value(new Fs(5));
                    res.setFeat("top", top);
                }
                Value bot = res.getFeat("bot");
                if (bot == null) {
                    bot = new Value(new Fs(5));
                    res.setFeat("bot", bot);
                }

                Set<String> keys = toAdd.keySet();
                Iterator<String> it = keys.iterator();
                while (it.hasNext()) {
                    String f = it.next();
                    if (!(top.getAvmVal().hasFeat(f))) {
                        top.getAvmVal().setFeat(f, toAdd.get(f));
                    }
                    if (!(bot.getAvmVal().hasFeat(f))) {
                        bot.getAvmVal().setFeat(f, toAdd.get(f));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return res;
    }

    /**
     * Process a fs XML tag to extract a TagNode label (class Fs)
     * 
     * @param e
     *            the DOM Element corresponding to the fs feature structure
     * 
     */
    public static Fs getFeats(Element e, int type,
            Hashtable<String, Value> toAdd, NameFactory nf) {
        // NB: an fs XML element has f element as children
        Fs res = null;
        String coref = e.getAttribute("coref");
        Value corefval = new Value(Value.VAR, nf.getName(coref));

        NodeList etypes = null;
        Value typevar = null;
        NodeList l = e.getChildNodes();

        // NodeList etypes = e.getElementsByTagName("type");
        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                if (el.getTagName().equals("ctype")) {
                    etypes = el.getElementsByTagName("type");
                    typevar = new Value(Value.VAR,
                            nf.getName(el.getAttribute("coref")));
                }
            }
        }

        Set<String> types = new HashSet<String>();
        if (etypes != null) {
            for (int i = 0; i < etypes.getLength(); i++) {
                Node n = etypes.item(i);
                Element el = (Element) n;
                // System.out.println("Type " + el.getAttribute("val"));
                types.add(el.getAttribute("val"));
            }
        }
        Type frame_type;
        if (typevar == null)
            frame_type = new Type(types);
        else
            frame_type = new Type(types, typevar);
        // System.out.println("Found a type: "+frame_type);

        res = new Fs(l.getLength(), frame_type, corefval);

        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                if (el.getTagName().equals("f")) {
                    String key = el.getAttribute("name");
                    Value val = null;
                    if (type == TOP || key.equals("top")) {
                        val = getVal(el, TOP, toAdd, key, nf);
                    } else if (type == BOT || key.equals("bot")) {
                        val = getVal(el, BOT, toAdd, key, nf);
                    } else {
                        val = getVal(el, NOFS, toAdd, key, nf);
                    }
                    res.setFeat(key, val);
                }
            }
        }
        return res;
    }

    /**
     * Method used to read Value objects from XML DOM elements
     * 
     * @param e
     *            the DOM element from which to extract the value
     * @param type
     *            BOT, TOP or NOFS
     * @param toAdd
     *            the mapping (name, value) to be added to both top and bot (for
     *            instance cat)
     * @param key
     *            the name of the feature whose value is being processed
     * @param tid
     *            the tree identifier (for new variable name)
     * @return a Value object
     */
    public static Value getVal(Element e, int type,
            Hashtable<String, Value> toAdd, String key, NameFactory nf) {
        /**
         * Process a f XML tag to extract a feature value (class Value)
         * 
         * @param e
         *            the DOM Element corresponding to the f feature
         */
        Value res = null;
        NodeList l = e.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                if (el.getTagName().equals("sym")) {
                    // the XML tag refers to a single value or variable
                    res = getSingleValue(el, nf);
                    if (type == NOFS) {
                        toAdd.put(key, res);
                    }
                } else if (el.getTagName().equals("vAlt")) {
                    // the XML tag refers to an atomic disjunction
                    NodeList l1 = el.getChildNodes();
                    LinkedList<Value> adisj = new LinkedList<Value>();
                    String theAdisjVar = nf.getName(el.getAttribute("coref"));

                    for (int j = 0; j < l1.getLength(); j++) {
                        Node n1 = l1.item(j);
                        Value v;
                        if (n1.getNodeType() == Node.ELEMENT_NODE) {
                            Element el1 = (Element) n1;

                            if (el1.getTagName().equals("sym")) {
                                v = getSingleValue(el1, nf);
                                adisj.add(v);
                            }
                        }
                    }
                    res = new Value(adisj, theAdisjVar);
                    if (type == NOFS) {
                        toAdd.put(key, res);
                    }
                } else {
                    // the XML tag is fs and refers to a feature structure
                    Value tmpres = new Value(getFeats(el, type, toAdd, nf));

                    if (tmpres.getAvmVal().getAVlist().isEmpty()
                            && tmpres.getAvmVal().getType().isEmpty()) {
                        // System.out.println(
                        // "Empty feature stucture as value of " + key);
                        res = tmpres.getAvmVal().getCoref();
                        // System.out.println("Coref is " + res);
                        toAdd.put(key, res);

                    } else {
                        res = tmpres;
                    }
                }
            }
        }
        return res;
    }

    public static Value getSingleValue(Element e, NameFactory nf) {
        /**
         * Process a sym XML tag to extract a feature value or variable
         * 
         * @param e
         *            the DOM Element corresponding to the sym tag
         * 
         */
        Value res = null;

        if (e.hasAttribute("value")) {
            res = new Value(Value.VAL, e.getAttribute("value"));
        } else {
            // the variable is renamed to be sure there is no variable name
            // conflict
            // with the lexicon
            String newName = nf.getName(e.getAttribute("varname"));
            res = new Value(Value.VAR, newName);
        }
        return res;
    }

    public boolean needsAnchoring() {
        // only true values are added
        return (needsAnchoring.size() > 0);
    }
}
