package de.duesseldorf.frames;

import java.util.HashSet;
import java.util.Set;

import de.tuebingen.tag.Environment;
import de.tuebingen.tag.Fs;
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
            System.out.println("constraintsToCheck: " + constraintsToCheck);
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
        return true;
    }

    private boolean checkConstraint(TypeConstraint constraint, Fs fs) {
        // first check the particular constraint, then go recursive on the
        // AVList.
        boolean fsHasCorrectType = checkForType(constraint.getType(),
                fs.getType());
        System.out.println("constraint:\n" + constraint);
        System.out.println("fs:\n" + fs);
        for (String attrFromConstraint : constraint.getAttributes()) {
            boolean fsHasFeat = fs.hasFeat(attrFromConstraint);
            // TODO get elemtypes from type Variable
            // boolean fsHasType = fs.getType().getElementaryTypes().
            if (fsHasFeat) {
                Value valFromFS = fs.getFeat(attrFromConstraint);
                Value valFromEnv = env.get(attrFromConstraint);
            } else {
                // attribute not there, constraint not met
                return false;
            }
        }

        // go recursive here? Is a constraint on a frame valid for fs within
        // that frame?

        return false;
    }

    private boolean checkForType(Type fromConstraint, Type fromFS) {
        if (fromConstraint.getElementaryTypes().size() == 0
                || fromFS.getElementaryTypes().size() == 0) {
            System.out.println(
                    "types only with variables and not with elementary types. Check ConstraintChecker:\n"
                            + fromConstraint + "\n" + fromFS);
        }
        if (fromConstraint.subsumes(fromFS))
            return true;
        return false;
    }

}
