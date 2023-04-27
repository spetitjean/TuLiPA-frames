/**
 * File FSPrinter.java
 * <p>
 * Authors:
 * David Arps <david.arps@hhu.de>
 * Simon Petitjean <petitjean@phil.hhu.de>
 * <p>
 * Copyright
 * David Arps, 2017
 * Simon Petitjean, 2017
 * <p>
 * This file is part of the TuLiPA-frames system
 * https://github.com/spetitjean/TuLiPA-frames
 * <p>
 * <p>
 * TuLiPA is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * TuLiPA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.duesseldorf.frames;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.tuebingen.tag.Environment;
import de.duesseldorf.frames.Value;
import de.duesseldorf.frames.TypeHierarchy;
import de.tuebingen.anchoring.NameFactory;


/**
 *
 * @author david
 *
 */
public class FsTools {

    public static List<Fs> cleanup(List<Fs> frames) {
        List<Fs> noSimpleDoubleoccurence = new LinkedList<Fs>();

        // check for FS that occur multiple times and only keep one instance

        // keep track of corefs you have already seen in order to avoid multiple
        // addings
        Set<Value> seenCorefs = new HashSet<Value>();

        for (Fs fs : frames) {
            Value v = fs.getCoref();
            if (!seenCorefs.contains(v)) {
                noSimpleDoubleoccurence.add(fs);
                seenCorefs.add(v);
            }
        }

        // tmp:
        // return noSimpleDoubleoccurence;

        // only keep a FS if it is not a value of any other Fs
        // TODO somehow still not working completely
        List<Fs> clean = new LinkedList<Fs>();
        for (Fs fs : noSimpleDoubleoccurence) {
            Value fsv = fs.getCoref();
            // System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%\nFS: \nCoref: "
            // + fsv + "\n" + printFS(fs));
            boolean keep = true;
            for (Fs fscompare : noSimpleDoubleoccurence) {

                if (fs.getCoref() != fscompare.getCoref()) {
                    // System.out.println("%%\nFScompare: \n" +
                    // printFS(fscompare)
                    // + "\n\n\n");
                    if (included(fs, fscompare, new HashSet<Value>())) {
                        keep = false;
                        break;
                    }

                }
            }
            if (keep) {
                clean.add(fs);
            }
        }

        return clean;
    }

    public static boolean included(Fs fs1, Fs fs2, HashSet<Value> seen) {
        if (seen.contains(fs2.getCoref())) {
            // System.out.println("Included fail because of recursion");
            return false;
        } else
            seen.add(fs2.getCoref());
        for (Value v : fs2.getAVlist().values()) {
            if (v.is(Value.Kind.AVM)
                    && v.getAvmVal().getCoref().equals(fs1.getCoref())) {
                return true;
            } else {
                if (v.is(Value.Kind.AVM)) {
                    if (included(fs1, v.getAvmVal(), seen)) {
                        return true;
                    }
                }
            }
        }
        return false;
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
	Fs result = FsTools.unify(fs1, fs2, env, new HashSet<Value>());
        return result;
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
     */
    static Fs unify(Fs fs1, Fs fs2, Environment env, Set<Value> seen)
            throws UnifyException {
        Hashtable<String, Value> avm1 = fs1.getAVlist();
        Hashtable<String, Value> avm2 = fs2.getAVlist();

        if (fs1.getCoref() != null && seen.contains(fs1.getCoref())) {
            //System.out.println("Stopping unification because of recursion: " + fs1);
            return fs1;
        } else {
            seen.add(fs1.getCoref());
            // seen.add(fs2.getCoref());
        }
        // the resulting avm:
        Hashtable<String, Value> resultingAVM = new Hashtable<String, Value>();
        // a temporary avm used to store non-common features:
        Hashtable<String, Value> todo = new Hashtable<String, Value>();

        // 1. loop through avm1
        for (String k : avm1.keySet()) {
            if (avm2.containsKey(k)) { // k is a common feature, we unify its
                // values
                Value nval = null;
                try {
                    // exception caught and re-thrown to extend the error
                    // message
                    // System.out.println("Unifying "+avm1.get(k)+" and
                    // "+avm2.get(k));
                    nval = ValueTools.unify(avm1.get(k), avm2.get(k), env,
                            seen);
                } catch (UnifyException e) {
                    throw new UnifyException(
                            "feature " + k + ": " + e.getMessage());
                }
                resultingAVM.put(k, nval);
            } else { // we keep it for later
                todo.put(k, avm1.get(k));
            }
        }
        // 2. loop through avm2

        for (String k : avm2.keySet()) {

            if (!(avm1.containsKey(k))) {
                todo.put(k, avm2.get(k));
            } // no else since the common features have already been processed
        }
        // 3. loop through delayed features
        // that is, features that appear only in one avm
        for (String k : todo.keySet()) {
            Value v = todo.get(k);
            if (v.is(Value.Kind.VAR)) { // if it is a variable
                Value w = env.deref(v);
                if (w == null) { // v is not bound
                    resultingAVM.put(k, v);
                } else { // v is bound
                    resultingAVM.put(k, w);
                }
            } else { // v is not a variable
                resultingAVM.put(k, v);
            }
        }

        // 4. set the type of the resulting FS
        Type resType = unifyTypes(fs1, fs2, env);

        // System.out.println("Computed type: "+resType);
        // 5. set the coref of the resulting FS
        Value resCoref;
        if (fs1.isTyped()) {
            if (fs2.isTyped()) {
                // System.out.println("Unifying coreferences: "+fs1.getCoref()+"
                // and "+fs2.getCoref());
                resCoref = ValueTools.unify(fs1.getCoref(), fs2.getCoref(), env,
                        seen);
                // System.out.println("Done unify");
            } else {
                resCoref = fs1.getCoref();
                fs2.setCoref(fs1.getCoref());
            }
        } else {
            resCoref = fs2.getCoref();
        }

        // finally, all the features have been processed, we return the avm res:
        return (new Fs(resultingAVM, resType, resCoref));
    }

    /**
     * @param fs1
     * @param fs2
     * @param env
     * @return
     * @throws UnifyException
     */
    private static Type unifyTypes(Fs fs1, Fs fs2, Environment env)
            throws UnifyException {
        Type resType = null;
        TypeHierarchy tyHi = Situation.getTypeHierarchy();
        if (tyHi != null) {
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
                resType = fs1.getType().union(fs2.getType(), env);
            } else if (fs1.isTyped()) {
                resType = fs1.getType();
            } else if (fs2.isTyped()) {
                resType = fs2.getType();
            }
        }
        return resType;
    }

    public static List<Fs> checkTypeConstraints(List<Fs> frames, Environment env, NameFactory nf)
	throws UnifyException{
	List<Fs> output_frames = new LinkedList<Fs>();
	if(Situation.getTypeHierarchy() != null){
	    HierarchyConstraints constraints = Situation.getTypeHierarchy().getHierarchyConstraints();
	    for (Fs frame : frames){
		Fs checkedFs = checkTypeConstraints(frame, constraints, env, nf);
		output_frames.add(checkedFs);
	    }
	}
	else{
	    output_frames = frames;
	}
	return output_frames;
    }

    public static Fs checkTypeConstraints(Fs frame, HierarchyConstraints constraints, Environment env, NameFactory nf)
	throws UnifyException {
	System.out.println("Checking constraints on "+frame.toString());
	// attr : type -> attr : type constraints
	for(HierarchyConstraint constraint : constraints.getConstraints()){
	    // we support only attr : type -> attr : type
	    if(constraint.getLeft().size() > 1 || constraint.getRight().size() > 1){
		System.out.println("Only constraints literal of length one are supported");
		continue;
	    }
	    if(constraint.getLeft().get(0).getConstraintType() == ConstraintLiteral.Kind.Attribute_type
	       && constraint.getRight().get(0).getConstraintType() == ConstraintLiteral.Kind.Attribute_type){
		LinkedList<String> path = new LinkedList(constraint.getLeft().get(0).getPath());
		List<String> type = constraint.getLeft().get(0).getType();
		if (checkAttrConstraint(frame, path, type, env, nf)){
		    
		    LinkedList<String> rightPath = new LinkedList(constraint.getRight().get(0).getPath());
		    List<String> rightType = constraint.getRight().get(0).getType();
		    applyAttrConstraint(frame, rightPath, rightType, env, nf);
		}
	    }
	}
	for (Value child : frame.getAVlist().values()){
	    if (child.is(Value.Kind.AVM)){
		checkTypeConstraints(child.getAvmVal(), constraints, env, nf);
	    }
	}
	// ToDo getAttrToPathConstraints
	return frame;
    }

    static boolean checkAttrConstraint(Fs frame, LinkedList<String> path, List<String> type, Environment env, NameFactory nf)
	throws UnifyException {

	Hashtable<String, Value> features = frame.getAVlist();
	if(path.size() == 1){
	    // check if the attribute is present
	    // if yes, check if it has the right type
	    if(features.get(path.get(0)) != null){
		if(features.get(path.get(0)).getType() != null && features.get(path.get(0)).is(Value.Kind.AVM)){
		    if (features.get(path.get(0)).getAvmVal().getType().getElementaryTypes().contains(type.get(0))){
			System.out.println("Found "+path.get(0).toString());
			System.out.println("Type: "+features.get(path.get(0)).getAvmVal().getType().toString());
			System.out.println("contains: "+type.get(0));
			return true;
		    }
		    else{
			// this should be done in a way cleaner way
			// this is when the type is a variable (typically, the type true)
			if (type.get(0).charAt(0) == '@'){
			    System.out.println("Found "+path.get(0).toString());
			    System.out.println("Type: "+features.get(path.get(0)).getAvmVal().getType().toString());
			    System.out.println("Unifiable with variable "+type.get(0));
			    return true;
			}
			else{
			    System.out.println("Found "+path.get(0).toString());
			    System.out.println("Type: "+features.get(path.get(0)).getAvmVal().getType().toString());
			    System.out.println("Doesn't contain: "+type.get(0));
			}
		    }
		}
	    }
	    return false;
	}
	else{
	    // check if the path exists, so if path.get(0) is an attribute of frame
	    if (features.get(path.get(0)) != null && features.get(path.get(0)).is(Value.Kind.AVM)){
		String attr = path.removeFirst();
		return checkAttrConstraint(features.get(attr).getAvmVal(), path, type, env, nf);
	    }
	    
	    return false;
	}
    }

    // ToDo: chech(Path/Type)Constraint (Actually, Type should be taken care of by the original hierarchy)

    static void applyAttrConstraint(Fs frame, LinkedList<String> path, List<String> type, Environment env, NameFactory nf)
	throws UnifyException {
	System.out.println("Applying constraint");
	System.out.println("path: " + path.toString());
	System.out.println("type: " + type.toString());
	
	Hashtable<String, Value> features = frame.getAVlist();
	if(path.size() == 1){
	    // check if the attribute is present
	    // if yes, check if it has the right type
	    if(features.get(path.get(0)) != null){
		// is the getType() useful here?
		if (features.get(path.get(0)).getType() != null && features.get(path.get(0)).is(Value.Kind.AVM) ){
		    System.out.println("Found "+path.get(0).toString());
		    System.out.println("Type: "+features.get(path.get(0)).getAvmVal().getType().toString());
		    System.out.println("contains: "+type.get(0));
		    // Here you want to unify the types (so that if fails when not compatible)
		    // this should be done in a way cleaner way
		    // this is when the type is a variable (typically, the type true)
		    Value typeValue;
		    if (type.get(0).charAt(0) == '@'){
			// nothing to do
		    }
		    else{
			features.get(path.get(0)).setAvmVal(unify(features.get(path.get(0)).getAvmVal(),new Fs(new Type(type), new Value(Value.Kind.VAR,nf.getUniqueName())), env));
		    }
		}
		else{
		    if (features.get(path.get(0)).is(Value.Kind.AVM)){
			System.out.println(features.get(path.get(0)).getAvmVal().getType().getElementaryTypes());
		    }
		    else {
			if (features.get(path.get(0)).is(Value.Kind.VAR)){
			    //System.out.println("Variable: "+features.get(path.get(0)));
			    features.replace(path.get(0),ValueTools.unify(features.get(path.get(0)), new Value(new Fs(new Type(type), new Value(Value.Kind.VAR,nf.getUniqueName()))), env));
			}
			else{
			    System.out.println("feature exists but is not an AVM nor a variable!");
			}
		    }
		    
		}
		
	    }
	    else{
		System.out.println("feature doesn't exist, need to create it!");
		// this should be done in a way cleaner way
		// this is when the type is a variable (typically, the type true)
		if (type.get(0).charAt(0) == '@'){
		    // nothing to do
		    features.put(path.get(0), new Value (Value.Kind.VAR, nf.getUniqueName()));
		}
		else{
		    features.put(path.get(0), new Value (new Fs (new Type (type), new Value (Value.Kind.VAR, nf.getUniqueName()))));
		}
	    }
	}
	else{
	    // check if the path exists, so if path.get(0) is an attribute of frame
	    if (features.get(path.get(0)) != null && features.get(path.get(0)).is(Value.Kind.AVM)){
		String attr = path.removeFirst();
		//return applyAttrConstraint(features.get(attr).getAvmVal(), path, type, env);
		applyAttrConstraint(features.get(attr).getAvmVal(), path, type, env, nf);
	    }
	    else{
		System.out.println("path doesn't exist, need to create it!");			
		features.put(path.get(0), new Value (new Fs (new Type (new LinkedList()), new Value (Value.Kind.VAR, nf.getUniqueName()))));
		String attr = path.removeFirst();
		applyAttrConstraint(features.get(attr).getAvmVal(), path, type, env, nf);
	    }
	}	
    }

    // ToDo: apply(Path/Type)Constraint


    
    /**
     * similar to the old updateFs method. changes the variables in the frameSem
     *
     * @param frameSem
     * @param env
     * @param finalUpdate
     * @return
     * @throws UnifyException
     */
    public static Frame updateFrameSem(Frame frameSem, Environment env,
                                       boolean finalUpdate) throws UnifyException {

        List<Fs> newFs = new LinkedList<Fs>();
        if (frameSem != null && frameSem.getFeatureStructures() != null)
            for (Fs fs : frameSem.getFeatureStructures()) {
                if (fs != null)
                    newFs.add(Fs.updateFS(fs, env, finalUpdate));
            }

        Set<Relation> newRelations = new HashSet<Relation>();
        if (frameSem != null && frameSem.getRelations() != null)
            for (Relation oldRel : frameSem.getRelations()) {
                List<Value> newArgs = new LinkedList<Value>();
                for (Value oldVal : oldRel.getArguments()) {
                    Value oldCopy = new Value(oldVal);
                    oldCopy.update(env, finalUpdate);
                    // Value newVal = env.deref(oldVal);
                    newArgs.add(oldCopy);
                }
                newRelations.add(new Relation(oldRel.getName(), newArgs));
            }

        return new Frame(newFs, newRelations);
    }


    /**
     * Like updateFrameSem, but merging the frames in the end
     *
     * @param frameSem
     * @param env
     * @param finalUpdate
     * @return
     * @throws UnifyException
     */
    public static Frame updateFrameSemWithMerge(Frame frameSem, Environment env,
                                                boolean finalUpdate) throws UnifyException {
        NameFactory nf = new NameFactory();
        List<Fs> newFs = new LinkedList<Fs>();
        // System.out.println("Environment before update: "+env);

	for (Fs fs : frameSem.getFeatureStructures()) {
            if (fs != null){
                newFs.add(Fs.updateFS(fs, env, finalUpdate));
	    }
        }
	
        // do not know why 2 merges are now necessary...
        List<Fs> mergedFrames = Fs.mergeFS(newFs, env, nf);
        if (mergedFrames != null)
            mergedFrames = Fs.mergeFS(newFs, env, nf);

	mergedFrames = FsTools.checkTypeConstraints(mergedFrames, env, nf);

	
        List<Fs> cleanedFrames = new LinkedList<Fs>();
        if (mergedFrames == null) {
            System.err.println("Frame unification failed, tree discarded!\n");
            return null;
        } else {
            cleanedFrames = FsTools.cleanup(mergedFrames);
        }

        Set<Relation> newRelations = new HashSet<Relation>();
        // System.out.println("Old relations: "+frameSem.getRelations());

        for (Relation oldRel : frameSem.getRelations()) {
            List<Value> newArgs = new LinkedList<Value>();
            for (Value oldVal : oldRel.getArguments()) {
                Value oldCopy = new Value(oldVal);
                oldCopy.update(env, finalUpdate);
                // Value newVal = env.deref(oldVal);
                newArgs.add(oldCopy);
            }
            newRelations.add(new Relation(oldRel.getName(), newArgs));
        }

	// apparently a second round is needed
	cleanedFrames = FsTools.checkTypeConstraints(cleanedFrames, env, nf);
	// and another round...
	// this is for type constraints which target variables refering to FS, which then need propagation through the environment
	cleanedFrames = Fs.mergeFS(cleanedFrames, env, nf);
	cleanedFrames = FsTools.cleanup(cleanedFrames);
        // System.out.println("Environment: "+env);
        // System.out.println("New relations: "+newRelations);
        return new Frame(cleanedFrames, newRelations);
    }

    
}
