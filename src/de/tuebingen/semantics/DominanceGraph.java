package de.tuebingen.semantics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import de.duesseldorf.frames.Value;
import de.tuebingen.tag.SemDom;
import de.tuebingen.tag.SemLit;
import de.tuebingen.tag.SemPred;

public class DominanceGraph {
    public static boolean verbose = false;

    ArrayList<String> clauses;
    HashMap<String, Integer> nonPropositionalVariables;
    HashSet<String> propositionalVariables;

    int varNumber;
    HashMap<String, String> simplerVariableNames;

    public DominanceGraph(List<SemLit> holeSemantics) {
        varNumber = 0;
        simplerVariableNames = new HashMap<String, String>();
        clauses = new ArrayList<String>();
        nonPropositionalVariables = new HashMap<String, Integer>();
        propositionalVariables = new HashSet<String>();
        ConnectednessGraph cGraph = new ConnectednessGraph();
        // first go: examine the variables
        for (SemLit sl : holeSemantics) {
            if (sl instanceof SemDom) {
                SemDom sd = (SemDom) sl;
                String arg1 = makeSimple(sd.getArg1().toString());
                String arg2 = makeSimple(sd.getArg2().toString());
                propositionalVariables.add(arg1);
                propositionalVariables.add(arg2);
                nonPropositionalVariables.remove(arg1);
                nonPropositionalVariables.remove(arg2);
            } else if (sl instanceof SemPred) {
                SemPred sp = (SemPred) sl;
                String label = makeSimple(sp.getLabel().toString());
                for (Value v : sp.getArgs()) {
                    String arg = makeSimple(v.toString());
                    if (!propositionalVariables.contains(arg)) {
                        int number = 0;
                        if (nonPropositionalVariables.get(arg) != null) {
                            number = nonPropositionalVariables.get(arg);
                        }
                        number++;
                        nonPropositionalVariables.put(arg, number);
                    }
                }
                propositionalVariables.add(label);
                nonPropositionalVariables.remove(label);
            }
        }
        for (String var : nonPropositionalVariables.keySet()) {
            for (int i = 1; i <= nonPropositionalVariables.get(var); i++) {
                clauses.add("label(" + var + i + " " + var + ")");
            }
        }
        // second go: build the clauses
        for (SemLit sl : holeSemantics) {
            if (sl instanceof SemDom) {
                SemDom sd = (SemDom) sl;
                String arg1 = makeSimple(sd.getArg1().toString());
                String arg2 = makeSimple(sd.getArg2().toString());
                clauses.add("dom(" + arg2 + " " + arg1 + ")");
                cGraph.addEdge(arg2, arg1);
            } else if (sl instanceof SemPred) {
                SemPred sp = (SemPred) sl;
                String label = makeSimple(sp.getLabel().toString());
                String pred = makeNoDiacritics(sp.getPred().toString());
                ArrayList<String> args = new ArrayList<String>();
                for (Value v : sp.getArgs()) {
                    String arg = makeSimple(v.toString());
                    if (propositionalVariables.contains(arg)) {
                        args.add(arg);
                        cGraph.addEdge(label, arg);
                    } else if (nonPropositionalVariables.get(arg) > 0) {
                        int number = nonPropositionalVariables.get(arg);
                        args.add(arg + number);
                        cGraph.addEdge(label, arg + number);
                        number--;
                        nonPropositionalVariables.put(arg, number);
                    }
                }
                String clauseString = "label(" + label + " " + pred + "(";
                for (String arg : args) {
                    clauseString += arg + " ";
                }
                clauseString = clauseString.substring(0,
                        clauseString.length() - 1);
                clauseString += "))";
                clauses.add(clauseString);
            }
        }
        ArrayList<HashSet<String>> parts = cGraph.graphPartitions();
        ArrayList<String> partHoles = new ArrayList<String>();
        if (verbose)
            System.err.println("Parts:");
        for (HashSet<String> part : parts) {
            ArrayList<String> roots = cGraph.getRoots(part);
            if (verbose)
                System.err.println(part + " --> " + roots);
            String holeID = getNextSimpleName();
            partHoles.add(holeID);
            if (parts.size() > 1) {
                for (String root : roots) {
                    clauses.add("dom(" + holeID + " " + root + ")");
                }
            }
        }
        // no need for top-level conjunction if everything was connected before
        if (partHoles.size() > 1) {
            String topClauseString = "label(top and(";
            for (String arg : partHoles) {
                topClauseString += arg + " ";
            }
            topClauseString = topClauseString.substring(0,
                    topClauseString.length() - 1);
            topClauseString += "))";
            clauses.add(topClauseString);
        }
    }

    public String makeSimple(String variableName) {
        variableName = variableName.replaceAll("\\?", "");
        variableName = variableName.replaceAll("!", "");
        variableName = variableName.toLowerCase();
        if (variableName.indexOf("_") != -1) {
            String simplerName = simplerVariableNames.get(variableName);
            if (simplerName == null) {
                simplerName = getNextSimpleName();
                simplerVariableNames.put(variableName, simplerName);
            }
            variableName = simplerName;
        }
        return variableName;
    }

    /**
     * returns a String that is a version of pred without fnacy letters
     *
     * @param pred
     * @return
     */
    public String makeNoDiacritics(String pred) {
        pred = pred.replaceAll("\u00fc", "u"); // ü
        pred = pred.replaceAll("\u00f6", "o"); // ö
        pred = pred.replaceAll("\u00e4", "a"); // ä
        pred = pred.replaceAll("\u00e9", "e"); // é
        pred = pred.replaceAll("\u00e8", "e"); // è
        pred = pred.replaceAll("\u00ea", "e"); // ê
        pred = pred.replaceAll("\u00f9", "u"); // ù
        pred = pred.replaceAll("\u00fb", "u"); // û
        pred = pred.replaceAll("\u00f4", "o"); // ô
        pred = pred.replaceAll("\u00ee", "i"); // î
        pred = pred.replaceAll("\u00ef", "i"); // i trema
        pred = pred.replaceAll("\u00e2", "a"); // â
        pred = pred.replaceAll("\u00c0", "a"); // à
        return pred;
    }

    public String getNextSimpleName() {
        return "x_" + varNumber++;
    }

    public String toUtoolOzFormat() {
        String ozString = "[";
        for (String clause : clauses) {
            ozString += clause + " ";
        }
        ozString = ozString.substring(0, ozString.length() - 1);
        ozString += "]";
        return ozString;
    }
}
