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
import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGTree;

public class BracketedRRGReader {

    File grammar = null;
    private SystemLogger log;
    private int treeIdCount;
    private boolean removeDoubleTrees = true;

    public BracketedRRGReader(File grammar) {
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
                nextLine = tsvFileReader.readLine();
            } catch (Exception e) {
                log.info("exception while retrieving grammar entry: "
                        + nextLine);
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
            resultingTrees = removeDoubleTrees(resultingTrees);
        }
        return new RRG(resultingTrees);
    }

    private Set<RRGTree> removeDoubleTrees(Set<RRGTree> trees) {
        Set<RRGTree> result = new HashSet<RRGTree>();
        for (RRGTree treeFromResource : trees) {
            if (result.isEmpty()) {
                result.add(treeFromResource);
                continue;
            }
            boolean treeIsAlreadyIn = false;
            for (RRGTree treeFromResult : result) {
                if (((RRGNode) treeFromResult.getRoot())
                        .weakEquals((RRGNode) treeFromResource.getRoot())) {
                    treeIsAlreadyIn = true;
                    // log.info("found double tree: " + treeFromResource);
                    break;
                }
            }
            if (!treeIsAlreadyIn) {
                result.add(treeFromResource);
            }
        }
        log.info("number of equal trees that were filtered out: "
                + (trees.size() - result.size()));
        return result;
    }
}
