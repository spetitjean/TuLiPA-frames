package de.duesseldorf.frames;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.tuebingen.tag.Value;

public class TypeConstraint {

    private Set<String> attributes;
    private Type type;
    private Value val;

    public TypeConstraint(Collection<String> attributes, Type type, Value val) {
        this.attributes = new HashSet<String>(attributes);
        this.type = type;
        this.val = val;
    }

    public Set<String> getAttributes() {
        return attributes;
    }

    public Type getType() {
        return type;
    }

    public Value getVal() {
        return val;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Attributes: " + attributes);
        sb.append("\nType: " + type.toString());
        sb.append("\nValue: " + val.toString());

        return sb.toString();
    }
}
