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
package de.tuebingen.tag;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.duesseldorf.frames.TypeHierarchy;
import de.tuebingen.anchoring.NameFactory;

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

    public static Value unify(Value a, Value b, Environment env)
            throws UnifyException {
        return unify(a, b, env, null, new HashSet<Value>());
    }

    public static Value unify(Value a, Value b, Environment env,
            TypeHierarchy tyHi) throws UnifyException {
        return unify(a, b, env, tyHi, new HashSet<Value>());
    }

    /**
     * Performs unification between values and returns a value
     * 
     * @param a
     *            is a Value
     * @param b
     *            is a Value
     * @param env
     *            is an Environment object where to interpret a and b
     */
    public static Value unify(Value a, Value b, Environment env,
            TypeHierarchy tyHi, Set<Value> seen) throws UnifyException {

        // System.err.println("Unification: " + a.toString() + " and " +
        // b.toString());
        if (a.equals(b)) { // no need to compute anything
            return b;
        }
        Value res = null;
        switch (a.getType()) {

        case VAL: // a is an atom
            switch (b.getType()) {
            case VAL: // b is an atom
                if (!(a.getSVal().equals(b.getSVal()))) {
                    throw new UnifyException(a.toString(), b.toString());
                } else {
                    res = new Value(Kind.VAL, a.getSVal());
                }
                break;
            case VAR: // b is a variable, we dereference it
                Value bb = env.deref(b);
                // if b is unbound, we bind it to a
                if (bb.equals(b)) {
                    env.bind(b.getVarVal(), a);
                    res = a;
                } else { // b is already bound, the values must unify !
                    res = Value.unify(a, bb, env, tyHi, seen);
                }
                break;
            case ADISJ:
                res = unify(b, a, env, tyHi, seen);
                break;
            default:
                throw new UnifyException(a.toString(), b.toString());
            }
            break;
        case INT: // a is an integer
            switch (b.getType()) {
            case INT: // b is an integer
                if (!(a.getIVal() == b.getIVal())) {
                    throw new UnifyException(a.toString(), b.toString());
                } else {
                    res = new Value(a.getIVal());
                }
                break;
            case VAR: // b is a variable
                Value bb = env.deref(b);
                // if b is unbound, we bind it to a
                if (bb.equals(b)) {
                    env.bind(b.getVarVal(), a);
                    res = a;
                } else { // b is already bound, the values must match !
                    if (bb.is(Kind.INT) && (a.getIVal() == bb.getIVal())) {
                        // they do match:
                        res = new Value(a.getIVal());
                    } else {
                        // they do not:
                        throw new UnifyException(a.toString(), b.toString());
                    }
                }
                break;
            default:
                throw new UnifyException(a.toString(), b.toString());
            }
            break;
        case AVM: // a is an avm
            // System.out.println("A is an AVM");
            switch (b.getType()) {
            case AVM: // b is an avm
                res = new Value(Fs.unify(a.getAvmVal(), b.getAvmVal(), env,
                        tyHi, seen));
                /*
                 * // Uncaught exception (caught in Fs' unify method) try { res
                 * = new Value(Fs.unify(a.getAvmVal(), b.getAvmVal(), env)); }
                 * catch (UnifyException e) {
                 * System.out.println(e.getMessage()); // this error is caught
                 * here for now // but it will later be given to the calling
                 * method }
                 */
                break;

            case VAR: // b is a variable
                // System.out.println("B is a variable");
                Value bb = env.deref(b);
                // System.out.println(bb);
                // if b is unbound, we bind it to a
                if (bb.equals(b)) {
                    // System.out.println("B is unbound");
                    // Simon: I added this
                    // This might lead to problems when 2 bound variables refer
                    // to two different FS
                    // One solution would be to store the FS somewhere in the
                    // environment, like
                    // b <-> a.coref
                    // @b <-> a
                    if (a.getAvmVal().getCoref() != null) {
                        // System.out.println("Extra binding for coref:
                        // "+bb.getVarVal()+" and "+a.getAvmVal().getCoref());
                        // System.out.println("AVM for
                        // "+a.getAvmVal().getCoref()+" is "+a.getAvmVal());
                        // System.out.println("Deref
                        // "+a.getAvmVal().getCoref()+" is
                        // "+env.deref(a.getAvmVal().getCoref()));
                        if (bb.getVarVal() != env
                                .deref(a.getAvmVal().getCoref()).getVarVal()) {
                            env.bind(bb.getVarVal(),
                                    env.deref(a.getAvmVal().getCoref()));
                        } else {
                            // System.out.println("Not binding");
                            // System.out.println("@"+bb.getVarVal());
                            // System.out.println(env.deref(new
                            // Value(5,"@"+bb.getVarVal())));
                            // System.out.println("Type of this: "+env.deref(new
                            // Value(5,"@"+bb.getVarVal())).getType());
                            // env.bind("@"+bb.getVarVal(),new
                            // Value(a.getAvmVal()));
                            if (env.deref(
                                    new Value(Kind.VAR, "@" + bb.getVarVal()))
                                    .getType() == Kind.AVM) {
                                // System.out.println("Unifying AVM with bound
                                // AVM");
                                env.bind("$" + bb.getVarVal(),
                                        new Value(Fs.unify(a.getAvmVal(),
                                                env.deref(new Value(Kind.VAR,
                                                        "$" + bb.getVarVal()))
                                                        .getAvmVal(),
                                                env, tyHi, seen)));
                            } else {
                                // System.out.println("Binding a new AVM");
                                env.bind("$" + bb.getVarVal(),
                                        new Value(a.getAvmVal()));
                            }
                            // System.out.println("Environment: "+env);
                        }
                        // env.bind(a.getAvmVal().getCoref().getVarVal(),a);
                    } else {
                        // End
                        env.bind(bb.getVarVal(), a);
                    }
                    res = a;
                } else { // b is already bound, the values must match !
                    if (bb.is(Kind.AVM)) { // let us see if they do:
                        res = new Value(Fs.unify(a.getAvmVal(), bb.getAvmVal(),
                                env, tyHi, seen));
                        /*
                         * // Uncaught exception (caught in Fs' unify method)
                         * try { res = new Value(Fs.unify(a.getAvmVal(),
                         * x.getAvmVal(), env)); } catch (UnifyException e){
                         * System.out.println(e.getMessage()); // this error is
                         * caught here for now // but it will later be given to
                         * the calling method }
                         */
                    } else {
                        if (bb.is(Kind.VAR)) {
                            res = new Value(unify(a, bb, env, tyHi, seen));
                        } else {// they do not:
                            throw new UnifyException(a.toString(),
                                    b.toString());
                        }
                    }
                }

                break;
            default:
                throw new UnifyException(a.toString(), b.toString());
            }
            break;
        case ADISJ: // a is an atomic disjunction
            // System.err.println("Unifying ... " + a.toString() + " and " +
            // b.toString());

            // we check whether a is currently bound with some variable
            Value aMaybeVar = (a.getAdisj().size() > 0
                    && a.getAdisj().getFirst().is(Kind.VAR))
                            ? a.getAdisj().getFirst() : null;
            if (aMaybeVar != null) {
                Value v = env.deref(aMaybeVar);
                // NB: aMaybeVar can be unbound ! (e.g. the atomic disjunction
                // has been unified with a variable during anchoring)
                // in this case, we rebuild the binding between aMaybeVar and a
                if (v.equals(aMaybeVar)) {
                    env.bind(aMaybeVar.getVarVal(), a);
                    v = a;
                }
                if (!(v.equals(a))) {
                    return unify(v, b, env, tyHi, seen);
                }
            }
            // if a is either not bound or bound to a free variable:
            switch (b.getType()) {
            case VAL: // b is an atomic value
                if (a.getAdisj().contains(b)) {
                    res = b;
                    if (aMaybeVar != null) {
                        env.bind(aMaybeVar.getVarVal(), res);
                    }
                    // System.err.println(" adisj + val ... ==> " +
                    // res.toString());
                } else {
                    throw new UnifyException(a.toString(), b.toString());
                }
                break;
            case ADISJ: // b is an atomic disjunction
                // before looking for the intersection,
                // we check whether the atomic disjunction is bound to a
                // variable
                // if this is the case, the first element of the adisj is the
                // variable
                LinkedList<Value> bdisj = b.getAdisj();
                Value bMaybeVar = (b.getAdisj().size() > 0
                        && b.getAdisj().getFirst().is(Kind.VAR))
                                ? b.getAdisj().getFirst() : null;
                if (bMaybeVar != null) {
                    Value w = env.deref(bMaybeVar);
                    if (w.equals(bMaybeVar)) {
                        env.bind(bMaybeVar.getVarVal(), b);
                        w = b;
                    }
                    if (!(w.equals(b))) {
                        return unify(a, w, env, tyHi, seen);
                    }
                }
                // here, b is either not bound or bound to a free variable!
                // we compute the intersection:
                LinkedList<Value> intersec = new LinkedList<Value>();
                for (int i = 0; i < bdisj.size(); i++) {
                    Value z = bdisj.get(i);
                    if (!(z.is(Kind.VAR)) && a.getAdisj().contains(z)) {
                        intersec.add(z);
                    }
                }
                if (intersec.size() > 0) {
                    if (intersec.size() == 1) { // the intersection is a single
                                                // value:
                        res = intersec.getFirst();
                        // ----------------------
                        if (aMaybeVar != null)
                            env.bind(aMaybeVar.getVarVal(), res);
                        if (bMaybeVar != null)
                            env.bind(bMaybeVar.getVarVal(), res);
                        // ----------------------
                    } else {
                        res = new Value(intersec);
                        // ----------------------
                        if (aMaybeVar != null) {
                            res.getAdisj().addFirst(aMaybeVar);
                            env.bind(aMaybeVar.getVarVal(), res);
                            if (bMaybeVar != null
                                    && !(bMaybeVar.equals(aMaybeVar)))
                                env.bind(bMaybeVar.getVarVal(), aMaybeVar);
                        } else if (bMaybeVar != null) {
                            res.getAdisj().addFirst(bMaybeVar);
                            env.bind(bMaybeVar.getVarVal(), res);
                        }
                        // ----------------------
                    }
                } else {
                    throw new UnifyException(a.toString(), b.toString());
                }
                // System.err.println(" adisj + adisj ... ==> " +
                // res.toString());
                break;
            case VAR: // b is a variable
                Value bb = env.deref(b);
                // if b is unbound, we either bind it to a or a's variable
                if (bb.equals(b)) {
                    if (aMaybeVar != null) {
                        env.bind(b.getVarVal(), aMaybeVar);
                    } else {
                        // the variable b is now bound to the adisj a:
                        a.getAdisj().addFirst(b);
                        env.bind(b.getVarVal(), a);
                    }
                    res = a;
                    // System.err.println(" adisj + var ... ==> " +
                    // res.toString());
                } else { // b is already bound, the values must unify !
                    res = unify(a, bb, env, tyHi, seen);
                }
                break;
            default:
                throw new UnifyException("Unification failure (value) between "
                        + a.toString() + " and " + b.toString());
            }
            // System.err.println(" ... ==> " + res.toString() + " " +
            // env.toString());
            break;
        case VAR: // a is a variable
            // System.out.println("VAR and VAR");
            // System.out.println("Deref "+a);
            Value aa = env.deref(a);
            // System.out.println("is "+aa);
            switch (b.getType()) {
            case VAR: // if a and b are both variables
                // System.out.println("Deref "+b);
                Value bb = env.deref(b);
                // System.out.println("is "+bb);
                if (aa.is(Kind.VAR) && bb.is(Kind.VAR)) {
                    if (aa.equals(a) && bb.equals(b)) {
                        // if both variables are unbound
                        env.bind(a.getVarVal(), b);
                        res = a;
                    } else {
                        if (!(hasCycle(aa.getVarVal(), bb, env))) {
                            res = Value.unify(aa, bb, env, tyHi, seen);
                        } else {
                            res = aa;
                        }
                    }
                } else {
                    res = Value.unify(aa, bb, env, tyHi, seen);
                }
                break;
            default:
                // the case has already been defined above
                res = Value.unify(b, aa, env, tyHi, seen);
            }
            break;
        default: // skip
        }
        return res;
    }

    public static boolean hasCycle(String var, Value val, Environment env) {
        // we look for cycles
        boolean cycle = false;
        List<String> trace = new LinkedList<String>();
        env.getTrace(val, trace);
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
