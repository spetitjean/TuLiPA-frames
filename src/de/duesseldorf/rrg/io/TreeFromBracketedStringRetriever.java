
package de.duesseldorf.rrg.io;

import java.util.LinkedList;
import java.util.List;

import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.RRGTree;

public class TreeFromBracketedStringRetriever {
    public SystemLogger log;
    private String tabSeparatedLine;
    private RRGNode lexicalElement;

    public TreeFromBracketedStringRetriever(String tabSeparatedLine) {
        this.tabSeparatedLine = tabSeparatedLine;
        this.log = new SystemLogger(System.err, true);
    }

    /**
     * in: Tab-separated line of lexical element and bracketed tree, with
     * placeholder <> for the lex. element.
     * Out: An RRGTree object lexicalized with that lexical element
     * 
     * @param nextLine
     * @return
     */
    RRGTree createTree() {
        log.info("start parsing line: " + tabSeparatedLine);
        String[] split = tabSeparatedLine.split("\t");
        if (split.length != 2) {
            log.info("could not properly split the line \"" + tabSeparatedLine
                    + "\" because it contains more or less than one tab");
        } else {
            this.lexicalElement = new RRGNode(RRGNodeType.LEX, split[0],
                    split[0]);
            log.info("created lexical node " + lexicalElement);
            // log.info("bracketed subtrees: " + splitToSubtrees(split[1]));
            RRGTree resultingTree = new RRGTree(
                    createTreeFromBracketedString(split[1]), "fakeID");
            return resultingTree;
        }
        return null;
    }

    private RRGNode createTreeFromBracketedString(
            String remainingBracketedSubTree) {
        // log.info("create Tree from " + remainingBracketedSubTree);
        if (remainingBracketedSubTree.startsWith("(")) {
            remainingBracketedSubTree = remainingBracketedSubTree.substring(1);
            String[] remainingBracketedSubTreeSplit = remainingBracketedSubTree
                    .split(" ");
            RRGNode root = createNodeFromString(
                    remainingBracketedSubTreeSplit[0]);

            // hier mit einer externne Methode die gut geklammerten Subtrees
            // finden und dann die einzelnen Töchter hinzufügen
            // List<String> goodBracketedSubtrees = splitToSubtrees(
            // remainingBracketedSubTree);
            RRGNode subTreeAppendedToRoot = createTreeFromBracketedString(
                    remainingBracketedSubTree.substring(
                            remainingBracketedSubTreeSplit[0].length()));
            root.addRightmostChild(subTreeAppendedToRoot);
            return root;

        } else if (remainingBracketedSubTree.startsWith(" ")) {
            return createTreeFromBracketedString(
                    remainingBracketedSubTree.substring(1));
        } else if (remainingBracketedSubTree.length() == 0) {
            return null;
        } else if (remainingBracketedSubTree.startsWith(")")) {
            return createTreeFromBracketedString(
                    remainingBracketedSubTree.substring(1));
        } else if (remainingBracketedSubTree.startsWith("<>")) {
            return lexicalElement;
        }
        log.info("an error occured during retrieving tree " + tabSeparatedLine);
        return null;
    }

    private RRGNode createNodeFromString(String nodeStringFromResource) {
        if (nodeStringFromResource.equals("<>")) {
            return lexicalElement;
        }
        RRGNodeType nodeType = null;
        if (nodeStringFromResource.endsWith("*")) {
            nodeType = RRGNodeType.STAR;
            nodeStringFromResource = nodeStringFromResource.substring(0,
                    nodeStringFromResource.lastIndexOf("*"));
        } else {
            nodeType = RRGNodeType.STD;
        }
        return new RRGNode(nodeType, nodeStringFromResource,
                nodeStringFromResource);
    }

    List<String> splitToSubtrees(String remainingBracketedSubTree) {
        List<String> result = new LinkedList<String>();
        int openBrackets = 0;
        if (remainingBracketedSubTree.startsWith("(")) {
            openBrackets = openBrackets + 1;
            remainingBracketedSubTree = remainingBracketedSubTree.substring(1);
        } else {
            return splitToSubtrees(remainingBracketedSubTree.substring(1));
        }
        while (remainingBracketedSubTree.length() > 0) {
            String oneSubTree = "";
            log.info("open Brackets: " + openBrackets + ", oneSubTree"
                    + oneSubTree);
            while (openBrackets > 0) {
                if (remainingBracketedSubTree.startsWith("(")) {
                    openBrackets++;
                } else if (remainingBracketedSubTree.startsWith(")")) {
                    openBrackets--;
                }
                oneSubTree += remainingBracketedSubTree.charAt(0);
                remainingBracketedSubTree = remainingBracketedSubTree
                        .substring(1);
            }
            result.add(oneSubTree);
        }
        return result;
    }

    public static void main(String[] args) {
        String testBracketedTree1 = "(NP (CORE_N (NUC_N (N ) (N <>))))";
        String testBracketedTree2 = "(CORE_N (NUC_N (N ) (N <>)))";
        String testBracketedTree3 = "(CORE_N (NUC_N (N ) (N <>)))";

        TreeFromBracketedStringRetriever testObj = new TreeFromBracketedStringRetriever(
                testBracketedTree2);

        System.out.println(testObj.splitToSubtrees(testBracketedTree2));
    }
}