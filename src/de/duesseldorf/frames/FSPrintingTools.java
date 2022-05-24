package de.duesseldorf.frames;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class FSPrintingTools {

    /**
     * returns a naive string representation of a (probably recursive) typed or
     * untyped FS
     *
     * @param fs
     * @return
     */
    public static String printFS(Fs fs, boolean printTypeConstraints) {
        String res = "<p>";
        res += FSPrintingTools.printFS(fs, 0, new HashSet<Value>(),
                printTypeConstraints);
        res += "</p>";
        return res;
    }

    private static String printFS(Fs fs, int recursiondepth,
                                  HashSet<Value> seen, boolean printTypeConstraints) {
        StringBuffer sb = new StringBuffer();
        recursiondepth++;
        if (fs.isTyped()) {
            sb.append(fs.getCoref() + ":");
            sb.append("<br>");
            sb.append(FSPrintingTools.nonBreakingSpace(recursiondepth));
            sb.append("type: ");
            sb.append(fs.getType().toStringWithoutVariable());
            if (printTypeConstraints) {
                sb.append("<br>");
                sb.append(FSPrintingTools.nonBreakingSpace(recursiondepth));
                sb.append("type constraints: "
                        + fs.getType().getTypeConstraints());
                sb.append("</br>");
            }

        }

        if (seen.contains(fs.getCoref())) {
            // System.out.println("Printing stopped because of recursion");
            return sb.toString();
        } else
            seen.add(fs.getCoref());

        Set<String> keys = fs.getAVlist().keySet();
        Iterator<String> i = keys.iterator();
        while (i.hasNext()) {
            String k = i.next();
            sb.append("<br>");
            sb.append(FSPrintingTools.nonBreakingSpace(recursiondepth));
            sb.append(k);
            String sep = " = ";
            sb.append(sep);

            Value v = fs.getAVlist().get(k);

            if (v.is(Value.Kind.AVM)) {
                sb.append(printFS(v.getAvmVal(), recursiondepth + k.length(),
                        seen, printTypeConstraints));
            } else if (v.is(Value.Kind.VAL)) {
                sb.append(v.getSVal());
                sb.append("</br>");
            } else if (v.is(Value.Kind.VAR)) {
                sb.append(v.getVarVal());
            } else {
                ;
                // sb.append("minor FSPrinter fuckup: " + v.toString());
            }
        }
        return sb.toString();
    }

    /**
     * @param spaces amount of nbsp's
     * @return
     */
    private static String nonBreakingSpace(int spaces) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < spaces; i++) {
            sb.append("&nbsp;&nbsp;&nbsp;");
        }
        return sb.toString();
    }

    public static String printFrame(Frame frameSem,
                                    boolean printTypeConstraints) {
        StringBuffer sb = new StringBuffer();
        for (Fs fs : frameSem.getFeatureStructures()) {
            sb.append(printFS(fs, printTypeConstraints));
        }
        for (Relation rel : frameSem.getRelations()) {
            sb.append(FSPrintingTools.printRelation(rel));
        }
        return sb.toString();
    }

    private static String printRelation(Relation relation) {
        StringBuffer sb = new StringBuffer("<p>");
        sb.append(relation.toString());
        sb.append("</p>");
        return sb.toString();
    }

}
