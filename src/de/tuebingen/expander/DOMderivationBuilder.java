/*
 *  File DOMderivationBuilder.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *     Johannes Dellert <johannes.dellert@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Yannick Parmentier, 2007
 *     Johannes Dellert, 2007
 *
 *  Last modified:
 *     Sa 20. Okt 17:41:58 CEST 2007
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
package de.tuebingen.expander;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.duesseldorf.frames.Frame;
import de.duesseldorf.frames.Fs;
import de.duesseldorf.frames.Relation;
import de.duesseldorf.frames.Type;
import de.duesseldorf.frames.Value;
import de.tuebingen.derive.DerivedTree;
import de.tuebingen.gui.ParseTreeCollection;
import de.tuebingen.tag.SemDom;
import de.tuebingen.tag.SemLit;
import de.tuebingen.tag.SemPred;

public class DOMderivationBuilder {

    private static Document derivDoc;
    private String sentence;
    private int parsecounter;

    public DOMderivationBuilder(String sentence) {
        this.sentence = sentence;
        parsecounter = 0;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder constructor;
        try {
            constructor = factory.newDocumentBuilder();
            derivDoc = constructor.newDocument();
            derivDoc.setXmlVersion("1.0");
            derivDoc.setXmlStandalone(false);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param viewTreesFromDOM
     * @param sentence
     * @return
     */
    public Document buildDOMderivationGrammar(
            ArrayList<ParseTreeCollection> viewTreesFromDOM) {

        Element root = derivDoc.createElement("grammar");

        for (ParseTreeCollection ptc : viewTreesFromDOM) {
            buildOneGrammarFormat(root,
                    ptc.getDerivationTree().getDomNodes().get(0),
                    ptc.getDerivedTree().getDomNodes().get(0),
                    ptc.getOriginalDerivedTree(), ptc.getSemantics(),
                    ptc.getSpecifiedSemantics(), ptc.getFrameSem());
        }
        derivDoc.appendChild(root);
        return derivDoc;
    }

    /**
     * @param all
     * @param sentence
     * @return a DOM Document containing all derivation trees and derived trees
     * for the sentence, in the standard old TuLiPA format.
     */
    public Document buildDOMderivation(ArrayList<ParseTreeCollection> all) {

        Element root = derivDoc.createElement("parses");
        root.setAttribute("sentence", sentence);
        for (ParseTreeCollection ptc : all) {
            buildOne(root, ptc.getDerivationTree().getDomNodes().get(0),
                    ptc.getDerivedTree().getDomNodes().get(0),
                    ptc.getOriginalDerivedTree(), ptc.getSemantics(),
                    ptc.getSpecifiedSemantics(), ptc.getFrameSem());
        }
        // finally we do not forget the root
        derivDoc.appendChild(root);
        return derivDoc;

    }

    public void buildOneGrammarFormat(Element mother, Node derivation,
                                      Node derived, DerivedTree dTree, List<SemLit> semantics,
                                      String[] specifiedSemantics, Frame frameSem) {
        // the entry with the derived tree
        Element parseDerivedEntry = derivDoc.createElement("entry");
        // Element parseDerivationEntry = derivDoc.createElement("entry"); //
        // the entry with the derivation tree

        parseDerivedEntry.setAttribute("name",
                sentence + "_" + parsecounter + "_derivedTree");
        // parseDerivationEntry.setAttribute("name", sentence + "_" +
        // parsecounter+"_derivationTree");

        parsecounter++;
        Element derivationTree = derivDoc.createElement("tree"); // you need
        // separate entries in the document for every tree
        Element derivedTree = derivDoc.createElement("tree");
        // Element semElem = derivDoc.createElement("semantics");
        // Element specSemElem = derivDoc.createElement("specified_semantics");
        Element family = derivDoc.createElement("family");
        Element f = derivDoc.createElement("frame");
        Element trace = derivDoc.createElement("trace");

        buildDerivationTree(derivationTree, derivation);
        buildTrace(trace, derivation);
        parseDerivedEntry.appendChild(trace);

        buildFamily(family);
        parseDerivedEntry.appendChild(family);
        // parseDerivationEntry.appendChild(family);

        buildDerivedTree(derivedTree, dTree, dTree.root, derived);
        parseDerivedEntry.appendChild(derivedTree);

        // buildSemantics(semElem, semantics);
        // parseDerivedEntry.appendChild(semElem);
        // parseDerivationEntry.appendChild(semElem);

        // buildSpecifiedSemantics(specSemElem, specifiedSemantics);
        // parseDerivedEntry.appendChild(specSemElem);
        // parseDerivationEntry.appendChild(specSemElem);

        buildFrames(f, frameSem);
        parseDerivedEntry.appendChild(f);

        mother.appendChild(parseDerivedEntry);
        // mother.appendChild(parseDerivationEntry);
    }

    public static void buildOne(Element mother, Node derivation, Node derived,
                                DerivedTree dTree, List<SemLit> semantics,
                                String[] specifiedSemantics, Frame frameSem) {
        Element p = derivDoc.createElement("parse");
        Element d1 = derivDoc.createElement("derivationTree");
        Element d2 = derivDoc.createElement("derivedTree");
        Element s = derivDoc.createElement("semantics");
        Element s2 = derivDoc.createElement("specified_semantics");
        Element f = derivDoc.createElement("frame");

        buildDerivationTree(d1, derivation);
        p.appendChild(d1);

        buildDerivedTree(d2, dTree, dTree.root, derived);
        p.appendChild(d2);

        buildSemantics(s, semantics);
        p.appendChild(s);

        buildSpecifiedSemantics(s2, specifiedSemantics);
        p.appendChild(s2);

        buildFrames(f, frameSem);
        p.appendChild(f);

        mother.appendChild(p);
    }

    public static void buildFamily(Element family) {

    }

    public static void buildDerivationTree(Element mother, Node derivation) {
        Element t = derivDoc.createElement("tree");
        NamedNodeMap atts = derivation.getAttributes();
        for (int i = 0; i < atts.getLength(); i++) {
            Attr a = (Attr) atts.item(i);
            String name = a.getNodeName();
            String val = a.getNodeValue();
            if (name.equals("id")) {
                t.setAttribute("id", val);
            } else if (name.equals("op")) {
                t.setAttribute("op", val);
            } else if (name.equals("op-node")) {
                t.setAttribute("node", val);
            } // skip the other attributes
        }
        NodeList childList = derivation.getChildNodes();
        for (int i = 0; i < childList.getLength(); i++) {
            Node child = childList.item(i);
            if (child instanceof Element) {
                buildDerivationTree(t, child);
            }
        }
        mother.appendChild(t);
    }

    public static void buildTrace(Element mother, Node derivation) {
        Element t = derivDoc.createElement("class");
        NamedNodeMap atts = derivation.getAttributes();
        for (int i = 0; i < atts.getLength(); i++) {
            Attr a = (Attr) atts.item(i);
            String name = a.getNodeName();
            String val = a.getNodeValue();
            if (name.equals("id")) {
                t.setTextContent(val);
            } // else if (name.equals("op")) {
            // t.setAttribute("op", val);
            // } else if (name.equals("op-node")) {
            // t.setAttribute("node", val);
            // } // skip the other attributes
        }
        NodeList childList = derivation.getChildNodes();
        for (int i = 0; i < childList.getLength(); i++) {
            Node child = childList.item(i);
            if (child instanceof Element) {
                buildTrace(mother, child);
            }
        }
        mother.appendChild(t);
    }

    public static void buildDerivedTree(Element mother, DerivedTree dTree,
                                        Node current, Node derived) {
        Element t = derivDoc.createElement("node");
        Element narg = derivDoc.createElement("narg");

        // NamedNodeMap atts = derived.getAttributes();
        // for (int i = 0; i < atts.getLength(); i++) {
        // Attr a = (Attr) atts.item(i);
        // Element f = derivDoc.createElement("f");
        // String name = a.getNodeName();
        // f.setAttribute("name", name);
        // String val = a.getNodeValue();
        // buildVal(f, val);
        // fs.appendChild(f);
        // }
        Fs features = dTree.features.get(current);
        if (features != null) {
            buildFrame(narg, features);
        }
        t.appendChild(narg);
        NodeList children = current.getChildNodes();
        // NodeList childList = derived.getChildNodes();
        // for (int i = 0; i < childList.getLength(); i++) {
        // Node child = childList.item(i);
        // if (child instanceof Element) {
        // buildDerivedTree(t, dTree, current, child);
        // }
        // }
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            buildDerivedTree(t, dTree, child, derived);
        }
        if (children.getLength() == 0) {
            // lex node
            Element fs = derivDoc.createElement("fs");
            t.setAttribute("type", "lex");
            t.setAttribute("value", current.getNodeName());

            // for display in the XMG webgui
            Element f = derivDoc.createElement("f");
            f.setAttribute("name", "cat");
            Element sym = derivDoc.createElement("sym");
            sym.setAttribute("value", current.getNodeName());
            f.appendChild(sym);
            fs.appendChild(f);
            narg.appendChild(fs);
        } else {
            t.setAttribute("type", "std");
        }

        /*
         * goal:
         * <node type="lex">
         * <narg><fs>
         * <f name="cat">
         * <sym value="John"/>
         */
        mother.appendChild(t);
    }

    public static void buildSemantics(Element mother, List<SemLit> semantics) {
        for (SemLit sl : semantics) {
            if (sl instanceof SemPred) {
                Element e = derivDoc.createElement("literal");
                String isNeg = ((SemPred) sl).isNegated() ? "yes" : "no";
                e.setAttribute("negated", isNeg);
                buildSemPred(e, (SemPred) sl);
                mother.appendChild(e);
            } else if (sl instanceof SemDom) {
                Element e = derivDoc.createElement("semdominance");
                e.setAttribute("op", "ge");
                buildVal(e, ((SemDom) sl).getArg1().toString());
                buildVal(e, ((SemDom) sl).getArg2().toString());
                mother.appendChild(e);
            } else if (sl instanceof Value) {
                buildVal(mother, ((Value) sl).toString());
            }
        }
    }

    public static void buildSemPred(Element mother, SemPred sp) {
        Element label = derivDoc.createElement("label");
        buildVal(label, sp.getLabel().toString());
        mother.appendChild(label);
        Element pred = derivDoc.createElement("predicate");
        buildVal(pred, sp.getPred().toString());
        mother.appendChild(pred);
        for (Value arg : sp.getArgs()) {
            Element argel = derivDoc.createElement("arg");
            buildVal(argel, arg.toString());
            mother.appendChild(argel);
        }
    }

    public static void buildVal(Element mother, String val) {
        Element e;
        if (val.startsWith("X")) {
            e = derivDoc.createElement("sym");
            e.setAttribute("varname", val);
            mother.appendChild(e);
        }
        // should replace the previous case
        else if (val.startsWith("_V_")) {
            e = derivDoc.createElement("sym");
            e.setAttribute("varname", val.substring(3, val.length()));
            mother.appendChild(e);
        } else if (val.startsWith("?")) {
            e = derivDoc.createElement("sym");
            e.setAttribute("varname", val);
            mother.appendChild(e);
        } else if (val.startsWith("@{")) {
            e = derivDoc.createElement("vAlt");
            val = val.substring(2, val.length() - 1);
            String[] nval = getDisj(val);
            for (int i = 0; i < nval.length; i++) {
                buildVal(e, nval[i]);
            }
            mother.appendChild(e);
        } else {
            e = derivDoc.createElement("sym");
            e.setAttribute("value", val);
            mother.appendChild(e);
        }
    }

    public static String[] getDisj(String adisj) {
        String[] res = adisj.split("\\W", -2);
        return res;
    }

    public static void buildSpecifiedSemantics(Element mother, String[] sem) {
        if (sem != null) {
            for (int i = 0; i < sem.length; i++) {
                Element repr = derivDoc.createElement("reading");
                repr.setTextContent(sem[i]);
                mother.appendChild(repr);
            }
        }
    }

    public static void buildFrames(Element mother, Frame frameSem) {
        for (Fs frame : frameSem.getFeatureStructures()) {
            buildFrame(mother, frame);
        }
        buildRelations(mother, frameSem.getRelations());
    }

    private static void buildRelations(Element mother,
                                       Set<Relation> relations) {
        // go through all the relations
        for (Relation properRelation : relations) {
            // create the element
            Element relationEl = derivDoc.createElement("relation");
            relationEl.setAttribute("name", properRelation.getName());

            for (Value properVal : properRelation.getArguments()) {
                // buildVal(relationEl, properVal.getVarVal());
                Element e;
                e = derivDoc.createElement("sym");
                e.setAttribute("varname", properVal.getVarVal());
                relationEl.appendChild(e);
            }
            mother.appendChild(relationEl);
        }
    }

    public static void buildFrame(Element mother, Fs frame) {
        Element fs = derivDoc.createElement("fs");
        if (frame.getCoref() != null)
            fs.setAttribute("coref", frame.getCoref().toString());

        // get the type
        Type type = frame.getType();
        if (type != null) {
            Element t = derivDoc.createElement("ctype");
            for (String etype : type.getElementaryTypes()) {
                Element tt = derivDoc.createElement("type");
                tt.setAttribute("val", etype);
                t.appendChild(tt);
            }
            fs.appendChild(t);
        }
        // get the features
        Hashtable<String, Value> avm = frame.getAVlist();
        Set<String> keys = avm.keySet();
        List<String> sortedKeyList = new LinkedList<String>(keys);
        Collections.sort(sortedKeyList);
        for (String k : sortedKeyList) {
            Value fval = avm.get(k);

            Element f = derivDoc.createElement("f");
            f.setAttribute("name", k);

            // fval can be a variable, a constant, or a fs (we ignore int and
            // adisj for now)
            Element e;
            switch (fval.getType()) {
                // case VAL
                case VAL:
                    // System.out.println("case VAL");
                    e = derivDoc.createElement("sym");
                    e.setAttribute("value", fval.getSVal());
                    f.appendChild(e);
                    break;
                // case VAR
                case VAR:
                    // System.out.println("case VAR");
                    e = derivDoc.createElement("sym");
                    e.setAttribute("varname", fval.getVarVal());
                    f.appendChild(e);
                    break;
                // case AVM
                case AVM:
                    // System.out.println("case AVM");
                    buildFrame(f, fval.getAvmVal());
                    break;
            }
            fs.appendChild(f);
        }
        mother.appendChild(fs);
    }

}
