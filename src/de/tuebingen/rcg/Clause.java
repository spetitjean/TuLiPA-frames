/*
 *  File Clause.java
 *
 *  Authors:
 *     Wolfgang Maier <wo.maier@uni-tuebingen.de>
 *
 *  Copyright:
 *     Wolfgang Maier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:53:17 CEST 2007
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.parser.termtransform.RangeConstraintVector;
import de.tuebingen.parserconstraints.CanonicalNameFactory;
import de.tuebingen.parserconstraints.Constraint;
import de.tuebingen.parserconstraints.ConstraintVector;
import de.tuebingen.tag.TagTree;

/**
 * Represents a single clause (for RCGs).
 *
 * @author wmaier
 */
public class Clause {

    private Predicate lhs;
    private List<Predicate> rhs;
    private int cindex;

    private RangeConstraintVector rcv;
    private ConstraintVector vect;

    public Clause() {
        this(null, new ArrayList<Predicate>());
    }

    public Clause(Predicate p, List<Predicate> lp) {
        this(p, lp, -1);
    }

    public Clause(Predicate p, List<Predicate> lp, float prob) {
        lhs = p;
        rhs = lp;
    }

    public void setLhs(Predicate lhs) {
        this.lhs = lhs;
    }

    public Predicate getLhs() {
        return lhs;
    }

    public void setRhs(List<Predicate> rhs) {
        this.rhs = rhs;
    }

    public List<Predicate> getRhs() {
        return rhs;
    }

    public void addToRhs(Predicate p) {
        rhs.add(p);
    }

    public ConstraintVector getVect() {
        return vect;
    }

    public void setVect(ConstraintVector vect) {
        this.vect = vect;
    }

    public int getNbOfLhsRanges() {
        return this.lhs.getNbOfRanges();
    }

    // create a range variable constraint vector with renaming of the constants (cf Earley)
    public Clause createVect(NameFactory nf, CanonicalNameFactory cnf, Map<String, TagTree> gDict) {
        ConstraintVector cvect = new ConstraintVector(this.getNbOfLhsRanges());

        ArrayList<Predicate> preds = new ArrayList<Predicate>();
        preds.add(this.lhs);
        preds.addAll(this.rhs);
        // flag to distinguish the lhs from the other predicates
        boolean is_lhs = true;
        Clause newClause = new Clause();
        for (Predicate pred : preds) {
            Predicate newPred = new Predicate();
            newPred.setLabel(pred.getLabel()); // TODO does it need a copy constructor ?
            for (Argument arg : pred.getArgs()) {
                Argument newArg = new Argument();
                ArgContent oldArgContent = null;
                for (int i = 0; i < arg.getContent().size(); i++) {
                    ArgContent ac = arg.getContent().get(i);
                    ArgContent renamed = new ArgContent(ac);
                    renamed.process(cvect, nf, cnf, i, arg.getSize(), is_lhs, oldArgContent, gDict, newPred.getLabel());
                    oldArgContent = renamed;
                    newArg.addArg(renamed);
                }
                newPred.addArg(newArg);
            }
            if (is_lhs) {
                // adding a constraint for span covered by substitution
                if (newPred.getLabel() instanceof PredComplexLabel && (((PredComplexLabel) newPred.getLabel()).getType() == PredComplexLabel.SUB)) {
                    // Recall that sub branching clause only have a single argument
                    // hence left is at pos 0 and right at pos 1 in the boundaries table
                    String lbound = cvect.getBoundaries().get(0);
                    String rbound = cvect.getBoundaries().get(1);
                    Constraint c = new Constraint(Constraint.LE_EQ, lbound, rbound, 1, 0);
                    cvect.addConstraint(c);
                }
                newClause.setLhs(newPred);
            } else {
                newClause.addToRhs(newPred);
            }
            is_lhs = false;
        }
        newClause.setVect(cvect);
        newClause.setCindex(this.getCindex());
        return newClause;
    }

    // for limiting the RCG conversion:
    public void updateRhsDepth() {
        PredLabel label = lhs.getLabel();
        if (label instanceof PredComplexLabel) {
            int depth = ((PredComplexLabel) lhs.getLabel()).getDepth();
            for (int i = 0; i < rhs.size(); i++) {
                ((PredComplexLabel) rhs.get(i).getLabel()).setDepth(depth);
            }
        }
    }

    /* content of the map returned:
     * [variable -> ["l" -> [adjacent_variable_on_the_left_1,...], "r" -> [adjacent_variable_on_the_right_1,...]]]
     */
    private HashMap<ArgContent, HashMap<String, HashSet<ArgContent>>> getVarDic() {
        HashMap<ArgContent, HashMap<String, HashSet<ArgContent>>> ret =
                new HashMap<ArgContent, HashMap<String, HashSet<ArgContent>>>();

        ArrayList<Predicate> preds = new ArrayList<Predicate>();
        preds.add(this.lhs);
        preds.addAll(this.rhs);
        Iterator<Predicate> it = preds.iterator();
        while (it.hasNext()) {
            Iterator<Argument> argit = it.next().getArgs().iterator();
            while (argit.hasNext()) {
                List<ArgContent> arg = argit.next().getContent();
                for (int i = 0; i < arg.size(); ++i) {
                    ArgContent argc = arg.get(i);
                    // we should check if it's really a variable in the future
                    HashMap<String, HashSet<ArgContent>> lrmap = null;
                    if (!ret.containsKey(argc)) {
                        lrmap = new HashMap<String, HashSet<ArgContent>>();
                        lrmap.put("l", new HashSet<ArgContent>());
                        lrmap.put("r", new HashSet<ArgContent>());
                    } else {
                        lrmap = ret.get(argc);
                    }
                    // check adjacencies on both sides of the current variable
                    if (i > 0) {
                        lrmap.get("l").add(arg.get(i - 1));
                    }
                    if (i < arg.size() - 1) {
                        lrmap.get("r").add(arg.get(i + 1));
                    }
                    ret.put(argc, lrmap);
                }
            }
        }
        return ret;
    }

    public RangeConstraintVector getRangeConstraintVector() {
        return new RangeConstraintVector(this.rcv);
    }

    public void calcRangeConstraintVector() {
        HashMap<ArgContent, HashMap<String, HashSet<ArgContent>>> vardic = getVarDic();
        this.rcv = new RangeConstraintVector(vardic.size());

        // fill in the variables
        Set<ArgContent> vars = vardic.keySet();
        int count = 0;
        Iterator<ArgContent> varsit = vars.iterator();
        while (varsit.hasNext()) {
            ArgContent var = varsit.next();
            this.rcv.setKey(count, var);
            ++count;
        }
        // now set the right identies in the vector
        varsit = vars.iterator();
        while (varsit.hasNext()) {
            ArgContent var = varsit.next();
            HashMap<String, HashSet<ArgContent>> lrmap = vardic.get(var);
            HashSet<ArgContent> lset = lrmap.get("l");
            Iterator<ArgContent> lsetit = lset.iterator();
            while (lsetit.hasNext()) {
                ArgContent argc = lsetit.next();
                this.rcv.update(this.rcv.right(argc), this.rcv.left(var));
                /*ret.get(argc).setRight(ret.get(var).left());*/
            }
            HashSet<ArgContent> rset = lrmap.get("r");
            Iterator<ArgContent> rsetit = rset.iterator();
            while (rsetit.hasNext()) {
                ArgContent argc = rsetit.next();
                this.rcv.update(this.rcv.left(argc), this.rcv.right(var));
                /*ret.get(argc).setLeft(ret.get(var).right());*/
            }
            if (var.getName().startsWith("Eps")) {
                rcv.update(rcv.left(var), rcv.right(var));
            }
        }
        rcv.resetNumbering();
    }

    public List<ArgContent> getArgcList() {
        List<ArgContent> ret = new LinkedList<ArgContent>();
        for (Argument arg : lhs.getArgs()) {
            for (ArgContent argc : arg.getContent()) {
                ret.add(argc);
            }
        }
        return ret;
    }


    /* *********************************************************/

    public int getCindex() {
        return cindex;
    }

    public void setCindex(int cindex) {
        this.cindex = cindex;
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public boolean equals(Object o) {
        return this.hashCode() == o.hashCode();
    }

    public String print() {
        String res = "";
        res += this.toString();
        if (vect != null)
            res += " " + this.vect.print();
        return res;
    }

    public String toStringRenamed() {
        String res = "";
        if (vect == null)
            res += this.toString();
        else {
            res += this.toStringRenamed(this.vect.getMapNames());
            res += " " + this.vect.toStringRenamed();
        }
        return res;
    }

    public String toStringRenamed(Map<String, String> names) {
        String res = lhs.toStringRenamed(names) + " --> ";
        for (int i = 0; i < rhs.size(); i++) {
            res += rhs.get(i).toStringRenamed(names);
            if (i < rhs.size() - 1)
                res += " ";
        }
        if (rhs.size() < 1) {
            res += "[Eps]";
        }
        return res;
    }

    public String toShortString() {
        String res = String.valueOf(cindex);
        return res;
    }

    public String toString() {
        String res = lhs.toString() + " --> ";
        for (int i = 0; i < rhs.size(); i++) {
            res += rhs.get(i).toString();
            if (i < rhs.size() - 1)
                res += " ";
        }
        if (rhs.size() < 1) {
            res += "[Eps]";
        }
        return res;
    }

    // for pretty printing
    public String toString(Map<String, TagTree> dict) {
        String res = lhs.toString(dict) + " --> ";
        for (int i = 0; i < rhs.size(); i++) {
            res += rhs.get(i).toString(dict);
            if (i < rhs.size() - 1)
                res += " ";
        }
        if (rhs.size() < 1) {
            res += "[Eps]";
        }
        return res;
    }

}
