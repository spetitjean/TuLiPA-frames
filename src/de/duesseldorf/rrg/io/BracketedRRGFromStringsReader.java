package de.duesseldorf.rrg.io;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.duesseldorf.rrg.RRG;
import de.duesseldorf.rrg.RRGTree;

public class BracketedRRGFromStringsReader {

    /**
     * 
     * @param grammar
     *            a List<String> where the string has the format
     *            lexEntry\t(tree with <> for lex entry)
     * @return
     */
    public static RRG createRRGFromListOfBracketedStrings(
            List<String> grammar) {
        Set<RRGTree> resultingTrees = new HashSet<RRGTree>();
        for (String treeStr : grammar) {
            RRGTree tree = new TreeFromBracketedStringRetriever(treeStr)
                    .createTree();
            resultingTrees.add(tree);
        }
        return new RRG(resultingTrees);
    }
}
