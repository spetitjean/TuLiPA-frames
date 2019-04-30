package de.duesseldorf.rrg;

import de.duesseldorf.frames.Fs;
import de.duesseldorf.frames.UnifyException;
import de.duesseldorf.rrg.RRGNode.RRGNodeType;
import de.duesseldorf.util.GornAddress;
import de.tuebingen.tag.Environment;
import de.tuebingen.tree.Node;

/*
 *  File RRGTreeTools.java
 *
 *  Authors:
 *     David Arps <david.arps@hhu.de
 *     
 *  Copyright:
 *     David Arps, 2018
 *
 *  This file is part of the TuLiPA system
 *     https://github.com/spetitjean/TuLiPA-frames
 *
 *  TuLiPA is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TuLiPA is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
public class RRGTreeTools {

    /**
     * 
     * @param root
     *            the root node of the tree to be printed
     * @return A multiline string representation of <code>root</code> and its
     *         daughters
     */
    public static String recursivelyPrintNode(Node root) {
        StringBuffer sb = new StringBuffer();
        recursivelyPrintNode(root, sb, 0);
        return sb.toString();
    }

    /**
     * 
     * @param root
     *            the root node of the tree to be printed
     * @param sb
     *            The StringBuffer that is modified
     * @param sep
     *            The number of blanks needed to properly indent the nodes
     */
    private static void recursivelyPrintNode(Node root, StringBuffer sb,
            int sep) {
        // add separators
        for (int i = 0; i < sep; i++) {
            sb.append(" ");
        }
        sb.append(root.toString());
        sb.append("\n");
        for (Node node : root.getChildren()) {
            recursivelyPrintNode(node, sb, sep + 1);
        }
    }

    /**
     * 
     * @param treeRoot
     *            The root of a tree (gorn address eps) that will be modified to
     *            become the root of a tree with gorn addresses
     */
    public static void initGornAddresses(RRGNode treeRoot) {
        // base case:
        treeRoot.setGornAddress(new GornAddress());

        // recursive step:
        initGornAddressesRecursively(treeRoot);
        return;
    }

    private static void initGornAddressesRecursively(RRGNode root) {

        GornAddress motherAddress = root.getGornaddress();

        int numDaughters = root.getChildren().size();
        for (int i = 0; i < numDaughters; i++) {
            RRGNode ithDaughter = (RRGNode) root.getChildren().get(i);
            GornAddress ithDaughterAddress = motherAddress.ithDaughter(i);
            ithDaughter.setGornAddress(ithDaughterAddress);
            initGornAddressesRecursively(ithDaughter);
        }
        return;
    }

    public static String asStringWithNodeLabelsAndNodeType(RRGTree tree) {
        StringBuffer sb = new StringBuffer();
        sb.append("ID: " + tree.getId() + "\n");
        if (tree instanceof RRGParseTree) {
            ((RRGParseTree) tree).getIds().forEach((id) -> {
                sb.append(id);
                sb.append("\n");
            });
        }
        asStringWithNodeLabelsAndNodeType(tree.getRoot(), sb, 0);
        return sb.toString();
    }

    private static void asStringWithNodeLabelsAndNodeType(Node root,
            StringBuffer sb, int sep) {
        // add separators+ root.)
        for (int i = 0; i < sep; i++) {
            sb.append(".");
        }
        sb.append(((RRGNode) root).getCategory());
        sb.append(" ");
        sb.append(((RRGNode) root).getType());
        if (((RRGNode) root).getNodeFs() != null) {
            sb.append(" ");
            sb.append(
                    ((RRGNode) root).getNodeFs().toStringWithOutTypeOneLiner());
        }
        sb.append("\n");
        for (Node node : root.getChildren()) {
            asStringWithNodeLabelsAndNodeType(node, sb, sep + 1);
        }
    }

    /**
     * if both nodes don't have the same type, the type of the resulting node is
     * STD.
     * NOT symmetrical! The resulting node is built based on node1. The only
     * thing thats added are fetures tructures. The resulging nodes has the
     * childre from node1.
     * 
     * @param node1
     * @param node2
     * @return
     */
    public static RRGNode unifyNodes(RRGNode node1, RRGNode node2,
            Environment env) {
        RRGNode.Builder resultBuilder = new RRGNode.Builder(node1);
        if (!node1.getType().equals(node2.getType())) {
            resultBuilder = resultBuilder.type(RRGNodeType.STD);
        }
        if (node1.getType().equals(RRGNodeType.SUBST)
                || node2.getType().equals(RRGNodeType.SUBST)) {
            resultBuilder = resultBuilder.type(RRGNodeType.SUBST);
        }
        if (!node1.nodeUnificationPossible(node2, env)) {
            System.err.println("node unification not possible! ");
            System.err.println(node1);
            System.err.println(node2);
            return null;
        }
        try {
            Fs fsForResult = Fs.unify(node1.getNodeFs(), node2.getNodeFs(),
                    env);

            resultBuilder = resultBuilder.fs(fsForResult);
        } catch (UnifyException e) {
            System.err.println("could not unify node feature structures: ");
            System.err.println(node1);
            System.err.println(node2);
        }
        return resultBuilder.build();
    }
}
