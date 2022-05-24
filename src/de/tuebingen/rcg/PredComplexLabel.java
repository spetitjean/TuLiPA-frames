/*
 *  File PredComplexLabel.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@uni-tuebingen.de>
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Wolfgang Maier, 2007
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:54:09 CEST 2007
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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tuebingen.converter.CatPairs;
import de.tuebingen.tag.TagNode;
import de.tuebingen.tag.TagTree;

/**
 * @author wmaier, parmenti
 */
public class PredComplexLabel implements PredLabel {

    public static final int TREE = 0;
    public static final int ADJ = 1;
    public static final int SUB = 2;
    public static final int START = 3;

    private int type;            // tree, adj or sub
    private String treeid;
    private String nodeid;
    private List<Object> lpa;
    private CatPairs nodecat;    // for branching clauses
    private TagNode zenode;    // for branching clauses (do not appear in toString c.f. hash)
    private TagNode lexNode;    // the lex node
    // (with the word anchored, its position in the sentence, and the Gorn address for spine detection, do not appear in toString cf hash)
    private String tupleId;  // for branching clauses (do not appear in toString c.f. hash)
    private String tupleAncPos;  // for branching clauses (do not appear in toString c.f. hash)
    // to limit the RCG conversion (do not appear in toString, c.f. hash):
    private int depth;


    public PredComplexLabel(int t, String tid) {
        type = t;
        lpa = new LinkedList<Object>();
        treeid = tid;
        nodeid = null;
        nodecat = null;
        zenode = null;
        lexNode = null;
        depth = 0;
    }

    public PredComplexLabel(int t, String tid, String nid) {
        type = t;
        lpa = new LinkedList<Object>();
        treeid = tid;
        nodeid = nid;
        nodecat = null;
        zenode = null;
        lexNode = null;
        depth = 0;
    }

    public PredComplexLabel(int t, List<Object> l) {
        type = t;
        lpa = l;
        treeid = null;
        nodeid = null;
        nodecat = null;
        zenode = null;
        lexNode = null;
        depth = 0;
    }

    public PredComplexLabel(int t, List<Object> l, String tid) {
        type = t;
        lpa = l;
        treeid = tid;
        nodeid = null;
        nodecat = null;
        zenode = null;
        lexNode = null;
        depth = 0;
    }

    public PredComplexLabel(int t, List<Object> l, String tid, String nid) {
        type = t;
        lpa = l;
        treeid = tid;
        nodeid = nid;
        nodecat = null;
        zenode = null;
        lexNode = null;
        depth = 0;
    }

    public PredComplexLabel(PredComplexLabel pclab) {
        type = pclab.getType();
        lpa = new LinkedList<Object>();
        for (int i = 0; i < pclab.getLpa().size(); i++) {
            lpa.add(new String((String) pclab.getLpa().get(i)));
        }
        treeid = new String(pclab.getTreeid());
        if (pclab.getNodeid() != null) {
            nodeid = new String(pclab.getNodeid());
        }
        if (pclab.getNodecat() != null) {
            nodecat = new CatPairs(pclab.getNodecat());
            ;
        }
        if (pclab.getZenode() != null) {
            zenode = new TagNode(pclab.getZenode());
        }
        if (pclab.getLexNode() != null) {
            lexNode = new TagNode(pclab.getLexNode());
        }
        depth = pclab.getDepth();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTreeid() {
        return treeid;
    }

    public void setTreeid(String treeid) {
        this.treeid = treeid;
    }

    public String getNodeid() {
        return nodeid;
    }

    public void setNodeid(String nodeid) {
        this.nodeid = nodeid;
    }

    public CatPairs getNodecat() {
        return nodecat;
    }

    public void setNodecat(CatPairs nodecat) {
        this.nodecat = nodecat;
    }

    public TagNode getZenode() {
        return zenode;
    }

    public void setZenode(TagNode zenode) {
        this.zenode = zenode;
    }

    public TagNode getLexNode() {
        return lexNode;
    }

    public void setLexNode(TagNode lexNode) {
        this.lexNode = lexNode;
    }

    public List<Object> getLpa() {
        return lpa;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public String getTupleId() {
        return tupleId;
    }

    public void setTupleId(String tupleId) {
        this.tupleId = tupleId;
    }

    public String getTupleAncPos() {
        return tupleAncPos;
    }

    public void setTupleAncPos(String tupleAncPos) {
        this.tupleAncPos = tupleAncPos;
    }

    public void setLpa(List<Object> lpa) {
        this.lpa = lpa;
    }

    public void addToLpa(String o) {
        lpa.add(o);
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public boolean equals(Object o) {
        return this.hashCode() == o.hashCode();
    }

    public String toString() {
        String res = "";
        res += "<";
        switch (type) {
            case 0:
                res += treeid + ", ";
                break;
            case 1:
                res += "adj, " + treeid + ", " + nodeid + ", ";
                break;
            case 2:
                res += "sub, " + treeid + ", " + nodeid + ", ";
                break;
            case 3:
                res += treeid + ", ";
                break;
            default: //skip
        }
        res += "{";
        if (lpa != null) {
            for (int i = 0; i < lpa.size(); i++) {
                res += lpa.get(i) + " ";
            }
        }
        res += "}";
        res += ">";
        return res;
    }

    public String toString(Map<String, TagTree> dict) {
        String res = "";
        String realtreeid = "";
        if (dict.containsKey(treeid)) {
            realtreeid = dict.get(treeid).getOriginalId();
        }
        // for robustness:
        if (realtreeid.equals(""))
            realtreeid = treeid;
        res += "<";
        switch (type) {
            case 0:
                res += realtreeid + ", ";
                break;
            case 1:
                res += "adj, " + realtreeid + ", " + nodeid + ", ";
                break;
            case 2:
                res += "sub, " + realtreeid + ", " + nodeid + ", ";
                break;
            case 3:
                res += realtreeid + ", ";
                break;
            default: //skip
        }
        res += "{";
        if (lpa != null) {
            for (int i = 0; i < lpa.size(); i++) {
                String realLpa = "";
                if (dict.containsKey(lpa.get(i))) {
                    realLpa = dict.get(lpa.get(i)).getOriginalId();
                }
                // for robustness:
                if (realLpa.equals(""))
                    res += lpa.get(i) + " ";
                else
                    res += realLpa + " ";
            }
        }
        res += "}";
        res += ">";
        return res;
    }

    public String getComplexLabel() {
        // method used during XML export
        String res = "(";
        switch (type) {
            case 0:
                res += treeid + ",";
                break;
            case 1:
                res += "adj," + treeid + "," + nodeid + ",";
                break;
            case 2:
                res += "sub," + treeid + "," + nodeid + ",";
                break;
            case 3:
                res += treeid + ",";
                break;
            default: //skip
        }
        res += "{";
        if (lpa != null) {
            for (int i = 0; i < lpa.size(); i++) {
                res += lpa.get(i) + "-";
            }
        }
        res += "})";
        return res;
    }

    public String getComplexLabel(Map<String, TagTree> dict) {
        // method used during XML export
        String res = "(";
        String realtreeid = "";
        if (dict.containsKey(treeid)) {
            realtreeid = dict.get(treeid).getOriginalId();
        }
        // for robustness:
        if (realtreeid.equals(""))
            realtreeid = treeid;
        switch (type) {
            case 0:
                res += realtreeid + ",";
                break;
            case 1:
                res += "adj," + realtreeid + "," + nodeid + ",";
                break;
            case 2:
                res += "sub," + realtreeid + "," + nodeid + ",";
                break;
            case 3:
                res += realtreeid + ",";
                break;
            default: //skip
        }
        res += "{";
        if (lpa != null) {
            for (int i = 0; i < lpa.size(); i++) {
                String realLpa = "";
                if (dict.containsKey(lpa.get(i))) {
                    realLpa = dict.get(lpa.get(i)).getOriginalId();
                }
                // for robustness:
                if (realLpa.equals(""))
                    res += lpa.get(i) + " ";
                else
                    res += realLpa + " ";
            }
        }
        res += "})";
        return res;
    }
}
