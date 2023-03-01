
package de.duesseldorf.frames;

import java.util.List;

import de.tuebingen.tag.Environment;
import de.duesseldorf.frames.ConstraintLiteral;
/**
 *
 * @author Simon
 *
 */
public class HierarchyConstraint{

    private List<ConstraintLiteral> left;
    private List<ConstraintLiteral> right;

     /**
     * for type constraints (of the form "path : type -> path = path" for instance)
     *
     * @param left
     * @param right
     */
    public HierarchyConstraint(List<ConstraintLiteral> left, List<ConstraintLiteral> right) {
        this.left = left;
	this.right = right;
    }

    public String toString(){
	String result = "Type constraint\n Left: "+this.left+"\nRight: "+this.right;
	return result;
    }

    public List<ConstraintLiteral> getLeft(){
	return this.left;
    }

    public List<ConstraintLiteral> getRight(){
	return this.right;
    }

}
