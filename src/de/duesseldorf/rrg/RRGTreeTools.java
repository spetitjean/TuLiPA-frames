package de.duesseldorf.rrg;

import de.duesseldorf.util.GornAddress;
import de.tuebingen.tree.Node;

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

}
