/*
 *  File Fs.java
 *
 *  Authors:
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>
 *     David Arps <david.arps@hhu.de>
 *     Simon Petitjean <petitjean@phil.hhu.de>
 *     
 *  Copyright:
 *     Yannick Parmentier, 2007
 *     David Arps, 2017
 *     Simon Petitjean, 2017
 *
 *  Last modified:
 *     2017
 *
 *  This file is part of the TuLiPA-frames system
 *     https://github.com/spetitjean/TuLiPA-frames
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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.tag.Environment;
import de.tuebingen.tag.TagNode;

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
     * like setFeat, but if the val is already in there, the new val is stored
     * 
     * @param key
     * @param val
     */
    public void replaceFeat(String key, Value val) {
        if (AVlist.containsKey(key)) {
            AVlist.remove(key);
        }
        AVlist.put(key, val);
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
        if (AVlist.containsKey(key) && AVlist.get(key).is(Value.Kind.VAL))
            res = AVlist.get(key);
        else if (AVlist.containsKey(key) && AVlist.get(key).is(Value.Kind.INT))
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
            if (AVlist.get("cat").is(Value.Kind.VAL))
                return AVlist.get("cat").getSVal();
            else if (AVlist.get("cat").is(Value.Kind.ADISJ))
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
            if (v.is(Value.Kind.AVM))
                v.getAvmVal().removeCategory();
        }
    }

    public void propagateCategory(String cat) {
        if (!AVlist.containsKey("cat")) {
            this.setFeat("cat", new Value(Value.Kind.VAL, cat));
        }
        Iterator<String> feats = AVlist.keySet().iterator();
        while (feats.hasNext()) {
            String f = feats.next();
            Value v = AVlist.get(f);
            if (v.is(Value.Kind.AVM))
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
                case INT:
                    res = AVlist.get("top").getAvmVal().getFeat(f).getIVal()
                            + "";
                    break;
                case VAL:
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
                case INT:
                    res = AVlist.get("bot").getAvmVal().getFeat(f).getIVal()
                            + "";
                    break;
                case VAL:
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
        Set<Value> seen = new HashSet<Value>();
        return toStringRec(seen);
    }

    public String toStringRec(Set<Value> seen) {
        String res = "";

        if (isTyped()) {
            res = "(" + coref + ")" + res + type + "\n ";
        }
        if (seen.contains(coref)) {
            // System.out.println("Stopping print because of recursion");
            return res;
        } else
            seen.add(coref);
        Set<String> keys = AVlist.keySet();
        Iterator<String> i = keys.iterator();
        while (i.hasNext()) {
            String k = (String) i.next();
            // System.out.println(seen);
            if (AVlist.get(k).getType() == Value.Kind.AVM)
                res += k + " = " + AVlist.get(k).getAvmVal().toStringRec(seen)
                        + "\n ";
            else
                res += k + " = " + AVlist.get(k).toString() + "\n ";
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

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isTyped() {
        return this.is_typed;
    }

    /**
     * Temporary method so that nothing breaks
     * 
     * @param fs1
     * @param fs2
     * @param env
     * @return
     * @throws UnifyException
     */
    public static Fs unify(Fs fs1, Fs fs2, Environment env)
            throws UnifyException {
        return unify(fs1, fs2, env, null, new HashSet<Value>());
    }

    /**
     * Temporary method so that nothing breaks
     * 
     * @param fs1
     * @param fs2
     * @param env
     * @param tyHi
     * @return
     * @throws UnifyException
     */
    public static Fs unify(Fs fs1, Fs fs2, Environment env, TypeHierarchy tyHi)
            throws UnifyException {
        return unify(fs1, fs2, env, tyHi, new HashSet<Value>());
    }

    /**
     * Unifies two feature structures according to an environment and a Type
     * Hierarchy. To compute the unification of untyped feature structurs, set
     * the value of the typehierarchy null.
     * 
     * 
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
     * @param tyHi
     *            the type hierarchy with respect to which fs1 and fs2 are
     *            unified inn case they are typed
     */
    public static Fs unify(Fs fs1, Fs fs2, Environment env, TypeHierarchy tyHi,
            Set<Value> seen) throws UnifyException {
        Hashtable<String, Value> avm1 = fs1.getAVlist();
        Hashtable<String, Value> avm2 = fs2.getAVlist();

        if (seen.contains(fs1.getCoref()) && fs1.getCoref() != null) {
            // System.out.println("Stopping unification because of recursion:
            // "+fs1);
            return fs1;
        } else {
            seen.add(fs1.getCoref());
            // seen.add(fs2.getCoref());
        }
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
                    // System.out.println("Unifying "+avm1.get(k)+" and
                    // "+avm2.get(k));
                    nval = Value.unify(avm1.get(k), avm2.get(k), env, tyHi,
                            seen);
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
            if (v.is(Value.Kind.VAR)) { // if it is a variable
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
        if (tyHi != null) {
            // baseline algo: delete when sth better works
            // if (fs1.isTyped() && !fs2.isTyped()) {
            // resType = fs1.getType();
            // } else if (!fs1.isTyped() && fs2.isTyped()) {
            // resType = fs2.getType();
            // // } else if (fs1.isTyped() && fs2.isTyped()) {
            // // resType =
            // }
            // System.out.println("Unification of " + fs1.getType() + " and "
            // + fs2.getType());
            if (fs1.isTyped() && fs2.isTyped()) {
                try {
                    // System.out.println("Unify types: " + fs1.getType() + "
                    // and "
                    // + fs2.getType());
                    resType = tyHi.leastSpecificSubtype(fs1.getType(),
                            fs2.getType(), env);
                    // System.out.println("Result: " + resType);
                    // System.out.println("Env: " + env);
                } catch (UnifyException e) {
                    System.err.println("Incompatible types: " + fs1.getType()
                            + " and " + fs2.getType());
                    throw new UnifyException();
                }
                // System.out.println("Unification of "+fs1.getType()+" and
                // "+fs2.getType()+" -> "+resType);
            } else if (fs1.isTyped()) {
                resType = fs1.getType();
            } else if (fs2.isTyped()) {
                resType = fs2.getType();
            }
        } else {
            if (fs1.isTyped() && fs2.isTyped()) {
                resType = fs1.getType();
            } else if (fs1.isTyped()) {
                resType = fs1.getType();
            } else if (fs2.isTyped()) {
                resType = fs2.getType();
            }
        }

        // System.out.println("Computed type: "+resType);
        // 5. set the coref of the resulting FS
        Value resCoref;
        if (fs1.isTyped()) {
            if (fs2.isTyped()) {
                // System.out.println("Unifying coreferences: "+fs1.getCoref()+"
                // and "+fs2.getCoref());
                resCoref = Value.unify(fs1.getCoref(), fs2.getCoref(), env,
                        tyHi, seen);
                // System.out.println("Done unify");
            } else {
                resCoref = fs1.getCoref();
                fs2.setCoref(fs1.getCoref());
            }
        } else {
            resCoref = fs2.getCoref();
        }

        // finally, all the features have been processed, we return the avm res:
        return (new Fs(res, resType, resCoref));
    }

    public static Fs updateFS(Fs fs, Environment env, boolean finalUpdate)
            throws UnifyException {
        return updateFS(fs, env, finalUpdate, new HashSet<Value>());
    }

    /**
     * This method update some FS according to an environment (ie a list of
     * bindings)
     */
    public static Fs updateFS(Fs fs, Environment env, boolean finalUpdate,
            Set<Value> seen) throws UnifyException {
        // System.err.println("updating [" + fs.toString() + "] env: " +
        // env.toString());
        // System.out.println("Updating FS: "+fs);
        // System.out.println("\n\n\nUpdating with seen: "+seen+"\nhave
        // "+fs.getCoref());

        Fs res = null;
        if (fs.isTyped()) {
            Value coref = fs.getCoref();
            Value vderef = env.deref(coref);
            Value typevar = fs.getType().getVar();
            Type newType = fs.getType();
            switch (typevar.getType()) {
            case VAL:
                // here we should unify the value with the type of the fs (after
                // converting it to a type)

                // convert the value to a type
                Set<String> elementaryTypes = new HashSet<String>();
                elementaryTypes.add(typevar.getSVal());
                Type otherType = new Type(elementaryTypes, typevar,
                        fs.getType().getTypeConstraints());
                newType = Situation.getTypeHierarchy()
                        .leastSpecificSubtype(fs.getType(), otherType, env);
                typevar = new Value(new Fs(0));
                newType.setVar(typevar);
                break;
            case VAR:
                // System.out.println("Trying deref on: " + typevar + " ("
                // + typevar.getType() + ")");
                Value typevarderef = env.deref(typevar);
                if (!typevarderef.equals(typevar)) {
                    newType = new Type(fs.getType().getElementaryTypes(),
                            Value.unify(typevarderef, typevar, env),
                            fs.getType().getTypeConstraints());
                } else {
                    newType = new Type(fs.getType().getElementaryTypes(),
                            typevarderef, fs.getType().getTypeConstraints());
                }
            }
            if (!(vderef.equals(fs.getCoref()))) { // it is bound:
                res = new Fs(fs.getSize(), newType,
                        Value.unify(vderef, coref, env));
            } else { // it is not:
                res = new Fs(fs.getSize(), newType, vderef);
                // This was added for testing
                // env.bind(vderef,coref);
            }
            // System.out.println("Deref of "+fs.getCoref()+":
            // "+env.deref(fs.getCoref()));
        } else {
            res = new Fs(fs.getSize());
        }

        if (fs.getCoref() != null && seen.contains(fs.getCoref())) {
            // System.out.println("Stopping update because of recursion: "+fs);
            // System.out.println(env);
            return res;
        } else
            seen.add(fs.getCoref());

        Hashtable<String, Value> avm = fs.getAVlist();
        Set<String> keys = avm.keySet();
        Iterator<String> i = keys.iterator();
        while (i.hasNext()) {
            String k = (String) i.next();
            Value fval = avm.get(k);

            // System.err.println("Processing ... " +
            // k+":"+fval.toString());

            switch (fval.getType()) {
            case VAL: // for semantic labels
                fval.update(env, finalUpdate);
                res.setFeat(k, fval);
                break;
            case VAR:
                // if the feature value is a variable,
                // we look if it is bound to something in the environment
                Value v = env.deref(fval);

                if (!(v.equals(fval))) { // it is bound:
                    res.setFeat(k, Value.unify(fval, v, env));
                } else { // it is not:
                    // System.err.println("Variable not bound ... " + k +
                    // ":"
                    // + fval.toString());
                    res.setFeat(k, fval);
                    // This was added for testing
                    // env.bind(k,fval);
                }
                break;
            case AVM: // the value is an avm, we go on updating
                // System.out.println("Updating FS [rec] ");
                res.setFeat(k, new Value(
                        updateFS(fval.getAvmVal(), env, finalUpdate, seen)));
                break;
            case ADISJ:
                fval.update(env, finalUpdate);
                res.setFeat(k, fval);
                break;
            default:
                res.setFeat(k, fval);
            }
        }
        return res;
    }

    public Hashtable<String, Value> getAVlist() {
        return AVlist;
    }

    public void setAVlist(Hashtable<String, Value> vlist) {
        AVlist = vlist;
    }

    public boolean equals(Object fs) {

        if (!(fs instanceof Fs)) {
            return false;
        }

        if (this.getCoref() == ((Fs) fs).getCoref()) {
            return true;
        }

        Set<String> keys = AVlist.keySet();
        Iterator<String> it = keys.iterator();
        boolean res = true;
        while (it.hasNext()) {
            String f = it.next();
            if (!(((Fs) fs).hasFeat(f))) {
                res = false;
            } else {
                // System.out.println("Checking equals: "+((Fs) fs).getFeat(f));
                res &= ((Fs) fs).getFeat(f).equals(AVlist.get(f));
            }
        }
        return res;
    }

    @Override
    public int hashCode() {
        return Objects.hash(AVlist, type, is_typed, coref);
    }

    public static List<Fs> mergeFS(List<Fs> frames, Environment env,
            NameFactory nf) {
        // System.out.println("Starting merging frames");
        List<Fs> newFrames = new LinkedList<Fs>();
        List<Fs> cleanFrames = new LinkedList<Fs>();
        for (Fs fs : frames) {
            if (fs.getType() != null) {
                // System.out.println("Cleaning "+fs);
                cleanFrames.add(fs);
            }
        }
        for (Fs fs : cleanFrames) {
            // System.out.println("Collecting corefs (l.723) in: "+fs);
            // System.out.println("with environment "+env);
            if (fs.collect_corefs(env, nf, new HashSet<Value>()) == false) {
                // If collect_corefs returns false, it means that
                // unification failed somewhere, so we discard the
                // solution
                // System.out.println("Failed to collect corefs");
                return null;
            }
            try {
                // System.out.println("Updating FS [0] ");
                updateFS(fs, env, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // We do as many update rounds as there are FS in our solution
        // that should be the upper bound
        int i = cleanFrames.size();
        while (i > 0) {
            // System.out.println("\n\n\nRounds of update corefs left : "+i);
            newFrames = new LinkedList<Fs>();
            for (Fs fs : cleanFrames) {
                Set<Value> seen = new HashSet<Value>();
                Fs new_fs = fs.update_corefs(env, seen);
                // System.out.println("Done update_corefs: "+new_fs);
                if (new_fs == null) {
                    // If the result of update_corefs is null, it is
                    // because unification failed somewhere, so we
                    // need to discard the solution

                    // System.out.println("Failed to update corefs");
                    return null;
                }
                newFrames.add(new_fs);
                // System.out.println("updated");
            }
            cleanFrames = newFrames;
            i--;

        }

        for (Fs cleanFrame : cleanFrames) {
            try {
                // System.out.println("Updating FS [1] ");
                updateFS(cleanFrame, env, true);
                cleanFrame.cleanCorefs();
                // System.out.println("Updated : " + cleanFrame);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // System.out.println("Environment in the end: "+env);

        return newFrames;
    }

    public void cleanCorefs() {
        cleanCorefs(new HashSet<Value>());
    }

    public void cleanCorefs(Set<Value> seen) {

        Iterator<String> i = this.AVlist.keySet().iterator();
        while (i.hasNext()) {
            String f = i.next();
            Value v = this.AVlist.get(f);
            if (v.is(Value.Kind.AVM)) {
                Fs fs1 = v.getAvmVal();
                if (seen.contains(fs1.getCoref())) {
                    this.AVlist.put(f, new Value(fs1.getCoref()));
                } else {
                    seen.add(fs1.getCoref());
                    fs1.cleanCorefs(seen);
                }
            }
        }
    }

    public boolean collect_corefs(Environment env, NameFactory nf,
            Set<Value> seen) {
        // If the current coref is not a pretty name, we update it
        Fs New = this;

        // System.out.println("Going on ");
        if (env.deref(New.coref).getVarVal().charAt(0) != '@') {
            String oldVar = env.deref(this.coref).getVarVal();
            // String newVar = env.getPnf().getNextName();
            String newVar = nf.getUniqueName();
            // System.out.println("New name: "+newVar);
            // env.deref(New.coref).setVarVal(newVar);
            Value newVarVal = new Value(Value.Kind.VAR, newVar);
            New.coref = newVarVal;
            env.bind(oldVar, newVarVal);
            seen.add(newVarVal);
        }

        // Go through all the frames
        // for each coref X found, we put it in the environment

        String atCoref = "$" + env.deref(this.coref);
        Value valCoref = new Value(Value.Kind.VAR, atCoref);

        // System.out.println("coref: "+atCoref);
        // System.out.println("$: "+env.deref(valCoref));

        if (env.deref(valCoref) != valCoref) {
            try {
                New = unify(env.deref(valCoref).getAvmVal(), New, env,
                        Situation.getTypeHierarchy(), new HashSet<Value>());
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        env.bind(atCoref, new Value(New));

        Iterator<String> i = New.AVlist.keySet().iterator();
        while (i.hasNext()) {
            String f = i.next();
            Value v = New.AVlist.get(f);
            if (v.is(Value.Kind.AVM)) {
                // System.out.println("l.822: "+v.getAvmVal());
                // System.out.println("Seen: "+seen);
                // if(!seen.contains(v.getAvmVal().getCoref()))
                v.getAvmVal().collect_corefs(env, nf, new HashSet<Value>());
            }
        }
        return true;
    }

    public Fs update_corefs(Environment env, Set<Value> seen) {
        // System.out.println("Updating corefs in "+ this);
        // System.out.println("Seen: "+seen);
        Fs result = this;
        String atCoref = "$" + env.deref(this.coref);
        Value valCoref = new Value(Value.Kind.VAR, atCoref);

        // System.out.println("Seen "+ seen);

        if (env.deref(valCoref).is(Value.Kind.AVM)) {
            // System.out.println("Trying to unify: "+this);
            // System.out.println("With : "+env.deref(valCoref).getAvmVal());
            try {
                if (this != env.deref(valCoref).getAvmVal()) {
                    result = unify(this, env.deref(valCoref).getAvmVal(), env,
                            Situation.getTypeHierarchy(), new HashSet<Value>());
                } else
                    result = this;
                // System.out.println("Binding ");
                env.bind("$" + env.deref(this.coref), new Value(result));
                // System.out.println("Done unify");

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        if (seen.contains(coref)) {
            // System.out.println("Update corefs stopped by recursion: "+coref);
            // System.out.println(env.deref(this.coref));
            return result;
            // ;
        } else
            seen.add(coref);

        Iterator<String> i = this.AVlist.keySet().iterator();
        while (i.hasNext()) {
            // System.out.println("New while loop");
            String f = i.next();
            Value v = this.AVlist.get(f);
            if (v.is(Value.Kind.AVM)) {
                // System.out.println("AVM, coref: "+v.getAvmVal().getCoref());
                // System.out.println("Seen: "+seen);
                // if(!seen.contains(v.getAvmVal().getCoref()))
                // seen=new HashSet<Value>();
                // v.getAvmVal().update_corefs(env,seen);
                // System.out.println("Done update_corefs, putting in AVlist");

                // if(!seen.contains(v.getAvmVal().getCoref()))
                // seen=new HashSet<Value>();
                result.AVlist.put(f,
                        new Value(v.getAvmVal().update_corefs(env, seen)));
                // else
                // result.AVlist.put(f,
                // new Value(v.getAvmVal()));

            }
            // System.out.println("Here l.809");
            if (v.is(Value.Kind.VAR)) {
                // System.out.println("Have a variable");
                String atVar = "$" + env.deref(v);
                Value valVar = new Value(Value.Kind.VAR, atVar);
                // System.out.println("Var: "+v);
                // var is a coreference, get the FS
                if (env.deref(valVar).getType() == Value.Kind.AVM) {
                    result.AVlist.put(f,
                            new Value(env.deref(valVar).getAvmVal()));
                }
                // var is not a coreference
                else {
                    result.AVlist.put(f, new Value(env.deref(v)));
                }
            }
            // System.out.println("Here l.826");
            // System.out.println(i);
            // System.out.println(i.hasNext());
        }
        // System.out.println("Here l.830");
        // System.out.println("Result: "+result);
        // System.out.println("Return ");
        return result;
    }
}
