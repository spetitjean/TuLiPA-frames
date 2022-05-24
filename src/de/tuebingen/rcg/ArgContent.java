/*
 *  File ArgContent.java
 *
 *  Authors:
 *     Wolfgang Maier <wo.maier@uni-tuebingen.de>
 *
 *  Copyright:
 *     Wolfgang Maier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:52:19 CEST 2007
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

import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.parserconstraints.CanonicalNameFactory;
import de.tuebingen.parserconstraints.Constraint;
import de.tuebingen.parserconstraints.ConstraintVector;
import de.tuebingen.tag.TagTree;
import de.tuebingen.tag.TagNode;

public class ArgContent {

    // instance is emtpy
    public static final int EPSILON = -1;
    // instance is a single variable
    public static final int VAR = 0;
    // instance is a constant
    public static final int TERM = 1;
    // instance is a list
    public static final int LIST = 2;
    // instance is a separator (cf foot node)
    public static final int SEPARATOR = 3;
    // instance is not related to any kind of node (no tree to RCG conversion)
    public static final int STD_RANGE = 0;
    // instance is related to a substitution node
    public static final int SUBST_RANGE = 1;
    // instance is related to an adjunction node
    public static final int ADJ_RANGE = 2;
    // instance is related to a null-adjunction node
    public static final int NADJ_RANGE = 3;
    // instance is related to a mandatory-adjunction node
    public static final int MADJ_RANGE = 4;

    private int node;
    private String cat;
    private int type;
    private String name;
    private List<ArgContent> list;

    public ArgContent(int t, String n) {
        type = t;
        name = n;
        list = null;
        node = STD_RANGE;
    }

    public ArgContent(int t, String n, int node_type) {
        type = t;
        name = n;
        list = null;
        node = node_type;
    }

    public ArgContent(int t, String n, int node_type, String c) {
        this(t, n, node_type);
        cat = c;
    }

    public ArgContent(ArgContent a) {
        type = a.getType();
        name = new String(a.getName());
        node = a.getNode();
        cat = a.getCat();
        if (a.getType() == ArgContent.LIST) {
            list = new LinkedList<ArgContent>();
            for (int i = 0; i < a.getList().size(); i++) {
                ArgContent aa = new ArgContent(a.getList().get(i));
                list.add(aa);
            }
        }
    }

    public ArgContent(List<ArgContent> al) {
        switch (al.size()) {
            case 1:
                ArgContent ac = al.get(0);
                this.type = ac.getType();
                this.name = ac.getName();
                this.list = ac.getList();
                this.node = ac.getNode();
                this.cat = ac.getCat();
                break;
            default:
                type = ArgContent.LIST;
                list = new LinkedList<ArgContent>();
                name = "";
                for (int i = 0; i < al.size(); i++) {
                    ArgContent aa = new ArgContent(al.get(i));
                    list.add(aa);
                    name += " " + aa.getName().trim() + " ";
                }
        }
    }

    // pre-processing for constraint vectors

    /**
     * @param vect : the constraint vector being built
     *             nf: a name factory to create unique names
     *             cnt: the position of the argcontent in the argument
     *             length: the total number of arguments
     */
    public void process(ConstraintVector vect, NameFactory nf, CanonicalNameFactory cnf, Integer cnt, int length, boolean is_lhs, ArgContent oldAC, Map<String, TagTree> gDict, PredLabel plabel) {

        // for debug:
        //String oldName = this.getName();
        switch (this.getType()) {
            case ArgContent.EPSILON:
                String n = nf.getUniqueName();
                vect.updateMapNames(n, "Eps");
                this.setName(n);
                // add a constraint (left := right)
                Constraint c1 = new Constraint(Constraint.EQUALS, n + Constraint.LEFT, n + Constraint.RIGHT);
                vect.getConstraints().add(c1);
                break;
            case ArgContent.VAR:
                n = nf.getName(this.getName());
                // we use the canonical name n2
                String n2 = cnf.getName(this.getName());
                vect.updateMapNames(n, n2);
                this.setName(n);
                break;
            case ArgContent.TERM:
                String original = new String(this.getName());
                n = nf.getUniqueName();
                vect.updateMapNames(n, this.getName());
                this.setName(n);
                // add a constraint (plus / minus)
                Constraint c2 = new Constraint(Constraint.EQUALS, n + Constraint.LEFT, n + Constraint.RIGHT, 1, 0);
                vect.getConstraints().add(c2);
                // add a constraint on the position in the sentence
                // (TAG-based RCG only)
                if (plabel instanceof PredComplexLabel && (((PredComplexLabel) plabel).getType() == PredComplexLabel.TREE)) {
                    String treeId = ((PredComplexLabel) plabel).getTreeid();
                    TagTree tree = gDict.get(treeId);
                    TagNode tn = ((TagNode) tree.getLexAnc());
                    String word = tn.getWord().getWord();
                    if (original.equals(word)) {
                        int begin = tn.getWord().getStart();
                        int end = tn.getWord().getEnd();
                        Constraint c3 = new Constraint(Constraint.EQUALS, n + Constraint.LEFT, Constraint.VAL, 0, begin);
                        Constraint c4 = new Constraint(Constraint.EQUALS, n + Constraint.RIGHT, Constraint.VAL, 0, end);
                        vect.getConstraints().add(c3);
                        vect.getConstraints().add(c4);
                    }
                }
                break;
            case ArgContent.LIST:
                for (ArgContent ac : this.getList()) {
                    ac.process(vect, nf, cnf, ++cnt, length, is_lhs, this, gDict, plabel);
                }
            default: //skip
        }
        // we must update the lhs' boundaries
        if (is_lhs) {
            // for debug:
            //System.err.println(oldName + " processed." + cnt + " " + length);
            if (cnt == 0) {
                vect.getBoundaries().add(this.getName() + Constraint.LEFT);
                if (length == 1) {
                    vect.getBoundaries().add(this.getName() + Constraint.RIGHT);
                }
            } else {
                if (cnt == (length - 1))
                    vect.getBoundaries().add(this.getName() + Constraint.RIGHT);
            }
        }
		/* // for debug:
		else { // here the lhs has already been processed 
			System.err.println("*** Boundaries: " + vect.getBoundaries());
		}
		*/
        if (!(vect.getRanges().containsKey(this.getName()))) {
            vect.getRanges().put(this.getName(), this);
            // add an adjacency constraint if inside the same argument
            if (oldAC != null) {
                Constraint c3 = new Constraint(Constraint.EQUALS, oldAC.getName() + Constraint.RIGHT, this.getName() + Constraint.LEFT);
                vect.getConstraints().add(c3);
            }
            // add a less or equal constraint (except for terminals, cf T.l = T.r + 1 already added)
            if (!(this.getType() == ArgContent.TERM)) {
                Constraint c4 = new Constraint(Constraint.LE_EQ, this.getName() + Constraint.LEFT, this.getName() + Constraint.RIGHT);
                vect.getConstraints().add(c4);
            }
        }
    }

    public int getSize() {
        if (this.getType() != ArgContent.LIST) {
            return 1;
        } else {
            int res = 0;
            for (ArgContent ac : this.getList()) {
                res += ac.getSize();
            }
            return res;
        }
    }

    public ArgContent getRec(int i) {
        // looks for the ith daughter
        ArgContent res = null;
        List<ArgContent> lac = new LinkedList<ArgContent>();
        this.flatten(lac);
        res = lac.get(i);
        return res;
    }

    public void flatten(List<ArgContent> lac) {
        if (this.getType() != ArgContent.LIST) {
            lac.add(this);
        } else {
            for (ArgContent ac : this.getList()) {
                ac.flatten(lac);
            }
        }
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ArgContent> getList() {
        return list;
    }

    public void setList(List<ArgContent> list) {
        this.list = list;
    }

    public int getNode() {
        return node;
    }

    public void setNode(int node) {
        this.node = node;
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public boolean equals(Object o) {
        return this.hashCode() == o.hashCode();
    }

    public String toString() {
        if (type == ArgContent.EPSILON) {
            return this.name; //"Eps";
        } else if (type == ArgContent.VAR) {
            return "_" + this.name;
        } else {
            return this.name;
        }
    }

    public String toStringRenamed(Map<String, String> names) {
        if (names.containsKey(name))
            return names.get(name);
        else
            return name;
    }
}
