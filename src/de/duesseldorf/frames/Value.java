/*
 *  File Value.java
 *
 *  Authors:
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:50:46 CEST 2007
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
package de.duesseldorf.frames;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.tag.Environment;
import de.tuebingen.tag.SemLit;

/**
 * Class referring to Values within AVMs A value can be a String, an integer, an
 * AVM, an atomic disjunction or a variable
 * 
 * @author parmenti
 *
 */
public class Value implements SemLit {

    public enum Kind {
        VAL, INT, AVM, ADISJ, VAR;
    }

    // the value is atomic (String):
    private String sVal;
    // the value is an integer:
    private Integer iVal;
    // the value is an AVM:
    private Fs avmVal;
    // the value is an atomic disjunction:
    private LinkedList<Value> adisj;
    // the value is a variable (String):
    private String varVal;
    // For facilitating checking, we give it a type:
    // sVal:=1 .... varVal:=5
    private Kind type;

    /**
     * Create a new Integer Value
     * 
     * @param i
     *            is the Integer value of the new Value.
     */
    public Value(int i) {
        sVal = null;
        iVal = new Integer(i);
        avmVal = null;
        adisj = null;
        varVal = null;
        type = Kind.INT;
    }

    /**
     * Create a new value that is either a variable or a string
     * 
     * @param stype
     *            is either Value.VAR, or Value.VAL.
     * @param s
     *            is either the variable stored in this value, or the atomic
     *            (String)
     */
    public Value(Kind stype, String s) {
        if (stype == Kind.VAR) {
            sVal = null;
            varVal = s;
            type = Kind.VAR;
        } else if (stype == Kind.VAL) {
            sVal = s;
            varVal = null;
            type = Kind.VAL;
        }
        iVal = null;
        avmVal = null;
        adisj = null;
    }

    /**
     * Create a new value that is a AVM
     * 
     * @param fs
     *            The AVM
     */
    public Value(Fs fs) {
        sVal = null;
        iVal = null;
        avmVal = fs;
        adisj = null;
        varVal = null;
        type = Kind.AVM;
    }

    /**
     * Create a new value that is a atomic disjunction
     * 
     * @param ad
     *            The atomic disjunction
     */
    public Value(LinkedList<Value> ad) {
        sVal = null;
        iVal = null;
        avmVal = null;
        adisj = ad;
        varVal = null;
        type = Kind.ADISJ;
    }

    /**
     * Create a new value that is the atomic disjunction a, bound by coref
     * 
     * @param ad
     * @param coref
     */
    public Value(LinkedList<Value> ad, String coref) {
        sVal = null;
        iVal = null;
        avmVal = null;
        adisj = ad;
        varVal = null;
        type = Kind.ADISJ;
        adisj.add(0, new Value(Kind.VAR, coref)); // we bind the adisj to a var
    }

    public Value(Value v) {
        sVal = v.getSVal();
        iVal = v.getIVal();
        if (v.getAvmVal() != null) {
            avmVal = new Fs(v.getAvmVal());
        } else {
            avmVal = null;
        }
        LinkedList<Value> vadisj = v.getAdisj();
        if (vadisj != null) {
            adisj = new LinkedList<Value>();
            for (int i = 0; i < vadisj.size(); i++) {
                adisj.add(new Value(vadisj.get(i)));
            }
        } else {
            adisj = null;
        }
        if (v.getVarVal() != null) {
            varVal = v.getVarVal();
        }
        type = v.getType();
    }

    public Value(Value v, NameFactory nf) {

        if (v.getSVal() != null && nf.isIn(v.getSVal())) // for semantic labels
            sVal = nf.getName(v.getSVal());
        else
            sVal = v.getSVal();
        iVal = v.getIVal();
        if (v.getAvmVal() != null) {
            avmVal = new Fs(v.getAvmVal(), nf);
        } else {
            avmVal = null;
        }
        LinkedList<Value> vadisj = v.getAdisj();
        if (vadisj != null) {
            adisj = new LinkedList<Value>();
            for (int i = 0; i < vadisj.size(); i++) {
                adisj.add(new Value(vadisj.get(i), nf));
            }
        } else {
            adisj = null;
        }
        if (v.getVarVal() != null) {
            varVal = nf.getName(v.getVarVal());
        }
        type = v.getType();

    }

    public boolean is(Kind whatKind) {
        return (whatKind == type);
    }

    public boolean equals(Object v) {
        boolean res = false;
        if (((Value) v).getType() == this.getType()) {
            switch (this.getType()) {
            case VAL:
                res = (this.getSVal().equals(((Value) v).getSVal()));
                break;
            case INT:
                res = (this.getIVal() == ((Value) v).getIVal());
                break;
            case AVM:
                res = (this.getAvmVal().equals(((Value) v).getAvmVal()));
                break;
            case ADISJ:
                if (((Value) v).getAdisj().size() == this.getAdisj().size()) {
                    String[] adS = new String[this.getAdisj().size()];
                    String[] otS = new String[((Value) v).getAdisj().size()];
                    for (int i = 0; i < ((Value) v).getAdisj().size(); i++) {
                        adS[i] = this.getAdisj().get(i).toString();
                        otS[i] = this.getAdisj().get(i).toString();
                    }
                    Arrays.sort(adS);
                    Arrays.sort(otS);
                    res = true;
                    for (int i = 0; i < adS.length; i++) {
                        res &= adS[i].equals(otS[i]);
                    }
                }
                break;
            case VAR:
                res = (this.getVarVal().equals(((Value) v).getVarVal()));
                break;
            default:// skip
            }
        }
        return res;
    }

    public String toString() {
        String res = "";
        if (sVal != null) {
            // atomic value
            res = sVal;
        } else if (iVal != null) {
            // integer value
            res = iVal.toString();
        } else if (avmVal != null) {
            // AVM value
            res = "[" + avmVal.toString() + "]";
        } else if (adisj != null) {
            // atomic disjunction
            res += "@{";
            for (int i = 0; i < adisj.size(); i++) {
                res += adisj.get(i) + "|";
            }
            // eventually, we remove the last "|":
            if (adisj.size() > 0) {
                res = res.substring(0, (res.length() - 1));
            }
            res += "}";
        } else {
            // variable
            res = varVal;
        }
        return res;
    }

    public String getSVal() {
        return sVal;
    }

    public void setSVal(String val) {
        sVal = val;
    }

    public Integer getIVal() {
        return iVal;
    }

    public void setIVal(Integer val) {
        iVal = val;
    }

    public Fs getAvmVal() {
        return avmVal;
    }

    public void setAvmVal(Fs avmVal) {
        this.avmVal = avmVal;
    }

    public LinkedList<Value> getAdisj() {
        return adisj;
    }

    public void setAdisj(LinkedList<Value> adisj) {
        this.adisj = adisj;
    }

    /**
     * 
     * @return a variable if the Value is a variable, null otherwise.
     */
    public String getVarVal() {
        return varVal;
    }

    public void setVarVal(String varVal) {
        this.varVal = varVal;
    }

    public Kind getType() {
        return type;
    }

    public void setType(Kind kind) {
        this.type = kind;
    }

    public boolean hasCycle(String var, Environment env) {
        // we look for cycles
        boolean cycle = false;
        List<String> trace = new LinkedList<String>();
        env.getTrace(this, trace);
        if (trace.contains(var)) {
            cycle = true;
        }
        return cycle;
    }

    /**
     * @param finalUpdate
     */
    public void update(Environment env, boolean finalUpdate) {
        if (this.is(Kind.VAR)) {
            // lookup in the environment
            Value val = env.deref(this);
            // value update (substitution)
            if (finalUpdate && val.getAdisj() != null
                    && val.getAdisj().getFirst().is(Kind.VAR)) {
                val.getAdisj().remove();
            }
            this.adisj = val.getAdisj();
            this.avmVal = val.getAvmVal();
            this.iVal = val.getIVal();
            this.sVal = val.getSVal();
            this.type = val.getType();
            this.varVal = val.getVarVal();
        } else if (this.is(Kind.VAL)) {
            // for semantic labels (which are stored in the environment before
            // display):
            Value val = env.get(this.getSVal());
            if (val != null) {
                this.adisj = val.getAdisj();
                this.avmVal = val.getAvmVal();
                this.iVal = val.getIVal();
                this.sVal = val.getSVal();
                this.type = val.getType();
                this.varVal = val.getVarVal();
            }
        } else if (this.is(Kind.ADISJ)) {
            // for bound atomic disjunctions
            if (this.getAdisj().getFirst().is(Kind.VAR)) {
                Value aVar = this.getAdisj().getFirst();
                Value val = env.deref(aVar);
                // if the variable is unbound
                // c.f. binding made at anchoring and lost since new
                // environments are used for derived tree building
                if (val.equals(aVar)) {
                    env.bind(aVar.getVarVal(), this);
                    val = this;
                }
                if (finalUpdate && val.getAdisj() != null
                        && val.getAdisj().getFirst().is(Kind.VAR)) {
                    val.getAdisj().remove();
                }
                this.adisj = val.getAdisj();
                this.avmVal = val.getAvmVal();
                this.iVal = val.getIVal();
                this.sVal = val.getSVal();
                this.type = val.getType();
                this.varVal = val.getVarVal();
            }
        }
    }

}
