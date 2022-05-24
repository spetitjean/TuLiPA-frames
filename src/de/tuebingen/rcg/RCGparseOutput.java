/*
 *  File RCGparseOutput.java
 *
 *  Authors:
 *     Johannes Dellert
 *
 *  Copyright:
 *     Johannes Dellert, 2007
 *
 *  Last modified:
 *     Do 16. Apr 09:55:36 CEST 2009
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.tuebingen.parser.ClauseKey;
import de.tuebingen.parser.DNode;
import de.tuebingen.parser.DStep;

public class RCGparseOutput {

    private static boolean verbose;
    private static RCG grammar;
    private static List<ClauseKey> answers;
    private static List<ClauseKey> eRHS; //empty RHS
    private static List<Integer> done;
    private static Map<ClauseKey, DStep> dtable;
    private static Document rcgForestDoc;


    public static Document produceDOMparse(boolean v, RCG g, List<ClauseKey> a, List<ClauseKey> e, Map<ClauseKey, DStep> t) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder constructor = factory.newDocumentBuilder();
            rcgForestDoc = constructor.newDocument();
            verbose = v;
            grammar = g;
            answers = a;
            eRHS = e;
            dtable = t;
            done = new LinkedList<Integer>();

            rcgForestDoc.setXmlVersion("1.0");
            rcgForestDoc.setXmlStandalone(false);

            Element root = rcgForestDoc.createElement("rcg_forest");
            Comment parse = rcgForestDoc.createComment(printForest(dtable));
            root.appendChild(parse);

            Element clauses = rcgForestDoc.createElement("clauses");
            // for each clause instantiated in parsing
            for (int i = 0; i < answers.size(); i++) {
                Integer index = answers.get(i).getCindex();
                done.add(index);
                RCGDOMbuilder.exportClause(clauses, grammar.getClause(index), index, rcgForestDoc);
            }
            for (int i = 0; i < eRHS.size(); i++) {
                Integer index = eRHS.get(i).getCindex();
                done.add(index);
                RCGDOMbuilder.exportClause(clauses, grammar.getClause(index), index, rcgForestDoc);
            }
            Set<ClauseKey> keys = dtable.keySet();
            Iterator<ClauseKey> ick = keys.iterator();
            while (ick.hasNext()) {
                ClauseKey ck = ick.next();
                Integer index = ck.getCindex();
                if (verbose)
                    System.err.println(grammar.getClause(index).toString());
                if (!done.contains(index))
                    RCGDOMbuilder.exportClause(clauses, grammar.getClause(index), index, rcgForestDoc);
                done.add(index);
            }
            root.appendChild(clauses);

            // for each starting rule
            Element starte = rcgForestDoc.createElement("start_clauses");
            for (int i = 0; i < answers.size(); i++) {
                Element elId = rcgForestDoc.createElement("clause_call");
                elId.setAttribute("id", "_" + answers.get(i).getCindex());
                for (int j = 0; j < answers.get(i).getArglist().size(); j++) {
                    RCGDOMbuilder.exportArg(elId, answers.get(i).getArglist().get(j), rcgForestDoc);
                }
                starte.appendChild(elId);
            }
            root.appendChild(starte);

            // for each derivation rule
            Element rules = rcgForestDoc.createElement("rules");
            Set<ClauseKey> dKeys = dtable.keySet();
            Iterator<ClauseKey> dIt = dKeys.iterator();
            while (dIt.hasNext()) {
                ClauseKey lhs = dIt.next();
                Element rule = rcgForestDoc.createElement("rule");
                rule.setAttribute("id", "_" + lhs.getCindex());
                for (int j = 0; j < lhs.getArglist().size(); j++) {
                    RCGDOMbuilder.exportArg(rule, lhs.getArglist().get(j), rcgForestDoc);
                }
                Map<String, DNode[]> instantiations = dtable.get(lhs).clean();
                Set<String> iKeys = instantiations.keySet();
                Iterator<String> its = iKeys.iterator();
                while (its.hasNext()) {
                    String binding = its.next();
                    Element rhs = rcgForestDoc.createElement("rhs");
                    DNode[] dnodes = instantiations.get(binding);
                    for (int l = 0; l < dnodes.length; l++) {
                        DNode dn = dnodes[l];
                        if (dn.isAmbiguous()) {
                            Element disj = rcgForestDoc.createElement("disj");
                            exportOpNode(disj, dn, rcgForestDoc);
                            rhs.appendChild(disj);
                        } else {
                            exportOpNode(rhs, dn, rcgForestDoc);
                        }
                    }
                    rule.appendChild(rhs);
                }
                rules.appendChild(rule);
            }
            root.appendChild(rules);

            rcgForestDoc.appendChild(root);
            return rcgForestDoc;

        } catch (ParserConfigurationException ex) {
            System.err.println(ex);
            return null;
        }
    }

    public static void exportOpNode(Element mother, DNode dn, Document rcgforest) {
        Element e = rcgforest.createElement("clause_call");
        ClauseKey ck = dn.getInstantiation();
        e.setAttribute("id", "_" + ck.getCindex());
        for (int i = 0; i < ck.getArglist().size(); i++) {
            RCGDOMbuilder.exportArg(e, ck.getArglist().get(i), rcgforest);
        }
        mother.appendChild(e);
        if (dn.isAmbiguous()) {
            exportOpNode(mother, dn.getDerivStep(), rcgforest);
        }
    }

    public static String printForest(Map<ClauseKey, DStep> table) {
        String res = "Forest: \n";
        Set<ClauseKey> keys = table.keySet();
        Iterator<ClauseKey> it = keys.iterator();
        while (it.hasNext()) {
            ClauseKey ck = it.next();
            Map<String, DNode[]> instantiations = table.get(ck).clean();
            Set<String> iKeys = instantiations.keySet();
            Iterator<String> its = iKeys.iterator();
            while (its.hasNext()) {
                String binding = its.next();
                res += ck.getCindex() + " ";
                res += ck.getArgs() + " ";
                res += "=> ";

                DNode[] dnodes = instantiations.get(binding);
                for (int l = 0; l < dnodes.length; l++) {
                    DNode dn = dnodes[l];
                    res += printNode(dn);
                }
                res += "\n";
            }
        }
        return res;
    }

    public static String printNode(DNode dn) {
        String res = "";
        if (dn.isAmbiguous()) {
            res += "( ";
            res += dn.getInstantiation().getCindex() + " ";
            res += dn.getInstantiation().getArgs() + " ";
            res += " | ";
            res += printNode(dn.getDerivStep());
            res += " )";
        } else {
            res += dn.getInstantiation().getCindex() + " ";
            res += dn.getInstantiation().getArgs() + " ";
        }
        return res;
    }

}
