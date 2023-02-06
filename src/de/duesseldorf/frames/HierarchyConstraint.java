
package de.duesseldorf.frames;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import de.tuebingen.tag.Environment;
import de.duesseldorf.frames.ConstraintLiteral;
/**
 *
 * @author Simon
 *
 */
public class HierarchyConstraint{

    private ConstraintLiteral left;
    private ConstraintLiteral right;

     /**
     * for type constraints (of the form "path : type -> path = path" for instance)
     *
     * @param left
     * @param right
     */
    public HierarchyConstraint(ConstraintLiteral left, ConstraintLiteral right) {
        this.left = left;
	this.right = right;
    }

}
