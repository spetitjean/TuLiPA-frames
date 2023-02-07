
package de.duesseldorf.frames;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import de.tuebingen.tag.Environment;
/**
 *
 * @author Simon
 *
 */
public class ConstraintLiteral{

    
    public enum Kind {
        Types, Attribute_type, Path_identity;
    }

    private Kind constraint_type;
    private List<String> path;
    private List<String> type;
    private String value;
    private List<String> other_path;

     /**
     * These can be types, attribute(path)-type pairs, path identities
     *
     */

    public ConstraintLiteral(List<String> type) {
	this.constraint_type = Kind.Types;
	this.type = type;
    }

    public ConstraintLiteral(List<String> path, List<String> type, String value) {
	this.constraint_type = Kind.Attribute_type;
	this.path = path;
	this.type = type;
	this.value = value;
    }

    public ConstraintLiteral(List<String> path, List<String> other_path) {
	this.constraint_type = Kind.Path_identity;
	this.path = path;
	this.other_path = other_path;
    }

    public String toString(){
	String content = "";
	if (this.constraint_type == Kind.Types){
	    content = this.type.toString();
	}
	else if (this.constraint_type == Kind.Attribute_type){
	    content = this.path.toString() + this.type + this.value;
	}
	else if (this.constraint_type == Kind.Path_identity){
	    content = this.path.toString() + this.other_path.toString();
	}
	return "Constraint type: "+this.constraint_type+"\n"+content;
    }

    public Kind getConstraintType(){
	return this.constraint_type;
    }
    
}
