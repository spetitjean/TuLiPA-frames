package de.duesseldorf.rrg.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.duesseldorf.rrg.RRG;
import de.duesseldorf.rrg.RRGTree;

public class BracketedRRGReader {

    File grammar = null;

    public BracketedRRGReader(File grammar) {
        this.grammar = grammar;
    }

    public RRG parseRRG() {
        Set<RRGTree> resultingTrees = new HashSet<RRGTree>();
        // TODO all references to the treestrings can be removed when proper
        // trees are created
        List<String> treeStrings = new LinkedList<String>();
        try {
            BufferedReader tsvFileReader = new BufferedReader(
                    new FileReader(grammar));
            String nextLine = tsvFileReader.readLine();
            while (nextLine != null) {
                // the most innovative condition to filter out trees
                if (nextLine.contains("(")) {
                    treeStrings.add(nextLine);
                    RRGTree treeFromCurrentLine = createTreeFromTabSeparatedLine(
                            nextLine);
                }
                nextLine = tsvFileReader.readLine();
            }
            tsvFileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // for (String treeString : treeStrings) {
        // System.out.println(treeString);
        // }
        System.exit(1);
        return new RRG(resultingTrees);
    }

    private RRGTree createTreeFromTabSeparatedLine(String nextLine) {
        String[] split = nextLine.split("\t");
        for (String s : split) {
            System.out.println(s);
        }
        if (split.length != 2) {
            System.out.println("could not properly split the line \"" + nextLine
                    + "\" because it contains more than one tab");
        }
        return null;
    }

}
