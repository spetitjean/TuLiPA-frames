
package de.duesseldorf.rrg.io;

import java.util.Arrays;
import java.util.List;

import de.duesseldorf.frames.Fs;
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

    private SystemLogger log;
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
        this.log = new SystemLogger(System.err, false);
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
            this.lexicalElement = new RRGNode.Builder().type(RRGNodeType.LEX)
                    .cat(split[0]).name(split[0]).build();
            log.info("created lexical node " + lexicalElement);

            // creat the root (needed to append to tree recursively later
            RRGNode resultingRootNode = createRootNodeFromBracketedTree(
                    split[1]);
            log.info("created root node: " + resultingRootNode);
            this.resultingTree = new RRGTree(resultingRootNode, "");
            // currentGA = currentGA.ithDaughter(0);
            createTreeFromBracketedString(
                    split[1].substring(split[1].indexOf(" ")));

            // here, it's necessary to create a copy to retrieve the special
            // nodes
            resultingTree = new RRGTree(resultingTree);
            return resultingTree;
        }
        return null;
    }

    private void createTreeFromBracketedString(
            String remainingBracketedSubTree) {
        log.info("create tree from Br String called with "
                + remainingBracketedSubTree + " current GA: " + currentGA);
        if (remainingBracketedSubTree.startsWith("(")) {
            remainingBracketedSubTree = remainingBracketedSubTree.substring(1);
            RRGNode motherOfTheNewNode = resultingTree.findNode(currentGA);
            // check currentGA to add to the right place
            int newDaughterIndex = motherOfTheNewNode.getChildren().size();
            // adapt the GA to change position in tree for next round
            currentGA = currentGA.ithDaughter(newDaughterIndex);

            // create node
            String[] remainingBracketedSubTreeSplit = remainingBracketedSubTree
                    .split(" ");
            boolean isASubstNode = !(remainingBracketedSubTreeSplit[1]
                    .startsWith("(")
                    || remainingBracketedSubTreeSplit[1].startsWith("<"));
            RRGNode node = createNodeFromString(
                    remainingBracketedSubTreeSplit[0], isASubstNode);
            // add node to the tree
            motherOfTheNewNode.addRightmostChild(node);

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
            // currentGA = currentGA.ithDaughter(0);
            lexicalElement.setGornAddress(currentGA.ithDaughter(0));
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
        return createNodeFromString(rootNode, false);
    }

    /**
     * 
     * @param nodeStringFromResource
     *            the node label to create a node from
     * @param nodeIsRoot
     *            if the node is a root, NP and N nodes are STD and not SUBST
     *            nodes
     * @return
     */
    private RRGNode createNodeFromString(String nodeStringFromResource,
            boolean couldBeASubstNode) {
        Fs nodeFs = new Fs();
        // note that the lexical element is not handled here
        List<String> substNodeLabels = Arrays.asList("CL", "NP", "NUC_ADV",
                "NPIP", "QP", "V", "P", "CD", "POS", "N", "RP", "PP", "CLAUSE",
                "CLAUSE-PERI", "CORE-PERI", "NP-PERI", "CORE");
        RRGNodeType nodeType = null;
        // find feature structures
        if (nodeStringFromResource.contains("[")) {
            int fsStartingPoint = nodeStringFromResource.indexOf("[");
            String fsString = nodeStringFromResource.substring(fsStartingPoint);
            nodeStringFromResource = nodeStringFromResource.substring(0,
                    fsStartingPoint);
            if (fsString.endsWith("]*")) {
                fsString = fsString.substring(0, fsString.length() - 1);
                nodeStringFromResource += "*";
            }
            nodeFs = new FsFromBracketedStringRetriever(fsString)
                    .createFsFromString();

        }
        if (nodeStringFromResource.endsWith("*")) {
            nodeType = RRGNodeType.STAR;
            nodeStringFromResource = nodeStringFromResource.substring(0,
                    nodeStringFromResource.lastIndexOf("*"));
        } else if (nodeStringFromResource.endsWith("_d")) {
            nodeType = RRGNodeType.DDAUGHTER;
            nodeStringFromResource = nodeStringFromResource.substring(0,
                    nodeStringFromResource.lastIndexOf("_d"));
        } else if (couldBeASubstNode
                && substNodeLabels.contains(nodeStringFromResource)) {
            nodeType = RRGNodeType.SUBST;
        } else if (couldBeASubstNode) {
            // System.out.println(
            // "unknown subst node label: " + nodeStringFromResource);
            nodeType = RRGNodeType.SUBST;
        } else {
            nodeType = RRGNodeType.STD;
        }
        return new RRGNode.Builder().type(nodeType).cat(nodeStringFromResource)
                .name(nodeStringFromResource).gornaddress(currentGA).fs(nodeFs)
                .build();
    }
}