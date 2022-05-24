/*
 *  File RCGDOMbuilder.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Do 13. Dez 17:11:34 CET 2007
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
package de.tuebingen.rcg;

import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.tuebingen.tag.TagTree;


public class RCGDOMbuilder {

    private static Map<String, TagTree> dictionary;

    public static Document exportGrammar(RCG g, Map<String, TagTree> dict) {
        dictionary = dict;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder constructor = factory.newDocumentBuilder();
            Document rcggrammar = constructor.newDocument();
            rcggrammar.setXmlVersion("1.0");
            rcggrammar.setXmlStandalone(true);

            Element root = rcggrammar.createElement("rcg");
            String label = "";
            PredLabel p = g.getStartPredicateLabel();
            if (p instanceof PredStringLabel) {
                label = p.toString();
            } else if (p instanceof PredComplexLabel) {
                label = ((PredComplexLabel) p).getComplexLabel();
            }
            root.setAttribute("start", label);

            List<Clause> clauses = g.getClauses();
            for (int i = 0; i < clauses.size(); i++) {
                RCGDOMbuilder.exportClause(root, clauses.get(i), null, rcggrammar);
            }

            // finally we do not forget the root
            rcggrammar.appendChild(root);
            return rcggrammar;

        } catch (ParserConfigurationException e) {
            System.err.println(e);
            //System.err.println(e.getStackTrace());
            return null;
        }
    }

    public static void exportClause(Element mother, Clause cl, Integer id, Document rcggrammar) {
        Element e = rcggrammar.createElement("clause");
        RCGDOMbuilder.exportPredicate(e, cl.getLhs(), rcggrammar);
        for (int i = 0; i < cl.getRhs().size(); i++) {
            RCGDOMbuilder.exportPredicate(e, cl.getRhs().get(i), rcggrammar);
        }
        if (id != null)
            e.setAttribute("id", "_" + id);
        mother.appendChild(e);
    }

    public static void exportPredicate(Element mother, Predicate p, Document rcggrammar) {
        Element e = rcggrammar.createElement("pred");
        String label = "";
        if (p.getLabel() instanceof PredStringLabel) {
            label = p.toString();
        } else if (p.getLabel() instanceof PredComplexLabel) {
            label = ((PredComplexLabel) p.getLabel()).getComplexLabel(dictionary);
        }
        e.setAttribute("name", "\'" + label + "\'");
        for (int i = 0; i < p.getArgs().size(); i++) {
            RCGDOMbuilder.exportArg(e, p.getArgs().get(i), rcggrammar);
        }
        mother.appendChild(e);
    }

    public static void exportArg(Element mother, Argument arg, Document rcggrammar) {
        Element e = rcggrammar.createElement("argument");
        for (int i = 0; i < arg.size(); i++) {
            RCGDOMbuilder.exportRange(e, arg.get(i), rcggrammar);
        }
        mother.appendChild(e);
    }

    public static void exportRange(Element mother, ArgContent argc, Document rcggrammar) {
        Element e = rcggrammar.createElement("range");
        switch (argc.getType()) {
            case ArgContent.LIST:
                e.setAttribute("type", "seq");
                for (int i = 0; i < argc.getList().size(); i++) {
                    RCGDOMbuilder.exportRange(e, argc.getList().get(i), rcggrammar);
                }
                break;
            case ArgContent.EPSILON:
                e.setAttribute("type", "eps");
                break;
            case ArgContent.TERM:
                e.setAttribute("type", "const");
                e.setAttribute("content", "\'" + argc.getName() + "\'");
                break;
            case ArgContent.VAR:
                e.setAttribute("type", "var");
                e.setAttribute("content", argc.getName().replace('.', '_'));
                break;
            default://skip
        }
        mother.appendChild(e);
    }
}
