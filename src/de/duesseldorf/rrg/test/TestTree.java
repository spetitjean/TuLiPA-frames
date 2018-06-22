package de.duesseldorf.rrg.test;

import de.duesseldorf.io.XMLRRGTag;
import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.RRGParseTree;
import de.duesseldorf.rrg.RRGTree;
import de.duesseldorf.util.GornAddress;

public class TestTree {

    public static void main(String[] args) {
        // testTreePrinting();
        // printXMLEnums();
        // testGornAddresses();
        testTreeCloning();
    }

    private static void testTreeCloning() {
        // 1. build root tree - we later substitute to the daughter of the root
        RRGNode root = new RRGNode(RRGNodeType.STD, "ptree1_root", "RP");
        RRGNode daughter = new RRGNode(RRGNodeType.SUBST, "substnode1", "RP");
        // System.out.println(root);
        // System.out.println(daughter);
        root.addRightmostChild(daughter);
        RRGParseTree ptree1 = new RRGParseTree(root, "ptree1_id");
        System.out.println(ptree1);
        // a copy of ptree1 that should not undergo actions that ptree1
        // undergoes
        RRGParseTree ptree2 = new RRGParseTree(ptree1);
        ptree2.setId("shouldntbe ptree1_id");
        // System.out.println(ptree2);

        // 2. build substitution tree
        RRGNode substroot = new RRGNode(RRGNodeType.STD, "namer", "RP");
        RRGNode child = new RRGNode(RRGNodeType.STD, "namec", "ccat");
        RRGNode grandchild = new RRGNode(RRGNodeType.SUBST, "namegc", "gccat");
        ((RRGNode) child).addRightmostChild(grandchild);
        ((RRGNode) substroot).addRightmostChild(child);
        RRGTree substtree = new RRGTree(substroot, "great_treeeeeeee");
        System.out.println(
                "Substitute:\n" + substtree + "into that tree\n" + ptree1);
        RRGParseTree substitutedTree = ptree1.substitute(substtree,
                new GornAddress().ithDaughter(0));
        System.out.println("result: " + substitutedTree);
        System.out.println("ptree1 should not contain substtree:\n" + ptree1);
        System.out.println("ptree2 should not contain substtree:\n" + ptree2);

        // 3. Substitute a tree into itself?
        RRGTree quiteLikeptree1 = new RRGTree(root, "ptree1_id");
        System.out.println("Substitute this tree");
        System.out.println(quiteLikeptree1);
        System.out.println("into that tree:\n" + ptree1);
        RRGParseTree selfSubstResult = ptree1.substitute(quiteLikeptree1,
                new GornAddress().ithDaughter(0));
        System.out.println("result: " + selfSubstResult);
        System.out.println("original target tree: \n" + ptree1);
    }

    private static void testGornAddresses() {
        GornAddress root = new GornAddress();
        System.out.println("root: " + root.toString());
        GornAddress firstDaughter = root.ithDaughter(1);
        System.out.println("firstD: " + firstDaughter.toString());
        GornAddress scndGrandChild = firstDaughter.ithDaughter(2);
        System.out.println("second grandchild: " + scndGrandChild.toString());
        System.out.println("firstD: " + firstDaughter.toString());

    }

    private static void printXMLEnums() {
        for (XMLRRGTag e : XMLRRGTag.values()) {
            System.out.println(e + "\t" + e.StringVal());
        }
    }

    public static void testTreePrinting() {
        RRGNode root = new RRGNode(RRGNodeType.STD, "namer", "rcat");
        RRGNode child = new RRGNode(RRGNodeType.STD, "namec", "ccat");
        RRGNode grandchild = new RRGNode(RRGNodeType.SUBST, "namegc", "gccat");
        RRGTree tree = new RRGTree(root, "great_treeeeeeee");
        System.out.println(tree.toString());

        ((RRGNode) root).addRightmostChild(child);
        System.out.println(tree.toString());

        ((RRGNode) child).addRightmostChild(grandchild);
        ((RRGNode) root).addRightmostChild(child);
        System.out.println(tree.toString());
    }

}
