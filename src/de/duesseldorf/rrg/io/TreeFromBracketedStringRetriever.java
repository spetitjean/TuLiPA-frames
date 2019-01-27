
package de.duesseldorf.rrg.io;

import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.RRGTree;
import de.duesseldorf.util.GornAddress;

/**
 * This class takes a tab-separated pair of lexical element and bracketed tree
 * and creates an RRGTree object out of it.
 * Example:
 * greatly (NUC_ADV (ADV <>))
 * 
 * becomes
 * 
 * eps NUC_ADV NUC_ADV (STD)
 * .0 ADV ADV (STD)
 * ..0.0 greatly greatly (LEX)
 * 
 * @author david
 *
 */
public class TreeFromBracketedStringRetriever {
    public SystemLogger log;
    private String tabSeparatedLine;
    private RRGNode lexicalElement;

    private RRGTree resultingTree;
    GornAddress currentGA;

    /**
     * in: Tab-separated line of lexical element and bracketed tree, with
     * placeholder <> for the lex. element.
     * Important: Lexical Elements can only ever have one daugherj (because of
     * the way they are appended to the tree
     * 
     * @param tabSeparatedLine
     */
    public TreeFromBracketedStringRetriever(String tabSeparatedLine) {
        this.tabSeparatedLine = tabSeparatedLine;
        this.log = new SystemLogger(System.err, true);
    }

    /**
     * 
     * Out: An RRGTree object lexicalized with the lexical element in the
     * tabseparatedline
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
            // init variables needed for the whole process
            currentGA = new GornAddress();
            this.lexicalElement = new RRGNode(RRGNodeType.LEX, split[0],
                    split[0]);
            log.info("created lexical node " + lexicalElement);

            // creat the root (needed to append to tree recursively later
            RRGNode resultingRootNode = createRootNodeFromBracketedTree(
                    split[1]);
            log.info("created root node: " + resultingRootNode);
            this.resultingTree = new RRGTree(resultingRootNode, "");
            // currentGA = currentGA.ithDaughter(0);
            createTreeFromBracketedString(
                    split[1].substring(split[1].indexOf(" ")));
            return resultingTree;
        }
        return null;
    }

    private void createTreeFromBracketedString(
            String remainingBracketedSubTree) {
        // log.info("create tree from " + remainingBracketedSubTree);
        if (remainingBracketedSubTree.startsWith("(")) {
            remainingBracketedSubTree = remainingBracketedSubTree.substring(1);
            RRGNode motherOfTheNewNode = resultingTree.findNode(currentGA);
            // check currentGA to add to the right place
            int newDaughterIndex = motherOfTheNewNode.getChildren().size();
            currentGA = currentGA.ithDaughter(newDaughterIndex);

            // create node
            String[] remainingBracketedSubTreeSplit = remainingBracketedSubTree
                    .split(" ");
            RRGNode node = createNodeFromString(
                    remainingBracketedSubTreeSplit[0]);
            // add node to the tree
            motherOfTheNewNode.addRightmostChild(node);

            // adapt the GA to change position in tree for next round
            createTreeFromBracketedString(remainingBracketedSubTree
                    .substring(remainingBracketedSubTreeSplit[0].length()));
            return;
        } else if (remainingBracketedSubTree.startsWith(" ")) {
            createTreeFromBracketedString(
                    remainingBracketedSubTree.substring(1));
            return;
        } else if (remainingBracketedSubTree.length() == 0) {
            // recursion base case: finished reading tree
            return;
        } else if (remainingBracketedSubTree.startsWith(")")) {
            currentGA = currentGA.mother();
            createTreeFromBracketedString(
                    remainingBracketedSubTree.substring(1));
            return;
        } else if (remainingBracketedSubTree.startsWith("<>")) {
            // append lexical element to the right place
            RRGNode motherOfLex = resultingTree.findNode(currentGA);
            currentGA = currentGA.ithDaughter(0);
            lexicalElement.setGornAddress(currentGA);
            motherOfLex.addRightmostChild(lexicalElement);
            createTreeFromBracketedString(
                    remainingBracketedSubTree.substring(2));
            return;
        }
        log.info("an error occured during retrieving tree " + tabSeparatedLine);
        log.info("at current status in bracketed tree parsing: '"
                + remainingBracketedSubTree + "'");
        return;
    }

    private RRGNode createRootNodeFromBracketedTree(String bracketedTree) {
        // find the root node string
        String rootNode = bracketedTree.substring(
                bracketedTree.indexOf("(") + 1, bracketedTree.indexOf(" "));
        // create tree
        return createNodeFromString(rootNode);
    }

    private RRGNode createNodeFromString(String nodeStringFromResource) {
        // note that the lexical element is not handled here

        RRGNodeType nodeType = null;
        if (nodeStringFromResource.endsWith("*")) {
            nodeType = RRGNodeType.STAR;
            nodeStringFromResource = nodeStringFromResource.substring(0,
                    nodeStringFromResource.lastIndexOf("*"));
        } else {
            nodeType = RRGNodeType.STD;
        }
        return new RRGNode(nodeType, nodeStringFromResource,
                nodeStringFromResource, currentGA);
    }
}