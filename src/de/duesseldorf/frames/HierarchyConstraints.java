
package de.duesseldorf.frames;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;

import de.tuebingen.tag.Environment;
import de.duesseldorf.frames.HierarchyConstraint;
import de.duesseldorf.frames.ConstraintLiteral.Kind;
/**
 *
 * @author Simon
 *
 */
public class HierarchyConstraints{

    // private List<HierarchyConstraint> typeConstraints;
    // private List<HierarchyConstraint> typeToPathConstraints;
    // private List<HierarchyConstraint> typeToAttrConstraints;
    // private List<HierarchyConstraint> attrToAttrConstraints;
    // private List<HierarchyConstraint> attrToPathConstraints;
    private List<HierarchyConstraint> constraints;
    
    public HierarchyConstraints(List<HierarchyConstraint> hierarchyConstraints){
	// this.typeConstraints = new LinkedList<HierarchyConstraint>();
	// this.typeToPathConstraints = new LinkedList<HierarchyConstraint>();
	// this.typeToAttrConstraints = new LinkedList<HierarchyConstraint>();
	// this.attrToAttrConstraints = new LinkedList<HierarchyConstraint>();
	// this.attrToPathConstraints = new LinkedList<HierarchyConstraint>();

	// for (HierarchyConstraint constraint : hierarchyConstraints){
	//     if (constraint.getLeft().getConstraintType() == Kind.Types && constraint.getRight().getConstraintType() == Kind.Types){
	// 	typeConstraints.add(constraint);
	//     }
	//     else if (constraint.getLeft().getConstraintType() == Kind.Types && constraint.getRight().getConstraintType() == Kind.Path_identity){
	// 	typeToPathConstraints.add(constraint);
	//     }
	//     else if (constraint.getLeft().getConstraintType() == Kind.Types && constraint.getRight().getConstraintType() == Kind.Attribute_type){
	// 	typeToAttrConstraints.add(constraint);
	//     }
	//     else if (constraint.getLeft().getConstraintType() == Kind.Attribute_type && constraint.getRight().getConstraintType() == Kind.Attribute_type){
	//         attrToAttrConstraints.add(constraint);	
	//     }
	//     else if (constraint.getLeft().getConstraintType() == Kind.Attribute_type && constraint.getRight().getConstraintType() == Kind.Path_identity){
	//         attrToPathConstraints.add(constraint);	
	//     }
	//     else {
	// 	System.out.println("Unexpected type of constraint: ");
	// 	System.out.println(constraint);
	//     }
	// }

	// let us not split this anymore
	this.constraints = hierarchyConstraints;
	
    }

    public String toString(){
	return
	    "#####################\n" +
	    "HierarchyConstraints:\n" +
	    this.getConstraints().toString();
	    // "TypeConstraints:\n"+this.typeConstraints.toString() +
	    // "\n#####################\n" +
	    // "\nTypeToPathConstraints:\n"+this.typeToPathConstraints.toString() +
	    // "\n#####################\n" +
	    // "\nTypeToAttrConstraints:\n"+this.typeToAttrConstraints +
	    // "\n#####################\n" +
	    // "\nAttrToAttrConstraints:\n"+this.attrToAttrConstraints.toString() +
	    // "\n#####################\n" +
	    // "\nAttrToPathConstraints:\n"+this.attrToPathConstraints.toString() +
	    // "\n#####################\n" ;

	    }

    // public List<HierarchyConstraint> getAttrToAttrConstraints(){
    // 	return this.attrToAttrConstraints;
    // }

    // public List<HierarchyConstraint> getAttrToPathConstraints(){
    // 	return this.attrToPathConstraints;
    // }

    // public List<HierarchyConstraint> getPathToTypeConstraints(){
    // 	return null;
    // 	//return this.PathToTypeConstraints;
    // }

    // public List<HierarchyConstraint> getPathToAttrConstraints(){
    // 	return null;
    // 	//return this.pathToAttrConstraints;
    // }

    public List<HierarchyConstraint> getConstraints(){
	return this.constraints;
    }
}
