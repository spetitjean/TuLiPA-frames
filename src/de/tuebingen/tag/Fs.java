/*
 *  File Fs.java
 *
 *  Authors:
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:44:40 CEST 2007
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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.duesseldorf.frames.Type;
import de.tuebingen.anchoring.NameFactory;

/**
 * Represents an attribute value matrix.
 * 
 * @author wmaier, parmenti
 *
 */
public class Fs {

    private Hashtable<String, Value> AVlist;
    private Type type;
    private boolean is_typed;
    // we handle corefs only for typed feature structures to make it easier
    private Value coref;

    /**
     * @param capacity
     *            an int referring to the max capacity of the AVM
     */
    public Fs(int capacity) {
        AVlist = new Hashtable<String, Value>(capacity);
        type = null;
        coref = null;
        is_typed = false;
    }

    public Fs(int capacity, Type type, Value coref) {
        AVlist = new Hashtable<String, Value>(capacity);
        this.type = type;
        this.coref = coref;
        if (type != null) {
            is_typed = true;
        } else {
            is_typed = false;
        }
    }

    public Fs(Hashtable<String, Value> avm) {
        AVlist = avm;
        this.type = null;
        is_typed = false;
        this.coref = null;
    }

    public Fs(Hashtable<String, Value> avm, Type type, Value coref) {
        AVlist = avm;
        if (type != null) {
            is_typed = true;
        } else {
            is_typed = false;
        }
        this.type = type;
        this.coref = coref;
    }

    public Fs(Fs fs) {
        if (fs == null) {
            type = null;
            is_typed = false;
            coref = null;
            AVlist = new Hashtable<String, Value>();
        } else {
            type = fs.getType();
            is_typed = fs.isTyped();
            coref = fs.getCoref();
            AVlist = new Hashtable<String, Value>(fs.getAVlist().size());
            Set<String> keys = fs.getAVlist().keySet();
            Iterator<String> i = keys.iterator();
            while (i.hasNext()) {
                String k = (String) i.next();
                Value v = fs.getFeat(k);
                AVlist.put(new String(k), new Value(v));
            }
        }
    }

    public Fs(Fs fs, NameFactory nf) {
        if (fs == null) {
            this.type = null;
            this.is_typed = false;
            this.coref = null;
            AVlist = new Hashtable<String, Value>();
        } else {
            this.type = fs.getType();
            this.is_typed = fs.isTyped();
            if (fs.getCoref() != null) {
                this.coref = new Value(fs.getCoref(), nf);
            }
            AVlist = new Hashtable<String, Value>(fs.getAVlist().size());
            Set<String> keys = fs.getAVlist().keySet();
            Iterator<String> i = keys.iterator();
            while (i.hasNext()) {
                String k = (String) i.next();
                Value v = fs.getFeat(k);
                AVlist.put(new String(k), new Value(v, nf));
            }
        }
    }

    public int getSize() {
        return (AVlist.size());
    }

    /**
     * method that stores a new pair (key, val) into the avm.
     * NB: if the key already is in the AVM, the new entry is not stored
     * 
     * @param key,
     *            val
     *            key is the key (String) and val the value (Val)
     * 
     */
    public void setFeat(String key, Value val) {
        if (AVlist.containsKey(key)) {
            System.out.println(
                    "Key : " + key + " already used, feature skipped.");
        } else {
            AVlist.put(key, val);
        }
    }

    /**
     * method that lookup a feature in the FS
     */
    public Value getFeat(String key) {
        if (AVlist.containsKey(key)) {
            return AVlist.get(key);
        } else {
            return null;
        }
    }

    public Value getConstFeat(String key) {
        Value res = null;
        if (AVlist.containsKey(key) && AVlist.get(key).is(Value.VAL))
            res = AVlist.get(key);
        else if (AVlist.containsKey(key) && AVlist.get(key).is(Value.INT))
            res = AVlist.get(key);
        return res;
    }

    public void removeFeat(String key) {
        AVlist.remove(key);
    }

    public boolean hasFeat(String key) {
        return AVlist.containsKey(key);
    }

    public String getCategory() {
        if (AVlist.containsKey("cat")) {
            if (AVlist.get("cat").is(Value.VAL))
                return AVlist.get("cat").getSVal();
            else if (AVlist.get("cat").is(Value.ADISJ))
                return AVlist.get("cat").getAdisj().get(1).toString(); // arbitrary
                                                                       // for
                                                                       // now
            else
                return "";
        } else {
            return "";
        }
    }

    public void removeCategory() {
        if (AVlist.containsKey("cat")) {
            this.removeFeat("cat");
        }
        Iterator<String> feats = AVlist.keySet().iterator();
        while (feats.hasNext()) {
            String f = feats.next();
            Value v = AVlist.get(f);
            if (v.is(Value.AVM))
                v.getAvmVal().removeCategory();
        }
    }

    public void propagateCategory(String cat) {
        if (!AVlist.containsKey("cat")) {
            this.setFeat("cat", new Value(Value.VAL, cat));
        }
        Iterator<String> feats = AVlist.keySet().iterator();
        while (feats.hasNext()) {
            String f = feats.next();
            Value v = AVlist.get(f);
            if (v.is(Value.AVM))
                v.getAvmVal().propagateCategory(cat);
        }
    }

    /**
     * Retrieves the value of the cat feature
     * according to the fs to look in
     * (top or bot for aux trees)
     * (top for initial trees)
     */
    public String getCategory(boolean isAux, int fs) {
        String res = this.getCategory();

        if (isAux) { // for auxiliary trees
            switch (fs) {
            case TagNode.TOP:
                if (AVlist.containsKey("top") && AVlist.get("top").getAvmVal()
                        .getFeat("cat") != null) {
                    res = AVlist.get("top").getAvmVal().getFeat("cat")
                            .getSVal();
                }
                break;
            case TagNode.BOT:
                if (AVlist.containsKey("bot") && AVlist.get("bot").getAvmVal()
                        .getFeat("cat") != null) {
                    res = AVlist.get("bot").getAvmVal().getFeat("cat")
                            .getSVal();
                }
                break;
            default:// skip
            }
        } else { // for substitution trees
            if (AVlist.containsKey("top")
                    && AVlist.get("top").getAvmVal().getFeat("cat") != null) {
                res = AVlist.get("top").getAvmVal().getFeat("cat").getSVal();
            }
        }
        return res;
    }

    /**
     * Method used to get a feature value for restricting the adjunction
     * sets
     * while converting the TT-MCTAG to RCG
     * 
     * if f is neither in the Fs or, if its value is not a constant then it
     * returns null
     */
    public String getFeatVal(String f, int fs) {

        String res = null;
        switch (fs) {
        case TagNode.TOP:
            if (AVlist.containsKey("top")
                    && AVlist.get("top").getAvmVal().getFeat(f) != null) {
                switch (AVlist.get("top").getAvmVal().getFeat(f).getType()) {
                case Value.INT:
                    res = AVlist.get("top").getAvmVal().getFeat(f).getIVal()
                            + "";
                    break;
                case Value.VAL:
                    res = AVlist.get("top").getAvmVal().getFeat(f).getSVal();
                    break;
                default:// skip
                }
            }
            break;
        case TagNode.BOT:
            if (AVlist.containsKey("bot")
                    && AVlist.get("bot").getAvmVal().getFeat(f) != null) {
                switch (AVlist.get("bot").getAvmVal().getFeat(f).getType()) {
                case Value.INT:
                    res = AVlist.get("bot").getAvmVal().getFeat(f).getIVal()
                            + "";
                    break;
                case Value.VAL:
                    res = AVlist.get("bot").getAvmVal().getFeat(f).getSVal();
                    break;
                default:// skip
                }
            }
            break;
        default:
            if (AVlist.containsKey(f) && AVlist.get(f) != null) {
                res = AVlist.get(f).getSVal();
            }
        }
        return res;
    }

    public int getAdjStatus() {
        int res = TagNode.NOCST;
        Value top = this.getFeat("top");
        Value bot = this.getFeat("bot");
        if (top != null && bot != null) {
            Fs realTop = top.getAvmVal();
            Fs realBot = bot.getAvmVal();
            Iterator<String> it = realTop.getAVlist().keySet().iterator();
            while (it.hasNext()) {
                String feat = it.next();
                Value topVal = realTop.getConstFeat(feat);
                Value botVal = realBot.getConstFeat(feat);
                if (topVal != null && botVal != null
                        && !(topVal.equals(botVal)))
                    res = TagNode.NADJ;
            }
        }
        return res;
    }

    public Set<String> getKeys() {
        return AVlist.keySet();
    }

    public String toString() {
        String res = "";

        if (isTyped()) {
            res = "(" + coref + ")" + res + type + " - ";

        }

        Set<String> keys = AVlist.keySet();
        Iterator<String> i = keys.iterator();
        while (i.hasNext()) {
            String k = (String) i.next();
            res += k + " = " + AVlist.get(k).toString() + ", ";
        }
        if (res.length() > 2) {
            // we remove the last ", "
            res = res.substring(0, (res.length() - 2));
        }
        return res;
    }

    public Type getType() {
        return this.type;
    }

    public Value getCoref() {
        return this.coref;
    }

    public void setCoref(Value coref) {
        this.coref = coref;
    }

    public boolean isTyped() {
        return this.is_typed;
    }

    /**
     * Unifies two feature structures according to an environment
     * 
     * @param fs1,
     *            fs2, env
     *            fs1 is a feature structure containing a hashtable of
     *            String,Value
     *            fs2 is a feature structure containing a hashtable of
     *            String,Value
     *            env is an environment global to the 2 feature structures,
     *            and that
     *            is used to store the variables' bindings.
     */
    public static Fs unify(Fs fs1, Fs fs2, Environment env)
            throws UnifyException {
        Hashtable<String, Value> avm1 = fs1.getAVlist();
        Hashtable<String, Value> avm2 = fs2.getAVlist();

        // the resulting avm:
        Hashtable<String, Value> res = new Hashtable<String, Value>(
                avm1.size() + avm2.size());
        // a temporary avm used to store non-common features:
        Hashtable<String, Value> todo = new Hashtable<String, Value>();

        // 1. loop through avm1
        Set<String> keys = avm1.keySet();
        Iterator<String> i = keys.iterator();
        while (i.hasNext()) {
            String k = (String) i.next();
            if (avm2.containsKey(k)) { // k is a common feature, we unify its
                                       // values
                Value nval = null;
                try {
                    // exception caught and re-thrown to extend the error
                    // message
                    nval = Value.unify(avm1.get(k), avm2.get(k), env);
                } catch (UnifyException e) {
                    throw new UnifyException(
                            "feature " + k + ": " + e.getMessage());
                }
                res.put(k, nval);
            } else { // we keep it for later
                todo.put(k, avm1.get(k));
            }
        }
        // 2. loop through avm2
        keys = avm2.keySet();
        i = keys.iterator();
        while (i.hasNext()) {
            String k = (String) i.next();
            if (!(avm1.containsKey(k))) {
                todo.put(k, avm2.get(k));
            } // no else since the common features have already been processed
        }
        // 3. loop through delayed features
        // that is, features that appear only in one avm
        keys = todo.keySet();
        i = keys.iterator();
        while (i.hasNext()) {
            String k = (String) i.next();
            Value v = todo.get(k);
            if (v.is(Value.VAR)) { // if it is a variable
                Value w = env.deref(v);
                if (w == null) { // v is not bound
                    res.put(k, v);
                } else { // v is bound
                    res.put(k, w);
                }
            } else { // v is not a variable
                res.put(k, v);
            }
        }

        // 4. set the type of the resulting FS
        // TODO sth useful for the types
        Type resType = null;
        if (fs1.isTyped() && !fs2.isTyped()) {
            resType = fs1.getType();
        } else if (!fs1.isTyped() && fs2.isTyped()) {
            resType = fs2.getType();
            // } else if (fs1.isTyped() && fs2.isTyped()) {
            // resType =
        }

        // 5. set the coref of the resulting FS
        Value resCoref;
        if (fs1.isTyped()) {
            resCoref = fs1.getCoref();
            fs2.setCoref(fs1.getCoref());
        } else {
            resCoref = fs2.getCoref();
        }

        // finally, all the features have been processed, we return the avm res:
        return (new Fs(res, resType, resCoref));
    }

    /**
     * This method update some FS according to an environment (ie a list of
     * bindings)
     */
    public static Fs updateFS(Fs fs, Environment env, boolean finalUpdate)
            throws UnifyException {
        // System.err.println("updating [" + fs.toString() + "] env: " +
        // env.toString());
        // System.out.println("Starting UpdateFS");
        Fs res;
        if (fs.isTyped()) {
            Value coref = fs.getCoref();
            Value vderef = env.deref(coref);
            if (!(vderef.equals(fs.getCoref()))) { // it is bound:
                res = new Fs(fs.getSize(), fs.getType(),
                        Value.unify(vderef, coref, env));
            } else { // it is not:
                res = new Fs(fs.getSize(), fs.getType(), vderef);
                // This was added for testing
                // env.bind(vderef,coref);
            }
            // System.out.println("Deref of "+fs.getCoref()+":
            // "+env.deref(fs.getCoref()));
        } else {
            res = new Fs(fs.getSize());
        }
        Hashtable<String, Value> avm = fs.getAVlist();
        Set<String> keys = avm.keySet();
        Iterator<String> i = keys.iterator();
        while (i.hasNext()) {
            String k = (String) i.next();
            Value fval = avm.get(k);

            // System.err.println("Processing ... " + k+":"+fval.toString());

            switch (fval.getType()) {
            case Value.VAL: // for semantic labels
                fval.update(env, finalUpdate);
                res.setFeat(k, fval);
                break;
            case Value.VAR:
                // if the feature value is a variable,
                // we look if it is bound to something in the environment
                Value v = env.deref(fval);

                if (!(v.equals(fval))) { // it is bound:
                    res.setFeat(k, Value.unify(fval, v, env));
                } else { // it is not:
                    // System.err.println("Variable not bound ... " + k + ":"
                    // + fval.toString());
                    res.setFeat(k, fval);
                    // This was added for testing
                    // env.bind(k,fval);
                }
                break;
            case Value.AVM: // the value is an avm, we go on updating
                res.setFeat(k, new Value(
                        updateFS(fval.getAvmVal(), env, finalUpdate)));
                break;
            case Value.ADISJ:
                fval.update(env, finalUpdate);
                res.setFeat(k, fval);
                break;
            default:
                res.setFeat(k, fval);
            }
        }
        // System.out.println("Finished UpdateFS");

        return res;
    }

    public Hashtable<String, Value> getAVlist() {
        return AVlist;
    }

    public void setAVlist(Hashtable<String, Value> vlist) {
        AVlist = vlist;
    }

    public boolean equals(Object fs) {
        boolean res = true;
        Set<String> keys = AVlist.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String f = it.next();
            if (!(((Fs) fs).hasFeat(f))) {
                res = false;
            } else {
                res &= ((Fs) fs).getFeat(f).equals(AVlist.get(f));
            }
        }
        return res;
    }

    public static List<Fs> mergeFS(List<Fs> frames) {
        boolean cont = true;
        List<Value> seen = new ArrayList<Value>();
        Hashtable<Value, Fs> corefs = new Hashtable<Value, Fs>();
        List<Fs> merge = new ArrayList<Fs>();
        while (cont) {
            cont = false;
            for (Fs fs : frames) {
                boolean b = fs.mergeFS1(seen, corefs);
                if (b) {
                    cont = true;
                }
            }
        }
        return merge;
    }

    public boolean mergeFS1(List<Value> seen, Hashtable<Value, Fs> corefs) {
        boolean cont = false;
        Value coref = this.coref;
        if (!seen.contains(coref)) {
            // System.out.println("Add "+coref.getVarVal());
            seen.add(coref);
        } else {
            if (!corefs.keySet().contains(coref)) {
                // System.out.println("Found coreference with
                // "+coref.getVarVal()+", I will solve it next round.");
                cont = true;
            }
        }
        corefs.put(coref, this);
        Iterator<String> i = this.AVlist.keySet().iterator();
        while (i.hasNext()) {
            String f = i.next();
            Value v = this.AVlist.get(f);
            if (v.is(Value.AVM)) {
                v.getAvmVal().mergeFS1(seen, corefs);
            }

            if (v.is(Value.VAR)) {
                if (!seen.contains(v)) {
                    // System.out.println("Add "+v.getVarVal());
                    seen.add(v);
                } else {
                    // System.out.println("Found coreference with
                    // "+v.getVarVal());
                    if (corefs.keySet().contains(v)) {
                        this.AVlist.put(f, new Value(corefs.get(v)));
                    }
                }
            }

        }
        return cont;
    }
}
