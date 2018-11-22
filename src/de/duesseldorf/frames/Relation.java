package de.duesseldorf.frames;

import java.util.LinkedList;
import java.util.List;

import de.tuebingen.anchoring.NameFactory;
import de.tuebingen.tag.Value;

public class Relation {

    private String name;
    private List<Value> arguments;

    public String getName() {
        return name;
    }

    public List<Value> getArguments() {
        return arguments;
    }

    public Relation(Relation rel, NameFactory nf) {
        this.name = rel.getName();
        this.arguments = new LinkedList<Value>();
        for (Value v : rel.getArguments()) {
            arguments.add(new Value(v, nf));
        }
    }

    public Relation(String name, List<Value> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public Relation rename(NameFactory nf) {
        List<Value> newArgs = new LinkedList<Value>();
        for (Value oldNamedVal : this.arguments) {
            String newVarName = nf.getName(oldNamedVal.getVarVal());
            Value newVal = new Value(Value.VAR, newVarName);
            newArgs.add(newVal);
            // System.out.println("renamed " + oldNamedVal.getVarVal() + " to "
            // + newVal.getVarVal());
        }
        return new Relation(this.name, newArgs);
    }

    /**
     * Returns a String of the relation of the form
     * name(argument1, argument2, )
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(name);
        sb.append("(");
        // walk through all the arguments
        int i = 0;
        for (Value value : arguments) {
            i++;
            sb.append(value.toString());
            sb.append(", ");
        }
        // remove the last ", "
        if (i > 0) {
            sb.replace(sb.length() - 2, sb.length(), "");
        }
        sb.append(")");
        return sb.toString();
    }
}
