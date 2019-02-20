
package de.duesseldorf.rrg.io;

import java.util.Arrays;
import java.util.List;

import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.RRGTree;
import de.duesseldorf.util.GornAddress;
import de.tuebingen.tag.Fs;
import de.tuebingen.tag.Value;

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
        // log.info("create tree from Br String called with "
        // + remainingBracketedSubTree + " current GA: " + currentGA);
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
        Fs nodeFs = new Fs(1);
        // note that the lexical element is not handled here
        List<String> substNodeLabels = Arrays.asList("NP", "NUC_ADV", "NPIP",
                "QP", "V", "P", "CD", "POS", "N", "RP", "PP", "CLAUSE",
                "CLAUSE-PERI", "CORE-PERI", "NP-PERI", "CORE");
        RRGNodeType nodeType = null;
        // find feature structures
        if (nodeStringFromResource.contains("[")) {
            int fsStartingPoint = nodeStringFromResource.indexOf("[");
            String fsString = nodeStringFromResource.substring(fsStartingPoint);
            nodeFs = createFsFromString(fsString);
            nodeStringFromResource = nodeStringFromResource.substring(0,
                    fsStartingPoint);
        }
        if (nodeStringFromResource.endsWith("*")) {
            nodeType = RRGNodeType.STAR;
            nodeStringFromResource = nodeStringFromResource.substring(0,
                    nodeStringFromResource.lastIndexOf("*"));
        } else if (couldBeASubstNode
                && substNodeLabels.contains(nodeStringFromResource)) {
            nodeType = RRGNodeType.SUBST;
        } else if (couldBeASubstNode) {
            System.out.println(
                    "unknown subst node label: " + nodeStringFromResource);
        } else {
            nodeType = RRGNodeType.STD;
        }
        return new RRGNode.Builder().type(nodeType).cat(nodeStringFromResource)
                .name(nodeStringFromResource).gornaddress(currentGA).fs(nodeFs)
                .build();
    }

    /**
     * format: Core*[OP=CLAUSE,OTHER=[SOMEATTR=SOMEVAL]]
     * This doess not capture fs of a "depth" > 1 at the momemnt
     * possibly going recursive, though in practice the fs will be rather small
     * 
     * @param fsString
     * @return
     */
    private Fs createFsFromString(String fsString) {
        int startindex = fsString.startsWith("[") ? 1 : 0;
        int endindex = fsString.endsWith("[") ? fsString.length() - 1
                : fsString.length() - 1;
        fsString = fsString.substring(startindex, endindex);
        Fs result = new Fs(1);

        String[] fsStringSplit = fsString.split(",");
        for (String avPair : fsStringSplit) {
            String[] avPairSplit = avPair.split("=");
            Value val = new Value(Value.VAL, avPairSplit[1]);
            result.setFeat(avPairSplit[0], val);
        }
        // find out the string for the first val, may be an fs or a simple value
        // if (fsStringSplit[1].startsWith("[")) {
        // find the string for the fs, retrieve recursively and setFeat
        // }

        // go recursive until the whole fs is done
        System.out.println("new Fs: " + result);
        return result;
    }
}