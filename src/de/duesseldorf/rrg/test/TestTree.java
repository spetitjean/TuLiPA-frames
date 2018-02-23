package de.duesseldorf.rrg.test;

import de.duesseldorf.io.XMLRRGTag;
import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.RRGTree;

public class TestTree {

    public static void main(String[] args) {
        // testTreePrinting();
        // printXMLEnums();

    }

    private static void printXMLEnums() {
        for (XMLRRGTag e : XMLRRGTag.values()) {
            System.out.println(e + "\t" + e.StringVal());
        }
    }

    public static void testTreePrinting() {
        RRGNode root = new RRGNode(RRGNodeType.STD);
        RRGNode child = new RRGNode(RRGNodeType.STD);
        RRGNode grandchild = new RRGNode(RRGNodeType.SUBST);
        RRGTree tree = new RRGTree(root);
        System.out.println(tree.toString());

        ((RRGNode) root).addRightmostChild(child);
        System.out.println(tree.toString());

        ((RRGNode) child).addRightmostChild(grandchild);
        ((RRGNode) root).addRightmostChild(child);
        System.out.println(tree.toString());
    }

}
