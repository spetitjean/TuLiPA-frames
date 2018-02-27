package de.duesseldorf.rrg.test;

import de.duesseldorf.io.XMLRRGTag;
import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.RRGTree;
import de.duesseldorf.util.GornAddress;

public class TestTree {

    public static void main(String[] args) {
        // testTreePrinting();
        // printXMLEnums();
        testGornAddresses();

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
        RRGTree tree = new RRGTree(root);
        System.out.println(tree.toString());

        ((RRGNode) root).addRightmostChild(child);
        System.out.println(tree.toString());

        ((RRGNode) child).addRightmostChild(grandchild);
        ((RRGNode) root).addRightmostChild(child);
        System.out.println(tree.toString());
    }

}
