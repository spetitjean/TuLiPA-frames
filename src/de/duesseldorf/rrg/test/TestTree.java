package de.duesseldorf.rrg.test;

import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.RRGTree;
import de.tuebingen.tree.Node;

public class TestTree {

    public static void main(String[] args) {
        testTreePrinting();
    }

    public static void testTreePrinting() {
        Node root = new RRGNode(RRGNodeType.STD);
        Node child = new RRGNode(RRGNodeType.STD);
        Node grandchild = new RRGNode(RRGNodeType.SUBST);
        RRGTree tree = new RRGTree(root);
        System.out.println(tree.toString());

        ((RRGNode) root).addRightmostChild(child);
        System.out.println(tree.toString());

        ((RRGNode) child).addRightmostChild(grandchild);
        ((RRGNode) root).addRightmostChild(child);
        System.out.println(tree.toString());
    }

}
