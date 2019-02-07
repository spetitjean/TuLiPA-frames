package de.duesseldorf.frames;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.tuebingen.tag.Environment;
import de.tuebingen.tag.Fs;
import de.tuebingen.tag.UnifyException;
import de.tuebingen.tag.Value;

public class ConstraintChecker {

    private Frame frame;
    private Environment env;

    public ConstraintChecker(Frame frame, Environment env) {
        this.frame = frame;
        this.env = env;
    }

    public Frame checkConstraints() {
        for (int i = 0; i < frame.getFeatureStructures().size(); i++) {
            // check the fs itself
            Fs ithFSFromFrameWithConstraints = frame.getFeatureStructures()
                    .get(i);
            Set<TypeConstraint> constraintsToCheck = ithFSFromFrameWithConstraints
                    .getType().getTypeConstraints();

            ithFSFromFrameWithConstraints = checkConstraintsAgainstOneFS(
                    constraintsToCheck, ithFSFromFrameWithConstraints);

        }
        return frame;
    }

    private Fs checkConstraintsAgainstOneFS(
            Set<TypeConstraint> constraintsToCheck, Fs fsToCheck) {
        Fs fsToCheckWithConstraints = fsToCheck;
        for (TypeConstraint constr : constraintsToCheck) {
            fsToCheckWithConstraints = checkConstraint(constr,
                    fsToCheckWithConstraints);
            if (fsToCheckWithConstraints == null) {
                return null;
            }
        }
        // check all attributes
        Map<String, Fs> avPairsToAdd = new HashMap<String, Fs>();
        for (Entry<String, Value> fsAVPair : fsToCheckWithConstraints
                .getAVlist().entrySet()) {
            if (fsAVPair.getValue().is(Value.AVM)) {
                Fs valFromAVPair = fsAVPair.getValue().getAvmVal();
                Set<TypeConstraint> constraintsForNewVal = valFromAVPair
                        .getType().getTypeConstraints();
                Fs newValFromAVPair = checkConstraintsAgainstOneFS(
                        constraintsForNewVal, valFromAVPair);
                if (newValFromAVPair == null) {
                    return null;
                }
                avPairsToAdd.put(fsAVPair.getKey(), newValFromAVPair);
            }
        }
        for (Entry<String, Fs> pairToAdd : avPairsToAdd.entrySet()) {
            Value newValFromAVPairasValue = new Value(pairToAdd.getValue());
            fsToCheckWithConstraints.replaceFeat(pairToAdd.getKey(),
                    newValFromAVPairasValue);
        }
        return fsToCheckWithConstraints;

    }

    /**
     * iff the constraint is fulfilled, this method returns the Fs that is the
     * changed Fs given as parameter
     * 
     * Iff the constraint is not fulfilled (unification does not succeed), the
     * method returns null
     * 
     * @param constraint
     * @param fs
     * @return
     */
    private Fs checkConstraint(TypeConstraint constraint, Fs fs) {
        // System.out.println(
        // "check constraint: " + constraint + " against fs " + fs);
        // first check the particular constraint, then go recursive on the
        // AVList.
        Fs constraintAsFs = constraint.asFs();
        Fs testUnify = null;
        try {
            testUnify = Fs.unify(constraintAsFs, fs, env,
                    Situation.getTypeHierarchy());
        } catch (UnifyException e) {
            return null;
        }
        if (testUnify == null) {
            return null;
        }
        return testUnify;
        // System.out.println(testUnify.getSize());
        //
        // for (Entry<String, Value> s : testUnify.getAVlist().entrySet()) {
        // System.out.println("key: " + s.getKey());
        // System.out.println("val: " + s.getValue());
        // }
        // // System.out
        // // .println("at this point, set testUnify to the right attribute");
        // // System.out.println("constraint met.");
        // // System.out.println("constraint:\n" + constraint);
        // System.out.println("fs:\n" + fs
        // + "\n++++++++++++++++++++++++++++++++++++++++++++++\n\n");
        // System.out.println("testUnify: " + testUnify + "\n\n");
        // System.out.println(
        // "problem: the constraint is met, but the amazement feature is"
        // + " not put in the printed fs and it is not clear what the values
        // mean.");
        // System.out.println("the value is not even in the environment:");
        // System.out.println(env.get(constraint.getVal().getVarVal()));
    }
}
