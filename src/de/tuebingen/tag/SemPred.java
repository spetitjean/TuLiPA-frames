/*
 *  File SemPred.java
 *
 *  Authors:
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2008
 *
 *  Last modified:
 *     Tue Jan 29 10:42:11 CET 2009
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
package de.tuebingen.tag;

import java.util.LinkedList;
import java.util.List;

import de.tuebingen.anchoring.NameFactory;

public class SemPred implements SemLit {

    private boolean negated;
    private Value label;
    private Value pred;
    private List<Value> args;

    public SemPred() {
        negated = false; // default value
    }

    public SemPred(SemPred sp) {
        negated = sp.isNegated();
        label = new Value(sp.getLabel());
        pred = new Value(sp.getPred());
        args = new LinkedList<Value>();
        for (int i = 0; i < sp.getArgs().size(); i++) {
            args.add(new Value(sp.getArgs().get(i)));
        }
    }

    public SemPred(SemPred sp, NameFactory nf) {
        negated = sp.isNegated();
        label = new Value(sp.getLabel(), nf);
        pred = new Value(sp.getPred(), nf);
        args = new LinkedList<Value>();
        for (int i = 0; i < sp.getArgs().size(); i++) {
            args.add(new Value(sp.getArgs().get(i), nf));
        }
    }

    public void update(Environment env, boolean finalUpdate) {
        label.update(env, finalUpdate);
        pred.update(env, finalUpdate);
        for (int i = 0; i < args.size(); i++) {
            args.get(i).update(env, finalUpdate);
        }
    }

    public void addArg(Value a) {
        if (args == null)
            args = new LinkedList<Value>();
        args.add(a);
    }

    public boolean isNegated() {
        return negated;
    }

    public void setNegated(boolean negated) {
        this.negated = negated;
    }

    public Value getLabel() {
        return label;
    }

    public void setLabel(Value label) {
        this.label = label;
    }

    public Value getPred() {
        return pred;
    }

    public void setPred(Value pred) {
        this.pred = pred;
    }

    public List<Value> getArgs() {
        return args;
    }

    public void setArgs(List<Value> args) {
        this.args = args;
    }

    public String toString() {
        String res = "";
        res += label.toString() + ":" + pred.toString() + "(";
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) {
                res += ", ";
            }
            res += args.get(i).toString();
        }
        res += ")";
        return res;
    }

}
