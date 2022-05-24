package de.duesseldorf.rrg.test;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import de.duesseldorf.frames.UnifyException;
import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.RRGParseTree;
import de.duesseldorf.rrg.RRGTree;
import de.duesseldorf.rrg.io.XMLRRGTag;
import de.duesseldorf.util.GornAddress;

/**
 * File TestTree.java
 * <p>
 * Authors:
 * David Arps <david.arps@hhu.de>
 * <p>
 * Copyright
 * David Arps, 2018
 * <p>
 * <p>
 * This file is part of the TuLiPA-frames system
 * https://github.com/spetitjean/TuLiPA-frames
 * <p>
 * <p>
 * TuLiPA is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * TuLiPA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
public class TestTree {

    public static void main(String[] args) {
        // testTreePrinting();
        // printXMLEnums();
        // testGornAddresses();
        // testTreeCloning();
        testGAcomparison();
        // testSortedMaps();
    }

    private static void testSortedMaps() {

        Map<String, String> unsortMap = new HashMap<String, String>();
        unsortMap.put("Z", "z");
        unsortMap.put("B", "b");
        unsortMap.put("A", "a");
        unsortMap.put("C", "c");
        unsortMap.put("D", "d");
        unsortMap.put("E", "e");
        unsortMap.put("Y", "y");
        unsortMap.put("N", "n");
        unsortMap.put("J", "j");
        unsortMap.put("M", "m");
        unsortMap.put("F", "f");

        System.out.println("Unsort Map......");
        printMap(unsortMap);

        System.out.println("\nSorted Map......By Key");
        Map<String, String> treeMap = new TreeMap<String, String>(unsortMap);
        printMap(treeMap);

    }

    // pretty print a map. Copied from
    // https://www.mkyong.com/java/how-to-sort-a-map-in-java/ (June 27,2018)
    private static <K, V> void printMap(Map<K, V> map) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            System.out.println(
                    "Key : " + entry.getKey() + " Value : " + entry.getValue());
        }
    }

    private static void testGAcomparison() {
        GornAddress root = new GornAddress();
        GornAddress one = root.ithDaughter(0);
        System.out.println("root: " + root + "\ndaughter: " + one);
        System.out
                .println("one > root should be > 0:  " + (one.compareTo(root)));
        GornAddress two = one.rightSister();
        System.out
                .println("one < root should be < 0:  " + (root.compareTo(one)));
        System.out.println("two < one should be < 0: " + one.compareTo(two));
        System.out.println("two > one should be > 0: " + two.compareTo(one));

        GornAddress oneone = one.ithDaughter(0);
        System.out
                .println("oneone < two should be <0: " + oneone.compareTo(two));
        System.out
                .println("oneone > two should be >0: " + two.compareTo(oneone));
    }

    private static void testTreeCloning() throws UnifyException {
        // 1. build root tree - we later substitute to the daughter of the root
        RRGNode root = new RRGNode.Builder().type(RRGNodeType.STD)
                .name("ptree1_root").cat("RP").build();
        RRGNode daughter = new RRGNode.Builder().type(RRGNodeType.SUBST)
                .name("substnode1").cat("RP").build();
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

        // 2. build substitution tree7
        RRGNode substroot = new RRGNode.Builder().type(RRGNodeType.STD)
                .name("namer").cat("RP").build();
        RRGNode child = new RRGNode.Builder().type(RRGNodeType.STD)
                .name("namec").cat("ccat").build();
        RRGNode grandchild = new RRGNode.Builder().type(RRGNodeType.SUBST)
                .name("namegc").cat("gccat").build();
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

        // 4. Sister adjunction
        System.out.println(
                "now to sisadj cases. Take this tree: \n" + substitutedTree);
        System.out.println("and this one as adjoining tree: \n" + ptree2);
        System.out.println("and sisadj at GA 1 as leftmost daughter. ");
        RRGParseTree leftsisadj = null;
        try {
            leftsisadj = substitutedTree.sisterAdjoin(ptree2,
                    new GornAddress().ithDaughter(0), 0);
        } catch (UnifyException e) {
            e.printStackTrace();
        }
        System.out.println("result: " + leftsisadj);

        System.out.println(
                "now, do the same adjunction again to see if reference stuff works properly. "
                        + "Result(1) should now have three children.");
        RRGParseTree leftsisadj2 = null;
        try {
            leftsisadj2 = leftsisadj.sisterAdjoin(ptree2,
                    new GornAddress().ithDaughter(0), 0);
        } catch (UnifyException e) {
            e.printStackTrace();
        }
        System.out.println("result: \n" + leftsisadj2);

        System.out.println(
                "now, take this tree and add the following tree as 2nd daughter of GA 1:\n"
                        + substitutedTree);

        RRGParseTree rightsisadj1 = null;
        try {
            rightsisadj1 = leftsisadj2.sisterAdjoin(substitutedTree,
                    new GornAddress().ithDaughter(0), 1);
        } catch (UnifyException e) {
            e.printStackTrace();
        }
        System.out.println("result: \n" + rightsisadj1);
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
        RRGNode root = new RRGNode.Builder().type(RRGNodeType.STD).name("name")
                .cat("rcat").build();
        RRGNode child = new RRGNode.Builder().type(RRGNodeType.STD)
                .name("namec").cat("ccat").build();
        RRGNode grandchild = new RRGNode.Builder().type(RRGNodeType.SUBST)
                .name("namegc").cat("gccat").build();
        RRGTree tree = new RRGTree(root, "great_treeeeeeee");
        System.out.println(tree.toString());

        ((RRGNode) root).addRightmostChild(child);
        System.out.println(tree.toString());

        ((RRGNode) child).addRightmostChild(grandchild);
        ((RRGNode) root).addRightmostChild(child);
        System.out.println(tree.toString());
    }

}
