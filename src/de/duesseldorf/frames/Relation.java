package de.duesseldorf.frames;

import java.util.List;

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

    public Relation(String name, List<Value> arguments) {
        this.name = name;
        this.arguments = arguments;
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
