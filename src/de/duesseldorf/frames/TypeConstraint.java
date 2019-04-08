package de.duesseldorf.frames;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import de.tuebingen.anchoring.NameFactory;

public class TypeConstraint {

    private LinkedList<String> attributes;
    private Type type;
    private Value val;

    public TypeConstraint(Collection<String> attributes, Type type, Value val) {
        this.attributes = new LinkedList<String>(attributes);
        this.type = type;
        this.val = val;
    }

    public LinkedList<String> getAttributes() {
        return attributes;
    }

    public Type getType() {
        return type;
    }

    public Value getVal() {
        return val;
    }

    public Fs asFs() {
        Fs result = new Fs(0);
        result.setType(getType());

        LinkedList<String> newAttributes = new LinkedList<String>(
                getAttributes());
        return asFsRec(result, newAttributes);
    }

    public Fs asFsRec(Fs result, LinkedList<String> attributes) {
        String first = attributes.pop();
        if (attributes.size() == 0) {
            result.setFeatWithoutReplace(first, getVal());
        }
        // attributes should be a path of attributes
        // the value should be given to att1 -> att2 ... -> attn
        else {
            // No NameFactory is available here, but I guess this is
            // safe enough (we only need unique names)
            NameFactory nf = new NameFactory();
            String newVar = nf.getUniqueName();
            Fs in = new Fs(0, new Type(new HashSet<String>()),
                    new Value(Value.Kind.VAR, newVar));
            Fs inresult = asFsRec(in, attributes);
            Value valResult = new Value(inresult);
            result.setFeatWithoutReplace(first, valResult);
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
