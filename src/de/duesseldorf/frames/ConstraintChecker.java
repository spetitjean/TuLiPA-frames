package de.duesseldorf.frames;

import java.util.HashSet;
import java.util.Set;

import de.tuebingen.tag.Environment;
import de.tuebingen.tag.Fs;
import de.tuebingen.tag.UnifyException;
import de.tuebingen.tag.Value;

public class ConstraintChecker {

    private Frame frame;
    private Set<Integer> alreadyChecked;
    private Environment env;

    public ConstraintChecker(Frame frame, Environment env) {
        this.frame = frame;
        this.env = env;
        alreadyChecked = new HashSet<Integer>();
    }

    public boolean checkConstraints() {
        for (Fs fsToCheck : frame.getFeatureStructures()) {
            Set<TypeConstraint> constraintsToCheck = fsToCheck.getType()
                    .getTypeConstraints();
            if (!checkConstraintsAgainstOneFS(constraintsToCheck, fsToCheck)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkConstraintsAgainstOneFS(
            Set<TypeConstraint> constraintsToCheck, Fs fsToCheck) {
        for (TypeConstraint constraintToCheck : constraintsToCheck) {
            if (!checkConstraint(constraintToCheck, fsToCheck)) {
                return false;
            }
        }
        for (String fsAttr : fsToCheck.getKeys()) {
            Value fsValForfsAttr = fsToCheck.getFeat(fsAttr);
            if (fsValForfsAttr.is(Value.AVM)) {
                Set<TypeConstraint> constraintsForVal = fsValForfsAttr
                        .getAvmVal().getType().getTypeConstraints();
                if (!checkConstraintsAgainstOneFS(constraintsForVal,
                        fsValForfsAttr.getAvmVal())) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean checkConstraint(TypeConstraint constraint, Fs fs) {
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
            return false;
        }
        if (testUnify == null) {
            return false;
        }

        System.out.println("constraint met.");
        System.out.println("constraint:\n" + constraint);
        System.out.println("fs:\n" + fs
                + "\n++++++++++++++++++++++++++++++++++++++++++++++\n\n");
        System.out.println("testUnify: " + testUnify + "\n\n");
        System.out.println(
                "problem: the constraint is met, but the amazement feature is"
                        + " not put in the printed fs and it is not clear what the values mean.");
        System.out.println("the value is not even in the environment:");
        System.out.println(env.get(constraint.getVal().getVarVal()));
        return true;
    }
}
