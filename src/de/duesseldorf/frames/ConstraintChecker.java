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
    private boolean debugMode;

    public ConstraintChecker(Frame frame, Environment env, boolean debugMode) {
        this.frame = frame;
        this.env = env;
        this.debugMode = debugMode;
    }

    public Frame checkConstraints() {
        System.out.println("frame: " + frame);
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
        if (debugMode) {
            System.err.println("constraint checked: " + constraint);
            System.err.println("on feature structure: " + fs);
            System.err.println("resulting in a change to the frame semantics: "
                    + testUnify);
        }
        return testUnify;
    }
}
