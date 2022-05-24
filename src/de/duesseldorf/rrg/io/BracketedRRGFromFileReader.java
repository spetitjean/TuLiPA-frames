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
import de.duesseldorf.rrg.RRGTools;
import de.duesseldorf.rrg.RRGTree;

public class BracketedRRGFromFileReader {

    File grammar = null;
    private SystemLogger log;
    private int treeIdCount;
    private boolean removeDoubleTrees = true;

    public BracketedRRGFromFileReader(File grammar) {
        this.grammar = grammar;
        this.log = new SystemLogger(System.err, true);
        this.treeIdCount = 0;
    }

    /**
     * create a Reader for the trees file and go through it line by line,
     * creating an RRG object consisting of lexicalised trees.
     *
     * @return
     */
    public RRG parseRRG() {
        Set<RRGTree> resultingTrees = new HashSet<RRGTree>();
        // TODO all references to the treestrings can be removed when proper
        // trees are created
        List<String> treeStrings = new LinkedList<String>();
        BufferedReader tsvFileReader;
        try {
            tsvFileReader = new BufferedReader(new FileReader(grammar));
        } catch (FileNotFoundException e) {
            log.info("could not read grammar file " + grammar);
            return null;
        }
        String nextLine = "";
        try {
            nextLine = tsvFileReader.readLine();
        } catch (Exception e) {
            log.info("could not read first line of grammar" + grammar);
            return null;
        }
        while (nextLine != null) {
            try {
                // the most innovative condition to filter out lines without
                // trees
                if (nextLine.contains("(")) {
                    treeStrings.add(nextLine);
                    RRGTree treeFromCurrentLine = new TreeFromBracketedStringRetriever(
                            nextLine).createTree();
                    // log.info("created tree: " + treeFromCurrentLine);
                    String lexLabel = new LinkedList<String>(
                            treeFromCurrentLine.getLexNodes().keySet()).get(0)
                            .toString();
                    treeFromCurrentLine.setId(lexLabel + "_" + treeIdCount);
                    treeIdCount++;
                    resultingTrees.add(treeFromCurrentLine);
                }
            } catch (Exception e) {
                log.info("exception while retrieving grammar entry: "
                        + nextLine);
                e.printStackTrace();
                System.exit(1);
            }
            try {
                nextLine = tsvFileReader.readLine();
            } catch (IOException e) {
                System.err.println(
                        "error while reading line at or after: " + nextLine);
            }
        }
        try {
            tsvFileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // for (String treeString : treeStrings) {
        // System.out.println(treeString);
        // }
        if (removeDoubleTrees) {
            resultingTrees = RRGTools
                    .removeDoubleTreesByWeakEquals(resultingTrees);
        }
        return new RRG(resultingTrees);
    }
}
