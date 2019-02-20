package de.duesseldorf.frames;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.tuebingen.tag.Fs;
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

    public Fs asFs() {
        Fs result = new Fs(1);
        result.setType(getType());
        for (String attr : attributes) {
            result.setFeat(attr, getVal());
        }
        return result;
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
