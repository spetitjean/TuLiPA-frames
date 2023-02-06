
package de.duesseldorf.frames;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedList;

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

    private Kind type;

     /**
     * These can be types, attribute(path)-type pairs, path identities
     *
     */
    public ConstraintLiteral(LinkedList<String> path, Type type, Value value) {
	this.type = Kind.Attribute_type;
    }

}
