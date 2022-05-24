/*
 *  File Environment.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *     Johannes Dellert <johannes.dellert@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Yannick Parmentier, 2007
 *     Johannes Dellert, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:44:09 CEST 2007
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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.duesseldorf.frames.UnifyException;
import de.duesseldorf.frames.Value;
import de.duesseldorf.frames.ValueTools;
import de.tuebingen.derive.PrettyNameFactory;

/**
 * Environment - version 1
 * <p>
 * Class providing an environment to compute unifications An environment is a
 * table associating to each variable either:
 * - itself if it is unbound
 * - a Value if it is bound (this value may be a variable of a constant)
 *
 * @author parmenti
 */
public class Environment {

    private Hashtable<String, Value> table;
    // only for pretty printing of variables and semantic labels:
    private List<String> semlabels;
    private PrettyNameFactory pnf;

    public Environment(int capacity) {
        table = new Hashtable<String, Value>(capacity);
    }

    /**
     * @param var
     * @return This method return either:
     * 1. the constant to which var is bound
     * 2. or the last variable occuring in a path of bindings
     * between variables
     * 3. or the variable itself if it is unbound
     */
    public Value deref(Value var) {

        Value res = var;
        // NB: the environment should only contain variables as keys
        String name = var.getVarVal();
        if (table.containsKey(name)) {
            // var is bound to something
            Value v = table.get(name);
            // System.out.println("Got deref "+v);
            if (v.is(Value.Kind.VAR)) {
                // // var is bound to a variable, we go on dereferencing
                // // I created a loop here somehow, this must be fixed better
                // System.out.println("v:"+v.toString());
                // System.out.println("name:"+name);
                // if (v.toString() != name) {
                // res = deref(v);
                // }
                // else{
                // res=v;
                // }
                res = deref(v);
            } else {
                // var is bound to the constant v!
                res = v;
            }

        }
        // System.err.println("deref on " + var.toString() + " returns ... " +
        // res.toString());
        return res;
    }

    /**
     * Puts a binding of the variable var to the Value val in this Environment.
     *
     * @param var
     * @param val
     */
    public void bind(String var, Value val) {
        // System.err.println("binding " + var.toString() + " and " +
        // val.toString());

        // System.out.println("Putting in the environment: "+var+"-"+val);

        table.put(var, val);
    }

    public Value get(String var) {
        return table.get(var);
    }

    /**
     * Follow the trace of xand add it to the trace list, as long as x is bound
     * to something in the environment.
     *
     * @param var
     * @param trace
     */
    public void getTrace(Value var, List<String> trace) {
        String key = var.getVarVal();
        if (table.containsKey(key)) { // x is bound to something
            Value x = table.get(key);
            if (x.is(Value.Kind.VAR)) {
                trace.add(x.getVarVal());
                // Same dirty fix here
                // if(x.toString()!=key){
                getTrace(x, trace);
                // }
            }
        }
    }

    public String toString() {
        String res = "";
        res += "\n Environment state : \n";
        Set<String> keys = table.keySet();
        Iterator<String> i = keys.iterator();
        while (i.hasNext()) {
            String k = i.next();
            res += " " + k + " <-> " + table.get(k).toString() + "\n";
        }
        res += "\n";
        res += "\n";
        return res;
    }

    public Hashtable<String, Value> getTable() {
        return table;
    }

    /**
     * Method merging e1 and e2, returning the result
     *
     * @param e1
     * @param e2
     * @return
     * @throws UnifyException
     */
    public static Environment merge(Environment e1, Environment e2)
            throws UnifyException {
        Environment newEnv = new Environment(1);
        Environment.addBindings(e1, newEnv);
        Environment.addBindings(e2, newEnv);
        return newEnv;
    }

    /**
     * method merging env into nenv
     *
     * @throws UnifyException
     */
    public static void addBindings(Environment env, Environment nenv)
            throws UnifyException {
        Hashtable<String, Value> eTable = env.getTable();
        Set<String> keys = eTable.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            // we look if the variable is bound
            String eVar = it.next();
            Value val = new Value(Value.Kind.VAR, eVar);
            Value eVal = env.deref(val);
            if (!(eVal.equals(val))) {
                // if it is, we unify the bound values in the new environment
                ValueTools.unify(val, eVal, nenv);
            }
        }
    }

    public List<String> getSemlabels() {
        return semlabels;
    }

    public void setSemlabels(List<String> semlabels) {
        this.semlabels = semlabels;
    }

    public void setPnf(PrettyNameFactory pnf) {
        this.pnf = pnf;
    }

    public PrettyNameFactory getPnf() {
        return pnf;
    }

    /**
     * Method used to set up the variable and semantic label renaming
     * <p>
     * NB1: variables bound to constants (ie values) are not pretty renamed
     * NB2: free variables are not in the environment and thus cannot be pretty
     * renamed
     */
    public static void rename(Environment eEnv) {
        eEnv.setPnf(new PrettyNameFactory());
        // 1. We rename (with pretty names) the semantic labels
        for (int i = 0; i < eEnv.getSemlabels().size(); i++) {
            String slabel = eEnv.getSemlabels().get(i);
            if (slabel != null)
                eEnv.bind(slabel, new Value(Value.Kind.VAL,
                        "!" + eEnv.getPnf().getName(slabel)));
        }

        // 2. We rename (with pretty names) the variables that are bound to
        // variables
        List<Value> bounded = new LinkedList<Value>();
        Set<String> keys = eEnv.getTable().keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String name = it.next();
            Value v = eEnv.deref(new Value(Value.Kind.VAR, name));
            if (v.is(Value.Kind.VAR)) {
                bounded.add(v);
            }
        }
        for (Value v : bounded)
            eEnv.bind(v.getVarVal(), new Value(Value.Kind.VAR,
                    eEnv.getPnf().getName(v.getVarVal())));

    }

}
