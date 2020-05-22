package test;

import de.duesseldorf.rrg.RRGNode;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.rrg.RRGParseTree;
import de.duesseldorf.rrg.RRGTree;
import de.duesseldorf.util.GornAddress;
import org.junit.Assert;
import org.junit.Test;


/**
 * File GornAddressTest.java
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
public class GornAddressTest {

    public static void main(String[] args) {
        testTreeCloning();
    }

    @Test
    public void testGornAddresses() {
        GornAddress root = new GornAddress(); // eps
        GornAddress sndDaughter = root.ithDaughter(1); // 1
        GornAddress thirdGrandChild = sndDaughter.ithDaughter(2); // 1.2

        // test left sisters
        Assert.assertFalse("root has no left sister", root.hasLeftSister());
        Assert.assertTrue("1 has left sister", sndDaughter.hasLeftSister());
        Assert.assertTrue("grandchild has left sister", thirdGrandChild.hasLeftSister());

        // test address lists
        Assert.assertEquals("root GA is empty", 0, root.getAddress().size());
        Assert.assertEquals("second daughter of root has address length 1", 1, sndDaughter.getAddress().size());
        Assert.assertEquals("thirdGC 1.2 has address length 2 ", 2, thirdGrandChild.getAddress().size());
    }

    /**
     * test GornAdrees.compareTo
     */
    @Test
    public void testGAcomparison() {
        GornAddress root = new GornAddress();
        GornAddress one = root.ithDaughter(0);
        GornAddress two = one.rightSister();
        GornAddress oneone = one.ithDaughter(0);

        GornAddress rootClone = new GornAddress(root);
        GornAddress oneClone = rootClone.ithDaughter(0);
        GornAddress oneoneClone = oneClone.ithDaughter(0);

        Assert.assertEquals("two roots are same", root, rootClone);
        Assert.assertEquals("two roots compared are 0", 0, root.compareTo(rootClone));
        Assert.assertEquals("two ones are same", one, oneClone);
        Assert.assertEquals("two ones compared are 0", 0, oneClone.compareTo(one));
        Assert.assertEquals("two oneones are same", oneoneClone, oneone);
        Assert.assertEquals("two oneones compared are 0", 0, oneoneClone.compareTo(oneone));
        Assert.assertTrue("one vs. root should be > 0:  ", one.compareTo(root) > 0);
        Assert.assertTrue("root vs. one should be < 0:  ", root.compareTo(one) < 0);
        Assert.assertTrue("one vs two should be < 0: ", one.compareTo(two) < 0);
        Assert.assertTrue("two vs one should be > 0: ", two.compareTo(one) > 0);
        Assert.assertTrue("oneone vs two should be < 0: ", oneone.compareTo(two) < 0);
        Assert.assertTrue("two vs oneone should be > 0: ", two.compareTo(oneone) > 0);
    }

    private static void testTreeCloning() {
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
        RRGParseTree leftsisadj = substitutedTree.sisterAdjoin(ptree2,
                new GornAddress().ithDaughter(0), 0);
        System.out.println("result: " + leftsisadj);

        System.out.println(
                "now, do the same adjunction again to see if reference stuff works properly. "
                        + "Result(1) should now have three children.");
        RRGParseTree leftsisadj2 = leftsisadj.sisterAdjoin(ptree2,
                new GornAddress().ithDaughter(0), 0);
        System.out.println("result: \n" + leftsisadj2);

        System.out.println(
                "now, take this tree and add the following tree as 2nd daughter of GA 1:\n"
                        + substitutedTree);

        RRGParseTree rightsisadj1 = leftsisadj2.sisterAdjoin(substitutedTree,
                new GornAddress().ithDaughter(0), 1);
        System.out.println("result: \n" + rightsisadj1);
    }

}
