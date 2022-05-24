/*
 *  File ProduceDOM.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Fr 18. Jan 19:44:44 CET 2008
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
package de.tuebingen.forest;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.rcg.PredComplexLabel;
import de.tuebingen.tag.TagTree;

public class ProduceDOM {

    private static Document forestDoc;
    private static Map<Tidentifier, List<Rule>> forest;
    private static List<Tidentifier> start;
    private static NameFactory name_factory; // to rename the clause identifiers
    // in case of instantiation
    // ambiguity
    private static Map<String, TagTree> dictionary; // to find out original tree
    // ids

    public static Document buildDOMForest(Map<Tidentifier, List<Rule>> f,
                                          List<Tidentifier> s, String sentence, String grammarname,
                                          NameFactory nf, Map<String, TagTree> dict) {
        dictionary = dict;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder constructor = factory.newDocumentBuilder();
            forestDoc = constructor.newDocument();
            forest = f;
            start = s;
            name_factory = nf;
            forestDoc.setXmlVersion("1.0");
            forestDoc.setXmlStandalone(false);

            Element root = forestDoc.createElement("forest");
            root.setAttribute("sentence", sentence);
            root.setAttribute("grammar", grammarname);
            /*
             * // for debugging:
             * Iterator<Tidentifier> iid = f.keySet().iterator();
             * while(iid.hasNext()) {
             * Tidentifier next = iid.next();
             * System.out.print(next + " ");
             * System.out.println(f.get(next).size());
             * }
             */

            // for each starting starting rule
            Element starte = forestDoc.createElement("start");
            for (int i = 0; i < start.size(); i++) {
                // What if there are several starting rules (disjunction on
                // start) ?
                List<Rule> starts = f.containsKey(start.get(i))
                        ? f.get(start.get(i)) : null;
                for (int j = 0; j < starts.size(); j++) {
                    Element elId = forestDoc.createElement("start_rule");
                    elId.setAttribute("id", "" + name_factory
                            .getName(starts.get(j).getLhs().toString()));
                    // elId.setAttribute("id",
                    // ""+name_factory.getName(""+start.get(i).getClauseId()));
                    starte.appendChild(elId);
                }
            }
            root.appendChild(starte);

            // BEGIN OPTION
            // 1---------------------------------------------------------------
            /*
             * Preprocessing needed ? discussion going on with Johannes
             *
             * // Pre-processing needed: we gather disjunctions of clause
             * instantiation
             * // within the same forest rule
             * Tidentifier[] ids = forest.keySet().toArray(new Tidentifier[0]);
             * Arrays.sort(ids);
             *
             * // for each rule
             * Tidentifier current = null;
             * Element curelt = null;
             * int i = 0;
             * while (i < ids.length) {
             * Tidentifier next = ids[i];
             * if (current == null || next.getClauseId() !=
             * current.getClauseId()) {
             * // new LHS
             * Element st = forestDoc.createElement("rule");
             * //st.setAttribute("id", name_factory.getName(next.toString()));
             * st.setAttribute("id",
             * ""+name_factory.getName(""+next.getClauseId()));
             * // dictionary **has to be left undefined** for real forest
             * extraction
             * // but it is set for pretty printing (see Interface.java)
             * if (dictionary == null)
             * st.setAttribute("tree_name", next.getTreeId());
             * else
             * st.setAttribute("tree_name",
             * dictionary.get(next.getTreeId()).getOriginalId());
             *
             * curelt = st;
             * }
             *
             * // in all cases, we add the rules
             * List<Rule> rules = forest.get(next);
             * for(int j = 0 ; j < rules.size() ; j++){
             * buildRHS(curelt, rules.get(j));
             * }
             * root.appendChild(curelt);
             *
             * current = next;
             * i++;
             * }
             */
            // END OPTION
            // 1---------------------------------------------------------------
            // BEGIN OPTION
            // 2---------------------------------------------------------------
            // /* section to be replaced in future releases ? Discussion with
            // Johannes going on
            Set<Tidentifier> keys = forest.keySet();
            Iterator<Tidentifier> it = keys.iterator();
            while (it.hasNext()) {
                Tidentifier tid = it.next();
                Element st = forestDoc.createElement("rule");
                st.setAttribute("id", name_factory.getName(tid.toString()));
                // st.setAttribute("id",
                // ""+name_factory.getName(""+tid.getClauseId()));
                // dictionary **has to be left undefined** for real forest
                // extraction
                // but it is set for pretty printing (see Interface.java)
                if (dictionary == null)
                    st.setAttribute("tree_name", tid.getTreeId());
                else
                    st.setAttribute("tree_name",
                            dictionary.get(tid.getTreeId()).getOriginalId());

                List<Rule> rules = forest.get(tid);
                for (int j = 0; j < rules.size(); j++) {
                    buildRHS(st, rules.get(j));
                }
                root.appendChild(st);
            }
            // */
            // END OPTION
            // 2---------------------------------------------------------------

            // finally we do not forget the root
            forestDoc.appendChild(root);
            return forestDoc;

        } catch (ParserConfigurationException e) {
            System.err.println(e);
            return null;
        }
    }

    public static void buildRHS(Element mother, Rule rule) {
        Element e = forestDoc.createElement("rhs");
        buildCombination(e, rule.getRhs());
        mother.appendChild(e);
    }

    public static void buildCombination(Element mother, Combination c) {
        Iterator<TreeOp> it = c.iterator();
        while (it.hasNext()) {
            TreeOp to = it.next();
            buildTreeOp(mother, to);
        }
    }

    public static void buildTreeOp(Element mother, TreeOp top) {
        if (top.isDisj()) {
            Element e = forestDoc.createElement("disj");
            buildDisj(e, top);
            mother.appendChild(e);
        } else {
            buildUnique(mother, top);
        }
    }

    public static void buildDisj(Element mother, TreeOp top) {
        TreeOp t = new TreeOp(top.getId(), top.getType());
        buildUnique(mother, t);
        if (top.isDisj())
            buildDisj(mother, top.getOr());
    }

    public static void buildUnique(Element mother, TreeOp top) {
        Element e = forestDoc.createElement("operation");
        String value = "";
        switch (top.getType()) {
            case PredComplexLabel.ADJ:
                value = "adj";
                break;
            case PredComplexLabel.SUB:
                value = "sub";
                break;
            default: // skip
        }
        if (!value.equals(""))
            e.setAttribute("type", value);
        e.setAttribute("id", name_factory.getName(top.getId().toString()));
        // e.setAttribute("id",
        // ""+name_factory.getName(""+top.getId().getClauseId()));
        e.setAttribute("node", top.getId().getNodeId());
        // System.out.println("top.getId(): "+top.getId());
        // System.out.println("top.getId().getNodeId():
        // "+top.getId().getNodeId());
        mother.appendChild(e);
    }

}
