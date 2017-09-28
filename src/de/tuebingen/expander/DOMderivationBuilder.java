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
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.tuebingen.gui.ParseTreeCollection;
import de.tuebingen.tag.SemDom;
import de.tuebingen.tag.SemLit;
import de.tuebingen.tag.SemPred;
import de.tuebingen.tag.Value;


public class DOMderivationBuilder {
	
	private static Document  derivDoc;
	
	public static Document buildDOMderivation(ArrayList<ParseTreeCollection> all, String sentence) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder constructor    = factory.newDocumentBuilder();
			derivDoc                       = constructor.newDocument();
			derivDoc.setXmlVersion("1.0");
			derivDoc.setXmlStandalone(false);
			
			Element root = derivDoc.createElement("parses");
			root.setAttribute("sentence", sentence);
            for (ParseTreeCollection ptc : all)
            {
            	buildOne(root, ptc.getDerivationTree().getDomNodes().get(0), ptc.getDerivedTree().getDomNodes().get(0), ptc.getSemantics(), ptc.getSpecifiedSemantics());
            }
			// finally we do not forget the root
			derivDoc.appendChild(root);
			return derivDoc;
			
		} catch (ParserConfigurationException e) {
			System.err.println(e);
			return null;
		}		
	}
	
	public static void buildOne(Element mother, Node derivation, Node derived, List<SemLit> semantics, String[] specifiedSemantics) {
		Element p  = derivDoc.createElement("parse");
		Element d1 = derivDoc.createElement("derivationTree");
		Element d2 = derivDoc.createElement("derivedTree");
		Element s  = derivDoc.createElement("semantics");
		Element s2 = derivDoc.createElement("specified_semantics");
		
		buildDerivationTree(d1, derivation);
		p.appendChild(d1);
		
		buildDerivedTree(d2, derived);
		p.appendChild(d2);
		
		buildSemantics(s, semantics);
		p.appendChild(s);
		
		buildSpecifiedSemantics(s2, specifiedSemantics);
		p.appendChild(s2);
		
		mother.appendChild(p);
	}
	
	public static void buildDerivationTree(Element mother, Node derivation){
		Element t = derivDoc.createElement("tree");
		NamedNodeMap atts = derivation.getAttributes();		
		for (int i = 0 ; i < atts.getLength() ; i++){
			Attr a = (Attr) atts.item(i);
			String name = a.getNodeName();
			String val  = a.getNodeValue();
			if (name.equals("id")) {
				t.setAttribute("id", val);
			} else if (name.equals("op")) {
				t.setAttribute("op", val);
			} else if (name.equals("op-node")) {
				t.setAttribute("node", val);
			} // skip the other attributes
		}
		NodeList childList = derivation.getChildNodes();
        for (int i = 0; i < childList.getLength(); i++)
        {
            Node child = childList.item(i);
            if (child instanceof Element)
            {
                buildDerivationTree(t, child);
            }
        }
        mother.appendChild(t);
	}

	public static void buildDerivedTree(Element mother, Node derived){
		Element t = derivDoc.createElement("node");
		
		Element narg = derivDoc.createElement("narg");
		t.appendChild(narg);
		
		Element fs= derivDoc.createElement("fs");
		narg.appendChild(fs);
		
		NamedNodeMap atts = derived.getAttributes();
		for (int i = 0 ; i < atts.getLength() ; i++){
			Attr a = (Attr) atts.item(i);
			Element f = derivDoc.createElement("f");
			String name = a.getNodeName();
			f.setAttribute("name", name);
			String val  = a.getNodeValue();
			buildVal(f, val);
			fs.appendChild(f);
		}
		NodeList childList = derived.getChildNodes();
        for (int i = 0; i < childList.getLength(); i++)
        {
            Node child = childList.item(i);
            if (child instanceof Element)
            {
                buildDerivedTree(t, child);
            }
        }
        if (childList.getLength() == 0) {
        	//lex node
        	t.setAttribute("type", "lex");
        	t.setAttribute("value", derived.getNodeName());
        } else {
        	t.setAttribute("type", "std");
        }
		mother.appendChild(t);
	}
	
	public static void buildSemantics(Element mother, List<SemLit> semantics){
		for(SemLit sl : semantics) {
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
		for(Value arg : sp.getArgs()) {
			Element argel = derivDoc.createElement("arg");
			buildVal(argel, arg.toString());
			mother.appendChild(argel);
		}
	}
	
	public static void buildVal(Element mother, String val){
		Element e;
		if (val.startsWith("X")) {
			e = derivDoc.createElement("sym");
			e.setAttribute("varname", val);
			mother.appendChild(e);	
		} else if (val.startsWith("?")) { 
			e = derivDoc.createElement("sym");
			e.setAttribute("varname", val);
			mother.appendChild(e);			
		} else if (val.startsWith("@{")) {
			e = derivDoc.createElement("vAlt");
			val = val.substring(2, val.length() - 1);
			String[] nval = getDisj(val);
			for(int i = 0 ; i < nval.length ; i++){
				buildVal(e, nval[i]);
			}
			mother.appendChild(e);	
		} else {
			e = derivDoc.createElement("sym");
			e.setAttribute("value", val);
			mother.appendChild(e);	
		}
	}
	
	public static String[] getDisj(String adisj){
		String[] res = adisj.split("\\W", -2);
		return res;
	}
	
	public static void buildSpecifiedSemantics(Element mother, String[] sem) {
		if (sem != null) {
			for(int i = 0 ; i < sem.length ; i++) {
				Element repr = derivDoc.createElement("reading");
				repr.setTextContent(sem[i]);
				mother.appendChild(repr);
			}
		}
	}
}
