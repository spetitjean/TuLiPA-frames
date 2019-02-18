package de.duesseldorf.frames;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import de.tuebingen.tag.Environment;

public class ValueTools {

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
                res = unifyVALandVAL(a, b, res);
                break;
            case ADISJ:
                res = unify(b, a, env, tyHi, seen);
                break;
            case VAR: // b is a variable, we dereference it
                res = unifyVALandVAR(a, b, env, tyHi, seen);
                break;
            default:
                throw new UnifyException(a.toString(), b.toString());
            }
            break;
        case INT: // a is an integer
            switch (b.getType()) {
            case INT: // b is an integer
                res = unifyINTandINT(a, b, res);
                break;
            case VAR: // b is a variable
                res = unifyINTandVAR(a, b, env, res);
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
                break;
            case VAR: // b is a variable
                res = unifyAVMandVAR(a, b, env, tyHi, seen, res);
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
                    && a.getAdisj().getFirst().is(Value.Kind.VAR))
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
                res = extractADISJandVAL(a, b, env, res, aMaybeVar);
                break;
            case ADISJ: // b is an atomic disjunction
                res = unifyADISJandADISJ(a, b, env, tyHi, seen, res, aMaybeVar);
                break;
            case VAR: // b is a variable
                res = unifyADISJandVAR(a, b, env, tyHi, seen, aMaybeVar);
                break;
            default:
                throw new UnifyException("Unification failure (value) between "
                        + a.toString() + " and " + b.toString());
            }
            break;
        case VAR: // a is a variable
            switch (b.getType()) {
            case VAR: // if a and b are both variables
                res = unifyVARandVAR(a, b, env, tyHi, seen);
                break;
            default: // the case has been defined above
                Value aa = env.deref(a);
                res = ValueTools.unify(b, aa, env, tyHi, seen);
            }
            break;
        }
        // if you do something here, it may have side effects. For example,
        // unifyADISJandADISJ has a return statement after which you might not
        // want to change the result.
        return res;
    }

    /**
     * @param a
     * @param b
     * @param res
     * @return
     * @throws UnifyException
     */
    private static Value unifyVALandVAL(Value a, Value b, Value res)
            throws UnifyException {
        if (!(a.getSVal().equals(b.getSVal()))) {
            throw new UnifyException(a.toString(), b.toString());
        } else {
            res = new Value(Value.Kind.VAL, a.getSVal());
        }
        return res;
    }

    /**
     * @param a
     * @param b
     * @param env
     * @param tyHi
     * @param seen
     * @return
     * @throws UnifyException
     */
    private static Value unifyVALandVAR(Value a, Value b, Environment env,
            TypeHierarchy tyHi, Set<Value> seen) throws UnifyException {
        Value res;
        Value bb = env.deref(b);
        // if b is unbound, we bind it to a
        if (bb.equals(b)) {
            env.bind(b.getVarVal(), a);
            res = a;
        } else { // b is already bound, the values must unify !
            res = ValueTools.unify(a, bb, env, tyHi, seen);
        }
        return res;
    }

    /**
     * @param a
     * @param b
     * @param env
     * @param res
     * @return
     * @throws UnifyException
     */
    private static Value unifyINTandVAR(Value a, Value b, Environment env,
            Value res) throws UnifyException {
        Value bb = env.deref(b);
        // if b is unbound, we bind it to a
        if (bb.equals(b)) {
            env.bind(b.getVarVal(), a);
            res = a;
        } else { // b is already bound, the values must match !
            if (bb.is(Value.Kind.INT) && (a.getIVal() == bb.getIVal())) {
                // they do match:
                res = new Value(a.getIVal());
            } else {
                // they do not:
                throw new UnifyException(a.toString(), b.toString());
            }
        }
        return res;
    }

    /**
     * @param a
     * @param b
     * @param res
     * @return
     * @throws UnifyException
     */
    private static Value unifyINTandINT(Value a, Value b, Value res)
            throws UnifyException {
        if (!(a.getIVal() == b.getIVal())) {
            throw new UnifyException(a.toString(), b.toString());
        } else {
            res = new Value(a.getIVal());
        }
        return res;
    }

    /**
     * @param a
     * @param b
     * @param env
     * @param tyHi
     * @param seen
     * @param res
     * @return
     * @throws UnifyException
     */
    private static Value unifyAVMandVAR(Value a, Value b, Environment env,
            TypeHierarchy tyHi, Set<Value> seen, Value res)
            throws UnifyException {
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
                if (bb.getVarVal() != env.deref(a.getAvmVal().getCoref())
                        .getVarVal()) {
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
                            new Value(Value.Kind.VAR, "@" + bb.getVarVal()))
                            .getType() == Value.Kind.AVM) {
                        // System.out.println("Unifying AVM with bound
                        // AVM");
                        env.bind("$" + bb.getVarVal(), new Value(Fs.unify(
                                a.getAvmVal(),
                                env.deref(new Value(Value.Kind.VAR,
                                        "$" + bb.getVarVal())).getAvmVal(),
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
            if (bb.is(Value.Kind.AVM)) { // let us see if they do:
                res = new Value(Fs.unify(a.getAvmVal(), bb.getAvmVal(), env,
                        tyHi, seen));
                /*
                 * // Uncaught exception (caught in Fs' unify method)
                 * try { res = new Value(Fs.unify(a.getAvmVal(),
                 * x.getAvmVal(), env)); } catch (UnifyException e){
                 * System.out.println(e.getMessage()); // this error is
                 * caught here for now // but it will later be given to
                 * the calling method }
                 */
            } else {
                if (bb.is(Value.Kind.VAR)) {
                    res = new Value(unify(a, bb, env, tyHi, seen));
                } else {// they do not:
                    throw new UnifyException(a.toString(), b.toString());
                }
            }
        }
        return res;
    }

    /**
     * @param a
     * @param b
     * @param env
     * @param res
     * @param aMaybeVar
     * @return
     * @throws UnifyException
     */
    private static Value extractADISJandVAL(Value a, Value b, Environment env,
            Value res, Value aMaybeVar) throws UnifyException {
        if (a.getAdisj().contains(b)) {
            res = b;
            if (aMaybeVar != null) {
                env.bind(aMaybeVar.getVarVal(), res);
            }
        } else {
            throw new UnifyException(a.toString(), b.toString());
        }
        return res;
    }

    /**
     * @param a
     * @param b
     * @param env
     * @param tyHi
     * @param seen
     * @param res
     * @param aMaybeVar
     * @return
     * @throws UnifyException
     */
    private static Value unifyADISJandADISJ(Value a, Value b, Environment env,
            TypeHierarchy tyHi, Set<Value> seen, Value res, Value aMaybeVar)
            throws UnifyException {
        // before looking for the intersection,
        // we check whether the atomic disjunction is bound to a
        // variable
        // if this is the case, the first element of the adisj is the
        // variable
        LinkedList<Value> bdisj = b.getAdisj();
        Value bMaybeVar = (b.getAdisj().size() > 0
                && b.getAdisj().getFirst().is(Value.Kind.VAR))
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
            if (!(z.is(Value.Kind.VAR)) && a.getAdisj().contains(z)) {
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
                    if (bMaybeVar != null && !(bMaybeVar.equals(aMaybeVar)))
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
        return res;
    }

    /**
     * @param a
     * @param b
     * @param env
     * @param tyHi
     * @param seen
     * @param aMaybeVar
     * @return
     * @throws UnifyException
     */
    private static Value unifyADISJandVAR(Value a, Value b, Environment env,
            TypeHierarchy tyHi, Set<Value> seen, Value aMaybeVar)
            throws UnifyException {
        Value res;
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
        return res;
    }

    /**
     * @param a
     * @param b
     * @param env
     * @param tyHi
     * @param seen
     * @param aa
     * @return
     * @throws UnifyException
     */
    private static Value unifyVARandVAR(Value a, Value b, Environment env,
            TypeHierarchy tyHi, Set<Value> seen) throws UnifyException {
        Value res;
        Value aa = env.deref(a);
        // System.out.println("Deref "+b);
        Value bb = env.deref(b);
        // System.out.println("is "+bb);
        if (aa.is(Value.Kind.VAR) && bb.is(Value.Kind.VAR)) {
            if (aa.equals(a) && bb.equals(b)) {
                // if both variables are unbound
                env.bind(a.getVarVal(), b);
                res = a;
            } else {
                if (!(bb.hasCycle(aa.getVarVal(), env))) {
                    res = ValueTools.unify(aa, bb, env, tyHi, seen);
                } else {
                    res = aa;
                }
            }
        } else {
            res = ValueTools.unify(aa, bb, env, tyHi, seen);
        }
        return res;
    }

    public static Value unify(Value a, Value b, Environment env,
            TypeHierarchy tyHi) throws UnifyException {
        return unify(a, b, env, tyHi, new HashSet<Value>());
    }

    public static Value unify(Value a, Value b, Environment env)
            throws UnifyException {
        return unify(a, b, env, null, new HashSet<Value>());
    }

}
